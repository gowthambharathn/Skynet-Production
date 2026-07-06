package skynetbee.developers.developerenvironment

/**
 * Valitators.kt
 * Created by Gowtham Bharath N
 * Date: 23-06-2026
 */


fun generateRandomAlphaNumeric(length: Int): String {
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

fun generateRandomString(length: Int): String {
    val chars = "abcdefghijklmnopqrstuvwxyz"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

fun generateRandomNumber(length: Int): String {
    val chars = "0123456789"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}