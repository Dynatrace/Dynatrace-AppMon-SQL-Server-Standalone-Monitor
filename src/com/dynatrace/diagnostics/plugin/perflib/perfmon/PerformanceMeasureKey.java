package com.dynatrace.diagnostics.plugin.perflib.perfmon;

public class PerformanceMeasureKey {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((counterName == null) ? 0 : counterName.hashCode());
		result = prime * result
				+ ((instanceName == null) ? 0 : instanceName.hashCode());
		result = prime * result
				+ ((objectName == null) ? 0 : objectName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PerformanceMeasureKey other = (PerformanceMeasureKey) obj;
		if (counterName == null) {
			if (other.counterName != null)
				return false;
		} else if (!counterName.equals(other.counterName))
			return false;
		if (instanceName == null) {
			if (other.instanceName != null)
				return false;
		} else if (!instanceName.equals(other.instanceName))
			return false;
		if (objectName == null) {
			if (other.objectName != null)
				return false;
		} else if (!objectName.equals(other.objectName))
			return false;
		return true;
	}
	public PerformanceMeasureKey(String objectName, String counterName,
			String instanceName) {
		super();
		this.objectName = objectName;
		this.counterName = counterName;
		this.instanceName = instanceName;
	}
	private String objectName;
	private String counterName;
	private String instanceName;
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setCounterName(String counterName) {
		this.counterName = counterName;
	}
	public String getCounterName() {
		return counterName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public String getInstanceName() {
		return instanceName;
	}
	
}
