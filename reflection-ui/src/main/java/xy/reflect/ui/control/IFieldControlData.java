


package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface provides what field controls need to look and behave properly.
 * 
 * It is intended to be a proxy of {@link IFieldInfo} that hides pieces of
 * information that are useless for field controls in order to maximize their
 * reusability. Typically it hides the field owner object. <br>
 * <br>
 * About colors:
 * <ul>
 * <li>There are 4 colors that controls must take into account:</li>
 * <ul>
 * <li>label foreground color (usually the color of a non-editable text
 * describing the control)</li>
 * <li>editor background color (usually the background color of an editable
 * text)</li>
 * <li>editor foreground color (usually the color of an editable text)</li>
 * <li>border color
 * </ul>
 * <li>Note that the controls have usually 2 states that must be distinctly
 * displayed while respecting the above colors:</li>
 * <ul>
 * <li>editable (typically when {@link #isGetOnly()} returns false)</li>
 * <li>not editable (typically when {@link #isGetOnly()} returns true)</li>
 * </ul>
 * <li>In case the control cannot conform to the specified colors, it is
 * tolerated that it gets its natural colors</li>
 * </ul>
 * 
 * @author olitank
 *
 */
public interface IFieldControlData {

	/**
	 * Retrieves the underlying field value.
	 * 
	 * Note that null may be returned (even if {@link #isNullValueDistinct()}
	 * returns false). All field controls must then support null (not crash). If
	 * null is returned and {@link #isNullValueDistinct()} returns true then the
	 * null value must be distinctly displayed (not like a default non-null value).
	 * 
	 * @return the value that the control must display.
	 */
	Object getValue();

	/**
	 * Updates the underlying field with the provided value.
	 * 
	 * @param value The new value.
	 */
	void setValue(Object value);

	/**
	 * @param newValue The new value.
	 * @return a job that can revert the next value update or null if the default
	 *         undo job should be used.
	 */
	Runnable getNextUpdateCustomUndoJob(Object newValue);

	/**
	 * @return the name that the field control must display.
	 */
	String getCaption();

	/**
	 * @return the help text of the field control.
	 */
	String getOnlineHelp();

	/**
	 * @return the type information of the underlying field.
	 */
	ITypeInfo getType();

	/**
	 * @return false if and only if the control value can be set. Otherwise
	 *         {@link #setValue(Object)} should not be called.
	 */
	boolean isGetOnly();

	/**
	 * @return true if and only if this control value update should not be stored in
	 *         a modification stack.
	 */
	boolean isTransient();

	/**
	 * @return the value return mode of this control data. It may impact the
	 *         behavior of the control.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * @return true if and only if the control must distinctly display and allow to
	 *         set the null value. This is usually needed if a null value has a
	 *         special meaning different from "empty/default value" for the
	 *         developer. Note that the null value may be returned by
	 *         {@link #getValue()} even if it is not required to be distinctly
	 *         displayed (false returned by the current method).
	 */
	boolean isNullValueDistinct();

	/**
	 * @return a text that should be displayed by the control to describe the null
	 *         value.
	 */
	String getNullValueLabel();

	/**
	 * @return true if the control associated with this field control data must be a
	 *         generic form. If false is returned then a custom control may be
	 *         displayed. Note that the form is either embedded in the parent form
	 *         or displayed in a child dialog according to the return value of
	 *         {@link #isFormControlEmbedded()}.
	 */
	boolean isFormControlMandatory();

	/**
	 * @return whether the generic form associated with this field control data is
	 *         embedded in the parent form or displayed in a child dialog. Note that
	 *         this method has no impact if a custom control is displayed instead of
	 *         a generic form.
	 */
	boolean isFormControlEmbedded();

	/**
	 * @return an object used to filter out some fields and methods from the generic
	 *         form associated with this field control data. Note that this method
	 *         has no impact if a custom control is displayed instead of a generic
	 *         form.
	 */
	IInfoFilter getFormControlFilter();

	/**
	 * @return custom properties intended to be used to extend the this field
	 *         control data for specific renderers.
	 */
	Map<String, Object> getSpecificProperties();

	/**
	 * @return the text color that the control must use on its non-editable parts or
	 *         null if the default text color should be used.
	 */
	ColorSpecification getLabelForegroundColor();

	/**
	 * @return the border color that the control must use or null if the default
	 *         border should be used.
	 */
	ColorSpecification getBorderColor();

	/**
	 * @return the background color that the control must use on its editable parts
	 *         or null if the default background color should be used. Note that
	 *         this color is intended to be used in combination with the foreground
	 *         color returned by {@link #getEditorForegroundColor()}.
	 */
	ColorSpecification getEditorBackgroundColor();

	/**
	 * @return the text color that the control must use on its editable parts or
	 *         null if the default text color should be used. Note that this color
	 *         must be used in combination with the background color returned by
	 *         {@link #getEditorBackgroundColor()}.
	 */
	ColorSpecification getEditorForegroundColor();

	/**
	 * @return the resource location of an image that the buttons of the control
	 *         must use as their background image or null if the control buttons
	 *         must have their default background.
	 */
	ResourcePath getButtonBackgroundImagePath();

	/**
	 * @return the background color that the buttons of the control must use as
	 *         their background color or null if the control buttons must have their
	 *         default background color.
	 */
	ColorSpecification getButtonBackgroundColor();

	/**
	 * @return the text color that the buttons of the control must use as their text
	 *         color or null if the control buttons must have the default text
	 *         color.
	 */
	ColorSpecification getButtonForegroundColor();

	/**
	 * @return the border color that the buttons of the control must use as their
	 *         border color or null if the control buttons must have their default
	 *         border.
	 */
	ColorSpecification getButtonBorderColor();

}
