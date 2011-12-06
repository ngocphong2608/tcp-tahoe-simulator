package com.tcp.tahoe.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.tcp.tahoe.data.impl.AckPacket;
import com.tcp.tahoe.data.impl.SenderVariableDoubleData;
import com.tcp.tahoe.data.impl.SenderVariableLongData;
import com.tcp.tahoe.data.impl.Segment;

public class Sender {
	private int state; // 0 - for slow start & 1 - for congestion avoidance
	
	private boolean initialSSThresh;
	private boolean rtoTimeout;
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
	
	private List<Segment> segmentsSent;
	private Queue<Segment> segmentsToSend;
	private List<AckPacket> acknowledgements;
	private List<AckPacket> dupAcknowlegements;
	
	//for dataCollection
	private Collection<SenderVariableLongData> congWinCollection;
	private Collection<SenderVariableLongData> effectiveWinCollection;
	private Collection<SenderVariableLongData> flightSizeCollection;
	private Collection<SenderVariableLongData> ssThreshCollection;
	private Collection<SenderVariableDoubleData> senderUtility;


	public Sender(long mss, long rtt, int numberOfPackets, long RcvWindow) {
		this.state = 0;
		this.CongWindow = 1;
		
		this.rtoTimeout = false;
		this.initialSSThresh = true;
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
		
		this.segmentsSent = new ArrayList<Segment>();
		this.segmentsToSend = new LinkedList<Segment>();
		this.acknowledgements = new ArrayList<AckPacket>();
		this.dupAcknowlegements = new ArrayList<AckPacket>();
		
		//Data Collection
		this.congWinCollection = new ArrayList<SenderVariableLongData>();
		this.effectiveWinCollection = new ArrayList<SenderVariableLongData>();
		this.flightSizeCollection = new ArrayList<SenderVariableLongData>();
		this.ssThreshCollection = new ArrayList<SenderVariableLongData>();
		this.senderUtility = new ArrayList<SenderVariableDoubleData>();
	}

	public boolean isDoneSending() {
		if (numOfPackets == 0) {
			return true;
		} else {
			if(acknowledgements.isEmpty())
				return false;
			else if (acknowledgements.get(acknowledgements.size()-1).getId() >= numOfPackets)
				return true;
			else 
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
	
	public long getFlightSize(){
		return FlightSize;
	}
	
	public void sendSegments(long clock, int linkSpeed){
		rtoTimeout = false;
		
		//removing all in the segmentsToSend queue
		segmentsToSend.removeAll(segmentsToSend);
		rttCount = rtt;

		//finding the LastByteAcked
		if(!acknowledgements.isEmpty())
			LastByteAcked = (acknowledgements.get(acknowledgements.size() - 1).getId() * mss);
		else 
			LastByteAcked = 0;
				
		//start check for RTO timeout
		int lastIdSegmentAcked = (int) (LastByteAcked / mss);
		for(int i = lastIdSegmentAcked; i < segmentsSent.size(); i++){
			Segment tempSeg = segmentsSent.get(i); 
			tempSeg.incrementRTOCount();
			if(tempSeg.getRTOCount() >= 3){
				state = 1;
				rtoTimeout = true;
				CongWindow = mss;
				state = 1;
				initialSSThresh = false;
			}
		}
		//end check for RTO timeout
				
		FlightSize = LastByteSent - LastByteAcked;
		
		EffectiveWindow = Math.min(CongWindow, RcvWindow) - FlightSize;
			
		if(initialSSThresh)
			SSThresh = 65535;
		else
			SSThresh = Math.max((long)(0.5 * FlightSize), 2 * mss);

		
		// -----------------------------------------
		//if loss is detected -- fast retransmit
		if(state == 1){
			state = 0;
			EffectiveWindow = 1 * mss;
			
			if(rtoTimeout){
				for(int i = lastIdSegmentAcked; i < segmentsSent.size(); i++){
					Segment tempSeg = segmentsSent.get(i);
					if(tempSeg.getRTOCount() >= 3){
						tempSeg.resetRTOCount();
						segmentsToSend.add(tempSeg);
						//storing what segments have been sent
						addToSegmentsSent(tempSeg);
						System.out.println("RTO Timeout -- Resending Outstanding Segment");
						break;
					}
				}
			} else {
				Segment tempSeg;
				
				tempSeg = new Segment(acknowledgements.get(acknowledgements.size() - 1).getId(), mss);
				
				
				
				System.out.println("3 Duplicate ACK -- Resending Outstanding Segment");
				
				segmentsToSend.add(tempSeg);
				//storing what segments have been sent
				addToSegmentsSent(tempSeg);
			}
		} else {
			//send next available segments
			if(EffectiveWindow < (0 * mss))
				EffectiveWindow = 1 * mss;
			
			int lastIdSend;
			if(!acknowledgements.isEmpty())
				lastIdSend = segmentsSent.get(segmentsSent.size()-1).getId() + 1;
			else 
				lastIdSend = 0;
				
			int lastAckReceived; 
			if(!acknowledgements.isEmpty())
				lastAckReceived = acknowledgements.get(acknowledgements.size()-1).getId();
			else
				lastAckReceived = 0;
			
			for(int i=0; i< EffectiveWindow/mss; i++){
				if((lastAckReceived) < numOfPackets){
					Segment tempSeg = new Segment(lastIdSend,mss);
					segmentsToSend.add(tempSeg);
					lastIdSend++;
					
					//storing what segments have been sent
					addToSegmentsSent(tempSeg);
				} else {
					System.out.println();
				}
			}
		}
		// -----------------------------------------
		
		
		
		//Adding to Collection for graphing data
		double temp1 = ((double)linkSpeed) / 1000000;
		double temp2 = rtt;
		double temp3 = ((double)Math.pow(2, 20)) / 1;
		double maxSegments =  (long) (temp1 * temp2 * temp3);
		double senderUtilityCount = ((double)EffectiveWindow * 100)/maxSegments;
		
		senderUtility.add(new SenderVariableDoubleData(clock, senderUtilityCount));
		congWinCollection.add(new SenderVariableLongData(clock, CongWindow));
		effectiveWinCollection.add(new SenderVariableLongData(clock, EffectiveWindow));
		flightSizeCollection.add(new SenderVariableLongData(clock, FlightSize));
		ssThreshCollection.add(new SenderVariableLongData(clock, SSThresh));
		
		//finding the LastByteSent
		LastByteSent = (segmentsSent.get(segmentsSent.size() - 1).getId() * mss) + mss;
	}
	private void addToSegmentsSent(Segment tempSeg){
		//storing what segments have been sent
		if(segmentsSent.isEmpty()){
			segmentsSent.add(tempSeg);
		} else {
			boolean foundSegment = false;
			for(Segment seg : segmentsSent){
				if(seg.getId() == tempSeg.getId()){
					foundSegment = true;
					break;
				}
			}
			if(!foundSegment)
				segmentsSent.add(tempSeg);
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
		
		//adding the ack to the acknowledgment list
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
			if((CongWindow) <= SSThresh){
				//Slow Start
				CongWindow += mss;
			} else {
				//Congestion Avoidance
				double temp = mss * (((double)mss)/CongWindow);
				CongWindow = (long) (CongWindow + temp);
			}
		} else if(dupAcknowlegements.size() >= 3){
			//loss is detected
			CongWindow = mss;
			state = 1;
			initialSSThresh = false;
		} 

	}

	public void incrementClk() {
		rttCount--;
	}
	
	public Collection<SenderVariableLongData> getCongWindowData(){
		return congWinCollection;
	}
	
	public Collection<SenderVariableLongData> getEffectWinData(){
		return effectiveWinCollection;
	}
	
	public Collection<SenderVariableLongData> getFlightSizeData(){
		return flightSizeCollection;
	}
	
	public Collection<SenderVariableLongData> getSSThreshData(){
		return ssThreshCollection;
	}
	
	public Collection<SenderVariableDoubleData> getSenderUtilityData(){
		return senderUtility;
	}
}
