
package xy.reflect.ui.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for delayed tasks that will be executed 1 time (not more, not
 * less) after {@link #schedule()} is called. If the action is already scheduled
 * calling {@link #reschedule()} will cancel the current schedule (can be done
 * with {@link #cancelSchedule()}) and setup another one.
 * 
 * Note that scheduling attempts are synchronized and may be blocking if the
 * provided task execution service ({@link #getTaskExecutor()}) is not able to
 * process them in parallel.
 * 
 * @author olitank
 *
 */
public abstract class ReschedulableTask {

	protected abstract void execute();

	protected abstract long getExecutionDelayMilliseconds();

	protected abstract ExecutorService getTaskExecutor();

	protected final Object scheduleMutex = new Object();
	protected BetterFutureTask<Boolean> launchTask;
	protected long executionScheduledSince = -1;
	protected AtomicInteger activeWorkerCount = new AtomicInteger(0);

	public Object getExecutionMutex() {
		return scheduleMutex;
	}

	/**
	 * Schedules an execution if there isn't already a scheduled execution.
	 * 
	 * @return true if the execution was scheduled, or false if there is already a
	 *         scheduled execution.
	 */
	public boolean schedule() {
		synchronized (scheduleMutex) {
			if (launchTask == null) {
				final Semaphore launchTaskStartupNotification = new Semaphore(0);
				getTaskExecutor().submit(launchTask = new BetterFutureTask<Boolean>(new Runnable() {
					@Override
					public void run() {
						activeWorkerCount.incrementAndGet();
						try {
							try {
								launchTaskStartupNotification.release();
								executionScheduledSince = System.currentTimeMillis();
								try {
									try {
										Thread.sleep(getExecutionDelayMilliseconds());
									} catch (InterruptedException e) {
										return;
									}
								} finally {
									executionScheduledSince = -1;
								}
							} finally {
								launchTask = null;
							}
							execute();
						} finally {
							activeWorkerCount.decrementAndGet();
						}
					}
				}, true));
				try {
					launchTaskStartupNotification.acquire();
				} catch (InterruptedException e) {
					throw new ReflectionUIError(e);
				}
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Cancels the current execution schedule if there is one.
	 * 
	 * @return whether there was a scheduled execution.
	 */
	public boolean cancelSchedule() {
		synchronized (scheduleMutex) {
			if (launchTask != null) {
				try {
					launchTask.cancelAndWait(true);
				} catch (InterruptedException e) {
					throw new ReflectionUIError(e);
				}
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Cancels the current execution schedule if there is one, and creates a new
	 * one.
	 * 
	 * @return whether there was a scheduled execution.
	 */
	public boolean reschedule() {
		synchronized (scheduleMutex) {
			boolean wasScheduled = cancelSchedule();
			schedule();
			return wasScheduled;
		}
	}

	/**
	 * @return the remaining number of milliseconds before the execution or -1 if
	 *         there is no scheduled execution.
	 */
	public long getMillisecondsToExecution() {
		long startTime = executionScheduledSince;
		if (startTime == -1) {
			return -1;
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		return getExecutionDelayMilliseconds() - elapsedTime;
	}

	/**
	 * @return whether there are any ongoing or scheduled execution.
	 */
	public boolean isActive() {
		return activeWorkerCount.get() > 0;
	}
}
