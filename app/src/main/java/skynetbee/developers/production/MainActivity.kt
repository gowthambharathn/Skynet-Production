package skynetbee.developers.production

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background.Packagename

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Packagename.init(packageName)

        testDatabase()
    }

    private fun testDatabase() {

        if (DF.db == null || !DF.db!!.isOpen) {
            Log.e("DB_TEST", "❌ Database not opened")
            return
        }

        // Create table if it doesn't exist
        val createResult = DF.executeQuery("""
        CREATE TABLE IF NOT EXISTS test_table (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL
        );
    """.trimIndent())

        Log.d("DB_TEST", "Create Table: $createResult")

        val result = sql.insert(
            tableName = "all_system_developer_details",
            kvp = mapOf(
                "official_name" to "Gowtham Barath",
                "email" to "gowtham@example.com",
                "phone" to "9876543210",
                "unique_member_id" to "DEV001",
                "otp" to "123456",
                "overallstars" to "5",
                "cp" to "100",
                "rank" to "Developer"
            )
        )

        Log.d("DB_TEST", result.toString())
    }
}