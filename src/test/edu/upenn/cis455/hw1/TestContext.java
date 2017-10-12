package test.edu.upenn.cis455.hw1;

import junit.framework.TestCase;

import edu.upenn.cis455.webserver.Context;

public class TestContext extends TestCase {
	Context cxt=new Context();

	public void testGetAttribute() {
		
		cxt.setAttribute("ContextName","Check");
		assertEquals("Check",cxt.getAttribute("ContextName").toString());
	}

	public void testGetInitParameter()
	{
		cxt.setInitParam("Key", "value");
		assertEquals("value",cxt.getInitParameter("Key"));
	}

	public void testRemoveAttribute() {
		
		cxt.setAttribute("ContextName","Check");
		cxt.removeAttribute("Contextname");
		assertNotNull(cxt.getAttribute("ContextName"));
	}
}
