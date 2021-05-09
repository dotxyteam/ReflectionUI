/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.info.type.iterable.structure;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ITypeInfoFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.InfoFilter;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ListLengthUnit;
import xy.reflect.ui.info.custom.InfoCustomizations.TreeStructureDiscoverySettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.filter.InfoFilterProxy;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPositionProxy;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo.SubListGroupField.SubListGroupItem;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo.SubListGroupField.SubListGroupItemTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.column.ColumnInfoProxy;
import xy.reflect.ui.info.type.iterable.structure.column.FieldColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.PositionColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.StringValueColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.TypeNameColumnInfo;
import xy.reflect.ui.util.MiscUtils;
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
		this.columnFields = collectFields();
		this.columns = getColumns();
	}

	@Override
	public int getLength() {
		if (listCustomization.getLength() != null) {
			if (listCustomization.getLength().getUnit() == ListLengthUnit.PIXELS) {
				return listCustomization.getLength().getValue();
			} else if (listCustomization.getLength().getUnit() == ListLengthUnit.SCREEN_PERCENT) {
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
				return Math.round((listCustomization.getLength().getValue() / 100f) * screenSize.height);
			} else {
				throw new ReflectionUIError();
			}
		}
		return super.getLength();
	}

	protected ITypeInfo findRootItemType() {
		TreeStructureDiscoverySettings treeStructure = listCustomization.getTreeStructureDiscoverySettings();
		if (treeStructure != null) {
			ITypeInfoFinder nodeTypeFinder = treeStructure.getCustomBaseNodeTypeFinder();
			if (nodeTypeFinder != null) {
				return nodeTypeFinder.find(reflectionUI, null);
			}
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
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		Object item = itemPosition.getItem();
		if (item != null) {
			ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
			if (actualItemType instanceof SubListGroupItemTypeInfo) {
				result.add(((SubListGroupItemTypeInfo) actualItemType).getDetailsField());
			} else {
				List<IFieldInfo> itemFields = actualItemType.getFields();
				for (IFieldInfo field : itemFields) {
					boolean excluded = false;
					for (InfoFilter excludedField : listCustomization.getTreeStructureDiscoverySettings()
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
						if (item instanceof SubListGroupItem) {
							result.add(field);
						} else if (isValidSubListItemType(subListItemType)) {
							result.add(field);
						}
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
		return false;
	}

	@Override
	public IInfoFilter getItemInfoFilter(final ItemPosition itemPosition) {
		return new InfoFilterProxy(super.getItemInfoFilter(itemPosition)) {

			@Override
			public boolean excludeMethod(IMethodInfo method) {
				String methodSignature = method.getSignature();
				for (InfoFilter filter : listCustomization.getMethodsExcludedFromItemDetails()) {
					if (filter.matches(methodSignature)) {
						return true;
					}
				}
				return super.excludeMethod(method);
			}

			@Override
			public boolean excludeField(IFieldInfo field) {
				for (InfoFilter filter : listCustomization.getFieldsExcludedFromItemDetails()) {
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
			result.add(0, new PositionColumnInfo());
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
		if (itemPosition.getItem() instanceof SubListGroupField.ValueListItem) {
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

}
