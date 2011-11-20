package com.tcp.tahoe;

import java.util.List;

import com.tcp.tahoe.data.impl.Segment;
import com.tcp.tahoe.modules.*;

public class Simulate {
	private static final int NUMBER_OF_PACKETS = 10;
	
	//in bytes
	private static final long RCV_WINDOW = 1048576;
	private static final long MSS = 1024;
	private static final long ROUTER_BUFFER_SIZE = 10000 * MSS;
	
	//in ms
	private static final long RTT = 500;
	private static final long CLOCK_LIMIT = 1000;
	private static long clock = 1;
	
	
	//in MB/s
	private static final int SENDER_TO_ROUTER_LINK_SPEED = 10;
	private static final int ROUTER_TO_RECEIVER_LINK_SPEED = 1;
	
	public static void main(String[] args) {
		
		//Initialize Modules
		Sender sender = new Sender(MSS, RTT, NUMBER_OF_PACKETS);
		Receiver receiver = new Receiver(RCV_WINDOW);
		Router router = new Router(ROUTER_BUFFER_SIZE);
		Link senderToRouter = new Link(SENDER_TO_ROUTER_LINK_SPEED);
		Link routerToReceiver = new Link(ROUTER_TO_RECEIVER_LINK_SPEED);
		
		while(!sender.isDoneSending()){
			if(!senderToRouter.isBusy()){
				if(!senderToRouter.isEmpty()){
					router.enqueue(senderToRouter.getData());
					senderToRouter.freeLink();
				}
				if(sender.hasSomethingtoSend())
					senderToRouter.addData(sender.sendSegments());
					
			}
			
			if(!routerToReceiver.isBusy()){
				if(!routerToReceiver.isEmpty()){
					receiver.recieveSegment(routerToReceiver.getData());
					sender.recieveAck(receiver.getAck());
					routerToReceiver.freeLink();
				}
				if(router.hasSegmentsToSend())
					routerToReceiver.addData(router.dequeue());
			}
		
		//Start Printout
		System.out.printf("Clock Cycle %10d(ms)\n", clock);
		
		// TODO Print out Sender Information
		
		//Printing out segments on the first link
		System.out.print("Segments on 1st Link: {");
		for(Segment segment : senderToRouter.getData()){
			System.out.print(segment.getId() + ",");
		}
		System.out.print("}\n");
		
		//Printing out segments in the router buffer
		System.out.print("Segments in Router: {");	
		for(List<Segment> segments : router.getSegmentsInRouterBuffer()){
			System.out.print("[");
			for(Segment segment : segments){
				System.out.print(segment.getId() + ",");
			}
			System.out.print("],");
		}
		System.out.print("}\n");
		
		//Printing out segments on the second link
		System.out.print("Segments on 2nd Link: {");
		for(Segment segment : routerToReceiver.getData()){
			System.out.print(segment.getId() + ",");
		}
		System.out.print("}\n");
		//End Printout
		
		// TODO Print out Receiver Information
		
		senderToRouter.incrementClk();
		routerToReceiver.incrementClk();
		sender.incrementClk();
		clock++;
		
		if(clock >= CLOCK_LIMIT)
			break;
		}
	}
}
