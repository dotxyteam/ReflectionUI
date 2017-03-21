package xy.reflect.ui.info;

import xy.reflect.ui.util.ReflectionUIError;

public enum ValueReturnMode {
	DIRECT_OR_PROXY, INDETERMINATE, CALCULATED;

	public static ValueReturnMode combine(ValueReturnMode parent, ValueReturnMode child) {
		if (parent == ValueReturnMode.DIRECT_OR_PROXY) {
			if (child == ValueReturnMode.DIRECT_OR_PROXY) {
				return ValueReturnMode.DIRECT_OR_PROXY;
			} else if (child == ValueReturnMode.INDETERMINATE) {
				return ValueReturnMode.INDETERMINATE;
			} else if (child == ValueReturnMode.CALCULATED) {
				return ValueReturnMode.CALCULATED;
			} else {
				throw new ReflectionUIError();
			}
		} else if (parent == ValueReturnMode.INDETERMINATE) {
			if (child == ValueReturnMode.DIRECT_OR_PROXY) {
				return ValueReturnMode.INDETERMINATE;
			} else if (child == ValueReturnMode.INDETERMINATE) {
				return ValueReturnMode.INDETERMINATE;
			} else if (child == ValueReturnMode.CALCULATED) {
				return ValueReturnMode.CALCULATED;
			} else {
				throw new ReflectionUIError();
			}
		} else if (parent == ValueReturnMode.CALCULATED) {
			if (child == ValueReturnMode.DIRECT_OR_PROXY) {
				return ValueReturnMode.CALCULATED;
			} else if (child == ValueReturnMode.INDETERMINATE) {
				return ValueReturnMode.CALCULATED;
			} else if (child == ValueReturnMode.CALCULATED) {
				return ValueReturnMode.CALCULATED;
			} else {
				throw new ReflectionUIError();
			}
		} else {
			throw new ReflectionUIError();
		}
	}
}
