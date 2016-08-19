package xy.reflect.ui.info.type.iterable.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;

public abstract class AbstractListAction implements IMethodInfo{

	protected ListControl listControl;

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	public ListControl getListControl() {
		return listControl;
	}
	
	public void setListControl(ListControl listControl){
		this.listControl = listControl;
	}

	@Override
	public String getName() {
		return getCaption();
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return null;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return Collections.emptyList();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public IModification getUndoModification(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public String toString() {
		return getCaption();
	}

	
	
	

}
