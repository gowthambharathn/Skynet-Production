package skynetbee.developers.production

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background.Background1
import skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background.Packagename

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Packagename.init(packageName)

        setContent {
            Background1()
            testDevOpsDatabase()

        }
    }
}


private fun testDeviceFingerPrintDatabase() {

    Log.d("TEST", "========== DeviceFingerPrint Test Started ==========")

    // Insert Test Data
    DF.executeQuery("""
        INSERT INTO all_system_developer_details(
            official_name,
            email,
            phone,
            unique_member_id,
            otp,
            overallstars,
            cp,
            rank,
            area
        )
        VALUES(
            'Gowtham',
            'gowtham@gmail.com',
            '9876543210',
            'DEV001',
            '123456',
            '5',
            '1500',
            '1',
            'Android'
        )
    """.trimIndent())

    Log.d("TEST", "Data Inserted")

    // Read All Records
    var row = DF.select("* FROM all_system_developer_details")

    while (row != null) {

        Log.d("TEST", "--------------------------------")
        Log.d("TEST", "Name      : ${row["official_name"]}")
        Log.d("TEST", "Email     : ${row["email"]}")
        Log.d("TEST", "Phone     : ${row["phone"]}")
        Log.d("TEST", "Member ID : ${row["unique_member_id"]}")
        Log.d("TEST", "OTP       : ${row["otp"]}")
        Log.d("TEST", "Stars     : ${row["overallstars"]}")
        Log.d("TEST", "CP        : ${row["cp"]}")
        Log.d("TEST", "Rank      : ${row["rank"]}")
        Log.d("TEST", "Area      : ${row["area"]}")

        row = DF.select("* FROM all_system_developer_details")
    }

    // Update Record
    DF.executeQuery("""
        UPDATE all_system_developer_details
        SET official_name='Prince'
        WHERE unique_member_id='DEV001'
    """.trimIndent())

    Log.d("TEST", "Data Updated")

    // Delete Record
    DF.executeQuery("""
        DELETE FROM all_system_developer_details
        WHERE unique_member_id='DEV001'
    """.trimIndent())

    Log.d("TEST", "Data Deleted")

    Log.d("TEST", "========== DeviceFingerPrint Test Completed ==========")
}

private fun testNeuralMemoryDatabase() {

    cl("========== NeuralMemory Test Started ==========")

    // Create Table
    NM.executeQuery("""
        CREATE TABLE IF NOT EXISTS test_table(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            age TEXT
        );
    """.trimIndent())

    cl("Table Created")

    // Insert
    NM.executeQuery("""
        INSERT INTO test_table(name, age)
        VALUES('Gowtham', '20');
    """.trimIndent())

    cl("Data Inserted")

    // Select
    val result = NM.executeQuery("""
        SELECT * FROM test_table;
    """.trimIndent())

    if (result.first) {

        result.second?.forEach { row ->

            cl("----------------------------")
            cl("ID   : ${row["id"]}")
            cl("Name : ${row["name"]}")
            cl("Age  : ${row["age"]}")
        }

    } else {
        cl("No Data Found")
    }

    // Update
    NM.executeQuery("""
        UPDATE test_table
        SET age='21'
        WHERE name='Gowtham';
    """.trimIndent())

    cl("Data Updated")

    // Verify Update
    val updated = NM.executeQuery("""
        SELECT * FROM test_table;
    """.trimIndent())

    updated.second?.forEach { row ->
        cl("Updated -> Name: ${row["name"]}, Age: ${row["age"]}")
    }

    // Delete
    NM.executeQuery("""
        DELETE FROM test_table
        WHERE name='Gowtham';
    """.trimIndent())

    cl("Data Deleted")

    // Verify Delete
    val deleted = NM.executeQuery("""
        SELECT * FROM test_table;
    """.trimIndent())

    if (deleted.second.isNullOrEmpty()) {
        cl("Table is Empty")
    } else {
        deleted.second?.forEach { row ->
            cl("Remaining -> $row")
        }
    }

    cl("========== NeuralMemory Test Completed ==========")
}

private fun testDevOpsDatabase() {

    val result = sql.insert(
        tableName = "test_table",
        kvp = mapOf(
            "name" to "Gowtham",
            "age" to "20"
        )
    )

    cl(result)
}


