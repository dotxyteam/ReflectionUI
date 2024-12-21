package xy.reflect.ui.info.type.iterable.util;

/**
 * This interface is the base type of list features that can be accessed
 * according to the current selection of items. Such features will typically be
 * available on the list control tool bar.
 * 
 * @author olitank
 *
 */
public interface IDynamicListFeauture {

	public enum DisplayMode {
		TOOLBAR, CONTEXT_MENU, TOOLBAR_AND_CONTEXT_MENU;
		
		public static DisplayMode getDefault() {
			return TOOLBAR_AND_CONTEXT_MENU;
		}
	}

	/**
	 * @return a value specifying where the feature should be made available.
	 */
	DisplayMode getDisplayMode();

}
