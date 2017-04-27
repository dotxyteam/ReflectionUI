package xy.reflect.ui.info.custom;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.EmbeddedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ResourcePath;
import xy.reflect.ui.util.SystemProperties;

@XmlRootElement
public class InfoCustomizations implements Serializable {

	private static final long serialVersionUID = 1L;

	public static InfoCustomizations defaultInstance;
	public static final String CURRENT_PROXY_SOURCE_PROPERTY_KEY = InfoCustomizations.class.getName()
			+ ".CURRENT_PROXY_SOURCE";

	protected List<TypeCustomization> typeCustomizations = new ArrayList<InfoCustomizations.TypeCustomization>();
	protected List<ListCustomization> listCustomizations = new ArrayList<InfoCustomizations.ListCustomization>();
	protected List<EnumerationCustomization> enumerationCustomizations = new ArrayList<InfoCustomizations.EnumerationCustomization>();
	protected List<MenuItemCategory> menus = new ArrayList<MenuItemCategory>();

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
					} catch (Throwable t) {
						throw new ReflectionUIError(t);
					}
				}
			}
		}
		return defaultInstance;
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

	public List<MenuItemCategory> getMenus() {
		return menus;
	}

	public void setMenus(List<MenuItemCategory> menus) {
		this.menus = menus;
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
		menus = loaded.menus;

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

	public static List<MenuItemCategory> getMenuItemCategoryPath(InfoCustomizations infoCustomizations,
			MenuItemCategory menuItemCategory) {
		for (MenuItemCategory rootMenu : infoCustomizations.getMenus()) {
			List<MenuItemCategory> result = getMenuItemCategoryPath(rootMenu, menuItemCategory);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public static List<MenuItemCategory> getMenuItemCategoryPath(MenuItemCategory from,
			MenuItemCategory menuItemCategory) {
		if (from == menuItemCategory) {
			return Collections.singletonList(from);
		}
		if (from instanceof MenuSpecification) {
			MenuSpecification menu = (MenuSpecification) from;
			for (MenuItemCategory item : menu.getItemCategories()) {
				List<MenuItemCategory> result = getMenuItemCategoryPath(item, menuItemCategory);
				if (result != null) {
					result = new ArrayList<InfoCustomizations.MenuItemCategory>(result);
					result.add(0, from);
					return result;
				}
				;
			}
		}
		return null;
	}

	public static List<MenuItemCategory> getMethodMenuPathOptions(InfoCustomizations infoCustomizations,
			MethodCustomization m) {
		List<MenuItemCategory> result = new ArrayList<InfoCustomizations.MenuItemCategory>();
		for (MenuItemCategory rootMenu : infoCustomizations.getMenus()) {
			result.addAll(getAllMenuItemCategories(rootMenu));
		}
		return result;
	}

	public static List<MenuItemCategory> getAllMenuItemCategories(MenuItemCategory from) {
		List<MenuItemCategory> result = new ArrayList<InfoCustomizations.MenuItemCategory>();
		result.add(from);
		if (from instanceof MenuSpecification) {
			for (MenuItemCategory item : ((MenuSpecification) from).getItemCategories()) {
				result.addAll(getAllMenuItemCategories(item));
			}
		}
		return result;
	}

	public static List<CustomizationCategory> getMemberCategoryOptions(InfoCustomizations infoCustomizations,
			AbstractMemberCustomization m) {
		TypeCustomization tc = findParentTypeCustomization(infoCustomizations, m);
		return tc.getMemberCategories();
	}

	public static TypeCustomization findParentTypeCustomization(InfoCustomizations infoCustomizations,
			AbstractMemberCustomization memberCustumization) {
		for (TypeCustomization tc : getTypeCustomizationsPlusMemberSpecificities(infoCustomizations)) {
			for (FieldCustomization fc : tc.getFieldsCustomizations()) {
				if (fc == memberCustumization) {
					return tc;
				}
			}
			for (MethodCustomization mc : tc.getMethodsCustomizations()) {
				if (mc == memberCustumization) {
					return tc;
				}
			}
		}
		return null;
	}

	public static List<TypeCustomization> getTypeCustomizationsPlusMemberSpecificities(
			InfoCustomizations infoCustomizations) {
		List<TypeCustomization> result = new ArrayList<InfoCustomizations.TypeCustomization>();
		for (TypeCustomization tc : infoCustomizations.getTypeCustomizations()) {
			result.add(tc);
			for (FieldCustomization fc : tc.getFieldsCustomizations()) {
				result.addAll(getTypeCustomizationsPlusMemberSpecificities(fc.getSpecificTypeCustomizations()));
			}
			for (MethodCustomization mc : tc.getMethodsCustomizations()) {
				result.addAll(
						getTypeCustomizationsPlusMemberSpecificities(mc.getSpecificReturnValueTypeCustomizations()));
			}
		}
		return result;
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
		int infoIndex = list.indexOf(info);
		int newInfoIndex = -1;
		int offsetSign = ((offset > 0) ? 1 : -1);
		InfoCategory infoCategory = getCategory(info);
		int currentInfoIndex = infoIndex;
		for (int iOffset = 0; iOffset != offset; iOffset = iOffset + offsetSign) {
			int nextSameCategoryInfoIndex = -1;
			while (true) {
				currentInfoIndex += offsetSign;
				if ((offsetSign == -1) && (currentInfoIndex == -1)) {
					break;
				}
				if ((offsetSign == 1) && (currentInfoIndex == list.size())) {
					break;
				}
				I otherInfo = list.get(currentInfoIndex);
				InfoCategory otherFieldCategory = getCategory(otherInfo);
				if (ReflectionUIUtils.equalsOrBothNull(infoCategory, otherFieldCategory)) {
					nextSameCategoryInfoIndex = currentInfoIndex;
					break;
				}
			}
			if (nextSameCategoryInfoIndex == -1) {
				break;
			} else {
				newInfoIndex = nextSameCategoryInfoIndex;
			}
		}

		if (newInfoIndex == -1) {
			throw new ReflectionUIError("Cannot move item: Limit reached");
		}

		List<I> resultList = new ArrayList<I>(list);
		resultList.remove(info);
		resultList.add(newInfoIndex, info);

		ArrayList<String> newOrder = new ArrayList<String>();
		for (I info2 : resultList) {
			String name = info2.getName();
			if ((name == null) || (name.trim().length() == 0)) {
				throw new ReflectionUIError("Cannot move item: 'getName()' method returned an empty value for item n°"
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

		protected Map<String, Object> specificProperties = new HashMap<String, Object>();

		public Map<String, Object> getSpecificProperties() {
			return specificProperties;
		}

		public void setSpecificProperties(Map<String, Object> specificProperties) {
			this.specificProperties = specificProperties;
		}

	}

	public static class MenuItemCategory extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String name = "";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			MenuItemCategory other = (MenuItemCategory) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "MenuItemCategory [name=" + name + "]";
		}

	}

	public static class MenuSpecification extends MenuItemCategory {
		private static final long serialVersionUID = 1L;

		protected List<MenuItemCategory> itemCategories = new ArrayList<MenuItemCategory>();

		public List<MenuItemCategory> getItemCategories() {
			return itemCategories;
		}

		public void setItemCategories(List<MenuItemCategory> itemCategories) {
			this.itemCategories = itemCategories;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((itemCategories == null) ? 0 : itemCategories.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			MenuSpecification other = (MenuSpecification) obj;
			if (itemCategories == null) {
				if (other.itemCategories != null)
					return false;
			} else if (!itemCategories.equals(other.itemCategories))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "MenuSpecification [name=" + name + ", itemCategories=" + itemCategories + "]";
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
		protected ResourcePath iconImagePath;
		protected ITypeInfo.FieldsLayout fieldsLayout;

		public ITypeInfo.FieldsLayout getFieldsLayout() {
			return fieldsLayout;
		}

		public void setFieldsLayout(ITypeInfo.FieldsLayout fieldsLayout) {
			this.fieldsLayout = fieldsLayout;
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

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

	}

	public static class MethodReturnValueTypeSpecificities extends InfoCustomizations {
		private static final long serialVersionUID = 1L;

	}

	public static class FieldCustomization extends AbstractMemberCustomization
			implements Comparable<FieldCustomization> {
		private static final long serialVersionUID = 1L;

		protected String fieldName;
		protected String customFieldCaption;
		protected boolean nullableFacetHidden = false;
		protected boolean getOnlyForced = false;
		protected String customSetterSignature;
		protected String valueOptionsFieldName;
		protected ValueReturnMode customValueReturnMode;
		protected String nullValueLabel;
		protected String encapsulationFieldName;
		protected boolean getterGenerated;
		protected boolean setterGenerated;
		protected boolean displayedAsSingletonList = false;
		protected boolean nullStatusFieldDisplayed;
		protected FieldTypeSpecificities specificTypeCustomizations = new FieldTypeSpecificities();
		protected boolean formControlEmbeddingForced = false;
		protected boolean formControlCreationForced = false;

		public String getEncapsulationFieldName() {
			return encapsulationFieldName;
		}

		public void setEncapsulationFieldName(String encapsulationFieldName) {
			this.encapsulationFieldName = encapsulationFieldName;
		}

		public boolean isFormControlCreationForced() {
			return formControlCreationForced;
		}

		public void setFormControlCreationForced(boolean formControlCreationForced) {
			this.formControlCreationForced = formControlCreationForced;
		}

		public boolean isFormControlEmbeddingForced() {
			return formControlEmbeddingForced;
		}

		public void setFormControlEmbeddingForced(boolean formControlEmbeddingForced) {
			this.formControlEmbeddingForced = formControlEmbeddingForced;
		}

		public String getCustomSetterSignature() {
			return customSetterSignature;
		}

		public void setCustomSetterSignature(String customSetterSignature) {
			this.customSetterSignature = customSetterSignature;
		}

		public boolean isGetterGenerated() {
			return getterGenerated;
		}

		public void setGetterGenerated(boolean getterGenerated) {
			this.getterGenerated = getterGenerated;
		}

		public boolean isSetterGenerated() {
			return setterGenerated;
		}

		public void setSetterGenerated(boolean setterGenerated) {
			this.setterGenerated = setterGenerated;
		}

		public FieldTypeSpecificities getSpecificTypeCustomizations() {
			return specificTypeCustomizations;
		}

		public void setSpecificTypeCustomizations(FieldTypeSpecificities specificTypeCustomizations) {
			this.specificTypeCustomizations = specificTypeCustomizations;
		}

		public boolean isNullStatusFieldDisplayed() {
			return nullStatusFieldDisplayed;
		}

		public void setNullStatusFieldDisplayed(boolean nullStatusFieldDisplayed) {
			this.nullStatusFieldDisplayed = nullStatusFieldDisplayed;
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
		protected boolean returnValueFieldGenerated;
		protected MethodReturnValueTypeSpecificities specificReturnValueTypeCustomizations = new MethodReturnValueTypeSpecificities();
		protected boolean detachedReturnValueForced;
		protected String encapsulationFieldName;
		protected boolean parametersFormDisplayed;
		protected ResourcePath iconImagePath;
		protected MenuItemCategory menuItemCategory;

		public MenuItemCategory getMenuItemCategory() {
			return menuItemCategory;
		}

		public void setMenuItemCategory(MenuItemCategory menuItemCategory) {
			this.menuItemCategory = menuItemCategory;
		}

		public String getMethodName() {
			return ReflectionUIUtils.extractMethodNameFromSignature(methodSignature);
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

		public String getEncapsulationFieldName() {
			return encapsulationFieldName;
		}

		public void setEncapsulationFieldName(String encapsulationFieldName) {
			this.encapsulationFieldName = encapsulationFieldName;
		}

		public boolean isReturnValueFieldGenerated() {
			return returnValueFieldGenerated;
		}

		public void setReturnValueFieldGenerated(boolean returnValueFieldGenerated) {
			this.returnValueFieldGenerated = returnValueFieldGenerated;
		}

		public boolean isParametersFormDisplayed() {
			return parametersFormDisplayed;
		}

		public void setParametersFormDisplayed(boolean parametersFormDisplayed) {
			this.parametersFormDisplayed = parametersFormDisplayed;
		}

		public boolean isDetachedReturnValueForced() {
			return detachedReturnValueForced;
		}

		public void setDetachedReturnValueForced(boolean detachedReturnValueForced) {
			this.detachedReturnValueForced = detachedReturnValueForced;
		}

		public MethodReturnValueTypeSpecificities getSpecificReturnValueTypeCustomizations() {
			return specificReturnValueTypeCustomizations;
		}

		public void setSpecificReturnValueTypeCustomizations(
				MethodReturnValueTypeSpecificities specificReturnValueTypeCustomizations) {
			this.specificReturnValueTypeCustomizations = specificReturnValueTypeCustomizations;
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
			return ReflectionUIUtils.compareNullables(getMethodName(), o.getMethodName());
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
		protected boolean dynamicEnumerationForced = false;

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

		public boolean isDynamicEnumerationForced() {
			return dynamicEnumerationForced;
		}

		public void setDynamicEnumerationForced(boolean dynamicEnumerationForced) {
			this.dynamicEnumerationForced = dynamicEnumerationForced;
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
		protected boolean itemContructorSelectableforced = false;

		public boolean isItemContructorSelectableforced() {
			return itemContructorSelectableforced;
		}

		public void setItemContructorSelectableforced(boolean itemContructorSelectableforced) {
			this.itemContructorSelectableforced = itemContructorSelectableforced;
		}

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

	public static interface ITypeInfoFinder {
		ITypeInfo find(ReflectionUI reflectionUI);

		String getCriteria();
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
		public String getCriteria() {
			return "className=" + ((className == null) ? "" : className);
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
		public String getCriteria() {
			return "implementationClassName=" + ((implementationClassName == null) ? "" : implementationClassName);
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
