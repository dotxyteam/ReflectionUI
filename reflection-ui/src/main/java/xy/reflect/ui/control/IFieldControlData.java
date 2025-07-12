
package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;

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
	 * Behaves like {@link IFieldInfo#getValue(Object)} with a specific underlying
	 * object.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	Object getValue();

	/**
	 * Behaves like {@link IFieldInfo#setValue(Object, Object)} with a specific
	 * underlying object.
	 * 
	 * @param value Similar to the parameter of
	 *              {@link IFieldInfo#setValue(Object, Object)}.
	 */
	void setValue(Object value);

	/**
	 * Behaves like {@link IFieldInfo#getNextUpdateCustomUndoJob(Object, Object)}
	 * with a specific underlying object.
	 * 
	 * @param newValue Similar to the parameter of
	 *                 {@link IFieldInfo#getNextUpdateCustomUndoJob(Object, Object)}.
	 * @return the value corresponding to the behavior described above.
	 */
	Runnable getNextUpdateCustomUndoJob(Object newValue);

	/**
	 * Behaves like
	 * {@link IFieldInfo#getPreviousUpdateCustomRedoJob(Object, Object)} with a
	 * specific underlying object.
	 * 
	 * @param newValue Similar to the parameter of
	 *                 {@link IFieldInfo#getPreviousUpdateCustomRedoJob(Object, Object)}.
	 * @return the value corresponding to the behavior described above.
	 */
	Runnable getPreviousUpdateCustomRedoJob(Object newValue);

	/**
	 * Behaves like {@link IFieldInfo#getCaption()}. *
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	String getCaption();

	/**
	 * Behaves like {@link IFieldInfo#getOnlineHelp()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	String getOnlineHelp();

	/**
	 * Behaves like {@link IFieldInfo#getType()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	ITypeInfo getType();

	/**
	 * Behaves like {@link IFieldInfo#isGetOnly()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isGetOnly();

	/**
	 * Behaves like {@link IFieldInfo#isTransient()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isTransient();

	/**
	 * Behaves like {@link IFieldInfo#getValueReturnMode()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * Behaves like {@link IFieldInfo#isNullValueDistinct()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isNullValueDistinct();

	/**
	 * Behaves like {@link IFieldInfo#getNullValueLabel()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	String getNullValueLabel();

	/**
	 * Behaves like {@link IFieldInfo#isFormControlMandatory()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isFormControlMandatory();

	/**
	 * Behaves like {@link IFieldInfo#isFormControlEmbedded()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isFormControlEmbedded();

	/**
	 * Behaves like {@link IFieldInfo#getFormControlFilter()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	IInfoFilter getFormControlFilter();

	/**
	 * Behaves like {@link IFieldInfo#getSpecificProperties()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	Map<String, Object> getSpecificProperties();

	/**
	 * Behaves like {@link ITypeInfo#getLastFormRefreshStateRestorationJob(Object)}
	 * with a specific underlying object.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	Runnable getLastFormRefreshStateRestorationJob();

	/**
	 * Behaves like {@link IFieldInfo#isValueValidityDetectionEnabled()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isValueValidityDetectionEnabled();

	/**
	 * Behaves like {@link IFieldInfo#getValueAbstractFormValidationJob(Object)}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	IValidationJob getValueAbstractFormValidationJob();

	/**
	 * @return the text color that the control should use on its non-editable parts
	 *         or null if the default text color should be used.
	 */
	ColorSpecification getLabelForegroundColor();

	/**
	 * @return the resource location of a font object that the control should use on
	 *         its non-editable parts to display text or null if the default font
	 *         should be used.
	 */
	ResourcePath getLabelCustomFontResourcePath();

	/**
	 * @return the border color that the control should use or null if the default
	 *         border should be used.
	 */
	ColorSpecification getBorderColor();

	/**
	 * @return the background color that the control should use on its non-editable
	 *         parts or null if the default background color should be used. Note
	 *         that this color is intended to be used in combination with the
	 *         foreground color returned by
	 *         {@link #getNonEditableForegroundColor()}.
	 */
	ColorSpecification getNonEditableBackgroundColor();

	/**
	 * @return the text color that the control should use on its non-editable parts
	 *         or null if the default text color should be used. Note that this
	 *         color must be used in combination with the background color returned
	 *         by {@link #getNonEditableBackgroundColor()}.
	 */
	ColorSpecification getNonEditableForegroundColor();

	/**
	 * @return the background color that the control should use on its editable
	 *         parts or null if the default background color should be used. Note
	 *         that this color is intended to be used in combination with the
	 *         foreground color returned by {@link #getEditorForegroundColor()}.
	 */
	ColorSpecification getEditorBackgroundColor();

	/**
	 * @return the text color that the control should use on its editable parts or
	 *         null if the default text color should be used. Note that this color
	 *         must be used in combination with the background color returned by
	 *         {@link #getEditorBackgroundColor()}.
	 */
	ColorSpecification getEditorForegroundColor();

	/**
	 * @return the resource location of a font object that the control should use on
	 *         its editable parts to display text or null if the default font should
	 *         be used.
	 */
	ResourcePath getEditorCustomFontResourcePath();

	/**
	 * @return the resource location of an image that the buttons of the control
	 *         must use as their background image or null if the control buttons
	 *         must have their default background.
	 */
	ResourcePath getButtonBackgroundImagePath();

	/**
	 * @return the resource location of a font object that the buttons of the
	 *         control should use to display text or null if the control buttons
	 *         must use their default font.
	 */
	ResourcePath getButtonCustomFontResourcePath();

	/**
	 * @return the background color that the buttons of the control should use as
	 *         their background color or null if the control buttons must have their
	 *         default background color.
	 */
	ColorSpecification getButtonBackgroundColor();

	/**
	 * @return the text color that the buttons of the control should use as their
	 *         text color or null if the control buttons must have the default text
	 *         color.
	 */
	ColorSpecification getButtonForegroundColor();

	/**
	 * @return the border color that the buttons of the control should use as their
	 *         border color or null if the control buttons must have their default
	 *         border.
	 */
	ColorSpecification getButtonBorderColor();

}
