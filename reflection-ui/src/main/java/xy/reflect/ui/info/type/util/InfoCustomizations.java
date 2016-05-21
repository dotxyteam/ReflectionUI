package xy.reflect.ui.info.type.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class InfoCustomizations {

	protected CustomizationsProxyGenerator proxyGenerator;
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

	public ParameterCustomization getParameterCustomization(ITypeInfo containingType,
			IMethodInfo method, IParameterInfo param) {
		return getParameterCustomization(containingType, method, param, false);
	}

	public ParameterCustomization getParameterCustomization(ITypeInfo containingType,
			IMethodInfo method, IParameterInfo param, boolean create) {
		MethodCustomization m = getMethodCustomization(containingType, method, create);
		if (m != null) {
			for (ParameterCustomization p : m.parametersCustomizations) {
				if (param.getCaption().equals(p.ParameterCaption)) {
					return p;
				}
			}
			if (create) {
				ParameterCustomization p = new ParameterCustomization(param.getCaption());
				m.parametersCustomizations.add(p);
				return p;
			}
		}
		return null;
	}

	public FieldCustomization getFieldCustomization(ITypeInfo containingType, IFieldInfo field) {
		return getFieldCustomization(containingType, field, false);
	}

	public FieldCustomization getFieldCustomization(ITypeInfo containingType, IFieldInfo field,
			boolean create) {
		TypeCustomization t = getTypeCustomization(containingType, create);
		if (t != null) {
			for (FieldCustomization f : t.fieldsCustomizations) {
				if (field.getCaption().equals(f.FieldCaption)) {
					return f;
				}
			}
			if (create) {
				FieldCustomization f = new FieldCustomization(field.getCaption());
				t.fieldsCustomizations.add(f);
				return f;
			}
		}
		return null;
	}

	public MethodCustomization getMethodCustomization(ITypeInfo containingType, IMethodInfo method) {
		return getMethodCustomization(containingType, method, false);
	}

	public MethodCustomization getMethodCustomization(ITypeInfo containingType, IMethodInfo method,
			boolean create) {
		TypeCustomization t = getTypeCustomization(containingType, create);
		if (t != null) {
			for (MethodCustomization m : t.methodsCustomizations) {
				if (method.getCaption().equals(m.MethodCaption)) {
					return m;
				}
			}
			if (create) {
				MethodCustomization m = new MethodCustomization(method.getCaption());
				t.methodsCustomizations.add(m);
				return m;
			}
		}
		return null;
	}

	public TypeCustomization getTypeCustomization(ITypeInfo containingType) {
		return getTypeCustomization(containingType, false);
	}

	public TypeCustomization getTypeCustomization(ITypeInfo containingType, boolean create) {
		for (TypeCustomization t : typeCustomizations) {
			if (containingType.getCaption().equals(t.TypeCaption)) {
				return t;
			}
		}
		if (create) {
			TypeCustomization t = new TypeCustomization(containingType.getCaption());
			typeCustomizations.add(t);
			return t;
		}
		return null;
	}

	public static class TypeCustomization {
		private String TypeCaption;
		private String customTypeCaption;
		private List<FieldCustomization> fieldsCustomizations = new ArrayList<InfoCustomizations.FieldCustomization>();
		private List<MethodCustomization> methodsCustomizations = new ArrayList<InfoCustomizations.MethodCustomization>();

		public TypeCustomization(String TypeCaption) {
			super();
			this.TypeCaption = TypeCaption;
		}

		public String getTypeCaption() {
			return TypeCaption;
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
			result = prime * result + ((TypeCaption == null) ? 0 : TypeCaption.hashCode());
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
			if (TypeCaption == null) {
				if (other.TypeCaption != null)
					return false;
			} else if (!TypeCaption.equals(other.TypeCaption))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TypeCustomization [TypeCaption=" + TypeCaption + "]";
		}

	}

	public static class FieldCustomization {
		private String FieldCaption;
		private String customFieldCaption;
		private boolean hidden = false;
		private boolean nullableFacetHidden = false;
		private boolean getOnlyForced = false;

		public FieldCustomization(String FieldCaption) {
			super();
			this.FieldCaption = FieldCaption;
		}

		public String getFieldCaption() {
			return FieldCaption;
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
			result = prime * result + ((FieldCaption == null) ? 0 : FieldCaption.hashCode());
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
			if (FieldCaption == null) {
				if (other.FieldCaption != null)
					return false;
			} else if (!FieldCaption.equals(other.FieldCaption))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FieldCustomization [FieldCaption=" + FieldCaption + "]";
		}
	}

	public static class MethodCustomization {
		private String MethodCaption;
		private String customMethodCaption;
		private boolean hidden = false;
		private boolean readOnlyForced = false;
		private List<ParameterCustomization> parametersCustomizations = new ArrayList<InfoCustomizations.ParameterCustomization>();

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

		public String getMethodCaption() {
			return MethodCaption;
		}

		public MethodCustomization(String MethodCaption) {
			super();
			this.MethodCaption = MethodCaption;
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
			return "MethodCustomization [MethodCaption=" + MethodCaption + "]";
		}

	}

	public static class ParameterCustomization {
		private String ParameterCaption;
		private String customParameterCaption;
		private boolean hidden = false;
		private boolean nullableFacetHidden = false;

		public ParameterCustomization(String ParameterCaption) {
			super();
			this.ParameterCaption = ParameterCaption;
		}

		public String getParameterCaption() {
			return ParameterCaption;
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
			result = prime * result + ((ParameterCaption == null) ? 0 : ParameterCaption.hashCode());
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
			if (ParameterCaption == null) {
				if (other.ParameterCaption != null)
					return false;
			} else if (!ParameterCaption.equals(other.ParameterCaption))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ParameterCustomization [ParameterCaption=" + ParameterCaption + "]";
		}

	}

	protected class CustomizationsProxyGenerator extends HiddenNullableFacetsInfoProxyGenerator {

		public CustomizationsProxyGenerator(ReflectionUI reflectionUI) {
			super(reflectionUI);
		}

		@Override
		protected boolean isNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			ParameterCustomization p = getParameterCustomization(containingType, method, param);
			if (p != null) {
				if (p.nullableFacetHidden) {
					return false;
				}
			}
			return param.isNullable();
		}

		@Override
		protected String getCaption(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			ParameterCustomization p = getParameterCustomization(containingType, method, param);
			if (p != null) {
				if (p.customParameterCaption != null) {
					return p.customParameterCaption;
				}
			}
			return super.getCaption(param, method, containingType);
		}

		@Override
		protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType, field);
			if (f != null) {
				if (f.nullableFacetHidden) {
					return false;
				}
			}
			return field.isNullable();
		}

		@Override
		protected boolean isGetOnly(IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType, field);
			if (f != null) {
				if (f.getOnlyForced) {
					return true;
				}
			}
			return super.isGetOnly(field, containingType);
		}

		@Override
		protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType, field);
			if (f != null) {
				if (f.customFieldCaption != null) {
					return f.customFieldCaption;
				}
			}
			return super.getCaption(field, containingType);
		}

		@Override
		protected boolean isReadOnly(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType, method);
			if (m != null) {
				if (m.readOnlyForced) {
					return true;
				}
			}
			return super.isReadOnly(method, containingType);
		}

		@Override
		protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType, method);
			if (m != null) {
				List<IParameterInfo> result = super.getParameters(method, containingType);
				for (ParameterCustomization p : m.parametersCustomizations) {
					if (p.hidden) {
						IParameterInfo param = ReflectionUIUtils.findInfoByCaption(result, p.ParameterCaption);
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
			MethodCustomization m = getMethodCustomization(containingType, method);
			if (m != null) {
				if (m.customMethodCaption != null) {
					return m.customMethodCaption;
				}
			}
			return super.getCaption(method, containingType);
		}

		@Override
		protected List<IFieldInfo> getFields(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(type);
			if (t != null) {
				List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
				for (FieldCustomization f : t.fieldsCustomizations) {
					if (f.hidden) {
						IFieldInfo field = ReflectionUIUtils.findInfoByCaption(result, f.FieldCaption);
						if (field != null) {
							result.remove(field);
						}
					}
				}
				return result;
			}
			return super.getFields(type);
		}

		@Override
		protected List<IMethodInfo> getMethods(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(type);
			if (t != null) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
				for (MethodCustomization m : t.methodsCustomizations) {
					if (m.hidden) {
						IMethodInfo method = ReflectionUIUtils.findInfoByCaption(result, m.MethodCaption);
						if (method != null) {
							result.remove(method);
						}
					}
				}
				return result;
			}
			return super.getMethods(type);
		}

		@Override
		protected String getCaption(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(type);
			if (t != null) {
				if (t.customTypeCaption != null) {
					return t.customTypeCaption;
				}
			}
			return super.getCaption(type);
		}

	}

}
