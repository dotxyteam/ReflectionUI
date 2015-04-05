package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.List;

import xy.reflect.ui.control.ListControl.AutoUpdatingFieldItemPosition;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public interface IListTypeInfo extends ITypeInfo {
	ITypeInfo getItemType();

	Object[] toListValue(Object value);

	Object fromListValue(Object[] listValue);

	IListStructuralInfo getStructuralInfo();

	boolean isOrdered();

	List<IListAction> getSpecificActions(Object object, IFieldInfo field,
			List<? extends ItemPosition> selection);

	public interface IListStructuralInfo {

		String getCellValue(ItemPosition itemPosition, int columnIndex);

		String getColumnCaption(int columnIndex);

		int getColumnCount();

		IFieldInfo getItemSubListField(ItemPosition itemPosition);

		IInfoCollectionSettings getItemInfoSettings(
				ItemPosition itemPosition);
	}

	public class ItemPosition {

		protected IFieldInfo containingListField;
		protected AutoUpdatingFieldItemPosition parentItemPosition;
		protected int index;
		protected Object rootListOwner;

		public ItemPosition(IFieldInfo containingListField,
				AutoUpdatingFieldItemPosition parentItemPosition, int index, Object rootListOwner) {
			this.containingListField = containingListField;
			this.parentItemPosition = parentItemPosition;
			this.index = index;
			this.rootListOwner = rootListOwner;
		}

		public boolean supportsValue(Object object) {
			ITypeInfo itemType = getContainingListType().getItemType();
			return (itemType == null) || (itemType.supportsValue(object));
		}

		public boolean isContainingListReadOnly() {
			if (getContainingListField().isReadOnly()) {
				return true;
			}
			if (getParentItemPosition() == null) {
				return false;
			}
			return getRootListItemPosition().isContainingListReadOnly();
		}

		public int getIndex() {
			return index;
		}

		public Object getItem() {
			Object[] listValue = getContainingListValue();
			if (index < 0) {
				return null;
			}
			if (index >= listValue.length) {
				return null;
			}
			return listValue[index];
		}

		public IFieldInfo getContainingListField() {
			return containingListField;
		}

		public Object[] getContainingListValue() {
			Object list = getContainingListField().getValue(getContainingListOwner());
			return getContainingListType().toListValue(list);
		}

		public IListTypeInfo getContainingListType() {
			return (IListTypeInfo) getContainingListField().getType();
		}

		public AutoUpdatingFieldItemPosition getParentItemPosition() {
			return parentItemPosition;
		}

		@Override
		public String toString() {
			return "Item(depth=" + getDepth() + ", position=" + getIndex()
					+ ", value=" + getItem() + ")";
		}

		public int getDepth() {
			int result = 0;
			ItemPosition current = this;
			while (current.getParentItemPosition() != null) {
				current = current.getParentItemPosition();
				result++;
			}
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ItemPosition)) {
				return false;
			}
			ItemPosition other = (ItemPosition) obj;
			if (!ReflectionUIUtils.equalsOrBothNull(getParentItemPosition(),
					other.getParentItemPosition())) {
				return false;
			}
			if (getIndex() != other.getIndex()) {
				return false;
			}
			return true;
		}

		public Object getContainingListOwner() {
			if (getParentItemPosition() != null) {
				return getParentItemPosition().getItem();
			} else {
				return rootListOwner;
			}

		}

		
		public ItemPosition getSibling(int index2) {
			return new ItemPosition(getContainingListField(),
					getParentItemPosition(), index2, rootListOwner);
		}

		public boolean isRootListItemPosition() {
			return getRootListItemPosition().equals(this);
		}

		public ItemPosition getRootListItemPosition() {
			ItemPosition current = this;
			while (current.getParentItemPosition() != null) {
				current = current.getParentItemPosition();
			}
			return current;
		}

	}

	public interface IListAction {

		void perform(Component listControl);

		String getTitle();

	}
}
