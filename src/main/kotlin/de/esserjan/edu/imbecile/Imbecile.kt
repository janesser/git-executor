package de.esserjan.edu.imbecile

import de.esserjan.edu.imbecile.util.SubProcessBuilder
import org.osgi.service.component.annotations.Component
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

@Component
class Imbecile(
    var executable: File = File("git"),
    var repositoryDirectory: File? = null,
    val extraEnvVars: MutableMap<String, String> = mutableMapOf()
) {

    enum class PullMode {
        FF_ONLY, REBASE_MERGE
    }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    class ImbecileException(e: Exception) : Exception(e)

    @Throws(ImbecileException::class)
    private fun gitCommandExec(command: String, vararg commandArgs: String): ImbecileResult {
        try {
            val commandStrings = mutableListOf(executable.path, command)
            commandStrings.addAll(commandArgs)

            if (extraEnvVars.containsKey(ENV_SSH_ASK_PASS)) {
                commandStrings.add(0, SET_SID)
                logger.debug("Detected {} pointing to {}", ENV_SSH_ASK_PASS, extraEnvVars[ENV_SSH_ASK_PASS])
            }

            val pb = SubProcessBuilder(commandStrings, repositoryDirectory, extraEnvVars)
            logger.debug("Executing: {}", commandStrings)
            logger.debug("With extraEnvVars: {}", extraEnvVars)
            logger.debug("In workingDir: {}", repositoryDirectory)

            val p = pb.start()
            val collectedOutput = p.collectedOutput()

            return ImbecileResult(p.waitFor(), collectedOutput)
        } catch (e: Exception) {
            throw ImbecileException(e)
        }
    }

    fun version() = gitCommandExec("--version")

    // working-tree operations

    fun reset(hard: Boolean, commitId: String) =
        if (hard) gitCommandExec("reset", "--hard", commitId)
        else gitCommandExec("reset", commitId)

    fun commit(
        message: String,
        amend: Boolean = false,
        allowEmpty: Boolean = false,
        commitAll: Boolean = false
    ): ImbecileResult {
        if (commitAll) addAll()

        val commitArgs = mutableListOf("-m", message)
        if (amend)
            commitArgs.add("--amend")
        if (allowEmpty)
            commitArgs.add("--allow-empty")

        return gitCommandExec("commit", *commitArgs.toTypedArray())
    }

    fun addAll() = gitCommandExec("add", "-A", ":/")

    fun add(f: File) = gitCommandExec("add", repositoryDirectory?.toPath()!!.relativize(f.toPath()).toString())

    fun clean() = gitCommandExec("clean", "-fd", ":/")

    fun rebase(parentCommitId: String) = gitCommandExec("rebase", "--onto", parentCommitId)

    fun rebaseAbort() = gitCommandExec("rebase", "--abort")

    /**
     * https://stackoverflow.com/a/3879077
     */
    fun hasChanges(): Boolean {
        if (gitCommandExec("update-index", "--refresh").exitCode != 0) return true

        if (gitCommandExec("diff-index", "--quiet", "HEAD").exitCode != 0) return true

        return false
    }

    // remote operations

    fun addRemote(remoteName: String, remoteUrl: String) = gitCommandExec("remote", "add", remoteName, remoteUrl)
    fun removeRemote(remoteName: String) = gitCommandExec("remote", remoteName)

    fun clone(remoteUrl: String, repoDir: File, depth: Int? = null, bare: Boolean? = null): ImbecileResult {
        this.repositoryDirectory = repoDir

        val cloneArgs = mutableListOf(remoteUrl, repoDir.path)

        depth?.let {
            cloneArgs.addAll(0, listOf("--depth", depth.toString()))
        }

        bare?.let {
            cloneArgs.add("--bare")
        }

        return gitCommandExec("clone", *cloneArgs.toTypedArray())
    }

    fun fetch(remote: String = "origin") = gitCommandExec("fetch", remote)

    fun pull(remote: String = "origin", mode: PullMode = PullMode.FF_ONLY): ImbecileResult {
        val pullArg = when (mode) {
            PullMode.FF_ONLY -> "--ff-only"
            PullMode.REBASE_MERGE -> "--rebase=merges"
        }

        return gitCommandExec("pull", pullArg, remote)
    }

    fun push(remote: String = "origin", force: Boolean = false): ImbecileResult {
        return if (force)
            gitCommandExec("push", "-f", remote)
        else
            gitCommandExec("push", remote)
    }

    fun config(key:String, value:String) = gitCommandExec("config", key, value)

    companion object {
        const val ENV_SSH_ASK_PASS = "SSH_ASKPASS"
        const val SET_SID = "setsid"
    }

}