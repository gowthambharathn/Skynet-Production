package skynetbee.developers.production

import android.annotation.SuppressLint
import android.util.Log
import net.zetetic.database.sqlcipher.SQLiteDatabase
import skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background.Packagename
import java.io.File


open class NeuralMemoryConnectionEstablisher {

    var db: SQLiteDatabase? = null

    init {
        System.loadLibrary("sqlcipher")
        initializeDatabase()
    }


    fun executeQuery(query: String): Pair<Boolean, List<Map<String, String>>?> {
        if (db == null || !db!!.isOpen) {
            Log.e("NM_ERROR", "Database is null or closed")
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
                            val value = it.getString(i) ?: "NULL"
                            row[columnName] = value
                        }
                        rows.add(row)
                    }
                }

                Log.d("NM_QUERY", "Executed SELECT query: $query, Rows fetched: ${rows.size}")
                Pair(rows.isNotEmpty(), rows)
            } else {
                db?.execSQL(query)
                Log.d("NM_QUERY", "Executed non-SELECT query: $query")
                Pair(true, null)
            }
        } catch (e: Exception) {
            Log.e("NM_ERROR", "Error executing query: $query, Exception: ${e.message}")
            Pair(false, null)
        }
    }




    @SuppressLint("SdCardPath")
    private fun initializeDatabase() {


        val dbPath1 = "/data/data/${Packagename.pkgnam}/Databases/NeuralMemory.db"
        val dbFile1 = File(dbPath1)
        if (!dbFile1.parentFile?.exists()!!) {
            if (dbFile1.parentFile?.mkdirs()!!) {
                Log.d("NM-Database", "Directory NeuralMemory created successfully in this package(${Packagename.pkgnam}).")
            } else {
                Log.e("NM-Database", "Failed to create directory NeuralMemory in this package(${Packagename.pkgnam}).")
                return
            }
        }

        try {
            // Check if the database file already exists
            if (dbFile1.exists()) {
                Log.d("NM-Database", "NeuralMemory Database already exists. Skipping creation in this package(${Packagename.pkgnam}).")
            } else {
                Log.d("NM-Database", "Creating new database NeuralMemory in this package(${Packagename.pkgnam}).")
            }
            db = SQLiteDatabase.openDatabase(
                dbPath1,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY,
                null
            )

            db?.rawExecSQL("PRAGMA key = '123';")
            Log.d("NM-Database", "NeuralMemory Database instance: $db")

            if (db != null) {
                Log.d("NM-Database", "NeuralMemory Database initialized successfully in this package(${Packagename.pkgnam}).")
            } else {
                Log.e("NM-Database", "NeuralMemory Database initialization failed in this package(${Packagename.pkgnam}).")
            }

        } catch (e: Exception) {
            Log.e("NM-Database", "Error initializing database NeuralMemory: ${e.message}")
        }
    }
}



