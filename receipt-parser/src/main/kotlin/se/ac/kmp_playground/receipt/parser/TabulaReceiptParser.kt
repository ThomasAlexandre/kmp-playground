package se.ac.kmp_playground.receipt.parser

import se.ac.kmp_playground.receipt.model.*
import technology.tabula.ObjectExtractor
import technology.tabula.RectangularTextContainer
import technology.tabula.Table
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Robust receipt parser using Tabula for tables and PDFBox for text
 */
class TabulaReceiptParser {

    private val spreadsheetExtractor = SpreadsheetExtractionAlgorithm()
    private val textStripper = PDFTextStripper()

    /**
     * Parse a PDF receipt file
     */
    fun parse(pdfFile: File): ParsedReceipt {
        require(pdfFile.exists()) { "PDF file does not exist: ${pdfFile.absolutePath}" }

        PDDocument.load(pdfFile).use { document ->
            // Extract full text for header/metadata
            val fullText = textStripper.getText(document)
            val headerInfo = parseHeaderFromText(fullText)

            // Extract tables using Tabula
            val extractor = ObjectExtractor(document)
            val page = extractor.extract(1)
            val tables = spreadsheetExtractor.extract(page)

            // Parse product items from table
            val items = parseProductItems(tables, fullText)

            return ParsedReceipt(
                storeName = headerInfo.storeName,
                storeAddress = headerInfo.storeAddress,
                orgNumber = headerInfo.orgNumber,
                date = headerInfo.isoDate,
                receiptNumber = headerInfo.receiptNumber,
                items = items,
                totalAmount = headerInfo.totalAmount,
                currency = "SEK"
            )
        }
    }

    /**
     * Parse header information from extracted text
     */
    private fun parseHeaderFromText(text: String): HeaderInfo {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        var storeName = ""
        var storeAddress = ""
        var orgNumber = ""
        var date = ""
        var time = ""
        var receiptNumber = ""
        var totalAmount = ""

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            when {
                // Store name detection
                line.contains("ICA Supermarket") || line.contains("Maxi ICA") || line.contains("ICA Kvantum") -> {
                    storeName = line
                }

                // Address (usually follows store name)
                line.matches(Regex(".*\\d{5}\\s+\\w+.*")) -> {
                    // Matches lines with postal code like "16836 Bromma"
                    storeAddress = if (i > 0 && !lines[i-1].contains("ICA")) {
                        "${lines[i-1]}, $line"
                    } else {
                        line
                    }
                }

                // Date detection - look for YYYY-MM-DD pattern
                line.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                    date = line
                }

                // Time detection - look for HH:MM pattern
                line.matches(Regex("\\d{2}:\\d{2}")) -> {
                    time = line
                }

                // Org number
                line.startsWith("SE") && line.length >= 14 -> {
                    orgNumber = line
                }

                // Receipt number - usually 4 digits
                line.matches(Regex("\\d{4}")) && receiptNumber.isEmpty() && date.isNotEmpty() -> {
                    receiptNumber = line
                }

                // Total amount - look for "Betalat" line
                line.contains("Betalat") -> {
                    val amount = Regex("([\\d]+[,.]\\d{2})").find(line)?.value
                    if (amount != null) {
                        totalAmount = amount.replace(",", ".")
                    }
                }
            }
            i++
        }

        // Fallback: try to extract from filename if store name not found
        if (storeName.isEmpty()) {
            storeName = "Unknown Store"
        }

        val isoDate = buildIsoDate(date, time)

        return HeaderInfo(
            storeName = storeName,
            storeAddress = storeAddress,
            orgNumber = orgNumber,
            isoDate = isoDate,
            receiptNumber = receiptNumber,
            totalAmount = totalAmount
        )
    }

    /**
     * Parse product items from tables
     */
    private fun parseProductItems(tables: List<Table>, fullText: String): List<ReceiptItem> {
        // First, try to find and parse the main product table
        val productTable = tables.maxByOrNull { it.rows.size }

        if (productTable != null && productTable.rows.size > 1) {
            val items = parseTableRows(productTable)
            if (items.isNotEmpty()) return items
        }

        // Fallback: parse from text if table extraction failed
        return parseItemsFromText(fullText)
    }

    /**
     * Parse items from table rows
     */
    private fun parseTableRows(table: Table): List<ReceiptItem> {
        val items = mutableListOf<ReceiptItem>()

        for (row in table.rows) {
            val cells = row.mapNotNull { cell ->
                (cell as? RectangularTextContainer<*>)?.text?.trim()
            }.filter { it.isNotEmpty() }

            // Skip header row and rows with insufficient data
            if (cells.size < 3) continue
            if (cells.any { it.contains("Beskrivning") || it.contains("Artikelnummer") }) continue

            // Try to identify the structure
            // Expected: Name, ArticleNumber, Price, Quantity, Total
            val item = tryParseRow(cells)
            if (item != null) {
                items.add(item)
            }
        }

        return items
    }

    /**
     * Try to parse a row into a receipt item
     */
    private fun tryParseRow(cells: List<String>): ReceiptItem? {
        // Skip discount lines
        if (cells.any { it.contains("rabatt", ignoreCase = true) }) return null

        // Find the article number (numeric string, usually 5-13 digits)
        val articleIndex = cells.indexOfFirst { it.matches(Regex("\\d{4,13}")) }
        if (articleIndex < 0) return null

        val name = cells.subList(0, articleIndex).joinToString(" ").trim()
        val articleNumber = cells[articleIndex]

        // Find prices (numbers with decimals)
        val pricePattern = Regex("\\d+[,.]\\d{2}")
        val prices = cells.drop(articleIndex + 1)
            .mapNotNull { pricePattern.find(it)?.value?.replace(",", ".") }

        val unitPrice = prices.firstOrNull() ?: return null
        val totalPrice = prices.lastOrNull() ?: unitPrice

        // Find quantity
        val quantityCell = cells.drop(articleIndex + 1)
            .find { it.contains("st") || it.contains("kg") || it.matches(Regex("\\d+[,.]\\d+\\s*\\w*")) }

        val (quantity, unit) = parseQuantityString(quantityCell ?: "1 st")

        return ReceiptItem(
            name = name,
            articleNumber = articleNumber,
            unitPrice = unitPrice,
            quantity = quantity,
            unit = unit,
            totalPrice = totalPrice
        )
    }

    /**
     * Fallback: Parse items from raw text
     */
    private fun parseItemsFromText(text: String): List<ReceiptItem> {
        val items = mutableListOf<ReceiptItem>()
        val lines = text.split("\n")

        // Pattern: Name followed by article number, price, quantity, total
        // Example: "Amigas Brygg KRAV 7310760012537 99,95 1,00 st 99,95"
        val itemPattern = Regex(
            """(.+?)\s+(\d{4,13})\s+([\d,]+)\s+([\d,]+)\s*(st|kg)?\s+([\d,]+)"""
        )

        for (line in lines) {
            val match = itemPattern.find(line.trim())
            if (match != null) {
                val groups = match.groupValues
                items.add(
                    ReceiptItem(
                        name = groups[1].trim(),
                        articleNumber = groups[2],
                        unitPrice = groups[3].replace(",", "."),
                        quantity = groups[4].replace(",", ".").toDoubleOrNull() ?: 1.0,
                        unit = groups[5].ifEmpty { "st" },
                        totalPrice = groups[6].replace(",", ".")
                    )
                )
            }
        }

        return items
    }

    /**
     * Parse quantity string
     */
    private fun parseQuantityString(str: String): Pair<Double, String> {
        val normalized = str.replace(",", ".").trim()
        val match = Regex("([\\d.]+)\\s*(st|kg|g|l|ml)?", RegexOption.IGNORE_CASE).find(normalized)

        val quantity = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
        val unit = match?.groupValues?.get(2)?.lowercase() ?: "st"

        return quantity to unit
    }

    /**
     * Build ISO 8601 date string
     */
    private fun buildIsoDate(date: String, time: String): String {
        return try {
            val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            val localTime = if (time.isNotEmpty()) {
                LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                LocalTime.NOON
            }
            val dateTime = localDate.atTime(localTime)
            val zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Stockholm"))
            zonedDateTime.format(DateTimeFormatter.ISO_INSTANT)
        } catch (e: Exception) {
            if (date.isNotEmpty()) "${date}T12:00:00Z"
            else java.time.Instant.now().toString()
        }
    }

    private data class HeaderInfo(
        val storeName: String,
        val storeAddress: String,
        val orgNumber: String,
        val isoDate: String,
        val receiptNumber: String,
        val totalAmount: String
    )
}