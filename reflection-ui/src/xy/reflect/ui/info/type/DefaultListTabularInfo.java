package xy.reflect.ui.info.type;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.info.type.IListTypeInfo.IListTabularInfo;

public class DefaultListTabularInfo implements IListTabularInfo {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo itemType;
	protected List<IFieldInfo> itemFields;
	protected boolean addKindColumn;

	public DefaultListTabularInfo(ReflectionUI reflectionUI,
			ITypeInfo itemType, boolean showKindColumn) {
		this.reflectionUI = reflectionUI;
		this.itemType = itemType;
		this.itemFields = filterFields(this.itemType.getFields());
		this.addKindColumn = showKindColumn;
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
		return itemFields.size() + (addKindColumn ? 1 : 0);
	}

	@Override
	public String getColumnCaption(int columnIndex) {
		if (addKindColumn) {
			if (columnIndex == 0) {
				return "Type";
			}
			return itemFields.get(columnIndex - 1).getCaption();
		} else {
			return itemFields.get(columnIndex).getCaption();
		}
	}

	@Override
	public String getCellValue(IItemPosition itemPosition, int columnIndex) {
		Object item = itemPosition.getItem();
		if (addKindColumn) {
			if (columnIndex == 0) {
				return reflectionUI.getObjectKind(item);
			}
			columnIndex = columnIndex - 1;
		}
		if (itemType.equals(itemPosition.getContainingListType().getItemType())) {
			IFieldInfo itemField = itemFields.get(columnIndex);
			Object value = itemField.getValue(item);
			return reflectionUI.getObjectSummary(value);

		} else {
			return null;
		}
	}

	@Override
	public boolean isValid() {
		return itemFields.size() > 0;
	}

}
