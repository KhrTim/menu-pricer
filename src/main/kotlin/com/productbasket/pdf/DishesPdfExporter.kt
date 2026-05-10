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

fun exportDishesPdf(
    out: OutputStream,
    dishes: List<Dish>,
    productsById: Map<UUID, Product>,
    includeIngredients: Boolean
) {
    val doc = Document(PageSize.A4, 40f, 40f, 40f, 40f)
    val writer = PdfWriter.getInstance(doc, out)
    doc.open()

    val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f)
    val headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f)
    val cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9f)
    val smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8f)

    val title = Paragraph("Меню — Стоимость блюд\n${LocalDate.now().format(DATE_FMT)}", titleFont)
    title.alignment = Element.ALIGN_CENTER
    doc.add(title)
    doc.add(Paragraph(" "))

    val colWidths = floatArrayOf(3f, 2f, 1f, 1.5f, 1.5f)
    val table = PdfPTable(colWidths.size).apply {
        widthPercentage = 100f
        setWidths(colWidths)
    }

    fun headerCell(text: String) = PdfPCell(Phrase(text, headerFont)).apply {
        backgroundColor = Color(220, 220, 220)
        paddingTop = 4f; paddingBottom = 4f
        horizontalAlignment = Element.ALIGN_CENTER
    }
    fun dataCell(text: String, align: Int = Element.ALIGN_LEFT) = PdfPCell(Phrase(text, cellFont)).apply {
        paddingTop = 3f; paddingBottom = 3f
        horizontalAlignment = align
    }

    table.addCell(headerCell("Блюдо"))
    table.addCell(headerCell("Категория"))
    table.addCell(headerCell("Порций"))
    table.addCell(headerCell("Цена/порция"))
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
                paddingLeft = 20f
                paddingBottom = 4f
                border = Rectangle.BOTTOM
            }
            val ingText = dish.ingredients.joinToString("  |  ") { ing ->
                val p = productsById[ing.productId]
                val name = p?.name ?: "(удалён)"
                "${ing.quantity.let { if (it == it.toLong().toDouble()) it.toLong() else it }} ${ing.unit.display} $name"
            }
            ingCell.addElement(Phrase(ingText, smallFont))
            table.addCell(ingCell)
        }
    }

    doc.add(table)
    doc.close()
}
