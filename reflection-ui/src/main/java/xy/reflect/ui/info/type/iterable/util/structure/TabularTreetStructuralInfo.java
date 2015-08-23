package xy.reflect.ui.info.type.iterable.util.structure;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
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
					Object item = ((ItemPosition) object).getItem();
					return reflectionUI.getObjectKind(item);
				}

				@Override
				public String toString() {
					return getCaption();
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
	public Image getCellIconImage(ItemPosition itemPosition, int columnIndex) {
		if (columnIndex == 0) {
			return reflectionUI.getIconImage(itemPosition.getItem());
		}
		return null;
	}

	@Override
	protected boolean autoDetectTreeStructure() {
		return getTreeColumnField() != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((columnFields == null) ? 0 : columnFields.hashCode());
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
		TabularTreetStructuralInfo other = (TabularTreetStructuralInfo) obj;
		if (columnFields == null) {
			if (other.columnFields != null)
				return false;
		} else if (!columnFields.equals(other.columnFields))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TabularTreetStructuralInfo [rootItemType=" + rootItemType
				+ ", columnFields=" + columnFields + "]";
	}

}
