package xy.reflect.ui.util;

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
