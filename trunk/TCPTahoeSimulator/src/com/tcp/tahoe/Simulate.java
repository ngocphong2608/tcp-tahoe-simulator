package com.tcp.tahoe;

import java.util.Collection;
import com.tcp.tahoe.data.impl.AckPacket;
import com.tcp.tahoe.data.impl.Segment;
import com.tcp.tahoe.data.impl.SenderVariableData;
import com.tcp.tahoe.modules.*;
import java.io.IOException;
import java.io.File;
import jxl.*; 
import jxl.read.biff.BiffException;
import jxl.write.*; 
import jxl.write.Number;

public class Simulate {
	
	
	
	private static final int NUMBER_OF_PACKETS = 10;
	
	//in bytes
	private static final long RCV_WINDOW = 1048576;
	private static final long MSS = 1024;
	private static final long ROUTER_BUFFER_SIZE = 10000 * MSS;
	
	//in ms
	private static final long RTT = 500;
	private static long clock = 1;
	
	
	//in MB/s
	private static final int SENDER_TO_ROUTER_LINK_SPEED = 10;
	private static final int ROUTER_TO_RECEIVER_LINK_SPEED = 1;
	
	public static void main(String[] args) throws IOException {
		
		//Initialize Modules
		Sender sender = new Sender(MSS, RTT, NUMBER_OF_PACKETS, RCV_WINDOW);
		Receiver receiver = new Receiver(RCV_WINDOW);
		Router router = new Router(ROUTER_BUFFER_SIZE);
		Link senderToRouter = new Link(SENDER_TO_ROUTER_LINK_SPEED);
		Link routerToReceiver = new Link(ROUTER_TO_RECEIVER_LINK_SPEED);
		
		
		//Starting the Simulation
		System.out.println("TCP Tahoe Siumlation: ");
		while(!sender.isDoneSending()){
			
			//RTO timeout 
			if(sender.isRTODone()){
				sender.sendSegments(clock);				
			}
			
			if(!senderToRouter.isBusy()){
				if(!senderToRouter.isEmpty()){
					
					//Start Printout
					System.out.println("\nClock = " + clock + "ms");
					System.out.println("Segment enqueued onto the Router:" + senderToRouter.getData());
					//End Printout
					
					router.enqueue(senderToRouter.getData());
					senderToRouter.freeLink();
				}	
				
				if(sender.hasSomethingtoSend()) {
					senderToRouter.freeLink();			
					Segment segmentToSend = sender.sendNextSegment();
					
					//TODO sender sending segments
					senderToRouter.addData(segmentToSend);	
					
					//Start Printout
					System.out.println("\nClock = " + clock + "ms");
					System.out.println("Segment added on Link1:" + segmentToSend);
					//End Printout
				}
			}		
			
			if(!routerToReceiver.isBusy()){
				if(!routerToReceiver.isEmpty()){
					Segment recievedSegment = routerToReceiver.getData();
					receiver.recieveSegment(recievedSegment);
					
					AckPacket ackPacket = receiver.getAck();
					sender.recieveAck(ackPacket);
					routerToReceiver.freeLink();
					
					//Start Printout
					System.out.println("\nClock = " + clock + "ms");
					System.out.println("Receiver Recieved :" + recievedSegment);
					System.out.println("Receiver Sends :" + ackPacket.toString());
					//End Printout
				}
				if(router.hasSegmentsToSend()){
					Segment segmentToSend = router.dequeue();
					routerToReceiver.addData(segmentToSend);
					
					//Start Printout
					System.out.println("\nClock = " + clock + "ms");
					System.out.println("Segment dequeued onto Link2:" + segmentToSend);
					//End Printout
				}
			}
				
		senderToRouter.incrementClk();
		routerToReceiver.incrementClk();
		sender.incrementClk();
		clock++;
		
		}
		
		//Graphing Part of the Application
		Collection<SenderVariableData> congWinCollection = sender.getCongWindowData();
		Collection<SenderVariableData> effectiveWinCollection = sender.getEffectWinData();
		Collection<SenderVariableData> flightSizeCollection = sender.getFlightSizeData();
		Collection<SenderVariableData> ssThreshCollection = sender.getSSThreshData();
		

		
		System.out.println("\nVariable Data");
		System.out.println("Congestion Window Data: " + congWinCollection.toString());
		System.out.println("Effective Window Data:  " + effectiveWinCollection.toString());
		System.out.println("Flight Size Data:       " + flightSizeCollection.toString());
		System.out.println("SS Threshold Data:      " + ssThreshCollection.toString());
		
		
		
		try {
			File inputWorkbook = new File("C:/Users/Administrator/workspace/TCPTahoeSimulator/src/com/tcp/tahoe/graph_template.xls");
			Workbook w = Workbook.getWorkbook(inputWorkbook);
			WritableWorkbook copy = Workbook.createWorkbook(new File("C:/Users/Administrator/workspace/TCPTahoeSimulator/src/com/tcp/tahoe/output.xls"), w); 
			WritableSheet sheet0 = copy.getSheet(0);   //congestion win
			WritableSheet sheet1 = copy.getSheet(1);  //effective win
			WritableSheet sheet2 = copy.getSheet(2);  //flight
			WritableSheet sheet3 = copy.getSheet(3); //ssThresh
			
			
			int row=1; 
			for(SenderVariableData dataObj : congWinCollection){
				Number number0,number1;
				number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
				number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
				sheet0.addCell(number0);
				sheet0.addCell(number1);
			
				row++;
			}		
			row=1;
			for(SenderVariableData dataObj : effectiveWinCollection){
				Number number0,number1;
				number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
				number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
				sheet1.addCell(number0);
				sheet1.addCell(number1);
			
				row++;
			}
			row=1;
			for(SenderVariableData dataObj : flightSizeCollection){
				Number number0,number1;
				number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
				number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
				sheet2.addCell(number0);
				sheet2.addCell(number1);
			
				row++;
			}
			row=1;
			for(SenderVariableData dataObj : ssThreshCollection){
				Number number0,number1;
				number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
				number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
				sheet3.addCell(number0);
				sheet3.addCell(number1);
			
				row++;
			}	
			
						
			
			//write and close output.xls
			copy.write();
			copy.close(); 
			
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
}
