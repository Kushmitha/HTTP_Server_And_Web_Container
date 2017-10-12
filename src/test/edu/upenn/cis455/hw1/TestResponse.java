package test.edu.upenn.cis455.hw1;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import edu.upenn.cis455.webserver.Container;
import edu.upenn.cis455.webserver.Response;
public class TestResponse extends TestCase {
	static final Logger logger = Logger.getLogger(TestResponse.class);
	Container ctr=new Container("././conf/web.xml", logger);
	Response res=new Response(ctr);

	public void testAddCookie() {
		Cookie c=new Cookie("Test","12345");
		res.addCookie(c);
		assertEquals(true,res.m_props.get("Set-Cookie").toString().contains("Test"));
	}

	public void testSetStatus()
	{

		res.setStatus(404);
		assertEquals(res.retCodes.get(404),"Page Not Found");
		
	}

	public void testgetExpiry()
	{
		Cookie c=new Cookie("Test","12345");
		Calendar curr = Calendar.getInstance(); 
		SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US); 
		df.setTimeZone(TimeZone.getTimeZone("GMT")); 
		String date = df.format(curr.getTimeInMillis());
		assertEquals(res.getExpiry(c),date);
	}

}
