package com.example.sprint0backend

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class CountdownTimerLogic(private val startTime: String) {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseStartTime(): ZonedDateTime? {
        val correctedStartTime = if (!startTime.contains("Z")) "$startTime" + "Z" else startTime
        return try {
            ZonedDateTime.parse(correctedStartTime)
        } catch (e: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeLeft(): String {
        val currentTime = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"))
        val saleStartTime = parseStartTime()

        if (saleStartTime == null) {
            return "Invalid sale time"
        }

        val currentDate = currentTime.toLocalDate()
        val saleStartDate = saleStartTime.toLocalDate()

        return when {
            saleStartDate.isEqual(currentDate) -> "Sale is today"
            saleStartDate.isBefore(currentDate) -> "Sale expired"
            else -> {
                val daysLeft = Duration.between(currentDate.atStartOfDay(ZoneId.of("America/Los_Angeles")), saleStartDate.atStartOfDay(ZoneId.of("America/Los_Angeles"))).toDays()
                "$daysLeft days till sale"
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CountdownTimer(startTime: String) {
    val countdownLogic = remember { CountdownTimerLogic(startTime) }
    val countdownText = countdownLogic.getTimeLeft()

    Text(
        text = countdownText,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
        ),
        modifier = Modifier.padding(8.dp)
    )
}