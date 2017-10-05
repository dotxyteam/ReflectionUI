package xy.reflect.ui.util;

public abstract class DelayedUpdateProcess {

	private boolean updateActionInQueue;
	private Object updateMutex = new Object();
	private long delayMilliseconds = 500;

	protected abstract void run();

	public long getDelayMilliseconds() {
		return delayMilliseconds;
	}

	public void setDelayMilliseconds(long delayMilliseconds) {
		this.delayMilliseconds = delayMilliseconds;
	}

	public void schedule() {
		if (updateActionInQueue) {
			return;
		}
		updateActionInQueue = true;
		new Thread("DelayedUpdateProcessor[of=" + DelayedUpdateProcess.this + "]") {
			@Override
			public void run() {
				synchronized (updateMutex) {
					try {
						sleep(delayMilliseconds);
					} catch (InterruptedException e) {
						throw new AssertionError(e);
					}
					updateActionInQueue = false;
					DelayedUpdateProcess.this.run();
				}
			}
		}.start();
	}

}
