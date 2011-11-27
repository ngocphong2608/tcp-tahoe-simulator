package com.tcp.tahoe.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.tcp.tahoe.data.impl.AckPacket;
import com.tcp.tahoe.data.impl.SenderVariableData;
import com.tcp.tahoe.data.impl.Segment;

public class Sender {
	private int state; // 0 - for slow start & 1 - for congestion avoidance
	
	
	private long mss;
	private long rtt;
	private long rttCount;
	private int numOfPackets;
	
	private long RcvWindow;
	private long CongWindow;
	private long EffectiveWindow;
	private long LastByteAcked;
	private long LastByteSent;
	private long FlightSize;
	private long SSThresh;
	
	private Queue<Segment> segmentsToSend;
	private List<AckPacket> acknowledgements;
	private List<AckPacket> dupAcknowlegements;
	
	//for dataCollection
	private Collection<SenderVariableData> congWinCollection;
	private Collection<SenderVariableData> effectiveWinCollection;
	private Collection<SenderVariableData> flightSizeCollection;
	private Collection<SenderVariableData> ssThreshCollection;

	public Sender(long mss, long rtt, int numberOfPackets, long RcvWindow) {
		this.state = 0;
		this.CongWindow = 1;
		
		this.mss = mss;
		this.rttCount = 0;
		this.rtt = rtt;
		this.numOfPackets = numberOfPackets;
		
		//Sender algorithm variables
		this.RcvWindow = RcvWindow;
		this.CongWindow = mss;
		this.EffectiveWindow = 1;
		this.LastByteAcked = 0;
		this.LastByteSent = 0;
		this.FlightSize = 0;
		this.SSThresh = 0;
		
		this.segmentsToSend = new LinkedList<Segment>();
		this.acknowledgements = new ArrayList<AckPacket>();
		this.dupAcknowlegements = new ArrayList<AckPacket>();
		
		//Data Collection
		this.congWinCollection = new ArrayList<SenderVariableData>();
		this.effectiveWinCollection = new ArrayList<SenderVariableData>();
		this.flightSizeCollection = new ArrayList<SenderVariableData>();
		this.ssThreshCollection = new ArrayList<SenderVariableData>();
	}

	public boolean isDoneSending() {
		if (numOfPackets == 0) {
			return true;
		} else if (acknowledgements.size() >= numOfPackets) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasSomethingtoSend() {
		if (segmentsToSend.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean isRTODone(){
		if(rttCount > 0)
			return false;
		else {
			rttCount = rtt;
			return true;
		}
	}

	public Segment sendNextSegment(){
		if (!segmentsToSend.isEmpty())
			return segmentsToSend.remove();
		else 
			return null;
	}
	
	public void sendSegments(long clock){
		//removing all in the segmentsToSend queue
		segmentsToSend.removeAll(segmentsToSend);
		rttCount = rtt;

		//finding the LastByteAcked
		if(!acknowledgements.isEmpty())
			LastByteAcked = (acknowledgements.get(acknowledgements.size() - 1).getId() * mss) - mss;
		else 
			LastByteAcked = 0;
				
				
		FlightSize = LastByteSent - LastByteAcked;
		EffectiveWindow = Math.min(CongWindow, RcvWindow) - FlightSize;
			
		if(state == 0)
			SSThresh = 65535;
		else
			SSThresh = Math.max((long)(0.5 * FlightSize), 2 * mss);

		
		// -----------------------------------------
		//if loss is detected -- fast retransmit
		if(state == 1){
			state = 0;
			EffectiveWindow = 1 * mss;		
			segmentsToSend.add(new Segment(acknowledgements.get(acknowledgements.size() - 1).getId(), mss));
		} else {
			//send next available segments
			int lastAckId;
			if(!acknowledgements.isEmpty())
				lastAckId = acknowledgements.get(acknowledgements.size() - 1).getId();
			else 
				lastAckId = 0;
				
			for(int i=0; i< EffectiveWindow/mss; i++){
				segmentsToSend.add(new Segment(lastAckId,mss));
				lastAckId++;
			}
		}
		// -----------------------------------------
		
		
		
		//Adding to Collection for graphing data
		congWinCollection.add(new SenderVariableData(clock, CongWindow));
		effectiveWinCollection.add(new SenderVariableData(clock, EffectiveWindow));
		flightSizeCollection.add(new SenderVariableData(clock, FlightSize));
		ssThreshCollection.add(new SenderVariableData(clock, SSThresh));
		
		//finding the LastByteSent
		Iterator<Segment> iter = segmentsToSend.iterator();
		while(iter.hasNext()){
			LastByteSent = iter.next().getId() * mss;
		}
		
		
	}

	public void recieveAck(AckPacket ack) {
			
		if(dupAcknowlegements.isEmpty()){
			dupAcknowlegements.add(ack);
		} else {
			if(dupAcknowlegements.get(0).getId() == ack.getId()){
				dupAcknowlegements.add(ack);
			} else {
				dupAcknowlegements.removeAll(dupAcknowlegements);
				dupAcknowlegements.add(ack);
			}
		}
		
		//adding the ack to the acknowledgement list
		boolean foundAcknowledgement = false;;
		for(AckPacket acknowledgement : acknowledgements){
			if(acknowledgement.getId() == ack.getId()){
				foundAcknowledgement = true;
			}
		}
		if(!foundAcknowledgement){
			acknowledgements.add(ack);
		}
		
		//Calculating the congestionWindow
		if(dupAcknowlegements.size() <= 1){
			if((CongWindow + mss) <= SSThresh){
				//Slow Start
				CongWindow += mss;
			} else {
				//Congestion Avoidance
				CongWindow = CongWindow + mss * (mss/CongWindow);
			}
		} else if(dupAcknowlegements.size() >= 3){
			//loss is detected
			CongWindow = mss;
			state = 1;
		} 

	}

	public void incrementClk() {
		rttCount--;
	}
	
	public Collection<SenderVariableData> getCongWindowData(){
		return congWinCollection;
	}
	
	public Collection<SenderVariableData> getEffectWinData(){
		return effectiveWinCollection;
	}
	
	public Collection<SenderVariableData> getFlightSizeData(){
		return flightSizeCollection;
	}
	
	public Collection<SenderVariableData> getSSThreshData(){
		return ssThreshCollection;
	}
}
