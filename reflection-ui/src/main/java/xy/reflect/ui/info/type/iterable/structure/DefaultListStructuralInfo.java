package xy.reflect.ui.info.type.iterable.structure;

import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.StringValueColumnInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;

public class DefaultListStructuralInfo implements IListStructuralInfo {

	protected ReflectionUI reflectionUI;

	public DefaultListStructuralInfo(ReflectionUI reflectionUI) {
		this.reflectionUI=reflectionUI;
	}

	
	@Override
	public List<IColumnInfo> getColumns() {
		return Collections.<IColumnInfo>singletonList(new StringValueColumnInfo(reflectionUI));
	}


	@Override
	public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
		return null;
	}


	@Override
	public IInfoFilter getItemInfoFilter(ItemPosition itemPosition) {
		return IInfoFilter.DEFAULT;
	}

	

}
