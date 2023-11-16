import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private val REGEX_SEARCH = """
kz\.(jusan|jysan)\..*?\.R\.\w+\.\w+
"""

private val IMPORT_ALIASES = mapOf(
    "kz.jysan.business.card" to "CardR",
    "kz.jysan.business.localization" to "LocalizationR",
    "kz.jysan.business.core.ui" to "CoreUiR",
    "kz.jysan.business.deposit" to "DepositR",
    "kz.jysan.business.home" to "HomeR",
    "kz.jusan.business.accounting" to "AccountingR",
    "kz.jysan.business.credits.presentation" to "CreditsPresentationR",
    "kz.jysan.business.splash" to "SplashR",
    "kz.jysan.business.statements" to "StatementsR",
    "kz.jusan.accounting.sdk" to "AccountingSdkR",
    "kz.jusan.feature.tole.result" to "ToleResultR",
    "kz.jysan.business.face_tec" to "FaceTecR",
    "kz.jusan.business.auth" to "AuthR",
    "kz.jysan.business.special.offer" to "SpecialOfferR",
    "kz.jysan.business.transfers" to "TransfersR",
    "kz.jysan.business.hint" to "HintR",
    "kz.jysan.business.tariffs" to "TariffsR",
    "kz.jysan.business.documents" to "DocumentsR",
    "kz.jusan.business.access.control" to "AccessControlR",
    "kz.jysan.business.oneClickTax" to "OneClickTaxR",
    "kz.jusan.business.registration.individual" to "RegistrationIndividualR",
    "kz.jusan.business.feature.r2p.visa" to "R2pVisaR",
    "kz.jysan.business.letters" to "LettersR",
    "kz.jusan.business.more" to "MoreR",
)

fun main() {
    val projectPath = "C:\\Users\\15010\\AndroidStudioProjects\\jysan-android"

    val script = FullyQualifiedResourceToImportAlias(projectPath)
    IMPORT_ALIASES.forEach { (packageName, rReplacement) ->
        script.run(packageName, rReplacement)
    }
}

private class FullyQualifiedResourceToImportAlias(
    private val projectPath: String,
) {
    private val packageRegex = "package ([a-zA-Z0-9_.]+)".toRegex()

    @SuppressLint("NewApi")
    fun run(packageName: String, rReplacement: String) {
        println("Looking for $packageName.R usages")

        val rRegex = "($packageName\\.R)\\.".toRegex()

        File("$projectPath/").walk()
            .filter {
                !it.isDirectory && it.isKotlinFile()
            }
            .forEach { file ->
                val shouldAddImport = refactorFqNames(file, rRegex, rReplacement)

                if (shouldAddImport) {
                    addImport(file, packageName, rReplacement)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refactorFqNames(file: File, rRegex: Regex, rReplacement: String): Boolean {
        val br = file.bufferedReader()
        var writeFile = false

        val newFile = File(file.absolutePath + "_tmp").apply {
            createNewFile()
        }

        newFile.bufferedWriter().use { bw ->
            var line = br.readLine()
            while (line != null) {
                val matches = rRegex.findAll(line).toList().reversed()
                matches.forEach {
                    val (prefix) = it.destructured

                    if (prefix != "R") {
                        line = line.replaceRange(it.groups[1]!!.range, rReplacement)

                        writeFile = true
                    }
                }

                bw.write(line)
                line = br.readLine()
                if (line != null) {
                    bw.newLine()
                }
            }
        }
        br.close()

        if (writeFile) {
            println("Refactoring ${file.name}")
            Files.move(newFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } else {
            newFile.delete()
        }
        return writeFile
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addImport(file: File, packageName: String, rReplacement: String) {
        val newestFile = File(file.absolutePath + "_tmp1").apply {
            createNewFile()
        }

        newestFile.bufferedWriter().use { bw ->
            file.bufferedReader().use { bufferedReader ->
                var matchFound = false
                var line = bufferedReader.readLine()
                while (line != null) {
                    if (!matchFound) {
                        val match = packageRegex.find(line)
                        if (match != null) {
                            matchFound = true
                            bw.write(match.value)
                            bw.newLine()
                            bw.newLine()
                            line = "import $packageName.R as $rReplacement"
                        }
                    }

                    bw.write(line)
                    line = bufferedReader.readLine()
                    bw.newLine()
                }
            }
        }

        Files.move(newestFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    private fun File.isKotlinFile() = name.endsWith(".kt")
}