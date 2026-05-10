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
// use a system TrueType font with IDENTITY_H encoding (full Unicode).
private fun cyrillicFont(sizePt: Float, bold: Boolean = false): Font {
    val candidates = listOf(
        "C:/Windows/Fonts/${if (bold) "arialbd" else "arial"}.ttf",
        "C:/Windows/Fonts/${if (bold) "calibrib" else "calibri"}.ttf",
        "/System/Library/Fonts/Supplemental/Arial${if (bold) " Bold" else ""}.ttf",
        "/Library/Fonts/Arial${if (bold) " Bold" else ""}.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-${if (bold) "Bold" else "Regular"}.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans${if (bold) "-Bold" else ""}.ttf",
    )
    for (path in candidates) {
        try {
            val bf = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
            return Font(bf, sizePt)
        } catch (_: Exception) { }
    }
    val style = if (bold) Font.BOLD else Font.NORMAL
    return FontFactory.getFont(FontFactory.HELVETICA, sizePt, style)
}

private val COL_HEADER_BG = Color(200, 210, 230)
private val DISH_BG       = Color.WHITE
private val ING_BG        = Color(243, 245, 250)
private val ING_BORDER    = Color(210, 216, 228)

fun exportDishesPdf(
    out: OutputStream,
    dishes: List<Dish>,
    productsById: Map<UUID, Product>,
    includeIngredients: Boolean
) {
    val doc = Document(PageSize.A4, 40f, 40f, 50f, 40f)
    PdfWriter.getInstance(doc, out)
    doc.open()

    val titleFont  = cyrillicFont(13f, bold = true)
    val headerFont = cyrillicFont(9f,  bold = true)
    val dishFont   = cyrillicFont(9f)
    val ingFont    = cyrillicFont(8f)

    val title = Paragraph("Меню — Стоимость блюд", titleFont).apply {
        alignment = Element.ALIGN_CENTER
    }
    val subtitle = Paragraph(LocalDate.now().format(DATE_FMT), cyrillicFont(9f)).apply {
        alignment = Element.ALIGN_CENTER
        spacingAfter = 10f
    }
    doc.add(title)
    doc.add(subtitle)

    // 5 columns — shared by both dish rows and ingredient rows:
    //   Col 1: Dish name  /  Ingredient name (indented)
    //   Col 2: Category   /  Quantity
    //   Col 3: Portions   /  Unit
    //   Col 4: Price/portion  /  (empty)
    //   Col 5: Total      /  Ingredient cost
    val colWidths = floatArrayOf(3.8f, 1.6f, 0.9f, 1.6f, 1.6f)
    val table = PdfPTable(colWidths.size).apply {
        widthPercentage = 100f
        setWidths(colWidths)
        setHeaderRows(1)
    }

    fun headerCell(text: String, align: Int = Element.ALIGN_CENTER) =
        PdfPCell(Phrase(text, headerFont)).apply {
            backgroundColor = COL_HEADER_BG
            setPadding(5f)
            horizontalAlignment = align
            verticalAlignment = Element.ALIGN_MIDDLE
        }

    fun dishCell(text: String, align: Int = Element.ALIGN_LEFT) =
        PdfPCell(Phrase(text, dishFont)).apply {
            backgroundColor = DISH_BG
            setPaddingLeft(6f); setPaddingRight(6f)
            setPaddingTop(4f);  setPaddingBottom(4f)
            horizontalAlignment = align
        }

    fun ingCell(text: String, align: Int = Element.ALIGN_LEFT, lastInGroup: Boolean = false) =
        PdfPCell(Phrase(text, ingFont)).apply {
            backgroundColor = ING_BG
            setPaddingLeft(6f); setPaddingRight(6f)
            setPaddingTop(2f);  setPaddingBottom(2f)
            horizontalAlignment = align
            borderColor = ING_BORDER
            // thicker bottom border on the last ingredient row of each dish
            if (lastInGroup) borderWidthBottom = 1.2f
        }

    // Header
    table.addCell(headerCell("Блюдо",       Element.ALIGN_LEFT))
    table.addCell(headerCell("Категория"))
    table.addCell(headerCell("Порций"))
    table.addCell(headerCell("Цена / порция"))
    table.addCell(headerCell("Итого"))

    for (dish in dishes) {
        val total      = dishCost(dish, productsById)
        val perPortion = if (dish.portions > 0) total / dish.portions else 0.0

        // Dish summary row
        table.addCell(dishCell(dish.name))
        table.addCell(dishCell(dish.category.display, Element.ALIGN_CENTER))
        table.addCell(dishCell(dish.portions.toString(), Element.ALIGN_CENTER))
        table.addCell(dishCell(formatMoney(perPortion), Element.ALIGN_RIGHT))
        table.addCell(dishCell(formatMoney(total),      Element.ALIGN_RIGHT))

        // Ingredient rows — flat rows in the same table, no nesting
        if (includeIngredients && dish.ingredients.isNotEmpty()) {
            dish.ingredients.forEachIndexed { idx, ing ->
                val p    = productsById[ing.productId]
                val name = p?.name ?: "(удалён)"
                val qty  = ing.quantity.let {
                    if (it == it.toLong().toDouble()) it.toLong().toString()
                    else "%.3f".format(it).trimEnd('0').trimEnd('.')
                }
                val cost = if (p != null) try { ingredientCost(ing, p) } catch (_: Exception) { 0.0 } else 0.0
                val last = idx == dish.ingredients.lastIndex

                table.addCell(ingCell("    $name",              Element.ALIGN_LEFT,  last))
                table.addCell(ingCell(qty,                      Element.ALIGN_RIGHT, last))
                table.addCell(ingCell(ing.unit.display,         Element.ALIGN_CENTER,last))
                table.addCell(ingCell("",                       Element.ALIGN_RIGHT, last))
                table.addCell(ingCell(formatMoney(cost),        Element.ALIGN_RIGHT, last))
            }
        }
    }

    doc.add(table)
    doc.close()
}
