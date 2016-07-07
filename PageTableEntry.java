public class PageTableEntry {

	private int frameNumber;
	private int validBit;
	private int referenceBit;
	private int dirtyBit;
	private char referenceByte;

	public PageTableEntry(int frameNumber) {
		this.frameNumber = frameNumber;
	}

	public int getFrameNumber() {
		return frameNumber;
	}

	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}

	public int getValidBit() {
		return validBit;
	}

	public void setValidBit(int validBit) {
		this.validBit = validBit;
	}

	public int getReferenceBit() {
		return referenceBit;
	}

	public void setReferenceBit(int referenceBit) {
		this.referenceBit = referenceBit;
	}

	public int getDirtyBit() {
		return dirtyBit;
	}

	public void setDirtyBit(int dirtyBit) {
		this.dirtyBit = dirtyBit;
	}

	public char getReferenceByte() {
		return referenceByte;
	}

	public void setReferenceByte(char referenceByte) {
		this.referenceByte = referenceByte;
	}
	
	
}
