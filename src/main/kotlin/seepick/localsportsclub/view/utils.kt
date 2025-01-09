package seepick.localsportsclub.view

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import seepick.localsportsclub.openFromClasspath
import java.io.File

@OptIn(ExperimentalResourceApi::class)
fun readImageBitmapFromClasspath(classpath: String): ImageBitmap =
    openFromClasspath(classpath).readAllBytes().decodeToImageBitmap()

@OptIn(ExperimentalResourceApi::class)
fun readImageBitmapFromFile(file: File): ImageBitmap =
    file.inputStream().readAllBytes().decodeToImageBitmap()
