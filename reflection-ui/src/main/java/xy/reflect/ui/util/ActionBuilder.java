package xy.reflect.ui.util;

import java.util.HashMap;
import java.util.Map;

public class ActionBuilder {

	protected Action currentAction;

	public Runnable begin(Performer performer) {
		currentAction = new Action(performer);
		return currentAction;
	}

	public void setOption(String key, Object value) {
		checkCurrentAction();
		currentAction.getOptions().put(key, value);
	}

	public void end() {
		checkCurrentAction();
		currentAction.setReady(true);
		currentAction = null;
	}

	protected void checkCurrentAction() {
		if (currentAction == null) {
			throw new IllegalStateException("Cannot end action creation: Did not begin");
		}
	}

	protected class Action implements Runnable {

		protected boolean ready = false;
		protected Performer performer;
		protected Map<String, Object> options = new HashMap<String, Object>();

		public Action(Performer performer) {
			this.performer = performer;
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
			performer.perform(options);
		}

	}

	public interface Performer {

		public void perform(Map<String, Object> options);

	}

}
