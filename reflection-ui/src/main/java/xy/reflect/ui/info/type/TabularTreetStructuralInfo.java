package xy.reflect.ui.info.type;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.util.ReflectionUIError;

public class TabularTreetStructuralInfo extends AbstractTreeDetectionListStructuralInfo{

	protected List<IFieldInfo> rootItemFields;

	public TabularTreetStructuralInfo(ReflectionUI reflectionUI,
			ITypeInfo rootItemType) {
		super(reflectionUI, rootItemType);
		if (rootItemType != null) {
			this.rootItemFields = filterFields(this.rootItemType.getFields());
		}
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
		if (!isFieldBased()) {
			return 1;
		}
		return rootItemFields.size() + (shouldShowValueKindColumn() ? 1 : 0);
	}

	protected boolean shouldShowValueKindColumn() {
		if (!isFieldBased()) {
			return false;
		}
		return !rootItemType.isConcrete();
	}

	@Override
	public String getColumnCaption(int columnIndex) {
		if (!isFieldBased()) {
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
		if (!isFieldBased()) {
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

	@Override
	protected boolean isFieldBased() {
		return (rootItemFields != null) && (rootItemFields.size() > 0);
	}

}
