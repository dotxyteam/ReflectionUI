package xy.reflect.ui.info.type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings("rawtypes")
public class StandardEnumerationTypeInfo extends DefaultTypeInfo implements
		IEnumerationTypeInfo {

	public StandardEnumerationTypeInfo(ReflectionUI reflectionUI,
			Class javaEnumType) {
		super(reflectionUI, javaEnumType);
	}

	@Override
	public Object[] getPossibleValues() {
		return javaType.getEnumConstants();
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
						StandardEnumerationTypeInfo.this) {

					@Override
					public Object invoke(Object object,
							InvocationData invocationData) {
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
					return reflectionUI.toString(object);
				}
				
				@Override
				public String getCaption() {
					return reflectionUI.toString(object);
				}
			};
		}
	}
	
	
	

}