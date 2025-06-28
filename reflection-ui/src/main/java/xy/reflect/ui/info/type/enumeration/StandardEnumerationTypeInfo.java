
package xy.reflect.ui.info.type.enumeration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Enumeration type information extracted from the Java enum type encapsulated
 * in the given type information source.
 * 
 * @author olitank
 *
 */
public class StandardEnumerationTypeInfo extends DefaultTypeInfo implements IEnumerationTypeInfo {

	public StandardEnumerationTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source) {
		super(reflectionUI, source);
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

			@Override
			public ITypeInfo getReturnValueType() {
				return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(StandardEnumerationTypeInfo.this, null));
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
	public IEnumerationItemInfo getValueInfo(final Object item) {
		if (item == null) {
			return null;
		} else {
			return new StandardEnumerationItemInfo(item);
		}
	}

	@Override
	public String toString() {
		return "StandardEnumerationTypeInfo [source=" + getSource() + "]";
	}

	public static class StandardEnumerationItemInfo implements IEnumerationItemInfo {

		protected Object item;

		public StandardEnumerationItemInfo(Object item) {
			this.item = item;
		}

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
			return item.toString();
		}

		@Override
		public Object getValue() {
			return item;
		}

		@Override
		public String getCaption() {
			return ReflectionUIUtils.identifierToCaption(getName());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((item == null) ? 0 : item.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StandardEnumerationItemInfo other = (StandardEnumerationItemInfo) obj;
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "StandardEnumerationItemInfo [item=" + item + "]";
		}

	}

}
