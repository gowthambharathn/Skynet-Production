package skynetbee.developers.production

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
fun Modifier.backgroundCard(): Modifier {
    return this.then(
        Modifier
            .padding(top = 10.dp, bottom = 10.dp)
            .fillMaxWidth(0.95f)
            .border(1.dp, color = Color(0x30B7B4B4), RoundedCornerShape(25.dp))
            .background(Color.LightGray.copy(0.1f), shape = RoundedCornerShape(25.dp))
    )
}