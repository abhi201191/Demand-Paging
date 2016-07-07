import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ReferencedBitSecondChance extends PageReplacement {

	/* Function to return victim frame */
	public int getVictimFrame(List<Integer> fifoQueue, PageTable PTable)
	{
		int victimFrame = -1;
		for(int i = 0; i < fifoQueue.size(); i++)
		{
			/* Get victim frame from fifoQueue */
			victimFrame = fifoQueue.get(i);
			
			/* 
			 * If its reference bit is 1 then give it second chance 
			 * and set its reference bit to 0 
			 * else return this page as victim frame  
			 */
			if(PTable.PT.get(victimFrame).getReferenceBit() == 1)
				PTable.PT.get(victimFrame).setReferenceBit(0);
			
			else
				return victimFrame;
		}
		
		if(victimFrame == -1)
		{
			/* Return first page as victim frame */
			victimFrame = fifoQueue.get(0);
			return victimFrame;
		}
		return victimFrame;
	}
	
	
	@Override
	public void allocateFrame(List<Process> processList,
			List<Reference> refList, Simulator sim) {
		Reference ref;
		Process process;
		PageTable PTable;
		PageTableEntry PTE;

		/*
		 * Used for FIFO page replacement, to store referenced pages in FIFO
		 * order. Helps in selecting victim page
		 */
		List<Integer> fifoQueue = new LinkedList<Integer>();

		/*
		 * Each referenced TLB entry is stored in FIFO order. Helps in selecting
		 * victim TLB entry
		 */
		List<Integer> queueForTLB = new LinkedList<Integer>();
		List<Integer> freeFramesList;
		HashMap<Integer, KernelTableEntry> kernelTable;

		int victimFrame, swapIndex = -1, processIDnew, pageNo, frameNo, processIDold = -1, TLBindex = 0;
		boolean presentInTLB;

		/* Iterate over references */
		Iterator<Reference> itr = refList.iterator();
		while (itr.hasNext()) {
			presentInTLB = false;
			ref = itr.next();
			processIDnew = ref.getProcessID();
			System.out.println("\nProcess " + processIDnew + "\nLogical Address <"
					+ ref.getLogicalAddress() + ">");

			/* If new process is going to execute, then flush TLB */
			if (processIDnew != processIDold) {
				System.out.println("\tContext Switch. TLB is flushed.");
				sim.flushTLB();
				queueForTLB.clear();
				TLBindex = 0;
			}

			/* Get process corresponding to process id */
			process = processList.get(processIDnew - 1);
			
			/* Check for out of range reference */
			if(ref.getLogicalAddress() >= process.getSize())
			{
				sim.invalidAccess = true;
				break;
			}

			/* Calculate in which page number, logical address is present */
			pageNo = (ref.getLogicalAddress()) / sim.frameSize;

			/* Get TLB */
			TLB = sim.getTLB();

			/* Get page table corresponding to process which is going to execute */
			PTable = process.getPageTable();
			fifoQueue = process.queue;

			/*
			 * Check whether entry for the page no is present in TLB or not.
			 * Each row contains of TLB contains Page No, Frame No and Dirty
			 * Bit.
			 */
			for (int i = 0; i < sim.sizeOfTLB; i++) {
				if (TLB[i][0] == pageNo) {
					
					/* If Operation is Write operation then set dirty bit to 1 */
					if (ref.getAccessType() == "W")
						TLB[i][2] = 1;
					/* else set to 0 */
					else
						TLB[i][2] = 0;

					/* Increment TLB hit count for this process */
					process.setTLBHit(process.getTLBHit() + 1);
					presentInTLB = true;
					
					/* Add this referenced TLB entry to queue */
					queueForTLB.add(i);
					System.out.println("\tTLB hit.");
					System.out.println("\tPage No " + pageNo
							+ " is present at Frame No " + TLB[i][1]);
					
					/* Set its reference bit to 1 because it is referenced again */
					PTable.PT.get(pageNo).setReferenceBit(1);
				}
			}

			/* If entry for page no is not present in TLB */
			if (presentInTLB != true) {
				
				/* Increment TLB hit count for this process */
				process.setTLBMiss(process.getTLBMiss() + 1);

				/* Get Page table entry corresponding to page no */
				PTE = PTable.PT.get(pageNo);

				/* If operation is Write operation, then set dirty bit to 1 */
				if (ref.getAccessType() == "W")
					PTE.setDirtyBit(1);

				/*
				 * Check whether this page table entry is valid or not by
				 * checking valid bit. If valid bit is set. Then get the frame
				 * number and access that page from memory.
				 */
				if (PTE.getValidBit() == 1) {
					System.out.println("\tTLB Miss.");
					System.out.println("\tEntry for page present in Page Table");
					System.out.println("\tPage No " + pageNo
									+ " is present at Frame No "
									+ PTE.getFrameNumber());
					
					/* Set its reference bit to 1 because it is referenced again */
					PTE.setReferenceBit(1);
				}

				/*
				 * If valid bit is not set. Then page is not present in memory
				 * and to be brought into memory
				 */
				else if (PTE.getValidBit() != 1) {
					
					/* Increment page fault count for this process */
					process.setFaults(process.getFaults() + 1);

					System.out.println("\tTLB Miss.");	
					System.out.println("\tPAGE FAULT occurs.");
					System.out.println("\tPage not present in memory and to be brought into memory");

					/* Get kernel table */
					kernelTable = sim.getKernelTable();

					/*
					 * If no of frames used by process is less than max number
					 * of frames that can be allocated to process. Then get free
					 * frame in memory and brought page to that free frame and
					 * change kernel table entry accordingly.
					 */
					if (process.getNoOfFramesUsed() < sim.maxNoOfFramesPerProcess) {
						System.out.println("\tFree Space is available in Memory.");
						
						/* Get free frame list and remove first frame from list */
						freeFramesList = sim.getFreeFramesList();
						frameNo = freeFramesList.remove(0);

						/*
						 * Store process id and page no to kernel table entry
						 * corresponding to frame where page is brought in
						 */
						kernelTable.put(frameNo,new KernelTableEntry(process.getProcessID(),pageNo));

						/*
						 * Set frame no in page table entry with the frame no
						 * where page is placed
						 */
						PTE.setFrameNumber(frameNo);

						/* Set page table entry dirty bit */
						PTE.setValidBit(1);

						/* Increment no. of frames used for this process */
						process.setNoOfFramesUsed((process.getNoOfFramesUsed()) + 1);
						
						/* Set its reference bit to 0 because it is new page */
						PTE.setReferenceBit(0);
						System.out.println("\tPage No " + pageNo + " is now allocated in Frame No " + frameNo);
					}

					/*
					 * If no. of frames allowed for process are filled up then
					 * select one victim frame and replace that frame with the
					 * page to be referenced.
					 */
					else {

						/*
						 * If page is present in Swap Area then swap in that
						 * page back to main memory after swapping out victim
						 * page.
						 */
						if (PTE.getFrameNumber() != -1)
							System.out.println("\tPage is present in Swap Area");
						/* Else page to be brought from Hard Disk */
						else
							System.out.println("\tPage to be brought from Hard Disk");

						System.out.println("\tSpace is Memory is not Free. Victim Frame to be selected.");
						
						/* Get Victim Frame and remove that from fifoQueue */
						victimFrame = getVictimFrame(fifoQueue, PTable);
						System.out.println("\tVictim Frame No :" + victimFrame);
						fifoQueue.remove(fifoQueue.indexOf(victimFrame));
						
						/*
						 * Check free area in swap area and place that victim
						 * frame at that area of swap area.
						 */
						boolean isFound = false;

						for (int i = 0; i < (sim.swapAreaSize / sim.frameSize); i++) {
							if ((sim.swapArea.get(i).getFirst() == processIDnew)
									&& (sim.swapArea.get(i).getSecond() == victimFrame)) {
								swapIndex = i;
								isFound = true;
								break;
							}
						}

						if (isFound == false) {
							for (int i = 0; i < (sim.swapAreaSize / sim.frameSize); i++) {
								if ((sim.swapArea.get(i).getFirst() == -1)
										&& (sim.swapArea.get(i).getSecond() == -1)) {
									swapIndex = i;
									break;
								}
							}
						}
						
						/*
						 * Set frame number of page table entry of page to be
						 * reference by the frame number of main memory where
						 * victim frame was present
						 */
						PTE.setFrameNumber(PTable.PT.get(victimFrame).getFrameNumber());
						
						/* Set valid bit */
						PTE.setValidBit(1);
						PTE.setReferenceBit(0);

						System.out.println("\tPage No " + pageNo + " is now allocated in Frame No " + PTable.PT.get(victimFrame).getFrameNumber());
						
						/* Change page table entry values for victim frame. */
						PTable.PT.get(victimFrame).setFrameNumber(swapIndex);
						if(isFound == true && PTable.PT.get(victimFrame).getDirtyBit() == 1)
						{
							Pair<Integer, Integer> p = new Pair<Integer, Integer>(processIDnew, victimFrame);
							sim.swapArea.set(swapIndex, p);
						}
						
						
						PTable.PT.get(victimFrame).setValidBit(0);
						PTable.PT.get(victimFrame).setDirtyBit(0);

						/* Remove entry of victim frame from TLB */
						for (int i = 0; i < sim.sizeOfTLB; i++) {
							if (TLB[i][0] == victimFrame) {
								TLB[i][0] = TLB[i][1] = -1;
								TLB[i][2] = 0;
								queueForTLB.remove(queueForTLB.indexOf(i));
								TLBindex--;
							}
						}
						
						if(isFound == false)
						{
							Pair<Integer, Integer> p = new Pair<Integer, Integer>(processIDnew, victimFrame);
							sim.swapArea.set(swapIndex, p);
						}

						/* Set reference bit of victim frame to 0 */
						PTable.PT.get(victimFrame).setReferenceBit(0);
					}
					
					/* Add referenced page to fifoQueue */
					fifoQueue.add(pageNo);
				}
				
				/* Page which is reference, then its entry should be present in TLB */
				/* If TLB is not full then store entries in free TLB entry */
				if (TLBindex != sim.sizeOfTLB) {
					for(int i = 0; i < sim.sizeOfTLB ; i++)
					{
						if(TLB[i][0] == -1)
						{
							/* Store page number and frame number */
							TLB[i][0] = pageNo;
							TLB[i][1] = PTE.getFrameNumber();

							/* If operation is write operation then set dirty bit */
							if (ref.getAccessType() == "W")
								TLB[i][2] = 1;
							else
								TLB[i][2] = 0;

							/* Add TLB index in queueForTLB */
							queueForTLB.add(i);
						}
					}
					TLBindex++;
				}
				
				/* TLB is full then choose victim entry */
				else {
					/* Remove first element from queueForTLB as victim entry */
					int rowNo = queueForTLB.remove(0);
					
					/* If TLB entry for dirty bit for victim is set, then set dirty bit for corresponding page no in page table */ 
					if (TLB[rowNo][2] == 1)
						PTable.PT.get(TLB[rowNo][0]).setDirtyBit(1);
					
					/* Store page number and frame number */
					TLB[rowNo][0] = pageNo;
					TLB[rowNo][1] = PTE.getFrameNumber();
					
					/* If operation is write operation then set dirty bit */
					if (ref.getAccessType() == "W")
						TLB[rowNo][2] = 1;
					else
						TLB[rowNo][2] = 0;
					
					/* Add TLB index in queueForTLB */
					queueForTLB.add(rowNo);
				}
				processIDold = processIDnew;
			}
		}
	}
}
