import java.util.LinkedList;
import java.util.List;

public class Process {

	private int processID;
	private int size;
	private int noOfFramesUsed;
	private int faults;
	
	
	public List<Integer> getPagesInMemory() {
		return pagesInMemory;
	}

	public void setPagesInMemory(List<Integer> pagesInMemory) {
		this.pagesInMemory = pagesInMemory;
	}

	private int TLBMiss;
	private int TLBHit;

	List<Integer> queue = new LinkedList<Integer>();
	List<Integer> pagesInMemory = new LinkedList<Integer>();

	private PageTable pageTable;

	public PageTable getPageTable() {
		return pageTable;
	}

	public void setPageTable(PageTable pageTable) {
		this.pageTable = pageTable;
	}

	public int getNoOfFramesUsed() {
		return noOfFramesUsed;
	}

	public void setNoOfFramesUsed(int noOfFramesUsed) {
		this.noOfFramesUsed = noOfFramesUsed;
	}

	public int getFaults() {
		return faults;
	}

	public void setFaults(int faults) {
		this.faults = faults;
	}

	public int getTLBMiss() {
		return TLBMiss;
	}

	public void setTLBMiss(int tLBMiss) {
		TLBMiss = tLBMiss;
	}

	public int getTLBHit() {
		return TLBHit;
	}

	public void setTLBHit(int tLBHit) {
		TLBHit = tLBHit;
	}

	public Process(int processID, int size) {

		this.processID = processID;
		this.size = size;
		pageTable = new PageTable(this.size);

	}

	public int getProcessID() {
		return processID;
	}

	public int getSize() {
		return size;
	}

}