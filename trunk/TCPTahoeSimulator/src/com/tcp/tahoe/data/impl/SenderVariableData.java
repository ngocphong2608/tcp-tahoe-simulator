package com.tcp.tahoe.data.impl;

public class SenderVariableData {
	private long time;
	private long congWin;
	
	public SenderVariableData(long time, long congWin){
		this.time = time;
		this.congWin = congWin;
	}
	
	public long getTme(){
		return time;
	}
	
	public long getCongWin(){
		return congWin;
	}
	
	@Override
	public String toString(){
		return "Clk:" + time + "|Value:" + congWin;
	}
}
