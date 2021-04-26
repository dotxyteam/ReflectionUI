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

/**
 * Base class for asynchronous jobs that can started (or stopped) to perform a
 * periodic task.
 * 
 * @author olitank
 *
 */
public abstract class AbstractPollingService extends AbstractService {

	private Thread thread;
	private boolean interruptionRequested = false;

	protected abstract boolean doPollingAction();

	protected abstract long getPeriodicSleepDurationMilliseconds();

	public boolean isInterruptionRequested() {
		return interruptionRequested;
	}

	@Override
	final public void start() {
		reset();
		super.start();
	}

	@Override
	final public void stop() {
		super.stop();
	}

	protected void reset() {
	}

	protected void sleepBetweenPeriods() {
		if (interruptionRequested) {
			return;
		}
		long sleepDurationMs = getPeriodicSleepDurationMilliseconds();
		if (sleepDurationMs > 0) {
			try {
				Thread.sleep(sleepDurationMs);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	protected void setUp() throws Exception {
		interruptionRequested = false;
		thread = new Thread(getServiceName()) {

			@Override
			public void run() {
				while (!interruptionRequested) {
					try {
						if (!doPollingAction()) {
							if (!interruptionRequested) {
								synchronized (getLifeCycleMutex()) {
									logInfo("Cleaning up after " + getServiceName() + " self interruption");
									active = false;
									try {
										cleanUp();
										initialized = false;
									} catch (Throwable t) {
										handleInconsistentState();
										throw new RuntimeException(
												getServiceName() + " self interruption error: " + t.toString(), t);
									}
								}
								break;
							}
						}
					} catch (final Throwable t) {
						handleRunErrorAndStop(t);
					}
					sleepBetweenPeriods();
				}
			}

		};
		configureThread(thread);
	}

	protected void configureThread(Thread thread) {
		thread.setDaemon(true);
	}

	@Override
	final protected void triggerActivation() throws Exception {
		thread.start();
	}

	@Override
	final protected void triggerInterruption() throws Exception {
		interruptionRequested = true;
	}

	@Override
	final protected void waitForCompleteInterruption() {
		try {
			thread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void cleanUp() throws Exception {
		thread = null;
		interruptionRequested = false;
	}

}
