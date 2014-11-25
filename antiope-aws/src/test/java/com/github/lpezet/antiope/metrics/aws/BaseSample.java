/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;

/**
 * @author Luc Pezet
 *
 */
public class BaseSample {
	
	private Server mServer;
	private int mPort;
	
	private Server mCloudWatchServer;
	private int mCloudWatchPort;
	
	@Before
	public void setup() throws Exception {
		mServer = new Server(0);
		mServer.setHandler(new AbstractHandler() {
			
			@Override
			public void handle(String target, 
					org.eclipse.jetty.server.Request baseRequest,
					HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				response.setContentType("text/html;charset=utf-8");
		        response.setStatus(HttpServletResponse.SC_OK);
		        baseRequest.setHandled(true);
		        response.getWriter().println("<h1>Hello World</h1>");
			}
		});
		mServer.start();
		mPort = mServer.getConnectors()[0].getLocalPort();
		
		mCloudWatchServer = new Server(0);
		mCloudWatchServer.setHandler(new AbstractHandler() {
			
			@Override
			public void handle(String target, 
					org.eclipse.jetty.server.Request baseRequest,
					HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				System.out.println("CloudWatch Request....");
				System.out.println("######### Got CloudWatch Request:\n" + printRequest(request));
				response.setContentType("application/xml;charset=utf-8");
		        response.setStatus(HttpServletResponse.SC_OK);
		        baseRequest.setHandled(true);
		        response.getWriter().println("<ok/>");
			}

			private String printRequest(HttpServletRequest pRequest) throws IOException {
				StringBuffer oBuf = new StringBuffer(pRequest.getMethod()).append(" ").append(pRequest.getPathInfo());
				for(Enumeration<String> e = pRequest.getHeaderNames(); e.hasMoreElements(); ) {
					String oHeaderName = e.nextElement();
					oBuf.append("\n").append(oHeaderName).append(": ").append(pRequest.getHeader(oHeaderName));
				}
				if (pRequest.getInputStream() != null) {
					oBuf.append("\n\n").append(IOUtils.toString(pRequest.getInputStream()));
				}
				return oBuf.toString();
			}
		});
		mCloudWatchServer.start();
		mCloudWatchPort = mCloudWatchServer.getConnectors()[0].getLocalPort();
	}
	
	@After
	public void tearDown() throws Exception {
		mServer.stop();
		mServer.destroy();
		
		mCloudWatchServer.stop();
		mCloudWatchServer.destroy();
	}
	
	public int getCloudWatchPort() {
		return mCloudWatchPort;
	}
	
	public Server getCloudWatchServer() {
		return mCloudWatchServer;
	}
	
	public int getPort() {
		return mPort;
	}
	
	public Server getServer() {
		return mServer;
	}

}
