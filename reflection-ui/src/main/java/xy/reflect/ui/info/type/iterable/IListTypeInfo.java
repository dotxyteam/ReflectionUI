
package xy.reflect.ui.info.type.iterable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransaction;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This interface allows to specify UI-oriented properties of list types.
 * 
 * Generating UIs for lists is complex because there are multiple
 * implementations (arrays, collections, maps, ...) and not enough conventions.
 * A list can then be ordered or not, unmodifiable or not, supporting null items
 * or not, etc. This interface allows to describe all sorts of lists so that an
 * aware renderer will be able to display and allow as much as possible to edit
 * them all.
 * 
 * Structural preferences such as tabular or hierarchical facets of lists are
 * also supported via the {@link IListStructuralInfo} interface.
 * 
 * @author olitank
 *
 */
public interface IListTypeInfo extends ITypeInfo {

	/**
	 * Dummy instance of this class made for utilitarian purposes.
	 */
	public static IListTypeInfo NULL_LIST_TYPE_INFO = new IListTypeInfo() {

		@Override
		public String toString() {
			return "NULL_LIST_TYPE_INFO";
		}

		@Override
		public String getName() {
			return "NULL_LIST_TYPE_INFO";
		}

		@Override
		public ITypeInfo getItemType() {
			return null;
		}

		@Override
		public Object[] toArray(Object listValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IValidationJob getListItemAbstractFormValidationJob(ItemPosition itemPosition) {
			return null;
		}

		@Override
		public boolean isItemNodeValidityDetectionEnabled(ItemPosition itemPosition) {
			return true;
		}

		@Override
		public boolean areItemsAutomaticallyPositioned() {
			return false;
		}

		@Override
		public boolean canInstantiateFromArray() {
			return false;
		}

		@Override
		public Object fromArray(Object[] array) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean canReplaceContent() {
			return false;
		}

		@Override
		public void replaceContent(Object listValue, Object[] array) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return IListStructuralInfo.NULL_LIST_STRUCTURAL_INFO;
		}

		@Override
		public IListItemDetailsAccessMode getDetailsAccessMode() {
			return new DetachedItemDetailsAccessMode();
		}

		@Override
		public boolean isInsertionAllowed() {
			return false;
		}

		@Override
		public boolean isRemovalAllowed() {
			return false;
		}

		@Override
		public boolean isMoveAllowed() {
			return false;
		}

		@Override
		public boolean canViewItemDetails() {
			return true;
		}

		@Override
		public List<IDynamicListAction> getDynamicActions(List<? extends ItemPosition> selection,
				Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
			return Collections.emptyList();
		}

		@Override
		public List<IDynamicListProperty> getDynamicProperties(List<? extends ItemPosition> selection,
				Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
			return Collections.emptyList();
		}

		@Override
		public boolean isItemNullValueSupported() {
			return false;
		}

		@Override
		public ItemCreationMode getItemCreationMode() {
			return ItemCreationMode.UNDEFINED;
		}

		@Override
		public ValueReturnMode getItemReturnMode() {
			return ValueReturnMode.INDETERMINATE;
		}

		@Override
		public ToolsLocation getToolsLocation() {
			return ToolsLocation.EAST;
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
		public boolean supports(Object localObject) {
			return false;
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
			throw new UnsupportedOperationException();
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

		@Override
		public IFieldInfo getSelectionTargetField(ITypeInfo objectType) {
			return null;
		}
	};

	/**
	 * @return the type of items supported by the lists of this type or null if this
	 *         type is not known.
	 */
	ITypeInfo getItemType();

	/**
	 * @param listValue An instance of the current list type.
	 * @return a generic array containing the items of the given list value.
	 */
	Object[] toArray(Object listValue);

	/**
	 * @return true if and only if instances of this list type can be created from
	 *         generic arrays. Otherwise {@link #fromArray(Object[])} should not be
	 *         called.
	 */
	boolean canInstantiateFromArray();

	/**
	 * @param array A generic array containing items supported by this list type.
	 * @return a new instance of this list type containing the same items as the
	 *         generic array passed as parameter.
	 */
	Object fromArray(Object[] array);

	/**
	 * @return true if and only if instances of this list type can have their
	 *         content replaced by calling
	 *         {@link #replaceContent(Object, Object[])}.
	 */
	boolean canReplaceContent();

	/**
	 * Replaces the list of items of the given instance by the one contained in the
	 * given generic array.
	 * 
	 * @param listValue An instance of the current list type.
	 * @param array     A generic array containing items supported by this list
	 *                  type.
	 */
	void replaceContent(Object listValue, Object[] array);

	/**
	 * @return tabular and hierarchical preferences about this list type.
	 */
	IListStructuralInfo getStructuralInfo();

	/**
	 * @return preferences about the way item details are displayed. Note that when
	 *         the list is actually a tree then this method must be called from the
	 *         root list type information.
	 */
	IListItemDetailsAccessMode getDetailsAccessMode();

	/**
	 * @return whether the position of each item in this list type instances is
	 *         automatically determined or not (manually).
	 */
	boolean areItemsAutomaticallyPositioned();

	/**
	 * @return whether item addition should be allowed on instances of this list
	 *         type.
	 */
	boolean isInsertionAllowed();

	/**
	 * @return whether item removal should be allowed on instances of this list
	 *         type.
	 */
	boolean isRemovalAllowed();

	/**
	 * @return whether item position change should be allowed on instances of this
	 *         list type.
	 */
	boolean isMoveAllowed();

	/**
	 * @return whether item details display should be allowed on instances of this
	 *         list type.
	 */
	boolean canViewItemDetails();

	/**
	 * @param selection                       The list of selected item positions.
	 * @param listModificationFactoryAccessor An object that maps the selection to a
	 *                                        list modification factory.
	 * @return actions that can be performed on a list instance according to the
	 *         given selection of items. Note that when the list is actually a tree
	 *         then this method must be called from the root list type information
	 *         since the selection may include items positioned at different levels
	 *         of depth in the tree.
	 */
	List<IDynamicListAction> getDynamicActions(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor);

	/**
	 * @param selection                       The list of selected item positions.
	 * @param listModificationFactoryAccessor An object that maps the selection to a
	 *                                        list modification factory.
	 * @return list instance properties that can be accessed according to the given
	 *         selection of items. Note that when the list is actually a tree then
	 *         this method must be called from the root list type information since
	 *         the selection may include items positioned at different levels of
	 *         depth in the tree.
	 */
	List<IDynamicListProperty> getDynamicProperties(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor);

	/**
	 * @return whether instances of this list type support null items.
	 */
	boolean isItemNullValueSupported();

	/**
	 * @return an option describing how the UI reacts to item creation requests.
	 */
	ItemCreationMode getItemCreationMode();

	/**
	 * @return the value return mode of the items. It may impact the behavior of the
	 *         list control.
	 */
	ValueReturnMode getItemReturnMode();

	/**
	 * @param objectType The parent object type.
	 * @return a field information that will be used to provide the reference/value
	 *         of the selected item to the parent object. Note that if the selected
	 *         item type is not compatible with the target field, then null or a
	 *         default value (when null is not supported) will be provided to the
	 *         parent object.
	 */
	IFieldInfo getSelectionTargetField(ITypeInfo objectType);

	/**
	 * @param itemPosition The position of the list item to inspect.
	 * @return whether validation errors should be checked for the element
	 *         designated by the item position passed as a parameter. Note that in
	 *         the case of trees, if the checking is disabled for the current item,
	 *         it will also be disabled for entire subtree.
	 */
	boolean isItemNodeValidityDetectionEnabled(ItemPosition itemPosition);

	/**
	 * Allows to choose how the UI behaves when creating items. Typically it answers
	 * the question "should the framework require constructor parameter values from
	 * users or not?".
	 * 
	 * @author olitank
	 *
	 */

	/**
	 * @return where the tool bar will be located on the list control.
	 */
	ToolsLocation getToolsLocation();

	/**
	 * @param itemPosition The position of the list item to validate.
	 * @return a validation task that can be used to fully validate the state of the
	 *         item at the given position in the absence of a concrete form that
	 *         would have orchestrated the item's validation. Subtree items should
	 *         not be validated by this job since the list/tree control takes care
	 *         of their validation. Note that this method was added for optimization
	 *         purposes because, with the Swing toolkit, it was mandatory to create
	 *         any form from the UI thread, which was therefore slowed down, just to
	 *         have a form orchestrating the validation.
	 */
	IValidationJob getListItemAbstractFormValidationJob(ItemPosition itemPosition);

	public enum ItemCreationMode {
		/**
		 * A null value is directly inserted in the list.
		 */
		UNVERIFIED_NULL,
		/**
		 * The item type is instantiated without requiring user input and the result is
		 * directly inserted in the list.
		 */
		DEFAULT_UNVERIFIED_INSTANCE,
		/**
		 * The item type is instantiated according user preferences and the result is
		 * directly inserted in the list.
		 */
		CUSTOM_UNVERIFIED_INSTANCE,
		/**
		 * A null value is presented to the user for modification/validation before
		 * inserting it in the list.
		 */
		VERIFIED_NULL,
		/**
		 * The item type is instantiated without requiring user input and the result
		 * presented to the user for modification/validation before inserting it in the
		 * list.
		 */
		DEFAULT_VERIFIED_INSTANCE,
		/**
		 * The item type is instantiated according user preferences and the result
		 * presented to the user for modification/validation before inserting it in the
		 * list.
		 */
		CUSTOM_VERIFIED_INSTANCE,
		/**
		 * A relevant mode is used.
		 */
		UNDEFINED
	}

	/**
	 * Allows to choose where the toolbar will be located on the list control.
	 * 
	 * @author olitank
	 *
	 */
	public enum ToolsLocation {
		NORTH, SOUTH, EAST, WEST, HIDDEN
	}

}
