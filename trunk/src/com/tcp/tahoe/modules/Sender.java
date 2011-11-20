package com.tcp.tahoe.modules;

import java.util.List;

import com.tcp.tahoe.data.impl.AckPacket;
import com.tcp.tahoe.data.impl.Segment;

public class Sender {

	public Sender(long mss, long rtt, int numberOfPackets) {
		// TODO Auto-generated constructor stub
	}

	public boolean isDoneSending() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasSomethingtoSend() {
		// TODO Auto-generated method stub
		return false;
	}

	public List<Segment> sendSegments() {
		// TODO Auto-generated method stub
		return null;
	}

	public void recieveAck(AckPacket ack) {
		// TODO Auto-generated method stub
		
	}

	public void incrementClk() {
		// TODO Auto-generated method stub
		
	}


}
