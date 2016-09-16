package xy.reflect.ui.info.type.iterable.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.InfoCollectionSettingsProxy;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldAsListInfo;
import xy.reflect.ui.info.field.MultipleFieldAsListInfo.MultipleFieldAsListItem;
import xy.reflect.ui.info.field.MultipleFieldAsListInfo.MultipleFieldAsListItemTypeInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.map.IMapEntryTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.column.ColumnInfoProxy;
import xy.reflect.ui.info.type.iterable.structure.column.FieldColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.PositionColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.StringValueColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.TypeNameColumnInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.util.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.ListStructureCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.TreeStructureDiscoverySettings;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class CustomizedStructuralInfo extends ListStructuralInfoProxy {

	protected List<IFieldInfo> columnFields;
	protected ReflectionUI reflectionUI;
	protected ITypeInfo rootItemType;
	protected ListStructureCustomization customization;
	protected List<IColumnInfo> columns;
	protected IListTypeInfo listType;

	public CustomizedStructuralInfo(ReflectionUI reflectionUI, IListStructuralInfo base, IListTypeInfo listType,
			ListStructureCustomization customization) {
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
			if (itemPosition.getItem() instanceof MultipleFieldAsListItem) {
				return candidateField;
			} else if (displaysSubListFieldNameAsTreeNode(candidateField, itemPosition)) {
				return new MultipleFieldAsListInfo(reflectionUI, Collections.singletonList(candidateField));
			} else {
				return candidateField;
			}
		} else {
			return new MultipleFieldAsListInfo(reflectionUI, candidateFields);
		}
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
	public IInfoCollectionSettings getItemInfoSettings(final ItemPosition itemPosition) {
		if (customization.getTreeStructureDiscoverySettings() == null) {
			return super.getItemInfoSettings(itemPosition);
		}
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

	protected List<IFieldInfo> collectFields() {
		if (rootItemType == null) {
			return null;
		}
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo candidateField : this.rootItemType.getFields()) {
			if (candidateField.getType() instanceof IListTypeInfo) {
				continue;
			}
			result.add(candidateField);
		}
		return result;
	}

	@Override
	public List<IColumnInfo> getColumns() {
		final List<IColumnInfo> result = new ArrayList<IColumnInfo>();
		for (IColumnInfo column : super.getColumns()) {
			if (customization.getTreeStructureDiscoverySettings() == null) {
				result.add(column);
			} else {
				result.add(new ColumnInfoProxy(column) {
					@Override
					public String getCellValue(ItemPosition itemPosition) {
						if (itemPosition.isRootListItemPosition()) {
							return super.getCellValue(itemPosition);
						} else {
							return null;
						}
					}
				});
			}
		}
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
		if (itemType instanceof IMapEntryTypeInfo) {
			return false;
		}
		if (itemType instanceof MultipleFieldAsListItemTypeInfo) {
			return false;
		}
		return !subListField.getName().equals(itemPosition.getContainingListField().getName());
	}

}
