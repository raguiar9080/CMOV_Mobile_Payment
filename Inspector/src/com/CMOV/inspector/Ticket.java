package com.CMOV.inspector;

import java.io.Serializable;
import java.util.Date;

public class Ticket implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int ticketID=-1;
	private int  clientID=-1;
	private String validTime=null;
	
	Ticket(){}
	
	Ticket(int tid, int cid, String date)
	{
		ticketID=tid;
		clientID=cid;			
		validTime=date;
	}

	public int getTicketID()
	{
		return ticketID;
	}

	public void setTicketID(int ticketID) 
	{
		this.ticketID = ticketID;
	}

	public int getClientID() 
	{
		return clientID;
	}

	public void setClientID(int clientID) 
	{
		this.clientID = clientID;
	}

	public String getValidTime() 
	{
		return validTime;
	}

	public void setValidTime(String validTime) 
	{
		this.validTime = validTime;
	}	

}
