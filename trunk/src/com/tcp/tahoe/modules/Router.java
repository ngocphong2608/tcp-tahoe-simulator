package com.tcp.tahoe.modules;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.tcp.tahoe.data.impl.Segment;

public class Router {

	private Queue<List<Segment>> routerQueue;
	private long bufferSize; // The router's buffer size in bytes
	private long freeSpace; // The router's free space in bytes

	public Router(long bufferSize) {
		// initializing the routerQueue
		routerQueue = new LinkedList<List<Segment>>();

		// initializing the bufferSize and freeSpace parameters
		this.bufferSize = bufferSize;
		freeSpace = this.bufferSize;
	}

	public void enqueue(List<Segment> segments) {
		// retrieving the MSS of the segment in bytes
		long segmentMss = segments.iterator().next().getMss();

		for (Segment segment : segments) {
			// checking for freeSpace in router and if there is no free space
			// then we remove the segment from the List otherwise we
			// decrement the free space
			if (freeSpace < segmentMss)
				segments.remove(segment);
			else
				freeSpace = freeSpace - segmentMss;
		}

		// if the segment size is not empty then we add it to the router queue
		if (segments.size() != 0)
			routerQueue.add(segments);
	}

	public List<Segment> dequeue() {
		// if the router queue is empty then we return null
		if (routerQueue.isEmpty())
			return null;
		else {
			// retrieve the next in line segments from the routerQueue
			List<Segment> dequeueSegments = routerQueue.remove();
			long segmentMss = dequeueSegments.iterator().next().getMss();

			// increment the free space by the amount of segments take off
			freeSpace = freeSpace + dequeueSegments.size() * segmentMss;

			// return the segments that were dequeued
			return dequeueSegments;
		}
	}

	public boolean hasSegmentsToSend() {
		// if the router queue is empty then return false otherwise return true
		if (routerQueue.isEmpty())
			return false;
		else
			return true;
	}
	
	public Queue<List<Segment>> getSegmentsInRouterBuffer(){
		return routerQueue;
	}
}
