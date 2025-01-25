package seepick.localsportsclub.view.usage

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import org.junit.Test
import seepick.localsportsclub.view.UiTest
import seepick.localsportsclub.view.common.CheckboxTexted

class CheckboxTextedUiTest : UiTest() {

    @Test
    fun `When text clicked Then state changed`() = uiTest {
        val checked = mutableStateOf(false)
        content {
            CheckboxTexted(
                label = "label",
                checked = checked,
                textFieldTestTag = "text"
            )
        }

        val textNode = compose.onNodeWithTag("text", useUnmergedTree = true)
        textNode.assertIsDisplayed()
        textNode.assertTextEquals("label")
        textNode.performClick()
        checked.value shouldBe true
    }
}
