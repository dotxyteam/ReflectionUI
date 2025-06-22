
package xy.reflect.ui.info.type.iterable.structure;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.InfoPattern;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ControlSizeUnit;
import xy.reflect.ui.info.custom.InfoCustomizations.TreeStructureDiscoverySettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsListFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.filter.InfoFilterProxy;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPositionProxy;
import xy.reflect.ui.info.type.iterable.structure.CustomizedListStructuralInfo.SubListGroupField.SubListGroupItemTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.column.ColumnInfoProxy;
import xy.reflect.ui.info.type.iterable.structure.column.FieldColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.PositionColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.StringValueColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.TypeNameColumnInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Structural information proxy that conforms the base structural information to
 * the specified {@link ListCustomization}.
 * 
 * @author olitank
 *
 */
public class CustomizedListStructuralInfo extends ListStructuralInfoProxy {

	protected List<IFieldInfo> columnFields;
	protected ReflectionUI reflectionUI;
	protected ListCustomization listCustomization;
	protected List<IColumnInfo> columns;
	protected IListTypeInfo listType;
	protected ITypeInfo rootItemType;

	public CustomizedListStructuralInfo(ReflectionUI reflectionUI, IListStructuralInfo base, IListTypeInfo listType,
			ListCustomization listCustomization) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.listType = listType;
		this.listCustomization = listCustomization;
		this.reflectionUI = reflectionUI;
		this.rootItemType = findRootItemType();
		this.columnFields = collectColumnFields();
		this.columns = getColumns();
	}

	@Override
	public int getWidth() {
		if (listCustomization.getWidth() != null) {
			if (listCustomization.getWidth().getUnit() == ControlSizeUnit.PIXELS) {
				return listCustomization.getWidth().getValue();
			} else if (listCustomization.getWidth().getUnit() == ControlSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
				return Math.round((listCustomization.getWidth().getValue() / 100f) * screenSize.width);
			} else {
				throw new ReflectionUIError();
			}
		}
		return super.getWidth();
	}

	@Override
	public int getHeight() {
		if (listCustomization.getHeight() != null) {
			if (listCustomization.getHeight().getUnit() == ControlSizeUnit.PIXELS) {
				return listCustomization.getHeight().getValue();
			} else if (listCustomization.getHeight().getUnit() == ControlSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
				return Math.round((listCustomization.getHeight().getValue() / 100f) * screenSize.height);
			} else {
				throw new ReflectionUIError();
			}
		}
		return super.getHeight();
	}

	protected ITypeInfo findRootItemType() {
		TreeStructureDiscoverySettings treeStructure = listCustomization.getTreeStructureDiscoverySettings();
		if (treeStructure != null) {
			if (treeStructure.getCustomBaseNodeTypeFinder() != null) {
				return treeStructure.getCustomBaseNodeTypeFinder().find(reflectionUI, null);
			}
		}
		if (listCustomization.getCustomItemTypeFinder() != null) {
			return listCustomization.getCustomItemTypeFinder().find(reflectionUI, null);
		}
		return listType.getItemType();
	}

	@Override
	public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
		final Object item = itemPosition.getItem();
		if (item == null) {
			return null;
		}
		itemPosition = new ItemPositionProxy(itemPosition) {
			// optimization to not retrieve multiple times the same item.
			@Override
			public Object getItem() {
				return item;
			}
		};
		if (listCustomization.getTreeStructureDiscoverySettings() == null) {
			return super.getItemSubListField(itemPosition);
		}
		List<IFieldInfo> candidateFields = getItemSubListCandidateFields(itemPosition);
		ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
		if (candidateFields.size() == 0) {
			return null;
		} else if (candidateFields.size() == 1) {
			IFieldInfo candidateField = candidateFields.get(0);
			if (isSubListFieldNameDisplayedAsTreeNode(candidateField, itemPosition)) {
				return getSubListsGroupingField(Collections.singletonList(candidateField), actualItemType);
			} else {
				return candidateField;
			}
		} else {
			return getSubListsGroupingField(candidateFields, actualItemType);
		}
	}

	protected IFieldInfo getSubListsGroupingField(List<IFieldInfo> subListFields, ITypeInfo actualItemType) {
		return new SubListGroupField(reflectionUI, subListFields, actualItemType);
	}

	protected List<IFieldInfo> getItemSubListCandidateFields(ItemPosition itemPosition) {
		if (listCustomization.getTreeStructureDiscoverySettings() == null) {
			return Collections.emptyList();
		}
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		final Object item = itemPosition.getItem();
		if (item != null) {
			ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
			List<IFieldInfo> itemFields = actualItemType.getFields();
			for (final IFieldInfo field : itemFields) {
				boolean excluded = false;
				for (InfoPattern excludedField : listCustomization.getTreeStructureDiscoverySettings()
						.getExcludedSubListFields()) {
					if (excludedField.matches(field.getName())) {
						excluded = true;
						break;
					}
				}
				if (excluded) {
					continue;
				}
				ITypeInfo fieldType = field.getType();
				if (fieldType instanceof IListTypeInfo) {
					ITypeInfo subListItemType = ((IListTypeInfo) fieldType).getItemType();
					if (isValidSubListItemType(subListItemType)) {
						result.add(field);
					}
				}
			}
		}
		return result;
	}

	protected boolean isValidSubListItemType(ITypeInfo itemType) {
		if (listCustomization.getTreeStructureDiscoverySettings().isHeterogeneousTree()) {
			return true;
		}
		if (itemType == null) {
			return false;
		}
		if (rootItemType.getName().equals(itemType.getName())) {
			return true;
		}
		for (ITypeInfo rootTypeDescendant : ReflectionUIUtils.listDescendantTypes(rootItemType)) {
			if (rootTypeDescendant.getName().equals(itemType.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IInfoFilter getItemDetailsInfoFilter(final ItemPosition itemPosition) {
		return new CustomizedItemDetailsInfoFilterProxy(super.getItemDetailsInfoFilter(itemPosition),
				listCustomization);
	}

	protected List<IFieldInfo> collectColumnFields() {
		if (rootItemType == null) {
			return Collections.emptyList();
		}
		return rootItemType.getFields();
	}

	@Override
	public List<IColumnInfo> getColumns() {
		final List<IColumnInfo> result = new ArrayList<IColumnInfo>();
		result.addAll(super.getColumns());

		if (listCustomization.isStringValueColumnAdded()) {
			result.add(0, new StringValueColumnInfo(reflectionUI));
		}
		if (listCustomization.isFieldColumnsAdded()) {
			int insertindex = 0;
			for (final IFieldInfo field : columnFields) {
				result.add(insertindex, new FieldColumnInfo(reflectionUI, rootItemType, field));
				insertindex++;
			}
		}
		if (listCustomization.isItemTypeColumnAdded()) {
			result.add(0, new TypeNameColumnInfo(reflectionUI));
		}
		if (listCustomization.isPositionColumnAdded()) {
			result.add(0, new PositionColumnInfo(reflectionUI));
		}

		final List<IColumnInfo> filteredResult = new ArrayList<IColumnInfo>();

		for (IColumnInfo column : result) {
			final ColumnCustomization c = InfoCustomizations.getColumnCustomization(listCustomization,
					column.getName());
			if (c != null) {
				if (c.isHidden()) {
					continue;
				}
				if (c.getCustomCaption() != null) {
					column = new ColumnInfoProxy(column) {
						@Override
						public String getCaption() {
							return c.getCustomCaption();
						}
					};
				}
				if (c.getMinimalCharacterCount() != null) {
					column = new ColumnInfoProxy(column) {

						@Override
						public int getMinimalCharacterCount() {
							return c.getMinimalCharacterCount();
						}
					};
				}
			}
			filteredResult.add(column);
		}

		List<String> customOrder = listCustomization.getColumnsCustomOrder();
		if (customOrder != null) {
			Collections.sort(filteredResult, ReflectionUIUtils.getInfosComparator(customOrder, filteredResult));
		}

		return filteredResult;
	}

	protected boolean isSubListFieldNameDisplayedAsTreeNode(IFieldInfo subListField, ItemPosition itemPosition) {
		ITypeInfo itemType = itemPosition.getContainingListType().getItemType();
		if (itemPosition.getItem() instanceof SubListGroupField.ListItem) {
			return false;
		}
		if (itemType instanceof SubListGroupItemTypeInfo) {
			return false;
		}
		if (listCustomization.getTreeStructureDiscoverySettings().isSingleSubListFieldNameNeverDisplayedAsTreeNode()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((listCustomization == null) ? 0 : listCustomization.hashCode());
		result = prime * result + ((listType == null) ? 0 : listType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomizedListStructuralInfo other = (CustomizedListStructuralInfo) obj;
		if (listCustomization == null) {
			if (other.listCustomization != null)
				return false;
		} else if (!listCustomization.equals(other.listCustomization))
			return false;
		if (listType == null) {
			if (other.listType != null)
				return false;
		} else if (!listType.equals(other.listType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CustomizedStructuralInfo [customization=" + listCustomization + ", listType=" + listType + ", base="
				+ base + "]";
	}

	/**
	 * Version of {@link MultipleFieldsAsListFieldInfo} used for list structure
	 * customization only.
	 * 
	 * @author olitank
	 *
	 */
	public static class SubListGroupField extends MultipleFieldsAsListFieldInfo {

		public SubListGroupField(ReflectionUI reflectionUI, List<IFieldInfo> fields, ITypeInfo containingItemType) {
			super(reflectionUI, fields, containingItemType);
		}

		@Override
		public String getName() {
			return "subListGroup [containingItemType=" + getContainingItemType().getName() + "]";
		}

		@Override
		public boolean isGetOnly() {
			return false;
		}

		@Override
		public void setValue(Object object, Object value) {
		}

		@Override
		protected ListItem getListItem(Object object, IFieldInfo listFieldInfo) {
			return new SubListGroupItem(object, listFieldInfo);
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI
					.getTypeInfo(new PrecomputedTypeInstanceWrapper.TypeInfoSource(new SubListGroupTypeInfo()));
		}

		@Override
		public Object getValue(Object object) {
			List<Object> result = new ArrayList<Object>();
			for (IFieldInfo field : fields) {
				result.add(new PrecomputedTypeInstanceWrapper(getListItem(object, field),
						new SubListGroupItemTypeInfo(field)));
			}
			return new PrecomputedTypeInstanceWrapper(result, new SubListGroupTypeInfo());
		}

		public ITypeInfo getContainingItemType() {
			return objectType;
		}

		@Override
		public String toString() {
			return "SubListGroupField [containingItemType=" + getContainingItemType() + "]";
		}

		public class SubListGroupTypeInfo extends ListTypeInfo {

			public SubListGroupTypeInfo() {
				this.itemType = reflectionUI.getTypeInfo(new PrecomputedTypeInstanceWrapper.TypeInfoSource(
						new DefaultTypeInfo(reflectionUI, new JavaTypeInfoSource(SubListGroupItem.class, null))));
			}

			@Override
			public IListStructuralInfo getStructuralInfo() {
				return new ListStructuralInfoProxy(super.getStructuralInfo()) {

					@Override
					public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
						PrecomputedTypeInstanceWrapper item = (PrecomputedTypeInstanceWrapper) itemPosition.getItem();
						SubListGroupItem subListGroupItem = (SubListGroupItem) (item).getInstance();
						return new PrecomputedTypeInstanceWrapper.TypeInfoSource(
								new SubListGroupItemTypeInfo(subListGroupItem.getField())).buildTypeInfo(reflectionUI)
										.getFields().get(0);
					}

				};
			}

			@Override
			public String toString() {
				return "SubListGroupTypeInfo [containingItemType=" + getContainingItemType() + "]";
			}

		}

		public class SubListGroupItemTypeInfo extends ListItemTypeInfo {

			public SubListGroupItemTypeInfo(IFieldInfo field) {
				super(field);
			}

			@Override
			public String getName() {
				return "SubListGroupItemTypeInfo [of=" + SubListGroupField.this.getName() + ", itemField="
						+ field.getName() + "]";
			}

			@Override
			public IFieldInfo getDetailsField() {
				return new SubListGroupItemDetailsFieldInfo(field);
			}

			@Override
			public String toString() {
				return "SubListGroupItemTypeInfo [of=" + SubListGroupField.this + ", itemField=" + field + "]";
			}

		}

		public class SubListGroupItem extends ListItem {

			public SubListGroupItem(Object object, IFieldInfo field) {
				super(object, field);
			}

		}

		public class SubListGroupItemDetailsFieldInfo extends ListItemDetailsFieldInfo {

			public SubListGroupItemDetailsFieldInfo(IFieldInfo field) {
				super(field);
			}

			public SubListGroupItemTypeInfo getSubListGroupItemTypeInfo() {
				return new SubListGroupItemTypeInfo(base);
			}

			@Override
			public ITypeInfo getType() {
				return reflectionUI.getTypeInfo(new TypeInfoSourceProxy(base.getType().getSource()) {
					@Override
					public SpecificitiesIdentifier getSpecificitiesIdentifier() {
						return new SpecificitiesIdentifier(
								new SubListGroupItemTypeInfo(SubListGroupItemDetailsFieldInfo.this.base).getName(),
								SubListGroupItemDetailsFieldInfo.this.getName());
					}

					@Override
					protected String getTypeInfoProxyFactoryIdentifier() {
						return "FieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", parent="
								+ SubListGroupField.this.getName() + "]";
					}
				});
			}

			@Override
			public String toString() {
				return "SubListGroupItemDetailsFieldInfo [of=" + SubListGroupField.this + ", itemField=" + base + "]";
			}
		}

	}

	protected static class CustomizedItemDetailsInfoFilterProxy extends InfoFilterProxy {

		protected ListCustomization listCustomization;

		public CustomizedItemDetailsInfoFilterProxy(IInfoFilter base, ListCustomization listCustomization) {
			super(base);
			this.listCustomization = listCustomization;
		}

		@Override
		public IMethodInfo apply(IMethodInfo method) {
			String methodSignature = method.getSignature();
			for (InfoPattern pattern : listCustomization.getMethodsExcludedFromItemDetails()) {
				if (pattern.matches(methodSignature)) {
					return null;
				}
			}
			return super.apply(method);
		}

		@Override
		public IFieldInfo apply(IFieldInfo field) {
			for (InfoPattern pattern : listCustomization.getFieldsExcludedFromItemDetails()) {
				if (pattern.matches(field.getName())) {
					return null;
				}
			}
			return super.apply(field);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((listCustomization == null) ? 0 : listCustomization.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			CustomizedItemDetailsInfoFilterProxy other = (CustomizedItemDetailsInfoFilterProxy) obj;
			if (listCustomization == null) {
				if (other.listCustomization != null)
					return false;
			} else if (!listCustomization.equals(other.listCustomization))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CustomizedItemDetailsInfoFilter [base=" + base + ", listCustomization=" + listCustomization + "]";
		}

	}

}
