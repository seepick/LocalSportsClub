package seepick.localsportsclub.view.common.swing

import java.awt.Component
import java.awt.Toolkit

object SwingUtil {

    fun setCenterLocation(component: Component) {
        setCenterLocation(component, 0, 0)
    }

    fun setCenterLocation(component: Component, xOffset: Int, yOffset: Int) {
        val screenSize = Toolkit.getDefaultToolkit().screenSize

        val x = (screenSize.width - component.width) / 2
        val y = (screenSize.height - component.height) / 2

        component.setLocation(x + xOffset, y + yOffset)
    }
}
