package xy.reflect.ui.info.type;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

/**
 * This interface allows to specify UI-oriented attributes of objects typically
 * inferred from classes. It is a kind of abstraction of GUI forms.
 * 
 * @author olitank
 *
 */
public interface ITypeInfo extends IInfo {

	/**
	 * @return the source object from which this type was created.
	 */
	ITypeInfoSource getSource();

	/**
	 * @return true if and only if objects of this type can be persisted via
	 *         {@link #save(Object, OutputStream)} and
	 *         {@link #load(Object, InputStream)} methods.
	 */
	boolean canPersist();

	/**
	 * Saves the given object state to the output stream.
	 * 
	 * @param object
	 *            An object of the current type.
	 * @param out
	 *            The output stream.
	 */
	void save(Object object, OutputStream out);

	/**
	 * Loads the given object state from the input stream.
	 * 
	 * @param object
	 *            An object of the current type.
	 * @param in
	 *            The input stream.
	 */
	void load(Object object, InputStream in);

	/**
	 * @return true if and only if objects of the current type should be considered
	 *         as immutable.
	 */
	boolean isImmutable();

	/**
	 * @return true if and only if objects of the current type are primitive.
	 */
	boolean isPrimitive();

	/**
	 * @return true if and only if the current type is concrete. Otherwise the type
	 *         is considered as abstract and thus instances cannot be created from
	 *         it.
	 */
	boolean isConcrete();

	/**
	 * @return the list of constructors of this type.
	 */
	List<IMethodInfo> getConstructors();

	/**
	 * @return the list of fields of this type.
	 */
	List<IFieldInfo> getFields();

	/**
	 * @return the list of methods of this type.
	 */
	List<IMethodInfo> getMethods();

	/**
	 * @param object
	 *            Any object.
	 * @return whether the type is compatible with the given object.
	 */
	boolean supportsInstance(Object object);

	/**
	 * @return the list of known types derived from the current one. They must
	 *         support all the instances of the current type.
	 */
	List<ITypeInfo> getPolymorphicInstanceSubTypes();

	/**
	 * @param object
	 *            Any object of the current type.
	 * @return the textual representation of the given object. Typically this method
	 *         would allow to provide an alternative to the
	 *         {@link Object#toString()} method of the given object.
	 */
	String toString(Object object);

	/**
	 * Validates the state of the given object. An exception is thrown if the object
	 * state is not valid. Otherwise the object is considered as valid.
	 * 
	 * @param object
	 *            Any object of the current type.
	 * @throws Exception
	 *             If the state of the given object is not valid.
	 */
	void validate(Object object) throws Exception;

	/**
	 * @param object
	 *            Any object of the current type.
	 * @return true if and only if the given object can be duplicated. Otherwise the
	 *         {@link #copy(Object)} method should not be called.
	 */
	boolean canCopy(Object object);

	/**
	 * @param object
	 *            Any object of the current type.
	 * @return a copy of the given object.
	 */
	Object copy(Object object);

	/**
	 * @return true if and only if the undo/redo/etc features should be made
	 *         available typically when an object of the current type is the root of
	 *         a window.
	 */
	boolean isModificationStackAccessible();

	/**
	 * @return the location of an image resource associated with objects of the
	 *         current type or null.
	 */
	ResourcePath getIconImagePath();

	/**
	 * @return the layout strategy to be used when displaying the fields of objects
	 *         of the current type or null if the default layout strategy should be
	 *         used.
	 */
	ITypeInfo.FieldsLayout getFieldsLayout();

	/**
	 * @return the layout strategy to be used when displaying the methods of objects
	 *         of the current type or null if the default layout strategy should be
	 *         used.
	 */
	ITypeInfo.MethodsLayout getMethodsLayout();

	/**
	 * @return the descriptor of the menu bar elements provided by objects of the
	 *         current type.
	 */
	MenuModel getMenuModel();

	/**
	 * This method should be called by the renderer when the visibility of the form
	 * generated for the given object changes.
	 * 
	 * @param object
	 *            Any object of the current type.
	 * @param visible
	 *            true when the form becomes visible, false when it becomes
	 *            invisible.
	 * @return whether an update of the form is required.
	 */
	boolean onFormVisibilityChange(Object object, boolean visible);

	/**
	 * @return the preferred size (in pixels) of forms generated from this type.
	 */
	Dimension getFormPreferredSize();

	/**
	 * @return background color of forms generated from this type or null if the
	 *         default background color should be used.
	 */
	ColorSpecification getFormBackgroundColor();

	/**
	 * @return text color of forms generated from this type or null if the default
	 *         text color should be used.
	 */
	ColorSpecification getFormForegroundColor();

	/**
	 * @return the resource location of a background image displayed on forms
	 *         generated from this type.
	 */
	ResourcePath getFormBackgroundImagePath();

	/**
	 * @return the fields and methods categories display style.
	 */
	CategoriesStyle getCategoriesStyle();

	/**
	 * Fields layout strategy class.
	 * 
	 * @author olitank
	 *
	 */
	public enum FieldsLayout {
		HORIZONTAL_FLOW, VERTICAL_FLOW
	}

	/**
	 * Methods layout strategy class.
	 * 
	 * @author olitank
	 *
	 */
	public enum MethodsLayout {
		HORIZONTAL_FLOW, VERTICAL_FLOW
	}

	/**
	 * Fields and methods categories display style, subject to interpretation
	 * according to the renderer.
	 * 
	 * @author olitank
	 *
	 */
	public enum CategoriesStyle {
		CLASSIC, MODERN;

		public static CategoriesStyle getDefault() {
			return MODERN;
		}
	}

}
