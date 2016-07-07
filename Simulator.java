/*
  OS Assignment	
  
  Name					: 	Abhishek Singh (CS1423) 
  							Satish Chandra (CS1419)
  
  Date of Submission	:	10 Aug 2015
  
  Program Discription 	:  Write a simulator for a demand paging memory management system. The
						   following will be input parameters for your program:
						   - a file containing a list of processes and their sizes
						   - a file containing a sequence of memory accesses in the following form:
						     <pid> <logical address>
						   - page replacement algorithm (implement at least FIFO, optimal, true
						     LRU, and reference byte/second chance)
						   - amount of RAM and swap available
						   - number of TLB entries
						   Your simulator should have data structures for per-process page tables,
						   and the kernel frame table. The simulator's output should report TLB
						   hits/misses, and page faults for each process.
*/

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Scanner;

public class Simulator {

	private int TLB[][];
	public int maxNoOfFramesPerProcess;
	public int sizeOfTLB;
	public int memorySize;
	public int swapAreaSize;
	public int frameSize = 1024;
	public boolean invalidAccess = false;

	List<Integer> freeFramesList;
	HashMap<Integer, KernelTableEntry> kernelTable;
	List<Pair<Integer, Integer>> swapArea;

	/* Getter and Setter functions */
	public int getSizeOfTLB() {
		return sizeOfTLB;
	}

	public int getMemorySize() {
		return memorySize;
	}

	public int getSwapAreaSize() {
		return swapAreaSize;
	}

	public int getFrameSize() {
		return frameSize;
	}

	public int[][] getTLB() {
		return TLB;
	}

	public void setTLB(int[][] tLB) {
		TLB = tLB;
	}

	public List<Integer> getFreeFramesList() {
		return freeFramesList;
	}

	public void setFreeFramesList(List<Integer> freeFramesList) {
		this.freeFramesList = freeFramesList;
	}

	public HashMap<Integer, KernelTableEntry> getKernelTable() {
		return kernelTable;
	}

	public void setKernelTable(HashMap<Integer, KernelTableEntry> kernelTable) {
		this.kernelTable = kernelTable;
	}

	/* 
	 * Method to allocate kernel table.
	 * Number of entries in kernel table is same as number of frames in memory. 
	 */
	public void allocateKernelTable(int memorySize, int frameSize) {
		int noOfFrames = memorySize / frameSize;
		kernelTable = new HashMap<Integer, KernelTableEntry>();
		for (int i = 0; i < noOfFrames; i++) {
			KernelTableEntry KTE = new KernelTableEntry(-1, -1);
			kernelTable.put(i, KTE);
		}
	}

	/* Method to allocate swap area */
	public void allocateSwapArea(int swapAreaSize, int frameSize) {
		int noOfBlocks = swapAreaSize / frameSize;
		swapArea = new ArrayList<Pair<Integer, Integer>>();
		for (int i = 0; i < noOfBlocks; i++) {
			Pair<Integer, Integer> p = new Pair<Integer, Integer>(-1, -1);
			swapArea.add(i, p);
		}
	}

	/* Method to allocate free frame list */
	public void freeFrames(int memorySize, int frameSize) {
		freeFramesList = new LinkedList<Integer>();
		int noOfFrames = memorySize / frameSize;
		for (int i = 0; i < noOfFrames; i++) {
			freeFramesList.add(i);
		}
	}

	/* Method to initialise values from system */
	private void initialiseSystem(int memorySize, int swapAreaSize, int frameSize) {
		allocateTLB();
		allocateKernelTable(memorySize, frameSize);
		allocateSwapArea(swapAreaSize, frameSize);
		freeFrames(memorySize, frameSize);

	}

	/* Method to allocate TLB */
	public void allocateTLB() {
		TLB = new int[sizeOfTLB][3];
		for (int i = 0; i < sizeOfTLB; i++) {
			for (int j = 0; j < 2; j++) {
				TLB[i][j] = -1;

			}
		}
	}

	/* Method to flush TLB */
	public void flushTLB() {
		for (int i = 0; i < sizeOfTLB; i++) {
			for (int j = 0; j <= 2; j++) {
				if (j == 2)
					TLB[i][j] = 0;
				else
					TLB[i][j] = -1;
			}
		}
	}

	public static void main(String[] args) throws IOException {

		Process process;
		Reference ref;

		Scanner sc = new Scanner(System.in);
		Simulator sim = new Simulator();

		List<Process> processList = new ArrayList<Process>();
		List<Reference> refList = new ArrayList<Reference>();

		/*
		 * System.out.println(
		 * "Enter file name containing Processes and their Size: "); String
		 * fileOfProcess = sc.next();
		 * 
		 * System.out.println("Enter file name containing list of references: ");
		 * String fileOfRefs = sc.next();
		 */
		System.out.println("Page Replacement Algorithms: " + " \n" + "1. FIFO "
				+ "\n" + "2. LRU" + "\n" + "3. Optimal" + "\n"
				+ "4. Reference Bit Second Chance\n" + "5. Approximation LRU\n");
		System.out.println("Enter your choice: ");
		int choice = sc.nextInt();

		System.out.println("Enter amount of RAM (in Bytes): ");
		sim.memorySize = sc.nextInt();

		System.out.println("Enter amount of Swap Area (in Bytes): ");
		sim.swapAreaSize = sc.nextInt();

		System.out.println("Enter number of entries in TLB: ");
		sim.sizeOfTLB = sc.nextInt();

		sim.initialiseSystem(sim.memorySize, sim.swapAreaSize, sim.frameSize);

		sc.close();

		// BufferedReader br = new BufferedReader(new
		// FileReader(fileOfProcess));
		
		/* Read Process file and create object of each process and store it in processList */
		BufferedReader br = new BufferedReader(new FileReader("src/Process.txt"));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\\s+");
				process = new Process(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
				processList.add(process);
			}
		} 
		finally 
		{
			if (br != null)
				br.close();
		}

		// br = new BufferedReader(new FileReader(fileOfRefs));
		
		/* Read reference file and create object of each reference and store it in referenceList */
		br = new BufferedReader(new FileReader("src/references.txt"));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\\s+");

				ref = new Reference(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), tokens[2]);
				refList.add(ref);
			}
		} 
		finally 
		{
			if (br != null)
				br.close();
		}

		/* 
		 * Calculate maximum number of frames per process can be allocated in memory. 
		 * Fixed Allocation 
		 */
		sim.maxNoOfFramesPerProcess = (sim.memorySize / sim.frameSize) / processList.size();

		switch (choice) {
		case 1:
			FIFO fifoPageReplacement = new FIFO();
			fifoPageReplacement.allocateFrame(processList, refList, sim);
			break;

		case 2:
			LRU lruPageReplacement = new LRU();
			lruPageReplacement.allocateFrame(processList, refList, sim);
			break;

		case 3:
			Optimal optimalPageReplacement = new Optimal();
			optimalPageReplacement.allocateFrame(processList, refList, sim);
			break;

		case 4:
			ReferencedBitSecondChance refBitSecChangePageReplacement = new ReferencedBitSecondChance();
			refBitSecChangePageReplacement.allocateFrame(processList, refList,
					sim);
			break;

		case 5:
			ApproximationLRU approxLRU = new ApproximationLRU();
			approxLRU.allocateFrame(processList, refList, sim);
			break;
			
		default:
			System.out.println("No valid choice");
		}

		
		if(sim.invalidAccess == false)
		{
			System.out.println("\n----------------------------------------------------------------------");
			System.out.println("                         *Simulation Summary*                         ");
			System.out.println("----------------------------------------------------------------------");
			Iterator<Process> itr = processList.iterator();
			while (itr.hasNext()) {
				process = itr.next();
				System.out.println("\nProcess " + process.getProcessID() + ": \n");
				System.out.println("\tTLB Hit for Process : " + process.getTLBHit());
				System.out.println("\tTLB Miss for Process : "	+ process.getTLBMiss());
				System.out.println("\tNumber of Page Fault for Process : " + process.getFaults());
			}
			System.out.println("----------------------------------------------------------------------");
		}
		else
			System.out.println("\n\nERROR:\nReferenced Logical Address is Out of Process Range");
	}
}
