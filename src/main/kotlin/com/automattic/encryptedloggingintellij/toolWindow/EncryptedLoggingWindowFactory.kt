package com.automattic.encryptedloggingintellij.toolWindow

import com.automattic.encryptedloggingintellij.services.MyProjectService
import com.automattic.encryptedloggingintellij.services.createCredentialAttributes
import com.automattic.encryptedloggingintellij.ui.LoginDialog
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.TextComponentEmptyText
import com.intellij.ui.content.ContentFactory
import io.ktor.util.*
import io.ktor.utils.io.errors.*
import org.jsoup.Jsoup
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.SocketException
import java.net.URL
import java.net.URLConnection
import java.util.Base64
import java.util.function.Predicate
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea


class EncryptedLoggingWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val encryptedLoggingWindow = EncryptedLoggingWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(encryptedLoggingWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class EncryptedLoggingWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()

        fun getContent() = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            // Text field for input
            val inputTextField = JBTextField().apply {
                columns = 36
                emptyText.setText("Enter logs UUID");
                putClientProperty(
                    TextComponentEmptyText.STATUS_VISIBLE_FUNCTION,
                    Predicate<JBTextField> { tf: JBTextField -> tf.text.isEmpty() }
                );
            }

            // Button next to the text field
            val actionButton = JButton("Fetch")

            // Panel for the input text field and button
            val inputPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(inputTextField)
                add(actionButton)
            }

            // Read-only text area
            val outputTextArea = JTextArea().apply {
                isEditable = false
            }

            // Scroll pane for the read-only text area
            val scrollPane = JBScrollPane(outputTextArea)

            // Add input panel and scroll pane to the main panel
            add(inputPanel, BorderLayout.NORTH)
            add(scrollPane, BorderLayout.CENTER)


            actionButton.apply {
                addActionListener {
                    val response = sendRequest(inputTextField.text.ifBlank { "default-incorrect-uuid" })
                    WriteCommandAction.runWriteCommandAction(null) {
                        response.fold(
                            onSuccess = { log ->
                                outputTextArea.text = log
                            },
                            onFailure = { throwable ->
                                outputTextArea.text = throwable.stackTraceToString()
                                if(throwable is SocketException) {
                                    outputTextArea.text += "\n ⚠️ Make sure Autoproxxy is connected!\n"
                                } else if (throwable is IOException || throwable is NullPointerException){
                                    outputTextArea.text += "\n ⚠️ Authentication failed!\n"
                                    LoginDialog(okAction = { username, password ->
                                        outputTextArea.text = ""
                                        PasswordSafe.instance.set(
                                            createCredentialAttributes(),
                                            Credentials(username, password)
                                        )
                                    }).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        private fun sendRequest(uuid: String): Result<String> {
            val url: URL =
                URL("https://mc.a8c.com/encrypted-logs.php?uuid=$uuid")
            val proxyAddress = InetSocketAddress("localhost", 8080)

            val proxy = Proxy(Proxy.Type.SOCKS, proxyAddress)
            val connection: URLConnection = url.openConnection(proxy)

            val usernamePassword = PasswordSafe.instance.get(createCredentialAttributes())

            return runCatching {
                val encodedBasicAuth = "${usernamePassword!!.userName}:${usernamePassword.password!!}".encodeBase64()

                connection.setRequestProperty(
                    "Authorization",
                    "Basic $encodedBasicAuth"
                )

                connection.getInputStream().bufferedReader().use {
                    Jsoup.parse(it.readText())
                        .select("pre")
                        .text()
                }
            }
        }
    }
}
