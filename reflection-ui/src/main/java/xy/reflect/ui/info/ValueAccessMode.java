package xy.reflect.ui.info;

import xy.reflect.ui.util.ReflectionUIError;

public enum ValueAccessMode {
	SELF, PROXY, COPY;

	public static ValueAccessMode combine(ValueAccessMode parent, ValueAccessMode child) {
		if (parent == ValueAccessMode.SELF) {
			if (child == ValueAccessMode.SELF) {
				return ValueAccessMode.SELF;
			} else if (child == ValueAccessMode.PROXY) {
				return ValueAccessMode.PROXY;
			} else if (child == ValueAccessMode.COPY) {
				return ValueAccessMode.COPY;
			} else {
				throw new ReflectionUIError();
			}
		} else if (parent == ValueAccessMode.PROXY) {
			if (child == ValueAccessMode.SELF) {
				return ValueAccessMode.PROXY;
			} else if (child == ValueAccessMode.PROXY) {
				return ValueAccessMode.PROXY;
			} else if (child == ValueAccessMode.COPY) {
				return ValueAccessMode.COPY;
			} else {
				throw new ReflectionUIError();
			}
		} else if (parent == ValueAccessMode.COPY) {
			if (child == ValueAccessMode.SELF) {
				return ValueAccessMode.COPY;
			} else if (child == ValueAccessMode.PROXY) {
				return ValueAccessMode.COPY;
			} else if (child == ValueAccessMode.COPY) {
				return ValueAccessMode.COPY;
			} else {
				throw new ReflectionUIError();
			}
		} else {
			throw new ReflectionUIError();
		}
	}
}
