package xy.reflect.ui.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public abstract class DelayedUpdateProcess {

	protected abstract void commit();

	protected abstract long getCommitDelayMilliseconds();

	protected Object commitMutex = new Object();
	protected Future<?> dataUpdateTask;
	protected ExecutorService dataUpdateJobExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread result = new Thread(r);
			result.setName("DataUpdateJobExecutor [of=" + DelayedUpdateProcess.this + "]");
			result.setDaemon(true);
			return result;
		}
	});

	public void scheduleCommit() {
		synchronized (commitMutex) {
			dataUpdateTask = dataUpdateJobExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(getCommitDelayMilliseconds());
					} catch (InterruptedException e) {
						return;
					}
					commit();
				}
			});
		}
	}

	public void cancelCommitSchedule() {
		synchronized (commitMutex) {
			if (dataUpdateTask != null) {
				dataUpdateTask.cancel(true);
			}
		}
	}

}
