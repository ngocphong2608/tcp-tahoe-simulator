package com.tcp.tahoe.modules;

import com.tcp.tahoe.data.impl.Segment;

public class Link {
	private Segment segment; // the segment on the link
	private boolean isEmpty;
	private int speed; // the speed of the link in MB/s
	private long clk; // in milliseconds
	private long maxCount; // in milliseconds

	public Link(int speed) {
		// initializing the link speed
		this.speed = speed;

		isEmpty = true;
		
		// initializing the clock
		clk = 1;
	}

	public boolean isBusy() {
		// if there are no segments on the the link return true
		if (isEmpty)
			return false;
		else {
			// retrieving the segment MSS value
			long segmentMss = segment.getMss();
			
			// maxCount[ms] = MSS[bytes] * (1[s] / speed[MB]) * (1[MB] / 1048576[bytes]) * (1000[ms] / 1[s])
			maxCount = (segmentMss / speed) * (1 / 1048576) * (1000 / 1);

			// if the segment has waited their fair share on the link then the link is free
			if (clk < maxCount)
				return true;
			else
				return false;
		}
	}

	public boolean isEmpty() {
		// return true if there are any segments left on the link
		return isEmpty;
	}

	public Segment getData() {
		// returns the collection of segments on the link
		return segment;
	}

	public void freeLink() {
		isEmpty = true;

		// reset the clock
		clk = 1;
	}

	public void addData(Segment sendSegment) {
		isEmpty = false;
		
		// add the specified segments to the link
		segment = sendSegment;
	}

	public void incrementClk() {
		// Increment the clock of the link
		clk++;
	}

}
