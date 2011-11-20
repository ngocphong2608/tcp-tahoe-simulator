package com.tcp.tahoe.modules;

import java.util.ArrayList;
import java.util.List;

import com.tcp.tahoe.data.impl.AckPacket;
import com.tcp.tahoe.data.impl.Segment;

public class Receiver {
	
	private long RcvWindow;
	
	//ordered collection in ascending order
	private List<Segment> bufferedSegments;
	private List<Segment> recievedSegments;
	
	public Receiver(long RcvWindow) {
		this.RcvWindow = RcvWindow;
		bufferedSegments = new ArrayList<Segment>();
		recievedSegments = new ArrayList<Segment>();
	}

	public void recieveSegment(List<Segment> data) {
		int lastIdSegmentRecieved;
		
		//if first time and there are no received Segments set lastIdSegmentRecieved to equal -1
		if(recievedSegments.isEmpty())
			lastIdSegmentRecieved = -1;
		else
			lastIdSegmentRecieved = recievedSegments.get(recievedSegments.size() - 1).getId();
		
		while(true){
			lastIdSegmentRecieved++;
			boolean foundSegment = false;
			
			for(Segment segment : data){
				if(segment.getId() == lastIdSegmentRecieved){
					recievedSegments.add(segment);
					data.remove(segment);
					foundSegment = true;
				}
			}
			if(!foundSegment){
				for(Segment segment : bufferedSegments){
					if(segment.getId() == lastIdSegmentRecieved){
						recievedSegments.add(segment);
						bufferedSegments.remove(segment);
						foundSegment = true;
					}
				}
			}
			
			if(!foundSegment)
				break;
		}
		
		for(Segment segment : data){
			// retrieving the MSS of the segment in bytes
			long segmentMss = segment.getMss();
			
			if(RcvWindow >= segmentMss){
				bufferedSegments.add(segment);
				RcvWindow = RcvWindow + segmentMss;
			}
		}

	}

	public AckPacket getAck() {
		if(recievedSegments.isEmpty())
			return new AckPacket(0);
		else
			return new AckPacket(recievedSegments.get(recievedSegments.size() - 1).getId());
	}
}
