package Servlets.src;

import java.io.*;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
  {
	  response.setContentType("text/html");
	  PrintWriter out = response.getWriter();

	  // Print the HTML header
	  out.println("<HTML><HEAD><TITLE>");
	  out.println("Request info");
	  out.println("</TITLE></HEAD>");

	  // Print the HTML body
	  out.println("<BODY><H1>Request info</H1><PRE>");
	  
	  out.println("getCharacterEncoding: " + request.getCharacterEncoding());
	  out.println("getContentLength: " + request.getContentLength());
	  
	  out.println("getContentType: " + request.getContentType());
	  out.println("getProtocol: " + request.getProtocol());
	  out.println("getRemoteAddr: " + request.getRemoteAddr());
	  out.println("getRemoteHost: " + request.getRemoteHost());
	  out.println("getScheme: " + request.getScheme());
	  out.println("getServerName: " + request.getServerName());
	  out.println("getServerPort: " + request.getServerPort());
	  out.println("getAuthType: " + request.getAuthType());
	  out.println("getMethod: " + request.getMethod());
	  out.println("getPathInfo: " + request.getPathInfo());
	  out.println("getPathTranslated: " + request.getPathTranslated());
	  out.println("getQueryString: " + request.getQueryString());
	  out.println("getRemoteUser: " + request.getRemoteUser());
	  out.println("getRequestURI: " + request.getRequestURI());
	  out.println("getServletPath: " + request.getServletPath());

	  out.println();
	  
	  out.println("Parameters:");
	  Enumeration paramNames = request.getParameterNames();
	  while (paramNames.hasMoreElements()) {
	    String name = (String) paramNames.nextElement();
	    String[] values = request.getParameterValues(name);
	    out.println("    " + name + ":");
	    for (int i = 0; i < values.length; i++) {
	      out.println("      " + values[i]);
	    }
	  }

	  out.println();
	  out.println("Request headers:");
	  Enumeration headerNames = request.getHeaderNames();
	  while (headerNames.hasMoreElements()) {
	    String name = (String) headerNames.nextElement();
	    String value = request.getHeader(name);
	    out.println("  " + name + " : " + value);
	  }

	  out.println();
	  out.println("Cookies:");
	  Cookie[] cookies = request.getCookies();
	  for (int i = 0; i < cookies.length; i++) {
	    String name = cookies[i].getName();
	    String value = cookies[i].getValue();
	    out.println("  " + name + " : " + value);
	  }

	  // Print the HTML footer
	  out.println("</PRE></BODY></HTML>");
	  out.close();
	}
}
  