import java.util.*;

public abstract class PageReplacement {

	public int[][] TLB;

	public abstract void allocateFrame(List<Process> processList,
			List<Reference> refList, Simulator sim);

}
