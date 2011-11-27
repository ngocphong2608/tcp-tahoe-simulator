package com.tcp.tahoe.modules;

import java.util.ArrayList;
import java.util.List;

import com.tcp.tahoe.data.impl.AckPacket;
import com.tcp.tahoe.data.impl.Segment;

public class Receiver {

	private long rcvWindow;
	private long bufferSpace;

	// ordered collection in ascending order
	private List<Segment> bufferedSegments;
	private List<Segment> recievedSegments;

	public Receiver(long RcvWindow) {
		this.bufferSpace = this.rcvWindow = RcvWindow;
		bufferedSegments = new ArrayList<Segment>();
		recievedSegments = new ArrayList<Segment>();
	}

	public void recieveSegment(Segment data) {
		int lastIdSegmentRecieved;

		// if first time and there are no received Segments set
		// lastIdSegmentRecieved to equal -1
		if (recievedSegments.isEmpty())
			lastIdSegmentRecieved = -1;
		else
			lastIdSegmentRecieved = recievedSegments.get(
					recievedSegments.size() - 1).getId();

		lastIdSegmentRecieved++;
		if (data.getId() == lastIdSegmentRecieved) {
			recievedSegments.add(data);

			// check if there is anything in the buffer than can fill up the
			// received segments list
			while (true) {
				lastIdSegmentRecieved++;
				boolean addedSegment = false;
				for (Segment segment : bufferedSegments) {
					if (segment.getId() == lastIdSegmentRecieved) {
						recievedSegments.add(segment);
						
						bufferedSegments.remove(segment);
						bufferSpace = bufferSpace + segment.getMss();
						
						addedSegment = true;
					}
				}
				if (!addedSegment)
					break;
			}

		} else if (data.getId() < lastIdSegmentRecieved) {
			// already received segment, so if it is a duplicate then discard
		} else if (data.getId() > lastIdSegmentRecieved) {
			// check if there is enough room in the reciever's buffer space
			// if there is enough room then add the data otherwise discard it
			if (bufferSpace >= data.getMss()) {
				// check is there is a duplicate segment in the buffer
				boolean duplicateBuffered = false;
				for (Segment segment : bufferedSegments) {
					if (segment.getId() == data.getId()) {
						duplicateBuffered = true;
					}
				}
				if (!duplicateBuffered) {
					bufferedSegments.add(data);
					bufferSpace = bufferSpace - data.getMss();
				}
			}
		}
	}

	public AckPacket getAck() {
		if (recievedSegments.isEmpty())
			return new AckPacket(0);
		else
			return new AckPacket(recievedSegments.get(
					recievedSegments.size() - 1).getId() + 1);
	}
}
