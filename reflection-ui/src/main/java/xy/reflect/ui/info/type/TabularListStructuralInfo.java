package xy.reflect.ui.info.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultiSubListField;
import xy.reflect.ui.info.field.MultiSubListField.VirtualItem;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.info.type.IListTypeInfo.IListStructuralInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TabularListStructuralInfo implements IListStructuralInfo {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo rootItemType;
	protected List<IFieldInfo> rootItemFields;

	public TabularListStructuralInfo(ReflectionUI reflectionUI,
			ITypeInfo rootItemType) {
		this.reflectionUI = reflectionUI;
		this.rootItemType = rootItemType;
		if (rootItemType != null) {
			this.rootItemFields = filterFields(this.rootItemType.getFields());
		}
	}

	@Override
	public IFieldInfo getItemSubListField(IItemPosition itemPosition) {
		List<IFieldInfo> candidateFields = getItemSubListCandidateFields(itemPosition);
		for (int i = 0; i < candidateFields.size(); i++) {
			candidateFields.set(i, new HiddenNullableFacetFieldInfoProxy(
					reflectionUI, candidateFields.get(i)));
		}
		if (candidateFields.size() == 0) {
			return null;
		} else if (candidateFields.size() == 1) {
			return candidateFields.get(0);
		} else {
			return new MultiSubListField(reflectionUI, candidateFields);
		}
	}

	protected List<IFieldInfo> getItemSubListCandidateFields(
			IItemPosition itemPosition) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		ITypeInfo itemType = itemPosition.getContainingListType().getItemType();
		Object item = itemPosition.getItem();
		ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(item));
		if ((actualItemType instanceof IMapEntryTypeInfo)
				&& (itemPosition.getParentItemPosition() != null)) {
			IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) actualItemType;
			IFieldInfo entryValueField = entryType.getValueField();
			ITypeInfo entryValueType = entryValueField.getType();
			if (entryValueType instanceof IListTypeInfo) {
				if (!isTabular()) {
					result.add(entryValueField);
				} else {
					ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType)
							.getItemType();
					ITypeInfo parentListItemType = itemPosition
							.getParentItemPosition().getContainingListType()
							.getItemType();
					if (ReflectionUIUtils.equalsOrBothNull(parentListItemType,
							entryValuListItemType)) {
						result.add(entryValueField);
					}
				}
			}
		} else {
			List<IFieldInfo> itemFields = actualItemType.getFields();
			for (IFieldInfo field : itemFields) {
				ITypeInfo fieldType = field.getType();
				if (fieldType instanceof IListTypeInfo) {
					if (!isTabular()) {
						result.add(field);
					} else {
						ITypeInfo subListItemType = ((IListTypeInfo) fieldType)
								.getItemType();
						if (subListItemType instanceof IMapEntryTypeInfo) {
							IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) subListItemType;
							ITypeInfo entryValueType = entryType
									.getValueField().getType();
							if (entryValueType instanceof IListTypeInfo) {
								ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType)
										.getItemType();
								if (ReflectionUIUtils.equalsOrBothNull(
										itemType, entryValuListItemType)) {
									result.add(field);
								}
							}
						} else {
							if (ReflectionUIUtils.equalsOrBothNull(itemType,
									subListItemType)) {
								result.add(field);
							}
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<IFieldInfo> getItemSubListFieldsToExcludeFromDetailsView(
			IItemPosition itemPosition) {
		Object item = itemPosition.getItem();
		if (item instanceof VirtualItem) {
			return ((IListTypeInfo) new MultiSubListField(reflectionUI,
					Collections.<IFieldInfo> emptyList()).getType())
					.getStructuralInfo()
					.getItemSubListFieldsToExcludeFromDetailsView(itemPosition);
		}
		return getItemSubListCandidateFields(itemPosition);
	}

	protected List<IFieldInfo> filterFields(List<IFieldInfo> fields) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo candidateField : fields) {
			if (candidateField.getType() instanceof IListTypeInfo) {
				continue;
			}
			result.add(candidateField);
		}
		return result;
	}

	@Override
	public int getColumnCount() {
		if (!isTabular()) {
			return 1;
		}
		return rootItemFields.size() + (shouldShowValueKindColumn() ? 1 : 0);
	}

	protected boolean shouldShowValueKindColumn() {
		if (!isTabular()) {
			return false;
		}
		return !rootItemType.isConcrete();
	}

	@Override
	public String getColumnCaption(int columnIndex) {
		if (!isTabular()) {
			return "";
		}
		if (shouldShowValueKindColumn()) {
			if (columnIndex == 0) {
				return "Type";
			}
			return rootItemFields.get(columnIndex - 1).getCaption();
		} else {
			return rootItemFields.get(columnIndex).getCaption();
		}
	}

	@Override
	public String getCellValue(IItemPosition itemPosition, int columnIndex) {
		if (!isTabular()) {
			if (columnIndex != 0) {
				throw new ReflectionUIError();
			}
			return reflectionUI.toString(itemPosition.getItem());
		}
		Object item = itemPosition.getItem();
		if (shouldShowValueKindColumn()) {
			if (columnIndex == 0) {
				return reflectionUI.getObjectKind(item);
			}
			columnIndex = columnIndex - 1;
		}
		if (rootItemType.equals(itemPosition.getContainingListType()
				.getItemType())) {
			IFieldInfo itemField = rootItemFields.get(columnIndex);
			Object value = itemField.getValue(item);
			return reflectionUI.toString(value);
		} else {
			if (columnIndex == 0) {
				return reflectionUI.toString(itemPosition.getItem());
			} else {
				return null;
			}
		}
	}

	protected boolean isTabular() {
		return (rootItemFields != null) && (rootItemFields.size() > 0);
	}

}
