package com.redhat.contentspec.client;

import com.redhat.contentspec.interfaces.ShutdownAbleApp;

/**
 * A class that allows a Shutdown able Application to have the shutdown intercepted and then shutdown using the applications shutdown method();
 * 
 * @author lnewson
 *
 */
public class ShutdownInterceptor extends Thread {

	private final ShutdownAbleApp app;
	private final long maxWaitTime;
	
	public ShutdownInterceptor(ShutdownAbleApp app) {
		this.app = app;
		this.maxWaitTime = 5000;
	}
	
	public ShutdownInterceptor(ShutdownAbleApp app, long maxWaitTime) {
		this.app = app;
		this.maxWaitTime = maxWaitTime;
	}
	
	@Override
	public void run() {
		long shutdownTime = System.currentTimeMillis() + maxWaitTime;
		app.shutdown();
		while(!app.isShutdown() && System.currentTimeMillis() <= shutdownTime) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
