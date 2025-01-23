package seepick.localsportsclub.service

data class WindowPref(
    val width: Int,
    val height: Int,
    val posX: Int,
    val posY: Int,
) {
    companion object {
        val default = WindowPref(
            width = 1500,
            height = 1200,
            posX = 100,
            posY = 100,
        )
    }
}
