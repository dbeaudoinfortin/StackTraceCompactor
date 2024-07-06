package com.dbf.utils.stacktrace;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;


public class StackTraceCompactorTest {

	@Test
	public void testStackElementCompaction() {
		String rawStack = "java.net.ConnectException: Connection refused (Connection refused)\r\n" + 
				"    at AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:350)\r\n" + 
				"    at java.net.PlainSocketImpl.socketConnect(Native Method)\r\n" + 
				"    at java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:350)\r\n" + 
				"    at java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.blorg)\r\n" + 
				"    at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.blorg:188)\r\n" + 
				"    at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)\r\n" + 
				"    at java.net.Socket.connect(Socket.java:589)\r\n" + 
				"    at okhttp3.internal.platform.Platform.connectSocket(Platform.java:124)\r\n" + 
				"    at okhttp3.internal.connection.RealConnection.connectSocket(RealConnection.java:223)\r\n" + 
				" [wrapped] java.net.ConnectException: Failed to connect to crazy-broken-server/192.168.0.0:1001\r\n" + 
				"    at okhttp3.internal.connection.RealConnection.connectSocket(RealConnection.java:225)\r\n" + 
				"    at okhttp3.internal.connection.RealConnection.connect(RealConnection.java:149)\r\n" + 
				"    at okhttp3.internal.connection.StreamAllocation.findConnection(StreamAllocation.java:192)\r\n" + 
				"    at okhttp3.internal.connection.StreamAllocation.findHealthyConnection(StreamAllocation.java:121)\r\n" + 
				"    at okhttp3.internal.connection.StreamAllocation.newStream(StreamAllocation.java:100)\r\n" + 
				"    at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.java:42)\r\n" + 
				"    at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java)\r\n" + 
				"    at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain:67)\r\n" + 
				"    at okhttp3.internal.cache.CacheInterceptors.intercept(CacheInterceptor.java:93)\r\n" + 
				"    at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:92)\r\n" + 
				"    at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:67)\r\n" + 
				"    at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.java:93)\r\n" + 
				"    at RealInterceptorChain.proceed(RealInterceptorChain.java:92)\r\n" + 
				"at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInter�"; //The end character in invalid on purpose
		
		String expectedStack = "java.net.ConnectException: Connection refused (Connection refused)\r\n" + 
				"    at AbstractPlainSocketImpl.doConnect(350)\r\n" + 
				"    at java.net.PlainSocketImpl.socketConnect(Native Method)\r\n" + 
				"    at j.n.AbstractPlainSocketImpl.doConnect(350)\r\n" + 
				"    at j.n.A.connectToAddress(~.blorg)\r\n" + 
				"    at j.n.A.connect(~.blorg:188)\r\n" + 
				"    at j.n.SocksSocketImpl.connect(392)\r\n" + 
				"    at j.n.Socket.connect(589)\r\n" + 
				"    at okhttp3.internal.platform.Platform.connectSocket(124)\r\n" + 
				"    at o.i.connection.RealConnection.connectSocket(223)\r\n" + 
				"[wrapped] java.net.ConnectException: Failed to connect to crazy-broken-server/192.168.0.0:1001\r\n" + 
				"    at o.i.c.R.connectSocket(225)\r\n" + 
				"    at o.i.c.R.connect(149)\r\n" + 
				"    at o.i.c.StreamAllocation.findConnection(192)\r\n" + 
				"    at o.i.c.S.findHealthyConnection(121)\r\n" + 
				"    at o.i.c.S.newStream(100)\r\n" + 
				"    at o.i.c.ConnectInterceptor.intercept(42)\r\n" + 
				"    at o.i.http.RealInterceptorChain.proceed()\r\n" + 
				"    at o.i.h.R.proceed(67)\r\n" + 
				"    at o.i.cache.CacheInterceptors.intercept(CacheInterceptor.java:93)\r\n" + 
				"    at o.i.http.RealInterceptorChain.proceed(92)\r\n" + 
				"    at o.i.h.R.proceed(67)\r\n" + 
				"    at o.i.h.BridgeInterceptor.intercept(93)\r\n" + 
				"    at RealInterceptorChain.proceed(92)\r\n" + 
				"at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInter�"; //The end character in invalid on purpose
		
		//Split the stack trace into elements, compact it, and then recombine it
		String[] originalStackElements = StringUtils.split(rawStack, "\r\n");
		StackTraceCompactor.collapseStackElements(originalStackElements);
		Assert.assertEquals(expectedStack, combineStackElements(originalStackElements));
	}
	
	@Test
	public void tripleWrappedMatchingExcpetionTest() {
		Throwable testThrowable = null;
		try {
			try {
				throw new IOException("Your pipe is broken, sir.");
			} catch (Throwable t) {
				try {
					throw new RuntimeException(t);
				} catch (Throwable t2) {
					throw new RuntimeException(t2);
				}
			}
		} catch (Throwable t) {
			testThrowable = t;
		}
		String resultingStack = StackTraceCompactor.getCompactStackTrace(testThrowable);
		
		//Verify that redundant exception messages have been removed
		Assert.assertTrue(resultingStack.contains("java.io.IOException: Your pipe is broken, sir." + System.lineSeparator()));
		Assert.assertTrue(resultingStack.contains("[wrapped] java.lang.RuntimeException:" + System.lineSeparator()));
		Assert.assertEquals(StringUtils.countMatches(resultingStack, "[wrapped] java.lang.RuntimeException:" + System.lineSeparator()), 2);
	}
	
	@Test
	public void tripleWrappedExcpetionTest() {
		Throwable testThrowable = null;
		try {
			try {
				throw new IOException("Your pipe is broken, sir.");
			} catch (Throwable t) {
				try {
					throw new IllegalArgumentException(t);
				} catch (Throwable t2) {
					throw new RuntimeException(t2);
				}
			}
		} catch (Throwable t) {
			testThrowable = t;
		}
		String resultingStack = StackTraceCompactor.getCompactStackTrace(testThrowable);
		
		//Verify that redundant exception messages have been removed
		Assert.assertTrue(resultingStack.contains("java.io.IOException: Your pipe is broken, sir." + System.lineSeparator()));
		Assert.assertTrue(resultingStack.contains("[wrapped] java.lang.IllegalArgumentException:" + System.lineSeparator()));
		Assert.assertTrue(resultingStack.contains("[wrapped] java.lang.RuntimeException:" + System.lineSeparator()));
	}
	
	@Test
	public void doubleWrappedExcpetionTest() {
		Throwable testThrowable = null;
		try {
			try {
				throw new IOException("Your pipe is broken, sir.");
			} catch (Throwable t) {
				try {
					throw new IllegalArgumentException("Bad file provided.", t);
				} catch (Throwable t2) {
					throw new RuntimeException(t2);
				}
			}
		} catch (Throwable t) {
			testThrowable = t;
		}
		String resultingStack = StackTraceCompactor.getCompactStackTrace(testThrowable);
		
		//Verify that redundant exception messages have been removed
		Assert.assertTrue(resultingStack.contains("java.io.IOException: Your pipe is broken, sir." + System.lineSeparator()));
		Assert.assertTrue(resultingStack.contains("[wrapped] java.lang.IllegalArgumentException: Bad file provided." + System.lineSeparator()));
		Assert.assertTrue(resultingStack.contains("[wrapped] java.lang.RuntimeException:" + System.lineSeparator()));
	}
	
	@Test
	public void singleWrappedExcpetionTest() {
		
		//Can't match the entire stack because it will change everytime a line of code is changed in this file
		String expectedStack = "java.io.IOException: Your pipe is broken, sir." + System.lineSeparator()
				+ "	at com.dbf.utils.stacktrace.StackTraceCompactorTest.singleWrappedExcpetionTest(";
		
		Throwable testThrowable = null;
		try {
			throw new IOException("Your pipe is broken, sir.");
		} catch (Throwable t) {
			testThrowable = t;
		}
		String resultingStack = StackTraceCompactor.getCompactStackTrace(testThrowable);	
		Assert.assertTrue(resultingStack.startsWith(expectedStack));
	}
	
	private static String combineStackElements(String[] stackElements) {
		StringBuilder sb = new StringBuilder();
		String seperator = "";
		for (String element : stackElements) {
			sb.append(seperator);
			sb.append(element);
			seperator = "\r\n";
		}
		return sb.toString();
	}
	
}
