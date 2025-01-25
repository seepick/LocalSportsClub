package seepick.localsportsclub.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Rule

abstract class UiTest {
    @get:Rule
    val compose = createComposeRule()

    fun content(content: @Composable () -> Unit) {
        compose.setContent {
            content()
        }
        runBlocking(Dispatchers.Main) {
            compose.awaitIdle()
        }
    }

    fun uiTest(testCode: () -> Unit) {
        testCode()
    }
}
