package xy.reflect.ui.info.type.enumeration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings({ "rawtypes", "unused" })
public class StandardEnumerationTypeInfo extends DefaultTypeInfo implements IEnumerationTypeInfo {

	public StandardEnumerationTypeInfo(ReflectionUI reflectionUI, Class javaEnumType) {
		super(reflectionUI, javaEnumType);
	}

	@Override
	public boolean isDynamicEnumeration() {
		return false;
	}

	@Override
	public boolean isImmutable() {
		return true;
	}

	@Override
	public Object[] getPossibleValues() {
		return javaType.getEnumConstants();
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

			@Override
			public ITypeInfo getReturnValueType() {
				return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(StandardEnumerationTypeInfo.this));
			}

			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				return javaType.getEnumConstants()[0];
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		});
	}

	@Override
	public IEnumerationItemInfo getValueInfo(final Object object) {
		if (object == null) {
			return null;
		} else {
			return new IEnumerationItemInfo() {

				@Override
				public Map<String, Object> getSpecificProperties() {
					return Collections.emptyMap();
				}

				@Override
				public String getOnlineHelp() {
					return null;
				}

				@Override
				public String getName() {
					return object.toString();
				}

				@Override
				public String getCaption() {
					return object.toString();
				}

				@Override
				public String toString() {
					return object.toString();
				}
			};
		}
	}

	@Override
	public String toString() {
		return "StandardEnumerationTypeInfo [javaType=" + javaType + "]";
	}

}
