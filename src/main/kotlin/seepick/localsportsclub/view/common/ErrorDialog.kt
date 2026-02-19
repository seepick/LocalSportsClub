package seepick.localsportsclub.view.common

import seepick.localsportsclub.AppPropertiesProvider
import seepick.localsportsclub.readRecentLogEntries
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.view.common.swing.SwingImage
import seepick.localsportsclub.view.common.swing.SwingImageFactory
import seepick.localsportsclub.view.common.swing.SwingUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextPane
import javax.swing.event.HyperlinkEvent

fun showErrorDialog(
    message: String,
    exception: Throwable?,
    fileResolver: FileResolver,
    imageType: SwingImage = SwingImage.Error,
) {
    ErrorDialog(
        title = "Error",
        message = message,
        exception = exception,
        imageType = imageType,
        logLines = readRecentLogEntries(linesToRead = 30, logsDir = fileResolver.resolve(DirectoryEntry.Logs)),
    ).isVisible = true
}

class ErrorDialog(
    title: String,
    message: String,
    exception: Throwable?,
    imageType: SwingImage,
    logLines: String?,
) : JDialog(null as JDialog?, title, true) {

    private val labelDetailsShow = "Details >>"
    private val labelDetailsHide = "Details <<"
    private val panelDetails = JPanel()
    private val btnDetails = JButton(labelDetailsShow)
    private val btnClose = JButton("Close")
    private val btnCopyClipboard = JButton("Copy to Clipboard")
    private val detailsTextArea = JTextArea()
    private val sizeSmall = Dimension(550, 170)
    private val sizeBig = Dimension(550, 378)
    private val appVersion = AppPropertiesProvider.provide().version
    private val detailsString: String

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

        detailsString = buildString {
            appendLine(message)
            appendLine()
            appendLine("LSC version $appVersion")
            if (exception != null) {
                appendLine()
                append(exception.stackTraceToString())
            }
            logLines?.also {
                appendLine()
                appendLine(it)
            }
        }
        detailsTextArea.text = detailsString
        btnCopyClipboard.addActionListener { doCopyClipboard() }
        btnDetails.addActionListener { doToggleDetails() }
        btnClose.addActionListener { doClose() }
        getRootPane().defaultButton = btnClose

        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createEmptyBorder(10, 16, 10, 16) // top left bottom right
        panel.add(this.initComponents(message, exception, imageType), BorderLayout.NORTH)
        panel.add(this.createDetailsPanel(), BorderLayout.CENTER)
        contentPane.add(panel)

        this.size = sizeSmall
        this.isResizable = true
        SwingUtil.setCenterLocation(this)
    }

    private fun uriEncode(input: String): String = buildString {
        for (ch in input.toCharArray()) {
            append(if (Character.isLetterOrDigit(ch)) ch else String.format("%%%02X", ch.code))
        }
    }

    private fun buildMessagePane(message: String): JComponent {
        val textPane = JTextPane()
        textPane.contentType = "text/html"
        val desktop = java.awt.Desktop.getDesktop()
        val messageMail = if (desktop.isSupported(java.awt.Desktop.Action.MAIL)) {
            buildString {
                append("<br/><br/>")
                append("Please consider sending this to: ")
                val subject = uriEncode("LocalSportsClub v${appVersion} Error Report")
                val body =
                    uriEncode("Feel free to explain here a bit what happened, what you did,...\n\n=================================\n\nERROR REPORT:\n\n$detailsString")
                append("<a href=\"mailto:see.pick.mail@gmail.com?subject=$subject&body=$body\">see.pick.mail@gmail.com</a>")
            }
        } else ""
        textPane.text =
            """<html><font style="font-family:sans-serif;">$message$messageMail</font></html>"""
        textPane.isEditable = false
        textPane.addHyperlinkListener { e ->
            if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                desktop.mail(e.url.toURI())
            }
        }
        textPane.isOpaque = false
        val scrollPane = JScrollPane(textPane)
        scrollPane.viewport.isOpaque = false
        scrollPane.isOpaque = false
        scrollPane.border = null
        return scrollPane
    }

    private fun initComponents(message: String, exception: Throwable?, imageType: SwingImage): JPanel {
        val westPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        westPanel.border = BorderFactory.createEmptyBorder(0, 0, 0, 10)
        westPanel.add(JLabel(SwingImageFactory.getImage(imageType)))
        westPanel.isOpaque = false

        val southPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        // JButton btnReport = new JButton("Send Report...");
//		southPanel.add(btnReport); // ... could implement Send Report...
        southPanel.add(btnClose)
        if (exception != null) {
            southPanel.add(this.btnDetails)
        }
        southPanel.isOpaque = false

        val wrapPanel = JPanel(BorderLayout())
        wrapPanel.isOpaque = false
        wrapPanel.add(westPanel, BorderLayout.WEST)
        wrapPanel.add(southPanel, BorderLayout.SOUTH)
        wrapPanel.add(buildMessagePane(message), BorderLayout.CENTER)
        return wrapPanel
    }

    private fun createDetailsPanel(): JPanel {
        panelDetails.layout = BorderLayout(0, 5)
        panelDetails.isVisible = false

        panelDetails.add(JScrollPane(this.detailsTextArea), BorderLayout.CENTER)
        val southPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        southPanel.add(btnCopyClipboard)
        panelDetails.add(southPanel, BorderLayout.SOUTH)

        panelDetails.isOpaque = false
        southPanel.isOpaque = false

        return this.panelDetails
    }

    private fun doToggleDetails() {
        btnDetails.text = if (panelDetails.isVisible) labelDetailsShow else labelDetailsHide
        panelDetails.isVisible = !panelDetails.isVisible
        size = if (panelDetails.isVisible) sizeBig else sizeSmall
    }

    private fun doCopyClipboard() {
        val stringSelection = StringSelection(detailsTextArea.text)
        val systemClipboard = Toolkit.getDefaultToolkit().systemClipboard
        systemClipboard.setContents(stringSelection) { _, _ ->
            // can be ignored
        }
    }

    private fun doClose() {
        this.dispose()
    }
}
