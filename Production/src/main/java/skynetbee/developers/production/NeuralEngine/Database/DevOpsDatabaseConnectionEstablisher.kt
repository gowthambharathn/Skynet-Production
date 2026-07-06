package skynetbee.developers.production

import android.annotation.SuppressLint
import android.util.Log
import net.zetetic.database.sqlcipher.SQLiteDatabase
import skynetbee.developers.developerenvironment.getCurrentDate
import skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background.Packagename
import java.io.File

class DevOpsDatabaseConnectionEstablisher {
    var db: SQLiteDatabase? = null

    init {
        System.loadLibrary("sqlcipher")
        initializeDatabase()
    }

    val dosqt = mutableListOf<SqlTracker>()
    fun select(qry: String, file: String = "#file", line: Int = -1): Map<String, String>? {
        val query = "SELECT $qry"

        val fileName = file.substringAfterLast("/")
        val lineNumber = " @ $line"
        val track = fileName + lineNumber

        for (sqlTracer in dosqt) {
            if (sqlTracer.tracker == track) {
                if (sqlTracer.query == query) {
                    return if (sqlTracer.tableData.indices.contains(sqlTracer.nextRow)) {
                        sqlTracer.tableData[sqlTracer.nextRow++]
                    } else {
                        null
                    }
                } else {
                    println("SQL Error thrown by Sqlize.select : tracker : $track reused for ${sqlTracer.query} and $query")
                    return null
                }
            }
        }

        val eR = executeQuery(query)
        return if (eR.first) {
            eR.second?.let {
                dosqt.add(0, SqlTracker(track, it, query))
                if (it.isNotEmpty()) {
                    dosqt[0].nextRow = 1
                    it[0]
                } else {
                    null
                }
            } ?: run {
                println("SQL Error thrown by Sqlize.select : No Data Returned by Database for Tracker : $track and Query : $query")
                null
            }
        } else {
            println("SQL Error thrown by Sqlize.select : Execution Failed for Tracker : $track and Query : $query")
            null
        }
    }
    fun executeQuery(query: String): Pair<Boolean, List<Map<String, String>>?> {
        Log.d("DEV_EXQuery","reached line 174")
        if (db == null || !db!!.isOpen) {
            Log.e("DEV_EXQuery", "Database not initialized or not open")
            return Pair(false, null)
        }

        return try {

            if (query.trim().startsWith("SELECT", ignoreCase = true)) {
                val rows = mutableListOf<Map<String, String>>()
                val cursor = db?.rawQuery(query, null)

                cursor?.use {
                    while (it.moveToNext()) {
                        val row = mutableMapOf<String, String>()
                        for (i in 0 until it.columnCount) {
                            val columnName = it.getColumnName(i)
                            val value = it.getString(i)
                            row[columnName] = value
                        }
                        rows.add(row)
                    }
                }

                Pair(rows.isNotEmpty(), rows)
            } else {
                db?.execSQL(query)
                Log.d("DEV_EXQuery", "Query executed successfully (non-SELECT).")
                Pair(true, null)
            }
        } catch (e: Exception) {
            Log.e("DEV_EXQuery", "Error executing query: ${e.message}", e)
            Pair(false, null)
        }

    }


    @SuppressLint("SdCardPath")
    private fun initializeDatabase() {

        val dbPath1 = "/data/data/${Packagename.pkgnam}/Databases/DevOps.db"
        val dbFile1 = File(dbPath1)

        if (!dbFile1.parentFile?.exists()!!) {
            if (dbFile1.parentFile?.mkdirs()!!) {
                Log.d("DevOpsDatabase", "Directory DevOps created successfully in this package(${Packagename.pkgnam}).")
            } else {
                Log.e("DevOpsDatabase", "Failed to create directory DevOps in this package(${Packagename.pkgnam}).")
                return
            }
        }

        try {
            if (dbFile1.exists()) {
                Log.d("DevOpsDatabase", "DevOps Database already exists. Skipping creation in this package(${Packagename.pkgnam}).")
            } else {
                Log.d("DevOpsDatabase", "Creating new database DevOps in this package(${Packagename.pkgnam}).")
            }
            db = SQLiteDatabase.openDatabase(
                dbPath1,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY,
                null
            )
            db?.rawExecSQL("PRAGMA key = '123';")
            Log.d("DevOpsDatabase", "DevOps Database instance: $db")

            if (db != null) {
                Log.d("DevOpsDatabase", "DevOps Database initialized successfully in this package(${Packagename.pkgnam}).")
                createTableIfNotExists()
            } else {
                Log.e("DevOpsDatabase", "DevOps Database initialization failed in this package(${Packagename.pkgnam}).")
            }

        } catch (e: Exception) {
            Log.e("DevOpsDatabase", "Error initializing database DevOps: ${e.message}")
        }

    }

    private fun createTableIfNotExists() {
        try {
            db?.execSQL(
                "CREATE TABLE IF NOT EXISTS \"t${getCurrentDate().replace("-","")}\" (" +
                        "fileName TEXT," +
                        "className TEXT," +
                        "function TEXT," +
                        "type TEXT," +
                        "line TEXT," +
                        "message TEXT," +
                        "toe TEXT" +
                        ");"
            )
            Log.d("DevOpsDatabase", "Table created successfully.")
        } catch (e: Exception) {
            Log.e("DevOpsDatabase", "Error creating table: ${e.message}")
        }
    }
}

val DevOps = DevOpsDatabaseConnectionEstablisher()
