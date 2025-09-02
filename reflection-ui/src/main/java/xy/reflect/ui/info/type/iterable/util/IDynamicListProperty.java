
package xy.reflect.ui.info.type.iterable.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

/**
 * This interface allows to specify a list instance property that can be
 * accessed according to the current selection of items. Such a property will
 * typically be available on the list control tool bar.
 * 
 * The current selection is provided as the 1st parameter of
 * {@link IListTypeInfo#getDynamicProperties(List, xy.reflect.ui.util.Mapper)}.
 * 
 * Note that the owner object passed to {@link #setValue(Object, Object)} and
 * {@link #getValue(Object)} is {@link IDynamicListProperty#NO_OWNER}.
 * 
 * @author olitank
 *
 */
public interface IDynamicListProperty extends IDynamicListFeauture, IFieldInfo {

	public static final Object NO_OWNER = new Object() {

		@Override
		public String toString() {
			return IDynamicListProperty.class.getName() + ".NO_OWNER";
		}

	};

	public static final IDynamicListProperty NULL_DYNAMIC_LIST_PROPERTY = new IDynamicListProperty() {

		ITypeInfo type = new DefaultTypeInfo(ReflectionUI.getDefault(), new JavaTypeInfoSource(Object.class, null));

		@Override
		public String getName() {
			return "NULL_FIELD_INFO";
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public DisplayMode getDisplayMode() {
			return DisplayMode.TOOLBAR_AND_CONTEXT_MENU;
		}

		@Override
		public List<ItemPosition> getPostSelection() {
			return null;
		}

		@Override
		public IValidationJob getValueAbstractFormValidationJob(Object object) {
			return null;
		}

		@Override
		public boolean isControlValueValiditionEnabled() {
			return false;
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean visible) {
		}

		@Override
		public double getDisplayAreaHorizontalWeight() {
			return 1.0;
		}

		@Override
		public double getDisplayAreaVerticalWeight() {
			return 0.0;
		}

		@Override
		public boolean isDisplayAreaHorizontallyFilled() {
			return true;
		}

		@Override
		public boolean isDisplayAreaVerticallyFilled() {
			return false;
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public boolean isRelevant(Object object) {
			return true;
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public void setValue(Object object, Object value) {
		}

		@Override
		public boolean isGetOnly() {
			return true;
		}

		@Override
		public boolean isTransient() {
			return false;
		}

		@Override
		public boolean isNullValueDistinct() {
			return false;
		}

		@Override
		public String getNullValueLabel() {
			return null;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.INDETERMINATE;
		}

		@Override
		public Object getValue(Object object) {
			return null;
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			return null;
		}

		@Override
		public Runnable getPreviousUpdateCustomRedoJob(Object object, Object newValue) {
			return null;
		}

		@Override
		public boolean hasValueOptions(Object object) {
			return false;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

		@Override
		public ITypeInfo getType() {
			return type;
		}

		@Override
		public List<IMethodInfo> getAlternativeConstructors(Object object) {
			return null;
		}

		@Override
		public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
			return null;
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}

		@Override
		public boolean isFormControlMandatory() {
			return false;
		}

		@Override
		public boolean isFormControlEmbedded() {
			return false;
		}

		@Override
		public IInfoFilter getFormControlFilter() {
			return IInfoFilter.DEFAULT;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String toString() {
			return getName();
		}

		@Override
		public long getAutoUpdatePeriodMilliseconds() {
			return -1;
		}

	};

	/**
	 * @return the list of item positions that should be selected after the display
	 *         of the current property or null if the selection should not be
	 *         updated.
	 */
	List<ItemPosition> getPostSelection();

	/**
	 * @return whether the list property can be displayed or not.
	 */
	boolean isEnabled();

}
