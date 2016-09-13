package xy.reflect.ui.info.type.iterable.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldAsListListTypeInfo;
import xy.reflect.ui.info.field.MultipleFieldAsListListTypeInfo.MultipleFieldAsListItem;
import xy.reflect.ui.info.field.MultipleFieldAsListListTypeInfo.MultipleFieldAsListItemTypeInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.map.IMapEntryTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.column.ColumnInfoProxy;
import xy.reflect.ui.info.type.iterable.structure.column.FieldColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.PositionColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.TypeNameColumnInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.util.InfoCustomizationsNew.ColumnCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizationsNew.ListStructureCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizationsNew.TreeStructureDiscoverySettings;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class CustomizedStructuralInfo implements IListStructuralInfo {

	protected List<IFieldInfo> columnFields;
	protected ReflectionUI reflectionUI;
	protected ITypeInfo rootItemType;
	protected IListStructuralInfo base;
	protected ListStructureCustomization customization;
	protected List<IColumnInfo> columns;
	protected IListTypeInfo listType;

	public CustomizedStructuralInfo(ReflectionUI reflectionUI, IListStructuralInfo base, IListTypeInfo listType,
			ListStructureCustomization customization) {
		this.reflectionUI = reflectionUI;
		this.base = base;
		this.listType = listType;
		this.customization = customization;
		this.reflectionUI = reflectionUI;
		this.rootItemType = listType.getItemType();
		this.columnFields = collectFields();
		this.columns = getColumns();
	}

	protected List<IFieldInfo> collectFields() {
		if (rootItemType == null) {
			return null;
		}
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		IFieldInfo treeField = getTreeColumnField();
		if (treeField != null) {
			result.add(treeField);
		}
		for (IFieldInfo candidateField : this.rootItemType.getFields()) {
			if (candidateField.getType() instanceof IListTypeInfo) {
				continue;
			}
			if (candidateField.equals(treeField)) {
				continue;
			}
			result.add(new FieldInfoProxy(candidateField) {

				@Override
				public Object getValue(Object object) {
					ItemPosition itemPosition = (ItemPosition) object;
					Object item = itemPosition.getItem();
					if (rootItemType.equals(itemPosition.getContainingListType().getItemType())) {
						Object value = super.getValue(item);
						return reflectionUI.toString(value);
					} else {
						return null;
					}
				}

			});
		}
		return result;
	}

	@Override
	public List<IColumnInfo> getColumns() {
		final List<IColumnInfo> result = new ArrayList<IColumnInfo>();
		result.addAll(base.getColumns());
		if (customization.isFieldColumnsAdded()) {
			int insertindex = 0;
			for (final IFieldInfo field : columnFields) {
				result.add(insertindex, new FieldColumnInfo(field));
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
			}
			filteredResult.add(column);
		}

		if (customization.getColumnsCustomOrder() != null) {
			Collections.sort(filteredResult,
					ReflectionUIUtils.getInfosComparator(customization.getColumnsCustomOrder(), filteredResult));
		}
		
		return filteredResult;
	}

	@Override
	public IFieldInfo getItemSubListField(final ItemPosition itemPosition) {
		TreeStructureDiscoverySettings treeSettings = customization.getTreeStructureDiscoverySettings();
		if (treeSettings == null) {
			return base.getItemSubListField(itemPosition);
		}
		List<IFieldInfo> candidateFields = getItemSubListCandidateFields(itemPosition);
		if (candidateFields.size() == 0) {
			return null;
		} else if (candidateFields.size() == 1) {
			IFieldInfo candidateField = candidateFields.get(0);
			if (itemPosition.getItem() instanceof MultipleFieldAsListItem) {
				return candidateField;
			} else if (displaysSubListFieldNameAsTreeNode(candidateField, itemPosition)) {
				return new MultipleFieldAsListListTypeInfo(reflectionUI, Collections.singletonList(candidateField));
			} else {
				return candidateField;
			}
		} else {
			return new MultipleFieldAsListListTypeInfo(reflectionUI, candidateFields);
		}
	}

	protected boolean displaysSubListFieldNameAsTreeNode(IFieldInfo subListField, ItemPosition itemPosition) {
		ITypeInfo itemType = itemPosition.getContainingListType().getItemType();
		if (itemType instanceof IMapEntryTypeInfo) {
			return false;
		}
		if (itemType instanceof MultipleFieldAsListItemTypeInfo) {
			return false;
		}
		return !subListField.getName().equals(itemPosition.getContainingListField().getName());
	}

	protected List<IFieldInfo> getItemSubListCandidateFields(ItemPosition itemPosition) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		Object item = itemPosition.getItem();
		ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
		if (actualItemType instanceof MultipleFieldAsListItemTypeInfo) {
			result.add(((MultipleFieldAsListItemTypeInfo) actualItemType).getValueField());
		} else {
			List<IFieldInfo> itemFields = actualItemType.getFields();
			for (IFieldInfo field : itemFields) {
				ITypeInfo fieldType = field.getType();
				if (fieldType instanceof IListTypeInfo) {
					ITypeInfo subListItemType = ((IListTypeInfo) fieldType).getItemType();
					if (item instanceof MultipleFieldAsListItem) {
						result.add(field);
					} else if (isValidTreeNodeItemType(subListItemType)) {
						result.add(field);
					}
				}
			}
		}
		return result;
	}

	protected boolean isValidTreeNodeItemType(ITypeInfo type) {
		if (ReflectionUIUtils.equalsOrBothNull(rootItemType, type)) {
			return true;
		}
		if (type instanceof IMapEntryTypeInfo) {
			IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) type;
			ITypeInfo entryValueType = entryType.getValueField().getType();
			if (entryValueType instanceof IListTypeInfo) {
				ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType).getItemType();
				if (isValidTreeNodeItemType(entryValuListItemType)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public IInfoCollectionSettings getItemInfoSettings(final ItemPosition itemPosition) {
		return new IInfoCollectionSettings() {

			@Override
			public boolean excludeMethod(IMethodInfo method) {
				return false;
			}

			@Override
			public boolean excludeField(IFieldInfo field) {
				List<IFieldInfo> subListCandidateFields = getItemSubListCandidateFields(itemPosition);
				return subListCandidateFields.contains(field);
			}
		};
	}

	protected IFieldInfo getTreeColumnField() {
		TreeStructureDiscoverySettings treeSettings = customization.getTreeStructureDiscoverySettings();
		if (treeSettings == null) {
			return null;
		}
		if (treeSettings.getTreeColumnFieldName() == null) {
			return null;
		}
		return ReflectionUIUtils.findInfoByName(columnFields, treeSettings.getTreeColumnFieldName());
	}

}
