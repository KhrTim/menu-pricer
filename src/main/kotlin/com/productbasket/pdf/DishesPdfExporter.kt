package com.productbasket.pdf

import com.lowagie.text.*
import com.lowagie.text.pdf.*
import com.productbasket.domain.*
import com.productbasket.ui.common.formatMoney
import java.awt.Color
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

private val DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy")

// Standard PDF Type1 fonts only cover Latin. For reliable Cyrillic rendering we
// use a system TrueType font with IDENTITY_H encoding (full Unicode). This tries
// common system font locations; falls back to Helvetica if none is found (in which
// case a PDF viewer will do its own substitution, which usually works fine on
// modern systems but may fail on old ones).
private fun cyrillicFont(sizePt: Float, bold: Boolean = false): Font {
    val candidates = listOf(
        // Windows
        "C:/Windows/Fonts/${if (bold) "arialbd" else "arial"}.ttf",
        "C:/Windows/Fonts/${if (bold) "calibrib" else "calibri"}.ttf",
        // macOS
        "/System/Library/Fonts/Supplemental/Arial${if (bold) " Bold" else ""}.ttf",
        "/Library/Fonts/Arial${if (bold) " Bold" else ""}.ttf",
        // Linux
        "/usr/share/fonts/truetype/liberation/LiberationSans-${if (bold) "Bold" else "Regular"}.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans${if (bold) "-Bold" else ""}.ttf",
    )
    for (path in candidates) {
        try {
            val bf = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
            return Font(bf, sizePt)
        } catch (_: Exception) { }
    }
    // Fallback — works for Latin; PDF viewer substitutes Cyrillic
    val style = if (bold) Font.BOLD else Font.NORMAL
    return FontFactory.getFont(FontFactory.HELVETICA, sizePt, style)
}

fun exportDishesPdf(
    out: OutputStream,
    dishes: List<Dish>,
    productsById: Map<UUID, Product>,
    includeIngredients: Boolean
) {
    val doc = Document(PageSize.A4, 40f, 40f, 40f, 40f)
    PdfWriter.getInstance(doc, out)
    doc.open()

    val titleFont  = cyrillicFont(14f, bold = true)
    val headerFont = cyrillicFont(10f, bold = true)
    val cellFont   = cyrillicFont(9f)
    val smallFont  = cyrillicFont(8f)

    val title = Paragraph("Меню — Стоимость блюд\n${LocalDate.now().format(DATE_FMT)}", titleFont)
    title.alignment = Element.ALIGN_CENTER
    doc.add(title)
    doc.add(Paragraph(" "))

    val colWidths = floatArrayOf(3f, 2f, 1f, 1.5f, 1.5f)
    val table = PdfPTable(colWidths.size).apply {
        widthPercentage = 100f
        setWidths(colWidths)
        setHeaderRows(1)   // repeat header row on every page
    }

    fun headerCell(text: String) = PdfPCell(Phrase(text, headerFont)).apply {
        backgroundColor = Color(200, 210, 230)
        setPadding(5f)
        horizontalAlignment = Element.ALIGN_CENTER
        verticalAlignment = Element.ALIGN_MIDDLE
    }
    fun dataCell(text: String, align: Int = Element.ALIGN_LEFT) = PdfPCell(Phrase(text, cellFont)).apply {
        setPadding(4f)
        horizontalAlignment = align
    }

    table.addCell(headerCell("Блюдо"))
    table.addCell(headerCell("Категория"))
    table.addCell(headerCell("Порций"))
    table.addCell(headerCell("Цена / порция"))
    table.addCell(headerCell("Итого"))

    for (dish in dishes) {
        val total = dishCost(dish, productsById)
        val perPortion = if (dish.portions > 0) total / dish.portions else 0.0
        table.addCell(dataCell(dish.name))
        table.addCell(dataCell(dish.category.display, Element.ALIGN_CENTER))
        table.addCell(dataCell(dish.portions.toString(), Element.ALIGN_CENTER))
        table.addCell(dataCell(formatMoney(perPortion), Element.ALIGN_RIGHT))
        table.addCell(dataCell(formatMoney(total), Element.ALIGN_RIGHT))

        if (includeIngredients && dish.ingredients.isNotEmpty()) {
            val ingCell = PdfPCell().apply {
                colspan = 5
                setPaddingLeft(20f)
                setPaddingBottom(5f)
                setPaddingTop(2f)
                border = Rectangle.BOTTOM
            }
            val ingText = dish.ingredients.joinToString("   |   ") { ing ->
                val p = productsById[ing.productId]
                val name = p?.name ?: "(удалён)"
                val qty = ing.quantity.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
                "$qty ${ing.unit.display} $name"
            }
            ingCell.addElement(Phrase(ingText, smallFont))
            table.addCell(ingCell)
        }
    }

    doc.add(table)
    doc.close()
}
