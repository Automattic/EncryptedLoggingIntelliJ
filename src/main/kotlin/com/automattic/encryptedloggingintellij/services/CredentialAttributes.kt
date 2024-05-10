package com.automattic.encryptedloggingintellij.services

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName


fun createCredentialAttributes(): CredentialAttributes {
    return CredentialAttributes(
        generateServiceName("automattic-encrypted-logs", "login")
    )
}