package com.tcp.tahoe.modules;

import java.util.LinkedList;
import java.util.Queue;

import com.tcp.tahoe.data.impl.Segment;

public class Router {

	private Queue<Segment> routerQueue;
	private long bufferSize; // The router's buffer size in bytes
	private long freeSpace; // The router's free space in bytes
	private boolean printOut;

	public Router(long bufferSize, boolean printOut) {
		// initializing the routerQueue
		routerQueue = new LinkedList<Segment>();

		// initializing the bufferSize and freeSpace parameters
		this.bufferSize = bufferSize;
		freeSpace = this.bufferSize;
		
		this.printOut = printOut;
	}

	public void enqueue(Segment segment) {
		// retrieving the MSS of the segment in bytes
		long segmentMss = segment.getMss();

		// checking for freeSpace in router and if there is no free space
		// then we remove the segment from the List otherwise we
		// decrement the free space
		if (freeSpace >= segmentMss){
			freeSpace = freeSpace - segmentMss;
			routerQueue.add(segment);
			if(printOut)
				System.out.println("Segment enqueued onto the Router:" + segment);
		} else {
			if(printOut)
				System.out.println("Segment dropped b/c Router Buffer Overflow:" + segment);
		}
		
		if(printOut)
			System.out.println("Router Queue: " + routerQueue.toString());
	}

	public Segment dequeue() {
		// if the router queue is empty then we return null
		if (routerQueue.isEmpty())
			return null;
		else {
			// retrieve the next in line segments from the routerQueue
			Segment dequeueSegment = routerQueue.remove();
			long segmentMss = dequeueSegment.getMss();

			// increment the free space by the amount of segments take off
			freeSpace = freeSpace + segmentMss;

			// return the segments that were dequeued
			return dequeueSegment;
		}
	}

	public boolean hasSegmentsToSend() {
		// if the router queue is empty then return false otherwise return true
		if (routerQueue.isEmpty())
			return false;
		else
			return true;
	}
	
	public Queue<Segment> getSegmentsInRouterBuffer(){
		return routerQueue;
	}
}
