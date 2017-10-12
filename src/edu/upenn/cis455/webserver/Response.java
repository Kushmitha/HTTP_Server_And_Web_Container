package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author tjgreen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Response implements HttpServletResponse {

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public HashMap<Integer,String> responseCodes = new HashMap<Integer,String>();

	public HashMap<String,Object> m_props;
	public HashMap<Integer,String> retCodes = new HashMap<Integer,String>();
	public boolean isCommitted;
	Socket s;
	public StringWriter buf;
	
	Container container;
	PrintWriter pw;	
	int bufSize;
	
	public Response(Container t) {
		m_props = new HashMap<String,Object>();
		pw = null;
		bufSize = 8192;
		isCommitted = false;
		buf = null;
		container = t;
		responseCodes.put(SC_CONTINUE,"Continue");
		responseCodes.put(SC_OK,"OK");
		responseCodes.put(SC_FORBIDDEN,"Forbidden request");
		responseCodes.put(SC_EXPECTATION_FAILED,"Expectation Failed");
		responseCodes.put(SC_REQUEST_TIMEOUT,"Request Timeout");
		responseCodes.put(SC_INTERNAL_SERVER_ERROR,"Internal Server Error");
		responseCodes.put(SC_BAD_REQUEST,"Bad Request");
		responseCodes.put(SC_HTTP_VERSION_NOT_SUPPORTED,"HTTP Version not Supported");
		responseCodes.put(SC_NOT_FOUND,"Page Not Found");
		responseCodes.put(SC_NOT_IMPLEMENTED,"HTTP Methods not Implemented");
	}
	
	public void addCookie(Cookie arg0) {

		if(arg0.getName().equalsIgnoreCase("JSESSIONID"))
		{
			System.out.println("HIIIII");
			if(!m_props.containsKey("Set-Cookie")){
				m_props.put("Set-Cookie", arg0.getName()+"="+arg0.getValue());
				return;
			}
			m_props.put("Set-Cookie", (String) m_props.get("Set-Cookie")+"\r\nSet-Cookie:"+arg0.getName()+"="+arg0.getValue());

		}
		else
		{
			if(!m_props.containsKey("Set-Cookie")){
				m_props.put("Set-Cookie", arg0.getName()+"="+arg0.getValue()+"; Expires="+getExpiry(arg0));
				return;
			}
			m_props.put("Set-Cookie", (String) m_props.get("Set-Cookie")+"\r\nSet-Cookie:"+arg0.getName()+"="+arg0.getValue()+"; Expires="+getExpiry(arg0));

		}
	}

	public String getExpiry(Cookie arg0) {
		// TODO Auto-generated method stub
		
		Calendar curr = Calendar.getInstance(); 
		SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US); 
		df.setTimeZone(TimeZone.getTimeZone("GMT")); 
		String date = df.format(curr.getTimeInMillis()+arg0.getMaxAge()*1000);
		return date;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String arg0) {
		// TODO Auto-generated method stub
		return m_props.containsKey(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted())
			throw new IllegalStateException();
		else
			retCodes.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted())
			throw new IllegalStateException();
		else
		{
			retCodes.put(arg0,responseCodes.get(arg0).toString());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String arg0) throws IOException {

		String redirecTo="";
		if(arg0.startsWith("http://localhost"))
			redirecTo=arg0;
		else if(arg0.startsWith("/"))
			redirecTo="http://localhost:"+HttpServer.port+arg0;
		else
			redirecTo=m_props.get("filename")+"/"+arg0;
		addHeader("Location", redirecTo);
		String status303="<html>303 Error : Page moved. <a href=\""+redirecTo+"\">Click here</a> to redirect.</html>";
		setStatus(303);
		setBufferSize(status303.length());
		setContentLength(status303.length());
		setContentType("text/html");
		getWriter().print(status303);
		//flushBuffer();	
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		m_props.put(arg0,m_props.get(arg0).toString()+","+arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		if(m_props.get(arg0)!=null)	{
			m_props.put(arg0,(String) m_props.get(arg0)+","+arg1);
		}
		else
			m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
		m_props.put(arg0,(String) m_props.get(arg0)+","+arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub
		if(responseCodes.get(arg0)!=null)
			retCodes.put(arg0, responseCodes.get(arg0));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub
		retCodes.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		if(m_props.containsKey("Encoding"))
			return (String)m_props.get("Encoding");
		else
			return "ISO-8859-1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		String contentType = (String) m_props.get("Content-Type");
		if(contentType == null)
			return "text/html";
		return contentType;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		
		buf = new StringWriter(bufSize);
		pw = new PrintWriter(buf,false);
		return pw;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		m_props.put("Character-Encoding",arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		m_props.put("Content-Length", arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		m_props.put("Content-Type", arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub
		bufSize = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return bufSize;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		container.writeResponse(this);
		isCommitted = true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		// TODO Auto-generated method stub
		if(isCommitted())
			throw new IllegalStateException();
		else
		{	buf.getBuffer().setLength(0);
			return;	
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return isCommitted;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub
		if(isCommitted())
			throw new IllegalStateException();
		else
		{
			buf.getBuffer().setLength(0);
			m_props.clear();
			retCodes.clear();
			return;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		m_props.put("Locale",arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return (Locale) m_props.get("Locale");
	}
	void setSocket(Socket sock)
	{
		s = sock;
	}
	
	Socket getSocket()
	{
		return s;
	}
}
