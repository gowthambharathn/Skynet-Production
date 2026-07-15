package skynetbee.developers.production

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background.Background
import skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background.Packagename

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Packagename.init(packageName)

        setContent {
            Background()
            GraphScreen()
        }
    }
}

@Composable
fun GraphScreen() {

    val graphData = mapOf(
        "TCS" to listOf(
            120f,
            135f,
            128f,
            150f,
            165f,
            180f
        )
    )

    OneLineGraph(
        title = "TCS Share Price",
        dataValues = graphData,
        xAxisValues = listOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun"
        ),
        yAxisValues = listOf(
            0f,
            50f,
            100f,
            150f,
            200f,
            250f
        )
    )
}