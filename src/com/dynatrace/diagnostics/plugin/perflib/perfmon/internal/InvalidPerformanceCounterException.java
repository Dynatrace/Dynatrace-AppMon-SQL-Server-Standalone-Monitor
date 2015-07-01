package com.dynatrace.diagnostics.plugin.perflib.perfmon.internal;

import com.dynatrace.diagnostics.plugin.perflib.perfmon.PerformanceCounterException;

class InvalidPerformanceCounterException extends PerformanceCounterException {

	private static final long serialVersionUID = 1L;

	public InvalidPerformanceCounterException(String message) {
		super(message);
	}

}
