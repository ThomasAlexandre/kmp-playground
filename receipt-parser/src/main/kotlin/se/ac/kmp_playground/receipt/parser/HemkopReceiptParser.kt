package se.ac.kmp_playground.receipt.parser

import se.ac.kmp_playground.receipt.model.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Receipt parser for Hemköp/Axfood stores (Hemköp, Willys, Tempo, etc.)
 *
 * Key differences from ICA receipts:
 * - No article numbers/barcodes - products identified by name only
 * - Simpler item format: PRODUCT NAME followed by price
 * - Weight items: X,XXXkg*YYY,YYkr/kg format
 * - Quantity items: Xst*YY,YY format
 */
class HemkopReceiptParser {

    private val textStripper = PDFTextStripper()

    /**
     * Parse a Hemköp PDF receipt file
     */
    fun parse(pdfFile: File): ParsedReceipt {
        require(pdfFile.exists()) { "PDF file does not exist: ${pdfFile.absolutePath}" }

        PDDocument.load(pdfFile).use { document ->
            val fullText = textStripper.getText(document)
            val lines = fullText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

            val headerInfo = parseHeader(lines)
            val items = parseItems(lines)

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
     * Parse header information
     */
    private fun parseHeader(lines: List<String>): HeaderInfo {
        var storeName = ""
        var storeAddress = ""
        var orgNumber = ""
        var date = ""
        var time = ""
        var receiptNumber = ""
        var totalAmount = ""

        for (i in lines.indices) {
            val line = lines[i]

            when {
                // First non-empty line is usually store name
                storeName.isEmpty() && !line.contains("---") && !line.matches(Regex(".*\\d{5}.*")) -> {
                    storeName = line
                }

                // Address with postal code (but NOT payment terminal lines)
                // Must be like "Sobelgränd 4, 167 58 BROMMA" - not "Ref: 200452150477"
                line.matches(Regex(".*,\\s*\\d{3}\\s*\\d{2}\\s+[A-ZÄÖÅØ]+.*")) -> {
                    storeAddress = line
                }

                // Org number - various formats
                line.contains("Orgnr:") || line.contains("Org:") -> {
                    val match = Regex("\\d{6}-\\d{4}").find(line)
                    if (match != null) {
                        orgNumber = match.value
                    }
                }

                // Date/time in format YYYY-MM-DD HH:MM:SS or YYYY-MM-DD HH:MM
                line.matches(Regex(".*\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}.*")) -> {
                    val dateMatch = Regex("(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2})").find(line)
                    if (dateMatch != null) {
                        date = dateMatch.groupValues[1]
                        time = dateMatch.groupValues[2]
                    }
                }

                // Total amount - look for "Totalt" followed by amount
                line.startsWith("Totalt") && line.contains("SEK") -> {
                    val match = Regex("([\\d,]+)\\s*SEK").find(line)
                    if (match != null) {
                        totalAmount = match.groupValues[1].replace(",", ".")
                    }
                }

                // Kassa line often has date/time at the end
                line.startsWith("Kassa:") -> {
                    val dateMatch = Regex("(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2})").find(line)
                    if (dateMatch != null && date.isEmpty()) {
                        date = dateMatch.groupValues[1]
                        time = dateMatch.groupValues[2]
                    }
                    // Extract receipt number from kassa line
                    val kassaMatch = Regex("Kassa:\\s*([\\d/]+)").find(line)
                    if (kassaMatch != null) {
                        receiptNumber = kassaMatch.groupValues[1]
                    }
                }
            }
        }

        val isoDate = buildIsoDate(date, time)

        return HeaderInfo(
            storeName = storeName.ifEmpty { "Hemköp" },
            storeAddress = storeAddress,
            orgNumber = orgNumber,
            isoDate = isoDate,
            receiptNumber = receiptNumber,
            totalAmount = totalAmount
        )
    }

    /**
     * Parse item lines from receipt
     */
    private fun parseItems(lines: List<String>): List<ReceiptItem> {
        val items = mutableListOf<ReceiptItem>()

        // Find the items section (between first "---" and "Totalt")
        var inItemsSection = false
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            // Start of items section
            if (line.contains("---") && !inItemsSection) {
                inItemsSection = true
                i++
                continue
            }

            // End of items section
            if (inItemsSection && (line.startsWith("Totalt") || line.contains("---"))) {
                break
            }

            if (inItemsSection) {
                // Skip discount lines (negative prices)
                if (line.contains("Klubbpris") || line.startsWith("-") ||
                    (line.contains("-") && Regex("-\\d+[,.]\\d{2}").containsMatchIn(line))) {
                    i++
                    continue
                }

                // Try to parse item
                val item = tryParseItem(lines, i)
                if (item != null) {
                    items.add(item.first)
                    i = item.second // Move to next unparsed line
                } else {
                    i++
                }
            } else {
                i++
            }
        }

        return items
    }

    /**
     * Try to parse an item starting at the given line index
     * Returns pair of (ReceiptItem, nextLineIndex) or null if not an item
     */
    private fun tryParseItem(lines: List<String>, startIndex: Int): Pair<ReceiptItem, Int>? {
        val line = lines[startIndex]

        // Skip empty or separator lines
        if (line.isEmpty() || line.contains("---")) return null

        // Pattern 1: Simple item - "PRODUCT NAME    XX,XX"
        // Allow more characters in product names including %, -, special chars
        val simplePattern = Regex("^([A-ZÄÖÅØÉÊÈÔ][A-ZÄÖÅØÉÊÈÔ0-9\\s&/%\\-\\.]+?)\\s+(\\d+[,.]\\d{2})$")
        val simpleMatch = simplePattern.find(line)
        if (simpleMatch != null) {
            val name = simpleMatch.groupValues[1].trim()
            val price = simpleMatch.groupValues[2].replace(",", ".")
            return Pair(
                ReceiptItem(
                    name = name,
                    articleNumber = generateProductCode(name),
                    unitPrice = price,
                    quantity = 1.0,
                    unit = "st",
                    totalPrice = price
                ),
                startIndex + 1
            )
        }

        // Pattern 2: Weight item - name on one line, weight*price on next
        // "PRODUCT NAME"
        // "    X,XXXkg*YYY,YYkr/kg    ZZ,ZZ"
        if (startIndex + 1 < lines.size) {
            val nextLine = lines[startIndex + 1]
            val weightPattern = Regex("([\\d,]+)kg\\*([\\d,]+)kr/kg\\s+([\\d,]+)")
            val weightMatch = weightPattern.find(nextLine)

            // Allow product names with special characters like ENTRECÔTE
            if (weightMatch != null && line.matches(Regex("^[A-ZÄÖÅØÉÊÈÔ][A-ZÄÖÅØÉÊÈÔ0-9\\s&/%\\-\\.]*$"))) {
                val name = line.trim()
                val weight = weightMatch.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 1.0
                val pricePerKg = weightMatch.groupValues[2].replace(",", ".")
                val totalPrice = weightMatch.groupValues[3].replace(",", ".")

                return Pair(
                    ReceiptItem(
                        name = name,
                        articleNumber = generateProductCode(name),
                        unitPrice = pricePerKg,
                        quantity = weight,
                        unit = "kg",
                        totalPrice = totalPrice
                    ),
                    startIndex + 2
                )
            }
        }

        // Pattern 3: Quantity item - "PRODUCT NAME    Xst*YY,YY    ZZ,ZZ"
        val qtyPattern = Regex("^([A-ZÄÖÅØÉÊÈÔ][A-ZÄÖÅØÉÊÈÔ0-9\\s&/%\\-\\.]+?)\\s+(\\d+)st\\*([\\d,]+)\\s+([\\d,]+)$")
        val qtyMatch = qtyPattern.find(line)
        if (qtyMatch != null) {
            val name = qtyMatch.groupValues[1].trim()
            val qty = qtyMatch.groupValues[2].toDoubleOrNull() ?: 1.0
            val unitPrice = qtyMatch.groupValues[3].replace(",", ".")
            val totalPrice = qtyMatch.groupValues[4].replace(",", ".")

            return Pair(
                ReceiptItem(
                    name = name,
                    articleNumber = generateProductCode(name),
                    unitPrice = unitPrice,
                    quantity = qty,
                    unit = "st",
                    totalPrice = totalPrice
                ),
                startIndex + 1
            )
        }

        // Pattern 4: Just a number code with price (store internal code)
        // "988402 45,37"
        val codePattern = Regex("^(\\d{4,8})\\s+(\\d+[,.]\\d{2})$")
        val codeMatch = codePattern.find(line)
        if (codeMatch != null) {
            val code = codeMatch.groupValues[1]
            val price = codeMatch.groupValues[2].replace(",", ".")
            return Pair(
                ReceiptItem(
                    name = "Unknown ($code)",
                    articleNumber = code,
                    unitPrice = price,
                    quantity = 1.0,
                    unit = "st",
                    totalPrice = price
                ),
                startIndex + 1
            )
        }

        // Pattern 5: Product name only on this line, price may be elsewhere
        // Check if it's just a product name (all caps, no numbers at end)
        if (line.matches(Regex("^[A-ZÄÖÅØÉÊÈÔ][A-ZÄÖÅØÉÊÈÔ0-9\\s&/%\\-\\.]+$")) && !line.contains("Totalt")) {
            // This might be a product name for a weighted item - peek at next line
            if (startIndex + 1 < lines.size) {
                val nextLine = lines[startIndex + 1]
                // If next line has weight info, it will be caught by Pattern 2
                // Otherwise skip this standalone name line
            }
        }

        return null
    }

    /**
     * Generate a store-specific product code from the product name
     * Since Hemköp doesn't provide barcodes, we create a deterministic code
     */
    private fun generateProductCode(name: String): String {
        // Normalize the name and create a hash-based code
        // Handle special Swedish characters and accented letters
        val normalized = name.uppercase()
            .replace('É', 'E')
            .replace('Ê', 'E')
            .replace('È', 'E')
            .replace('Ô', 'O')
            .replace(Regex("[^A-ZÄÖÅØ0-9]"), "")
            .take(20)

        // Use a simple hash to create a numeric code
        val hash = normalized.hashCode().let { if (it < 0) -it else it }
        return "HK${hash.toString().take(8).padStart(8, '0')}"
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