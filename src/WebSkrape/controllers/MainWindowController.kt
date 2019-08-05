package WebSkrape.controllers

import WebSkrape.MainTornado
import WebSkrape.models.SkrapeResult
import com.google.i18n.phonenumbers.PhoneNumberUtil
import javafx.application.Platform
import org.jsoup.Jsoup
import org.nibor.autolink.LinkExtractor
import org.nibor.autolink.LinkType
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import tornadofx.Controller
import tornadofx.FXTask
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet



import javax.swing.*
import kotlin.properties.Delegates
import kotlin.reflect.jvm.javaMethod

class MainWindowController : Controller(){
    val urlListPath = "${System.getenv("USERPROFILE")}\\url_list.txt"
    val urlTempPath = "${System.getenv("USERPROFILE")}\\temp.txt"

    val numberList = ConcurrentSkipListSet<String>()
    val emailList = ConcurrentSkipListSet<String>()
    val urlList = ConcurrentSkipListSet<String>()

    var sentinelControl: Boolean = false

    fun skrape(url: String, mode: String, uiFunc: (List<String>, List<String>) -> Unit): SkrapeResult{
       val result = runAsync {
           val doc = Jsoup.connect(url).get()
           val phoneUtil = PhoneNumberUtil.getInstance()

           val hrefs: List<String> = (doc.select("a[href]") +
                   doc.select("link[href]"))
                   .map {
                       it.attr("href")
                   }.filter {
                       it.startsWith("http://")
                       ||it.startsWith("https://")
                   }.distinct()

           val bodyString = doc.body().allElements.text().toString()

           val numbers = phoneUtil
                   .findNumbers(bodyString,
                           "US")
                   .map {
                       "${it.number().nationalNumber}"
                   }.distinct()

           val emails = LinkExtractor.builder()
                   .linkTypes(EnumSet.of(LinkType.EMAIL))
                   .build()
                   .extractLinks(bodyString)
                   .map {
                       doc.body()
                               .allElements
                               .text()
                               .toString()
                               .substring(it.beginIndex, it.endIndex)
                   }.distinct()

           val scrape = SkrapeResult(numbers, emails, hrefs)
           hrefs.forEach{ println(it)}

           if (mode==Modes.MODE_SINGLE){
               CSVWriter.writeResults(scrape)
               CSVWriter.writeUrls(scrape.urls)
           }else if(mode==Modes.MODE_CONTINUOUS){
               numberList.addAll(scrape.phoneNumbers)
               emailList.addAll(scrape.emailAddrs)
               urlList.addAll(scrape.urls)
               if (urlList.size>=200){
                   CSVWriter.writeUrls(urlList.toList())
                   urlList.clear()
               }
               if (numberList.size>=10||emailList.size>=10){
                   CSVWriter.writeResults(SkrapeResult(numberList.toList(), emailList.toList(), listOf()))
               }
           }

           scrape
        }ui {
           uiFunc(it.emailAddrs, it.phoneNumbers)
       }
       return result.get()
    }

    fun crawl(uiFunc: (List<String>, List<String>) -> Unit){
        println("crawl function called...")
        var loop = 0
        sentinelControl = true
        val fileScanner = Scanner(File(urlTempPath))
        while (sentinelControl){
            println("loop iter: $loop")
            fileScanner.nextLine()
                    .let {
                        skrape(it, Modes.MODE_CONTINUOUS, uiFunc)
                    }
        }
    }

    fun rxCrawl(urls: List<String>, uiFunc: (List<String>, List<String>) -> Unit){

        Observable.from(urls)
                .map { url ->

                    try {
                        val doc = Jsoup.connect(url).get()
                        val phoneUtil = PhoneNumberUtil.getInstance()

                        val hrefs: List<String> = (doc.select("a[href]") +
                                doc.select("link[href]"))
                                .map {
                                    it.attr("href")
                                }.filter {
                                    it.startsWith("http://")
                                            ||it.startsWith("https://")
                                }.distinct()

                        val bodyString = doc.body().allElements.text().toString()

                        val numbers = phoneUtil
                                .findNumbers(bodyString,
                                        "US")
                                .map {
                                    "${it.number().nationalNumber}"
                                }.distinct()

                        val emails = LinkExtractor.builder()
                                .linkTypes(EnumSet.of(LinkType.EMAIL))
                                .build()
                                .extractLinks(bodyString)
                                .map {
                                    doc.body()
                                            .allElements
                                            .text()
                                            .toString()
                                            .substring(it.beginIndex, it.endIndex)
                                }.distinct()
                        Optional.of(SkrapeResult(numbers, emails, urls))
                    }catch (e: Exception){
                        e.printStackTrace()
                        Optional.empty<SkrapeResult>()
                    }

                    //SkrapeResult(numbers, emails, urls)
                }.observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe(SkrapeSubScriber(uiFunc))
    }

}

class SkrapeSubScriber(val uiFunc: (List<String>, List<String>) -> Unit): Subscriber<Optional<SkrapeResult>>(){
    override fun onError(e: Throwable) {
        e.printStackTrace()
    }

    override fun onNext(result: Optional<SkrapeResult>) {
        if (result.isPresent){
            Platform.runLater {
                uiFunc(result.get().emailAddrs, result.get().phoneNumbers)
            }}
        //Platform.runLater { uiFunc(result.emailAddrs, result.phoneNumbers) }
    }

    override fun onCompleted() {
        //OUT FILE TO BE OVERWITTEN WITH CONTENTS OF TEMP FILE
        println("testing complete!")
    }

}

class Modes{
    companion object {
        val MODE_SINGLE = "SINGLE"
        val MODE_CONTINUOUS = "CONTINUOUS"
    }
}
