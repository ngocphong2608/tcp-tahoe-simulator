package com.tcp.tahoe.data.impl;
import com.tcp.tahoe.data.Packet;

public class Segment extends Packet {
	private long mss;
	private int rtoCount;
	private long bufferStartTime;
	private long bufferEndTime;

	public Segment(int id, long mss) {
		super(id);
		this.mss = mss;
		this.rtoCount = 0;
		bufferStartTime = 0;
		bufferEndTime = 0;
	}
	
	public long getMss(){
		return mss;
	}
	
	public void incrementRTOCount(){
		rtoCount++;
	}
	
	public void resetRTOCount(){
		rtoCount = 0;
	}
	
	public int getRTOCount(){
		return rtoCount;
	}
	
	//for calculating average queuing delay
	public void setBufferStartTime(long bufferStartTime){
		this.bufferStartTime = bufferStartTime;
	}
	
	public void setBufferEndTime(long bufferEndTime){
		this.bufferEndTime = bufferEndTime;
	}
	
	public long getBufferTime(){
		return bufferEndTime - bufferStartTime;
	}
	
	@Override
	public String toString(){
		return "Seg-" + super.toString();
	}

}
