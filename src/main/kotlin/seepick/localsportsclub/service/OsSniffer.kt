package seepick.localsportsclub.service

object OsSniffer {
    val os = if (System.getProperty("os.name") == "Mac OS X") Os.mac else Os.win //"dmg" else "exe"

    enum class Os(val installerSuffix: String) {
        mac("dmg"), win("exe")
    }
}
