package edu.upenn.cis455.webserver;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.Servlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class Container {
	String webxmlPath;
	static int session_timeout;
	boolean parsingDone;
	Handler h;
	HashMap<String,HttpServlet> servlets;
	Context ServletContext=null;
	static Logger logger;
	SimpleDateFormat sdf;
	//static final Logger logger = Logger.getLogger(Container.class);
	static class Handler extends DefaultHandler {//Handler for webxml
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				m_state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 10) ? 11 : 21;
			} else if(qName.compareTo("url-pattern") == 0) {
				m_state=5;
			} else if(qName.compareTo("session-timeout") == 0){
				m_state=6;
			}
		}
		public void characters(char[] ch, int start, int length) {//parsing webxml
			String value = new String(ch, start, length);
			if (m_state == 1) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 2) {
				m_servlets.put(m_servletName, value);
				m_state = 0;
			} else if (m_state == 10 || m_state == 20) {
				m_paramName = value;
			} else if (m_state == 11) {
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 21) {
				if (m_paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = m_servletParams.get(m_servletName);
				if (p == null) {
					p = new HashMap<String,String>();
					m_servletParams.put(m_servletName, p);
				}
				p.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			}
			else if(m_state == 5){
				m_urlpattern.put(m_servletName, value);
				m_state = 0;
			}
			else if(m_state==6){
				session_timeout=Integer.parseInt(value);
			}
		}
		private int m_state = 0;
		private String m_servletName;
		private String m_paramName;
		HashMap<String,String> m_urlpattern=new HashMap<String, String>();
		HashMap<String,String> m_servlets = new HashMap<String,String>();
		HashMap<String,String> m_contextParams = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();
	}

	public Container(String webxmlPath,Logger l) {//initialize parameters
		// TODO Auto-generated constructor stub
		this.webxmlPath=webxmlPath;
		parsingDone=false;
		logger=l;
		logger.getLogger(Container.class);
		session_timeout=0;
		sdf=new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {//check if file exists
			logger.info("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	private static Context createContext(Handler h) {//creates Servletcontext based on context params from webxml
		Context fc = new Context();
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		}
		return fc;
	}
	
	private static HashMap<String,HttpServlet> createServlets(Handler h, Context fc) throws Exception {//creates servlets based on servlet class/name mapping from webxml
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			Config config = new Config(servletName, fc);
			String className = h.m_servlets.get(servletName);
			//System.out.println("className :"+className);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}
	
	public boolean servletRequestHandling(String filename, Socket s,
			HashMap<String, String> reqParams) throws Exception {
		// TODO Auto-generated method stub
		if(parsingDone==false)//to ensure parsing is done only once
		{
			h = parseWebdotxml(webxmlPath);
			Context context = createContext(h);
			if(ServletContext==null)
				ServletContext=context;
			logger.info("Webxml parsed!");
			servlets = createServlets(h, context);
			parsingDone=true;
		}
		logger.info("Session Timeout : "+session_timeout);
		Session session = new Session();//instantiate session
		Response res=new Response(this);//response
		Request req=new Request(session, res);//and request objects
		//session.setMaxInactiveInterval(session_timeout*60);
		res.setSocket(s);
		String str[]=null;
		String temp=null;
		if(filename.contains("?") && !filename.equals("/shutdown?"))//check if QueryString or shutdown command
		{
			logger.info("Query String found!: ");
			str=filename.split("\\?");
			temp=str[0];
			req.setAttribute("QueryString", str[1]);
		}
		else
			temp=filename;	
		//System.out.println("Temp: "+temp);
		//String servletPath=getServletPath(temp);
		res.m_props.put("filename",temp);
		String servletName=getServletName(temp);
		logger.info("Servlet Name: "+servletName);
		//System.out.println("Servlet Path: "+servletPath);
		
		if(servletName!=null)//check if servlet mapping found
		{
			HttpServlet servlet=servlets.get(servletName);//check servelt is not null
			if(servlet==null)
			{
				logger.warn("No servlet found!");
				return false;
			}
			else
			{
				String servletPath=h.m_urlpattern.get(servletName);
				if(servletPath.contains("/*"))
					servletPath=servletPath.replace("/*","  ");//obtain servletPath
				//System.out.println("ServletPath: "+servletPath.trim());
				req.setAttribute("ServletPath", servletPath.trim());
				String PathInfo=temp.substring(servletPath.trim().length());//obtain pathInfo
				req.setAttribute("PathInfo", PathInfo);
				res.setHeader("httpVersion", reqParams.get("httpVersion"));
				String date=sdf.format(new Date());
				res.setHeader("Date",date);
				res.setHeader("Connection","close");
				for(String t:reqParams.keySet())
				{
					req.setAttribute(t, reqParams.get(t));//set request attributes from request headers
				}
				req.setAttribute("ServletContext",ServletContext);
				String[] qs=null;
				String[] tmp=null;
				if(filename.contains("?"))
				{
					qs=req.getAttribute("QueryString").toString().split("&");
					tmp=new String[2];
					for(int i=0;i<qs.length;i++)//set request parameters from query string
					{
						tmp=qs[i].split("=");
						req.setParameter(tmp[0],tmp[1]);
					}
				}
				if(reqParams.get("Body")!=null)//set request parameters if present in body
				{
					qs=reqParams.get("Body").split("&");
					for(int i=0;i<qs.length;i++)
					{
						tmp=qs[i].split("=");
						req.setParameter(tmp[0],tmp[1]);
					}
				}

				if(reqParams.get("method").equals("GET")||reqParams.get("method").equals("POST"))//support only doGet() and doPost()
				{
					req.setMethod(reqParams.get("method"));
					logger.info("Calling servlet service ");
					servlet.service(req,res);//servicing the servlet
					logger.info("Finished servlet service ");
					//writeResponse(res);
					if(res.isCommitted()==false)//write response only if it is not committed
					{
						writeResponse(res);
							
					}
				}
				else
				{
					logger.info("Only GET and POST methods supported by Servlet container");
					return false;
				}
			}
		}
		else
		{
			logger.info("URL match not found. Directing request to Static server!");
			return false;
		}

		return true;
	}

	private String getServletName(String str) {//url parsing and returns matching servlet name
		// TODO Auto-generated method stub	   // Supports two types
		String value="";					   //1. Exact matching
		String ret="";						   //2. foo/bar/xyz/ -> foo/bar/*
		for (HashMap.Entry<String, String> entry : h.m_urlpattern.entrySet()) 
		{
		    value = entry.getValue();
		    if(value.equals(str))//exact matching
		    	{
		    		return entry.getKey();
		    	}
		    if(value.equals("/*"))
		    	ret=entry.getKey();
		}
		int maxlen=ret.length();
		String temp=null;
		for (HashMap.Entry<String, String> entry : h.m_urlpattern.entrySet()) //recursive check
		{
			temp=entry.getValue();
			if(temp.contains("*"))
			{
				temp=temp.substring(0,temp.indexOf("*")-1);
				if(str.contains(temp.trim())&&temp.length()>maxlen)
				{
					if(str.length()<=temp.length()||(str.length()>temp.length()&&str.charAt(temp.length())=='/'))
					{
						ret=entry.getKey();
						maxlen=temp.length();
					}
				}
			}
		}
		return ret;
	}

	public void writeResponse(Response response) throws IOException {//write response to output
		// TODO Auto-generated method stub
		if(response.getSocket().isClosed())
			return;
		OutputStream out=response.getSocket().getOutputStream();
		if(response.retCodes.isEmpty())
			out.write((response.m_props.get("httpVersion")+" 200 OK\r\n").getBytes()); 
		else
		{
			Set<Integer> key = response.retCodes.keySet();
			Iterator<Integer> a = key.iterator();
			out.write((response.m_props.get("httpVersion")+" "+a.next()+response.retCodes.get(a.next())+"\r\n").getBytes());
		}
		for(Object t: response.m_props.keySet())
		{
			out.write((t+":"+response.m_props.get(t)+"\r\n").getBytes());
		}
		out.write(("\r\n").getBytes());
		out.flush();
		//out=response.getSocket().getOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(out);
		PrintWriter pw=new PrintWriter(bos,true);
		if(response.buf.toString()!=null)
		{
			pw.print(response.buf.toString());
			pw.flush();
		}
		response.isCommitted=true;
		out.close();
		pw.close();

	}
	
	public void shutDown() {//do shutdown and destroy all servlets
		// TODO Auto-generated method stub
		for(Servlet s:servlets.values())
			s.destroy();
		servlets.clear();
	}

}
