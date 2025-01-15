package seepick.localsportsclub.view.common

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

fun showErrorDialog(
    title: String,
    message: String,
    exception: Throwable?,
) {
    ErrorDialog(title, message, exception).isVisible = true
}

private class ErrorDialog(
    title: String,
    message: String,
    exception: Throwable?,
) : JDialog(null as JDialog?, title, true) {

    private val panelDetails = JPanel()
    private val btnDetails = JButton(DETAILS_SHOW)
    private val stackTraceText = JTextArea()

    init {
        this.defaultCloseOperation = DO_NOTHING_ON_CLOSE
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                doClose()
            }
        })

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ESCAPE) {
                    doClose()
                }
            }
        })


        val panel = JPanel(BorderLayout())
//        panel.background = backgroundColor
        panel.border = BorderFactory.createEmptyBorder(10, 16, 10, 16) // top left bottom right
        panel.add(this.initComponents(message, exception), BorderLayout.CENTER)

        if (exception != null) {
            panel.add(this.createExceptionPanel(exception), BorderLayout.SOUTH)
        }
        this.contentPane.add(panel)

        this.pack()
        this.isResizable = false
        SwingUtil.setCenterLocation(this)
    }

    private fun initComponents(message: String, exception: Throwable?): JPanel {
        val wrapPanel = JPanel(BorderLayout())

        val westPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        westPanel.border = BorderFactory.createEmptyBorder(0, 0, 0, 10)
        westPanel.add(JLabel(SwingImageFactory.dialogError))

        val centerPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val messageTxt = JTextArea(message)
        messageTxt.isEditable = false
        messageTxt.lineWrap = true
        messageTxt.wrapStyleWord = true
        //		txt.setOpaque(true);
//		txt.setBackground(Color.WHITE);
        val scrolledMessageTxt = JScrollPane(messageTxt)
        //		txtScroll.setOpaque(false);
//		txtScroll.setBorder(BorderFactory.createEmptyBorder());
        scrolledMessageTxt.preferredSize = Dimension(650, 120)
        centerPanel.add(scrolledMessageTxt)

        val southPanel = JPanel(FlowLayout(FlowLayout.RIGHT))

        // JButton btnReport = new JButton("Send Report...");
//		southPanel.add(btnReport); // ... could implement Send Report...
        val btnClose = JButton("Close")
        southPanel.add(btnClose)

        if (exception != null) {
            southPanel.add(this.btnDetails)
        }
        btnClose.addActionListener { doClose() }

        btnDetails.addActionListener { doToggleDetails() }
        getRootPane().defaultButton = btnClose

        westPanel.isOpaque = false
        southPanel.isOpaque = false
        centerPanel.isOpaque = false
        wrapPanel.isOpaque = false

        wrapPanel.add(westPanel, BorderLayout.WEST)
        wrapPanel.add(southPanel, BorderLayout.SOUTH)
        wrapPanel.add(centerPanel, BorderLayout.CENTER)
        return wrapPanel
    }

    private fun createExceptionPanel(exception: Throwable): JPanel {
        panelDetails.layout = BorderLayout(0, 5)
        panelDetails.isVisible = false

        val detailText: String = ExceptionUtil.convertExceptionToString(exception)
        stackTraceText.text = detailText
        stackTraceText.rows = 15
        stackTraceText.columns = 60

        val btnCopyClipboard = JButton("Copy to Clipboard")
        btnCopyClipboard.addActionListener { doCopyClipboard() }

        panelDetails.add(JScrollPane(this.stackTraceText), BorderLayout.CENTER)
        val southPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        southPanel.add(btnCopyClipboard)
        panelDetails.add(southPanel, BorderLayout.SOUTH)

        panelDetails.isOpaque = false
        southPanel.isOpaque = false

        return this.panelDetails
    }

    private fun doCopyClipboard() {
        val stringSelection = StringSelection(stackTraceText.text)
        val systemClipboard = Toolkit.getDefaultToolkit().systemClipboard
        systemClipboard.setContents(stringSelection) { _, _ ->
            // can be ignored
        }
    }

    private fun doClose() {
        this.dispose()
    }

    private fun doToggleDetails() {
        btnDetails.text = if (panelDetails.isVisible) DETAILS_SHOW else DETAILS_HIDE
        panelDetails.isVisible = !panelDetails.isVisible
        this.pack()
    }

    companion object {

        private const val DETAILS_SHOW = "Details >>"
        private const val DETAILS_HIDE = "Details <<"


        @JvmStatic
        fun main(args: Array<String>) {
            val cause = Exception("ganz unten")
            val cause2 = Exception("rums", cause)
            val ex = Exception("bla blu", cause2)

            val message = "This is a longer message explaining what it is."
            ErrorDialog("Some Title!", message, ex).isVisible = true
        }
    }
}

interface IPtEscapeDisposeReceiver {
    fun doEscape()
}

object PtEscapeDisposer {
    private val log = logger {}

    fun enableEscapeOnDialogWithoutFocusableComponents(
        dialog: JDialog,
        receiver: IPtEscapeDisposeReceiver
    ) {
        dialog.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ESCAPE) {
                    receiver.doEscape()
                }
            }
        })
    }


}

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

object ExceptionUtil {

    fun convertExceptionToString(throwable: Throwable, withCause: Boolean = false): String {
        val sb = StringBuffer()
        sb.append(convertSingleException(throwable))

        if (withCause) {
            var cause = throwable.cause
            while (cause != null) {
                sb.append("Caused by: ")
                sb.append(convertSingleException(cause))
                cause = cause.cause
            }
        }

        return sb.toString()
    }

    fun convertSingleException(t: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        return sw.toString()
    }
}

object SwingImageFactory {

    val dialogWarning: ImageIcon
        get() = loadImage("/icons/dialog_warning.png")

    val dialogError: ImageIcon
        get() = loadImage("/icons/dialog_error.png")

    private val iconsCache: MutableMap<String, ImageIcon> = HashMap()

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
