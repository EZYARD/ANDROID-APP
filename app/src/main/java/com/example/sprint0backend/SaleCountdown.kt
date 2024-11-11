package com.example.sprint0backend


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

import java.time.Instant
import java.time.format.DateTimeParseException

class CountdownTimerLogic(private val startTime: String) {

    // Convert ISO 8601 string to a Long timestamp (milliseconds)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseStartTime(): Long {
        return try {
            Instant.parse(startTime).toEpochMilli()  // Convert ISO 8601 to milliseconds
        } catch (e: DateTimeParseException) {
            System.currentTimeMillis() // Default to current time if parsing fails
        }
    }

    // Function to get the countdown string
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeLeft(): String {
        val currentTime = System.currentTimeMillis()
        val parsedStartTime = parseStartTime()
        val timeDifference = parsedStartTime - currentTime

        return if (timeDifference > 0) {
            val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
            val hours = TimeUnit.MILLISECONDS.toHours(timeDifference) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60
            when {
                days > 0 -> "$days days till sale"
                hours > 0 -> "$hours hours till sale"
                else -> "$minutes minutes till sale"
            }
        } else {
            "Sale started!"
        }
    }
}

// Countdown Timer Composable
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CountdownTimer(startTime: String) {
    val countdownLogic = remember { CountdownTimerLogic(startTime) }

    // Get the countdown text using the logic class
    val countdownText = countdownLogic.getTimeLeft()

    // Display the countdown
    Text(
        text = "Time Left: $countdownText",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(8.dp)
    )
}