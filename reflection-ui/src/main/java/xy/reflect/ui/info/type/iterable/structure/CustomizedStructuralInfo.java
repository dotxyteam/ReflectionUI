package xy.reflect.ui.info.type.iterable.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsOne;
import xy.reflect.ui.info.field.MultipleFieldsAsOne.ListItem;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.filter.InfoFilterProxy;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.map.IMapEntryTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.SubListsGroupingField.SubListGroupTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.column.ColumnInfoProxy;
import xy.reflect.ui.info.type.iterable.structure.column.FieldColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.PositionColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.StringValueColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.TypeNameColumnInfo;
import xy.reflect.ui.info.type.util.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.InfoFilter;
import xy.reflect.ui.info.type.util.InfoCustomizations.ListCustomization;
import xy.reflect.ui.util.ReflectionUIUtils;

public class CustomizedStructuralInfo extends ListStructuralInfoProxy {

	protected List<IFieldInfo> columnFields;
	protected ReflectionUI reflectionUI;
	protected ITypeInfo rootItemType;
	protected ListCustomization customization;
	protected List<IColumnInfo> columns;
	protected IListTypeInfo listType;

	public CustomizedStructuralInfo(ReflectionUI reflectionUI, IListStructuralInfo base, IListTypeInfo listType,
			ListCustomization customization) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.listType = listType;
		this.customization = customization;
		this.reflectionUI = reflectionUI;
		this.rootItemType = listType.getItemType();
		this.columnFields = collectFields();
		this.columns = getColumns();

	}

	@Override
	public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
		if (customization.getTreeStructureDiscoverySettings() == null) {
			return super.getItemSubListField(itemPosition);
		}
		List<IFieldInfo> candidateFields = getItemSubListCandidateFields(itemPosition);
		if (candidateFields.size() == 0) {
			return null;
		} else if (candidateFields.size() == 1) {
			IFieldInfo candidateField = candidateFields.get(0);
			if (displaysSubListFieldNameAsTreeNode(candidateField, itemPosition)) {
				return getSubListsGroupingField(Collections.singletonList(candidateField));
			} else {
				return candidateField;
			}
		} else {
			return getSubListsGroupingField(candidateFields);
		}
	}

	protected IFieldInfo getSubListsGroupingField(List<IFieldInfo> subListFields) {
		return new SubListsGroupingField(reflectionUI, subListFields);			
	}

	protected List<IFieldInfo> getItemSubListCandidateFields(ItemPosition itemPosition) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		Object item = itemPosition.getItem();
		ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
		if (actualItemType instanceof SubListGroupTypeInfo) {
			result.add(((SubListGroupTypeInfo) actualItemType).getDetailsField());
		} else {
			List<IFieldInfo> itemFields = actualItemType.getFields();
			for (IFieldInfo field : itemFields) {
				ITypeInfo fieldType = field.getType();
				if (fieldType instanceof IListTypeInfo) {
					ITypeInfo subListItemType = ((IListTypeInfo) fieldType).getItemType();
					if (item instanceof ListItem) {
						result.add(field);
					} else if (isValidSubListNodeItemType(subListItemType)) {
						result.add(field);
					}
				}
			}
		}
		return result;
	}

	protected boolean isValidSubListNodeItemType(ITypeInfo type) {
		if (customization.getTreeStructureDiscoverySettings().isHeterogeneousTree()) {
			return true;
		}
		if (ReflectionUIUtils.equalsOrBothNull(rootItemType, type)) {
			return true;
		}
		if (type instanceof IMapEntryTypeInfo) {
			IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) type;
			ITypeInfo entryValueType = entryType.getValueField().getType();
			if (entryValueType instanceof IListTypeInfo) {
				ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType).getItemType();
				if (isValidSubListNodeItemType(entryValuListItemType)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public IInfoFilter getItemInfoFilter(final ItemPosition itemPosition) {
		return new InfoFilterProxy(super.getItemInfoFilter(itemPosition)) {

			@Override
			public boolean excludeMethod(IMethodInfo method) {
				String methodSignature = ReflectionUIUtils.getMethodSignature(method);
				for (InfoFilter filter : customization.getMethodsExcludedFromItemDetails()) {
					if (filter.matches(methodSignature)) {
						return true;
					}
				}
				return super.excludeMethod(method);
			}

			@Override
			public boolean excludeField(IFieldInfo field) {
				if (customization.getTreeStructureDiscoverySettings() != null) {
					List<IFieldInfo> subListCandidateFields = getItemSubListCandidateFields(itemPosition);
					if (subListCandidateFields.contains(field)) {
						return true;
					}
				}
				for (InfoFilter filter : customization.getFieldsExcludedFromItemDetails()) {
					if (filter.matches(field.getName())) {
						return true;
					}
				}
				return super.excludeField(field);
			}
		};
	}

	protected List<IFieldInfo> collectFields() {
		if (rootItemType == null) {
			return Collections.emptyList();
		}
		return rootItemType.getFields();
	}

	@Override
	public List<IColumnInfo> getColumns() {
		final List<IColumnInfo> result = new ArrayList<IColumnInfo>();
		result.addAll(super.getColumns());

		if (customization.isStringValueColumnAdded()) {
			result.add(0, new StringValueColumnInfo(reflectionUI));
		}
		if (customization.isFieldColumnsAdded()) {
			int insertindex = 0;
			for (final IFieldInfo field : columnFields) {
				result.add(insertindex, new FieldColumnInfo(reflectionUI, rootItemType, field));
				insertindex++;
			}
		}
		if (customization.isItemTypeColumnAdded()) {
			result.add(0, new TypeNameColumnInfo(reflectionUI));
		}
		if (customization.isPositionColumnAdded()) {
			result.add(0, new PositionColumnInfo());
		}

		final List<IColumnInfo> filteredResult = new ArrayList<IColumnInfo>();

		for (IColumnInfo column : result) {
			final ColumnCustomization c = customization.getColumnCustomization(column.getName());
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

		if (customization.getColumnsCustomOrder() != null) {
			Collections.sort(filteredResult,
					ReflectionUIUtils.getInfosComparator(customization.getColumnsCustomOrder(), filteredResult));
		}

		return filteredResult;
	}

	protected boolean displaysSubListFieldNameAsTreeNode(IFieldInfo subListField, ItemPosition itemPosition) {
		ITypeInfo itemType = itemPosition.getContainingListType().getItemType();
		if (itemPosition.getItem() instanceof MultipleFieldsAsOne.ListItem) {
			return false;
		}
		if (itemType instanceof IMapEntryTypeInfo) {
			return false;
		}

		if (itemType instanceof SubListGroupTypeInfo) {
			return false;
		}
		return !subListField.getCaption().equals(itemPosition.getContainingListTitle());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((customization == null) ? 0 : customization.hashCode());
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
		CustomizedStructuralInfo other = (CustomizedStructuralInfo) obj;
		if (customization == null) {
			if (other.customization != null)
				return false;
		} else if (!customization.equals(other.customization))
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
		return "CustomizedStructuralInfo [customization=" + customization + ", listType=" + listType + ", base=" + base
				+ "]";
	}

}
