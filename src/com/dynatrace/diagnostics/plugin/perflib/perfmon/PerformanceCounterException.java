package com.dynatrace.diagnostics.plugin.perflib.perfmon;

/**
 * An exception that is thrown if the perfmon library causes an error.
 * 
 */
public class PerformanceCounterException extends Exception {
	private static final long serialVersionUID = -1787471249005513789L;

	public PerformanceCounterException() {
		super();
	}

	public PerformanceCounterException(String message) {
		super(message);
	}

}
