package com.automattic.encryptedloggingintellij.toolWindow

import com.automattic.encryptedloggingintellij.services.MyProjectService
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
import java.awt.BorderLayout
import java.awt.FlowLayout
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
                }
            }
        }
    }
}
