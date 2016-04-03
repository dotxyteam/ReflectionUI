package xy.reflect.ui.info.type;

import java.awt.Component;
import java.awt.Image;
import java.util.List;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public interface ITypeInfo extends IInfo {

	boolean isConcrete();

	List<IMethodInfo> getConstructors();

	List<IFieldInfo> getFields();

	List<IMethodInfo> getMethods();

	boolean supportsInstance(Object object);

	List<ITypeInfo> getPolymorphicInstanceSubTypes();

	String toString(Object object);
	
	void validate(Object object) throws Exception;

	Image getIconImage(Object object);
	
	Component createFieldControl(Object object, IFieldInfo field);

	boolean hasCustomFieldControl(Object object, IFieldInfo field);
}
