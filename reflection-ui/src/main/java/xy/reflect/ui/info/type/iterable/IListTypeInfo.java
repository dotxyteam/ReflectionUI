


package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.Mapper;

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
	boolean canInstanciateFromArray();

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
	 * @return whether the items in this list type instances are ordered (can be
	 *         moved to specific positions) or not.
	 */
	boolean isOrdered();

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
	InitialItemValueCreationOption getInitialItemValueCreationOption();

	/**
	 * @return the value return mode of the items. It may impact the behavior of the
	 *         list control.
	 */
	ValueReturnMode getItemReturnMode();

	/**
	 * Allows to choose how the UI behaves when creating items. Typically it answers
	 * the question "should the framework require constructor parameter values from
	 * users or not ?".
	 * 
	 * @author olitank
	 *
	 */
	public enum InitialItemValueCreationOption {
		/**
		 * Null items are created by default.
		 */
		CREATE_INITIAL_NULL_VALUE,
		/**
		 * Items are created by using using default constructors or other constructors
		 * with default parameter values, etc, without requiring any information from
		 * the user.
		 */
		CREATE_INITIAL_VALUE_AUTOMATICALLY,
		/**
		 * All item creation options are presented to the user who makes a choice. This
		 * is the default behavior.
		 */
		CREATE_INITIAL_VALUE_ACCORDING_USER_PREFERENCES
	}

}
