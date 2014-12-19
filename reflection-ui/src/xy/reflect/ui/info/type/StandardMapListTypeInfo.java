package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ListControl;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class StandardMapListTypeInfo extends DefaultTypeInfo implements
		IListTypeInfo {

	protected Class<?> keyJavaType;
	protected Class<?> valueJavaType;

	public StandardMapListTypeInfo(ReflectionUI reflectionUI,
			Class<?> javaType, Class<?> keyJavaType, Class<?> valueJavaType) {
		super(reflectionUI, javaType);
		this.keyJavaType = keyJavaType;
		this.valueJavaType = valueJavaType;
	}

	@Override
	public IMapEntryTypeInfo getItemType() {
		return new StandardMapEntryTypeInfo();
	}

	@Override
	public String getName() {
		return "Map<" + ((keyJavaType == null) ? "?" : keyJavaType.getName())
				+ ", "
				+ ((valueJavaType == null) ? "?" : valueJavaType.getName())
				+ ">";
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public String getCaption() {
		if ((keyJavaType == null) && (valueJavaType != null)) {
			return getItemType().getKeyField().getType().getCaption() + " to "
					+ getItemType().getValueField().getType().getCaption()
					+ " Map";
		} else {
			return "Map";
		}
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<?> toStandardList(Object value) {
		List<StandardMapEntry> result = new ArrayList<StandardMapListTypeInfo.StandardMapEntry>();
		for (Object obj : ((Map) value).entrySet()) {
			Map.Entry entry = (Entry) obj;
			result.add(new StandardMapEntry(entry.getKey(), entry.getValue()));
		}
		return result;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		if (ReflectionUIUtils.getNParametersMethod(super.getConstructors(), 0) != null) {
			return super.getConstructors();
		} else {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>(
					super.getConstructors());
			result.add(new AbstractConstructorMethodInfo(this) {

				@Override
				public Object invoke(Object object,
						Map<String, Object> valueByParameterName) {
					return new HashMap<Object, Object>();
				}

				@Override
				public List<IParameterInfo> getParameters() {
					return Collections.emptyList();
				}
			});
			return result;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object fromStandardList(List<?> list) {
		IMethodInfo constructor = ReflectionUIUtils
				.getZeroParameterConstrucor(this);
		Map result = (Map) constructor.invoke(null,
				Collections.<String, Object> emptyMap());
		for (Object item : list) {
			StandardMapEntry entry = (StandardMapEntry) item;
			if (result.containsKey(entry.getKey())) {
				throw new AssertionError("Duplicate key: "
						+ reflectionUI.toInfoString(entry.getKey()));
			}
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	@Override
	public IListStructuralInfo getStructuralInfo() {
		return new DefaultListStructuralInfo(reflectionUI, getItemType()) {

			@Override
			protected boolean shouldShowValueKindColumn() {
				return false;
			}

		};
	}

	@Override
	public Component createNonNullFieldValueControl(Object object,
			IFieldInfo field) {
		return new ListControl(reflectionUI, object, field);
	}

	@Override
	public int hashCode() {
		return javaType.hashCode() + keyJavaType.hashCode()
				+ valueJavaType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!javaType.equals(((DefaultTypeInfo) obj).javaType)) {
			return false;
		}
		if (!ReflectionUIUtils.equalsOrBothNull(keyJavaType,
				((StandardMapListTypeInfo) obj).keyJavaType)) {
			return false;
		}
		if (!ReflectionUIUtils.equalsOrBothNull(valueJavaType,
				((StandardMapListTypeInfo) obj).valueJavaType)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isOrdered() {
		return false;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		if (Map.class.isAssignableFrom(javaType)) {
			if (ReflectionUIUtils
					.getZeroParameterConstrucor(new DefaultTypeInfo(
							new ReflectionUI(), javaType)) != null) {
				return true;
			}
			if (javaType.isAssignableFrom(HashMap.class)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isImmutable() {
		return false;
	}

	@Override
	public boolean hasCustomFieldControl() {
		return true;
	}

	public class StandardMapEntry<K, V> implements Map.Entry<K, V> {
		protected K key;
		protected V value;

		public StandardMapEntry() {
		}

		public StandardMapEntry(K key, V value) {
			super();
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return "" + key + "";
		}

		public K getKey() {
			return key;
		}

		public void setKey(K key) {
			this.key = key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		public StandardMapListTypeInfo getMapType() {
			return StandardMapListTypeInfo.this;
		}

		public ITypeInfo getTypeInfo() {
			return getItemType();
		}

		@Override
		public int hashCode() {
			return ((key != null) ? key.hashCode() : 0)
					+ ((value != null) ? value.hashCode() : 0);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			if (!ReflectionUIUtils.equalsOrBothNull(key,
					((StandardMapEntry<?, ?>) obj).key)) {
				return false;
			}
			if (!ReflectionUIUtils.equalsOrBothNull(value,
					((StandardMapEntry<?, ?>) obj).value)) {
				return false;
			}
			return true;
		}

	}

	public class StandardMapEntryTypeInfo extends DefaultTypeInfo implements
			IMapEntryTypeInfo {

		public StandardMapEntryTypeInfo() {
			super(StandardMapListTypeInfo.this.reflectionUI,
					StandardMapEntry.class);
		}

		@Override
		public String getCaption() {
			return "Entry";
		}

		@Override
		public IFieldInfo getKeyField() {
			try {
				return new GetterFieldInfo(reflectionUI,
						StandardMapEntry.class.getMethod("getKey",
								new Class<?>[0]), StandardMapEntry.class) {
					@Override
					public ITypeInfo getType() {
						if (keyJavaType == null) {
							return null;
						}
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(
								keyJavaType));
					}
				};
			} catch (SecurityException e) {
				throw new AssertionError(e);
			} catch (NoSuchMethodException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public IFieldInfo getValueField() {
			try {
				return new GetterFieldInfo(reflectionUI,
						StandardMapEntry.class.getMethod("getValue",
								new Class<?>[0]), StandardMapEntry.class) {
					@Override
					public ITypeInfo getType() {
						if (valueJavaType == null) {
							return null;
						}
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(
								valueJavaType));
					}
				};
			} catch (SecurityException e) {
				throw new AssertionError(e);
			} catch (NoSuchMethodException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			result.add(getKeyField());
			result.add(getValueField());
			return result;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections
					.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
							StandardMapEntryTypeInfo.this) {

						@SuppressWarnings("rawtypes")
						@Override
						public Object invoke(Object object,
								Map<String, Object> valueByParameterName) {
							return new StandardMapEntry();
						}

						@Override
						public List<IParameterInfo> getParameters() {
							return Collections.emptyList();
						}
					});
		}

	}

}
