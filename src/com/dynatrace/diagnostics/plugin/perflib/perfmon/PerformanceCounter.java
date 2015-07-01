package com.dynatrace.diagnostics.plugin.perflib.perfmon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a performance counter that contains instance names. An optional
 * scale factor can be set, for example to retrieve more precise values.
 */
public class PerformanceCounter implements Cloneable {
	// constants
	// do NOT change these values
	
	
	public static final int NO_SCALE = 0x00001400;
	public static final int SCALE_1000 = 0x00002400;
	
	public static class PerformanceInstance {
		
		private String instanceName;
		private boolean doLog;		
		private boolean initialized = false;
		private String lastErrorMessage = null;
		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;			
			result = prime * result
					+ ((instanceName == null) ? 0 : instanceName.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PerformanceInstance other = (PerformanceInstance) obj;			
			if (instanceName == null) {
				if (other.instanceName != null)
					return false;
			} else if (!instanceName.equals(other.instanceName))
				return false;
			return true;
		}
		public PerformanceInstance(String instanceName, boolean doLog) {			
			this.instanceName = instanceName;
			this.doLog = doLog;
		}
		
		public void setInstanceName(String instanceName) {
			this.instanceName = instanceName;
		}
		public String getInstanceName() {
			return instanceName;
		}
		public void setDoLog(boolean exceptionLogged) {
			this.doLog = exceptionLogged;
		}
		public boolean isDoLog() {			
			return doLog;
		}
		public void setInitialized(boolean initialized) {
			this.initialized = initialized;
		}
		public boolean isInitialized() {
			return initialized;
		}
		public void setLastErrorMessage(String lastErrorMessage) {
			this.lastErrorMessage = lastErrorMessage;
		}
		public String getLastErrorMessage() {
			return lastErrorMessage;
		}	
	}
	
	private String name;
	private Map<String, PerformanceInstance> instances = new HashMap<String, PerformanceInstance>();
	private int scaleFactor = NO_SCALE;

	/**
	 * Creates an empty performance counter object.
	 * 
	 * @param name
	 *            the name of the performance counter
	 */
	public PerformanceCounter(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
	}

	/**
	 * Creates a performance counter object with an instance.
	 * 
	 * @param counterName
	 *            the name of the performance counter
	 * @param instanceName
	 *            the name of the instance
	 */
	public PerformanceCounter(String counterName, String instanceName) {
		this(counterName);		
		this.addInstance(new PerformanceInstance(instanceName, true));		
	}

	/**
	 * Creates a performance counter object with an instance and scale factor.
	 * 
	 * @param counterName
	 *            the name of the performance counter
	 * @param scaleFactor
	 *            the factor used to scale the values
	 * @param instanceName
	 *            the name of the instance
	 */
	public PerformanceCounter(String counterName, int scaleFactor, String instanceName) {
		this(counterName, instanceName);
		setScaleFactor(scaleFactor);
	}

	/**
	 * @return the name of this performance counter
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the performance counter name to the given parameter.
	 * 
	 * @param name
	 *            the name of the performance counter
	 */
	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
	}

	/**
	 * Adds an instance name.
	 * 
	 * @param instance
	 *            the name of the instance
	 * @return if the instance name was added
	 */
	public void addInstance(PerformanceInstance instance) {
		instances.put(instance.getInstanceName(), instance);
	}

	/**
	 * Removes an instance name.
	 * 
	 * @param instance
	 *            the name of the instance
	 * @return if the instance name was removed
	 */
	public boolean removeInstance(String instance) {
		if (instances == null) return false;
		return instances.remove(instance) != null;
	}

	/**
	 * @param instance
	 *            the name of the instance
	 * @return if this performance counter contains the given instance name
	 */
	public boolean containsInstance(String instance) {
		if (instances == null) return false;
		return instances.containsKey(instance);		
	}
	
	public PerformanceInstance getInstance(String instanceName) {
		if (instances == null || instances.size() == 0) {
			return null;
		}		
		return instances.get(instanceName);
	}

	/**
	 * @return all instance names that were added to this performance counter
	 */
	public Collection<PerformanceInstance> getInstances() {
		return instances.values();
	}

	/**
	 * Returns the scale factor for this counter. The returned integer value is
	 * not equal the scale factor. If you want to check which factor is set,
	 * compare the returned value with the <tt>SCALE</tt> constants.
	 * @return the scale factor for this counter, the default is {@link #NO_SCALE}
	 * @see PerformanceMonitor#query()
	 */
	public int getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * Sets the scale factor for this counter to the given parameter.
	 * Possible values are: {@link #NO_SCALE}, {@link #SCALE_1000}. 
	 * 
	 * @param scaleFactor
	 *            the scale factor for this counter
	 * @see PerformanceMonitor#query()
	 */
	public void setScaleFactor(int scaleFactor) {
		if (scaleFactor != NO_SCALE && scaleFactor != SCALE_1000) {
			throw new IllegalArgumentException();
		}
		this.scaleFactor = scaleFactor;
	}

	public boolean errorOccurred()  {
		for (PerformanceInstance performanceInstance : instances.values()) {
			if (performanceInstance.getLastErrorMessage() != null) {
				return true;
			}
		}
		return false;		
	}
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null || !o.getClass().equals(this.getClass())) {
			return false;
		}

		PerformanceCounter that = (PerformanceCounter) o;

		if (name == null) {
			if (that.name != null) {
				return false;
			}
		} else if (!name.equals(that.name)) {
			return false;
		}
		if (instances == null) {
			if (that.instances != null) {
				return false;
			}
		} else if (!instances.equals(that.instances)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = (name == null ? 0 : name.hashCode()) + instances.hashCode();
		return result;
	}

	@Override
	public Object clone() {
		PerformanceCounter clone = new PerformanceCounter(name);
		clone.scaleFactor = scaleFactor;
		for (PerformanceInstance instance : instances.values()) {
			clone.instances.put(instance.getInstanceName(), new PerformanceInstance(instance.getInstanceName(), instance.isDoLog()));
		}
		return clone;
	}
}
