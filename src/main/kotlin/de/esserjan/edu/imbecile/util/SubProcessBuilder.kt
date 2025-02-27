package de.esserjan.edu.imbecile.util

import org.slf4j.LoggerFactory
import java.io.*

class SubProcess(private val p: Process) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Throws(IOException::class)
    fun collectedOutput(): String {
        val sb = StringBuilder()
        val reader = p.inputReader()

        do {
            val line = reader.readLine()
            if (line != null) {
                logger.debug(line)
                sb.append(line)
                sb.append(System.lineSeparator())
            }
        } while (line != null)

        return sb.toString()
    }

    fun inputStream(): BufferedInputStream = BufferedInputStream(p.inputStream)
    fun outputStream(): BufferedOutputStream = BufferedOutputStream(p.outputStream)

    fun inputReader(): BufferedReader = p.inputReader()
    fun outputWriter(): BufferedWriter = p.outputWriter()

    fun waitFor(): Int = p.waitFor()
}

class SubProcessBuilder(
    commandStrings: List<String>,
    workingDirectory: File? = null,
    extraEnvVars: Map<String, String>? = null
) {

    private val pb: ProcessBuilder = ProcessBuilder(commandStrings)

    init {
        pb.redirectErrorStream(true)
        pb.redirectInput(ProcessBuilder.Redirect.PIPE)
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE)

        if (workingDirectory?.exists() == true)
            pb.directory(workingDirectory)

        extraEnvVars?.let { pb.environment().putAll(extraEnvVars) }
    }

    fun start(): SubProcess = SubProcess(pb.start())

    fun envVars(): MutableMap<String, String> = pb.environment()

    fun shellCommand(): String {
        val envDeclarations: String = pb.environment().map { (key, value) -> "$key=$value" }.reduce { acc, envVar -> "$envVar $acc" }
        val command: String = pb.command().reduce { acc, commandArg -> "$acc $commandArg" }
        return "$envDeclarations $command"
    }
}
