package se.ac.kmp_playground.receipt

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import se.ac.kmp_playground.receipt.converter.toPriceApiRequests
import se.ac.kmp_playground.receipt.model.PriceApiRequest
import se.ac.kmp_playground.receipt.parser.TabulaReceiptParser
import java.io.File

val json = Json {
    prettyPrint = true
    encodeDefaults = true
}

class ReceiptParserCli : CliktCommand(name = "receipt-parser") {
    override fun run() = Unit
}

class ParseCommand : CliktCommand(name = "parse") {
    private val files by argument()
        .file(mustExist = true, canBeDir = false)
        .multiple(required = true)

    private val outputDir by option("-o", "--output")
        .file(canBeFile = false)

    private val priceApiFormat by option("--price-api")
        .flag(default = false)

    override fun run() {
        val parser = TabulaReceiptParser()

        outputDir?.mkdirs()

        for (file in files) {
            echo("Parsing: ${file.name}")

            try {
                val receipt = parser.parse(file)
                echo("  Store: ${receipt.storeName}")
                echo("  Date: ${receipt.date}")
                echo("  Items: ${receipt.items.size}")

                val outputJson = if (priceApiFormat) {
                    val requests = receipt.toPriceApiRequests()
                    json.encodeToString(requests)
                } else {
                    json.encodeToString(receipt)
                }

                if (outputDir != null) {
                    val outputFile = File(outputDir, "${file.nameWithoutExtension}.json")
                    outputFile.writeText(outputJson)
                    echo("  Written to: ${outputFile.absolutePath}")
                } else {
                    echo(outputJson)
                }
            } catch (e: Exception) {
                echo("  Error: ${e.message}", err = true)
            }

            echo("")
        }
    }
}

class UploadCommand : CliktCommand(name = "upload") {
    private val files by argument()
        .file(mustExist = true, canBeDir = false)
        .multiple(required = true)

    private val apiUrl by option("--api-url")
        .default("https://thomasalexandre.unison-services.cloud/s/prices-api")

    private val dryRun by option("--dry-run")
        .flag(default = false)

    private val storeId by option("--store-id")

    override fun run() = runBlocking {
        val parser = TabulaReceiptParser()

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { encodeDefaults = true })
            }
        }

        for (file in files) {
            echo("Processing: ${file.name}")

            try {
                val receipt = parser.parse(file)
                echo("  Store: ${receipt.storeName}")
                echo("  Date: ${receipt.date}")
                echo("  Items: ${receipt.items.size}")

                val requests = receipt.toPriceApiRequests()

                // Apply store ID override if specified
                val finalRequests = if (storeId != null) {
                    requests.map { it.copy(storeId = storeId!!) }
                } else {
                    requests
                }

                if (dryRun) {
                    echo("  Would upload ${finalRequests.size} price records:")
                    finalRequests.forEach { req ->
                        echo("    - ${req.product.code}: ${req.price.price} ${req.price.currency}")
                    }
                } else {
                    uploadPrices(client, apiUrl, finalRequests)
                }

            } catch (e: Exception) {
                echo("  Error: ${e.message}", err = true)
            }

            echo("")
        }

        client.close()
    }

    private suspend fun uploadPrices(
        client: HttpClient,
        baseUrl: String,
        requests: List<PriceApiRequest>
    ) {
        var successCount = 0
        var errorCount = 0

        for (request in requests) {
            try {
                val response = client.post("$baseUrl/prices") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                if (response.status.isSuccess()) {
                    successCount++
                    echo("    ✓ ${request.product.code}: ${request.price.price} ${request.price.currency}")
                } else {
                    errorCount++
                    echo("    ✗ ${request.product.code}: ${response.status} - ${response.bodyAsText()}", err = true)
                }
            } catch (e: Exception) {
                errorCount++
                echo("    ✗ ${request.product.code}: ${e.message}", err = true)
            }
        }

        echo("  Uploaded: $successCount success, $errorCount errors")
    }
}

class ListStoresCommand : CliktCommand(name = "list-stores") {
    override fun run() {
        echo("Known store mappings:")
        se.ac.kmp_playground.receipt.converter.PriceApiConverter.DEFAULT_STORE_MAPPING.forEach { (key, value) ->
            echo("  $key -> Store ID: $value")
        }
    }
}

fun main(args: Array<String>) = ReceiptParserCli()
    .subcommands(ParseCommand(), UploadCommand(), ListStoresCommand())
    .main(args)