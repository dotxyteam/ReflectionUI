


package xy.reflect.ui.control.plugin;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.RejectedFieldControlInputException;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface allows to specify a plugin that will provide a selectable
 * custom field control.
 * 
 * @author olitank
 *
 */
public interface IFieldControlPlugin {

	/**
	 * The key that references the name of the chosen field control plugin in the
	 * map returned by {@link ITypeInfo#getSpecificProperties()}.
	 */
	public String CHOSEN_PROPERTY_KEY = IFieldControlPlugin.class.getName() + ".CHOSEN";

	/**
	 * @param input The specification of the field to be displayed.
	 * @return whether the field described by the given input object can be
	 *         displayed by the current plugin control.
	 */
	boolean handles(IFieldControlInput input);

	/**
	 * @return whether the current plugin control can handle (display distinctly and
	 *         allow to set) the null value.
	 */
	boolean canDisplayDistinctNullValue();

	/**
	 * @param renderer The UI renderer object.
	 * @param input    The specification of the field to be displayed.
	 * @return A control able to display the field specified by the given input
	 *         object.
	 * @throws RejectedFieldControlInputException If it is discovered during the
	 *                                            creation that the input cannot be
	 *                                            handled.
	 */
	Object createControl(Object renderer, IFieldControlInput input) throws RejectedFieldControlInputException;

	/**
	 * @return words describing the current plugin.
	 */
	String getControlTitle();

	/**
	 * @return a string that uniquely identifies the current plugin.
	 */
	String getIdentifier();

	/**
	 * @param renderer    The UI renderer object.
	 * @param controlData The field control data allowing to handle the value of the
	 *                    field to be displayed.
	 * @return an output field control data corresponding to the input field control
	 *         data but optionally modified to handle conveniently null values.
	 *         Typically its type would have an additional zero-arg constructor
	 *         allowing to move smoothly from a null to a non-null field value. This
	 *         method is not used if {@link #canDisplayDistinctNullValue()} returns
	 *         true.
	 */
	IFieldControlData filterDistinctNullValueControlData(Object renderer, IFieldControlData controlData);
}
