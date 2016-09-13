package xy.reflect.ui.info.type.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.CustomizedStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.ListStructuralInfoProxy;
import xy.reflect.ui.info.type.iterable.structure.column.DefaultListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.ListStructureCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

@SuppressWarnings("unused")
public final class InfoCustomizations {

	public static final InfoCustomizations DEFAULT = new InfoCustomizations();
	static {
		{
			if (SystemProperties.areDefaultInfoCustomizationsActive()) {
				String filePath = SystemProperties.getDefaultInfoCustomizationsFilePath();
				File file = new File(filePath);
				if (file.exists()) {
					try {
						DEFAULT.loadFromFile(file);
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
				}
			}
		}
	};

	protected Set<TypeCustomization> typeCustomizations = new TreeSet<InfoCustomizations.TypeCustomization>();
	protected Set<ListStructureCustomization> listStructures = new TreeSet<InfoCustomizations.ListStructureCustomization>();

	public Set<TypeCustomization> getTypeCustomizations() {
		return typeCustomizations;
	}

	public void setTypeCustomizations(Set<TypeCustomization> typeCustomizations) {
		if (typeCustomizations == null) {
			this.typeCustomizations = null;
		}
		this.typeCustomizations = new TreeSet<TypeCustomization>(typeCustomizations);
	}

	public Set<ListStructureCustomization> getListStructures() {
		return listStructures;
	}

	public void setListStructures(Set<ListStructureCustomization> listStructures) {
		if (listStructures == null) {
			this.listStructures = null;
		}
		this.listStructures = new TreeSet<ListStructureCustomization>(listStructures);
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
		InfoCustomizations loaded;
		XStream xstream = getXStream();
		loaded = (InfoCustomizations) xstream.fromXML(input);
		typeCustomizations = loaded.typeCustomizations;
		listStructures = loaded.listStructures;
	}

	public void saveToFile(File output) throws IOException {
		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		saveToStream(memoryStream);
		FileOutputStream stream = new FileOutputStream(output);
		try {
			stream.write(memoryStream.toByteArray());
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void saveToStream(OutputStream output) throws IOException {
		InfoCustomizations toSave = new InfoCustomizations();
		toSave.typeCustomizations = typeCustomizations;
		toSave.listStructures = listStructures;
		XStream xstream = getXStream();
		xstream.toXML(toSave, output);
	}

	protected XStream getXStream() {
		XStream result = new XStream();
		result.registerConverter(new JavaBeanConverter(result.getMapper()), -20);
		return result;
	}

	public ParameterCustomization getParameterCustomization(String containingTypeName, String methodSignature,
			String paramName) {
		return getParameterCustomization(containingTypeName, methodSignature, paramName, true);
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
		return getFieldCustomization(containingTypeName, fieldName, true);
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
		return getMethodCustomization(containingTypeName, methodSignature, true);
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

	public TypeCustomization getTypeCustomization(String typeName) {
		return getTypeCustomization(typeName, true);
	}

	public TypeCustomization getTypeCustomization(String typeName, boolean create) {
		for (TypeCustomization t : typeCustomizations) {
			if (typeName.equals(t.typeName)) {
				return t;
			}
		}
		if (create) {
			TypeCustomization t = new TypeCustomization(typeName);
			typeCustomizations.add(t);
			return t;
		}
		return null;
	}

	public ListStructureCustomization getListStructureCustomization(String listTypeName, String itemTypeName) {
		return getListStructureCustomization(listTypeName, itemTypeName, true);
	}

	public ListStructureCustomization getListStructureCustomization(String listTypeName, String itemTypeName,
			boolean create) {
		for (ListStructureCustomization l : listStructures) {
			if (listTypeName.equals(l.listTypeName)) {
				if (ReflectionUIUtils.equalsOrBothNull(l.itemTypeName, itemTypeName)) {
					return l;
				}
			}
		}
		if (create) {
			ListStructureCustomization l = new ListStructureCustomization(listTypeName, itemTypeName);
			listStructures.add(l);
			return l;
		}
		return null;
	}

	public ColumnCustomization getColumnCustomization(String listTypeName, String itemTypeName, String columnName) {
		return getColumnCustomization(listTypeName, itemTypeName, columnName, true);
	}

	public ColumnCustomization getColumnCustomization(String listTypeName, String itemTypeName, String columnName,
			boolean create) {
		for (ListStructureCustomization l : listStructures) {
			if (listTypeName.equals(l.listTypeName)) {
				if (ReflectionUIUtils.equalsOrBothNull(itemTypeName, l.itemTypeName)) {
					for (ColumnCustomization c : l.columnsCustomizations) {
						if (columnName.equals(c.columnName)) {
							return c;
						}
					}
				}
			}
		}
		if (create) {
			ListStructureCustomization l = getListStructureCustomization(listTypeName, itemTypeName, true);
			ColumnCustomization c = new ColumnCustomization(columnName);
			l.columnsCustomizations.add(c);
			return c;
		}
		return null;
	}

	protected static <I extends IInfo> List<String> getInfosOrderAfterMove(List<I> list, I info, int offset) {
		int index = list.indexOf(info);
		List<I> resultList = new ArrayList<I>(list);
		resultList.remove(index);
		int offsetSign = ((offset > 0) ? 1 : -1);
		InfoCategory infoCategory = getCategory(info);
		for (int iOffset = 0; iOffset != offset; iOffset = iOffset + offsetSign) {
			while (true) {
				index = index + offsetSign;
				if (index == 0) {
					break;
				}
				if (index == resultList.size()) {
					break;
				}
				I otherInfo = resultList.get(index);
				InfoCategory otherFieldCategory = getCategory(otherInfo);
				if (ReflectionUIUtils.equalsOrBothNull(infoCategory, otherFieldCategory)) {
					break;
				}
			}
			if (index == 0) {
				break;
			}
			if (index == resultList.size()) {
				break;
			}
		}
		resultList.add(index, info);
		ArrayList<String> newOrder = new ArrayList<String>();
		for (I info2 : resultList) {
			String name = info2.getName();
			if ((name == null) || (name.trim().length() == 0)) {
				throw new ReflectionUIError(
						"Cannot move item order. 'getName()' method returned an empty value for item n°"
								+ (list.indexOf(info2) + 1) + " (caption='" + info2.getCaption() + "')");
			}
			newOrder.add(name);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((listStructures == null) ? 0 : listStructures.hashCode());
		result = prime * result + ((typeCustomizations == null) ? 0 : typeCustomizations.hashCode());
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
		InfoCustomizations other = (InfoCustomizations) obj;
		if (listStructures == null) {
			if (other.listStructures != null)
				return false;
		} else if (!listStructures.equals(other.listStructures))
			return false;
		if (typeCustomizations == null) {
			if (other.typeCustomizations != null)
				return false;
		} else if (!typeCustomizations.equals(other.typeCustomizations))
			return false;
		return true;
	}

	public abstract class AbstractInfoCustomization {
		protected Map<String, Object> specificProperties;

		public Map<String, Object> getSpecificProperties() {
			return specificProperties;
		}

		public void setSpecificProperties(Map<String, Object> specificProperties) {
			this.specificProperties = specificProperties;
		}

	}

	public class TypeCustomization extends AbstractInfoCustomization implements Comparable<TypeCustomization> {
		protected String typeName;
		protected String customTypeCaption;
		protected Set<FieldCustomization> fieldsCustomizations = new TreeSet<InfoCustomizations.FieldCustomization>();
		protected Set<MethodCustomization> methodsCustomizations = new TreeSet<InfoCustomizations.MethodCustomization>();
		protected List<String> customFieldsOrder;
		protected List<String> customMethodsOrder;
		protected String onlineHelp;
		protected List<CustomizationCategory> memberCategories = new ArrayList<CustomizationCategory>();
		protected List<ITypeInfoFinder> polymorphicSubTypeFinders = new ArrayList<ITypeInfoFinder>();

		public TypeCustomization(String TypeName) {
			super();
			this.typeName = TypeName;
		}

		public String getTypeName() {
			return typeName;
		}

		public List<ITypeInfoFinder> getPolymorphicSubTypeFinders() {
			return polymorphicSubTypeFinders;
		}

		public void setPolymorphicSubTypeFinders(List<ITypeInfoFinder> polymorphicSubTypeFinders) {
			this.polymorphicSubTypeFinders = polymorphicSubTypeFinders;
		}

		public List<CustomizationCategory> getMemberCategories() {
			return memberCategories;
		}

		public void setMemberCategories(List<CustomizationCategory> memberCategories) {
			this.memberCategories = memberCategories;
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

		public Set<FieldCustomization> getFieldsCustomizations() {
			return fieldsCustomizations;
		}

		public void setFieldsCustomizations(Set<FieldCustomization> fieldsCustomizations) {
			if (fieldsCustomizations == null) {
				this.fieldsCustomizations = null;
			}
			this.fieldsCustomizations = new TreeSet<FieldCustomization>(fieldsCustomizations);
		}

		public Set<MethodCustomization> getMethodsCustomizations() {
			return methodsCustomizations;
		}

		public void setMethodsCustomizations(Set<MethodCustomization> methodsCustomizations) {
			if (methodsCustomizations == null) {
				this.methodsCustomizations = null;
			}
			this.methodsCustomizations = new TreeSet<MethodCustomization>(methodsCustomizations);
		}

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
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
		public int compareTo(TypeCustomization o) {
			return typeName.compareTo(o.typeName);
		}

		@Override
		public String toString() {
			return typeName;
		}

		public void moveField(List<IFieldInfo> customizedFields, String fieldName, int offset) {
			IFieldInfo customizedField = ReflectionUIUtils.findInfoByName(customizedFields, fieldName);
			if (customizedField == null) {
				return;
			}
			customFieldsOrder = getInfosOrderAfterMove(customizedFields, customizedField, offset);
		}

		public void moveMethod(List<IMethodInfo> customizedMethods, String methodSignature, int offset) {
			IMethodInfo customizedMethod = ReflectionUIUtils.findMethodBySignature(customizedMethods, methodSignature);
			if (customizedMethod == null) {
				return;
			}
			customMethodsOrder = getInfosOrderAfterMove(customizedMethods, customizedMethod, offset);
		}

	}

	public static class CustomizationCategory {
		protected String caption;

		public String getCaption() {
			return caption;
		}

		public void setCaption(String caption) {
			this.caption = caption;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((caption == null) ? 0 : caption.hashCode());
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
			CustomizationCategory other = (CustomizationCategory) obj;
			if (caption == null) {
				if (other.caption != null)
					return false;
			} else if (!caption.equals(other.caption))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return caption;
		}

	}

	public abstract class AbstractMemberCustomization extends AbstractInfoCustomization {
		protected boolean hidden = false;
		protected CustomizationCategory category;
		protected String onlineHelp;

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public CustomizationCategory getCategory() {
			return category;
		}

		public void setCategory(CustomizationCategory category) {
			this.category = category;
		}

		public List<CustomizationCategory> getCategoryOptions() {
			for (TypeCustomization tc : typeCustomizations) {
				for (FieldCustomization fc : tc.fieldsCustomizations) {
					if (fc == this) {
						return tc.memberCategories;
					}
				}
				for (MethodCustomization mc : tc.methodsCustomizations) {
					if (mc == this) {
						return tc.memberCategories;
					}
				}
			}
			return null;
		}

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
		}
	}

	public class FieldCustomization extends AbstractMemberCustomization implements Comparable<FieldCustomization> {
		protected String fieldName;
		protected String customFieldCaption;
		protected boolean nullableFacetHidden = false;
		protected boolean getOnlyForced = false;
		protected String valueOptionsFieldName;

		public FieldCustomization(String FieldName) {
			super();
			this.fieldName = FieldName;
		}

		public String getFieldName() {
			return fieldName;
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

		public String getValueOptionsFieldName() {
			return valueOptionsFieldName;
		}

		public void setValueOptionsFieldName(String valueOptionsFieldName) {
			this.valueOptionsFieldName = valueOptionsFieldName;
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
		public int compareTo(FieldCustomization o) {
			return fieldName.compareTo(o.fieldName);
		}

		@Override
		public String toString() {
			return fieldName;
		}

	}

	public class MethodCustomization extends AbstractMemberCustomization implements Comparable<MethodCustomization> {
		protected String methodSignature;
		protected String customMethodCaption;
		protected boolean readOnlyForced = false;
		protected List<ParameterCustomization> parametersCustomizations = new ArrayList<InfoCustomizations.ParameterCustomization>();

		public MethodCustomization(String methodSignature) {
			super();
			this.methodSignature = methodSignature;
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
			result = prime * result + ((methodSignature == null) ? 0 : methodSignature.hashCode());
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
			if (methodSignature == null) {
				if (other.methodSignature != null)
					return false;
			} else if (!methodSignature.equals(other.methodSignature))
				return false;
			return true;
		}

		@Override
		public int compareTo(MethodCustomization o) {
			return methodSignature.compareTo(o.methodSignature);
		}

		@Override
		public String toString() {
			return methodSignature;
		}
	}

	public class ParameterCustomization extends AbstractInfoCustomization {
		protected String parameterName;
		protected String customParameterCaption;
		protected boolean hidden = false;
		protected boolean nullableFacetHidden = false;
		protected String onlineHelp;

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

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
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
			return parameterName;
		}

	}

	public class ListStructureCustomization implements Comparable<ListStructureCustomization> {
		protected String listTypeName;
		protected String itemTypeName;
		protected boolean itemTypeColumnAdded;
		protected boolean positionColumnAdded;
		protected boolean fieldColumnsAdded;
		protected Set<ColumnCustomization> columnsCustomizations = new TreeSet<ColumnCustomization>();
		protected List<String> columnsCustomOrder;
		protected TreeStructureDiscoverySettings treeStructureDiscoverySettings;

		public ListStructureCustomization(String listTypeName, String itemTypeName) {
			super();
			this.listTypeName = listTypeName;
			this.itemTypeName = itemTypeName;
		}

		public TypeCustomization getItemTypeCustomization() {
			if (itemTypeName == null) {
				return null;
			}
			return getTypeCustomization(itemTypeName);
		}

		public List<String> getColumnsCustomOrder() {
			return columnsCustomOrder;
		}

		public void setColumnsCustomOrder(List<String> columnsCustomOrder) {
			this.columnsCustomOrder = columnsCustomOrder;
		}

		public TreeStructureDiscoverySettings getTreeStructureDiscoverySettings() {
			return treeStructureDiscoverySettings;
		}

		public void setTreeStructureDiscoverySettings(TreeStructureDiscoverySettings treeStructureDiscoverySettings) {
			this.treeStructureDiscoverySettings = treeStructureDiscoverySettings;
		}

		public String getListTypeName() {
			return listTypeName;
		}

		public String getItemTypeName() {
			return itemTypeName;
		}

		public Set<ColumnCustomization> getColumnsCustomizations() {
			return columnsCustomizations;
		}

		public void setColumnsCustomizations(Set<ColumnCustomization> columnsCustomizations) {
			if (columnsCustomizations == null) {
				this.columnsCustomizations = null;
			}
			this.columnsCustomizations = new TreeSet<ColumnCustomization>(columnsCustomizations);
		}

		public boolean isItemTypeColumnAdded() {
			return itemTypeColumnAdded;
		}

		public void setItemTypeColumnAdded(boolean itemTypeColumnAdded) {
			this.itemTypeColumnAdded = itemTypeColumnAdded;
		}

		public boolean isPositionColumnAdded() {
			return positionColumnAdded;
		}

		public void setPositionColumnAdded(boolean positionColumnAdded) {
			this.positionColumnAdded = positionColumnAdded;
		}

		public boolean isFieldColumnsAdded() {
			return fieldColumnsAdded;
		}

		public void setFieldColumnsAdded(boolean fieldColumnsAdded) {
			this.fieldColumnsAdded = fieldColumnsAdded;
		}

		public ColumnCustomization getColumnCustomization(String columnName) {
			return InfoCustomizations.this.getColumnCustomization(listTypeName, itemTypeName, columnName);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itemTypeName == null) ? 0 : itemTypeName.hashCode());
			result = prime * result + ((listTypeName == null) ? 0 : listTypeName.hashCode());
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
			ListStructureCustomization other = (ListStructureCustomization) obj;
			if (itemTypeName == null) {
				if (other.itemTypeName != null)
					return false;
			} else if (!itemTypeName.equals(other.itemTypeName))
				return false;
			if (listTypeName == null) {
				if (other.listTypeName != null)
					return false;
			} else if (!listTypeName.equals(other.listTypeName))
				return false;
			return true;
		}

		@Override
		public int compareTo(ListStructureCustomization o) {
			return listTypeName.compareTo(o.listTypeName);
		}

		@Override
		public String toString() {
			return listTypeName + "(" + itemTypeName + ")";
		}

	}

	public static class TreeStructureDiscoverySettings {
		protected String treeColumnFieldName;

		public String getTreeColumnFieldName() {
			return treeColumnFieldName;
		}

		public void setTreeColumnFieldName(String treeColumnFieldName) {
			this.treeColumnFieldName = treeColumnFieldName;
		}

	}

	public class ColumnCustomization implements Comparable<ColumnCustomization> {
		protected String columnName;
		protected String customCaption;
		protected boolean hidden = false;

		public ColumnCustomization(String columnName) {
			super();
			this.columnName = columnName;
		}

		public String getColumnName() {
			return columnName;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public String getCustomCaption() {
			return customCaption;
		}

		public void setCustomCaption(String customCaption) {
			this.customCaption = customCaption;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
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
			ColumnCustomization other = (ColumnCustomization) obj;
			if (columnName == null) {
				if (other.columnName != null)
					return false;
			} else if (!columnName.equals(other.columnName))
				return false;
			return true;
		}

		@Override
		public int compareTo(ColumnCustomization o) {
			return columnName.compareTo(o.columnName);
		}

		@Override
		public String toString() {
			return columnName;
		}

	}

	public static interface ITypeInfoFinder {
		ITypeInfo find(ReflectionUI reflectionUI);
	}

	public static class JavaClassBasedTypeInfoFinder implements ITypeInfoFinder {

		protected String className;

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		@Override
		public ITypeInfo find(ReflectionUI reflectionUI) {
			Class<?> javaType;
			try {
				javaType = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new ReflectionUIError(e);
			}
			return reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaType));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((className == null) ? 0 : className.hashCode());
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
			JavaClassBasedTypeInfoFinder other = (JavaClassBasedTypeInfoFinder) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ITypeInfo based on the java class: '" + className + "'";
		}

	}

	public static class CustomTypeInfoFinder implements ITypeInfoFinder {

		protected String implementationClassName;

		public String getImplementationClassName() {
			return implementationClassName;
		}

		public void setImplementationClassName(String implementationClassName) {
			this.implementationClassName = implementationClassName;
		}

		@Override
		public ITypeInfo find(ReflectionUI reflectionUI) {
			try {
				Class<?> implementationClass = Class.forName(implementationClassName);
				return (ITypeInfo) implementationClass.newInstance();
			} catch (Exception e) {
				throw new ReflectionUIError("Failed to instanciate class implenation class: " + e.toString(), e);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((implementationClassName == null) ? 0 : implementationClassName.hashCode());
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
			CustomTypeInfoFinder other = (CustomTypeInfoFinder) obj;
			if (implementationClassName == null) {
				if (other.implementationClassName != null)
					return false;
			} else if (!implementationClassName.equals(other.implementationClassName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ITypeInfo custom implementation: '" + implementationClassName + "'";
		}

	}

	public static void copyValuesToNewVersion(final Object newInstance, Object oldInstance, List<Runnable> postUpdate)
			throws Exception {
		ReflectionUI r = new ReflectionUI();
		ITypeInfo newType = r.getTypeInfo(r.getTypeInfoSource(newInstance));
		ITypeInfo oldType = r.getTypeInfo(r.getTypeInfoSource(oldInstance));
		for (IFieldInfo oldField : oldType.getFields()) {
			IFieldInfo newField = ReflectionUIUtils.findInfoByName(newType.getFields(), oldField.getName());
			if (newField.isGetOnly()) {
				continue;
			}
			final Object oldFieldValue = oldField.getValue(oldInstance);
			if (oldFieldValue == null) {
				continue;
			}
			if (ClassUtils.isPrimitiveTypeOrWrapperOrString(oldFieldValue.getClass()) || ClassUtils
					.isPrimitiveTypeOrWrapperOrString(ClassUtils.forNameEvenPrimitive(oldField.getType().getName()))) {
				if (oldFieldValue instanceof String) {
					newField.setValue(newInstance, ((String) oldFieldValue).replace(InfoCustomizations.class.getName(),
							InfoCustomizationsNew.class.getName()));
				} else {
					newField.setValue(newInstance, oldFieldValue);
				}
			} else if (oldField.getType().getName().equals(CustomizationCategory.class.getName())) {
				postUpdate.add(new Runnable() {
					@Override
					public void run() {
						InfoCustomizationsNew.AbstractMemberCustomization newM = (xy.reflect.ui.info.type.util.InfoCustomizationsNew.AbstractMemberCustomization) newInstance;
						CustomizationCategory oldC = (CustomizationCategory) oldFieldValue;
						for (InfoCustomizationsNew.CustomizationCategory newC : newM.parent.memberCategories) {
							if (oldC.caption.equals(newC.caption)) {
								newM.setCategory(newC);
								break;
							}
						}
					}
				});
			} else {
				Object newFieldValue = createNewVersionInstance(r, newInstance, oldFieldValue);
				if (oldField.getType() instanceof IListTypeInfo) {
					IListTypeInfo oldListFieldType = (IListTypeInfo) oldField.getType();
					IListTypeInfo newListFieldType = (IListTypeInfo) newField.getType();
					Object[] oldFieldArray = oldListFieldType.toArray(oldFieldValue);
					List<Object> newItems = new ArrayList<Object>();
					for (Object oldItem : oldFieldArray) {
						Object newItem = createNewVersionInstance(r, newInstance, oldItem);
						InfoCustomizations.copyValuesToNewVersion(newItem, oldItem, postUpdate);
						newItems.add(newItem);
					}
					newListFieldType.replaceContent(newFieldValue, newItems.toArray());
					newField.setValue(newInstance, newFieldValue);
				} else {
					InfoCustomizations.copyValuesToNewVersion(newFieldValue, oldFieldValue, postUpdate);
				}
			}
		}
	}

	private static Object createNewVersionInstance(ReflectionUI r, Object parent, Object oldInstance) {
		ITypeInfo oldType = r.getTypeInfo(r.getTypeInfoSource(oldInstance));
		String newTypeName = oldInstance.getClass().getName().replace(InfoCustomizations.class.getName(),
				InfoCustomizationsNew.class.getName());
		ITypeInfo newType;
		try {
			newType = r.getTypeInfo(new JavaTypeInfoSource(Class.forName(newTypeName)));
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
		IMethodInfo ctor = ReflectionUIUtils.getZeroParameterConstrucor(newType);
		if (ctor == null) {
			ctor = newType.getConstructors().get(0);
		}
		InvocationData invocationData = new InvocationData();
		for (IParameterInfo p : ctor.getParameters()) {
			if (p.getName().equals("parent")) {
				invocationData.setparameterValue(p, parent);
			} else {
				String fieldName = p.getName();
				if (fieldName.contains("(")) {
					fieldName = fieldName.substring(0, fieldName.indexOf(" ("));
				}
				IFieldInfo oldField = ReflectionUIUtils.findInfoByName(oldType.getFields(), fieldName);
				Object oldFieldValue = oldField.getValue(oldInstance);
				invocationData.setparameterValue(p, oldFieldValue);
			}
		}
		Object result = ctor.invoke(null, invocationData);
		IFieldInfo parentField = ReflectionUIUtils.findInfoByName(newType.getFields(), "parent");
		if (parentField != null) {
			parentField.setValue(result, parent);
		}
		return result;
	}

}
