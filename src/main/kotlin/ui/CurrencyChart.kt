package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.model.CurrencyPricePoint
import data.util.roundCurrency
import kotlin.math.roundToInt

@Composable
fun CurrencyChart(modifier: Modifier = Modifier, currencyPoints: List<CurrencyPricePoint>) {
    var lastTouched by remember { mutableStateOf<Float?>(null) }
    var lastTouchedIndex by remember { mutableStateOf<Int?>(null) }
    val points = currencyPoints.map { it.price.toFloat() }
    Column {
        Canvas(modifier.pointerInput(Unit) {
            detectDragGestures(onDrag = { change, _ ->
                lastTouched = if (change.position.x > 0) change.position.x else 0f
            })
        }) {
            println(size.height)
            val spaceBetweenPoints = size.width / points.size
            val min = points.minOrNull() ?: 0f
            val max = points.maxOrNull() ?: 0f
            val variance = max - min
            val multiplier = size.height / variance
            var lastPoint = (points[0] - min) * multiplier
            points.forEachIndexed { i, item ->
                val newPoint = (item - min) * multiplier
                drawLine(
                    Color.Black,
                    Offset(i * spaceBetweenPoints, size.height - lastPoint),
                    Offset((i + 1) * spaceBetweenPoints, size.height - newPoint)
                )
                lastPoint = newPoint
            }
            lastTouched?.let {
                drawLine(Color.Red, Offset(it, 0f), Offset(it, size.height))
                val ind = (it / spaceBetweenPoints).roundToInt().coerceIn(currencyPoints.indices)
                val mid = Offset(it, size.height - ((currencyPoints[ind].price.toFloat() - min) * multiplier))
                drawCircle(Color.Red, 4f, mid)
                drawCircle(Color.Black, 2f, mid)
                lastTouchedIndex = ind
            }
        }
        Text(
            lastTouchedIndex?.let { "${currencyPoints[it].date} \n ${currencyPoints[it].price.roundCurrency()} PLN" }
                ?: "2020-00-00\n00.00 PLN",
            Modifier.padding(8.dp).fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}