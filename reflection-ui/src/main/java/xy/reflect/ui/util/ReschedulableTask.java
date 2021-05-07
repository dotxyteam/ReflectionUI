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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Base class for delayed tasks that will be executed 1 time (not more, not
 * less) after {@link #schedule()} is called. If the action is already scheduled
 * (can be checked with {@link #isScheduled()}) calling {@link #reschedule()}
 * again will cancel the current schedule (can be done with
 * {@link #cancelSchedule()}) and setup another one.
 * 
 * Limitation: All the task executions are performed by a single thread. Long
 * running tasks would then block all scheduling attempts while they are
 * running.
 * 
 * @author olitank
 *
 */
public abstract class ReschedulableTask {

	protected abstract void execute();

	protected abstract long getExecutionDelayMilliseconds();

	protected Object executionMutex = new Object();
	protected Future<?> taskStatus;
	protected ExecutorService taskExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread result = new Thread(r);
			result.setName("ReschedulableTaskExecutor [of=" + ReschedulableTask.this + "]");
			result.setDaemon(true);
			return result;
		}
	});
	protected boolean executionScheduled = false;

	public void schedule() {
		synchronized (executionMutex) {
			if (!executionScheduled) {
				boolean[] statusChanged = new boolean[] { false };
				taskStatus = taskExecutor.submit(new Runnable() {
					@Override
					public void run() {
						executionScheduled = true;
						statusChanged[0] = true;
						try {
							try {
								Thread.sleep(getExecutionDelayMilliseconds());
							} catch (InterruptedException e) {
								return;
							}
						} finally {
							executionScheduled = false;
						}
						execute();
					}
				});
				while (!statusChanged[0]) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						throw new ReflectionUIError(e);
					}
				}
			}
		}
	}

	public void cancelSchedule() {
		synchronized (executionMutex) {
			if (executionScheduled) {
				taskStatus.cancel(true);
				while (executionScheduled) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						throw new ReflectionUIError(e);
					}
				}
			}
		}
	}

	public boolean isScheduled() {
		return executionScheduled;
	}

	public void reschedule() {
		synchronized (executionMutex) {
			if (isScheduled()) {
				cancelSchedule();
			}
			schedule();
		}
	}

}
