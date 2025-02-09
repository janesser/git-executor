package de.esserjan.edu.git_executor;

public record GitExecutionResult(int exitCode, String outputText) {
}