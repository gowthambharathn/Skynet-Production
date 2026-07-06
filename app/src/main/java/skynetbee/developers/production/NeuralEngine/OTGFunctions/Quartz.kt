package skynetbee.developers.developerenvironment

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Quartz.kt
 * Created by Mega
 * Date: 23-06-2026
 */

fun formatDateAndTime(inputString: String): String {
    val input = inputString.trim()
    val parts = input.split(Regex(" |&t")).filter { it.isNotBlank() }

    var datePart: String? = null
    var timePart: String? = null

    for (part in parts) {
        if ((part.contains("-") || part.contains("/")) && datePart == null) {
            datePart = part
        } else if (part.contains(":") && timePart == null) {
            timePart = part
        }
    }

    val formattedDate = datePart?.let { fDate(it) }
    val formattedTime = timePart?.let { fTime(it) }

    return when {
        formattedDate != null && formattedTime != null -> "$formattedDate at $formattedTime"
        formattedDate != null -> formattedDate
        formattedTime != null -> formattedTime
        else -> "Invalid input"
    }
}
fun fDate(input: String): String {
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    )
    val outputFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    for (format in formats) {
        try {
            val date = format.parse(input)
            if (date != null) return outputFormatter.format(date)
        } catch (_: Exception) {}
    }
    return "Invalid date"
}
fun fTime(input: String): String {
    val inputFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val outputFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    return try {
        val time = inputFormatter.parse(input)
        outputFormatter.format(time!!)
    } catch (e: Exception) {
        "Invalid time"
    }
}
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}