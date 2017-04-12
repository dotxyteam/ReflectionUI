package xy.reflect.ui.info;

import java.util.Arrays;
import java.util.Collections;

public enum ValueReturnMode {
	DIRECT_OR_PROXY, CALCULATED, INDETERMINATE;

	public static ValueReturnMode combine(ValueReturnMode parent, ValueReturnMode child) {
		return Collections.max(Arrays.asList(parent, child));
	}
}
