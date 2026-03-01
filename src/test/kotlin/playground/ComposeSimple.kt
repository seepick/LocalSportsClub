package playground

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.view.common.launchBackgroundTask
import java.io.File

class SimpleViewModel : ViewModel() {
    var count by mutableStateOf(0)
        private set

    fun onButtonClicked() {
        launchBackgroundTask("foo", NoopFileResolver) {
            delay(500)
            count++
        }
    }
}

object NoopFileResolver : FileResolver {
    override fun resolve(entry: FileEntry): File {
        TODO("not implemented")
    }

    override fun resolve(entry: DirectoryEntry): File {
        TODO("not implemented")
    }
}

object Foo {
    @JvmStatic
    fun main(args: Array<String>) {
        application {
            KoinApplication(application = {
                modules(
                    module {
                        singleOf(::SimpleViewModel)
                    })
            }) {
                Window(
                    onCloseRequest = ::exitApplication,
                ) {
                    SimpleView()
                }
            }
        }
    }
}

@Composable
fun SimpleView(
    model: SimpleViewModel = koinInject(),
) {
    Row {
        Text("model.count = ${model.count}")
        Button(onClick = {
            model.onButtonClicked()
        }) {
            Text("Click Me")
        }
    }
}
