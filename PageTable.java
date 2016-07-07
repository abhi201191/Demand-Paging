import java.util.HashMap;

public class PageTable {

	HashMap<Integer, PageTableEntry> PT = new HashMap<Integer, PageTableEntry>();

	private PageTableEntry PTE;

	public PageTable(int size) {
		for (int i = 0; i < size; i++) {
			PTE = new PageTableEntry(-1);
			PT.put(i, PTE);
		}
	}

	public PageTableEntry getPTE() {
		return PTE;
	}

}
