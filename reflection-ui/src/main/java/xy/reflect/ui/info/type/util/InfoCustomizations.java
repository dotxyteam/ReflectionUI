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
public class InfoCustomizations extends HiddenNullableFacetsInfoProxyGenerator {

	public InfoCustomizations(ReflectionUI reflectionUI) {
		super(reflectionUI);
	}

	protected List<SpecificTypeCustomization> typeCustomizations = new ArrayList<InfoCustomizations.SpecificTypeCustomization>();
	
	public List<SpecificTypeCustomization> getTypeCustomizations() {
		return typeCustomizations;
	}

	public void setTypeCustomizations(List<SpecificTypeCustomization> typeCustomizations) {
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

	public SpecificParameterCustomization getSpecificParameterCustomization(ITypeInfo containingType,
			IMethodInfo method, IParameterInfo param) {
		return getSpecificParameterCustomization(containingType, method, param, false);
	}

	public SpecificParameterCustomization getSpecificParameterCustomization(ITypeInfo containingType,
			IMethodInfo method, IParameterInfo param, boolean create) {
		SpecificMethodCustomization m = getSpecificMethodCustomization(containingType, method, create);
		if (m != null) {
			for (SpecificParameterCustomization p : m.parametersCustomizations) {
				if (param.getCaption().equals(p.specificParameterCaption)) {
					return p;
				}
			}
			if (create) {
				SpecificParameterCustomization p = new SpecificParameterCustomization(param.getCaption());
				m.parametersCustomizations.add(p);
				return p;
			}
		}
		return null;
	}

	public SpecificFieldCustomization getSpecificFieldCustomization(ITypeInfo containingType, IFieldInfo field) {
		return getSpecificFieldCustomization(containingType, field, false);
	}

	public SpecificFieldCustomization getSpecificFieldCustomization(ITypeInfo containingType, IFieldInfo field,
			boolean create) {
		SpecificTypeCustomization t = getSpecificTypeCustomization(containingType, create);
		if (t != null) {
			for (SpecificFieldCustomization f : t.fieldsCustomizations) {
				if (field.getCaption().equals(f.specificFieldCaption)) {
					return f;
				}
			}
			if (create) {
				SpecificFieldCustomization f = new SpecificFieldCustomization(field.getCaption());
				t.fieldsCustomizations.add(f);
				return f;
			}
		}
		return null;
	}

	public SpecificMethodCustomization getSpecificMethodCustomization(ITypeInfo containingType, IMethodInfo method) {
		return getSpecificMethodCustomization(containingType, method, false);
	}

	public SpecificMethodCustomization getSpecificMethodCustomization(ITypeInfo containingType, IMethodInfo method,
			boolean create) {
		SpecificTypeCustomization t = getSpecificTypeCustomization(containingType, create);
		if (t != null) {
			for (SpecificMethodCustomization m : t.methodsCustomizations) {
				if (method.getCaption().equals(m.specificMethodCaption)) {
					return m;
				}
			}
			if (create) {
				SpecificMethodCustomization m = new SpecificMethodCustomization(method.getCaption());
				t.methodsCustomizations.add(m);
				return m;
			}
		}
		return null;
	}

	public SpecificTypeCustomization getSpecificTypeCustomization(ITypeInfo containingType) {
		return getSpecificTypeCustomization(containingType, false);
	}

	public SpecificTypeCustomization getSpecificTypeCustomization(ITypeInfo containingType, boolean create) {
		for (SpecificTypeCustomization t : typeCustomizations) {
			if (containingType.getCaption().equals(t.specificTypeCaption)) {
				return t;
			}
		}
		if (create) {
			SpecificTypeCustomization t = new SpecificTypeCustomization(containingType.getCaption());
			typeCustomizations.add(t);
			return t;
		}
		return null;
	}

	@Override
	protected boolean isNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		SpecificParameterCustomization p = getSpecificParameterCustomization(containingType, method, param);
		if (p != null) {
			if (p.nullableFacetHidden) {
				return false;
			}
		}
		return param.isNullable();
	}

	@Override
	protected String getCaption(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		SpecificParameterCustomization p = getSpecificParameterCustomization(containingType, method, param);
		if (p != null) {
			if (p.customParameterCaption != null) {
				return p.customParameterCaption;
			}
		}
		return super.getCaption(param, method, containingType);
	}

	@Override
	protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
		SpecificFieldCustomization f = getSpecificFieldCustomization(containingType, field);
		if (f != null) {
			if (f.nullableFacetHidden) {
				return false;
			}
		}
		return field.isNullable();
	}

	@Override
	protected boolean isGetOnly(IFieldInfo field, ITypeInfo containingType) {
		SpecificFieldCustomization f = getSpecificFieldCustomization(containingType, field);
		if (f != null) {
			if (f.getOnlyForced) {
				return true;
			}
		}
		return super.isGetOnly(field, containingType);
	}

	@Override
	protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
		SpecificFieldCustomization f = getSpecificFieldCustomization(containingType, field);
		if (f != null) {
			if (f.customFieldCaption != null) {
				return f.customFieldCaption;
			}
		}
		return super.getCaption(field, containingType);
	}

	@Override
	protected boolean isReadOnly(IMethodInfo method, ITypeInfo containingType) {
		SpecificMethodCustomization m = getSpecificMethodCustomization(containingType, method);
		if (m != null) {
			if (m.readOnlyForced) {
				return true;
			}
		}
		return super.isReadOnly(method, containingType);
	}

	@Override
	protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo containingType) {
		SpecificMethodCustomization m = getSpecificMethodCustomization(containingType, method);
		if (m != null) {
			List<IParameterInfo> result = super.getParameters(method, containingType);
			for (SpecificParameterCustomization p : m.parametersCustomizations) {
				if (p.hidden) {
					IParameterInfo param = ReflectionUIUtils.findInfoByCaption(result, p.specificParameterCaption);
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
		SpecificMethodCustomization m = getSpecificMethodCustomization(containingType, method);
		if (m != null) {
			if (m.customMethodCaption != null) {
				return m.customMethodCaption;
			}
		}
		return super.getCaption(method, containingType);
	}

	@Override
	protected List<IFieldInfo> getFields(ITypeInfo type) {
		SpecificTypeCustomization t = getSpecificTypeCustomization(type);
		if (t != null) {
			List<IFieldInfo> result = super.getFields(type);
			for (SpecificFieldCustomization f : t.fieldsCustomizations) {
				if (f.hidden) {
					IFieldInfo field = ReflectionUIUtils.findInfoByCaption(result, f.specificFieldCaption);
					if (field != null) {
						result = new ArrayList<IFieldInfo>(result);
						result.remove(field);
						return result;
					}
				}
			}
		}
		return super.getFields(type);
	}

	@Override
	protected List<IMethodInfo> getMethods(ITypeInfo type) {
		SpecificTypeCustomization t = getSpecificTypeCustomization(type);
		if (t != null) {
			List<IMethodInfo> result = super.getMethods(type);
			for (SpecificMethodCustomization m : t.methodsCustomizations) {
				if (m.hidden) {
					IMethodInfo method = ReflectionUIUtils.findInfoByCaption(result, m.specificMethodCaption);
					if (method != null) {
						result = new ArrayList<IMethodInfo>(result);
						result.remove(method);
						return result;
					}
				}
			}
		}
		return super.getMethods(type);
	}

	@Override
	protected String getCaption(ITypeInfo type) {
		SpecificTypeCustomization t = getSpecificTypeCustomization(type);
		if (t != null) {
			if (t.customTypeCaption != null) {
				return t.customTypeCaption;
			}
		}
		return super.getCaption(type);
	}

	public static class SpecificTypeCustomization {
		private String specificTypeCaption;
		private String customTypeCaption;
		private List<SpecificFieldCustomization> fieldsCustomizations = new ArrayList<InfoCustomizations.SpecificFieldCustomization>();
		private List<SpecificMethodCustomization> methodsCustomizations = new ArrayList<InfoCustomizations.SpecificMethodCustomization>();

		public SpecificTypeCustomization(String specificTypeCaption) {
			super();
			this.specificTypeCaption = specificTypeCaption;
		}

		public String getSpecificTypeCaption() {
			return specificTypeCaption;
		}

		public void setSpecificTypeCaption(String specificTypeCaption) {
			this.specificTypeCaption = specificTypeCaption;
		}

		public String getCustomTypeCaption() {
			return customTypeCaption;
		}

		public void setCustomTypeCaption(String customTypeCaption) {
			this.customTypeCaption = customTypeCaption;
		}

		public List<SpecificFieldCustomization> getFieldsCustomizations() {
			return fieldsCustomizations;
		}

		public void setFieldsCustomizations(List<SpecificFieldCustomization> fieldsCustomizations) {
			this.fieldsCustomizations = fieldsCustomizations;
		}

		public List<SpecificMethodCustomization> getMethodsCustomizations() {
			return methodsCustomizations;
		}

		public void setMethodsCustomizations(List<SpecificMethodCustomization> methodsCustomizations) {
			this.methodsCustomizations = methodsCustomizations;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((specificTypeCaption == null) ? 0 : specificTypeCaption.hashCode());
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
			SpecificTypeCustomization other = (SpecificTypeCustomization) obj;
			if (specificTypeCaption == null) {
				if (other.specificTypeCaption != null)
					return false;
			} else if (!specificTypeCaption.equals(other.specificTypeCaption))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SpecificTypeCustomization [specificTypeCaption=" + specificTypeCaption + "]";
		}

	}

	public static class SpecificFieldCustomization {
		private String specificFieldCaption;
		private String customFieldCaption;
		private boolean hidden = false;
		private boolean nullableFacetHidden = false;
		private boolean getOnlyForced = false;

		public SpecificFieldCustomization(String specificFieldCaption) {
			super();
			this.specificFieldCaption = specificFieldCaption;
		}

		public String getSpecificFieldCaption() {
			return specificFieldCaption;
		}

		public void setSpecificFieldCaption(String specificFieldCaption) {
			this.specificFieldCaption = specificFieldCaption;
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
			result = prime * result + ((specificFieldCaption == null) ? 0 : specificFieldCaption.hashCode());
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
			SpecificFieldCustomization other = (SpecificFieldCustomization) obj;
			if (specificFieldCaption == null) {
				if (other.specificFieldCaption != null)
					return false;
			} else if (!specificFieldCaption.equals(other.specificFieldCaption))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SpecificFieldCustomization [specificFieldCaption=" + specificFieldCaption + "]";
		}
	}

	public static class SpecificMethodCustomization {
		private String specificMethodCaption;
		private String customMethodCaption;
		private boolean hidden = false;
		private boolean readOnlyForced = false;
		private List<SpecificParameterCustomization> parametersCustomizations = new ArrayList<InfoCustomizations.SpecificParameterCustomization>();

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

		public String getSpecificMethodCaption() {
			return specificMethodCaption;
		}

		public SpecificMethodCustomization(String specificMethodCaption) {
			super();
			this.specificMethodCaption = specificMethodCaption;
		}

		public void setSpecificMethodCaption(String specificMethodCaption) {
			this.specificMethodCaption = specificMethodCaption;
		}

		public String getCustomMethodCaption() {
			return customMethodCaption;
		}

		public void setCustomMethodCaption(String customMethodCaption) {
			this.customMethodCaption = customMethodCaption;
		}

		public List<SpecificParameterCustomization> getParametersCustomizations() {
			return parametersCustomizations;
		}

		public void setParametersCustomizations(List<SpecificParameterCustomization> parametersCustomizations) {
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
			SpecificMethodCustomization other = (SpecificMethodCustomization) obj;
			if (customMethodCaption == null) {
				if (other.customMethodCaption != null)
					return false;
			} else if (!customMethodCaption.equals(other.customMethodCaption))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SpecificMethodCustomization [specificMethodCaption=" + specificMethodCaption + "]";
		}

	}

	public static class SpecificParameterCustomization {
		private String specificParameterCaption;
		private String customParameterCaption;
		private boolean hidden = false;
		private boolean nullableFacetHidden = false;

		public SpecificParameterCustomization(String specificParameterCaption) {
			super();
			this.specificParameterCaption = specificParameterCaption;
		}

		public String getSpecificParameterCaption() {
			return specificParameterCaption;
		}

		public void setSpecificParameterCaption(String specificParameterCaption) {
			this.specificParameterCaption = specificParameterCaption;
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
			result = prime * result + ((specificParameterCaption == null) ? 0 : specificParameterCaption.hashCode());
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
			SpecificParameterCustomization other = (SpecificParameterCustomization) obj;
			if (specificParameterCaption == null) {
				if (other.specificParameterCaption != null)
					return false;
			} else if (!specificParameterCaption.equals(other.specificParameterCaption))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SpecificParameterCustomization [specificParameterCaption=" + specificParameterCaption + "]";
		}

	}

}
