package edu.upenn.cis455.webserver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * @author Todd J. Green
 */
public class Request implements HttpServletRequest {
	
	private Properties m_params = new Properties();
	private Properties m_props = new Properties();
	private Session m_session = null;
	private String m_method;
	private Response res;
	private boolean fromCookie;
	Request() {
	}
	
	public Request(Session session, Response res) {
		//m_session = session;
		this.res=res;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		// TODO Auto-generated method stub
		return BASIC_AUTH;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		if(m_props.get("Cookie") == null)
			return new Cookie[0];
		String cookiestr = (String) m_props.get("Cookie");
		String[] c = cookiestr.split(";");
		Cookie[] cookies = new Cookie[c.length];
		for(int i = 0;i<c.length;i++) {
			String[] s = c[i].split("=");

			if(s[0].contains("JSESSIONID")) {
				m_session = HttpServer.Sessions.get(s[1]);
			}
			Cookie cookie = new Cookie(s[0].trim(),s[1]);
			cookies[i] = cookie;

		}
		return cookies;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String arg0) {
		// TODO Auto-generated method stub
		return Long.valueOf(m_props.getProperty(arg0));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		if(m_props.getProperty(arg0)==null)
			return null;
		else
			return m_props.getProperty(arg0).split(",")[0];
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration getHeaders(String arg0) {
		// TODO Auto-generated method stub
		if(m_props.getProperty(arg0)==null)
			return null;
		String temp[]=m_props.getProperty(arg0).split(",");
		List<String> list=new ArrayList<String>();
		for(int i=0;i<temp.length;i++)
			list.add(temp[i]);
		return (Enumeration)list;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration getHeaderNames() {
		// TODO Auto-generated method stub
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String arg0) {
		// TODO Auto-generated method stub
		if(m_props.getProperty(arg0)==null)
			return -1;
		try
		{
			return Integer.parseInt(m_props.getProperty(arg0));
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
		}
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return m_method;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		// TODO Auto-generated method stub
		try {
			return URLDecoder.decode(m_props.getProperty("PathInfo"),getCharacterEncoding());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		// TODO Auto-generated method stub
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		// TODO Auto-generated method stub
		try {
			if(m_props.getProperty("QueryString")!=null)
				return URLDecoder.decode(m_props.getProperty("QueryString"),getCharacterEncoding());
			else
				return "";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return m_session.getId();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		// TODO Auto-generated method stub
		String uri="";//assuming context path is in root
		if(getServletPath()!=null)
			uri+=getServletPath();
		if(getPathInfo()!=null)
			uri+=getPathInfo();
		return uri;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		StringBuffer url=new StringBuffer();
		url.append(getScheme()+"://"+getServerName()+":"+getServerPort()+getServletPath()+getPathInfo());
		return url;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		// TODO Auto-generated method stub
		return m_props.getProperty("ServletPath");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean arg0) {
		
		if(res.isCommitted())
			throw new IllegalStateException();
		Cookie[] cookies=getCookies();
		//System.out.println("Session value: "+m_session.toString());
		//System.out.println("Inside GetSession");
		if (arg0) 
		{
			if (!hasSession()) 
			{
				fromCookie = false;
				m_session = new Session();
				m_session.putValue("isNew", true);
				m_session.setAttribute("Id",UUID.randomUUID().toString());
				HttpServer.Sessions.put(m_session.getId(), m_session);
			}
			else
			{
				fromCookie = true;
				m_session.putValue("isNew", false);
				return m_session;
			}
			m_session.updateLastAccessedTime();
			Cookie c = new Cookie("JSESSIONID",m_session.getId());
			c.setMaxAge(5000);
			res.addCookie(c);
			return m_session;
		} 
		else 
		{
			if (!hasSession()) 
			{
				m_session = null;
			}
		}
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return m_session.isValid();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return fromCookie;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return fromCookie;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		if(m_props.getProperty("Character-Encoding")==null)
			return "ISO-8859-1";
		else 
			return m_props.getProperty("Character-Encoding");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		m_props.setProperty("Character-Encoding",arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		// TODO Auto-generated method stub
		if(m_props.getProperty("Content-Length")==null)
			return -1;
		else
		return Integer.valueOf(m_props.getProperty("Content-Length"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		return m_props.getProperty("Content-Type");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String arg0) {
		return m_params.getProperty(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return m_params.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String arg0) {
		// TODO Auto-generated method stub
		String temp=m_params.getProperty(arg0);
		if(temp==null)
			return null;
		else
			return temp.split(",");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		// TODO Auto-generated method stub
		Map<String,String> map = new HashMap<String, String>();
		Enumeration<Object> e=m_props.keys();
		String temp=null;
		while(e.hasMoreElements())
		{
			temp=e.nextElement().toString();
			map.put(temp, m_params.getProperty(temp));
		}
		return map;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		// TODO Auto-generated method stub
		return m_props.getProperty("httpVersion");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		// TODO Auto-generated method stub
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		// TODO Auto-generated method stub
		//return m_props.getProperty("ServerName");
		return "localhost";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		// TODO Auto-generated method stub
		return Integer.valueOf(m_props.getProperty("ServerPort"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return (BufferedReader)m_props.get("Reader");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		if(m_props.getProperty("RemoteAddr")==null)
			return "127.0.0.1";
		else
			return m_props.getProperty("RemoteAddr");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		if(m_props.getProperty("RemoteHost")==null)
			return "localhost";
		else
		return m_props.getProperty("RemoteHost");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		if(m_props.get("Locale")==null)
			return null;
		else
			return (Locale)m_props.get("Locale");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		// TODO Auto-generated method stub
		return "localhost";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return "127.0.0.1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return Integer.valueOf(m_props.getProperty(("Port")));
	}

	public void setMethod(String method) {
		m_method = method;
	}
	
	void setParameter(String key, String value) {
		if(m_params.getProperty(key)==null)
			m_params.setProperty(key, value);
		else
			m_params.setProperty(key, m_params.getProperty(key)+","+value);
	}
	
	void clearParameters() {
		m_params.clear();
	}
	
	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}
	

}
