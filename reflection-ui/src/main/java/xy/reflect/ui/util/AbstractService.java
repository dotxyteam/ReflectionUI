/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.util;

public abstract class AbstractService {

	protected boolean active = false;
	protected boolean initialized = false;
	protected boolean verbose = false;
	protected Object lifeCycleMutex = new Object();

	protected abstract String getServiceName();

	protected abstract void setUp() throws Exception;

	protected abstract void triggerActivation() throws Exception;

	protected abstract void triggerInterruption() throws Exception;

	protected abstract void waitForCompleteInterruption();

	protected abstract void cleanUp() throws Exception;

	protected AbstractService() {
	}

	public Object getLifeCycleMutex() {
		return lifeCycleMutex;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	protected void logInfo(String message) {
		System.out.println(message);
	}

	protected void logError(String message) {
		System.err.println(message);
	}

	public final boolean isActive() {
		return active;
	}

	public final boolean isSetup() {
		return initialized;
	}

	public void start() {
		synchronized (getLifeCycleMutex()) {
			if (active) {
				return;
			}
			try {
				logInfo("Initializing " + getServiceName());
				setUp();
				initialized = true;
				logInfo("Starting " + getServiceName());
				triggerActivation();
				active = true;
			} catch (Throwable t) {
				try {
					triggerInterruption();
					waitForCompleteInterruption();
				} catch (Throwable ignore) {
				}
				try {
					cleanUp();
				} catch (Throwable ignore) {
				}
				handleInconsistentState();
				throw new RuntimeException(getServiceName() + " startup error: " + t.toString(), t);
			}
		}
	}

	public void stop() {
		synchronized (getLifeCycleMutex()) {
			if (!active) {
				return;
			}
			try {
				logInfo("Stopping " + getServiceName());
				triggerInterruption();
				waitForCompleteInterruption();
				active = false;
				logInfo("Cleaning up after " + getServiceName() + " interruption");
				cleanUp();
				initialized = false;
			} catch (Throwable t) {
				handleInconsistentState();
				throw new RuntimeException(getServiceName() + " shutdown error: " + t.toString(), t);
			}
		}
	}

	protected void handleRunErrorAndStop(Throwable t) {
		new Thread(AbstractService.this.getServiceName() + " Error Handler") {
			public void run() {
				try {
					AbstractService.this.stop();
				} catch (Throwable ignore) {
					handleInconsistentState();
				}
			}
		}.start();
		throw new RuntimeException(getServiceName() + " error", t);
	}

	public void withTemporaryInterruptionIfActive(Runnable runnable) {
		boolean wasActive = isActive();
		if (wasActive) {
			stop();
		}
		try {
			runnable.run();
		} finally {
			if (wasActive) {
				start();
			}
		}
	}

	protected void handleInconsistentState() {
		active = initialized = false;
	}

}
