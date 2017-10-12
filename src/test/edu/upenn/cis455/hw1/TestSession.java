package test.edu.upenn.cis455.hw1;

import junit.framework.TestCase;

import edu.upenn.cis455.webserver.Session;

public class TestSession extends TestCase{

	public void testGetAttribute() {
		Session s=new Session();
		s.setAttribute("Id","1");
		assertEquals("1",s.getAttribute("Id").toString());
	}

	public void testRemoveAttribute() {
		Session s=new Session();
		s.setAttribute("Id","1");
		s.removeAttribute("Id");
		assertNull(s.getAttribute("Id"));
	}
}
