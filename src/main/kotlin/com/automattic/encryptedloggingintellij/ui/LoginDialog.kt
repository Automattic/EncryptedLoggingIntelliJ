package com.automattic.encryptedloggingintellij.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import java.awt.FlowLayout
import javax.swing.*


class LoginDialog() : DialogWrapper(null) {
    // Text fields for user input
    private val usernameField = JTextField(15)
    private val passwordField = JPasswordField(15)

    init {
        title = "Login to MC"
        init()
    }

    // Buttons for actions
    private val okButton = JButton("OK").apply {
        addActionListener {
            // Handle login logic here
            val username = usernameField.text
            val password = String(passwordField.password)
            println("Username: $username, Password: $password") // Replace with actual login handling

            // Close the dialog
            dispose()
        }
    }
    private val cancelButton = JButton("Cancel").apply {
        addActionListener {
            // Close the dialog without doing anything
            dispose()
        }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        val usernamePanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JLabel("Username:"))
            add(usernameField)
        }

        val passwordPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JLabel("Password:"))
            add(passwordField)
        }

        panel.add(usernamePanel)
        panel.add(passwordPanel)

        return panel
    }

    override fun doOKAction() {
        val username = usernameField.text
        val password = String(passwordField.password)
        // Handle login logic here
        println("Username: $username, Password: $password")  // Replace with actual login handling

        super.doOKAction()
    }

    override fun doValidateAll(): List<ValidationInfo> {
        val validationInfos = mutableListOf<ValidationInfo>()

        if (usernameField.text.isBlank()) {
            validationInfos.add(ValidationInfo("Please enter a username.", usernameField))
        }

        if (passwordField.password.isEmpty()) {
            validationInfos.add(ValidationInfo("Please enter a password.", passwordField))
        }

        return validationInfos
    }
}
