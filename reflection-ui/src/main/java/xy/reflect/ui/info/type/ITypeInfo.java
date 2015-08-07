package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.List;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public interface ITypeInfo extends IInfo {

	boolean isConcrete();

	List<IMethodInfo> getConstructors();

	List<IFieldInfo> getFields();

	List<IMethodInfo> getMethods();

	Component createFieldControl(Object object, IFieldInfo field);

	boolean supportsInstance(Object object);

	List<ITypeInfo> getPolymorphicInstanceSubTypes();

	boolean hasCustomFieldControl();

	String toString(Object object);
	
	void validate(Object object) throws Exception;
}
