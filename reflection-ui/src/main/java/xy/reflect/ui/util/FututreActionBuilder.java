


package xy.reflect.ui.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating template {@link Runnable} instances that can be
 * filled and run later.
 * 
 * @author olitank
 *
 */
public class FututreActionBuilder {

	protected Action currentAction;

	public Runnable will(FuturePerformance performance) {
		currentAction = new Action(performance);
		return currentAction;
	}

	public void setOption(String key, Object value) {
		if (currentAction == null) {
			throw new IllegalStateException("Cannot set action option: The performance was not specified");
		}
		currentAction.getOptions().put(key, value);
	}

	public void build() {
		if (currentAction == null) {
			throw new IllegalStateException("Cannot build action: The performance was not specified");
		}
		currentAction.setReady(true);
		currentAction = null;
	}

	protected class Action implements Runnable {

		protected boolean ready = false;
		protected FuturePerformance performance;
		protected Map<String, Object> options = new HashMap<String, Object>();

		public Action(FuturePerformance performance) {
			this.performance = performance;
		}

		public boolean isReady() {
			return ready;
		}

		public void setReady(boolean ready) {
			this.ready = ready;
		}

		public Map<String, Object> getOptions() {
			return options;
		}

		@Override
		public void run() {
			if (!ready) {
				throw new IllegalStateException("Cannot run action: Action creation is not finished");
			}
			performance.perform(options);
		}

	}

	public interface FuturePerformance {

		public void perform(Map<String, Object> options);

	}

}
