package test.edu.upenn.cis455.hw1;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import edu.upenn.cis455.webserver.Container;
import edu.upenn.cis455.webserver.Request;
import edu.upenn.cis455.webserver.Response;
import edu.upenn.cis455.webserver.Session;

public class TestRequest extends TestCase {
	final Logger logger = Logger.getLogger(TestRequest.class);
	Session session=new Session();
	Container c=new Container("././conf/web.xml", logger);
	Response res=new Response(c);
	Request req=new Request(session,res);

	public void testgetAuthType()
	{
		assertEquals("BASIC",req.getAuthType());
	}

	public void testGetMethod()
	{
		req.setMethod("GET");
		assertEquals("GET",req.getMethod());
		
	}

	public void testRemoveAttribute()
	{
		req.setAttribute("Method","GET");
		req.removeAttribute("Method");
		assertEquals(null,req.getAttribute("Method"));
	}

	public void testHasSeesion()
	{
		session.invalidate();
		assertEquals(null,req.getSession(false));
	}
}
