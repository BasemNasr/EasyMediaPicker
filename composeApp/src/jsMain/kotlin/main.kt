import androidx.compose.runtime.Composable
import com.bn.easymediapicker.demo.App
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        App()
    }
}
