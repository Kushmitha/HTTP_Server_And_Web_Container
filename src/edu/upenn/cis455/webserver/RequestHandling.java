package edu.upenn.cis455.webserver;


import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class RequestHandling {

	Socket s;
	String method;
	String filename;
	String httpVersion;
	String request;
	String rootDir;
	String controlPanelList;
	ThreadPool tp;
	String dirList;
	HashMap<String, String> reqParams;
	boolean isAlive;
	WorkerThread wt;
	int port;
	SimpleDateFormat[] sdf = new SimpleDateFormat[3];
	Container container;
	Logger logger;

	public RequestHandling(Socket s, String rootDir, int port, ThreadPool tp,
			boolean isAlive, Container cntr, WorkerThread workerThread, Logger l) {
		// TODO Auto-generated constructor stub
		// initialize the class parameters
		this.s = s;
		this.method = null;
		this.rootDir = rootDir;
		this.request = "";
		reqParams = new HashMap<String, String>();
		reqParams.put("ServerPort", String.valueOf(port));
		this.port = port;
		this.filename = null;
		this.controlPanelList = null;
		this.dirList = null;
		this.tp = tp;
		this.isAlive = isAlive;
		this.httpVersion = null;
		this.wt = workerThread;
		logger = l;
		sdf[0] = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss z");
		sdf[1] = new SimpleDateFormat("EEEE, d-MMM-yy hh:mm:ss z");
		sdf[2] = new SimpleDateFormat("EEE MMM dd hh:mm:ss yy");
		sdf[0].setTimeZone(TimeZone.getTimeZone("GMT"));
		sdf[1].setTimeZone(TimeZone.getTimeZone("GMT"));
		sdf[2].setTimeZone(TimeZone.getTimeZone("GMT"));
		container = cntr;
		// System.out.println("\nReq handling ctr\n");
	}

	public void processRequest() throws IOException// Process the request
	{
		// TODO Auto-generated method stub
		// System.out.println("\nProcessing\n");
		try {
			while (isAlive) {
				if (parseRequest())// returns a true value if the request is
									// valid and parsable
				{
					// System.out.println("if2");
					if (filename.equals(null)
							|| filename.equals("/favicon.ico")) // check for
																// NULL or
																// favicon
																// requests
						return;

					if (servletRequest()) {
						// System.out.println("Calling servlet request");
						logger.info("Calling servletRequest()");
						return;
					}

					if (filename.substring(0).equals("/control"))// handle
																	// control
																	// panel
																	// request
					{
						displayControlPanel();
						return;
					} else if (filename.substring(0).equals("/shutdown")
							|| filename.substring(0).equals("/shutdown?"))// handle
																			// shutdown
																			// request
					{
						shutDown();
						return;
					} 
					else if(filename.equals("/log")){
						displayLog();
						return;
					}
					else// return file to client
					{
						getFile();
						return;
					}
				} else
					return;
			}
		} catch (IOException E) {
			send500error();
			logger.info("500 error");
		}
	}

	private void displayLog() throws IOException
	{
		//File f=new File("/home/cis455/workspace/HW1MS2/logging.log");
		File f=new File("./logging.log");
		long cl=0;
		BufferedReader br=new BufferedReader(new FileReader("./logging.log"));
		String openingTag = "<html><h1>LOG</h1>";
		String closingTag="</html>";
		String line=null;
		if (wt.threadStatus()) {
			s.getOutputStream().write(
					(httpVersion + " 200 OK\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Type: text/html\r\n").getBytes());
			cl=openingTag.length()+closingTag.length()+f.length();
			s.getOutputStream().write(
					("Content-Length: " + cl + "\r\n")
							.getBytes());
			s.getOutputStream().write(
					("Date: " + sdf[0].format(new Date()) + "\r\n").getBytes());
			s.getOutputStream().write(("Connection: close\r\n\r\n").getBytes());
			s.getOutputStream().write(openingTag.getBytes());
			line=br.readLine();
			while(line!=null)
			{
				s.getOutputStream().write(line.getBytes());
				s.getOutputStream().write(("</br>").getBytes());
				line=br.readLine();
			}
			s.getOutputStream().write(closingTag.getBytes());
			s.getOutputStream().flush();
		}	
	}
	
	private boolean servletRequest() {
		// TODO Auto-generated method stub
		try {
			return container.servletRequestHandling(filename, s, reqParams);

		} catch (Exception e) {
			logger.warn(e);
			// e.printStackTrace();
			return false;
		}
	}

	private void shutDown() throws IOException {// process the shutdown request
		// TODO Auto-generated method stub
		// System.out.println("Shutdown called!");
		String shutdown = "<html>Server Shutdown!</html>";
		if (wt.threadStatus()) {
			s.getOutputStream().write((httpVersion + " 200 OK\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Type: text/html\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Length: " + shutdown.length() + "\r\n")
							.getBytes());
			s.getOutputStream().write(
					("Date: " + sdf[0].format(new Date()) + "\r\n").getBytes());
			s.getOutputStream().write(("Connection: close\r\n\r\n").getBytes());
			s.getOutputStream().write(shutdown.getBytes());
			s.getOutputStream().flush();
			logger.info("Server shutdown!");
			tp.shutDown();
		}

	}

	private void displayControlPanel() throws IOException {// display
		// controlPanel
		// TODO Auto-generated method stub
		if (wt.threadStatus()) {
			s.getOutputStream().write((httpVersion + " 200 OK\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Type: text/html\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Length: " + getControlPanelList() + "\r\n")
							.getBytes());
			s.getOutputStream().write(
					("Date: " + sdf[0].format(new Date()) + "\r\n").getBytes());
			s.getOutputStream().write(("Connection: close\r\n\r\n").getBytes());
			s.getOutputStream().write(controlPanelList.getBytes());
		}
	}

	private int getControlPanelList() {// returns all the threads and the
	// requests they are handling as a
	// string
	// TODO Auto-generated method stub

		if (method.equals("GET")) {
			controlPanelList = "<html><body><h1>KUSHMITHA UNNIKUMAR (kushm)</h1><h2>Thread ID and its State</h2></br>";
			for (WorkerThread t : tp.workers) {
				if (t.getState().equals(Thread.State.RUNNABLE))
					controlPanelList += (t.getId() + " " + t.filename + "</br>");
				else
					controlPanelList += (t.getId() + " "
							+ t.getState().toString() + "</br>");
			}
			controlPanelList += "<form action=\"shutdown\" method=\"get\"><button type=\"submit\">SHUTDOWN</button></form></br><a href=\"log\">Click here for log</a></body></html>";
			return controlPanelList.length();
		} else
			return 0;
	}

	private void getFile() {// Send the requested file to client if the file
							// exists in the specified path and authorized
							// directory
		// TODO Auto-generated method stub
		// System.out.println("In getfile()");
		boolean isAbsolute = false;
		String reqFile = null;
		try {

			if (filename.startsWith("http://"))
				if (filename.contains("localhost:" + port)) {
					isAbsolute = true;
					int index = filename.indexOf(Integer.toString(port))
							+ Integer.toString(port).length();
					//System.out.println(index);
					reqFile = filename.substring(index);
					//System.out.println("\nRequested file2 : "+reqFile);
					reqFile=rootDir+reqFile;
				}
			if (!isAbsolute) {
				if(filename.startsWith("/")==false)
					filename="/"+filename;
				reqFile = rootDir + filename;
				//System.out.println("\nRequested file1 : "+reqFile);
			}
			String parsedURL = parseURL(reqFile);
			if (parsedURL.contains(rootDir)) {
				// proceed to send file
				File f = new File(parsedURL);
				if (f.isDirectory())
					listFilenames(parsedURL);
				else if (f.exists()) {
					String contentType = null;
					String type = null;
					type = filename.substring(filename.lastIndexOf("."));
					// System.out.println("\nType : "+type);
					// get Type of file
					if (type.equals(".html") || type.equals(".htm"))
						contentType = "text/html";
					else if (type.equals(".jpg") || type.equals(".jpeg"))
						contentType = "image/jpeg";
					else if (type.equals(".gif"))
						contentType = "image/gif";
					else if (type.equals(".png"))
						contentType = "image/png";
					else if (type.equals(".txt"))
						contentType = "text/plain";
					else
						type = null;
					if (type == null) {
						send404error();
						return;
					}
					// System.out.println("Sending file");
					InputStream is = new FileInputStream(f);
					if (wt.threadStatus()) {
						s.getOutputStream().write(
								(httpVersion + " 200 OK\r\n").getBytes());
						s.getOutputStream().write(
								("Content-Length: " + f.length() + "\r\n")
										.getBytes());
						s.getOutputStream().write(
								("Content-Type: " + contentType + "\r\n")
										.getBytes());
						s.getOutputStream().write(
								("Date: " + sdf[0].format(new Date()) + "\r\n")
										.getBytes());
						s.getOutputStream().write(
								("Connection: close\r\n\r\n").getBytes());
					}
					if (method.equals("GET")) {
						// System.out.println("Method check!");
						byte[] buffer = new byte[1024];
						while (is.read(buffer) > 0 && wt.threadStatus())
							// should check for server running!
							s.getOutputStream().write(buffer);
					}
					s.getOutputStream().flush();
					is.close();
				} else {
					send404error();
					return;
				}
			} else
				send403error();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.warn(e);
		}
	}

	private String parseURL(String url) {// parse url if it is relative. like
											// ../../../
		// TODO Auto-generated method stub
		String[] path = url.split("/");
		int i = 0, j = 0;
		for (i = 0; i < path.length; i++)
			if (path[i].equals("..")) {
				path[i] = null;
				j = i - 1;
				while (j > 0) {
					if (path[j] != null) {
						path[j] = null;
						break;
					} else
						j--;
				}
			}
		String finalPath = null;
		j = path.length;
		for (i = 0; i < j; i++)
			if (path[i] != null)
				if (i == 0)
					finalPath = path[i] + "/";
				else if (i == j - 1)
					finalPath += path[i];
				else
					finalPath += path[i] + "/";
		return finalPath;
	}

	private void send403error() throws IOException {
		// TODO Auto-generated method stub
		String error403 = "<html>403 Error : Forbidden request. Not authorized to view the requested resource.</html>";
		if (wt.threadStatus()) {
			s.getOutputStream().write(
					(httpVersion + " 403 Forbidden\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Type: text/html\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Length: " + error403.length() + "\r\n")
							.getBytes());
			s.getOutputStream().write(
					("Date: " + sdf[0].format(new Date()) + "\r\n").getBytes());
			s.getOutputStream().write(("Connection: close\r\n\r\n").getBytes());
			s.getOutputStream().write(error403.getBytes());
			s.getOutputStream().flush();
		}
	}

	private void listFilenames(String parsedURL) {// send the list of files in
													// the directory requested
		// TODO Auto-generated method stub
		File d = new File(parsedURL);
		try {
			if (wt.threadStatus()) {
				s.getOutputStream().write(
						(httpVersion + " 200 OK\r\n").getBytes());
				s.getOutputStream().write(
						("Content-Type: text/html\r\n").getBytes());
				s.getOutputStream().write(
						("Content-Length: " + getContentLength(d) + "\r\n")
								.getBytes());
				s.getOutputStream().write(
						("Date: " + sdf[0].format(new Date()) + "\r\n")
								.getBytes());
				s.getOutputStream().write(
						("Connection: close\r\n\r\n").getBytes());
				s.getOutputStream().flush();
			}
			if (method.equals("GET") && wt.threadStatus()) {
				s.getOutputStream().write(dirList.getBytes());
				s.getOutputStream().flush();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.warn(e);
		}
	}

	private int getContentLength(File d) {// returns the length of the directory
											// list and populates it
		// TODO Auto-generated method stub
		dirList = "<html><h1>Dir Contents</h1><body>";
		for (File f : d.listFiles())
			dirList += "<a href=\"" + f.getName() + "\">" + f.getName()
					+ "<br>";
		dirList += "</body></html>";
		return dirList.length();
	}

	private boolean parseRequest() {// parse the incoming request
		// TODO Auto-generated method stub
		logger.info("In parseRequest()");
		while (!s.isClosed()) {
			try {
				s.setSoTimeout(20000);
				InputStreamReader isr;
				String line = null;
				BufferedReader reader;
				isr = new InputStreamReader(s.getInputStream());
				reader = new BufferedReader(isr);
				line = reader.readLine();
				if (line == null)
					return false;
				request = line;
				request += "\n";
				StringTokenizer token = new StringTokenizer(line, " ");
				method = token.nextToken();
				reqParams.put("method", method);
				if (method.equals("TRACE") || method.equals("DELETE")
						|| method.equals("PUT") || method.equals("OPTIONS")) {
					send501error();
					logger.info("505 error");
					return false;
				} else if (method.equals("GET") || method.equals("HEAD") || method.equals("POST")) {
					filename = token.nextToken();// assuming no spaces between
													// file and directory name
					logger.info("Requested filename: " + filename);
					wt.setFilename(filename);
					httpVersion = token.nextToken();
					reqParams.put("httpVersion", httpVersion);
					reqParams.put("filename", filename);
					// System.out.println("HTTP Version:"+httpVersion);
					String req = "";
					line = reader.readLine();
					String[] temp = new String[2];
					boolean Bodypresent = false;
					while (!line.isEmpty()) {
						if (req.equals(""))
							req = line + "\n";
						else
							req += line + "\n";
						if (line.contains(":")) {
							temp = line.split(":");
							reqParams.put(temp[0], temp[1]);
							if (line.contains("Content-Length:"))
								Bodypresent = true;
						}
						if (line.contains("Expect: 100-Continue")
								&& httpVersion.equals("HTTP/1.1")
								&& wt.threadStatus())
							s.getOutputStream().write(
									(httpVersion + " 100 Continue\r\n\r\n")
											.getBytes());
						if (line.contains("Expect: 100-Continue")
								&& httpVersion.equals("HTTP/1.0")
								&& wt.threadStatus()) {
							send417error();
							return false;
						}
						line = reader.readLine();
					}
					logger.info("Request: " + req);
					// char[] body=new
					// char[Integer.parseInt(reqParams.get("Content-Length").trim())];
					String body = "";
					if (Bodypresent == true) {
						line = reader.readLine();
						while (line!=null) {
							System.out.println("Line: "+line);
							body += line;
							line = reader.readLine();
							System.out.println("Line1: "+line);
						}
						reqParams.put("Body", body);

					}
					// System.out.println("Body:  "+reqParams.get("Body"));
					logger.info("Body: " + reqParams.get("Body"));
					if (httpVersion.equals("HTTP/1.1")) {
						if (!req.contains("Host:")) {
							send400error();
							return false;
						}
					}
					if (httpVersion.equals("HTTP/1.0")
							|| httpVersion.equals("HTTP/1.1")) {
						// System.out.println("In if1");
						if (req.contains("If-Modified-Since")) {
							String modifiedDate = null;
							modifiedDate = req.substring(req
									.indexOf("If-Modified-Since:"));

							int endindex = modifiedDate.indexOf("\n");
							if (modifiedDate.charAt(endindex - 1) == '\r')
								endindex -= 2;
							else
								endindex--;
							modifiedDate = modifiedDate.substring(19,
									endindex + 1);

							if (checkIfModified(modifiedDate)
									&& method.equals("GET"))
								return true;
							else
								return false;
						} else if (req.contains("If-Unmodified-Since")) {
							String modifiedDate = null;
							modifiedDate = req.substring(req
									.indexOf("If-Unmodified-Since:"));
							int endindex = modifiedDate.indexOf("\n");
							if (modifiedDate.charAt(endindex - 1) == '\r')
								endindex -= 2;
							else
								endindex--;
							modifiedDate = modifiedDate.substring(21,
									endindex + 1);
							if (checkIfUnmodified(modifiedDate))
								return true;
							else
								return false;
						} else
							return true;
					} else {
						send505error();
						return false;
					}
				} else {
					send400error();
					return false;
				}

			} catch (SocketTimeoutException t) {
				send408error();
				logger.info("408 error");
				return false;
			} catch (IOException e) {
				logger.info("500 error");
				send500error();
				return false;
			}
		}
		return false;
	}

	private void send417error() {
		// TODO Auto-generated method stub
		String error417 = "<html>417 Expectation Failed : Unmet by server.</html>";
		if (wt.threadStatus()) {
			try {
				s.getOutputStream().write(
						(httpVersion + " 417 Expectation Failed\r\n")
								.getBytes());
				s.getOutputStream().write(
						("Content-Type: text/html\r\n").getBytes());
				s.getOutputStream().write(
						("Content-Length: " + error417.length() + "\r\n")
								.getBytes());
				s.getOutputStream().write(
						("Date: " + sdf[0].format(new Date()) + "\r\n")
								.getBytes());
				s.getOutputStream().write(
						("Connection: close\r\n\r\n").getBytes());
				s.getOutputStream().write(error417.getBytes());
				s.getOutputStream().flush();
			} catch (IOException e) {
				// e.printStackTrace();
				logger.info(e);
			}

		}
	}

	private void send408error() {
		// TODO Auto-generated method stub
		String error408 = "<html>408 Request Timeout : Client didnot produce a request within twenty seconds. Try later</html>";
		if (wt.threadStatus()) {
			try {
				s.getOutputStream().write(
						(httpVersion + " 408 Bad Request\r\n").getBytes());
				s.getOutputStream().write(
						("Content-Type: text/html\r\n").getBytes());
				s.getOutputStream().write(
						("Content-Length: " + error408.length() + "\r\n")
								.getBytes());
				s.getOutputStream().write(
						("Date: " + sdf[0].format(new Date()) + "\r\n")
								.getBytes());
				s.getOutputStream().write(
						("Connection: close\r\n\r\n").getBytes());
				s.getOutputStream().write(error408.getBytes());
				s.getOutputStream().flush();
			} catch (IOException e) {
				// e.printStackTrace();
				logger.info(e);
			}

		}
	}

	private void send500error() {
		// TODO Auto-generated method stub
		String error500 = "<html>500 Internal Server Error : Server encountered an unexpected condition</html>";
		if (wt.threadStatus()) {
			try {
				s.getOutputStream().write(
						(httpVersion + " 500 Bad Request\r\n").getBytes());
				s.getOutputStream().write(
						("Content-Type: text/html\r\n").getBytes());
				s.getOutputStream().write(
						("Content-Length: " + error500.length() + "\r\n")
								.getBytes());
				s.getOutputStream().write(
						("Date: " + sdf[0].format(new Date()) + "\r\n")
								.getBytes());
				s.getOutputStream().write(
						("Connection: close\r\n\r\n").getBytes());
				s.getOutputStream().write(error500.getBytes());
				s.getOutputStream().flush();
			} catch (IOException e) {
				// e.printStackTrace();
				logger.info(e);
			}

		}
	}

	private boolean checkIfUnmodified(String modifiedDate) {// check-if-unmodified
															// header
		// TODO Auto-generated method stub
		boolean isAbsolute = false;
		String reqFile = null;
		try {

			if (filename.contains("http://"))
				if (filename.contains("localhost:" + port)) {
					isAbsolute = true;
					int index = filename.indexOf(Integer.toString(port))
							+ Integer.toString(port).length();
					// System.out.println(index);
					reqFile = filename.substring(index);
					// System.out.println("\nRequested file1 : "+reqFile);
				}
			if (!isAbsolute) {
				// System.out.println("Relative path");
				char c = rootDir.charAt(rootDir.length() - 1);
				if (c == '/')
					reqFile = rootDir + filename;
				else
					reqFile = rootDir + "/" + filename;
			}
			String parsedURL = parseURL(reqFile);
			if (parsedURL.startsWith(rootDir)) {
				File f = new File(parsedURL);
				Date headerDate = null;
				Date curDate = new Date();
				boolean isValidDate = false;
				int i = 0;
				for (i = 0; i < 3; i++)
					try {
						headerDate = sdf[i].parse(modifiedDate);
						isValidDate = true;
						break;
					} catch (java.text.ParseException e) {
						continue;
					}
				if (i == 3)
					return true;
				if (isValidDate && headerDate.compareTo(curDate) < 0) {
					if (headerDate.compareTo(sdf[i].parse(sdf[i]
							.format(new Date(f.lastModified())))) < 0)// header
																		// Date
																		// before
																		// file
																		// has
																		// been
																		// modified
					{
						// return 412
						if (wt.threadStatus()) {
							s.getOutputStream()
									.write((httpVersion + " 412 Precondition failed\r\n")
											.getBytes());
							s.getOutputStream().write(
									("Connection: close\r\n\r\n").getBytes());
							s.getOutputStream().flush();
						}
						return false;
					} else
						return true;
				} else
					// invalid date format or future date is referred. Ignore
					// the header
					return true;
			} else
				send403error();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.info(e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.info(e);
		}
		return false;

	}

	private boolean checkIfModified(String modifiedDate) {// check-if-modified
															// header
		// TODO Auto-generated method stub
		boolean isAbsolute = false;
		String reqFile = null;
		try {

			if (filename.contains("http://"))
				if (filename.contains("localhost:" + port)) {
					isAbsolute = true;
					int index = filename.indexOf(Integer.toString(port))
							+ Integer.toString(port).length();
					// System.out.println(index);
					reqFile = filename.substring(index);
					// System.out.println("\nRequested file1 : "+reqFile);
				}
			if (!isAbsolute) {
				// System.out.println("Relative path");
				char c = rootDir.charAt(rootDir.length() - 1);
				if (c == '/')
					reqFile = rootDir + filename;
				else
					reqFile = rootDir + "/" + filename;
			}
			String parsedURL = parseURL(reqFile);
			if (parsedURL.startsWith(rootDir)) {
				File f = new File(parsedURL);
				Date headerDate = null;
				Date curDate = new Date();
				boolean isValidDate = false;
				int i = 0;
				for (i = 0; i < 3; i++)
					try {
						headerDate = sdf[i].parse(modifiedDate);
						isValidDate = true;
						break;
					} catch (java.text.ParseException e) {
						continue;
					}
				if (i == 3)
					return true;
				if (isValidDate && headerDate.compareTo(curDate) < 0) {
					// System.out.println(new Date(f.lastModified()));
					if (headerDate.compareTo(sdf[i].parse(sdf[i]
							.format(new Date(f.lastModified())))) > 0
							&& wt.threadStatus())// header Date after file has
													// been modified
					{
						s.getOutputStream().write(
								(httpVersion + " 304 Not Modified\r\n")
										.getBytes());
						s.getOutputStream().write(
								("Date: " + sdf[0].format(new Date()) + "\r\n")
										.getBytes());
						s.getOutputStream().write(
								("Connection: close\r\n\r\n").getBytes());
						s.getOutputStream().flush();
						return false;
					} else
						return true;
				} else
					// invalid date format or future date is referred. Ignore
					// the header
					return true;
			} else
				send403error();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.info(e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.info(e);
		}
		return false;
	}

	private void send400error() throws IOException {
		// TODO Auto-generated method stub
		String error400 = "<html>400 Error : Bad Request. Include 'Host:' header for HTTP/1.1</html>";
		if (wt.threadStatus()) {
			s.getOutputStream().write(
					(httpVersion + " 400 Bad Request\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Type: text/html\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Length: " + error400.length() + "\r\n")
							.getBytes());
			s.getOutputStream().write(
					("Date: " + sdf[0].format(new Date()) + "\r\n").getBytes());
			s.getOutputStream().write(("Connection: close\r\n\r\n").getBytes());
			s.getOutputStream().write(error400.getBytes());
			s.getOutputStream().flush();
		}
	}

	private void send505error() throws IOException {
		// TODO Auto-generated method stub
		String error505 = "<html>505 Error : HTTP Version not Supported.</html>";
		if (wt.threadStatus()) {
			s.getOutputStream().write(
					(httpVersion + " 505 HTTP Version not Supported\r\n")
							.getBytes());
			s.getOutputStream().write(
					("Content-Type: text/html\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Length: " + error505.length() + "\r\n")
							.getBytes());
			s.getOutputStream().write(
					("Date: " + sdf[0].format(new Date()) + "\r\n").getBytes());
			s.getOutputStream().write(("Connection: close\r\n\r\n").getBytes());
			s.getOutputStream().write(error505.getBytes());
			s.getOutputStream().flush();
		}

	}

	private void send404error() throws IOException {
		// TODO Auto-generated method stub
		String error404 = "<html>404 Error : The requested resource doesn't exist.</html>";
		if (wt.threadStatus()) {
			s.getOutputStream().write(
					(httpVersion + " 404 Not Found\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Type: text/html\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Length: " + error404.length() + "\r\n")
							.getBytes());
			s.getOutputStream().write(
					("Date: " + sdf[0].format(new Date()) + "\r\n").getBytes());
			s.getOutputStream().write(("Connection: close\r\n\r\n").getBytes());
			s.getOutputStream().write(error404.getBytes());
			s.getOutputStream().flush();
		}
	}

	private void send501error() throws IOException {
		// TODO Auto-generated method stub
		String error501 = "<html>501 Error : Request Methods not implemented</html>";
		if (wt.threadStatus()) {
			s.getOutputStream().write(
					(httpVersion + " 501 Not Implemented\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Type: text/html\r\n").getBytes());
			s.getOutputStream().write(
					("Content-Length: " + error501.length() + "\r\n")
							.getBytes());
			s.getOutputStream().write(
					("Date: " + sdf[0].format(new Date()) + "\r\n").getBytes());
			s.getOutputStream().write(("Connection: close\r\n\r\n").getBytes());
			s.getOutputStream().write(error501.getBytes());
			s.getOutputStream().flush();
		}
	}

}
