package xy.reflect.ui.info.type.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class InfoCustomizations {

	transient protected CustomizationsProxyGenerator proxyGenerator;
	protected List<TypeCustomization> typeCustomizations = new ArrayList<InfoCustomizations.TypeCustomization>();

	public InfoCustomizations(ReflectionUI reflectionUI) {
		proxyGenerator = new CustomizationsProxyGenerator(reflectionUI);
	}

	public ITypeInfo get(ITypeInfo type) {
		return proxyGenerator.get(type);
	}

	public List<TypeCustomization> getTypeCustomizations() {
		return typeCustomizations;
	}

	public void setTypeCustomizations(List<TypeCustomization> typeCustomizations) {
		this.typeCustomizations = typeCustomizations;
	}

	public void loadFromFile(File input) throws IOException {
		FileInputStream stream = new FileInputStream(input);
		try {
			loadFromStream(stream);
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void loadFromStream(InputStream input) {
		XStream xstream = getXStream();
		InfoCustomizations loaded = (InfoCustomizations) xstream.fromXML(input);
		typeCustomizations = loaded.typeCustomizations;
	}

	public void saveToFile(File output) throws IOException {
		FileOutputStream stream = new FileOutputStream(output);
		try {
			saveToStream(stream);
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void saveToStream(OutputStream output) throws IOException {
		XStream xstream = getXStream();
		xstream.toXML(this, output);
	}

	protected XStream getXStream() {
		XStream result = new XStream();
		result.registerConverter(new JavaBeanConverter(result.getMapper()), -20);
		return result;
	}

	public ParameterCustomization getParameterCustomization(String containingTypeName, String methodSignature,
			String paramName) {
		return getParameterCustomization(containingTypeName, methodSignature, paramName, false);
	}

	public ParameterCustomization getParameterCustomization(String containingTypeName, String methodSignature,
			String paramName, boolean create) {
		MethodCustomization m = getMethodCustomization(containingTypeName, methodSignature, create);
		if (m != null) {
			for (ParameterCustomization p : m.parametersCustomizations) {
				if (paramName.equals(p.parameterName)) {
					return p;
				}
			}
			if (create) {
				ParameterCustomization p = new ParameterCustomization(paramName);
				m.parametersCustomizations.add(p);
				return p;
			}
		}
		return null;
	}

	public FieldCustomization getFieldCustomization(String containingTypeName, String fieldName) {
		return getFieldCustomization(containingTypeName, fieldName, false);
	}

	public FieldCustomization getFieldCustomization(String containingTypeName, String fieldName, boolean create) {
		TypeCustomization t = getTypeCustomization(containingTypeName, create);
		if (t != null) {
			for (FieldCustomization f : t.fieldsCustomizations) {
				if (fieldName.equals(f.fieldName)) {
					return f;
				}
			}
			if (create) {
				FieldCustomization f = new FieldCustomization(fieldName);
				t.fieldsCustomizations.add(f);
				return f;
			}
		}
		return null;
	}

	public MethodCustomization getMethodCustomization(String containingTypeName, String methodSignature) {
		return getMethodCustomization(containingTypeName, methodSignature, false);
	}

	public MethodCustomization getMethodCustomization(String containingTypeName, String methodSignature,
			boolean create) {
		TypeCustomization t = getTypeCustomization(containingTypeName, create);
		if (t != null) {
			for (MethodCustomization m : t.methodsCustomizations) {
				if (methodSignature.equals(m.methodSignature)) {
					return m;
				}
			}
			if (create) {
				MethodCustomization m = new MethodCustomization(methodSignature);
				t.methodsCustomizations.add(m);
				return m;
			}
		}
		return null;
	}

	public TypeCustomization getTypeCustomization(String containingTypeName) {
		return getTypeCustomization(containingTypeName, false);
	}

	public TypeCustomization getTypeCustomization(String containingTypeName, boolean create) {
		for (TypeCustomization t : typeCustomizations) {
			if (containingTypeName.equals(t.typeName)) {
				return t;
			}
		}
		if (create) {
			TypeCustomization t = new TypeCustomization(containingTypeName);
			typeCustomizations.add(t);
			return t;
		}
		return null;
	}

	protected static <I extends IInfo> List<String> getInfosOrderAfterMove(List<I> infos, I info, int offset) {
		int index = infos.indexOf(info);
		infos = new ArrayList<I>(infos);
		infos.remove(index);
		int offsetSign = ((offset > 0) ? 1 : -1);
		InfoCategory infoCategory = getCategory(info);
		for (int iOffset = 0; iOffset != offset; iOffset = iOffset + offsetSign) {
			while (true) {
				index = index + offsetSign;
				if (index == 0) {
					break;
				}
				if (index == infos.size()) {
					break;
				}
				I otherInfo = infos.get(index);
				InfoCategory otherFieldCategory = getCategory(otherInfo);
				if (ReflectionUIUtils.equalsOrBothNull(infoCategory, otherFieldCategory)) {
					break;
				}
			}
			if (index == 0) {
				break;
			}
			if (index == infos.size()) {
				break;
			}
		}
		infos.add(index, info);
		ArrayList<String> newOrder = new ArrayList<String>();
		for (I i : infos) {
			newOrder.add(i.getName());
		}
		return newOrder;
	}

	protected static InfoCategory getCategory(IInfo info) {
		if (info instanceof IFieldInfo) {
			return ((IFieldInfo) info).getCategory();
		} else if (info instanceof IMethodInfo) {
			return ((IMethodInfo) info).getCategory();
		} else {
			return null;
		}
	}

	public static class TypeCustomization {
		private String typeName;
		private String customTypeCaption;
		private List<FieldCustomization> fieldsCustomizations = new ArrayList<InfoCustomizations.FieldCustomization>();
		private List<MethodCustomization> methodsCustomizations = new ArrayList<InfoCustomizations.MethodCustomization>();
		private List<String> customFieldsOrder;
		private List<String> customMethodsOrder;

		public TypeCustomization(String TypeName) {
			super();
			this.typeName = TypeName;
		}

		public String getTypeName() {
			return typeName;
		}

		public List<String> getCustomFieldsOrder() {
			return customFieldsOrder;
		}

		public List<String> getCustomMethodsOrder() {
			return customMethodsOrder;
		}

		public String getCustomTypeCaption() {
			return customTypeCaption;
		}

		public void setCustomTypeCaption(String customTypeCaption) {
			this.customTypeCaption = customTypeCaption;
		}

		public List<FieldCustomization> getFieldsCustomizations() {
			return fieldsCustomizations;
		}

		public void setFieldsCustomizations(List<FieldCustomization> fieldsCustomizations) {
			this.fieldsCustomizations = fieldsCustomizations;
		}

		public List<MethodCustomization> getMethodsCustomizations() {
			return methodsCustomizations;
		}

		public void setMethodsCustomizations(List<MethodCustomization> methodsCustomizations) {
			this.methodsCustomizations = methodsCustomizations;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
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
			TypeCustomization other = (TypeCustomization) obj;
			if (typeName == null) {
				if (other.typeName != null)
					return false;
			} else if (!typeName.equals(other.typeName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TypeCustomization [typeName=" + typeName + "]";
		}

		public void moveField(InfoCustomizations parent, List<IFieldInfo> customizedFields, String fieldName, int offset) {
			IFieldInfo customizedField = ReflectionUIUtils.findInfoByName(customizedFields, fieldName);
			if (customizedField == null) {
				return;
			}
			customFieldsOrder = getInfosOrderAfterMove(customizedFields, customizedField, offset);
		}

		public void moveMethod(InfoCustomizations parent, List<IMethodInfo> customizedMethods, String methodSignature, int offset) {
			IMethodInfo customizedMethod = ReflectionUIUtils.findMethodBySignature(customizedMethods, methodSignature);
			if (customizedMethod == null) {
				return;
			}
			customMethodsOrder = getInfosOrderAfterMove(customizedMethods, customizedMethod, offset);
		}

	}

	public static class FieldCustomization {
		private String fieldName;
		private String customFieldCaption;
		private boolean hidden = false;
		private boolean nullableFacetHidden = false;
		private boolean getOnlyForced = false;

		public FieldCustomization(String FieldName) {
			super();
			this.fieldName = FieldName;
		}

		public String getFieldName() {
			return fieldName;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public boolean isNullableFacetHidden() {
			return nullableFacetHidden;
		}

		public void setNullableFacetHidden(boolean nullableFacetHidden) {
			this.nullableFacetHidden = nullableFacetHidden;
		}

		public boolean isGetOnlyForced() {
			return getOnlyForced;
		}

		public void setGetOnlyForced(boolean getOnlyForced) {
			this.getOnlyForced = getOnlyForced;
		}

		public String getCustomFieldCaption() {
			return customFieldCaption;
		}

		public void setCustomFieldCaption(String customFieldCaption) {
			this.customFieldCaption = customFieldCaption;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
			FieldCustomization other = (FieldCustomization) obj;
			if (fieldName == null) {
				if (other.fieldName != null)
					return false;
			} else if (!fieldName.equals(other.fieldName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FieldCustomization [fieldName=" + fieldName + "]";
		}
	}

	public static class MethodCustomization {
		private String methodSignature;
		private String customMethodCaption;
		private boolean hidden = false;
		private boolean readOnlyForced = false;
		private List<ParameterCustomization> parametersCustomizations = new ArrayList<InfoCustomizations.ParameterCustomization>();

		public MethodCustomization(String methodSignature) {
			super();
			this.methodSignature = methodSignature;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public boolean isReadOnlyForced() {
			return readOnlyForced;
		}

		public void setReadOnlyForced(boolean readOnlyForced) {
			this.readOnlyForced = readOnlyForced;
		}

		public String getMethodSignature() {
			return methodSignature;
		}

		public String getCustomMethodCaption() {
			return customMethodCaption;
		}

		public void setCustomMethodCaption(String customMethodCaption) {
			this.customMethodCaption = customMethodCaption;
		}

		public List<ParameterCustomization> getParametersCustomizations() {
			return parametersCustomizations;
		}

		public void setParametersCustomizations(List<ParameterCustomization> parametersCustomizations) {
			this.parametersCustomizations = parametersCustomizations;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((customMethodCaption == null) ? 0 : customMethodCaption.hashCode());
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
			MethodCustomization other = (MethodCustomization) obj;
			if (customMethodCaption == null) {
				if (other.customMethodCaption != null)
					return false;
			} else if (!customMethodCaption.equals(other.customMethodCaption))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "MethodCustomization [methodSignature=" + methodSignature + "]";
		}

	}

	public static class ParameterCustomization {
		private String parameterName;
		private String customParameterCaption;
		private boolean hidden = false;
		private boolean nullableFacetHidden = false;

		public ParameterCustomization(String ParameterName) {
			super();
			this.parameterName = ParameterName;
		}

		public String getParameterName() {
			return parameterName;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public boolean isNullableFacetHidden() {
			return nullableFacetHidden;
		}

		public void setNullableFacetHidden(boolean nullableFacetHidden) {
			this.nullableFacetHidden = nullableFacetHidden;
		}

		public String getCustomParameterCaption() {
			return customParameterCaption;
		}

		public void setCustomParameterCaption(String customParameterCaption) {
			this.customParameterCaption = customParameterCaption;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((parameterName == null) ? 0 : parameterName.hashCode());
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
			ParameterCustomization other = (ParameterCustomization) obj;
			if (parameterName == null) {
				if (other.parameterName != null)
					return false;
			} else if (!parameterName.equals(other.parameterName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ParameterCustomization [parameterName=" + parameterName + "]";
		}

	}

	protected class CustomizationsProxyGenerator extends HiddenNullableFacetsInfoProxyGenerator {

		public CustomizationsProxyGenerator(ReflectionUI reflectionUI) {
			super(reflectionUI);
		}

		@Override
		protected boolean isNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			ParameterCustomization p = getParameterCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method), param.getName());
			if (p != null) {
				if (p.nullableFacetHidden) {
					return false;
				}
			}
			return param.isNullable();
		}

		@Override
		protected String getCaption(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			ParameterCustomization p = getParameterCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method), param.getName());
			if (p != null) {
				if (p.customParameterCaption != null) {
					return p.customParameterCaption;
				}
			}
			return super.getCaption(param, method, containingType);
		}

		@Override
		protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType.getName(), field.getName());
			if (f != null) {
				if (f.nullableFacetHidden) {
					return false;
				}
			}
			return field.isNullable();
		}

		@Override
		protected boolean isGetOnly(IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType.getName(), field.getName());
			if (f != null) {
				if (f.getOnlyForced) {
					return true;
				}
			}
			return super.isGetOnly(field, containingType);
		}

		@Override
		protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType.getName(), field.getName());
			if (f != null) {
				if (f.customFieldCaption != null) {
					return f.customFieldCaption;
				}
			}
			return super.getCaption(field, containingType);
		}

		@Override
		protected boolean isReadOnly(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType.getName(), method.getName());
			if (m != null) {
				if (m.readOnlyForced) {
					return true;
				}
			}
			return super.isReadOnly(method, containingType);
		}

		@Override
		protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType.getName(), method.getName());
			if (m != null) {
				List<IParameterInfo> result = super.getParameters(method, containingType);
				for (ParameterCustomization p : m.parametersCustomizations) {
					if (p.hidden) {
						IParameterInfo param = ReflectionUIUtils.findInfoByCaption(result, p.parameterName);
						if (param != null) {
							result = new ArrayList<IParameterInfo>(result);
							result.remove(param);
							return result;
						}
					}
				}
			}
			return super.getParameters(method, containingType);
		}

		@Override
		protected String getCaption(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType.getName(), method.getName());
			if (m != null) {
				if (m.customMethodCaption != null) {
					return m.customMethodCaption;
				}
			}
			return super.getCaption(method, containingType);
		}

		@Override
		protected List<IFieldInfo> getFields(ITypeInfo type) {
			final TypeCustomization t = getTypeCustomization(type.getName());
			if (t != null) {
				final List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
				for (FieldCustomization f : t.fieldsCustomizations) {
					if (f.hidden) {
						IFieldInfo field = ReflectionUIUtils.findInfoByName(result, f.fieldName);
						if (field != null) {
							result.remove(field);
						}
					}
				}
				if (t.customFieldsOrder != null) {
					Collections.sort(result,
							getInfosComparator(t.customFieldsOrder, new ArrayList<IFieldInfo>(result)));
				}
				return result;
			}
			return super.getFields(type);
		}

		@Override
		protected List<IMethodInfo> getMethods(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(type.getName());
			if (t != null) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
				for (MethodCustomization m : t.methodsCustomizations) {
					if (m.hidden) {
						IMethodInfo method = ReflectionUIUtils.findMethodBySignature(result, m.methodSignature);
						if (method != null) {
							result.remove(method);
						}
					}
				}
				if (t.customMethodsOrder != null) {
					Collections.sort(result,
							getInfosComparator(t.customMethodsOrder, new ArrayList<IMethodInfo>(result)));
				}
				return result;
			}
			return super.getMethods(type);
		}

		@Override
		protected String getCaption(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(type.getName());
			if (t != null) {
				if (t.customTypeCaption != null) {
					return t.customTypeCaption;
				}
			}
			return super.getCaption(type);
		}

	}

	protected static <T extends IInfo> Comparator<T> getInfosComparator(final List<String> namesOrder,
			final List<T> infoListCopy) {
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (namesOrder.contains(o1.getName()) && namesOrder.contains(o2.getName())) {
					Integer index1 = new Integer(namesOrder.indexOf(o1.getName()));
					Integer index2 = new Integer(namesOrder.indexOf(o2.getName()));
					return index1.compareTo(index2);
				}
				if (namesOrder.contains(o1.getName())) {
					return 1;
				}
				if (namesOrder.contains(o2.getName())) {
					return -1;
				}
				Integer index1 = new Integer(infoListCopy.indexOf(o1));
				Integer index2 = new Integer(infoListCopy.indexOf(o2));
				return index1.compareTo(index2);
			}
		};
	}

}
