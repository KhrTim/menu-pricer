package com.productbasket.ui.common

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val moneyFmt = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("ru", "RU")))
private val qtyFmt = DecimalFormat("#,##0.###", DecimalFormatSymbols(Locale("ru", "RU")))

fun formatMoney(value: Double): String = "${moneyFmt.format(value)} ₽"
fun formatQty(value: Double): String = qtyFmt.format(value)

fun parsePositiveDouble(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

fun parsePositiveInt(text: String): Int? =
    text.trim().toIntOrNull()?.takeIf { it > 0 }
