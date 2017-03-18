package xy.reflect.ui.info;

import xy.reflect.ui.util.ReflectionUIError;

public enum ValueReturnMode {
	SELF_OR_PROXY, INDETERMINATE, COPY;

	public static ValueReturnMode combine(ValueReturnMode parent, ValueReturnMode child) {
		if (parent == ValueReturnMode.SELF_OR_PROXY) {
			if (child == ValueReturnMode.SELF_OR_PROXY) {
				return ValueReturnMode.SELF_OR_PROXY;
			} else if (child == ValueReturnMode.INDETERMINATE) {
				return ValueReturnMode.INDETERMINATE;
			} else if (child == ValueReturnMode.COPY) {
				return ValueReturnMode.COPY;
			} else {
				throw new ReflectionUIError();
			}
		} else if (parent == ValueReturnMode.INDETERMINATE) {
			if (child == ValueReturnMode.SELF_OR_PROXY) {
				return ValueReturnMode.INDETERMINATE;
			} else if (child == ValueReturnMode.INDETERMINATE) {
				return ValueReturnMode.INDETERMINATE;
			} else if (child == ValueReturnMode.COPY) {
				return ValueReturnMode.COPY;
			} else {
				throw new ReflectionUIError();
			}
		} else if (parent == ValueReturnMode.COPY) {
			if (child == ValueReturnMode.SELF_OR_PROXY) {
				return ValueReturnMode.COPY;
			} else if (child == ValueReturnMode.INDETERMINATE) {
				return ValueReturnMode.COPY;
			} else if (child == ValueReturnMode.COPY) {
				return ValueReturnMode.COPY;
			} else {
				throw new ReflectionUIError();
			}
		} else {
			throw new ReflectionUIError();
		}
	}
}
