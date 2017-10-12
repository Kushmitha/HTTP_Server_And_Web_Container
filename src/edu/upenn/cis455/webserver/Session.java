package edu.upenn.cis455.webserver;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class Session implements HttpSession {
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 */
	private Properties m_props = new Properties();
	private boolean m_valid = true;
	
	public Session()
	{
		m_props.setProperty("CreationTime", Long.toString(System.currentTimeMillis()));
		m_props.setProperty("LastAccessedTime", Long.toString(System.currentTimeMillis()));
	}
	
	public long getCreationTime() {
		// TODO Auto-generated method stub
		return Long.valueOf(m_props.getProperty("CreationTime"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	public String getId() {
		// TODO Auto-generated method stub
		return m_props.getProperty("Id");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return Long.valueOf(m_props.getProperty("LastAccessedTime"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return (ServletContext)m_props.get("ServletContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int arg0) {
		// TODO Auto-generated method stub
		m_props.setProperty("MaxInactiveInterval", Integer.toString(arg0));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		if(m_props.getProperty("MaxInactiveInterval")==null)
			return 0;
		return Integer.valueOf(m_props.getProperty("MaxInactiveInterval"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	public Enumeration<Object> getAttributeNames() {
		// TODO Auto-generated method stub
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */

	public String[] getValueNames() {
		// TODO Auto-generated method stub
		Enumeration<Object> e = m_props.keys();
		ArrayList<String> values = new ArrayList<String>();
		while(e.hasMoreElements())
			values.add((String) e.nextElement());
		return values.toArray(new String[0]);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	public void removeValue(String arg0) {
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	public void invalidate() {
		m_valid = false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	public boolean isNew() {
		// TODO Auto-generated method stub
		return Boolean.valueOf(m_props.getProperty("isNew"));
	}
	
	public void updateLastAccessedTime() {
		// TODO Auto-generated method stub
		m_props.setProperty("LastAccessedTime", Long.toString(System.currentTimeMillis()));
	}

	public boolean isValid() {//check if lastAccessed+session timeout<curr time
		if(m_valid==true)
		{
			Long time1=System.currentTimeMillis();
			int timeout=getMaxInactiveInterval();
			if(timeout==0)
				timeout=Container.session_timeout;
			Long time2=getLastAccessedTime()+timeout*60000;
			if( time1>time2)
			{
				m_valid=false;
				return m_valid;
			}
			else
				return m_valid;
		}
		else
			return false;
	}
}
