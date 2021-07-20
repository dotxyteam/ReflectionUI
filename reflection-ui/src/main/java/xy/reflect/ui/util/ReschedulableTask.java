


package xy.reflect.ui.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Base class for delayed tasks that will be executed 1 time (not more, not
 * less) after {@link #schedule()} is called. If the action is already scheduled
 * (can be checked with {@link #isScheduled()}) calling {@link #reschedule()}
 * again will cancel the current schedule (can be done with
 * {@link #cancelSchedule()}) and setup another one.
 * 
 * Note that scheduling attempts are synchronized and may be blocked if the
 * tasks duration are long and if the provided task execution service
 * ({@link #getTaskExecutor()}) is not able to process them in parallel.
 * 
 * @author olitank
 *
 */
public abstract class ReschedulableTask {

	protected abstract void execute();

	protected abstract long getExecutionDelayMilliseconds();

	protected abstract ExecutorService getTaskExecutor();

	protected Object executionMutex = new Object();
	protected Future<?> taskStatus;
	protected boolean executionScheduled = false;
	protected long executionScheduledSince = -1;

	public void schedule() {
		synchronized (executionMutex) {
			if (!executionScheduled) {
				boolean[] statusChanged = new boolean[] { false };
				taskStatus = getTaskExecutor().submit(new Runnable() {
					@Override
					public void run() {
						executionScheduled = true;
						executionScheduledSince = System.currentTimeMillis();
						statusChanged[0] = true;
						try {
							try {
								Thread.sleep(getExecutionDelayMilliseconds());
							} catch (InterruptedException e) {
								return;
							}
						} finally {
							executionScheduled = false;
							executionScheduledSince = -1;
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

	public long getMillisecondsToExecution() {
		long startTime = executionScheduledSince;
		if (startTime == -1) {
			return -1;
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		return getExecutionDelayMilliseconds() - elapsedTime;
	}

}
