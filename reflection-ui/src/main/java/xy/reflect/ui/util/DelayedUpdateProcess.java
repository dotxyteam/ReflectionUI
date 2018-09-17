package xy.reflect.ui.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class DelayedUpdateProcess {

	private long delayMilliseconds = 500;
	private boolean dirty = false;
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 1, 1L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {				
				@Override
				public Thread newThread(Runnable r) {
					return createThread(r);
				}
			});

	protected abstract void run();

	protected Thread createThread(Runnable r) {
		return new Thread(r, DelayedUpdateProcess.class.getSimpleName() + " Executor");
	}

	public long getDelayMilliseconds() {
		return delayMilliseconds;
	}

	public void setDelayMilliseconds(long delayMilliseconds) {
		this.delayMilliseconds = delayMilliseconds;
	}

	public void schedule() {
		dirty = true;
		if (executor.getQueue().size() > 0) {
			return;
		}
		executor.submit(new Runnable() {
			@Override
			public void run() {
				if (!dirty) {
					return;
				}
				try {
					Thread.sleep(delayMilliseconds);
				} catch (InterruptedException e) {
					throw new AssertionError(e);
				}
				dirty = false;
				DelayedUpdateProcess.this.run();
			}
		});
	}

}
