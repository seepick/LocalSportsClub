package seepick.localsportsclub.view.usage

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import seepick.localsportsclub.view.common.CheckboxTexted

class CheckboxTextedTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `When text clicked Then state changed`() {
//        runBlocking(Dispatchers.Main) {
        val checked = mutableStateOf(false)
        compose.setContent {
            CheckboxTexted(
                label = "label",
                checked = checked,
                testTagText = "text"
            )
        }
//            compose.awaitIdle()

        val textNode = compose.onNodeWithTag("text", useUnmergedTree = true)
        textNode.assertIsDisplayed()
        textNode.assertTextEquals("labelNOPE")
        textNode.performClick()
        checked.value shouldBe true
    }
//    }
}
