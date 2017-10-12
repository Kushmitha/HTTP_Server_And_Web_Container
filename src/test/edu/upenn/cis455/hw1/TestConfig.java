package test.edu.upenn.cis455.hw1;

import junit.framework.TestCase;
import edu.upenn.cis455.webserver.Config;
import edu.upenn.cis455.webserver.Context;

public class TestConfig extends TestCase {

	Config cfg=new Config("ServletName",new Context());

	public void testGetInitParameter() {
		cfg.setInitParam("Key", "value");
		assertEquals("value",cfg.getInitParameter("Key"));
	}

	public void testGetServletName() {
		assertEquals("ServletName",cfg.getServletName());
	}

}
