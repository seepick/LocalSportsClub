package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.koin.compose.koinInject
import seepick.localsportsclub.view.common.TitleText

@Composable
fun CarouselDialog(
    model: CarouselViewModel = koinInject(),
) {
    Dialog(onDismissRequest = model::dismiss) {
        Card(
            modifier = Modifier.size(width = 800.dp, height = 400.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            CarouselView()
        }
    }
}

@Composable
fun CarouselView(
    model: CarouselViewModel = koinInject(),
) {
    LaunchedEffect(key1 = true) {
        model.loadPicUrls()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleText(model.dialogTitle, modifier = Modifier.padding(bottom = 5.dp))
        if (model.isInitialized && model.totalImageCount > 0) {
            Row {
                CarouselNaviButton(model::showPrevious, model.hasPreviousImage, Icons.AutoMirrored.Filled.ArrowBack)
                Box(modifier = Modifier.defaultMinSize(60.dp), contentAlignment = Alignment.Center) {
                    Text("${model.currentImageIndex + 1} / ${model.totalImageCount}")
                }
                CarouselNaviButton(model::showNext, model.hasNextImage, Icons.AutoMirrored.Filled.ArrowForward)
            }
        }
        Box(Modifier.fillMaxSize()) {
            if (model.isInitialized && model.currentImageData != null) {
                androidx.compose.foundation.Image(
                    bitmap = model.currentImageData!!,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                )
            } else if (model.isInitialized && model.totalImageCount == 0) {
                Text("No carousel images available", modifier = Modifier.align(Alignment.Center))
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun CarouselNaviButton(onClick: () -> Unit, isEnabled: Boolean, icon: ImageVector) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp).size(20.dp)
    ) {
        Icon(icon, contentDescription = null)
    }
}
