package skynetbee.developers.production

import android.util.Log
import skynetbee.developers.developerenvironment.getCurrentDate
import skynetbee.developers.developerenvironment.getCurrentTime
import kotlin.jvm.javaClass
import kotlin.text.replace
import kotlin.to
import kotlin.toString


var clPrevFileName:String? = null
var clPrevClassName:String? = null
var clPrevFunction:String? = null
var clPrevType:String? = null
var clPrevLine = -1
var clIterator = 0

fun cl(
    msg: Any? = "Reached Line \$line : Iterated \$i times",
    type: String = "Quick Print",
    line: Int = Throwable().stackTrace[1].lineNumber,
    function: String = Throwable().stackTrace[1].methodName ?: "Unknown",
    file: String = Throwable().stackTrace[1].fileName ?: "Unknown",
    className: String = Throwable().stackTrace[1].className.split(".").last() ?: "Unknown"
) {
    var m = msg.toString()

    if (m == "Reached Line \$line : Iterated \$i times") {
        m = "Reached Line $line"
    }

    val filename = file

    if (clPrevFileName != filename || clPrevClassName != className || clPrevFunction != function || clPrevType != type) {
        clIterator = 0
        clPrevFileName = filename
        clPrevClassName = className
        clPrevFunction = function
        clPrevType = type
        clPrevLine = line
        Log.d(type, "--------------------------")
        Log.d(type, "$filename / $className / $function")
        Log.d(type, "on Line $line : $m")
    } else {
        if (clPrevLine != line) {
            clIterator = 0
            clPrevLine = line
            Log.d(type, "on Line $line : $m")
        } else {
            clIterator++
            Log.d(type, "on Line $line : $m : Iteration $clIterator")
        }
    }

    val tableName = "t${getCurrentDate().replace("-", "")}"
    val toe = getCurrentTime()
    DevOps.executeQuery("INSERT INTO $tableName (fileName, className, function, type, line, message, toe) " +
            "VALUES ('$filename', '$className', '$function', '$type', '$line', '${m.replace("'", "''")}', '$toe');")
}