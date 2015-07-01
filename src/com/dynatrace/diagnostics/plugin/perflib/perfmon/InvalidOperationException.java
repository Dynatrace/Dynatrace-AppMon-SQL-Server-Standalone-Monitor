package com.dynatrace.diagnostics.plugin.perflib.perfmon;

/**
 * An exception that is thrown if an operation is called that is invalid in the
 * current application state.
 * 
 */
public class InvalidOperationException extends RuntimeException {
	private static final long serialVersionUID = 8055411879781959553L;

	public InvalidOperationException() {
		super();
	}

	public InvalidOperationException(String message) {
		super(message);
	}

}
