package com.tcp.tahoe.data;

import java.util.Comparator;

public class PacketComparator implements Comparator<Packet>{

	@Override
	public int compare(Packet o1, Packet o2) {
		int id1 = o1.getId();        
        int id2 = o2.getId();
       
        if(id1 > id2)
        	return 1;
        else if(id2 > id1)
        	return -1;
        else
        	return 0;
	}

}
