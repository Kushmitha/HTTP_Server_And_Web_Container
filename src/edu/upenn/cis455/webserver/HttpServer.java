package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import org.apache.log4j.Logger;


public class HttpServer {
	static int port;
	static String rootDir;
	static ServerSocket serSock;
	static String webxmlPath;
	static final Logger logger = Logger.getLogger(HttpServer.class);
	public static HashMap<String,Session> Sessions;
	public static void main(String args[]) throws IOException {
		/* your code here */
		if (args.length < 3) {
			System.out.println("*** Author: Kushmitha Unnikumar (kushm)");
			return;
		}
		port = Integer.parseInt(args[0]);
		rootDir = args[1];
		webxmlPath=args[2];
		if(rootDir.endsWith("/"))//remove / , if rootdir ends with it
			rootDir=rootDir.substring(0,rootDir.length()-1);	
		logger.info("Root dir : "+rootDir);
		Container cntr = new Container(webxmlPath,logger);
		Sessions = new HashMap<String,Session>();
		ThreadPool tp = new ThreadPool(rootDir, port,cntr,logger);
		try {
			serSock = new ServerSocket(port, 100);
			logger.info("Server on");
			Socket s = null;
			while (tp.serverOn == 1) {
				if (!serSock.isClosed()) {
					s = serSock.accept();
					tp.enqueueRequest(s);
				}

			}
			if (s != null) {
				s.close();
			}

		} catch (SocketException s) {
			for (WorkerThread t : tp.workers)
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logger.warn(e);
				}
		} catch (InterruptedException e) {
			logger.warn(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//logger.warn(e);
		}
	}

	public static void stopServer() {
		try {
			serSock.close();
			System.exit(0);
		} catch (IOException e) {
			logger.warn(e);
		}
	}

}
