package seepick.localsportsclub.view.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import org.junit.Before
import seepick.localsportsclub.service.search.BooleanSearchOption
import seepick.localsportsclub.view.UiTest

class BooleanSearchFieldUiTest : UiTest() {

    private val label = "booleanSearchLabel"
    private val labelTagName = "labelTagName"
    private val checkboxTagName = "checkboxTagName"
    private var resetCounter = 0
    private val resetCountCallback: () -> Unit = { resetCounter++ }

    @Before
    fun beforeEach() {
        resetCounter = 0
    }

    //    @Test
    fun `When click checkbox Then search value changed`() = uiTest {
        val option = buildOptionAndSetContent(enabled = true, value = false)
        resetCounter shouldBe 0

        val checkbox = compose.onNodeWithTag(checkboxTagName, useUnmergedTree = true)
        checkbox.assertIsDisplayed()
        checkbox.performClick()
        option.searchBoolean shouldBe true
        resetCounter shouldBe 1
    }

    //    @Test
    fun `When click label Then enable flag toggled`() = uiTest {
        val option = buildOptionAndSetContent(enabled = false)
        resetCounter shouldBe 0

        val label = compose.onNodeWithTag(labelTagName, useUnmergedTree = true)
        label.assertIsDisplayed()
        label.performClick()
        option.enabled shouldBe true
        resetCounter shouldBe 1
    }

    private fun buildOptionAndSetContent(enabled: Boolean = true, value: Boolean = false) =
        buildOption(enabled = enabled, value = value).also {
            prepareContent(it)
        }

    private fun buildOption(enabled: Boolean = true, value: Boolean = false) = BooleanSearchOption<Boolean>(
        label = label,
        extractor = { it },
        reset = resetCountCallback,
        initialValue = value,
        initiallyEnabled = enabled,
    )

    private fun prepareContent(option: BooleanSearchOption<Boolean>) {
        content {
            BooleanSearchField(option, labelTestTag = labelTagName, checkboxTestTag = checkboxTagName)
        }
    }
}
