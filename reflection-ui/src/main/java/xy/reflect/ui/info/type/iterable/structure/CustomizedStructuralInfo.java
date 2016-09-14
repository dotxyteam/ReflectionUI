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
	protected AbstractTreeListStructuralInfo treeStructure;

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
		this.treeStructure = new AbstractTreeListStructuralInfo(reflectionUI, rootItemType) {

			@Override
			public List<IColumnInfo> getColumns() {
				return CustomizedStructuralInfo.this.getColumns();
			}

			@Override
			protected boolean autoDetectTreeStructure() {
				return CustomizedStructuralInfo.this.customization.getTreeStructureDiscoverySettings() != null;
			}

			@Override
			protected boolean isValidSubListNodeItemType(ITypeInfo type) {
				if (CustomizedStructuralInfo.this.customization.getTreeStructureDiscoverySettings()
						.isHeterogeneousTree()) {
					return true;
				} else {
					return super.isValidSubListNodeItemType(type);
				}
			}

		};
	}

	@Override
	public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
		if (customization.getTreeStructureDiscoverySettings() == null) {
			return super.getItemSubListField(itemPosition);
		}
		return treeStructure.getItemSubListField(itemPosition);
	}

	@Override
	public IInfoCollectionSettings getItemInfoSettings(ItemPosition itemPosition) {
		if (customization.getTreeStructureDiscoverySettings() == null) {
			return super.getItemInfoSettings(itemPosition);
		}
		if (itemPosition.isRootListItemPosition()) {
			return super.getItemInfoSettings(itemPosition);
		} else {
			return treeStructure.getItemInfoSettings(itemPosition);
		}
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

}
