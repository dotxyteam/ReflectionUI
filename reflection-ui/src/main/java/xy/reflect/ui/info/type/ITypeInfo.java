package xy.reflect.ui.info.type;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

public interface ITypeInfo extends IInfo {

	ITypeInfoSource getSource();

	boolean canPersist();

	void save(Object object, OutputStream out);

	void load(Object object, InputStream in);

	boolean isImmutable();

	boolean isPrimitive();

	boolean isConcrete();

	List<IMethodInfo> getConstructors();

	List<IFieldInfo> getFields();

	List<IMethodInfo> getMethods();

	boolean supportsInstance(Object object);

	List<ITypeInfo> getPolymorphicInstanceSubTypes();

	String toString(Object object);

	void validate(Object object) throws Exception;

	boolean canCopy(Object object);

	Object copy(Object object);

	boolean isModificationStackAccessible();

	ResourcePath getIconImagePath();

	ITypeInfo.FieldsLayout getFieldsLayout();

	ITypeInfo.MethodsLayout getMethodsLayout();

	MenuModel getMenuModel();

	boolean onFormVisibilityChange(Object object, boolean visible);

	Dimension getFormPreferredSize();

	ColorSpecification getFormBackgroundColor();

	ColorSpecification getFormForegroundColor();

	ResourcePath getFormBackgroundImagePath();

	public enum FieldsLayout {
		HORIZONTAL_FLOW, VERTICAL_FLOW
	}

	public enum MethodsLayout {
		HORIZONTAL_FLOW, VERTICAL_FLOW
	}

}
