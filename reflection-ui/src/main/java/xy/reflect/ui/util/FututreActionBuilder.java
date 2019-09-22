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

import java.util.HashMap;
import java.util.Map;

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
