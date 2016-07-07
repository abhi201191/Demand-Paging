public class KernelTableEntry {
	private int processID;
	private int pageNo;

	public int getProcessID() {
		return processID;
	}

	public void setProcessID(int processID) {
		this.processID = processID;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public KernelTableEntry(int processID, int pageNo) {
		this.processID = processID;
		this.pageNo = pageNo;
	}

}
