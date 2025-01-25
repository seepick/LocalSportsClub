package seepick.localsportsclub.view.common.swing

import java.awt.Toolkit
import javax.swing.ImageIcon

enum class SwingImage(val classpath: String) {
    Warning("/icons/dialog_warning.png"),
    Error("/icons/dialog_error.png"),
}

object SwingImageFactory {

    private val iconsCache: MutableMap<String, ImageIcon> = HashMap()

    fun getImage(image: SwingImage): ImageIcon =
        loadImage(image.classpath)

    private fun loadImage(imagePath: String): ImageIcon {
        if (iconsCache[imagePath] != null) {
            return iconsCache[imagePath]!!
        }

        val imageUrl = SwingImageFactory::class.java.getResource(imagePath)
            ?: throw RuntimeException("Could not load image by path '$imagePath'!")

        val image = ImageIcon(Toolkit.getDefaultToolkit().getImage(imageUrl))
        iconsCache[imagePath] = image

        return image
    }
}
