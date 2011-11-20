package com.tcp.tahoe.data.impl;
import com.tcp.tahoe.data.Packet;

public class Segment extends Packet {
	private long mss;

	public Segment(int id, long mss) {
		super(id);
		this.mss = mss;
	}
	
	public long getMss(){
		return mss;
	}

}
