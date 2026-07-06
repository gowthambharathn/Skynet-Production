package skynetbee.developers.production

import android.annotation.SuppressLint
import android.util.Log
import java.io.File
import net.zetetic.database.sqlcipher.SQLiteDatabase
import skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background.Packagename

class DeviceFingerPrintConnectionEstablisher {

    var db: SQLiteDatabase? = null

    init {
        System.loadLibrary("sqlcipher")
        initializeDatabase()
    }
    val dfsqt = mutableListOf<SqlTracker>()
    fun select(qry: String, file: String = Thread.currentThread().stackTrace[3].fileName, line: Int = Thread.currentThread().stackTrace[3].lineNumber): Map<String, String>? {
        val query = "SELECT $qry"

        val fileName = file.substringAfterLast("/")
        val lineNumber = " @ $line"
        val track = fileName + lineNumber

        for (sqlTracer in dfsqt) {
            if (sqlTracer.tracker == track) {
                if (sqlTracer.query == query) {
                    return if (sqlTracer.tableData.indices.contains(sqlTracer.nextRow)) {
                        sqlTracer.tableData[sqlTracer.nextRow++]
                    } else {
                        null
                    }
                } else {
                    Log.d("DF_SELECT","SQL Error thrown by Sqlize.select : tracker : $track reused for ${sqlTracer.query} and $query")
                    return null
                }
            }
        }

        val eR = executeQuery(query)
        return if (eR.first) {
            eR.second?.let {
                dfsqt.add(0, SqlTracker(track, it, query))
                if (it.isNotEmpty()) {
                    dfsqt[0].nextRow = 1
                    it[0]
                } else {
                    null
                }
            } ?: run {
                Log.d("DF_SELECT", "SQL Error thrown by Sqlize.select : No Data Returned by Database for Tracker : $track and Query : $query")
                null
            }
        } else {
            Log.d("DF_SELECT", "SQL Error thrown by Sqlize.select : Execution Failed for Tracker : $track and Query : $query")
            null
        }
    }

    fun executeQuery(query: String): Pair<Boolean, List<Map<String, String>>?> {
        Log.d("DF_QUERY", "Executing: $query")

        if (db == null || !db!!.isOpen) {
            Log.e("DF_QUERY", "Database not initialized or not open")
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
                            row[it.getColumnName(i)] = it.getString(i) ?: "NULL"
                        }
                        rows.add(row)
                    }
                }

                Log.d("DF_QUERY", "Query executed successfully: ${rows.size} rows fetched")
                Pair(rows.isNotEmpty(), rows)
            } else {
                db?.execSQL(query)
                Log.d("DF_QUERY", "Non-SELECT query executed successfully")
                Pair(true, null)
            }
        } catch (e: Exception) {
            Log.e("DF_QUERY", "Error executing query: ${e.localizedMessage}", e)
            Pair(false, null)
        }
    }

    @SuppressLint("SdCardPath")
    private fun initializeDatabase() {

        val dbPath1 = "/data/data/${Packagename.pkgnam}/Databases/DeviceFingerPrint.db"
        val dbFile1 = File(dbPath1)

        if (!dbFile1.parentFile?.exists()!!) {
            if (dbFile1.parentFile?.mkdirs()!!) {
                Log.d("DF-Database", "Directory DeviceFingerPrint created successfully in this package(${Packagename.pkgnam}).")
            } else {
                Log.e("DF-Database", "Failed to create directory DeviceFingerPrint in this package(${Packagename.pkgnam}).")
                return
            }
        }

        try {
            if (dbFile1.exists()) {
                Log.d("DF-Database", "DeviceFingerPrint Database already exists. Skipping creation in this package(${Packagename.pkgnam}).")
            } else {
                Log.d("DF-Database", "Creating new database DeviceFingerPrint in this package(${Packagename.pkgnam}).")
            }

            db = SQLiteDatabase.openDatabase(
                dbPath1,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY,
                null
            )

            db?.rawExecSQL("PRAGMA key = '123';")
            Log.d("DF-Database", "DeviceFingerPrint Database instance: $db")

            if (db != null) {
                Log.d("DF-Database", "DeviceFingerPrint Database initialized successfully in this package(${Packagename.pkgnam}).")
                createTableIfNotExists()
            } else {
                Log.e("DF-Database", "DeviceFingerPrint Database initialization failed in this package(${Packagename.pkgnam}).")
            }

        } catch (e: Exception) {
            Log.e("DF-Database", "Error initializing database DeviceFingerPrint: ${e.message} ")
        }

    }
    private fun createTableIfNotExists() {
        try {
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS all_system_projects_assigned_to_developers (
                    projectcode TEXT,
                    company TEXT,
                    hardnesslevel TEXT,
                    category TEXT,
                    project_name TEXT,
                    wing TEXT,
                    prolink TEXT,
                    prodes TEXT,
                    dat TEXT,
                    deadlinedat TEXT,
                    completedat TEXT,
                    delayed_days TEXT,
                    creditpoints TEXT,
                    marks TEXT,
                    developer_unique_member_id TEXT,
                    official_name TEXT,
                    rating TEXT,
                    tbl TEXT,
                    newtbl TEXT,
                    updation TEXT,
                    area TEXT,
                    counti INTEGER PRIMARY KEY AUTOINCREMENT,
                    mcounti TEXT,
                    fromdat TEXT,
                    ftodat TEXT NOT NULL DEFAULT '0000-00-00',
                    ftotim TEXT NOT NULL DEFAULT '00:00:00',
                    ftovername TEXT,
                    ftover TEXT,
                    ftopid TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00',
                    totim TEXT NOT NULL DEFAULT '00:00:00',
                    tovername TEXT,
                    tover TEXT,
                    topid TEXT,
                    ipmac TEXT,
                    deviceanduserainfo TEXT,
                    basesite TEXT,
                    owncomcode TEXT,
                    testeridentity TEXT,
                    testcontrol TEXT,
                    adderpid TEXT,
                    addername TEXT,
                    adder TEXT,
                    doe TEXT,
                    toe TEXT
                );
            """)


            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS all_system_developer_details (
                    official_name TEXT,
                    email TEXT,
                    phone TEXT,
                    unique_member_id TEXT,
                    otp TEXT,
                    overallstars TEXT,
                    cp TEXT,
                    rank TEXT,
                    area TEXT,
                    counti INTEGER PRIMARY KEY AUTOINCREMENT,
                    mcounti TEXT,
                    fromdat TEXT,
                    ftodat TEXT NOT NULL DEFAULT '0000-00-00',
                    ftotim TEXT NOT NULL DEFAULT '00:00:00',
                    ftovername TEXT,
                    ftover TEXT,
                    ftopid TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00',
                    totim TEXT NOT NULL DEFAULT '00:00:00',
                    tovername TEXT,
                    tover TEXT,
                    topid TEXT,
                    ipmac TEXT,
                    deviceanduserainfo TEXT,
                    basesite TEXT,
                    owncomcode TEXT,
                    testeridentity TEXT,
                    testcontrol TEXT,
                    adderpid TEXT,
                    addername TEXT,
                    adder TEXT,
                    doe TEXT,
                    toe TEXT
                );
            """)


            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS all_system_leaderboard (
                    official_photo TEXT,
                    fname TEXT,
                    fphoto TEXT,
                    sname TEXT,
                    sphoto TEXT,
                    tname TEXT,
                    tphoto TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00'
                );
            """)

            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS last_communication_with_server (
                    otp TEXT,
                    area TEXT,
                    currentdoe DATE NOT NULL DEFAULT '0000-00-00',
                    currenttoe TIME NOT NULL DEFAULT '00:00:00',
                    localcounti INTEGER PRIMARY KEY AUTOINCREMENT,
                    counti INTEGER,
                    mcounti INTEGER,
                    fromdat TEXT,
                    ftodat TEXT NOT NULL DEFAULT '0000-00-00',
                    ftotim TEXT NOT NULL DEFAULT '00:00:00',
                    ftovername TEXT,
                    ftover TEXT,
                    ftopid TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00',
                    totim TEXT NOT NULL DEFAULT '00:00:00',
                    tovername TEXT,
                    tover TEXT,
                    topid TEXT,
                    ipmac TEXT,
                    deviceanduserainfo TEXT,
                    basesite TEXT,
                    owncomcode TEXT,
                    testeridentity TEXT,
                    testcontrol TEXT,
                    adderpid TEXT,
                    addername TEXT,
                    adder TEXT,
                    doe TEXT,
                    toe TEXT NOT NULL DEFAULT '0000-00-00'
                );
            """)

            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS last_page_worked_on (
                    pagename TEXT,
                    area TEXT,
                    currentdoe DATE NOT NULL DEFAULT '0000-00-00',
                    currenttoe TIME NOT NULL DEFAULT '00:00:00',
                    localcounti INTEGER PRIMARY KEY AUTOINCREMENT,
                    counti INTEGER,
                    mcounti INTEGER,
                    fromdat TEXT,
                    ftodat TEXT NOT NULL DEFAULT '0000-00-00',
                    ftotim TEXT NOT NULL DEFAULT '00:00:00',
                    ftovername TEXT,
                    ftover TEXT,
                    ftopid TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00',
                    totim TEXT NOT NULL DEFAULT '00:00:00',
                    tovername TEXT,
                    tover TEXT,
                    topid TEXT,
                    ipmac TEXT,
                    deviceanduserainfo TEXT,
                    basesite TEXT,
                    owncomcode TEXT,
                    testeridentity TEXT,
                    testcontrol TEXT,
                    adderpid TEXT,
                    addername TEXT,
                    adder TEXT,
                    doe DATE NOT NULL DEFaULT '0000-00-00',
                    toe TIME NOT NULL DEFAULT '0000-00-00'
                );
            """)

            db?.execSQL("""
                    CREATE TABLE IF NOT EXISTS send_msg_via_unique_member_id (
                    uniquememberid TEXT,
                    message TEXT,
                    area TEXT,
                    localcounti INTEGER PRIMARY KEY AUTOINCREMENT,
                    mcounti TEXT,
                    fromdat TEXT,
                    ftodat TEXT NOT NULL DEFAULT '0000-00-00',
                    ftotim TEXT NOT NULL DEFAULT '00:00:00',
                    ftovername TEXT,
                    ftover TEXT,
                    ftopid TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00',
                    totim TEXT NOT NULL DEFAULT '00:00:00',
                    tovername TEXT,
                    tover TEXT,
                    topid TEXT,
                    ipmac TEXT,
                    deviceanduserainfo TEXT,
                    basesite TEXT,
                    owncomcode TEXT,
                    testeridentity TEXT,
                    testcontrol TEXT,
                    adderpid TEXT,
                    addername TEXT,
                    adder TEXT,
                    doe TEXT,
                    toe TEXT
                );
            """)

            db?.execSQL("""
                    CREATE TABLE IF NOT EXISTS send_msg_via_sms (
                    phonenumber TEXT,
                    message TEXT,
                    area TEXT,
                    localcounti INTEGER PRIMARY KEY AUTOINCREMENT,
                    mcounti TEXT,
                    fromdat TEXT,
                    ftodat TEXT NOT NULL DEFAULT '0000-00-00',
                    ftotim TEXT NOT NULL DEFAULT '00:00:00',
                    ftovername TEXT,
                    ftover TEXT,
                    ftopid TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00',
                    totim TEXT NOT NULL DEFAULT '00:00:00',
                    tovername TEXT,
                    tover TEXT,
                    topid TEXT,
                    ipmac TEXT,
                    deviceanduserainfo TEXT,
                    basesite TEXT,
                    owncomcode TEXT,
                    testeridentity TEXT,
                    testcontrol TEXT,
                    adderpid TEXT,
                    addername TEXT,
                    adder TEXT,
                    doe TEXT,
                    toe TEXT
                );
            """)

            db?.execSQL("""
                    CREATE TABLE IF NOT EXISTS send_msg_via_whatsapp (
                    whatsappnumber TEXT,
                    message TEXT,
                    area TEXT,
                    localcounti INTEGER PRIMARY KEY AUTOINCREMENT,
                    mcounti TEXT,
                    fromdat TEXT,
                    ftodat TEXT NOT NULL DEFAULT '0000-00-00',
                    ftotim TEXT NOT NULL DEFAULT '00:00:00',
                    ftovername TEXT,
                    ftover TEXT,
                    ftopid TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00',
                    totim TEXT NOT NULL DEFAULT '00:00:00',
                    tovername TEXT,
                    tover TEXT,
                    topid TEXT,
                    ipmac TEXT,
                    deviceanduserainfo TEXT,
                    basesite TEXT,
                    owncomcode TEXT,
                    testeridentity TEXT,
                    testcontrol TEXT,
                    adderpid TEXT,
                    addername TEXT,
                    adder TEXT,
                    doe TEXT,
                    toe TEXT
                );
            """)

            db?.execSQL("""
                    CREATE TABLE IF NOT EXISTS send_msg_via_email(
                    email TEXT,
                    subject TEXT,
                    body TEXT,
                    area TEXT,
                    localcounti INTEGER PRIMARY KEY AUTOINCREMENT,
                    mcounti TEXT,
                    fromdat TEXT,
                    ftodat TEXT NOT NULL DEFAULT '0000-00-00',
                    ftotim TEXT NOT NULL DEFAULT '00:00:00',
                    ftovername TEXT,
                    ftover TEXT,
                    ftopid TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00',
                    totim TEXT NOT NULL DEFAULT '00:00:00',
                    tovername TEXT,
                    tover TEXT,
                    topid TEXT,
                    ipmac TEXT,
                    deviceanduserainfo TEXT,
                    basesite TEXT,
                    owncomcode TEXT,
                    testeridentity TEXT,
                    testcontrol TEXT,
                    adderpid TEXT,
                    addername TEXT,
                    adder TEXT,
                    doe TEXT,
                    toe TEXT
                );
            """)

            db?.execSQL("""
            CREATE TABLE forge_login_with_history (
                    unique_member_id TEXT,
                    official_name TEXT,
                    photo TEXT,
                    membertype TEXT,
                    wtype TEXT,
                    wcatagory TEXT,
                    filename TEXT,
                    area TEXT,
                    localcounti INTEGER PRIMARY KEY AUTOINCREMENT,
                    mcounti TEXT,
                    fromdat TEXT,
                    ftodat TEXT NOT NULL DEFAULT '0000-00-00',
                    ftotim TEXT NOT NULL DEFAULT '00:00:00',
                    ftovername TEXT,
                    ftover TEXT,
                    ftopid TEXT,
                    todat TEXT NOT NULL DEFAULT '0000-00-00',
                    totim TEXT NOT NULL DEFAULT '00:00:00',
                    tovername TEXT,
                    tover TEXT,
                    topid TEXT,
                    ipmac TEXT,
                    deviceanduserainfo TEXT,
                    basesite TEXT,
                    owncomcode TEXT,
                    testeridentity TEXT,
                    testcontrol TEXT,
                    adderpid TEXT,
                    addername TEXT,
                    adder TEXT,
                    doe TEXT,
                    toe TEXT
                    );""".trimIndent() )

            db?.execSQL("""
                    CREATE TABLE IF NOT EXISTS draft_data_in_fill_from_database(
                    id TEXT,
                    "key" TEXT,
                    value TEXT
                );
            """)
            Log.d("DevOpsDatabase", "Table created successfully.")
        } catch (e: Exception) {
            Log.e("DevOpsDatabase", "Error creating table: ${e.message}")
        }
    }
}

val DF = DeviceFingerPrintConnectionEstablisher()