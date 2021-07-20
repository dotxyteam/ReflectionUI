


package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This interface allows to specify UI-oriented field properties.
 * 
 * @author olitank
 *
 */
public interface IFieldInfo extends IInfo {

	/**
	 * Dummy instance of this class made for utilitarian purposes.
	 */
	public IFieldInfo NULL_FIELD_INFO = new IFieldInfo() {

		ITypeInfo type = new DefaultTypeInfo(
				new JavaTypeInfoSource(ReflectionUIUtils.STANDARD_REFLECTION, Object.class, null));

		@Override
		public String getName() {
			return "NULL_FIELD_INFO";
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
			return 1.0;
		}

		@Override
		public boolean isHidden() {
			return false;
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
			return "NULL_FIELD_INFO";
		}

		@Override
		public long getAutoUpdatePeriodMilliseconds() {
			return -1;
		}

	};

	/**
	 * @return the type information of the current field.
	 */
	ITypeInfo getType();

	/**
	 * @param object The object hosting the field value or null if the field is
	 *               static.
	 * @return the value of this field extracted from the given object.
	 */
	Object getValue(Object object);

	/**
	 * @param object The object hosting the field value or null if the field is
	 *               static.
	 * @return whether this field value has options. If the return value is 'true'
	 *         then the {@link #getValueOptions(Object)} method must not return
	 *         null.
	 */
	boolean hasValueOptions(Object object);

	/**
	 * @param object The object hosting the field value or null if the field is
	 *               static.
	 * @return options for the value of this field. If the
	 *         {@link #hasValueOptions(Object)} method return value is 'true' then
	 *         this method must not return null.
	 */
	Object[] getValueOptions(Object object);

	/**
	 * Updates the current field of the given object with the given value.
	 * 
	 * @param object The object hosting the field value or null if the field is
	 *               static.
	 * @param value  The new field value.
	 */
	void setValue(Object object, Object value);

	/**
	 * @param object   The object hosting the field value or null if the field is
	 *                 static.
	 * @param newValue The new field value.
	 * @return a job that can revert the next field value update or null if the
	 *         default undo job should be used.
	 */
	Runnable getNextUpdateCustomUndoJob(Object object, Object newValue);

	/**
	 * @param object The object hosting the field value or null if the field is
	 *               static.
	 * @return a list of constructors that must be used (if non-null) to create a
	 *         new item instead of those returned by the call of
	 *         {@linkplain ITypeInfo#getConstructors()} on the result of the call of
	 *         {@link #getType()}.
	 */
	List<IMethodInfo> getAlternativeConstructors(Object object);

	/**
	 * @param object The object hosting the field value or null if the field is
	 *               static.
	 * @return a list of constructors that must be used (if non-null) to create a
	 *         new item when the current field is a list field ({@link #getType()}
	 *         instanceof {@link IListTypeInfo}) instead of those returned by the
	 *         call of {@linkplain ITypeInfo#getConstructors()} on the result of the
	 *         call of {@link IListTypeInfo#getItemType()} on the result of the call
	 *         of {@link #getType()}.
	 */
	List<IMethodInfo> getAlternativeListItemConstructors(Object object);

	/**
	 * @return true if and only if this field control must distinctly display and
	 *         allow to set the null value. This is usually needed if a null value
	 *         has a special meaning different from "empty/default value" for the
	 *         developer. Note that the null value may be returned by
	 *         {@link #getValue(Object)} even if it is not required to be distinctly
	 *         displayed (false returned by the current method).
	 */
	boolean isNullValueDistinct();

	/**
	 * @return false if and only if this field value can be set. Otherwise
	 *         {@link #setValue(Object, Object)} should not be called. Note that a
	 *         get-only field does not prevent all modifications. The field value
	 *         may be modified and these modifications may be volatile (for
	 *         calculated values, copies, ..) or persistent even if the new field
	 *         value is not set.
	 */
	boolean isGetOnly();

	/**
	 * @return true if and only if this field value update should not be stored in a
	 *         modification stack (in order to be reverted).
	 */
	boolean isTransient();

	/**
	 * @return a text that should be displayed by the field control to describe the
	 *         null value.
	 */
	String getNullValueLabel();

	/**
	 * @return the value return mode of this field. It may impact the behavior of
	 *         the field control.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * @return the category in which this field will be displayed.
	 */
	InfoCategory getCategory();

	/**
	 * @return true if this field value must be displayed as a generic form. If
	 *         false is returned then a custom control may be displayed. Note that
	 *         the form to display is either embedded in the current window or
	 *         displayed in a child dialog according to the return value of
	 *         {@link #isFormControlEmbedded()}.
	 */
	boolean isFormControlMandatory();

	/**
	 * @return whether this field value form is embedded in the current window or
	 *         displayed in a child dialog. Note that this method has no impact if a
	 *         custom control is displayed instead of a generic form.
	 */
	boolean isFormControlEmbedded();

	/**
	 * @return an object used to filter out some fields and methods from this field
	 *         value form. Note that this method has no impact if a custom control
	 *         is displayed instead of a generic form.
	 */
	IInfoFilter getFormControlFilter();

	/**
	 * @return the automatic update period (in milliseconds) that the field control
	 *         will try to respect.-1 means that there is no automatic update and 0
	 *         means that the update occurs as fast as possible.
	 */
	long getAutoUpdatePeriodMilliseconds();

	/**
	 * @return true if and only if the control of this field is filtered out from
	 *         the display.
	 */
	boolean isHidden();

	/**
	 * @return a number that specifies how to distribute extra horizontal space
	 *         between sibling field controls. If the resulting layout is smaller
	 *         horizontally than the area it needs to fill, the extra space is
	 *         distributed to each field in proportion to its horizontal weight. A
	 *         field that has a weight of zero receives no extra space. If all the
	 *         weights are zero, all the extra space appears between the grids of
	 *         the cell and the left and right edges. It should be a non-negative
	 *         value.
	 */
	double getDisplayAreaHorizontalWeight();

	/**
	 * @return a number that specifies how to distribute extra vertical space
	 *         between sibling field controls. If the resulting layout is smaller
	 *         vertically than the area it needs to fill, the extra space is
	 *         distributed to each field in proportion to its vertical weight. A
	 *         field that has a weight of zero receives no extra space. If all the
	 *         weights are zero, all the extra space appears between the grids of
	 *         the cell and the left and right edges. It should be a non-negative
	 *         value.
	 * 
	 */
	double getDisplayAreaVerticalWeight();

	/**
	 * This method is called by the renderer when the visibility of the field
	 * control changes for the given object in the generated UI.
	 * 
	 * @param object  The object hosting the field value or null if the field is
	 *                static.
	 * @param visible true when the field becomes visible, false when it becomes
	 *                invisible.
	 */
	void onControlVisibilityChange(Object object, boolean visible);

}
