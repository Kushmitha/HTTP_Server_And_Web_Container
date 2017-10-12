package edu.upenn.cis455.webserver;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

public class BlockingQueue {
	Queue <Socket> BQ;
	int MAX_QUEUE_SIZE=200;
	static final Logger logger = Logger.getLogger(BlockingQueue.class);
	public BlockingQueue() {
		// TODO Auto-generated constructor stub
		BQ=new LinkedList<Socket>();
	}
	public synchronized void enqueue(Socket s) throws InterruptedException
	{
		while(BQ.size()==MAX_QUEUE_SIZE)
			wait();
		if(BQ.size()==0)
			notifyAll();
		BQ.add(s);
			
	}
	
	public synchronized Socket dequeue() throws InterruptedException
	{
		while(BQ.size()==0)
			wait();
		if(BQ.size()==MAX_QUEUE_SIZE)
			notifyAll();
		
		return BQ.remove();
	}
		

}
