package xy.reflect.ui.info.type.iterable.structure;

import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.StringValueColumnInfo;

public class DefaultListStructuralInfo extends AbstractTreeListStructuralInfo {

	public DefaultListStructuralInfo(ReflectionUI reflectionUI) {
		super(reflectionUI, null);
	}

	@Override
	protected boolean isValidSubListNodeItemType(ITypeInfo type) {
		return false;
	}

	@Override
	public List<IColumnInfo> getColumns() {
		return Collections.<IColumnInfo>singletonList(new StringValueColumnInfo(reflectionUI));
	}

	@Override
	protected boolean autoDetectTreeStructure() {
		return true;
	}

}
