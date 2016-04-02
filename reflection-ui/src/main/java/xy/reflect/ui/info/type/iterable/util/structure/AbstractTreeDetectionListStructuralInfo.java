package xy.reflect.ui.info.type.iterable.util.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultiListField;
import xy.reflect.ui.info.field.MultiListField.MultiListVirtualParent;
import xy.reflect.ui.info.field.MultiListField.MultiListVirtualParentType;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.map.IMapEntryTypeInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract class AbstractTreeDetectionListStructuralInfo implements
		IListStructuralInfo {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo rootItemType;

	protected abstract boolean autoDetectTreeStructure();

	public AbstractTreeDetectionListStructuralInfo(ReflectionUI reflectionUI,
			ITypeInfo rootItemType) {
		this.reflectionUI = reflectionUI;
		this.rootItemType = rootItemType;
	}

	@Override
	public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
		if (!autoDetectTreeStructure()) {
			return null;
		}
		List<IFieldInfo> candidateFields = new ArrayList<IFieldInfo>(
				getItemSubListCandidateFields(itemPosition));
		for (int i = 0; i < candidateFields.size(); i++) {
			candidateFields.set(i, new HiddenNullableFacetFieldInfoProxy(
					reflectionUI, candidateFields.get(i)));
		}
		if (candidateFields.size() == 0) {
			return null;
		} else if (candidateFields.size() == 1) {
			IFieldInfo candidateField = candidateFields.get(0);
			if (itemPosition.getItem() instanceof MultiListVirtualParent) {
				return candidateField;
			} else if (displaysSubListFieldNameAsTreeNode(candidateField,
					itemPosition)) {
				return new MultiListField(reflectionUI,
						Collections.singletonList(candidateField));
			} else {
				return candidateField;
			}
		} else {
			return new MultiListField(reflectionUI, candidateFields);
		}
	}

	protected boolean displaysSubListFieldNameAsTreeNode(
			IFieldInfo subListField, ItemPosition itemPosition) {
		ITypeInfo itemType = itemPosition.getContainingListType().getItemType();
		if (itemType instanceof IMapEntryTypeInfo) {
			return false;
		}
		if (itemType instanceof MultiListVirtualParentType) {
			return false;
		}
		return !subListField.getName().equals(
				itemPosition.getContainingListField().getName());
	}

	protected List<IFieldInfo> getItemSubListCandidateFields(
			ItemPosition itemPosition) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		Object item = itemPosition.getItem();
		ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(item));
		List<IFieldInfo> itemFields = actualItemType.getFields();
		for (IFieldInfo field : itemFields) {
			ITypeInfo fieldType = field.getType();
			if (fieldType instanceof IListTypeInfo) {
				ITypeInfo subListItemType = ((IListTypeInfo) fieldType)
						.getItemType();
				if (item instanceof MultiListVirtualParent) {
					result.add(field);
				} else if (isValidTreeNodeItemType(subListItemType)) {
					result.add(field);
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
				ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType)
						.getItemType();
				if (isValidTreeNodeItemType(entryValuListItemType)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public IInfoCollectionSettings getItemInfoSettings(
			final ItemPosition itemPosition) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((rootItemType == null) ? 0 : rootItemType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractTreeDetectionListStructuralInfo other = (AbstractTreeDetectionListStructuralInfo) obj;
		if (rootItemType == null) {
			if (other.rootItemType != null)
				return false;
		} else if (!rootItemType.equals(other.rootItemType))
			return false;
		return true;
	}
	

}
