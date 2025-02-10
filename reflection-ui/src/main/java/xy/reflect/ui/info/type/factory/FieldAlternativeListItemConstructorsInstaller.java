package xy.reflect.ui.info.type.factory;

import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

public class FieldAlternativeListItemConstructorsInstaller extends InfoProxyFactory {

	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	public FieldAlternativeListItemConstructorsInstaller(ReflectionUI reflectionUI, Object object,
			IFieldInfo field) {
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
		return "FieldAlternativeListItemConstructorsInstaller [field=" + getFieldName() + ", objectType="
				+ getObjectTypeName() + "]";
	}

	@Override
	protected ITypeInfo getItemType(IListTypeInfo type) {
		ITypeInfo result = super.getItemType(type);
		if (result == null) {
			result = reflectionUI.getTypeInfo(new JavaTypeInfoSource(reflectionUI, Object.class, null));
		}
		result = new InfoProxyFactory() {

			@Override
			public String getIdentifier() {
				return "ItemConstructorsInstaller [parent=FieldAlternativeListItemConstructorsInstaller [field="
						+ getFieldName() + ", objectType=" + getObjectTypeName() + "]]";
			}

			@Override
			protected List<IMethodInfo> getConstructors(ITypeInfo type) {
				return field.getAlternativeListItemConstructors(object);
			}

		}.wrapTypeInfo(result);
		return result;
	}
}