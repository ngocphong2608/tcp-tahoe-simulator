package com.tcp.tahoe.modules;

import java.util.ArrayList;
import java.util.List;

import com.tcp.tahoe.data.impl.Segment;

public class Link {
	private List<Segment> segments; // the collection of segments on the link
	private int speed; // the speed of the link in MB/s
	private long clk; // in milliseconds
	private long maxCount; // in milliseconds

	public Link(int speed) {
		// initializing the link speed
		this.speed = speed;

		// initializing the collection of segments on the link
		segments = new ArrayList<Segment>();

		// initializing the clock
		clk = 1;
	}

	public boolean isBusy() {
		// if there are no segments on the the link return true
		if (segments.isEmpty())
			return false;
		else {
			// retrieving the segments MSS value
			long segmentMss = segments.iterator().next().getMss();
			// TODO Change to microSeconds
			// maxCount[ms] = MSS[bytes] * (1[s] / speed[MB]) * (1[MB] / 1048576[bytes]) * (1000[ms] / 1[s])
			maxCount = (segmentMss / speed) * (1 / 1048576) * (1000 / 1);

			// if the segments have waited their fair share on the link then
			// link is free
			if (clk < maxCount)
				return true;
			else
				return false;
		}
	}

	public boolean isEmpty() {
		// return true if there are any segments left on the link
		return segments.isEmpty();
	}

	public List<Segment> getData() {
		// returns the collection of segments on the link
		return segments;
	}

	public void freeLink() {
		// remove all the segments
		segments.removeAll(segments);

		// reset the clock
		clk = 1;
	}

	public void addData(List<Segment> sendSegments) {
		// add the specified segments to the link
		segments.addAll(sendSegments);
	}

	public void incrementClk() {
		// incrment the clock of the link
		clk++;
	}

}
