package git_executor;

public record GitExecutionResult(int exitCode, String outputText) {
}