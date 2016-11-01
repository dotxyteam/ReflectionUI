package xy.reflect.ui.info;

import xy.reflect.ui.util.ReflectionUIError;

public enum ValueReturnMode {
	SELF, PROXY, COPY;

	public static ValueReturnMode combine(ValueReturnMode parent, ValueReturnMode child) {
		if (parent == ValueReturnMode.SELF) {
			if (child == ValueReturnMode.SELF) {
				return ValueReturnMode.SELF;
			} else if (child == ValueReturnMode.PROXY) {
				return ValueReturnMode.PROXY;
			} else if (child == ValueReturnMode.COPY) {
				return ValueReturnMode.COPY;
			} else {
				throw new ReflectionUIError();
			}
		} else if (parent == ValueReturnMode.PROXY) {
			if (child == ValueReturnMode.SELF) {
				return ValueReturnMode.PROXY;
			} else if (child == ValueReturnMode.PROXY) {
				return ValueReturnMode.PROXY;
			} else if (child == ValueReturnMode.COPY) {
				return ValueReturnMode.COPY;
			} else {
				throw new ReflectionUIError();
			}
		} else if (parent == ValueReturnMode.COPY) {
			if (child == ValueReturnMode.SELF) {
				return ValueReturnMode.COPY;
			} else if (child == ValueReturnMode.PROXY) {
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
