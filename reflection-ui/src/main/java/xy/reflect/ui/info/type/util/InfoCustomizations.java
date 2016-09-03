package xy.reflect.ui.info.type.util;

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

	transient protected CustomizationsProxyGenerator proxyGenerator;
	protected List<TypeCustomization> typeCustomizations = new ArrayList<InfoCustomizations.TypeCustomization>();
	protected List<ListStructureCustomization> listStructures = new ArrayList<InfoCustomizations.ListStructureCustomization>();

	protected CustomizationsProxyGenerator createCustomizationsProxyGenerator(ReflectionUI reflectionUI) {
		return new CustomizationsProxyGenerator(reflectionUI);
	}

	public ITypeInfo get(ReflectionUI reflectionUI, ITypeInfo type) {
		if (proxyGenerator == null) {
			proxyGenerator = createCustomizationsProxyGenerator(reflectionUI);
		}
		return proxyGenerator.get(type);
	}

	public List<TypeCustomization> getTypeCustomizations() {
		return typeCustomizations;
	}

	public void setTypeCustomizations(List<TypeCustomization> typeCustomizations) {
		this.typeCustomizations = typeCustomizations;
	}

	public List<ListStructureCustomization> getListStructures() {
		return listStructures;
	}

	public void setListStructures(List<ListStructureCustomization> listStructures) {
		this.listStructures = listStructures;
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
		listStructures = loaded.listStructures;
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
		InfoCustomizations toSave = new InfoCustomizations();
		toSave.typeCustomizations = typeCustomizations;
		toSave.listStructures = listStructures;
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

	public class TypeCustomization extends AbstractInfoCustomization {
		protected String typeName;
		protected String customTypeCaption;
		protected List<FieldCustomization> fieldsCustomizations = new ArrayList<InfoCustomizations.FieldCustomization>();
		protected List<MethodCustomization> methodsCustomizations = new ArrayList<InfoCustomizations.MethodCustomization>();
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

	public class FieldCustomization extends AbstractMemberCustomization {
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
		public String toString() {
			return fieldName;
		}

	}

	public class MethodCustomization extends AbstractMemberCustomization {
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

	public class ListStructureCustomization {
		protected String listTypeName;
		protected String itemTypeName;
		protected boolean itemTypeColumnAdded;
		protected boolean positionColumnAdded;
		protected boolean fieldColumnsAdded;
		protected List<ColumnCustomization> columnsCustomizations = new ArrayList<ColumnCustomization>();
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

		public List<ColumnCustomization> getColumnsCustomizations() {
			return columnsCustomizations;
		}

		public void setColumnsCustomizations(List<ColumnCustomization> columnsCustomizations) {
			this.columnsCustomizations = columnsCustomizations;
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

	public class ColumnCustomization {
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
		public String toString() {
			return columnName;
		}

	}

	protected class CustomizationsProxyGenerator extends HiddenNullableFacetsInfoProxyGenerator {

		public CustomizationsProxyGenerator(ReflectionUI reflectionUI) {
			super(reflectionUI);
		}

		@Override
		protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
			TypeCustomization tc = getTypeCustomization(type.getName());
			if (tc != null) {
				if (tc.polymorphicSubTypeFinders != null) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					List<ITypeInfo> baseResult = super.getPolymorphicInstanceSubTypes(type);
					if (baseResult != null) {
						result.addAll(baseResult);
					}
					for (ITypeInfoFinder finder : tc.polymorphicSubTypeFinders) {
						ITypeInfo subType = finder.find(reflectionUI);
						result.add(subType);
					}
					return result;
				}
			}
			return super.getPolymorphicInstanceSubTypes(type);
		}

		@Override
		protected Map<String, Object> getSpecificProperties(IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType.getName(), field.getName());
			if (f != null) {
				if (f.specificProperties != null) {
					if (f.specificProperties.entrySet().size() > 0) {
						Map<String, Object> result = new HashMap<String, Object>(
								super.getSpecificProperties(field, containingType));
						result.putAll(f.specificProperties);
						return result;
					}
				}
			}
			return super.getSpecificProperties(field, containingType);
		}

		@Override
		protected Map<String, Object> getSpecificProperties(IParameterInfo param, IMethodInfo method,
				ITypeInfo containingType) {
			ParameterCustomization p = getParameterCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method), param.getName());
			if (p != null) {
				if (p.specificProperties != null) {
					if (p.specificProperties.entrySet().size() > 0) {
						Map<String, Object> result = new HashMap<String, Object>(
								super.getSpecificProperties(param, method, containingType));
						result.putAll(p.specificProperties);
						return result;
					}
				}
			}
			return super.getSpecificProperties(param, method, containingType);
		}

		@Override
		protected IListStructuralInfo getStructuralInfo(IListTypeInfo listType) {
			ITypeInfo itemType = listType.getItemType();
			final ListStructureCustomization customization = getListStructureCustomization(listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (customization != null) {
				final IListStructuralInfo base = super.getStructuralInfo(listType);
				return new CustomizedStructuralInfo(reflectionUI, base, listType, customization);
			}
			return super.getStructuralInfo(listType);
		}

		@Override
		protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
			final TypeCustomization t = getTypeCustomization(type.getName());
			if (t != null) {
				if (t.specificProperties != null) {
					if (t.specificProperties.entrySet().size() > 0) {
						Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
						result.putAll(t.specificProperties);
						return result;
					}
				}
			}
			return super.getSpecificProperties(type);
		}

		@Override
		protected Map<String, Object> getSpecificProperties(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method));
			if (m != null) {
				if (m.specificProperties != null) {
					if (m.specificProperties.entrySet().size() > 0) {
						Map<String, Object> result = new HashMap<String, Object>(
								super.getSpecificProperties(method, containingType));
						result.putAll(m.specificProperties);
						return result;
					}
				}
			}
			return super.getSpecificProperties(method, containingType);
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
			MethodCustomization m = getMethodCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method));
			if (m != null) {
				if (m.readOnlyForced) {
					return true;
				}
			}
			return super.isReadOnly(method, containingType);
		}

		@Override
		protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method));
			if (m != null) {
				List<IParameterInfo> result = new ArrayList<IParameterInfo>(
						super.getParameters(method, containingType));
				for (Iterator<IParameterInfo> it = result.iterator(); it.hasNext();) {
					IParameterInfo param = it.next();
					ParameterCustomization p = getParameterCustomization(containingType.getName(),
							ReflectionUIUtils.getMethodInfoSignature(method), param.getName());
					if ((p != null) && p.hidden) {
						it.remove();
					}
				}

			}
			return super.getParameters(method, containingType);
		}

		@Override
		protected String getCaption(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method));
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
				for (Iterator<IFieldInfo> it = result.iterator(); it.hasNext();) {
					IFieldInfo field = it.next();
					FieldCustomization f = getFieldCustomization(type.getName(), field.getName());
					if ((f != null) && f.hidden) {
						it.remove();
					}
				}
				if (t.customFieldsOrder != null) {
					Collections.sort(result, ReflectionUIUtils.getInfosComparator(t.customFieldsOrder, result));
				}
				return result;
			}
			return super.getFields(type);
		}

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(type.getName());
			if (t != null) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors(type));
				for (Iterator<IMethodInfo> it = result.iterator(); it.hasNext();) {
					IMethodInfo method = it.next();
					MethodCustomization m = getMethodCustomization(type.getName(),
							ReflectionUIUtils.getMethodInfoSignature(method));
					if ((m != null) && m.hidden) {
						it.remove();
					}
				}
				if (t.customMethodsOrder != null) {
					Collections.sort(result, ReflectionUIUtils.getInfosComparator(t.customMethodsOrder, result));
				}
				return result;
			}
			return super.getConstructors(type);
		}

		@Override
		protected List<IMethodInfo> getMethods(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(type.getName());
			if (t != null) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
				for (Iterator<IMethodInfo> it = result.iterator(); it.hasNext();) {
					IMethodInfo method = it.next();
					MethodCustomization m = getMethodCustomization(type.getName(),
							ReflectionUIUtils.getMethodInfoSignature(method));
					if ((m != null) && m.hidden) {
						it.remove();
					}
				}
				if (t.customMethodsOrder != null) {
					Collections.sort(result, ReflectionUIUtils.getInfosComparator(t.customMethodsOrder, result));
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

		@Override
		protected InfoCategory getCategory(IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType.getName(), field.getName());
			if (f != null) {
				CustomizationCategory category = f.getCategory();
				List<CustomizationCategory> categories = getTypeCustomization(
						containingType.getName()).memberCategories;
				int categoryPosition = categories.indexOf(category);
				if (categoryPosition != -1) {
					return new InfoCategory(category.getCaption(), categoryPosition);
				}
			}
			return super.getCategory(field, containingType);
		}

		@Override
		protected InfoCategory getCategory(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method));
			if (m != null) {
				CustomizationCategory category = m.getCategory();
				List<CustomizationCategory> categories = getTypeCustomization(
						containingType.getName()).memberCategories;
				int categoryPosition = categories.indexOf(category);
				if (categoryPosition != -1) {
					return new InfoCategory(category.getCaption(), categoryPosition);
				}
			}
			return super.getCategory(method, containingType);
		}

		@Override
		protected String getOnlineHelp(IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType.getName(), field.getName());
			if (f != null) {
				if (f.onlineHelp != null) {
					return f.onlineHelp;
				}
			}
			return super.getOnlineHelp(field, containingType);
		}

		@Override
		protected String getOnlineHelp(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			ParameterCustomization p = getParameterCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method), param.getName());
			if (p != null) {
				if (p.onlineHelp != null) {
					return p.onlineHelp;
				}
			}
			return super.getOnlineHelp(param, method, containingType);
		}

		@Override
		protected String getOnlineHelp(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(type.getName());
			if (t != null) {
				if (t.onlineHelp != null) {
					return t.onlineHelp;
				}
			}
			return super.getOnlineHelp(type);
		}

		@Override
		protected String getOnlineHelp(IMethodInfo method, ITypeInfo containingType) {
			MethodCustomization m = getMethodCustomization(containingType.getName(),
					ReflectionUIUtils.getMethodInfoSignature(method));
			if (m != null) {
				if (m.onlineHelp != null) {
					return m.onlineHelp;
				}
			}
			return super.getOnlineHelp(method, containingType);
		}

		@Override
		protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
			FieldCustomization f = getFieldCustomization(containingType.getName(), field.getName());
			if (f != null) {
				if (f.valueOptionsFieldName != null) {
					IFieldInfo valueOptionsfield = ReflectionUIUtils.findInfoByName(containingType.getFields(),
							f.valueOptionsFieldName);
					IListTypeInfo valueOptionsfieldType = (IListTypeInfo) valueOptionsfield.getType();
					Object options = valueOptionsfield.getValue(object);
					if (options == null) {
						return null;
					}
					return valueOptionsfieldType.toArray(options);
				}
			}
			return super.getValueOptions(object, field, containingType);
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

}
