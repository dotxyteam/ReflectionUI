package xy.reflect.ui.info.type;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoWrapper;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultiSubListField;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.info.type.IListTypeInfo.IListHierarchicalInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultListHierarchicalInfo implements IListHierarchicalInfo {

	protected ReflectionUI reflectionUI;

	public DefaultListHierarchicalInfo(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	public IFieldInfo getItemSubListField(IItemPosition itemPosition) {
		List<IFieldInfo> candidateFields = getItemSubListCandidateFields(itemPosition);
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
			ITypeInfo parentListItemType = itemPosition.getParentItemPosition()
					.getContainingListType().getItemType();
			IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) actualItemType;
			ITypeInfo entryValueType = entryType.getValueType();
			if (entryValueType instanceof IListTypeInfo) {
				ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType)
						.getItemType();
				if (ReflectionUIUtils.equalsOrBothNull(parentListItemType,
						entryValuListItemType)) {
					IFieldInfo entryValueField = ReflectionUIUtils
							.findInfoByName(entryType.getFields(), "value");
					result.add(entryValueField);
				}
			}
		} else {
			List<IFieldInfo> itemFields = actualItemType.getFields();
			for (IFieldInfo field : itemFields) {
				ITypeInfo fieldType = field.getType();
				if (fieldType instanceof IListTypeInfo) {
					ITypeInfo subListItemType = ((IListTypeInfo) fieldType)
							.getItemType();
					if (subListItemType instanceof IMapEntryTypeInfo) {
						IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) subListItemType;
						ITypeInfo entryValueType = entryType.getValueType();
						if (entryValueType instanceof IListTypeInfo) {
							ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType)
									.getItemType();
							if (ReflectionUIUtils.equalsOrBothNull(itemType,
									entryValuListItemType)) {
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
		for (int i = 0; i < result.size(); i++) {
			result.set(i, new HiddenNullableFacetFieldInfoWrapper(reflectionUI,
					result.get(i)));
		}
		return result;
	}

	@Override
	public List<String> getItemDetailsExcludedFieldNames(
			IItemPosition itemPosition) {
		List<String> result = new ArrayList<String>();
		for (IFieldInfo field : getItemSubListCandidateFields(itemPosition)) {
			result.add(field.getName());
		}
		return result;
	}

}
