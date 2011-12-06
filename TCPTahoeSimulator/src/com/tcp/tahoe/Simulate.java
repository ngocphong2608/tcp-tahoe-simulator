package com.tcp.tahoe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.tcp.tahoe.data.impl.AckPacket;
import com.tcp.tahoe.data.impl.Segment;
import com.tcp.tahoe.data.impl.SenderVariableDoubleData;
import com.tcp.tahoe.data.impl.SenderVariableLongData;
import com.tcp.tahoe.modules.*;

import java.lang.Boolean;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.Number;

public class Simulate {
	private static int NUMBER_OF_PACKETS;
	private static long RCV_WINDOW;
	private static long MSS;
	private static long ROUTER_BUFFER_SIZE;
	private static long RTT;
	private static int SENDER_TO_ROUTER_LINK_SPEED;
	private static int ROUTER_TO_RECEIVER_LINK_SPEED;
	private static boolean PRINTOUT;

	private static long clock = 1;

	private static int percentComplete = 0;
	private static int prevPercentComplete = -1;

	public static void main(String[] args) throws IOException {
		try{
			loadProperties();
			
			//Check for infinite loop potential
			double varTemp_1 = ((double)MSS) / Math.pow(2, 20) * (((double) 1000000) / SENDER_TO_ROUTER_LINK_SPEED);
			if(varTemp_1 >= RTT){
				System.out.println("Infinite Loop Potential");
				System.out.println("RTT: " + RTT);
				System.out.println("uS for 1 segment to traverse the first link: " + varTemp_1);
				
			} else {
				//Initialize Modules
				Sender sender = new Sender(MSS, RTT, NUMBER_OF_PACKETS, RCV_WINDOW);
				Receiver receiver = new Receiver(RCV_WINDOW);
				Router router = new Router(ROUTER_BUFFER_SIZE, PRINTOUT);
				Link senderToRouter = new Link(SENDER_TO_ROUTER_LINK_SPEED);
				Link routerToReceiver = new Link(ROUTER_TO_RECEIVER_LINK_SPEED);
				
				//adding data for 
				List<SenderVariableLongData> packetsRouterBuffer = new ArrayList<SenderVariableLongData>();
				
				System.out.println("TCP Tahoe Siumlation: ");
				
				//Starting the Simulation
				while(!sender.isDoneSending()){
					
					//RTO timeout
					boolean RTO_UP = sender.isRTODone(); 
					if(RTO_UP){
						sender.sendSegments(clock,Math.min(SENDER_TO_ROUTER_LINK_SPEED, ROUTER_TO_RECEIVER_LINK_SPEED),PRINTOUT);		
						
						//adding data
						packetsRouterBuffer.add(new SenderVariableLongData(clock,router.getSegmentsInRouterBuffer().size()));
					}
					
					if(!senderToRouter.isBusy() || RTO_UP){
						if(!senderToRouter.isEmpty() & !RTO_UP){
							
							if(PRINTOUT)
								System.out.println("\nClock = " + clock + "us");
													
							router.enqueue(senderToRouter.getData());
							senderToRouter.freeLink();
						}	
						
						if(sender.hasSomethingtoSend()) {		
							Segment segmentToSend = sender.sendNextSegment();
							
							senderToRouter.freeLink();	
							senderToRouter.addData(segmentToSend);	
							
							if(PRINTOUT){
								System.out.println("\nClock = " + clock + "us");
								System.out.println("Segment added on Link1: " + segmentToSend);
								System.out.println("Flight Size: " + sender.getFlightSize());
							} else {
								double tempPercent = ((double)segmentToSend.getId())/(NUMBER_OF_PACKETS-1);
								percentComplete = (int) (tempPercent * 100);
								if(prevPercentComplete != percentComplete){
									System.out.println("Percent Complete: " + percentComplete);
									prevPercentComplete = percentComplete;
								}
							}
						}
					}		
					
					if(!routerToReceiver.isBusy()){
						if(!routerToReceiver.isEmpty()){
							Segment recievedSegment = routerToReceiver.getData();
							receiver.recieveSegment(recievedSegment);
							routerToReceiver.freeLink();
							
							
							AckPacket ackPacket = receiver.getAck();
							sender.recieveAck(ackPacket);
							
							
							if(PRINTOUT){
								System.out.println("\nClock = " + clock + "us");
								System.out.println("Receiver Recieved :" + recievedSegment);
								System.out.println("Receiver Sends :" + ackPacket.toString());
								System.out.println("Receiver Buffer: " + receiver.getBufferedSegments().toString());
							}
						}
						if(router.hasSegmentsToSend()){
							Segment segmentToSend = router.dequeue();
							
							routerToReceiver.freeLink();
							routerToReceiver.addData(segmentToSend);
							
							if(PRINTOUT){
								System.out.println("\nClock = " + clock + "us");
								System.out.println("Segment dequeued onto Link2:" + segmentToSend);
							}
						}
					}
						
				senderToRouter.incrementClk();
				routerToReceiver.incrementClk();
				sender.incrementClk();
				clock++;
				
				}
				
				//Graphing Part of the Application
				Collection<SenderVariableLongData> congWinCollection = sender.getCongWindowData();
				Collection<SenderVariableLongData> effectiveWinCollection = sender.getEffectWinData();
				Collection<SenderVariableLongData> flightSizeCollection = sender.getFlightSizeData();
				Collection<SenderVariableLongData> ssThreshCollection = sender.getSSThreshData();
				Collection<SenderVariableDoubleData> senderUtilityCollection = sender.getSenderUtilityData();
				//graph packetsRouterBuffer
				
		
				if(PRINTOUT){
					System.out.println("\nVariable Data");
					System.out.println("Congestion Window Data: " + congWinCollection.toString());
					System.out.println("Effective Window Data:  " + effectiveWinCollection.toString());
					System.out.println("Flight Size Data:       " + flightSizeCollection.toString());
					System.out.println("SS Threshold Data:      " + ssThreshCollection.toString());
					System.out.println("Buffer Data:      		" + packetsRouterBuffer.toString());
					System.out.println("Sender Utility Data:	" + senderUtilityCollection.toString());
				}
				
				
				
				try {
					File inputWorkbook = new File("graph_template.xls");
					Workbook w = Workbook.getWorkbook(inputWorkbook);
					WritableWorkbook copy = Workbook.createWorkbook(new File("output.xls"), w); 
					WritableSheet sheet0 = copy.getSheet(0);   //congestion win
					WritableSheet sheet1 = copy.getSheet(1);  //effective win
					WritableSheet sheet2 = copy.getSheet(2);  //flight
					WritableSheet sheet3 = copy.getSheet(3); //ssThresh
					WritableSheet sheet5 = copy.getSheet(5);  //packetrouterBuff
					WritableSheet sheet6 = copy.getSheet(6);   //senderUtil
					
					int row=1; 
					for(SenderVariableLongData dataObj : congWinCollection){
						Number number0,number1;
						number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
						number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
						sheet0.addCell(number0);
						sheet0.addCell(number1);
					
						row++;
					}		
					row=1;
					for(SenderVariableLongData dataObj : effectiveWinCollection){
						Number number0,number1;
						number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
						number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
						sheet1.addCell(number0);
						sheet1.addCell(number1);
					
						row++;
					}
					row=1;
					for(SenderVariableLongData dataObj : flightSizeCollection){
						Number number0,number1;
						number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
						number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
						sheet2.addCell(number0);
						sheet2.addCell(number1);
					
						row++;
					}
					row=1;
					for(SenderVariableLongData dataObj : ssThreshCollection){
						Number number0,number1;
						number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
						number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
						sheet3.addCell(number0);
						sheet3.addCell(number1);
					
						row++;
					}	
					row=1;
					for(SenderVariableLongData dataObj : packetsRouterBuffer){
						Number number0,number1;
						number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
						number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
						sheet5.addCell(number0);
						sheet5.addCell(number1);
					
						row++;
					}	
					row=1;
					for(SenderVariableDoubleData dataObj : senderUtilityCollection){
						Number number0,number1;
						number0 = new Number(0,row,dataObj.getTime()); //Col A (time)
						number1 = new Number(1,row,dataObj.getData()); //Col B (Data)
						sheet6.addCell(number0);
						sheet6.addCell(number1);
					
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
		} catch (Exception e){
			System.out.println("Error Loading the Properties File");
			System.out.println(e.toString());
		} finally {
			System.out.println("\nDone Simulating");
			System.out.println("Press <Enter> to exit ...");
			System.in.read();
		}
		
	}

	private static void loadProperties() throws FileNotFoundException,
			IOException {
		Properties connectionProps = new Properties();
		connectionProps
				.load(new FileInputStream("TCPTahoeSimulator.properties"));

		NUMBER_OF_PACKETS = Integer.parseInt(connectionProps
				.getProperty("NUMBER_OF_PACKETS"));
		MSS = Long.parseLong(connectionProps.getProperty("MSS"));
		RCV_WINDOW = Long.parseLong(connectionProps.getProperty("RCV_WINDOW"));
		ROUTER_BUFFER_SIZE = Long.parseLong(connectionProps
				.getProperty("ROUTER_BUFFER_SIZE"));
		SENDER_TO_ROUTER_LINK_SPEED = Integer.parseInt(connectionProps
				.getProperty("SENDER_TO_ROUTER_LINK_SPEED"));
		ROUTER_TO_RECEIVER_LINK_SPEED = Integer.parseInt(connectionProps
				.getProperty("ROUTER_TO_RECEIVER_LINK_SPEED"));
		RTT = Long.parseLong(connectionProps.getProperty("RTT"));
		PRINTOUT = Boolean
				.parseBoolean(connectionProps.getProperty("PRINTOUT"));
	}
}
