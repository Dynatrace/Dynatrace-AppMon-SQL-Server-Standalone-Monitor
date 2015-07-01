package com.dynatrace.diagnostics.plugin.perflib;

import com.dynatrace.diagnostics.plugin.perflib.perfmon.PerformanceMonitor;
import com.dynatrace.diagnostics.plugin.perflib.perfmon.internal.PerformanceMonitorImpl;

/**
 * Creates objects that can be used to gather performance information.
 * 
 */
public class PerformanceFactory {
	
	// constants
	private static final String WINDOWS = "WINDOWS";
	private static final String OS_NAME = System.getProperty("os.name");
	private static final String ERR_UNSUPPORTED_OS = "unsupported OS: ";

	/**
	 * Creates a new PerformanceMonitor instance. This method only is supported
	 * on Windows operating systems.
	 * 
	 * @return a new PerformanceMonitor instance
	 * @throws UnsupportedOperationException
	 *             if the operating system is unsupported
	 */
	public static PerformanceMonitor createPerformanceMonitor()
			throws UnsupportedOperationException {
		// check if we are running under a supported windows platform
		if (OS_NAME == null || !OS_NAME.toUpperCase().contains(WINDOWS))
			throw new UnsupportedOperationException(ERR_UNSUPPORTED_OS + OS_NAME);

		return new PerformanceMonitorImpl();
	}

}
