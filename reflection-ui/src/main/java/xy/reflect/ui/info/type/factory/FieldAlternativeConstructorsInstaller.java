package xy.reflect.ui.info.type.factory;

import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class FieldAlternativeConstructorsInstaller extends InfoProxyFactory {

	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	public FieldAlternativeConstructorsInstaller(ReflectionUI reflectionUI, Object object, IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
	}

	protected String getObjectTypeName() {
		ITypeInfo objectType = (object == null) ? null
				: reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return ((objectType == null) ? "<unknown>" : objectType.getName());
	}

	protected String getFieldName() {
		return field.getName();
	}

	@Override
	public String getIdentifier() {
		return "FieldAlternativeConstructorsInstaller [field=" + getFieldName() + ", objectType="
				+ getObjectTypeName() + "]";
	}

	@Override
	protected List<IMethodInfo> getConstructors(ITypeInfo type) {
		return field.getAlternativeConstructors(object);
	}

}