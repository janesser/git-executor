package de.esserjan.edu.imbecile

import de.esserjan.edu.imbecile.Imbecile.ImbecileException
import de.esserjan.edu.imbecile.util.SubProcessBuilder
import org.osgi.service.component.annotations.Component
import java.io.IOException

@Component
class ImbecileCredentials {

    @Throws(ImbecileException::class)
    fun store(protocol: String, host: String, username: String, password: String?): ImbecileResult {
        try {
            gitCredentialExec(
                GIT_CREDENTIAL_FILL,
                protocol,
                host,
                username,
                password
            )
            return gitCredentialExec(
                GIT_CREDENTIAL_APPROVE,
                protocol,
                host,
                username,
                password
            )
        } catch (e: IOException) {
            throw ImbecileException(e)
        } catch (e: InterruptedException) {
            throw ImbecileException(e)
        }
    }

    @Throws(ImbecileException::class)
    fun erase(protocol: String, host: String, username: String): ImbecileResult {
        try {
            return gitCredentialExec(
                GIT_CREDENTIAL_REJECT,
                protocol,
                host,
                username,
                null
            )
        } catch (e: IOException) {
            throw ImbecileException(e)
        } catch (e: InterruptedException) {
            throw ImbecileException(e)
        }
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun gitCredentialExec(
        commandStrings: List<String>, protocol: String, host: String,
        username: String, password: String?
    ): ImbecileResult {
        val pb = SubProcessBuilder(commandStrings)

        val p = pb.start()

        p.outputWriter().use { writer ->
            Thread.sleep(100L)
            writer.write("protocol=$protocol")
            writer.write(System.lineSeparator())
            writer.write("host=$host")
            writer.write(System.lineSeparator())
            writer.write("username=$username")
            writer.write(System.lineSeparator())
            if (password != null) {
                writer.write("password=$password")
                writer.write(System.lineSeparator())
            }

            writer.write(System.lineSeparator())
            writer.flush()
        }
        val collectedOutput = p.collectedOutput()
        return ImbecileResult(p.waitFor(), collectedOutput)
    }

    companion object {
        private val GIT_CREDENTIAL_FILL = listOf("git", "credential", "fill")
        private val GIT_CREDENTIAL_APPROVE = listOf("git", "credential", "approve")
        private val GIT_CREDENTIAL_REJECT = listOf("git", "credential", "reject")
    }
}