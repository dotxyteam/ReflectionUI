package xy.reflect.ui.info.type.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.ValueAsListField;
import xy.reflect.ui.info.field.EncapsulatedValueField;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MethodAsField;
import xy.reflect.ui.info.method.FieldAsGetter;
import xy.reflect.ui.info.method.FieldAsSetter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.EnumerationItemInfoProxy;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.EmbeddedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.CustomizedStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

@XmlRootElement
public class InfoCustomizations implements Serializable {

	private static final long serialVersionUID = 1L;

	public static InfoCustomizations defaultInstance;
	public static final String ACTIVE_CUSTOMIZATIONS_PROPERTY_KEY = InfoCustomizations.class.getName();

	transient protected Factory proxyFactory;
	protected List<TypeCustomization> typeCustomizations = new ArrayList<InfoCustomizations.TypeCustomization>();
	protected List<ListCustomization> listCustomizations = new ArrayList<InfoCustomizations.ListCustomization>();
	protected List<EnumerationCustomization> enumerationCustomizations = new ArrayList<InfoCustomizations.EnumerationCustomization>();

	protected Factory createCustomizationsProxyFactory(ReflectionUI reflectionUI) {
		return new Factory(reflectionUI);
	}

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "InfoCustomizations.DEFAULT";
		} else {
			return super.toString();
		}
	}

	public static InfoCustomizations getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new InfoCustomizations();
			if (SystemProperties.areDefaultInfoCustomizationsActive()) {
				String filePath = SystemProperties.getDefaultInfoCustomizationsFilePath();
				File file = new File(filePath);
				if (file.exists()) {
					try {
						defaultInstance.loadFromFile(file);
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
				}
			}
		}
		return defaultInstance;
	}

	public ITypeInfo get(ReflectionUI reflectionUI, ITypeInfo type) {
		if (proxyFactory == null) {
			proxyFactory = createCustomizationsProxyFactory(reflectionUI);
		}
		return proxyFactory.get(type);
	}

	public List<TypeCustomization> getTypeCustomizations() {
		return typeCustomizations;
	}

	public void setTypeCustomizations(List<TypeCustomization> typeCustomizations) {
		this.typeCustomizations = typeCustomizations;
	}

	public List<ListCustomization> getListCustomizations() {
		return listCustomizations;
	}

	public void setListCustomizations(List<ListCustomization> listCustomizations) {
		this.listCustomizations = listCustomizations;
	}

	public List<EnumerationCustomization> getEnumerationCustomizations() {
		return enumerationCustomizations;
	}

	public void setEnumerationCustomizations(List<EnumerationCustomization> enumerationCustomizations) {
		this.enumerationCustomizations = enumerationCustomizations;
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

	public void loadFromStream(InputStream input) throws IOException {
		InfoCustomizations loaded;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(InfoCustomizations.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			loaded = (InfoCustomizations) jaxbUnmarshaller.unmarshal(input);
		} catch (Exception e) {
			throw new IOException(e);
		}
		typeCustomizations = loaded.typeCustomizations;
		listCustomizations = loaded.listCustomizations;
		enumerationCustomizations = loaded.enumerationCustomizations;

		fillXMLSerializationGap();
	}

	protected void fillXMLSerializationGap() {
		for (TypeCustomization t : typeCustomizations) {
			List<AbstractMemberCustomization> allMembers = new ArrayList<AbstractMemberCustomization>();
			allMembers.addAll(t.fieldsCustomizations);
			allMembers.addAll(t.methodsCustomizations);
			for (AbstractMemberCustomization m : allMembers) {
				if (m.category != null) {
					for (CustomizationCategory c : t.memberCategories) {
						if (m.category.caption.equals(c.caption)) {
							m.category = c;
						}
					}
				}
			}
		}
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
		toSave.listCustomizations = listCustomizations;
		toSave.enumerationCustomizations = enumerationCustomizations;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(InfoCustomizations.class);
			javax.xml.bind.Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(toSave, output);
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

	public static ParameterCustomization getParameterCustomization(MethodCustomization m, String paramName) {
		return getParameterCustomization(m, paramName, false);
	}

	public static ParameterCustomization getParameterCustomization(MethodCustomization m, String paramName,
			boolean createIfNotFound) {
		if (m != null) {
			for (ParameterCustomization p : m.parametersCustomizations) {
				if (paramName.equals(p.parameterName)) {
					return p;
				}
			}
			if (createIfNotFound) {
				ParameterCustomization p = new ParameterCustomization();
				p.setParameterName(paramName);
				m.parametersCustomizations.add(p);
				return p;
			}
		}
		return null;
	}

	public static FieldCustomization getFieldCustomization(TypeCustomization t, String fieldName) {
		return getFieldCustomization(t, fieldName, false);
	}

	public static FieldCustomization getFieldCustomization(TypeCustomization t, String fieldName,
			boolean createIfNotFound) {
		if (t != null) {
			for (FieldCustomization f : t.fieldsCustomizations) {
				if (fieldName.equals(f.fieldName)) {
					return f;
				}
			}
			if (createIfNotFound) {
				FieldCustomization f = new FieldCustomization();
				f.setFieldName(fieldName);
				t.fieldsCustomizations.add(f);
				return f;
			}
		}
		return null;
	}

	public static MethodCustomization getMethodCustomization(TypeCustomization t, String methodSignature) {
		return getMethodCustomization(t, methodSignature, false);
	}

	public static MethodCustomization getMethodCustomization(TypeCustomization t, String methodSignature,
			boolean createIfNotFound) {
		if (t != null) {
			for (MethodCustomization m : t.methodsCustomizations) {
				if (methodSignature.equals(m.methodSignature)) {
					return m;
				}
			}
			if (createIfNotFound) {
				MethodCustomization m = new MethodCustomization();
				m.setMethodSignature(methodSignature);
				t.methodsCustomizations.add(m);
				return m;
			}
		}
		return null;
	}

	public static TypeCustomization getTypeCustomization(InfoCustomizations infoCustomizations, String typeName) {
		return getTypeCustomization(infoCustomizations, typeName, false);
	}

	public static TypeCustomization getTypeCustomization(InfoCustomizations infoCustomizations, String typeName,
			boolean createIfNotFound) {
		for (TypeCustomization t : infoCustomizations.typeCustomizations) {
			if (typeName.equals(t.typeName)) {
				return t;
			}
		}
		if (createIfNotFound) {
			TypeCustomization t = new TypeCustomization();
			t.setTypeName(typeName);
			infoCustomizations.typeCustomizations.add(t);
			return t;
		}
		return null;
	}

	public static ListCustomization getListCustomization(InfoCustomizations infoCustomizations, String listTypeName,
			String itemTypeName) {
		return getListCustomization(infoCustomizations, listTypeName, itemTypeName, false);
	}

	public static ListCustomization getListCustomization(InfoCustomizations infoCustomizations, String listTypeName,
			String itemTypeName, boolean createIfNotFound) {
		for (ListCustomization l : infoCustomizations.listCustomizations) {
			if (listTypeName.equals(l.listTypeName)) {
				if (ReflectionUIUtils.equalsOrBothNull(l.itemTypeName, itemTypeName)) {
					return l;
				}
			}
		}
		if (createIfNotFound) {
			ListCustomization l = new ListCustomization();
			l.setListTypeName(listTypeName);
			l.setItemTypeName(itemTypeName);
			infoCustomizations.listCustomizations.add(l);
			return l;
		}
		return null;
	}

	public static ColumnCustomization getColumnCustomization(ListCustomization l, String columnName) {
		return getColumnCustomization(l, columnName, false);
	}

	public static ColumnCustomization getColumnCustomization(ListCustomization l, String columnName,
			boolean createIfNotFound) {
		for (ColumnCustomization c : l.columnCustomizations) {
			if (columnName.equals(c.columnName)) {
				return c;
			}
		}
		if (createIfNotFound) {
			ColumnCustomization c = new ColumnCustomization();
			c.setColumnName(columnName);
			l.columnCustomizations.add(c);
			return c;
		}
		return null;
	}

	public static EnumerationItemCustomization getEnumerationItemCustomization(EnumerationCustomization e,
			String enumItemName) {
		return getEnumerationItemCustomization(e, enumItemName, false);
	}

	public static EnumerationItemCustomization getEnumerationItemCustomization(EnumerationCustomization e,
			String enumItemName, boolean createIfNotFound) {
		for (EnumerationItemCustomization i : e.itemCustomizations) {
			if (enumItemName.equals(i.itemName)) {
				return i;
			}
		}
		if (createIfNotFound) {
			EnumerationItemCustomization i = new EnumerationItemCustomization();
			i.setItemName(enumItemName);
			e.itemCustomizations.add(i);
			return i;
		}
		return null;
	}

	public static EnumerationCustomization getEnumerationCustomization(InfoCustomizations infoCustomizations,
			String enumTypeName) {
		return getEnumerationCustomization(infoCustomizations, enumTypeName, false);
	}

	public static EnumerationCustomization getEnumerationCustomization(InfoCustomizations infoCustomizations,
			String enumTypeName, boolean createIfNotFound) {
		for (EnumerationCustomization e : infoCustomizations.enumerationCustomizations) {
			if (enumTypeName.equals(e.enumerationTypeName)) {
				return e;
			}
		}
		if (createIfNotFound) {
			EnumerationCustomization e = new EnumerationCustomization();
			e.setEnumerationTypeName(enumTypeName);
			infoCustomizations.enumerationCustomizations.add(e);
			return e;
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
		result = prime * result + ((listCustomizations == null) ? 0 : listCustomizations.hashCode());
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
		if (listCustomizations == null) {
			if (other.listCustomizations != null)
				return false;
		} else if (!listCustomizations.equals(other.listCustomizations))
			return false;
		if (typeCustomizations == null) {
			if (other.typeCustomizations != null)
				return false;
		} else if (!typeCustomizations.equals(other.typeCustomizations))
			return false;
		return true;
	}

	public static abstract class AbstractCustomization implements Serializable {
		private static final long serialVersionUID = 1L;

	}

	public static abstract class AbstractInfoCustomization extends AbstractCustomization {

		private static final long serialVersionUID = 1L;

		protected Map<String, Object> specificProperties;

		public Map<String, Object> getSpecificProperties() {
			return specificProperties;
		}

		public void setSpecificProperties(Map<String, Object> specificProperties) {
			this.specificProperties = specificProperties;
		}

	}

	public static class TypeCustomization extends AbstractInfoCustomization implements Comparable<TypeCustomization> {
		private static final long serialVersionUID = 1L;

		protected String typeName;
		protected String customTypeCaption;
		protected List<FieldCustomization> fieldsCustomizations = new ArrayList<InfoCustomizations.FieldCustomization>();
		protected List<MethodCustomization> methodsCustomizations = new ArrayList<InfoCustomizations.MethodCustomization>();
		protected List<String> customFieldsOrder;
		protected List<String> customMethodsOrder;
		protected String onlineHelp;
		protected List<CustomizationCategory> memberCategories = new ArrayList<CustomizationCategory>();
		protected boolean undoManagementHidden = false;
		protected boolean immutableForced = false;
		protected boolean abstractForced = false;

		protected List<ITypeInfoFinder> polymorphicSubTypeFinders = new ArrayList<ITypeInfoFinder>();

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public boolean isAbstractForced() {
			return abstractForced;
		}

		public void setAbstractForced(boolean abtractForced) {
			this.abstractForced = abtractForced;
		}

		public boolean isImmutableForced() {
			return immutableForced;
		}

		public void setImmutableForced(boolean immutableForced) {
			this.immutableForced = immutableForced;
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
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

		public void setCustomFieldsOrder(List<String> customFieldsOrder) {
			this.customFieldsOrder = customFieldsOrder;
		}

		public List<String> getCustomMethodsOrder() {
			return customMethodsOrder;
		}

		public void setCustomMethodsOrder(List<String> customMethodsOrder) {
			this.customMethodsOrder = customMethodsOrder;
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

		public boolean isUndoManagementHidden() {
			return undoManagementHidden;
		}

		public void setUndoManagementHidden(boolean undoManagementHidden) {
			this.undoManagementHidden = undoManagementHidden;
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
			return ReflectionUIUtils.compareNullables(typeName, o.typeName);
		}

		@Override
		public String toString() {
			return "TypeCustomization [typeName=" + typeName + "]";
		}

	}

	public static class CustomizationCategory extends AbstractCustomization implements Serializable {

		private static final long serialVersionUID = 1L;
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
			return "CustomizationCategory [caption=" + caption + "]";
		}

	}

	public static abstract class AbstractMemberCustomization extends AbstractInfoCustomization {
		private static final long serialVersionUID = 1L;

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

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
		}
	}

	public static class FieldTypeSpecificities extends InfoCustomizations {
		private static final long serialVersionUID = 1L;

		public void ensureProxyFactoryInitialized(ReflectionUI reflectionUI, final String typeName,
				final String fieldName) {
			if (proxyFactory == null) {
				proxyFactory = new Factory(reflectionUI) {
					@Override
					public String getIdentifier() {
						return "FieldTypeSpecificities [typeName=" + typeName + ", fieldName=" + fieldName + "]";
					}
				};
			}
		}

	}

	public static class FieldCustomization extends AbstractMemberCustomization
			implements Comparable<FieldCustomization> {
		private static final long serialVersionUID = 1L;

		protected String fieldName;
		protected String customFieldCaption;
		protected boolean nullableFacetHidden = false;
		protected boolean getOnlyForced = false;
		protected String valueOptionsFieldName;
		protected ValueReturnMode customValueReturnMode;
		protected String nullValueLabel;
		protected boolean displayedAsMethods = false;
		protected boolean displayedAsSingletonList = false;
		protected boolean displayedEncapsulated = false;
		protected FieldTypeSpecificities specificTypeCustomizations = new FieldTypeSpecificities();

		public FieldTypeSpecificities getSpecificTypeCustomizations() {
			return specificTypeCustomizations;
		}

		public void setSpecificTypeCustomizations(FieldTypeSpecificities specificTypeCustomizations) {
			this.specificTypeCustomizations = specificTypeCustomizations;
		}

		public boolean isDisplayedEncapsulated() {
			return displayedEncapsulated;
		}

		public void setDisplayedEncapsulated(boolean displayedEncapsulated) {
			this.displayedEncapsulated = displayedEncapsulated;
		}

		public boolean isDisplayedAsMethods() {
			return displayedAsMethods;
		}

		public void setDisplayedAsMethods(boolean displayedAsMethods) {
			this.displayedAsMethods = displayedAsMethods;
		}

		public boolean isDisplayedAsSingletonList() {
			return displayedAsSingletonList;
		}

		public void setDisplayedAsSingletonList(boolean displayedAsSingletonList) {
			this.displayedAsSingletonList = displayedAsSingletonList;
		}

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public boolean isNullableFacetHidden() {
			return nullableFacetHidden;
		}

		public void setNullableFacetHidden(boolean nullableFacetHidden) {
			this.nullableFacetHidden = nullableFacetHidden;
		}

		public ValueReturnMode getCustomValueReturnMode() {
			return customValueReturnMode;
		}

		public void setCustomValueReturnMode(ValueReturnMode customValueReturnMode) {
			this.customValueReturnMode = customValueReturnMode;
		}

		public String getNullValueLabel() {
			return nullValueLabel;
		}

		public void setNullValueLabel(String nullValueLabel) {
			this.nullValueLabel = nullValueLabel;
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
			return ReflectionUIUtils.compareNullables(fieldName, o.fieldName);
		}

		@Override
		public String toString() {
			return "FieldCustomization [fieldName=" + fieldName + "]";
		}

	}

	public static class MethodCustomization extends AbstractMemberCustomization
			implements Comparable<MethodCustomization> {
		private static final long serialVersionUID = 1L;

		protected String methodSignature;
		protected String customMethodCaption;
		protected boolean readOnlyForced = false;
		protected boolean validating = false;
		protected List<ParameterCustomization> parametersCustomizations = new ArrayList<InfoCustomizations.ParameterCustomization>();
		protected ValueReturnMode customValueReturnMode;
		protected String nullReturnValueLabel;
		protected boolean displayedAsField = false;

		public boolean isDisplayedAsField() {
			return displayedAsField;
		}

		public void setDisplayedAsField(boolean displayedAsField) {
			this.displayedAsField = displayedAsField;
		}

		public boolean isValidating() {
			return validating;
		}

		public void setValidating(boolean validating) {
			this.validating = validating;
		}

		public boolean isReadOnlyForced() {
			return readOnlyForced;
		}

		public void setReadOnlyForced(boolean readOnlyForced) {
			this.readOnlyForced = readOnlyForced;
		}

		public ValueReturnMode getCustomValueReturnMode() {
			return customValueReturnMode;
		}

		public void setCustomValueReturnMode(ValueReturnMode customReturnValueReturnMode) {
			this.customValueReturnMode = customReturnValueReturnMode;
		}

		public String getNullReturnValueLabel() {
			return nullReturnValueLabel;
		}

		public void setNullReturnValueLabel(String nullReturnValueLabel) {
			this.nullReturnValueLabel = nullReturnValueLabel;
		}

		public String getMethodSignature() {
			return methodSignature;
		}

		public void setMethodSignature(String methodSignature) {
			this.methodSignature = methodSignature;
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
			return ReflectionUIUtils.compareNullables(methodSignature, o.methodSignature);
		}

		@Override
		public String toString() {
			return "MethodCustomization [methodSignature=" + methodSignature + "]";
		}

	}

	public static class ParameterCustomization extends AbstractInfoCustomization
			implements Comparable<ParameterCustomization> {
		private static final long serialVersionUID = 1L;

		protected String parameterName;
		protected String customParameterCaption;
		protected boolean hidden = false;
		protected boolean nullableFacetHidden = false;
		protected String onlineHelp;

		public String getParameterName() {
			return parameterName;
		}

		public void setParameterName(String parameterName) {
			this.parameterName = parameterName;
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
		public int compareTo(ParameterCustomization o) {
			return ReflectionUIUtils.compareNullables(parameterName, o.parameterName);
		}

		@Override
		public String toString() {
			return "ParameterCustomization [parameterName=" + parameterName + "]";
		}

	}

	public static class ListItemFieldShortcut extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String fieldName;
		protected boolean alwaysShown = true;
		protected String customFieldCaption;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public boolean isAlwaysShown() {
			return alwaysShown;
		}

		public void setAlwaysShown(boolean alwaysShown) {
			this.alwaysShown = alwaysShown;
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
			ListItemFieldShortcut other = (ListItemFieldShortcut) obj;
			if (fieldName == null) {
				if (other.fieldName != null)
					return false;
			} else if (!fieldName.equals(other.fieldName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ListItemFieldShortcut [fieldName=" + fieldName + "]";
		}

	}

	public static class ListItemMethodShortcut extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String methodSignature;
		protected boolean alwaysShown = true;
		protected String customMethodCaption;

		public String getMethodSignature() {
			return methodSignature;
		}

		public void setMethodSignature(String methodSignature) {
			this.methodSignature = methodSignature;
		}

		public boolean isAlwaysShown() {
			return alwaysShown;
		}

		public void setAlwaysShown(boolean alwaysShown) {
			this.alwaysShown = alwaysShown;
		}

		public String getCustomMethodCaption() {
			return customMethodCaption;
		}

		public void setCustomMethodCaption(String customMethodCaption) {
			this.customMethodCaption = customMethodCaption;
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
			ListItemMethodShortcut other = (ListItemMethodShortcut) obj;
			if (methodSignature == null) {
				if (other.methodSignature != null)
					return false;
			} else if (!methodSignature.equals(other.methodSignature))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ListItemMethodShortcut [methodSignature=" + methodSignature + "]";
		}

	}

	public static class InfoFilter extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String value = "";
		protected boolean regularExpression = false;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public boolean isRegularExpression() {
			return regularExpression;
		}

		public void setRegularExpression(boolean regularExpression) {
			this.regularExpression = regularExpression;
		}

		public boolean matches(String s) {
			if (regularExpression) {
				Pattern pattern = Pattern.compile(value);
				Matcher matcher = pattern.matcher(s);
				return matcher.matches();
			} else {
				return s.equals(value);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (regularExpression ? 1231 : 1237);
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			InfoFilter other = (InfoFilter) obj;
			if (regularExpression != other.regularExpression)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InfoFilter [value=" + value + ", regularExpression=" + regularExpression + "]";
		}

	}

	public static class ListInstanciationOption extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected ITypeInfoFinder customInstanceTypeFinder;

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getCustomInstanceTypeFinder() {
			return customInstanceTypeFinder;
		}

		public void setCustomInstanceTypeFinder(ITypeInfoFinder customInstanceTypeFinder) {
			this.customInstanceTypeFinder = customInstanceTypeFinder;
		}

	}

	public static class ListEditOptions extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected boolean itemCreationEnabled = true;
		protected boolean itemDeletionEnabled = true;
		protected boolean itemMoveEnabled = true;
		protected ListInstanciationOption listInstanciationOption;

		public ListInstanciationOption getListInstanciationOption() {
			return listInstanciationOption;
		}

		public void setListInstanciationOption(ListInstanciationOption listInstanciationOption) {
			this.listInstanciationOption = listInstanciationOption;
		}

		public boolean isItemCreationEnabled() {
			return itemCreationEnabled;
		}

		public void setItemCreationEnabled(boolean itemCreationEnabled) {
			this.itemCreationEnabled = itemCreationEnabled;
		}

		public boolean isItemDeletionEnabled() {
			return itemDeletionEnabled;
		}

		public void setItemDeletionEnabled(boolean itemDeletionEnabled) {
			this.itemDeletionEnabled = itemDeletionEnabled;
		}

		public boolean isItemMoveEnabled() {
			return itemMoveEnabled;
		}

		public void setItemMoveEnabled(boolean itemMoveEnabled) {
			this.itemMoveEnabled = itemMoveEnabled;
		}

	}

	public static class EnumerationItemCustomization extends AbstractCustomization
			implements Comparable<EnumerationItemCustomization> {

		private static final long serialVersionUID = 1L;

		protected String itemName;
		protected String customCaption;
		protected boolean hidden;

		public String getItemName() {
			return itemName;
		}

		public void setItemName(String itemName) {
			this.itemName = itemName;
		}

		public String getCustomCaption() {
			return customCaption;
		}

		public void setCustomCaption(String customCaption) {
			this.customCaption = customCaption;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		@Override
		public int compareTo(EnumerationItemCustomization o) {
			int result = ReflectionUIUtils.compareNullables(itemName, o.itemName);
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itemName == null) ? 0 : itemName.hashCode());
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
			EnumerationItemCustomization other = (EnumerationItemCustomization) obj;
			if (itemName == null) {
				if (other.itemName != null)
					return false;
			} else if (!itemName.equals(other.itemName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "EnumerationItemCustomization [itemName=" + itemName + "]";
		}

	}

	public static class EnumerationCustomization extends AbstractCustomization
			implements Comparable<EnumerationCustomization> {

		private static final long serialVersionUID = 1L;
		protected String enumerationTypeName;
		protected List<EnumerationItemCustomization> itemCustomizations = new ArrayList<EnumerationItemCustomization>();
		protected boolean staticEnumerationForced = false;

		public String getEnumerationTypeName() {
			return enumerationTypeName;
		}

		public void setEnumerationTypeName(String enumerationTypeName) {
			this.enumerationTypeName = enumerationTypeName;
		}

		public List<EnumerationItemCustomization> getItemCustomizations() {
			return itemCustomizations;
		}

		public void setItemCustomizations(List<EnumerationItemCustomization> itemCustomizations) {
			this.itemCustomizations = itemCustomizations;
		}

		public boolean isStaticEnumerationForced() {
			return staticEnumerationForced;
		}

		public void setStaticEnumerationForced(boolean staticEnumerationForced) {
			this.staticEnumerationForced = staticEnumerationForced;
		}

		@Override
		public int compareTo(EnumerationCustomization o) {
			int result = ReflectionUIUtils.compareNullables(enumerationTypeName, o.enumerationTypeName);
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((enumerationTypeName == null) ? 0 : enumerationTypeName.hashCode());
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
			EnumerationCustomization other = (EnumerationCustomization) obj;
			if (enumerationTypeName == null) {
				if (other.enumerationTypeName != null)
					return false;
			} else if (!enumerationTypeName.equals(other.enumerationTypeName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "EnumerationCustomization [enumerationTypeName=" + enumerationTypeName + "]";
		}

	}

	public static class ListCustomization extends AbstractCustomization implements Comparable<ListCustomization> {
		private static final long serialVersionUID = 1L;

		protected String listTypeName;
		protected String itemTypeName;
		protected boolean itemTypeColumnAdded;
		protected boolean positionColumnAdded;
		protected boolean fieldColumnsAdded;
		protected boolean stringValueColumnAdded;
		protected List<ColumnCustomization> columnCustomizations = new ArrayList<ColumnCustomization>();
		protected List<String> columnsCustomOrder;
		protected TreeStructureDiscoverySettings treeStructureDiscoverySettings;
		protected List<ListItemFieldShortcut> allowedItemFieldShortcuts = new ArrayList<ListItemFieldShortcut>();
		protected List<ListItemMethodShortcut> allowedItemMethodShortcuts = new ArrayList<ListItemMethodShortcut>();
		protected List<InfoFilter> methodsExcludedFromItemDetails = new ArrayList<InfoFilter>();
		protected List<InfoFilter> fieldsExcludedFromItemDetails = new ArrayList<InfoFilter>();
		protected boolean itemDetailsViewDisabled;
		protected ListEditOptions editOptions = new ListEditOptions();
		protected boolean listSorted = false;
		protected IListItemDetailsAccessMode customDetailsAccessMode = null;

		@XmlElements({ @XmlElement(name = "detachedDetailsAccessMode", type = DetachedItemDetailsAccessMode.class),
				@XmlElement(name = "embeddedDetailsAccessMode", type = EmbeddedItemDetailsAccessMode.class) })
		public IListItemDetailsAccessMode getCustomDetailsAccessMode() {
			return customDetailsAccessMode;
		}

		public void setCustomDetailsAccessMode(IListItemDetailsAccessMode customDetailsAccessMode) {
			this.customDetailsAccessMode = customDetailsAccessMode;
		}

		public ListEditOptions getEditOptions() {
			return editOptions;
		}

		public void setEditOptions(ListEditOptions editOptions) {
			this.editOptions = editOptions;
		}

		public boolean isListSorted() {
			return listSorted;
		}

		public void setListSorted(boolean listSorted) {
			this.listSorted = listSorted;
		}

		public List<InfoFilter> getFieldsExcludedFromItemDetails() {
			return fieldsExcludedFromItemDetails;
		}

		public void setFieldsExcludedFromItemDetails(List<InfoFilter> fieldsExcludedFromItemDetails) {
			this.fieldsExcludedFromItemDetails = fieldsExcludedFromItemDetails;
		}

		public List<InfoFilter> getMethodsExcludedFromItemDetails() {
			return methodsExcludedFromItemDetails;
		}

		public void setMethodsExcludedFromItemDetails(List<InfoFilter> methods) {
			this.methodsExcludedFromItemDetails = methods;
		}

		public List<ListItemFieldShortcut> getAllowedItemFieldShortcuts() {
			return allowedItemFieldShortcuts;
		}

		public void setAllowedItemFieldShortcuts(List<ListItemFieldShortcut> allowedItemFieldShortcuts) {
			this.allowedItemFieldShortcuts = allowedItemFieldShortcuts;
		}

		public List<ListItemMethodShortcut> getAllowedItemMethodShortcuts() {
			return allowedItemMethodShortcuts;
		}

		public void setAllowedItemMethodShortcuts(List<ListItemMethodShortcut> allowedItemMethodShortcuts) {
			this.allowedItemMethodShortcuts = allowedItemMethodShortcuts;
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

		public void setListTypeName(String listTypeName) {
			this.listTypeName = listTypeName;
		}

		public String getItemTypeName() {
			return itemTypeName;
		}

		public void setItemTypeName(String itemTypeName) {
			this.itemTypeName = itemTypeName;
		}

		public List<ColumnCustomization> getColumnCustomizations() {
			return columnCustomizations;
		}

		public void setColumnCustomizations(List<ColumnCustomization> columnCustomizations) {
			this.columnCustomizations = columnCustomizations;
		}

		public boolean isItemDetailsViewDisabled() {
			return itemDetailsViewDisabled;
		}

		public void setItemDetailsViewDisabled(boolean itemDetailsViewDisabled) {
			this.itemDetailsViewDisabled = itemDetailsViewDisabled;
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

		public boolean isStringValueColumnAdded() {
			return stringValueColumnAdded;
		}

		public void setStringValueColumnAdded(boolean stringValueColumnAdded) {
			this.stringValueColumnAdded = stringValueColumnAdded;
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
			ListCustomization other = (ListCustomization) obj;
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
		public int compareTo(ListCustomization o) {
			int result = ReflectionUIUtils.compareNullables(listTypeName, o.listTypeName);
			if (result == 0) {
				result = ReflectionUIUtils.compareNullables(itemTypeName, o.itemTypeName);
			}
			return result;
		}

		@Override
		public String toString() {
			return "ListCustomization [listTypeName=" + listTypeName + ", itemTypeName=" + itemTypeName + "]";
		}

	}

	public static class TreeStructureDiscoverySettings extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected boolean heterogeneousTree;

		public boolean isHeterogeneousTree() {
			return heterogeneousTree;
		}

		public void setHeterogeneousTree(boolean heterogeneousTree) {
			this.heterogeneousTree = heterogeneousTree;
		}

	}

	public static class ColumnCustomization extends AbstractCustomization implements Comparable<ColumnCustomization> {

		private static final long serialVersionUID = 1L;
		protected String columnName;
		protected String customCaption;
		protected boolean hidden = false;
		protected Integer minimalCharacterCount;

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
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

		public Integer getMinimalCharacterCount() {
			return minimalCharacterCount;
		}

		public void setMinimalCharacterCount(Integer minimalCharacterCount) {
			this.minimalCharacterCount = minimalCharacterCount;
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
			return ReflectionUIUtils.compareNullables(columnName, o.columnName);
		}

		@Override
		public String toString() {
			return "ColumnCustomization [columnName=" + columnName + "]";
		}

	}

	protected class Factory extends HiddenNullableFacetsTypeInfoProxyFactory {

		public Factory(ReflectionUI reflectionUI) {
			super(reflectionUI);
		}

		@Override
		public String getIdentifier() {
			return InfoCustomizations.this.toString();
		}

		@Override
		protected boolean isConcrete(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, type.getName());
			if (t != null) {
				if (t.abstractForced) {
					return false;
				}
			}
			return super.isConcrete(type);
		}

		@Override
		protected ITypeInfoProxyFactory getTypeSpecificities(IFieldInfo field, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					ITypeInfoProxyFactory baseTypeSpecificities = field.getTypeSpecificities();
					if (baseTypeSpecificities == null) {
						f.specificTypeCustomizations.ensureProxyFactoryInitialized(reflectionUI, t.typeName,
								f.fieldName);
						return f.specificTypeCustomizations.proxyFactory;
					} else {
						return new TypeInfoProxyFactorChain(baseTypeSpecificities,
								f.specificTypeCustomizations.proxyFactory);
					}
				}
			}
			return super.getTypeSpecificities(field, containingType);
		}

		@Override
		protected boolean isImmutable(ITypeInfo type) {
			final TypeCustomization t = getTypeCustomization(InfoCustomizations.this, type.getName());
			if (t != null) {
				if (t.immutableForced) {
					return true;
				}
			}
			return super.isImmutable(type);
		}

		@Override
		protected Object[] toArray(IListTypeInfo listType, Object listValue) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				if (l.listSorted) {
					Object[] result = super.toArray(listType, listValue);
					Arrays.sort(result);
					return result;
				}
			}
			return super.toArray(listType, listValue);
		}

		@Override
		protected String getNullValueLabel(IFieldInfo field, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					if (f.nullValueLabel != null) {
						return f.nullValueLabel;
					}
				}
			}
			return super.getNullValueLabel(field, containingType);
		}

		@Override
		protected String getNullReturnValueLabel(IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					if (m.nullReturnValueLabel != null) {
						return m.nullReturnValueLabel;
					}
				}
			}
			return super.getNullReturnValueLabel(method, containingType);
		}

		@Override
		protected Object[] getPossibleValues(IEnumerationTypeInfo enumType) {
			EnumerationCustomization e = getEnumerationCustomization(InfoCustomizations.this, enumType.getName());
			if (e != null) {
				List<Object> result = new ArrayList<Object>();
				for (Object value : super.getPossibleValues(enumType)) {
					IEnumerationItemInfo valueInfo = getValueInfo(value, enumType);
					EnumerationItemCustomization i = getEnumerationItemCustomization(e, valueInfo.getName());
					if (i != null) {
						if (i.hidden) {
							continue;
						}
					}
					result.add(value);
				}
				return result.toArray();
			}
			return super.getPossibleValues(enumType);
		}

		@Override
		protected boolean isDynamicEnumeration(IEnumerationTypeInfo enumType) {
			EnumerationCustomization e = getEnumerationCustomization(InfoCustomizations.this, enumType.getName());
			if (e != null) {
				if (e.staticEnumerationForced) {
					return false;
				}
			}
			return super.isDynamicEnumeration(enumType);
		}

		@Override
		protected IEnumerationItemInfo getValueInfo(Object object, IEnumerationTypeInfo enumType) {
			IEnumerationItemInfo result = super.getValueInfo(object, enumType);
			EnumerationCustomization e = getEnumerationCustomization(InfoCustomizations.this, enumType.getName());
			if (e != null) {
				final EnumerationItemCustomization i = getEnumerationItemCustomization(e, result.getName());
				if (i != null) {
					return new EnumerationItemInfoProxy(result) {

						@Override
						public String getCaption() {
							if (i.customCaption != null) {
								return i.customCaption;
							}
							return super.getCaption();
						}

					};
				}
			}
			return result;
		}

		@Override
		protected ValueReturnMode getValueReturnMode(IFieldInfo field, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					if (f.customValueReturnMode != null) {
						return f.customValueReturnMode;
					}
				}
			}
			return super.getValueReturnMode(field, containingType);
		}

		@Override
		protected ValueReturnMode getValueReturnMode(IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					if (m.customValueReturnMode != null) {
						return m.customValueReturnMode;
					}
				}
			}
			return super.getValueReturnMode(method, containingType);
		}

		@Override
		protected List<AbstractListProperty> getDynamicProperties(IListTypeInfo listType,
				List<? extends ItemPosition> selection) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				List<AbstractListProperty> result = super.getDynamicProperties(listType, selection);
				result = new ArrayList<AbstractListProperty>(result);
				for (final ListItemFieldShortcut s : l.allowedItemFieldShortcuts) {
					final String fieldCaption;
					if (s.customFieldCaption != null) {
						fieldCaption = s.customFieldCaption;
					} else {
						fieldCaption = ReflectionUIUtils.identifierToCaption(s.fieldName);
					}
					boolean fieldFound = false;
					if (selection.size() == 1) {
						final ItemPosition itemPosition = selection.get(0);
						final Object item = itemPosition.getItem();
						if (item != null) {
							ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
							for (final IFieldInfo itemField : actualItemType.getFields()) {
								if (itemField.getName().equals(s.fieldName)) {
									AbstractListProperty property = new AbstractListProperty() {

										AbstractListProperty thisProperty = this;

										@Override
										public boolean isEnabled() {
											return true;
										}

										@Override
										public String getName() {
											return s.fieldName;
										}

										@Override
										public String getCaption() {
											return fieldCaption;
										}

										@Override
										public void setValue(Object object, Object value) {
											itemField.setValue(item, value);
											new ListModificationFactory(itemPosition, thisProperty)
													.set(itemPosition.getIndex(), item).applyAndGetOpposite();
										}

										@Override
										public Runnable getCustomUndoUpdateJob(Object object, Object value) {
											new ListModificationFactory(itemPosition, thisProperty)
													.set(itemPosition.getIndex(), item).applyAndGetOpposite();
											final Runnable itemCustomUndoUpdateJob = itemField
													.getCustomUndoUpdateJob(object, value);
											if (itemCustomUndoUpdateJob == null) {
												return null;
											}
											return new Runnable() {
												@Override
												public void run() {
													itemCustomUndoUpdateJob.run();
													new ListModificationFactory(itemPosition, thisProperty)
															.set(itemPosition.getIndex(), item).applyAndGetOpposite();
												}
											};
										}

										@Override
										public boolean isNullable() {
											return itemField.isNullable();
										}

										@Override
										public String getNullValueLabel() {
											return itemField.getNullValueLabel();
										}

										@Override
										public boolean isGetOnly() {
											return !new ListModificationFactory(itemPosition, this)
													.canSet(itemPosition.getIndex()) || itemField.isGetOnly();
										}

										@Override
										public ValueReturnMode getValueReturnMode() {
											return ValueReturnMode.combine(
													itemPosition.getContainingListType().getItemReturnMode(),
													itemField.getValueReturnMode());
										}

										@Override
										public Object getValue(Object object) {
											return itemField.getValue(item);
										}

										@Override
										public ITypeInfo getType() {
											return itemField.getType();
										}

										@Override
										public ITypeInfoProxyFactory getTypeSpecificities() {
											return null;
										}

									};
									result.add(property);
									fieldFound = true;
									break;
								}
							}
						}
					}
					if ((!fieldFound) && s.alwaysShown) {
						AbstractListProperty property = new AbstractListProperty() {

							@Override
							public boolean isEnabled() {
								return false;
							}

							@Override
							public String getName() {
								return s.fieldName;
							}

							@Override
							public String getCaption() {
								return fieldCaption;
							}

							@Override
							public void setValue(Object object, Object value) {
								throw new UnsupportedOperationException();
							}

							@Override
							public boolean isNullable() {
								throw new UnsupportedOperationException();
							}

							@Override
							public boolean isGetOnly() {
								throw new UnsupportedOperationException();
							}

							@Override
							public ValueReturnMode getValueReturnMode() {
								throw new UnsupportedOperationException();
							}

							@Override
							public Object getValue(Object object) {
								throw new UnsupportedOperationException();
							}

							@Override
							public ITypeInfo getType() {
								throw new UnsupportedOperationException();
							}

							@Override
							public ITypeInfoProxyFactory getTypeSpecificities() {
								return null;
							}

						};
						result.add(property);

					}
				}
				return result;
			}
			return super.getDynamicProperties(listType, selection);
		}

		@Override
		protected List<AbstractListAction> getDynamicActions(IListTypeInfo listType,
				List<? extends ItemPosition> selection) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				List<AbstractListAction> result = super.getDynamicActions(listType, selection);
				result = new ArrayList<AbstractListAction>(result);

				for (final ListItemMethodShortcut s : l.allowedItemMethodShortcuts) {
					final String methodName = ReflectionUIUtils.extractMethodNameFromSignature(s.methodSignature);
					final String methodCaption;
					if (s.customMethodCaption != null) {
						methodCaption = s.customMethodCaption;
					} else {
						methodCaption = ReflectionUIUtils.identifierToCaption(methodName);
					}
					boolean methodFound = false;
					if (selection.size() == 1) {
						final ItemPosition itemPosition = selection.get(0);
						final Object item = itemPosition.getItem();
						if (item != null) {
							ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
							for (final IMethodInfo itemMethod : actualItemType.getMethods()) {
								if (ReflectionUIUtils.getMethodSignature(itemMethod).equals(s.methodSignature)) {
									AbstractListAction action = new AbstractListAction() {

										AbstractListAction thisAction = this;

										@Override
										public String getName() {
											return methodName;
										}

										@Override
										public String getCaption() {
											return methodCaption;
										}

										@Override
										public boolean isEnabled() {
											return true;
										}

										@Override
										public boolean isReadOnly() {
											return !new ListModificationFactory(itemPosition, thisAction)
													.canSet(itemPosition.getIndex()) || itemMethod.isReadOnly();
										}

										@Override
										public String getNullReturnValueLabel() {
											return itemMethod.getNullReturnValueLabel();
										}

										@Override
										public ValueReturnMode getValueReturnMode() {
											return ValueReturnMode.combine(
													itemPosition.getContainingListType().getItemReturnMode(),
													itemMethod.getValueReturnMode());
										}

										@Override
										public void validateParameters(Object object, InvocationData invocationData)
												throws Exception {
											itemMethod.validateParameters(item, invocationData);
										}

										@Override
										public String getOnlineHelp() {
											return itemMethod.getOnlineHelp();
										}

										@Override
										public ITypeInfo getReturnValueType() {
											return itemMethod.getReturnValueType();
										}

										@Override
										public List<IParameterInfo> getParameters() {
											return itemMethod.getParameters();
										}

										@Override
										public Object invoke(Object object, InvocationData invocationData) {
											Object result = itemMethod.invoke(item, invocationData);
											new ListModificationFactory(itemPosition, thisAction)
													.set(itemPosition.getIndex(), item).applyAndGetOpposite();
											return result;
										}

										@Override
										public Runnable getUndoJob(Object object, final InvocationData invocationData) {
											final Runnable itemUndoJob = itemMethod.getUndoJob(item, invocationData);
											if (itemUndoJob == null) {
												return null;
											}
											return new Runnable() {

												@Override
												public void run() {
													itemUndoJob.run();
													new ListModificationFactory(itemPosition, thisAction)
															.set(itemPosition.getIndex(), item).applyAndGetOpposite()
															.applyAndGetOpposite();
												}

											};
										}

									};
									result.add(action);
									methodFound = true;
									break;
								}
							}
						}
					}
					if ((!methodFound) && s.alwaysShown) {
						result.add(new AbstractListAction() {

							@Override
							public String getName() {
								return methodName;
							}

							@Override
							public String getCaption() {
								return methodCaption;
							}

							@Override
							public boolean isEnabled() {
								return false;
							}

							@Override
							public Object invoke(Object object, InvocationData invocationData) {
								throw new UnsupportedOperationException();
							}

							@Override
							public ValueReturnMode getValueReturnMode() {
								throw new UnsupportedOperationException();
							}

						});
					}
				}
				return result;
			}
			return super.getDynamicActions(listType, selection);
		}

		@Override
		protected IListItemDetailsAccessMode getDetailsAccessMode(IListTypeInfo listType) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				if (l.customDetailsAccessMode != null) {
					return l.customDetailsAccessMode;
				}
			}
			return super.getDetailsAccessMode(listType);
		}

		@Override
		protected Object fromArray(IListTypeInfo listType, Object[] array) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				if (l.editOptions != null) {
					if (l.editOptions.listInstanciationOption != null) {
						Object newListInstance;
						if (l.editOptions.listInstanciationOption.customInstanceTypeFinder != null) {
							ITypeInfo customInstanceType = l.editOptions.listInstanciationOption.customInstanceTypeFinder
									.find(reflectionUI);
							newListInstance = ReflectionUIUtils.createDefaultInstance(reflectionUI, customInstanceType);
						} else {
							newListInstance = ReflectionUIUtils.createDefaultInstance(reflectionUI, listType);
						}
						super.replaceContent(listType, newListInstance, array);
						return newListInstance;
					}
				}
			}
			return super.fromArray(listType, array);
		}

		@Override
		protected boolean canInstanciateFromArray(IListTypeInfo listType) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				if (l.editOptions == null) {
					return false;
				}
				if (l.editOptions.listInstanciationOption != null) {
					return true;
				}
			}
			return super.canInstanciateFromArray(listType);
		}

		@Override
		protected boolean canReplaceContent(IListTypeInfo listType) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				if (l.editOptions == null) {
					return false;
				}
				if (l.editOptions.listInstanciationOption != null) {
					return false;
				}
			}
			return super.canReplaceContent(listType);
		}

		@Override
		protected boolean isInsertionAllowed(IListTypeInfo listType) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				if ((l.editOptions == null) || !l.editOptions.itemCreationEnabled) {
					return false;
				}
			}
			return super.isInsertionAllowed(listType);
		}

		@Override
		protected boolean isRemovalAllowed(IListTypeInfo listType) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				if ((l.editOptions == null) || !l.editOptions.itemDeletionEnabled) {
					return false;
				}
			}
			return super.isRemovalAllowed(listType);
		}

		@Override
		protected boolean isOrdered(IListTypeInfo listType) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				if ((l.editOptions == null) || !l.editOptions.itemMoveEnabled) {
					return false;
				}
				if (l.listSorted) {
					return false;
				}
			}
			return super.isOrdered(listType);
		}

		@Override
		protected boolean canViewItemDetails(IListTypeInfo listType) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization l = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (l != null) {
				if (l.itemDetailsViewDisabled) {
					return false;
				}
			}
			return super.canViewItemDetails(listType);
		}

		@Override
		protected boolean isModificationStackAccessible(ITypeInfo type) {
			TypeCustomization tc = getTypeCustomization(InfoCustomizations.this, type.getName());
			if (tc != null) {
				if (tc.isUndoManagementHidden()) {
					return false;
				}
			}
			return super.isModificationStackAccessible(type);
		}

		@Override
		protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
			TypeCustomization tc = getTypeCustomization(InfoCustomizations.this, type.getName());
			if (tc != null) {
				if (tc.polymorphicSubTypeFinders != null) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>(super.getPolymorphicInstanceSubTypes(type));
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
		protected IListStructuralInfo getStructuralInfo(IListTypeInfo listType) {
			ITypeInfo itemType = listType.getItemType();
			final ListCustomization customization = getListCustomization(InfoCustomizations.this, listType.getName(),
					(itemType == null) ? null : itemType.getName());
			if (customization != null) {
				final IListStructuralInfo base = super.getStructuralInfo(listType);
				return new CustomizedStructuralInfo(reflectionUI, base, listType, customization);
			}
			return super.getStructuralInfo(listType);
		}

		@Override
		protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
			Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
			result.put(ACTIVE_CUSTOMIZATIONS_PROPERTY_KEY, InfoCustomizations.this);
			final TypeCustomization t = getTypeCustomization(InfoCustomizations.this, type.getName());
			if (t != null) {
				if (t.specificProperties != null) {
					if (t.specificProperties.entrySet().size() > 0) {
						result.putAll(t.specificProperties);
					}
				}
			}
			return result;
		}

		@Override
		protected Map<String, Object> getSpecificProperties(IMethodInfo method, ITypeInfo containingType) {
			Map<String, Object> result = new HashMap<String, Object>(
					super.getSpecificProperties(method, containingType));
			result.put(ACTIVE_CUSTOMIZATIONS_PROPERTY_KEY, InfoCustomizations.this);
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					if (m.specificProperties != null) {
						if (m.specificProperties.entrySet().size() > 0) {
							result.putAll(m.specificProperties);
						}
					}
				}
			}
			return result;
		}

		@Override
		protected Map<String, Object> getSpecificProperties(IFieldInfo field, ITypeInfo containingType) {
			Map<String, Object> result = new HashMap<String, Object>(
					super.getSpecificProperties(field, containingType));
			result.put(ACTIVE_CUSTOMIZATIONS_PROPERTY_KEY, InfoCustomizations.this);
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					if (f.specificProperties != null) {
						for (String key : f.specificProperties.keySet()) {
							Object value = f.specificProperties.get(key);
							if (value != null) {
								result.put(key, value);
							}
						}
					}
				}
			}
			return result;
		}

		@Override
		protected Map<String, Object> getSpecificProperties(IParameterInfo param, IMethodInfo method,
				ITypeInfo containingType) {
			Map<String, Object> result = new HashMap<String, Object>(
					super.getSpecificProperties(param, method, containingType));
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					ParameterCustomization p = getParameterCustomization(m, param.getName());
					if (p != null) {
						if (p.specificProperties != null) {
							if (p.specificProperties.entrySet().size() > 0) {
								result.putAll(p.specificProperties);
							}
						}
					}
				}
			}
			return result;
		}

		@Override
		protected boolean isNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					ParameterCustomization p = getParameterCustomization(m, param.getName());
					if (p != null) {
						if (p.nullableFacetHidden) {
							return false;
						}
					}
				}
			}
			return param.isNullable();
		}

		@Override
		protected String getCaption(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					ParameterCustomization p = getParameterCustomization(m, param.getName());
					if (p != null) {
						if (p.customParameterCaption != null) {
							return p.customParameterCaption;
						}
					}
				}
			}
			return super.getCaption(param, method, containingType);
		}

		@Override
		protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					if (f.nullableFacetHidden) {
						return false;
					}
				}
			}
			return field.isNullable();
		}

		@Override
		protected boolean isGetOnly(IFieldInfo field, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					if (f.getOnlyForced) {
						return true;
					}
				}
			}
			return super.isGetOnly(field, containingType);
		}

		@Override
		protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					if (f.customFieldCaption != null) {
						return f.customFieldCaption;
					}
				}
			}
			return super.getCaption(field, containingType);
		}

		@Override
		protected boolean isReadOnly(IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					if (m.readOnlyForced) {
						return true;
					}
				}
			}
			return super.isReadOnly(method, containingType);
		}

		@Override
		protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					List<IParameterInfo> result = new ArrayList<IParameterInfo>(
							super.getParameters(method, containingType));
					for (Iterator<IParameterInfo> it = result.iterator(); it.hasNext();) {
						IParameterInfo param = it.next();
						ParameterCustomization p = getParameterCustomization(m, param.getName());
						if ((p != null) && p.hidden) {
							it.remove();
						}
					}

				}
			}
			return super.getParameters(method, containingType);
		}

		@Override
		protected String getCaption(IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					if (m.customMethodCaption != null) {
						return m.customMethodCaption;
					}
				}
			}
			return super.getCaption(method, containingType);
		}

		@Override
		protected List<IFieldInfo> getFields(ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				final List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(containingType));
				for (Iterator<IFieldInfo> it = result.iterator(); it.hasNext();) {
					IFieldInfo field = it.next();
					FieldCustomization f = getFieldCustomization(t, field.getName());
					if (f != null) {
						if (f.hidden) {
							it.remove();
						}
					}
				}
				for (MethodCustomization m : t.methodsCustomizations) {
					if (m.displayedAsField) {
						IMethodInfo method = ReflectionUIUtils.findMethodBySignature(containingType.getMethods(),
								m.methodSignature);
						if (method != null) {
							result.add(new MethodAsField(method));
						}
					}
				}
				for (int i = 0; i < result.size(); i++) {
					IFieldInfo field = result.get(i);
					FieldCustomization f = getFieldCustomization(t, field.getName());
					if (f != null) {
						if (f.displayedAsSingletonList) {
							field = new ValueAsListField(reflectionUI, field);
						}
						if (f.displayedEncapsulated) {
							field = new EncapsulatedValueField(reflectionUI, field);
						}
						result.set(i, field);
					}
				}
				if (t.customFieldsOrder != null) {
					Collections.sort(result, ReflectionUIUtils.getInfosComparator(t.customFieldsOrder, result));
				}
				return result;
			}
			return super.getFields(containingType);
		}

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors(containingType));
				for (Iterator<IMethodInfo> it = result.iterator(); it.hasNext();) {
					IMethodInfo ctor = it.next();
					MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(ctor));
					if (m != null) {
						if (m.hidden) {
							it.remove();
						}
					}
				}
				if (t.customMethodsOrder != null) {
					Collections.sort(result, ReflectionUIUtils.getInfosComparator(t.customMethodsOrder, result));
				}
				return result;
			}
			return super.getConstructors(containingType);
		}

		@Override
		protected List<IMethodInfo> getMethods(ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(containingType));
				for (Iterator<IMethodInfo> it = result.iterator(); it.hasNext();) {
					IMethodInfo method = it.next();
					MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
					if (m != null) {
						if (m.hidden) {
							it.remove();
						}
					}
				}
				for (FieldCustomization f : t.fieldsCustomizations) {
					IFieldInfo field = ReflectionUIUtils.findInfoByName(containingType.getFields(), f.fieldName);
					if (field != null) {
						if (f.displayedAsMethods) {
							result.add(new FieldAsGetter(field));
							if (!field.isGetOnly()) {
								result.add(new FieldAsSetter(field));
							}
						}
					}
				}
				if (t.customMethodsOrder != null) {
					Collections.sort(result, ReflectionUIUtils.getInfosComparator(t.customMethodsOrder, result));
				}
				return result;
			}
			return super.getMethods(containingType);
		}

		@Override
		protected String getCaption(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, type.getName());
			if (t != null) {
				if (t.customTypeCaption != null) {
					return t.customTypeCaption;
				}
			}
			return super.getCaption(type);
		}

		@Override
		protected void validate(ITypeInfo type, Object object) throws Exception {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, type.getName());
			if (t != null) {
				for (MethodCustomization m : t.methodsCustomizations) {
					if (m.validating) {
						IMethodInfo method = ReflectionUIUtils.findMethodBySignature(type.getMethods(),
								m.methodSignature);
						if (method != null) {
							if (method.getParameters().size() > 0) {
								throw new ReflectionUIError("Invalid validating method: Number of parameters > 0: "
										+ ReflectionUIUtils.getMethodSignature(method));
							}
							method.invoke(object, new InvocationData());
						}
					}
				}
			}
			super.validate(type, object);
		}

		@Override
		protected InfoCategory getCategory(IFieldInfo field, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					CustomizationCategory category = f.getCategory();
					List<CustomizationCategory> categories = t.memberCategories;
					int categoryPosition = categories.indexOf(category);
					if (categoryPosition != -1) {
						return new InfoCategory(category.getCaption(), categoryPosition);
					}
				}
			}
			return super.getCategory(field, containingType);
		}

		@Override
		protected InfoCategory getCategory(IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					CustomizationCategory category = m.getCategory();
					List<CustomizationCategory> categories = t.memberCategories;
					int categoryPosition = categories.indexOf(category);
					if (categoryPosition != -1) {
						return new InfoCategory(category.getCaption(), categoryPosition);
					}
				}
			}
			return super.getCategory(method, containingType);
		}

		@Override
		protected String getOnlineHelp(IFieldInfo field, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					if (f.onlineHelp != null) {
						return f.onlineHelp;
					}
				}
			}
			return super.getOnlineHelp(field, containingType);
		}

		@Override
		protected String getOnlineHelp(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					ParameterCustomization p = getParameterCustomization(m, param.getName());
					if (p != null) {
						if (p.onlineHelp != null) {
							return p.onlineHelp;
						}
					}
				}
			}
			return super.getOnlineHelp(param, method, containingType);
		}

		@Override
		protected String getOnlineHelp(ITypeInfo type) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, type.getName());
			if (t != null) {
				if (t.onlineHelp != null) {
					return t.onlineHelp;
				}
			}
			return super.getOnlineHelp(type);
		}

		@Override
		protected String getOnlineHelp(IMethodInfo method, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				MethodCustomization m = getMethodCustomization(t, ReflectionUIUtils.getMethodSignature(method));
				if (m != null) {
					if (m.onlineHelp != null) {
						return m.onlineHelp;
					}
				}
			}
			return super.getOnlineHelp(method, containingType);
		}

		@Override
		protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
			TypeCustomization t = getTypeCustomization(InfoCustomizations.this, containingType.getName());
			if (t != null) {
				FieldCustomization f = getFieldCustomization(t, field.getName());
				if (f != null) {
					if (f.valueOptionsFieldName != null) {
						IFieldInfo valueOptionsfield = ReflectionUIUtils.findInfoByName(containingType.getFields(),
								f.valueOptionsFieldName);
						if (valueOptionsfield == null) {
							throw new ReflectionUIError(
									"Value options field not found: '" + f.valueOptionsFieldName + "'");
						}
						IListTypeInfo valueOptionsfieldType = (IListTypeInfo) valueOptionsfield.getType();
						Object options = valueOptionsfield.getValue(object);
						if (options == null) {
							return null;
						}
						return valueOptionsfieldType.toArray(options);
					}
				}
			}
			return super.getValueOptions(object, field, containingType);
		}

	}

	public static interface ITypeInfoFinder {
		ITypeInfo find(ReflectionUI reflectionUI);
	}

	public static class JavaClassBasedTypeInfoFinder extends AbstractCustomization implements ITypeInfoFinder {

		private static final long serialVersionUID = 1L;
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
			return "JavaClassBasedTypeInfoFinder [className=" + className + "]";
		}

	}

	public static class CustomTypeInfoFinder extends AbstractCustomization implements ITypeInfoFinder {

		private static final long serialVersionUID = 1L;
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
			return "CustomTypeInfoFinder [implementationClassName=" + implementationClassName + "]";
		}

	}

}
