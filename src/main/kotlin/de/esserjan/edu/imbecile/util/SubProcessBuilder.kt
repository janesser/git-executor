package de.esserjan.edu.imbecile.util

import org.slf4j.LoggerFactory
import java.io.*

class SubProcess(private val p:Process) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Throws(IOException::class)
    fun collectedOutput() : String {
        val sb = StringBuilder()
        val reader = inputReader()

        do {
            val line = reader.readLine()
            logger.debug(line)
            if (line != null) {
                sb.append(line)
                sb.append(System.lineSeparator())
            }
        } while(line != null)

        return sb.toString()
    }

    fun inputReader(): BufferedReader = p.inputReader()
    fun outputWriter(): BufferedWriter = p.outputWriter()
    fun outputStream(): OutputStream = p.outputStream

    fun waitFor() : Int = p.waitFor()
}

class SubProcessBuilder(commandStrings: List<String>, workingDirectory: File? = null, extraEnvVars: Map<String, String>? = null) {

    private val pb : ProcessBuilder = ProcessBuilder(commandStrings)

    init  {
        pb.redirectErrorStream(true)
        pb.redirectInput(ProcessBuilder.Redirect.PIPE)
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE)

        if (workingDirectory?.exists() == true)
            pb.directory(workingDirectory)

        extraEnvVars?.let { pb.environment().putAll(extraEnvVars!!) }
    }
    
    fun start(): SubProcess = SubProcess(pb.start())
}
