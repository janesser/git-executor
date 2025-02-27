package de.esserjan.edu.imbecile.test.git_https_backend

import io.undertow.security.idm.Account
import io.undertow.security.idm.Credential
import io.undertow.security.idm.IdentityManager
import io.undertow.security.idm.PasswordCredential
import java.security.Principal

class MapIdentityManager(private val userMap: Map<String, String>) : IdentityManager {
    override fun verify(account: Account?): Account? {
        return account
    }

    override fun verify(id: String, credential: Credential?): Account? {
        if (credential is PasswordCredential)
            if (userMap[id]?.equals(String(credential.password)) == true)
                return object : Account {
                    override fun getPrincipal(): Principal {
                        return Principal { id }
                    }

                    override fun getRoles(): Set<String> {
                        return emptySet()
                    }
                }

        return null
    }

    override fun verify(credential: Credential?): Account? {
        return null
    }
}