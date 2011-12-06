package com.tcp.tahoe.data.impl;
import com.tcp.tahoe.data.Packet;

public class Segment extends Packet {
	private long mss;
	private int rtoCount;

	public Segment(int id, long mss) {
		super(id);
		this.mss = mss;
		this.rtoCount = 0;
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
	
	@Override
	public String toString(){
		return "Seg-" + super.toString();
	}

}
