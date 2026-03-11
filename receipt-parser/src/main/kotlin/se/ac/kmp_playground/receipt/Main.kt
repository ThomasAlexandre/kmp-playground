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
import io.ktor.client.call.*
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
import se.ac.kmp_playground.receipt.model.ParsedReceipt
import se.ac.kmp_playground.receipt.model.PriceApiRequest
import se.ac.kmp_playground.receipt.parser.HemkopReceiptParser
import se.ac.kmp_playground.receipt.parser.TabulaReceiptParser
import java.io.File

/**
 * Supported receipt parser types
 */
enum class ParserType {
    ICA,
    HEMKOP;

    companion object {
        fun fromString(value: String): ParserType = when (value.lowercase()) {
            "ica", "tabula" -> ICA
            "hemkop", "hemköp", "axfood" -> HEMKOP
            else -> throw IllegalArgumentException("Unknown parser type: $value. Supported: ica, hemkop")
        }
    }
}

/**
 * Interface for receipt parsers
 */
interface ReceiptParser {
    fun parse(file: File): ParsedReceipt
}

/**
 * Wrapper to make TabulaReceiptParser implement the interface
 */
class IcaReceiptParser : ReceiptParser {
    private val parser = TabulaReceiptParser()
    override fun parse(file: File): ParsedReceipt = parser.parse(file)
}

/**
 * Wrapper to make HemkopReceiptParser implement the interface
 */
class HemkopReceiptParserWrapper : ReceiptParser {
    private val parser = HemkopReceiptParser()
    override fun parse(file: File): ParsedReceipt = parser.parse(file)
}

/**
 * Factory to create the appropriate parser
 */
fun createParser(type: ParserType): ReceiptParser = when (type) {
    ParserType.ICA -> IcaReceiptParser()
    ParserType.HEMKOP -> HemkopReceiptParserWrapper()
}

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

    private val storeId by option("--store-id", help = "Override store ID for product mapping lookups")

    private val parserType by option("-p", "--parser", help = "Parser type: ica (default), hemkop")
        .default("ica")

    override fun run() {
        val parser = createParser(ParserType.fromString(parserType))
        echo("Using parser: ${parserType.uppercase()}")

        outputDir?.mkdirs()

        for (file in files) {
            echo("Parsing: ${file.name}")

            try {
                val receipt = parser.parse(file)
                echo("  Store: ${receipt.storeName}")
                echo("  Date: ${receipt.date}")
                echo("  Items: ${receipt.items.size}")

                val outputJson = if (priceApiFormat) {
                    // Use product mappings for ICA receipts to resolve artikelnummer to barcodes
                    val useProductMappings = ParserType.fromString(parserType) == ParserType.ICA
                    val requests = receipt.toPriceApiRequests(
                        useProductMappings = useProductMappings,
                        storeIdOverride = storeId
                    )
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

    private val parserType by option("-p", "--parser", help = "Parser type: ica (default), hemkop")
        .default("ica")

    override fun run() = runBlocking {
        val parser = createParser(ParserType.fromString(parserType))
        echo("Using parser: ${parserType.uppercase()}")

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

                // Use product mappings for ICA receipts to resolve artikelnummer to barcodes
                val useProductMappings = ParserType.fromString(parserType) == ParserType.ICA
                val finalRequests = receipt.toPriceApiRequests(
                    useProductMappings = useProductMappings,
                    storeIdOverride = storeId
                )

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
    private val apiUrl by option("--api-url")
        .default("https://thomasalexandre.unison-services.cloud/s/stores-api")

    override fun run() = runBlocking {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        echo("Fetching stores from $apiUrl...")
        try {
            val response = client.get("$apiUrl/stores")
            if (response.status.isSuccess()) {
                val stores = response.body<List<se.ac.kmp_playground.receipt.model.Store>>()
                echo("Known stores:")
                stores.forEach { store ->
                    echo("  ${store.key} -> Store ID: ${store.id} (${store.name})")
                }
            } else {
                echo("Failed to fetch stores: ${response.status}", err = true)
            }
        } catch (e: Exception) {
            echo("Error fetching stores: ${e.message}", err = true)
        } finally {
            client.close()
        }
    }
}

fun main(args: Array<String>) = ReceiptParserCli()
    .subcommands(ParseCommand(), UploadCommand(), ListStoresCommand())
    .main(args)