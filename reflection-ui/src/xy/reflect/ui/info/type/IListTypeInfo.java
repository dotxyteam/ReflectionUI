package xy.reflect.ui.info.type;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;

public interface IListTypeInfo extends ITypeInfo {
	ITypeInfo getItemType();

	List<?> toStandardList(Object value);

	Object fromStandardList(List<?> list);

	IListHierarchicalInfo getHierarchicalInfo();
	
	IListTabularInfo getTabularInfo();
	
	boolean isOrdered();
	
	public interface IListHierarchicalInfo{
		IFieldInfo getItemSubListField(IItemPosition itemPosition);

		List<String> getItemDetailsExcludedFieldNames(IItemPosition itemPosition);		
	}

	public interface IListTabularInfo{

		String getCellValue(IItemPosition itemPosition, int columnIndex);

		String getColumnCaption(int columnIndex);

		int getColumnCount();

		boolean isValid();
		
	}
	
	public interface IItemPosition {

		public abstract boolean isContainingListReadOnly();

		public abstract int getIndex();

		public abstract Object getItem();

		public abstract IFieldInfo getContainingListField();

		public abstract Object getContainingList();

		public abstract IListTypeInfo getContainingListType();

		public abstract IItemPosition getParentItemPosition();

		public abstract int getDepth();

		public abstract Object getContainingListOwner();

		public abstract IItemPosition getSibling(int index2);

		public abstract boolean isRootListItemPosition();

		public abstract IItemPosition getRootListItemPosition();

	}

}
