package com.dynatrace.diagnostics.plugin;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.plugin.perflib.PerformanceFactory;
import com.dynatrace.diagnostics.plugin.perflib.perfmon.InvalidOperationException;
import com.dynatrace.diagnostics.plugin.perflib.perfmon.PerformanceCounter;
import com.dynatrace.diagnostics.plugin.perflib.perfmon.PerformanceCounterException;
import com.dynatrace.diagnostics.plugin.perflib.perfmon.PerformanceMeasureKey;
import com.dynatrace.diagnostics.plugin.perflib.perfmon.PerformanceMonitor;
import com.dynatrace.diagnostics.plugin.perflib.perfmon.PerformanceObject;

/**
 * Class for the Windows Performance Monitor Plugin which queries performance
 * counters from a windows machines. Querying counters on remote machines is
 * possible with the help of NET USE.
 */
public class WindowsPerformanceMonitor implements Monitor {

	private static final String TRANSLATION = "translation";
	private static final String INSTANCE_NAME = "instance";
	
	private static final String MEASURE_CONFIG_STRING_OBJECT_NAME = "objectName";
	private static final String MEASURE_CONFIG_STRING_COUNTER_NAME = "counterName";
	private static final String MEASURE_CONFIG_STRING_INSTANCE_NAME = "instanceName";
	private static final String MEASURE_CONFIG_STRING_SCALE = "scale";

	private static final Logger log = Logger.getLogger(WindowsPerformanceMonitor.class.getName());
	
	private PerformanceMonitor perfmon;
	private String instancePrefix;

	private PerformanceObject addMetric(MonitorMeasure measure) throws InvalidOperationException,
			PerformanceCounterException {
		
		String objectName = instancePrefix + measure.getParameter(MEASURE_CONFIG_STRING_OBJECT_NAME);
		String counterName = measure.getParameter(MEASURE_CONFIG_STRING_COUNTER_NAME);
		String instanceName = measure.getParameter(MEASURE_CONFIG_STRING_INSTANCE_NAME);
		String scale = measure.getParameter(MEASURE_CONFIG_STRING_SCALE);
		
		PerformanceObject object = new PerformanceObject(objectName, counterName,
				scale != null && scale.equals("1000")
						? PerformanceCounter.SCALE_1000
						: PerformanceCounter.NO_SCALE,
				instanceName != null && !instanceName.isEmpty()
						? instanceName
						: null);
		
		perfmon.addQuery(object);				
		return object;
	}
	

	/**
	 * The setup method reads the configuration from the MonitorEnvironment and
	 * connects to the configured host. If authentication is enabled, the
	 * connection will be established using NET USE. If the username is empty,
	 * the setup will end with an error status. Then a number of performance
	 * metrics will be added to the query, which will be executed in the
	 * {@link #execute(MonitorEnvironment)} method.
	 *
	 * @throws Exception
	 */
	@Override
    public Status setup(MonitorEnvironment env) throws Exception {
		try {
			this.perfmon = PerformanceFactory.createPerformanceMonitor();
		} catch (UnsupportedOperationException ex) {
			return new Status(Status.StatusCode.ErrorInfrastructure, "This collector does not support windows monitors", "This collector does not support windows monitors", ex);
		}

		boolean translation;
		String hostname;

		try {
			translation = env.getConfigBoolean(TRANSLATION);
			hostname = env.getHost().getAddress();
			instancePrefix = env.getConfigString(INSTANCE_NAME).toUpperCase();
		} catch (NullPointerException ex) {
			return new Status(Status.StatusCode.ErrorInternal, "Missing configuration property", "Missing configuration property", ex);
		} catch (InvalidParameterException ipe) {
			return new Status(Status.StatusCode.ErrorInternal, "Invalid configuration property", "Invalid configuration property", ipe);
		}
		if (instancePrefix.equals(null) || instancePrefix.equals("")) {
			instancePrefix = "SQLServer:";
		}
		else {
			instancePrefix = "MSSQL$" + instancePrefix + ":";
		}
		try {
			perfmon.init(hostname, translation);
		} catch (Exception ex) {
			return new Status(Status.StatusCode.ErrorInfrastructure, "Connection problem", "Connecting to the host '" + hostname + "' caused exception: " + ex.getMessage(), ex);
		}

		Collection<MonitorMeasure> measures = env.getMonitorMeasures();
		boolean partial = false;
		for (MonitorMeasure measure : measures) {			
			try {
				addMetric(measure);					
			} catch (PerformanceCounterException ex) {
				partial = true;
				if (log.isLoggable(Level.WARNING)) {					
					String objectName = instancePrefix + measure.getParameter(MEASURE_CONFIG_STRING_OBJECT_NAME);
					String counterName = measure.getParameter(MEASURE_CONFIG_STRING_COUNTER_NAME);
					String instanceName = measure.getParameter(MEASURE_CONFIG_STRING_INSTANCE_NAME);
					log.log(Level.WARNING, "registering of perfmon measure " + objectName +"/"+counterName +"("+instanceName+")" +"caused an exception", ex);				
				}				
			}
		}		
		if (partial) {
			return new Status(Status.StatusCode.PartialSuccess, "Initializing performance queries caused errors", perfmon.getDetailedErrors());			
		}
		return new Status(Status.StatusCode.Success);
	}

	/**
	 * Executes the performance query and sets the measurements for each
	 * MonitorMeasure.
	 *
	 */
	@Override
    public Status execute(MonitorEnvironment env) throws Exception {
		Map<PerformanceMeasureKey, Long> queryResult;
		try {
			// execute the query
			queryResult = perfmon.query();
		} catch (PerformanceCounterException ex) {
			return new Status(Status.StatusCode.ErrorInternal, "Executing performance query failed with exception", "Executing performance query failed with exception: " + ex.getMessage(), ex);
		}
		
		boolean failed = true;
		boolean partial = false;
		Collection<MonitorMeasure> measures = env.getMonitorMeasures();
		if (measures.size() == 0) failed = false;
		PerformanceMeasureKey queryPerformanceMeasureKey = new PerformanceMeasureKey("", "", "");
		
		for (MonitorMeasure measure : measures) {
			String objectName = instancePrefix + measure.getParameter(MEASURE_CONFIG_STRING_OBJECT_NAME);
			String counterName = measure.getParameter(MEASURE_CONFIG_STRING_COUNTER_NAME);
			String instanceName = measure.getParameter(MEASURE_CONFIG_STRING_INSTANCE_NAME);
			PerformanceObject perfObject = perfmon.getPerformanceObject(objectName);
			
			if ((perfObject == null) || 
				(perfObject.getCounter(counterName) == null) ||
				(perfObject.getCounter(counterName).getInstance(instanceName) == null) ||
				(!perfObject.getCounter(counterName).getInstance(instanceName).isInitialized()))
				{
				try {
					if ((perfObject != null) && (perfObject.getCounter(counterName) != null) && 
						(perfObject.getCounter(counterName).getInstance(instanceName) != null) && 
						(!perfObject.getCounter(counterName).getInstance(instanceName).isInitialized())) {
						perfmon.addQuery(perfObject);
					} else { 					
						perfObject =  addMetric(measure);
					}

				} catch (PerformanceCounterException ex) {
					partial = true;					
					if (PerformanceObject.doLog(perfObject, counterName, instanceName)) {
						if (log.isLoggable(Level.WARNING)) {												
							log.log(Level.WARNING, "query of perfmon measure " + objectName +"/"+counterName +"("+instanceName+")" +"caused an exception", ex);
						}			
					}
					PerformanceObject.updateDoLogState(perfObject, counterName, instanceName, false);
				}							
			}

			queryPerformanceMeasureKey.setObjectName(objectName);
			queryPerformanceMeasureKey.setCounterName(counterName);
			queryPerformanceMeasureKey.setInstanceName(instanceName);			
			Long longValue = queryResult.get(queryPerformanceMeasureKey);
			if (longValue == null) {
				if (PerformanceObject.doLog(perfObject, counterName, instanceName)) {
					if (log.isLoggable(Level.WARNING)) {						
						log.warning("Failed to retrieve measurement for measure " + objectName +"/"+counterName +"("+instanceName+")");
					}
				}				
				PerformanceObject.updateDoLogState(perfObject, counterName, instanceName,false);
				partial = true;
				continue;							
			} 						
			
			double value = longValue.doubleValue();
			if ((perfObject != null && perfObject.getCounters()[0].getScaleFactor() == PerformanceCounter.SCALE_1000) || 
				// workaround for windows bug in e.g. "LogicalDisc Free Percentage" measure: 
				// we get scale factor "NO_SCALE", but the value is multiplied by 1000.
				// using scale indicator of measure subscription as fallback...
				(measure.getParameter(MEASURE_CONFIG_STRING_SCALE) != null &&
				measure.getParameter(MEASURE_CONFIG_STRING_SCALE).equals("1000"))) {
				value *= 0.001;
			}

			if (log.isLoggable(Level.FINE))
				log.fine("Measurement: " + measure + " = " + value);

			measure.setValue(value);
			failed = false;
		}
		if (failed) {
			return new Status(Status.StatusCode.ErrorInternal, "Executing all performance queries caused errors", perfmon.getDetailedErrors());
		}
		if (partial) {			
			return new Status(Status.StatusCode.PartialSuccess, "Executing some performance queries caused errors", perfmon.getDetailedErrors());								
		}
	
		return new Status(Status.StatusCode.Success);
	}

	/**
	 * Clear the metric map and performance query and disconnect the PerformanceMonitor.
	 * 
	 */
	@Override
    public void teardown(MonitorEnvironment env) throws Exception {
		if (perfmon == null)
			return;
		try {
			perfmon.clearQuery();
		} catch (PerformanceCounterException ex) {
			if (log.isLoggable(Level.WARNING))
				log.log(Level.WARNING, "Failed to clear performance query", ex);
		}
		try {
			perfmon.disconnect();
		} finally {
			perfmon = null;
		}
	}
}
