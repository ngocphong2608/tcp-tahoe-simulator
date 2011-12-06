package com.tcp.tahoe.data.impl;

public class SenderVariableLongData {
	private long time;
	private long data;
	
	public SenderVariableLongData(long time, long data){
		this.time = time;
		this.data = data;
	}
	
	public long getTime(){
		return time;
	}
	
	public long getData(){
		return data;
	}
	
	@Override
	public String toString(){
		return "Clk:" + time + "|Value:" + data;
	}
}
