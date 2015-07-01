package com.dynatrace.diagnostics.plugin.perflib.perfmon;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * The <tt>PerformanceMonitor</tt> can be used to retrieve performance
 * information from hosts that run the Windows operating system.
 * 
 */
public interface PerformanceMonitor {
	/**
	 * Connect to the host.
	 * 
	 * @param hostname
	 *            the name or IP address of the host
	 * @param translation enable or disable the counter name translation
	 * @throws PerformanceCounterException
	 *             if the perfmon library cannot be initialized
	 * @throws InvalidOperationException
	 *             if this performance monitor is already connected
	 * @throws IOException 
	 */
	void init(String hostname, boolean translation) throws PerformanceCounterException, InvalidOperationException, IOException;

	/**
	 * Closes the connection.
	 */
	void disconnect();

	/**
	 * Adds the given performance object to the query. The performance object
	 * must contain valid counters and the counters must contain valid instance
	 * names, else an exception will be thrown. If the object doesn't contain
	 * any counters, or the counter doesn't contain any instance names the
	 * object or counter will be ignored.
	 * 
	 * @see #query()
	 * @see #clearQuery()
	 * @param object
	 *            the performance object that will be added to the query
	 * @throws PerformanceCounterException
	 * @throws InvalidOperationException
	 *             if this performance monitor is not connected
	 */
	void addQuery(PerformanceObject object) throws PerformanceCounterException, InvalidOperationException;
	
	/**
	 * Returns the stored performance object with the specified name.
	 * 
	 * @param objectName a query object name
	 * @return matching PerformanceObject or null if no matching performance object is found.
	 */
	PerformanceObject getPerformanceObject(String objectName);
	
	/**
	 * Clears the query. All performance objects, counters and instance names
	 * will be removed.
	 * 
	 * @throws PerformanceCounterException
	 *             if the perfmon library fails to clear the query
	 * @throws InvalidOperationException
	 *             if this performance monitor is not connected
	 */
	void clearQuery() throws PerformanceCounterException, InvalidOperationException;

	
	
	/**
	 * Iterates over all performance objects and returns a string containing information about errors occurred
	 * during last adding of counters or query.
	 * @return formated error string
	 */
	String getDetailedErrors();
	
	/**
	 * Executes the query and returns all queried values in a map. The map
	 * consists of <tt>PerformanceObject</tt> keys and <tt>Long</tt> values.
	 * The values are scaled with the scale factors set in the
	 * <tt>PerformanceCounter</tt> objects. Every performance object key
	 * contains exactly one counter, and every counter contains exactly one
	 * instance name. The size of the returned map can be smaller than the
	 * actual number of queries, in case some values could not be retrieved.
	 * 
	 * @return a map that contains the queried values
	 * @throws PerformanceCounterException
	 *             if the query failed
	 * @throws InvalidOperationException
	 *             if this performance monitor is not connected
	 */
	Map<PerformanceMeasureKey, Long> query() throws PerformanceCounterException, InvalidOperationException;

	// TODO: implement
	/**
	 * Retrieves all available performance counters from the connected host and
	 * returns it as a collection of <tt>PerformanceObject</tt>s. This
	 * operation is currently UNSUPPORTED and will throw an
	 * {@link UnsupportedOperationException}.
	 * 
	 * @return
	 * @throws PerformanceCounterException
	 *             if the perfmon library cannot retrieve the information
	 * @throws InvalidOperationException
	 *             if this performance monitor is not connected
	 */
	Collection<PerformanceObject> getPerformanceIdentifiers() throws PerformanceCounterException,
			InvalidOperationException;
}
