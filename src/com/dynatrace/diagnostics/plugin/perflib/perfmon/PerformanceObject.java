package com.dynatrace.diagnostics.plugin.perflib.perfmon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.dynatrace.diagnostics.plugin.perflib.perfmon.PerformanceCounter.PerformanceInstance;

/**
 * Represents a performance monitor object that contains performance counters.
 * 
 */
public class PerformanceObject implements Cloneable {
	private String name;
	private Map<String, PerformanceCounter> counters = new HashMap<String, PerformanceCounter>();

	/**
	 * Creates an empty performance object.
	 * 
	 * @param name
	 *            the name of the performance object, also called category
	 */
	public PerformanceObject(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
	}

	/**
	 * Creates a performance object with a counter and instance.
	 * 
	 * @param objectName
	 *            the name of the performance object, also called category
	 * @param counterName
	 *            the name of the performance counter
	 * @param instanceName
	 *            the name of the instance
	 */
	public PerformanceObject(String objectName, String counterName, String instanceName) {
		this(objectName);
		if (counterName != null) {
			addCounter(new PerformanceCounter(counterName, instanceName));
		}
	}

	/**
	 * Creates a performance object with a scaled counter and instance.
	 * 
	 * @param objectName
	 *            the name of the performance object, also called category
	 * @param counterName
	 *            the name of the performance counter
	 * @param scaleFactor
	 *            the scale factor used to scale the performance counter values
	 * @param instanceName
	 *            the name of the instance
	 */
	public PerformanceObject(String objectName, String counterName, int scaleFactor, String instanceName) {
		this(objectName);
		if (counterName != null) {
			addCounter(new PerformanceCounter(counterName, scaleFactor, instanceName));
		}
	}

	/**
	 * @return the name of this performance object
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the performance object name to the given parameter.
	 * 
	 * @param name
	 *            the name of the performance object, also called category
	 */
	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
	}

	/**
	 * Adds a performance counter.
	 * 
	 * @param counter
	 *            a performance counter
	 * @return if the counter was added
	 */
	public void addCounter(PerformanceCounter counter) {
		if (counter == null) {
			throw new NullPointerException();
		}
		counters.put(counter.getName(), counter);
	}

	/**
	 * Removes a performance counter.
	 * 
	 * @param counter
	 *            a performance counter
	 * @return if the counter was removed
	 */
	public void removeCounter(PerformanceCounter counter) {
		if (counter == null) {
			throw new NullPointerException();
		}
		counters.remove(counter.getName());
	}

	/**
	 * @param counter
	 *            a performance counter
	 * @return if this performance object containes the given counter
	 */
	public boolean containsCounter(PerformanceCounter counter) {
		if (counter == null) {
			throw new NullPointerException();
		}
		return counters.containsKey(counter.getName());		
	}

	public PerformanceCounter  getCounter(String counterName) {
		if ((counters == null) || (counters.size() == 0)) {
			return null;
		}
		return counters.get(counterName);
	}
	
	public boolean errorOccurred() {
		for (PerformanceCounter counter : counters.values()) {
			if (counter.errorOccurred()) {
				return true;
			}
		}		
		return false;
	}
	/**
	 * @return all counters that were added to this performance object
	 */
	public PerformanceCounter[] getCounters() {
		return counters.values().toArray(new PerformanceCounter[counters.values().size()]);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null || !o.getClass().equals(this.getClass())) {
			return false;
		}

		PerformanceObject that = (PerformanceObject) o;

		if (name == null) {
			if (that.name != null)
				return false;
		} else if (!name.equals(that.name)) {
			return false;
		}
		if (!counters.equals(that.counters)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = (name == null ? 0 : name.hashCode()) + counters.hashCode();
		return result;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		PerformanceObject clone = new PerformanceObject(name);
		for (PerformanceCounter counter : counters.values()) {
			clone.counters.put(counter.getName(), (PerformanceCounter) counter.clone());
		}
		return clone;
	}
	
	public static boolean doLog(PerformanceObject object, String counterName, String instanceName) {
		if (object == null) return true;
		PerformanceCounter[] counters = object.getCounters();
		if (counters == null) return true;
		for (PerformanceCounter performanceCounter : counters) {
			if (performanceCounter.getName().equals(counterName)) {
				Collection<PerformanceInstance> instances = performanceCounter.getInstances();
				if (instances == null) return true;
				for (PerformanceInstance performanceInstance : instances) {
					if (((performanceInstance.getInstanceName() == null) && (instanceName == null)) ||
						((performanceInstance.getInstanceName() != null) && (instanceName != null)	&& performanceInstance.getInstanceName().equals(instanceName))) {
						return performanceInstance.isDoLog();
					}					
				}				
			}			
		}				
		return true;
	}

	public static void updateDoLogState(PerformanceObject object, String counterName, String instanceName, boolean newLogState) {
		if (object == null) return;
		PerformanceCounter[] counters = object.getCounters();
		if (counters == null) return;
		for (PerformanceCounter performanceCounter : counters) {
			if (performanceCounter.getName().equals(counterName)) {
				Collection<PerformanceInstance> instances = performanceCounter.getInstances();
				if (instances == null) return;
				for (PerformanceInstance performanceInstance : instances) {
					if (((performanceInstance.getInstanceName() == null) && (instanceName == null)) ||
						((performanceInstance.getInstanceName() != null) && (instanceName != null)	&& performanceInstance.getInstanceName().equals(instanceName))) {
						performanceInstance.setDoLog(newLogState);
					}					
				}				
			}			
		}						
	}
	
	
}
