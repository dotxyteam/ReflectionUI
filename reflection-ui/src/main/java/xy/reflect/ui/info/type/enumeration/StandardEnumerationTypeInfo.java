


package xy.reflect.ui.info.type.enumeration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;

/**
 * Enumeration type information extracted from the Java enum type encapsulated
 * in the given type information source.
 * 
 * @author olitank
 *
 */
public class StandardEnumerationTypeInfo extends DefaultTypeInfo implements IEnumerationTypeInfo {

	public StandardEnumerationTypeInfo(JavaTypeInfoSource source) {
		super(source);
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
	public Object[] getValues() {
		return getJavaType().getEnumConstants();
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

			ITypeInfo returnValueType;

			@Override
			public ITypeInfo getReturnValueType() {
				if (returnValueType == null) {
					returnValueType = reflectionUI
							.buildTypeInfo(new PrecomputedTypeInfoSource(StandardEnumerationTypeInfo.this, null));
				}
				return returnValueType;
			}

			@Override
			public Object invoke(Object ignore, InvocationData invocationData) {
				return getJavaType().getEnumConstants()[0];
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
				public ResourcePath getIconImagePath() {
					return null;
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
				public Object getValue() {
					return object;
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
		return "StandardEnumerationTypeInfo [source=" + getSource() + "]";
	}

}
