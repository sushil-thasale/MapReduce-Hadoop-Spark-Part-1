/*
 * NoSharing_ProcessorThread_Fibo extends thread class and computes average TMAX 
 * for the assigned load and stores it in local data structure "threadMap"
 * Also, adds a delay!
 */



import java.util.HashMap;

class NoSharing_ProcessorThread_Fibo extends Thread {

	long start, end;
	CentralData c;
	HashMap<String, RecordEntry> threadMap = new HashMap<String, RecordEntry>();

	/*
	 * @param start : starting position of the assigned subset of input file
	 * 
	 * @param end : end position of the assigned subset of input file
	 * 
	 * @param c : instance of CentralData to access stationMap
	 */
	public NoSharing_ProcessorThread_Fibo(long start, long end, CentralData c) {
		this.start = start;
		this.end = end;
		this.c = c;
		start();
	}

	public void run() {
		computeAverageTMAX();
	}

	/*
	 * computeAverageTMAX : computes average TMAX of each station from assigned
	 * load
	 */
	public void computeAverageTMAX() {

		String record = "";

		for (long i = start; i < end; i++) {

			record = c.csvArrayList.get((int) i);
			if (record.contains("TMAX")) {
				String columnValues[] = record.split(",");
				String stationID = columnValues[0];
				double tmax = Integer.parseInt(columnValues[3]);

				if (threadMap.containsKey(stationID)) {
					threadMap.get(stationID).computeAverage(tmax);
					fibo();
				} else {
					RecordEntry e = new RecordEntry();
					e.computeAverage(tmax);
					threadMap.put(stationID, e);
					fibo();
				}

			}
		}
	}

	/*
	 * getThreadMap : Returns results stored in local data structure
	 */
	public HashMap<String, RecordEntry> getThreadMap() {
		return threadMap;
	}

	/*
	 * fibo : computes fibo(17) and adds delay
	 */
	void fibo() {
		int num = 7;
		int x = 0, y = 1, z = 0;

		for (int i = 2; i <= num; i++) {
			z = x + y;
			x = y;
			y = z;
		}
	}

}
