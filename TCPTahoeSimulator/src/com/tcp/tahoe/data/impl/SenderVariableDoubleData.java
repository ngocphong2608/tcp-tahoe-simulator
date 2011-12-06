package com.tcp.tahoe.data.impl;

public class SenderVariableDoubleData {
	private long time;
	private double data;
	
	public SenderVariableDoubleData(long time, double data){
		this.time = time;
		this.data = data;
	}
	
	public long getTime(){
		return time;
	}
	
	public double getData(){
		return data;
	}
	
	@Override
	public String toString(){
		return "Clk:" + time + "|Value:" + data;
	}
}
