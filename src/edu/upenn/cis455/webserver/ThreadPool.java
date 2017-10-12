package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class ThreadPool {

	ArrayList<WorkerThread> workers;
	int num_workers;
	BlockingQueue queue;
	int serverOn;
	Logger logger;
	Container cntr;
	public ThreadPool(String rootDir, int port, Container c,Logger l) {
		// TODO Auto-generated constructor stub
		// System.out.println("\nIn threadpool\n");
		
		workers = new ArrayList<WorkerThread>();
		num_workers = 30;
		serverOn = 1;
		logger=l;
		queue = new BlockingQueue();
		cntr=c;
		// start all threads
		logger.info("In threadpool");
		for (int i = 0; i < num_workers; i++) {
			WorkerThread w = new WorkerThread(rootDir, port, queue, cntr, this,logger);
			workers.add(w);
			w.start();
		}
	}

	public void enqueueRequest(Socket s) throws InterruptedException {
		queue.enqueue(s);
	}

	public void shutDown() throws IOException {
		// stop all threads
		for (WorkerThread t : workers) {
			t.shutDowncalled();
			t.interrupt();
			if (t.s != null && t.s.isConnected())
				t.s.close();
		}
		//stop all servlets
		cntr.shutDown();
		serverOn = 0;
		HttpServer.stopServer();
	}
}
