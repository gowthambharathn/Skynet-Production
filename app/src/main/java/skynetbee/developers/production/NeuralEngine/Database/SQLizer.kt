package skynetbee.developers.production

import android.util.Log
import skynetbee.developers.developerenvironment.enqry
import skynetbee.developers.developerenvironment.generateRandomAlphaNumeric
import skynetbee.developers.developerenvironment.getCurrentDate
import skynetbee.developers.developerenvironment.getCurrentTime
import skynetbee.developers.developerenvironment.user
import kotlin.collections.filterKeys
import kotlin.collections.firstOrNull
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.mapValues
import kotlin.let
import kotlin.math.log
import kotlin.run
import kotlin.text.isEmpty
import kotlin.text.substringAfterLast
import kotlin.text.toIntOrNull


var sql = SQLize()


class SqlTracker(
    var tracker: String,
    var tableData: List<Map<String, String>>,
    var query: String,
    var nextRow: Int = 0
){
    override fun toString(): String {
        return "Query: $query\nTable Data: $tableData"
    }
}


class SQLize() : NeuralMemoryConnectionEstablisher() {
    init {
        Log.d("onClick","SQL Object Created")
    }

    val sqt = mutableListOf<SqlTracker>()

    fun insert(
        tableName: String,
        kvp: Map<String, String>,
        fileName: String = Throwable().stackTrace[1].fileName
    ): Pair<Triple<Int, Boolean, Boolean>, String> {

        Log.d("SQLResult", "Sqlize.Insert : insert() called with tableName = $tableName, kvp = $kvp, fileName = $fileName")

        val query = generateSelectQuery(tableName, kvp) + " and todat = '0000-00-00' and area='${user.getArea()}'"
        Log.d("SQLResult", "Sqlize.Insert : Generated query -> $query")

        reset()
        val result = this.select(query)
        Log.d("SQLResult", "Sqlize.Insert : executeQuery returned -> success = ${result}, data = ${result}")

        return if (result != null) {
            val rci = result.get(0)["localcounti"]?.toIntOrNull() ?: -1
            Log.d("SQLResult", "Sqlize.Insert : Query successful -> localcounti = $rci")
            Pair(Triple(rci, true, true), query)
        } else {
            Log.e("SQLError", "Sqlize.Insert : Query failed -> executing freeInsert() for table = $tableName")
            val freeInsertResult = freeInsert(tableName, kvp, fileName)
            Log.d("SQLResult", "Sqlize.Insert : freeInsert returned -> $freeInsertResult")
            Pair(Triple(freeInsertResult.first, freeInsertResult.second, true), freeInsertResult.third)
        }
    }


    private fun generateSelectQuery(tableName: String, data: Map<String, Any>): String {
        val conditions = data.entries.joinToString(" AND ") { (key, value) ->
            if (value == null) "$key IS NULL" else "$key='$value'"
        }

        val query = "localcounti FROM $tableName WHERE $conditions"

        Log.d("SQLResult", "generateSelectQuery: $query")
        return query

    }


    fun freeInsert(tableName: String, kvp: Map<String, String>, fileName: String = Throwable().stackTrace[1].fileName): Triple<Int, Boolean, String> {
        Log.d("SQLResult", "Sqlize.freeInsert : Called with tableName = $tableName, kvp = $kvp, fileName = $fileName")

        val columns = mutableListOf<String>()
        val values = mutableListOf<String>()

        for ((key, value) in kvp) {
            columns.add(enqry(key))
            values.add(ivc(value))
        }

        columns.addAll(
            listOf(
                "area", "counti", "mcounti", "fromdat", "ftodat", "ftotim",
                "ftovername", "ftover", "ftopid", "todat", "totim", "tovername", "tover", "topid",
                "ipmac", "deviceanduserainfo", "basesite", "owncomcode",
                "testeridentity", "testcontrol",
                "adderpid", "addername", "adder", "doe", "toe","syncstatus", "syncfailurereason"
            )
        )

        val randomStr = generateRandomAlphaNumeric(7)

        val data = DF.select("offinam, unique_member_id from all_system_developer_details")
        Log.d("SQLResult", "Sqlize.freeInsert : Generated random testcontrol = $randomStr")

        values.addAll(
            listOf(
                ivc(user.getArea()),          // area
                ivc("0"),                     // counti
                ivc("0"),                     // mcounti
                ivc("0000-00-00"),            // fromdat
                ivc("0000-00-00"),            // ftodat
                ivc("00:00:00"),              // ftotim
                ivc(""),                      // ftovername
                ivc(""),                      // ftover
                ivc(""),                      // ftopid
                ivc("0000-00-00"),            // todat
                ivc("00:00:00"),              // totim
                ivc(""),                      // tovername
                ivc(""),                      // tover
                ivc(""),                      // topid
                ivc("DevEnvironment"),        // ipmac
                ivc("Android"), // deviceanduserainfo
                ivc("DevEnv"),                // basesite
                ivc("developer"),             // owncomcode
                ivc(data?.get("unique_member_id") ?: "unknown"),             // testeridentity
                ivc(randomStr),               // testcontrol
                ivc(fileName),                // adderpid
                ivc(user.getName()),          // addername
                ivc(user.getId()),            // adder
                ivc(getCurrentDate()),        // doe
                ivc(getCurrentTime()),        // toe
                ivc(""),                      // syncstatus
                ivc(""),                      // syncrejectionreason
            )
        )


        val columnsString = columns.joinToString(", ")
        val valuesString = values.joinToString(", ")
        val insertQuery = "INSERT INTO $tableName($columnsString) VALUES($valuesString)"

        Log.d("SQLResult", "Sqlize.freeInsert : Generated INSERT query -> $insertQuery")

        if (executeQuery(insertQuery).first) {
            Log.d("SQLResult", "Sqlize.freeInsert : INSERT executed successfully")

            val ctn = executeQuery("SELECT localcounti FROM $tableName WHERE testcontrol = '$randomStr'")
            val count = ctn.second?.firstOrNull()?.get("localcounti")?.toIntOrNull() ?: -1

            Log.d("SQLResult", "Sqlize.freeInsert : Retrieved localcounti = $count")

            return if (count != -1) {
                executeQuery("UPDATE $tableName SET testcontrol='${data?.get("offinam") ?: "unknown"}' WHERE localcounti = $count AND testcontrol='$randomStr'")
                Log.d("SQLResult", "Sqlize.freeInsert : Reset testcontrol for localcounti = $count")
                Triple(count, true, insertQuery)
            } else {
                Log.e("SQLError", "Sqlize.freeInsert : Failed to retrieve localcounti for testcontrol = $randomStr")
                Triple(-1, false, insertQuery)
            }
        } else {
            Log.e("SQLError", "Sqlize.freeInsert : INSERT query failed -> $insertQuery")
            return Triple(-1, false, insertQuery)
        }
    }

    fun read(columns: String, tableName: String, whereCondition: String? = "", others : String? = "", file: String = Throwable().stackTrace[1].fileName, line: Int = Throwable().stackTrace[1].lineNumber): List<Map<String, String>>?{

        var condition = whereCondition

        condition = if (!condition.isNullOrEmpty()) {
            "$whereCondition AND (todat = '0000-00-00' AND totim = '00:00:00')"
        } else {
            "todat = '0000-00-00'"
        }
        val effectiveTracker ="$file @ $line"

        val query = if (condition.isNullOrEmpty() && others?.isNullOrEmpty() == true) {
            "SELECT $columns FROM $tableName"
        } else {
            "SELECT $columns FROM $tableName WHERE $condition $others"
        }

        Log.d("SQLResult", "Sqlize.read : Generated query -> $query")

        for (sqlTracer in sqt) {
            if ( sqlTracer.tracker == effectiveTracker) {
                if (sqlTracer.query == query) {
                    Log.d("SQLResult", "Sqlize.read : Query matched tracker -> Returning cached result")
                    return sqlTracer.tableData
                }
            }
        }

        val eR = executeQuery(query)
        val temp = eR.second
        Log.d("SQLResult", "Sqlize.read : Executed query -> $query")
        if (eR.first) {
            if (temp != null && temp.isNotEmpty()) {
                sqt.add(0, SqlTracker(tracker = effectiveTracker, tableData = temp, query = query))
//                sqt[0].nextRow = 1
                Log.d("SQLResult", "Sqlize.read : Query executed successfully -> ${sqt[0].tracker}")
                return sqt[0].tableData
            } else {
                sqt.add(0, SqlTracker(tracker = effectiveTracker, tableData = emptyList() , query = query))
//                sqt[0].nextRow = 1
                Log.d("SQLResult", "Sqlize.read : Query returned empty result")
            }
        } else {
            sqt.add(0, SqlTracker(tracker = effectiveTracker, tableData = emptyList() , query = query))
//            sqt[0].nextRow = 1
            Log.e("SQLError", "Sqlize.read : Query execution failed -> $query")
        }
        Log.d("SQLResult", "Sqlize.read : Returning null")
        return null
    }

    fun restart(line: Int = Throwable().stackTrace[1].lineNumber, file: String = Throwable().stackTrace[1].fileName): Boolean {

        val tracker = "${file} @ ${line + 1}"

        Log.d("SQLResult", "Sqlize.restart: Attempting to reset tracker $tracker")
        var i = 0
        for (cv in sqt) {
            if (cv.tracker == tracker) {
                sqt[i].nextRow = 0
                Log.d("SQLResult", "Sqlize.restart: Tracker found and reset for $tracker")
                return true
            } else {
                i++
            }
        }

        Log.e("SQLError", "Sqlize.restart: Tracker not found for $tracker")
        return false
    }


    fun restart(file: String = Throwable().stackTrace[1].fileName, lineNumber: Int): Boolean {

        val tracker = "$file @ $lineNumber"

        Log.d("SQLResult", "Sqlize.restart: Attempting to reset tracker $tracker")
        var i = 0
        for (cv in sqt) {
            if (cv.tracker == tracker) {
                sqt[i].nextRow = 0
                Log.d("SQLResult", "Sqlize.restart: Tracker found and reset for $tracker")
                return true
            } else {
                i++
            }
        }

        Log.e("SQLError", "Sqlize.restart: Tracker not found for $tracker")
        return false
    }


    fun reset(line: Int = Throwable().stackTrace[1].lineNumber, file: String = Throwable().stackTrace[1].fileName): Boolean {

        val tracker = "$file @ ${line + 1}"

        Log.d("SQLResult", "Sqlize.reset: Attempting to remove tracker $tracker")
        var i = 0
        for (cv in sqt) {
            if (cv.tracker == tracker) {
                sqt.removeAt(i)
                Log.d("SQLResult", "Sqlize.reset: Tracker removed for $tracker")
                return true
            } else {
                i++
            }
        }

        Log.e("SQLError", "Sqlize.reset: Tracker not found for $tracker")
        return false
    }


    fun reset(file: String = Throwable().stackTrace[1].fileName, lineNumber: Int): Boolean {

        val tracker = "$file @ $lineNumber"

        Log.d("SQLResult", "Sqlize.reset: Attempting to remove tracker $tracker")
        var i = 0
        for (cv in sqt) {
            if (cv.tracker == tracker) {
                sqt.removeAt(i)
                Log.d("SQLResult", "Sqlize.reset: Tracker removed for $tracker")
                return true
            } else {
                i++
            }
        }

        Log.e("SQLError", "Sqlize.reset: Tracker not found for $tracker")
        return false
    }



    fun delete(
        tableName: String,
        whereCondition: String,
        fileName: String = Throwable().stackTrace[1].fileName
    ): Triple<Int, Boolean, String> {

        val selQuery = "SELECT count(*) FROM $tableName WHERE $whereCondition AND area='${user.getArea()}' AND todat='0000-00-00'"
        val countResult = executeQuery(selQuery)
        val eQ = countResult.second?.firstOrNull()?.get("count(*)")
        val affectedRows = eQ?.toIntOrNull() ?: 0

        Log.d("SQLResult", "Sqlize.delete: Rows matching delete condition: $affectedRows from [$selQuery]")

        val query = """
        UPDATE $tableName
        SET todat='${getCurrentDate()}',
            totim='${getCurrentTime()}',
            tover='${user.getId()}',
            tovername='${user.getName()}',
            topid='$fileName'
        WHERE ($whereCondition) AND area='${user.getArea()}' AND (todat = '0000-00-00' or todat is null)
    """.trimIndent()

        val updateSuccess = executeQuery(query).first
        return if (updateSuccess) {
            Log.d("SQLResult", "Sqlize.delete: Successfully updated $affectedRows rows with [$query]")
            Triple(affectedRows, true, query)
        } else {
            Log.e("SQLError", "Sqlize.delete: Failed to execute update [$query]")
            Triple(0, false, query)
        }
    }


    fun end(
        tableName: String,
        whereCondition: String,
        fileName: String = Throwable().stackTrace[1].fileName
    ): Triple<Int, Boolean, String> {

        val selQuery = "SELECT count(*) FROM $tableName WHERE $whereCondition AND area='${user.getArea()}' AND ftodat='0000-00-00'"
        val countResult = executeQuery(selQuery)
        val eQ = countResult.second?.firstOrNull()?.get("count(*)")
        val affectedRows = eQ?.toIntOrNull() ?: 0

        Log.d("SQLResult", "Sqlize.end: Rows matching end condition: $affectedRows from [$selQuery]")

        val query = """
        UPDATE $tableName
        SET ftodat='${getCurrentDate()}',
            ftotim='${getCurrentTime()}',
            ftover='${user.getId()}',
            ftovername='${user.getName()}',
            ftopid='$fileName'
        WHERE ($whereCondition) AND area='${user.getArea()}' AND (ftodat='0000-00-00' OR ftodat IS NULL) AND (todat = '0000-00-00' or todat is null)
    """.trimIndent()

        val updateSuccess = executeQuery(query).first
        return if (updateSuccess) {
            Log.d("SQLResult", "Sqlize.end: Successfully updated $affectedRows rows with [$query]")
            Triple(affectedRows, true, query)
        } else {
            Log.e("SQLError", "Sqlize.end: Failed to execute update [$query]")
            Triple(0, false, query)
        }
    }



    fun debug(
        line: Int = -1,
        tag: String = "SQL Debug",
        file: String = Throwable().stackTrace[1].fileName
    ): SqlTracker? {
        var matched : SqlTracker?
        var tracker = "$file @ $line"
        if (line != -1) {
            matched = sqt.find { it.tracker == tracker }
        } else {
            matched = sqt.first()
            tracker = matched.tracker
        }

        return if (matched != null) {
            Log.d("SQLResult", "Sqlize.debug: Tracker found for [$tracker]")
            matched
        } else {
            Log.e("SQLError", "Sqlize.debug: No tracker found for [$tracker]")
            null
        }
    }


    // This is the fun that reads the value from the SQL Database
    fun select(qry: String, file: String = Throwable().stackTrace[1].fileName, line: Int = Throwable().stackTrace[1].lineNumber): List<Map<String, String>>? {
        val query = "SELECT $qry"

        val fileName = file.substringAfterLast("/")
        val lineNumber = " @ $line"
        val effectiveTracker = fileName + lineNumber

        for (sqlTracer in sqt) {
            if ( sqlTracer.tracker == effectiveTracker) {
                if (sqlTracer.query == query) {
                    Log.d("SQLResult", "Sqlize.read : Query matched tracker -> Returning cached result")
                    return sqlTracer.tableData
                }
            }
        }

        Log.d("SQLResult", "Sqlize.select: Query -> $query")
        val eR = executeQuery(query)
        val temp = eR.second
        if (eR.first) {
            if (temp != null && temp.isNotEmpty()) {
                sqt.add(0, SqlTracker(tracker = effectiveTracker, tableData = temp, query = query))
//                sqt[0].nextRow = 1
                Log.d("SQLResult", "Sqlize.read : Query executed successfully -> ${sqt[0].tracker}")
                return sqt[0].tableData
            } else {
                sqt.add(0, SqlTracker(tracker = effectiveTracker, tableData = emptyList() , query = query))
//                sqt[0].nextRow = 1
                Log.d("SQLResult", "Sqlize.read : Query returned empty result")
            }
        } else {
            sqt.add(0, SqlTracker(tracker = effectiveTracker, tableData = emptyList() , query = query))
//            sqt[0].nextRow = 1
            Log.e("SQLError", "Sqlize.read : Query execution failed -> $query")
        }
        Log.d("SQLResult", "Sqlize.read : Returning null")
        return null
    }


    fun update(tableName: String, kvp: Map<String, String>, localcounti: String, file: String = Throwable().stackTrace[1].fileName): Pair<Triple<Int, Boolean, Boolean>, String> {
        var selQuery = "SELECT * FROM $tableName WHERE localcounti='$localcounti' AND todat = '0000-00-00'"

        kvp.forEach { (key, value) ->
            selQuery += " AND $key = $value"
        }
        val eR = executeQuery(selQuery)

        return if (!eR.first) {
            eR.second?.firstOrNull()?.let { temp ->
                val invalidKeys = setOf(
                    "area", "counti", "mcounti", "fromdat", "ftodat", "ftotim", "ftovername", "ftover",
                    "ftopid", "todat", "totim", "tovername", "tover", "topid", "deviceanduserainfo",
                    "basesite", "owncomcode", "ipmac", "testeridentity", "testcontrol", "adderpid",
                    "addername", "adder", "doe", "toe", "localcounti", "syncstatus",
                    "syncfailurereason"
                )

                val ourkvp = temp.filterKeys { it !in invalidKeys }.mapValues { kvp[it.key] ?: it.value }
                Log.d("SQLResult", "Sqlize.update: Prepared KVP for update: $ourkvp")

                delete(tableName, "localcounti='$localcounti'", file).also {
                    Log.d("SQLResult", "Sqlize.update: Existing record deleted from $tableName where localcounti='$localcounti'")
                }

                val insertResult = insert(tableName, ourkvp, fileName = file)

                val firstValue = insertResult.first.first
                val secondValue = insertResult.first.second
                val thirdValue = insertResult.first.third

                Log.d("SQLResult", "Sqlize.update: Inserted updated record into $tableName. Result = ($firstValue, $secondValue, $thirdValue)")

                Pair(Triple(firstValue, secondValue, thirdValue), selQuery)
            } ?: run {
                Log.e("SQLError", "Sqlize.update: No record found in $tableName for localcounti='$localcounti'")
                Pair(Triple(-1, false, false), selQuery)
            }
        } else {
            Log.e("SQLError", "Sqlize.update: Query execution failed for $selQuery")
            Pair(Triple(-1, false, false), selQuery)
        }
    }

    fun ivc(value: String): String {
        return "'$value'"
    }
}