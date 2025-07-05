
package xy.reflect.ui.info.type;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ITransaction;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This interface allows to specify UI-oriented type information of objects.
 * This information is typically inferred from object classes.
 * 
 * @author olitank
 *
 */
public interface ITypeInfo extends IInfo {

	/**
	 * Dummy instance of this class made for utilitarian purposes.
	 */
	public static ITypeInfo NULL_BASIC_TYPE_INFO = new ITypeInfo() {

		@Override
		public String toString() {
			return "NULL_BASIC_TYPE_INFO";
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getName() {
			return "NULL_BASIC_TYPE_INFO";
		}

		@Override
		public String getCaption() {
			return ReflectionUIUtils.identifierToCaption(getName());
		}

		@Override
		public void validate(Object object, ValidationSession session) throws Exception {
		}

		@Override
		public String toString(Object object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean supports(Object object) {
			return true;
		}

		@Override
		public boolean onFormVisibilityChange(Object object, boolean visible) {
			return false;
		}

		@Override
		public void save(Object object, File outputFile) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void load(Object object, File inputFile) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isModificationStackAccessible() {
			return true;
		}

		@Override
		public boolean isImmutable() {
			return false;
		}

		@Override
		public boolean isConcrete() {
			return false;
		}

		@Override
		public ITransaction createTransaction(Object object) {
			return null;
		}

		@Override
		public void onFormRefresh(Object object) {
		}

		@Override
		public Runnable getLastFormRefreshStateRestorationJob(Object object) {
			return null;
		}

		@Override
		public ITypeInfoSource getSource() {
			return ITypeInfoSource.NULL_TYPE_INFO_SOURCE;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public MethodsLayout getMethodsLayout() {
			return MethodsLayout.HORIZONTAL_FLOW;
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public MenuModel getMenuModel() {
			return new MenuModel();
		}

		@Override
		public ResourcePath getIconImagePath(Object object) {
			return null;
		}

		@Override
		public int getFormSpacing() {
			return ITypeInfo.DEFAULT_FORM_SPACING;
		}

		@Override
		public int getFormPreferredWidth() {
			return -1;
		}

		@Override
		public int getFormPreferredHeight() {
			return -1;
		}

		@Override
		public ColorSpecification getFormForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormButtonForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormButtonBorderColor() {
			return null;
		}

		@Override
		public ResourcePath getFormButtonBackgroundImagePath() {
			return null;
		}

		@Override
		public ColorSpecification getFormButtonBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormBorderColor() {
			return null;
		}

		@Override
		public ResourcePath getFormBackgroundImagePath() {
			return null;
		}

		@Override
		public ColorSpecification getFormBackgroundColor() {
			return null;
		}

		@Override
		public FieldsLayout getFieldsLayout() {
			return FieldsLayout.VERTICAL_FLOW;
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.emptyList();
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.emptyList();
		}

		@Override
		public CategoriesStyle getCategoriesStyle() {
			return CategoriesStyle.getDefault();
		}

		@Override
		public ColorSpecification getCategoriesForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getCategoriesBackgroundColor() {
			return null;
		}

		@Override
		public Object copy(Object object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean canPersist() {
			return false;
		}

		@Override
		public boolean canCopy(Object object) {
			return false;
		}

		@Override
		public boolean isValidationRequired() {
			return false;
		}

	};

	public int DEFAULT_FORM_SPACING = 10;

	/**
	 * @return true if and only if successful validation is required to authorize
	 *         the agreement of modifications for objects of this type.
	 */
	boolean isValidationRequired();

	/**
	 * @param object Any object of the current type.
	 * @return a transaction manager for the given instance.
	 */
	ITransaction createTransaction(Object object);

	/**
	 * This method is called when a form displaying the given object gets refreshed.
	 * 
	 * @param object Any object of the current type.
	 */
	void onFormRefresh(Object object);

	/**
	 * @param object Any object of the current type.
	 * @return a job (may be null) that restores the state of the given object when
	 *         its form was last refreshed.
	 */
	Runnable getLastFormRefreshStateRestorationJob(Object object);

	/**
	 * @return the source object from which this type information was created.
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
	 * @param object     An object of the current type.
	 * @param outputFile The output file.
	 */
	void save(Object object, File outputFile);

	/**
	 * Loads the given object state from the input stream.
	 * 
	 * @param object    An object of the current type.
	 * @param inputFile The input file.
	 */
	void load(Object object, File inputFile);

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
	 * @param object Any object or null.
	 * @return whether the current type is compatible with the given object. If null
	 *         is passed it returns whether fields/parameters/... of the current
	 *         type support null.
	 */
	boolean supports(Object object);

	/**
	 * @return the list of known types derived from the current one.
	 */
	List<ITypeInfo> getPolymorphicInstanceSubTypes();

	/**
	 * @param object Any object of the current type (not null).
	 * @return the textual representation of the given object. This method allows to
	 *         provide an alternative to the {@link Object#toString()} method of the
	 *         given object for generated UIs.
	 */
	String toString(Object object);

	/**
	 * Validates the state of the given object. An exception is thrown if the object
	 * state is not valid. Otherwise the object is considered as valid. Note that
	 * this method is executed concurrently by a validation thread while the given
	 * object is possibly accessed/modified by another thread.
	 * 
	 * @param object  Any object of the current type.
	 * @param session The current validation session object.
	 * @throws Exception If the state of the given object is not valid.
	 */
	void validate(Object object, ValidationSession session) throws Exception;

	/**
	 * @param object Any object of the current type.
	 * @return true if and only if the given object can be duplicated. Otherwise the
	 *         {@link #copy(Object)} method should not be called.
	 */
	boolean canCopy(Object object);

	/**
	 * @param object Any object of the current type.
	 * @return a copy of the given object.
	 */
	Object copy(Object object);

	/**
	 * @return true if and only if the undo/redo/etc features should be made
	 *         available typically when an object of the current type is the root
	 *         one displayed in a window.
	 */
	boolean isModificationStackAccessible();

	/**
	 * @param object Any object of the current type or null.
	 * @return null or the location of an icon image resource that represents
	 *         specifically the object passed as parameter, or all objects of the
	 *         current type when null is passed as parameter.
	 */
	ResourcePath getIconImagePath(Object object);

	/**
	 * @return the layout strategy to be used when displaying the fields of objects
	 *         of the current type (or null if the default layout strategy should be
	 *         used).
	 */
	ITypeInfo.FieldsLayout getFieldsLayout();

	/**
	 * @return the layout strategy to be used when displaying the methods of objects
	 *         of the current type (or null if the default layout strategy should be
	 *         used).
	 */
	ITypeInfo.MethodsLayout getMethodsLayout();

	/**
	 * @return the descriptor of menu bar elements provided for objects of the
	 *         current type.
	 */
	MenuModel getMenuModel();

	/**
	 * This method is called by the renderer when the visibility of the form
	 * generated for the given object changes.
	 * 
	 * @param object  Any object of the current type.
	 * @param visible Is true when the form becomes visible, false when it becomes
	 *                invisible.
	 * @return whether an update of the form is required.
	 */
	boolean onFormVisibilityChange(Object object, boolean visible);

	/**
	 * @return the preferred width (in pixels) of forms generated from this type or
	 *         -1 if a default width should be used.
	 */
	int getFormPreferredWidth();

	/**
	 * @return the preferred height (in pixels) of forms generated from this type or
	 *         -1 if a default height should be used.
	 */
	int getFormPreferredHeight();

	/**
	 * @return the space (in pixels) between elements in forms generated from this
	 *         type.
	 */
	int getFormSpacing();

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
	 * @return border color used by forms generated from this type or null if the
	 *         default borders should be used.
	 */
	ColorSpecification getFormBorderColor();

	/**
	 * @return the resource location of a background image displayed on forms
	 *         generated from this type or null if no background image should be
	 *         used.
	 */
	ResourcePath getFormBackgroundImagePath();

	/**
	 * @return the display style of the member categories control.
	 */
	CategoriesStyle getCategoriesStyle();

	/**
	 * @return the background color of the categories control generated from this
	 *         type or null if the default background color should be used.
	 */
	ColorSpecification getCategoriesBackgroundColor();

	/**
	 * @return the text color of the categories control generated from this type or
	 *         null if the default text color should be used.
	 */
	ColorSpecification getCategoriesForegroundColor();

	/**
	 * @return the editable text color of controls generated from this type or null
	 *         if the default text color should be used.
	 */
	ColorSpecification getFormEditorForegroundColor();

	/**
	 * @return the editable background color of controls generated from this type or
	 *         null if the default background color should be used.
	 */
	ColorSpecification getFormEditorBackgroundColor();

	/**
	 * @return the background color of the buttons generated from this type or null
	 *         if the default background color should be used.
	 */
	ColorSpecification getFormButtonBackgroundColor();

	/**
	 * @return the text color of the buttons generated from this type or null if the
	 *         default text color should be used.
	 */
	ColorSpecification getFormButtonForegroundColor();

	/**
	 * @return the resource path to a background image for the buttons generated
	 *         from this type or null if no background image should be used.
	 */
	ResourcePath getFormButtonBackgroundImagePath();

	/**
	 * @return the border color of the buttons generated from this type or null if
	 *         the default border should be used.
	 */
	ColorSpecification getFormButtonBorderColor();

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
	 * Display style of member categories control, subject to interpretation
	 * according to the renderer.
	 * 
	 * @author olitank
	 *
	 */
	public enum CategoriesStyle {
		STANDARD, STANDARD_VERTICAL, CLASSIC, CLASSIC_VERTICAL, MODERN, MODERN_VERTICAL;

		public static CategoriesStyle getDefault() {
			return STANDARD;
		}
	}

}
