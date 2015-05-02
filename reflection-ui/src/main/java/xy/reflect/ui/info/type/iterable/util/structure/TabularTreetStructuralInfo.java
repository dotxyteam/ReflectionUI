package xy.reflect.ui.info.type.iterable.util.structure;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ListControl.AutoUpdatingFieldItemPosition;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.util.ReflectionUIError;

public class TabularTreetStructuralInfo extends
		AbstractTreeDetectionListStructuralInfo {

	protected List<IFieldInfo> columnFields;

	public TabularTreetStructuralInfo(ReflectionUI reflectionUI,
			ITypeInfo rootItemType) {
		super(reflectionUI, rootItemType);
		if (rootItemType == null) {
			throw new ReflectionUIError(
					"Illegal 'rootItemType' argument: Must not be null");
		}
		this.columnFields = adaptFieldList(this.rootItemType.getFields());
		if (columnFields.size() == 0) {
			throw new ReflectionUIError(
					"Cannot process the 'rootItemType' argument: No compatible field found");
		}
	}

	public List<IFieldInfo> getColumnFields() {
		return columnFields;
	}

	protected List<IFieldInfo> adaptFieldList(List<IFieldInfo> fields) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		IFieldInfo treeField = getTreeColumnField();
		if (treeField != null) {
			result.add(treeField);
		}
		for (IFieldInfo candidateField : fields) {
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
					if (rootItemType.equals(itemPosition
							.getContainingListType().getItemType())) {
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

	protected IFieldInfo getTreeColumnField() {
		if (rootItemType.isConcrete()) {
			return null;
		} else {
			return new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

				@Override
				public String getCaption() {
					return "Type";
				}

				@Override
				public Object getValue(Object object) {
					Object item = ((AutoUpdatingFieldItemPosition) object)
							.getItem();
					return reflectionUI.getObjectKind(item);
				}
			};
		}
	}

	@Override
	public int getColumnCount() {
		return columnFields.size();
	}

	@Override
	public String getColumnCaption(int columnIndex) {
		return columnFields.get(columnIndex).getCaption();
	}

	@Override
	public String getCellValue(ItemPosition itemPosition, int columnIndex) {
		IFieldInfo itemField = columnFields.get(columnIndex);
		return (String) itemField.getValue(itemPosition);
	}

	@Override
	protected boolean autoDetectTreeStructure() {
		return getTreeColumnField() != null;
	}

}
