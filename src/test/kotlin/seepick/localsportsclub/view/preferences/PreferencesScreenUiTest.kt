package seepick.localsportsclub.view.preferences

import org.junit.Before
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.test.KoinTest
import seepick.localsportsclub.api.MockUscApi
import seepick.localsportsclub.view.TestableSnackbarService
import seepick.localsportsclub.view.UiTest
import seepick.localsportsclub.view.usage.DummySinglesService

class PreferencesScreenUiTest : UiTest(), KoinTest {
    private var snackbarService = TestableSnackbarService()

    @Before
    fun beforeEach() {
        snackbarService = TestableSnackbarService()
    }

    fun `When foo`() = uiTest {
        val vm = PreferencesViewModel(
            singlesService = DummySinglesService(),
            uscApi = MockUscApi(),
            snackbarService = snackbarService,
        )
        // TODO work in progress...
        // LocalViewModelStoreOwner
//        startKoin {
//            modules(module {
//                viewModel { vm }
//            })
//        }
//        declare { vm }

        content {
            KoinApplication(application = {
                modules(module {
                    viewModel { vm }
                })
            }) {
                PreferencesScreen()
            }
        }
        println("yay")
    }
}
