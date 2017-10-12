package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

public class WorkerThread extends Thread {
	String rootDir;
	int port;
	BlockingQueue queue;
	boolean isAlive;
	Socket s;
	ThreadPool tp;
	String filename;
	Container cntr;
	Logger logger;
	public WorkerThread(String rootDir, int port, BlockingQueue queue, Container cntr,
			ThreadPool tp,Logger l) {
		// TODO Auto-generated constructor stub
		this.rootDir = rootDir;
		this.port = port;
		this.tp = tp;
		this.queue = queue;
		isAlive = true;
		this.cntr=cntr;
		logger=l;
	}

	public void run() {
		// TODO Auto-generated method stub
		// System.out.println(Thread.currentThread().getId());
		while (isAlive) {
			try {
				s = null;
				if (!this.isInterrupted())
					s = queue.dequeue();
				//System.out.println("\nDequeuing request\n");
				RequestHandling httpreq = new RequestHandling(s, rootDir, port,
						tp, isAlive, cntr, this,logger);
				//System.out.println("\n Calling process request\n");
				logger.info("Calling process Request");
				try {
					httpreq.processRequest();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
					logger.warn(e1);
				}
				try {
					s.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					logger.warn(e);
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//logger.warn(e);
			} finally {
				if (s != null && s.isClosed() == false) {
					try {
						s.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						logger.warn(e);
					}
				}
			}
		}

	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void shutDowncalled() {
		isAlive = false;
	}

	public boolean threadStatus() {
		return isAlive;
	}
}
