package de.esserjan.edu.git_executor;

import java.io.BufferedReader;
import java.io.IOException;

class ConsoleOutputCollector {
	
	static ConsoleOutputCollector collect(Process p) throws IOException {
		return new ConsoleOutputCollector().readOutput(p);
	}
	
	private StringBuilder sb = new StringBuilder();

	public ConsoleOutputCollector readOutput(Process p) throws IOException {
		BufferedReader reader = p.inputReader();
		
		String line = reader.readLine();
		while (line != null) {
			sb.append(line);
			sb.append(System.lineSeparator());
			line = reader.readLine();
		}
		
		return this;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
	
	
}