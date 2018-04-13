package WebSkrape.controllers

import WebSkrape.models.SkrapeResult
import java.io.File
import java.io.PrintWriter
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

object CSVWriter{
    val user_dir = System.getenv().get("USERPROFILE")
    val urls_out = "$user_dir\\temp.txt"

    @Throws(CSVException::class)
    fun writeResults(scrape: SkrapeResult){
        val numbers_out = "$user_dir\\numbers_out.txt"
        val email_out = "$user_dir\\email_out.txt"

        fun out(values: List<String>, path: String, append: Boolean){
            val file = File(path)
            val writer = PrintWriter(FileWriter(file, append))

            values.distinct().forEach{
                writer.write("$it,\r\n")
                writer.flush()
            }

            writer.close()

            values.distinct().forEach{ println(it)}
        }

        when(Files.exists(Paths.get(numbers_out))){
            true -> {out(scrape.phoneNumbers, numbers_out, true)}
            false -> {
                Files.createFile(Paths.get(numbers_out))
                out(scrape.phoneNumbers, numbers_out, false)
            }
        }

        when(Files.exists(Paths.get(email_out))){
            true -> {out(scrape.emailAddrs, email_out, true)}
            false -> {
                Files.createFile(Paths.get(email_out))
                out(scrape.emailAddrs, email_out, false)
            }
        }

    }

    @Throws(CSVException::class)
    fun writeUrls(webAddrs: List<String>){
        val append = Files.exists(Paths.get(urls_out))

        val file = File(urls_out)
        val writer = PrintWriter(FileWriter(file, append))

        webAddrs.distinct().forEach{
            writer.write("${it}\r\n")
            writer.flush()
        }

        writer.close()
    }

}//END CSVWRITER

class CSVException: IOException()