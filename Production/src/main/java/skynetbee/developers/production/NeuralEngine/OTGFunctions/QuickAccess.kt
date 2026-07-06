//package skynetbee.developers.developerenvironment
//
///**
// * QuickAccess.kt
// * Created by Mega
// * Date: 23-06-2026
// */
//
//import android.icu.text.DecimalFormat
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
//import androidx.compose.material.icons.filled.ArrowDropDown
//import androidx.compose.material.icons.filled.ArrowDropUp
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenuDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.LocalTextStyle
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.MenuItemColors
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.OutlinedTextFieldDefaults
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.produceState
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import skynetbee.developers.production.DF
//import skynetbee.developers.production.cl
//import skynetbee.developers.production.sql
//import java.util.Locale
//import java.util.regex.Pattern
//
//val Gold = Color(0xFFFFD277)
//val Silver = Color(0xFFBFBFBF)
//
//
//var selectItem = mutableStateMapOf<String, String>()
//var fill = mutableStateOf(String())
//fun numberToWords(number: Long): String {
//    if (number == 0L) return "Zero Rupees"
//
//
//    val parts = listOf(
//        1_00_00_00_00_00_000L to "Quintillion",
//        1_00_00_00_00_000L to "Quadrillion",
//        1_00_00_00_000L to "Trillion",
//        1_00_00_00_0L to "Billion",
//        1_00_00_000L to "Million",
//        1_00_00_0L to "Crore",
//        1_00_000L to "Lakh",
//        1_000L to "Thousand",
//        100L to "Hundred"
//    )
//
//    val units = listOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
//        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
//    val tens = listOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")
//
//    fun convertNumToWords(num: Int): String {
//        return when {
//            num < 20 -> units[num]
//            num < 100 -> tens[num / 10] + (if (num % 10 > 0) " "
//                    + units[num % 10] else "")
//            else -> units[num / 100] + " Hundred" + (if (num % 100 > 0) " and "
//                    + convertNumToWords(num % 100) else "")
//        }
//    }
//
//    var num = number
//    val words = mutableListOf<String>()
//
//    for ((value, name) in parts) {
//        if (num >= value) {
//            val chunk = (num / value).toInt()
//            num %= value
//            words.add("${convertNumToWords(chunk)} $name")
//        }
//    }
//
//    if (num > 0) {
//        words.add(convertNumToWords(num.toInt()))
//    }
//
//    return words.joinToString(" ")
//}
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DropDown(
//    selectItem: String,
//    data: List<Map<String, String>>,
//    ndval: String,
//    preSelectedValue: String? = null,
//    onValueSelected: (id: String, value: String) -> Unit = { _, _ -> },
//    modifier: Modifier = Modifier
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    val dropdownItems = data.flatMap { it.entries }
//
//    var selectedEntry by remember {
//        mutableStateOf(
//            dropdownItems.find { it.value == preSelectedValue } ?: dropdownItems.firstOrNull()
//        )
//    }
//    var textFieldWidth by remember { mutableStateOf(0.dp) }
//    val density = LocalDensity.current
//
//    LaunchedEffect(selectedEntry) {
//        selectedEntry?.let { onValueSelected(selectItem, it.value) }
//    }
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded }
//    ) {
//        OutlinedTextField(
//            value = selectedEntry?.key ?: "",
//            onValueChange = {},
//            readOnly = true,
//            label = { Text(ndval, fontWeight = FontWeight.Bold, color = Gold) },
//            textStyle = TextStyle(
//                color = Gold,
//                fontWeight = FontWeight.SemiBold
//            ),
//            modifier = modifier
//                .fillMaxWidth()
//                .menuAnchor()
//                .onGloballyPositioned {
//                    textFieldWidth = with(density) { it.size.width.toDp() }
//                },
//            trailingIcon = {
//                Icon(
//                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
//                    contentDescription = "Dropdown Arrow"
//                )
//            },
//            shape = RoundedCornerShape(16.dp),
//            colors =  OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = Color(0xFFC0A060),
//                unfocusedBorderColor = Color(0xFFC0A060),
//                focusedLabelColor = Color(0xFFC0A060),
//                unfocusedLabelColor = Color(0xFFC0A060),
//                cursorColor = Color(0xFFC0A060),
//                focusedContainerColor = Color.Transparent,
//                unfocusedContainerColor = Color.Transparent,
//                disabledBorderColor = Color(0xFFC0A060)
//            )
//        )
//
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            containerColor = Color.Black,
//            modifier = Modifier.width(textFieldWidth)
//        ) {
//            dropdownItems.forEach { entry ->
//                DropdownMenuItem(
//                    text = { Text(entry.key, fontWeight = FontWeight.Bold, color = Gold) },
//                    onClick = {
//                        selectedEntry = entry
//                        expanded = false
//                        onValueSelected(selectItem, entry.value)
//                    },
//                    colors = MenuItemColors(
//                        textColor = Gold,
//                        leadingIconColor = Color.Transparent,
//                        trailingIconColor = Color.Transparent,
//                        disabledTextColor = Gold,
//                        disabledLeadingIconColor = Color.Gray.copy(0.2f),
//                        disabledTrailingIconColor = Color.Gray.copy(0.2f)
//                    )
//                )
//            }
//        }
//    }
//    cl("[$selectItem] -> Selected: ${selectedEntry?.key} (${selectedEntry?.value})", "megava")
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DropDownUl(
//    selectItem: String,
//    data: List<Map<String, String>>,
//    preSelectedValue: String? = null,
//    onValueSelected: (id: String, value: String) -> Unit = { _, _ -> },
//    modifier: Modifier = Modifier
//) {
//
//    if (data.isEmpty()) return
//    val labelEntry = data.first().entries.first()
//    val menuItems  = data.drop(1)
//    var expanded by remember { mutableStateOf(false) }
//
//    var selectedLabel by rememberSaveable(selectItem) {
//        mutableStateOf(
//            menuItems.firstOrNull { it.values.first() == preSelectedValue }
//                ?.keys?.first() ?: ""
//        )
//    }
//    var selectedValue by rememberSaveable(selectItem) {
//        mutableStateOf(preSelectedValue ?: "")
//    }
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded },
//        modifier = modifier
//    ) {
//        TextField(
//            value = selectedLabel,
//            onValueChange = {},
//            readOnly = true,
//            textStyle = LocalTextStyle.current.copy(
//                fontWeight = FontWeight.Bold
//            ),
//            placeholder = {
//                Text(
//                    labelEntry.key,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFFBFBFBF)
//                )
//            },
//            trailingIcon = {
//                Icon(
//                    imageVector = if(!expanded) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.Filled.KeyboardArrowDown,
//                    contentDescription = null,
//                    tint = Silver
//                )
//            }
//            ,
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor   = Color.Transparent,
//                unfocusedContainerColor = Color.Transparent,
//                disabledContainerColor  = Color.Transparent,
//                errorContainerColor     = Color.Transparent,
//                focusedIndicatorColor   = Color.Transparent,
//                unfocusedIndicatorColor = Color.Transparent,
//                disabledIndicatorColor  = Color.Transparent,
//                errorIndicatorColor     = Color.Transparent,
//                focusedTextColor        = Silver,
//                unfocusedTextColor      = Silver
//            ),
//            modifier = Modifier
//                .menuAnchor()
//                .wrapContentWidth()
//        )
//
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            menuItems.withIndex().forEach { (index, map) ->
//                if (index != 0) {
//                    val (label, value) = map.entries.first()
//                    DropdownMenuItem(
//                        text = { Text(label, fontWeight = FontWeight.Bold, color = Silver) },
//                        onClick = {
//                            selectedLabel = label
//                            selectedValue = value
//                            expanded = false
//                            onValueSelected(selectItem, selectedValue)
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//fun getLastSelectionFromDatabase(id: String): Pair<String, String>? {
//    val query = """
//        SELECT key, value
//        FROM draft_data_in_fill_from_database
//        WHERE id = '$id'
//        LIMIT 1
//    """.trimIndent()
//
//    Log.d("🔹 DB", "getLastSelectionFromDatabase called")
//    Log.d("🔹 DB", "➡ Query: $query")
//
//    val (success, rows) = DF.executeQuery(query)
//
//    if (!success) {
//        Log.e("❌ DB", "Query failed for id=$id")
//        return null
//    }
//
//    if (rows.isNullOrEmpty()) {
//        Log.d("🔹 DB", "No row found for id=$id")
//        return null
//    }
//
//    val row = rows.first()
//    val key = row["key"]
//    val value = row["value"]
//
//    Log.d("✅ DB", "Row fetched for id=$id -> key=$key, value=$value")
//
//    return if (key != null && value != null) {
//        Log.d("✅ DB", "Returning Pair(key=$key, value=$value)")
//        Pair(key, value)
//    } else {
//        Log.e("❌ DB", "Row has null key or value for id=$id")
//        null
//    }
//}
//
//fun saveSelectionToDatabase(id: String, key: String, value: String) {
//    val deleteQuery = "DELETE FROM draft_data_in_fill_from_database where id = '${id}'"
//    val deleteResult = DF.executeQuery(deleteQuery)
//    Log.d("🔹 DB", "Deleted all previous data, success=$deleteResult")
//
//    val insertQuery = """
//        INSERT INTO draft_data_in_fill_from_database (id, "key", value)
//        VALUES ('$id', '$key', '$value')
//    """.trimIndent()
//
//    Log.d("🔹 DB", "Inserting new selection: id=$id, key=$key, value=$value")
//    val insertResult = DF.executeQuery(insertQuery)
//    if (insertResult.first) {
//        Log.d("✅ DB", "Insert success for id=$id")
//    } else {
//        Log.e("❌ DB", "Insert failed for id=$id")
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun fillFromDatabase(
//    id: String,
//    query: String,
//    ndval: String,
//    errorcode: String,
//    onchange: (Triple<Int, String, String>) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var expanded by remember { mutableStateOf(false) }
//    var selectedText by remember { mutableStateOf("") }
//
//    // Load items with index
//    val items by produceState(initialValue = listOf<Triple<Int, String, String>>()) {
//        Log.d("fillFromDatabase", query)
//        val rows = sql.select(query)
//        Log.d("fillFromDatabase", "$rows")
//        value = if (!rows.isNullOrEmpty()) {
//            Log.d("fillFromDatabase", "")
//            rows.mapIndexed { index, row ->
//                Triple(index, row["display"] ?: "", row["value"] ?: "")
//            }
//        } else {
//            listOf(Triple(0, errorcode, "nd"))
//        }
//        Log.d("fillFromDatabase", "Loaded items for $id: ${value.map { it.second }}")
//    }
//
//    LaunchedEffect(items) {
//        val lastSelection = getLastSelectionFromDatabase(id)
//        val match = lastSelection?.let { sel -> items.find { it.third == sel.second } }
//
//        when {
//            match != null -> {
//                selectedText = match.second
//                onchange(match)
//            }
//
//            items.size == 1 -> {
//                val first = items.first()
//                saveSelectionToDatabase(id, first.second, first.third)
//                selectedText = first.second
//                onchange(first)
//            }
//
//            items.isNotEmpty() -> {
//                val first = items.first()
//                saveSelectionToDatabase(id, first.second, first.third)
//                selectedText = first.second
//                onchange(first)
//            }
//
//            else -> {
//                selectedText = ndval
//            }
//        }
//    }
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded },
//        modifier = modifier
//    ) {
//        OutlinedTextField(
//            value = selectedText,
//            onValueChange = {},
//            readOnly = true,
//            label = { Text(ndval, color = Gold) },
//            textStyle = LocalTextStyle.current.copy(color = Gold),
//            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//            modifier = Modifier
//                .menuAnchor()
//                .fillMaxWidth()
//                .background(Color.Transparent, shape = RoundedCornerShape(16.dp)),
//            shape = RoundedCornerShape(16.dp),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedContainerColor = Color.Transparent,
//                unfocusedContainerColor = Color.Transparent,
//                disabledContainerColor = Color.Transparent,
//                errorContainerColor = Color.Transparent,
//                disabledBorderColor = Gold,
//                focusedBorderColor = Gold,
//                unfocusedBorderColor = Gold
//            )
//        )
//
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            containerColor = Color.LightGray.copy(0.2f)
//        ) {
//            items.forEach { (index, name, value) ->
//                DropdownMenuItem(
//                    text = { Text(name, color = Gold) },
//                    onClick = {
//                        selectedText = name
//                        expanded = false
//                        saveSelectionToDatabase(id, name, value)
//                        Log.d("fillFromDatabase", "User selected index=$index, name=$name, value=$value")
//                        onchange(Triple(index, name, value))
//                    }
//                )
//            }
//        }
//    }
//}
//
//
//fun formatTextWithNumber(input: String): String {
//    val regex = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)")
//    val matcher = regex.matcher(input)
//
//    val result = StringBuffer()
//    while (matcher.find()) {
//        val numberPart = matcher.group(1)
//        val formattedNumber = formatNumberIndianWithDecimal(numberPart)
//        matcher.appendReplacement(result, formattedNumber)
//    }
//    matcher.appendTail(result)
//
//    return result.toString()
//}
//
//fun formatNumberIndianWithDecimal(input: String): String? {
//    return try {
//        if (input.contains(".")) {
//            val parts = input.split(".")
//            val wholePart = parts[0]
//            val decimalPart = parts.getOrNull(1) ?: ""
//
//            val formattedWhole = Nfi(wholePart)
//            "$formattedWhole.$decimalPart"
//        } else {
//            Nfi(input)
//        }
//    } catch (e: Exception) {
//        input
//    }
//}
//
//fun Nfi(input: Any?): String? {
//    val raw = input?.toString()?.trim() ?: return null
//
//    val cleaned = raw.replace("_", "").replace("+", "").replace("-", "")
//
//    if (cleaned.any { it.isLetter() }) {
//        cl("❌ Invalid input: contains alphabets ➡ \"$raw\"", "formatNumberIndian")
//        return null
//    }
//
//    return try {
//        val number = cleaned.toDouble()
//
//        val symbols = DecimalFormat().decimalFormatSymbols.apply {
//            groupingSeparator = ','
//        }
//        val formatter = DecimalFormat("#,##,##0.###", symbols)
//        formatter.format(number)
//    } catch (e: NumberFormatException) {
//        cl("❌ Number format error for input ➡ \"$raw\"", "formatNumberIndian")
//        null
//    }
//}
//
//@Composable
//fun DisplayList(headline: String, items: List<String>) {
//    Column(modifier = Modifier.padding(16.dp)) {
//
//        Text(
//            text = headline,
//            style = TextStyle(
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold
//            )
//        )
//
//        items.forEach { item ->
//            Text(text = "     ➜ $item", style = MaterialTheme.typography.bodyLarge)
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//    }
//}
//
//fun ua(input:String):String{
//    return input.uppercase(Locale.ROOT)
//}
//
//fun uc(name: String): String {
//    val separators = " [-_.,/]"
//    val regex = Regex("(?<=[$separators])|(?=[$separators])")
//
//    return name.split(regex)
//        .filter { it.isNotEmpty() }
//        .mapIndexed { index, part ->
//            if (part.matches(Regex("[$separators]"))) {
//
//                part
//            } else {
//                if (index == 0) {
//                    part.lowercase().replaceFirstChar { it.uppercaseChar() }
//                } else if (part.length > 2) {
//                    part.lowercase().replaceFirstChar { it.uppercaseChar() }
//                } else {
//                    part.lowercase()
//                }
//            }
//        }
//        .joinToString("")
//}
