public class Reference {

	int processID;
	int logicalAddress;
	String accessType;

	public Reference(int processId, int logicalAddress, String accessType) {
		this.processID = processId;
		this.logicalAddress = logicalAddress;
		this.accessType = accessType;
	}

	public String getAccessType() {
		return accessType;
	}

	public int getProcessID() {
		return processID;
	}

	public int getLogicalAddress() {
		return logicalAddress;
	}

}
