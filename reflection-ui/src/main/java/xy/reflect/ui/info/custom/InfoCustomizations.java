/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.info.custom;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.AbstractMenuItem;
import xy.reflect.ui.info.menu.DefaultMenuElementPosition;
import xy.reflect.ui.info.menu.IMenuElement;
import xy.reflect.ui.info.menu.IMenuItemContainer;
import xy.reflect.ui.info.menu.Menu;
import xy.reflect.ui.info.menu.MenuElementKind;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.CloseWindowMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.HelpMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.OpenMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.RedoMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.ResetMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.SaveAsMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.SaveMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.UndoMenuItem;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.EmbeddedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

/**
 * This class allows to specify declarative customizations of abstract UI model
 * elements.
 * 
 * @author olitank
 *
 */
@XmlRootElement
public class InfoCustomizations implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String CURRENT_CUSTOMIZATIONS_KEY = InfoCustomizations.class.getName() + ".current";
	public static final String UID_FIELD_NAME = "uniqueIdentifier";
	public static final Object INITIAL_STATE_FIELD_NAME = "initial";

	public static final ReflectionUI INTROSPECTOR = new ReflectionUI();

	public static InfoCustomizations defaultInstance;
	protected ApplicationCustomization appplicationCustomization = new ApplicationCustomization();
	protected List<TypeCustomization> typeCustomizations = new ArrayList<InfoCustomizations.TypeCustomization>();
	protected List<ListCustomization> listCustomizations = new ArrayList<InfoCustomizations.ListCustomization>();
	protected List<EnumerationCustomization> enumerationCustomizations = new ArrayList<InfoCustomizations.EnumerationCustomization>();

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "InfoCustomizations.DEFAULT";
		} else {
			return super.toString();
		}
	}

	/**
	 * @return the default instance of this class. Note that it may try to load the
	 *         default customization file according these system properties:
	 *         {@link SystemProperties#DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE} and
	 *         {@link SystemProperties#DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH}
	 */
	public static InfoCustomizations getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new InfoCustomizations();
			if (SystemProperties.areDefaultInfoCustomizationsActive()) {
				String filePath = SystemProperties.getDefaultInfoCustomizationsFilePath();
				File file = new File(filePath);
				if (file.exists()) {
					try {
						defaultInstance.loadFromFile(file, null);
					} catch (Throwable t) {
						throw new ReflectionUIError(t);
					}
				}
			}
		}
		return defaultInstance;
	}

	public ApplicationCustomization getAppplicationCustomization() {
		return appplicationCustomization;
	}

	public void setAppplicationCustomization(ApplicationCustomization appplicationCustomization) {
		this.appplicationCustomization = appplicationCustomization;
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

	public void loadFromFile(File input, Listener<String> debugLogListener) throws IOException {
		FileInputStream stream = new FileInputStream(input);
		try {
			loadFromStream(stream, debugLogListener);
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void loadFromStream(InputStream input, Listener<String> debugLogListener) throws IOException {
		InfoCustomizations loaded;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(InfoCustomizations.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			loaded = (InfoCustomizations) jaxbUnmarshaller.unmarshal(input);
		} catch (Exception e) {
			throw new IOException(e);
		}
		appplicationCustomization = loaded.appplicationCustomization;
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
			for (AbstractMemberCustomization mc : allMembers) {
				if (mc.category != null) {
					for (CustomizationCategory c : t.memberCategories) {
						if (mc.uniqueIdentifier.equals(c.uniqueIdentifier)) {
							mc.category = c;
						}
					}
				}
			}
			for (MethodCustomization mc : t.methodsCustomizations) {
				if (mc.menuLocation != null) {
					for (IMenuItemContainerCustomization container : getAllMenuItemContainerCustomizations(t)) {
						if (((AbstractCustomization) mc.menuLocation).getUniqueIdentifier()
								.equals(((AbstractCustomization) container).getUniqueIdentifier())) {
							mc.menuLocation = container;
						}
					}
				}
			}
		}
	}

	public void saveToFile(File output, Listener<String> debugLogListener) throws IOException {
		saveToFile(output, debugLogListener, null);
	}

	public void saveToFile(File output, Listener<String> debugLogListener, final String comment) throws IOException {
		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		saveToStream(memoryStream, debugLogListener, comment);
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

	public void saveToStream(OutputStream output, Listener<String> debugLogListener) throws IOException {
		saveToStream(output, debugLogListener, null);
	}

	@SuppressWarnings("unchecked")
	public void saveToStream(OutputStream output, Listener<String> debugLogListener, final String comment)
			throws IOException {
		InfoCustomizations toSave = new InfoCustomizations();
		toSave.appplicationCustomization = (ApplicationCustomization) ReflectionUIUtils
				.copyThroughSerialization((Serializable) appplicationCustomization);
		toSave.typeCustomizations = (List<TypeCustomization>) ReflectionUIUtils
				.copyThroughSerialization((Serializable) typeCustomizations);
		toSave.listCustomizations = (List<ListCustomization>) ReflectionUIUtils
				.copyThroughSerialization((Serializable) listCustomizations);
		toSave.enumerationCustomizations = (List<EnumerationCustomization>) ReflectionUIUtils
				.copyThroughSerialization((Serializable) enumerationCustomizations);
		clean(toSave, debugLogListener);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(InfoCustomizations.class);
			javax.xml.bind.Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			XMLStreamWriter jaxbXmlWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(output);
			jaxbXmlWriter.writeStartDocument();
			if (comment != null) {
				jaxbXmlWriter.writeCharacters("\n");
				jaxbXmlWriter.writeComment(comment);
			}
			jaxbXmlWriter.close();

			jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			jaxbMarshaller.marshal(toSave, output);
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

	public static void clean(InfoCustomizations infoCustomizations, Listener<String> debugLogListener) {
		for (TypeCustomization tc : new ArrayList<TypeCustomization>(infoCustomizations.typeCustomizations)) {
			for (FieldCustomization fc : new ArrayList<FieldCustomization>(tc.fieldsCustomizations)) {
				if (fc.isInitial()) {
					tc.fieldsCustomizations.remove(fc);
					continue;
				}
			}
			for (MethodCustomization mc : new ArrayList<MethodCustomization>(tc.methodsCustomizations)) {
				if (mc.isInitial()) {
					tc.methodsCustomizations.remove(mc);
					continue;
				}
			}
			if (tc.isInitial()) {
				if (debugLogListener != null) {
					debugLogListener.handle("Serialization cleanup: Excluding " + tc);
				}
				infoCustomizations.typeCustomizations.remove(tc);
				continue;
			}

		}
		for (ListCustomization lc : new ArrayList<ListCustomization>(infoCustomizations.listCustomizations)) {
			for (ColumnCustomization cc : new ArrayList<ColumnCustomization>(lc.columnCustomizations)) {
				if (cc.isInitial()) {
					lc.columnCustomizations.remove(cc);
					continue;
				}
			}
			if (lc.isInitial()) {
				if (debugLogListener != null) {
					debugLogListener.handle("Serialization cleanup: Excluding " + lc);
				}
				infoCustomizations.listCustomizations.remove(lc);
				continue;
			}

		}
		for (EnumerationCustomization ec : new ArrayList<EnumerationCustomization>(
				infoCustomizations.enumerationCustomizations)) {
			for (EnumerationItemCustomization ic : new ArrayList<EnumerationItemCustomization>(ec.itemCustomizations)) {
				if (ic.isInitial()) {
					ec.itemCustomizations.remove(ic);
					continue;
				}
			}
			if (ec.isInitial()) {
				if (debugLogListener != null) {
					debugLogListener.handle("Serialization cleanup: Excluding " + ec);
				}
				infoCustomizations.enumerationCustomizations.remove(ec);
				continue;
			}
		}
	}

	public static boolean isSimilar(final AbstractCustomization c1, final AbstractCustomization c2,
			final String... excludedFieldNames) {
		return ReflectionUIUtils.equalsAccordingInfos(c1, c2, INTROSPECTOR, new IInfoFilter() {

			@Override
			public boolean excludeMethod(IMethodInfo method) {
				return false;
			}

			@Override
			public boolean excludeField(IFieldInfo field) {
				if (field.getName().equals(InfoCustomizations.UID_FIELD_NAME)) {
					return true;
				}
				if (field.getName().equals(InfoCustomizations.INITIAL_STATE_FIELD_NAME)) {
					return true;
				}
				if (Arrays.asList(excludedFieldNames).contains(field.getName())) {
					return true;
				}
				return false;
			}
		});
	}

	public static MenuElementKind getMenuElementKind(IMenuElementCustomization elementCustomization) {
		return ReflectionUIUtils.getMenuElementKind(elementCustomization.createMenuElement());
	}

	public static DefaultMenuElementPosition getMenuElementPosition(InfoCustomizations infoCustomizations,
			IMenuItemContainerCustomization menuItemContainerCustomization) {
		for (TypeCustomization tc : infoCustomizations.typeCustomizations) {
			DefaultMenuElementPosition result = getMenuElementPosition(tc.menuModelCustomization,
					menuItemContainerCustomization);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public static DefaultMenuElementPosition getMenuElementPosition(MenuModelCustomization menuModelCustomization,
			IMenuItemContainerCustomization menuItemContainerCustomization) {
		for (MenuCustomization menuCustomization : menuModelCustomization.menuCustomizations) {
			DefaultMenuElementPosition result = getMenuElementPosition(menuCustomization,
					menuItemContainerCustomization);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public static DefaultMenuElementPosition getMenuElementPosition(IMenuItemContainerCustomization fromContainer,
			IMenuItemContainerCustomization elementContainer) {
		String elementName = fromContainer.getName();
		MenuElementKind elementKind = getMenuElementKind(fromContainer);
		DefaultMenuElementPosition rootPosition = new DefaultMenuElementPosition(elementName, elementKind, null);
		if (fromContainer == elementContainer) {
			return rootPosition;
		}
		for (AbstractMenuItemCustomization menuItemCustomization : fromContainer.getItemCustomizations()) {
			if (menuItemCustomization instanceof IMenuItemContainerCustomization) {
				DefaultMenuElementPosition result = getMenuElementPosition(
						(IMenuItemContainerCustomization) menuItemCustomization, elementContainer);
				if (result != null) {
					((DefaultMenuElementPosition) result).getRoot().setParent(rootPosition);
					return result;
				}
			}
		}
		if (fromContainer instanceof MenuCustomization) {
			for (MenuItemCategoryCustomization menuItemCategoryCustomization : ((MenuCustomization) fromContainer)
					.getItemCategoryCustomizations()) {
				DefaultMenuElementPosition result = getMenuElementPosition(menuItemCategoryCustomization,
						elementContainer);
				if (result != null) {
					((DefaultMenuElementPosition) result).getRoot().setParent(rootPosition);
					return result;
				}
			}
		}
		return null;
	}

	public static List<IMenuItemContainerCustomization> getAllMenuItemContainerCustomizations(TypeCustomization tc) {
		List<IMenuItemContainerCustomization> result = new ArrayList<IMenuItemContainerCustomization>();
		for (IMenuElementCustomization rootMenuElementCustomization : tc.getMenuModelCustomization()
				.getMenuCustomizations()) {
			if (rootMenuElementCustomization instanceof IMenuItemContainerCustomization) {
				result.addAll(getAllMenuItemContainerCustomizations(
						(IMenuItemContainerCustomization) rootMenuElementCustomization));
			}
		}
		return result;
	}

	public static List<IMenuItemContainerCustomization> getAllMenuItemContainerCustomizations(
			IMenuItemContainerCustomization from) {
		List<IMenuItemContainerCustomization> result = new ArrayList<IMenuItemContainerCustomization>();
		result.add(from);
		for (AbstractMenuItemCustomization item : from.getItemCustomizations()) {
			if (item instanceof IMenuItemContainer) {
				result.addAll(getAllMenuItemContainerCustomizations((IMenuItemContainerCustomization) item));
			}
		}
		if (from instanceof MenuCustomization) {
			for (MenuItemCategoryCustomization item : ((MenuCustomization) from).getItemCategoryCustomizations()) {
				result.addAll(getAllMenuItemContainerCustomizations(item));
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

	public static boolean areInfoCustomizationsCreatedIfNotFound() {
		return SystemProperties.areInfoCustomizationsCreatedIfNotFound();
	}

	public static ParameterCustomization getParameterCustomization(MethodCustomization m, String paramName) {
		return getParameterCustomization(m, paramName, areInfoCustomizationsCreatedIfNotFound());
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
		return getFieldCustomization(t, fieldName, areInfoCustomizationsCreatedIfNotFound());
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
		return getMethodCustomization(t, methodSignature, areInfoCustomizationsCreatedIfNotFound());
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
		return getTypeCustomization(infoCustomizations, typeName, areInfoCustomizationsCreatedIfNotFound());
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
		return getListCustomization(infoCustomizations, listTypeName, itemTypeName,
				areInfoCustomizationsCreatedIfNotFound());
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
		return getColumnCustomization(l, columnName, areInfoCustomizationsCreatedIfNotFound());
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
		return getEnumerationItemCustomization(e, enumItemName, areInfoCustomizationsCreatedIfNotFound());
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
		return getEnumerationCustomization(infoCustomizations, enumTypeName, areInfoCustomizationsCreatedIfNotFound());
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

	public static <I extends IInfo> List<String> getInfosOrderAfterMove(List<I> list, I info, int offset) {
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
				if ((otherInfo instanceof IFieldInfo)) {
					if (((IFieldInfo) otherInfo).isHidden()) {
						continue;
					}
				}
				if ((otherInfo instanceof IMethodInfo)) {
					if (((IMethodInfo) otherInfo).isHidden()) {
						continue;
					}
				}
				if ((otherInfo instanceof IParameterInfo)) {
					if (((IParameterInfo) otherInfo).isHidden()) {
						continue;
					}
				}
				InfoCategory otherInfoCategory = getCategory(otherInfo);
				if (ReflectionUIUtils.equalsOrBothNull(infoCategory, otherInfoCategory)) {
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
			if (name == null) {
				throw new ReflectionUIError("Cannot move item: 'getName()' method returned <null> for item n°"
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

		public boolean isInitial() {
			try {
				return isSimilar(this, getClass().newInstance());
			} catch (Exception e) {
				throw new ReflectionUIError(e);
			}
		}

		protected String uniqueIdentifier = new UID().toString();

		public String getUniqueIdentifier() {
			return uniqueIdentifier;
		}

		public void setUniqueIdentifier(String uniqueIdentifier) {
			this.uniqueIdentifier = uniqueIdentifier;
		}

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

	public static class VirtualFieldDeclaration extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String fieldName;
		protected ITypeInfoFinder fieldTypeFinder;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getFieldTypeFinder() {
			return fieldTypeFinder;
		}

		public void setFieldTypeFinder(ITypeInfoFinder fieldTypeFinder) {
			this.fieldTypeFinder = fieldTypeFinder;
		}

		public void validate() throws Exception {
			if ((fieldName == null) || (fieldName.length() == 0)) {
				throw new IllegalStateException("Field name not provided");
			}
			if (fieldTypeFinder == null) {
				throw new IllegalStateException("Field type not provided");
			}
		}

	}

	public static interface IMenuElementCustomization {

		public String getName();

		public IMenuElement createMenuElement();
	}

	public static interface IMenuItemContainerCustomization extends IMenuElementCustomization {
		public List<AbstractMenuItemCustomization> getItemCustomizations();
	}

	public static class MenuItemCategoryCustomization extends AbstractCustomization
			implements IMenuItemContainerCustomization {
		private static final long serialVersionUID = 1L;

		protected String name;
		protected List<AbstractMenuItemCustomization> itemCustomizations = new ArrayList<AbstractMenuItemCustomization>();

		@Override
		public MenuItemCategory createMenuElement() {
			MenuItemCategory result = new MenuItemCategory();
			result.setName(name);
			for (AbstractMenuItemCustomization menuItemCustomization : itemCustomizations) {
				result.getItems().add(menuItemCustomization.createMenuElement());
			}
			return result;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlElements({ @XmlElement(name = "menu", type = Menu.class),
				@XmlElement(name = "exitMenuItem", type = ExitMenuItemCustomization.class),
				@XmlElement(name = "helpMenuItem", type = HelpMenuItemCustomization.class),
				@XmlElement(name = "undoMenuItem", type = UndoMenuItemCustomization.class),
				@XmlElement(name = "redoMenuItem", type = RedoMenuItemCustomization.class),
				@XmlElement(name = "resetMenuItem", type = ResetMenuItemCustomization.class),
				@XmlElement(name = "openMenuItem", type = OpenMenuItemCustomization.class),
				@XmlElement(name = "saveMenuItem", type = SaveMenuItemCustomization.class),
				@XmlElement(name = "saveAsMenuItem", type = SaveAsMenuItemCustomization.class) })
		public List<AbstractMenuItemCustomization> getItemCustomizations() {
			return itemCustomizations;
		}

		public void setItemCustomizations(List<AbstractMenuItemCustomization> itemCustomizations) {
			this.itemCustomizations = itemCustomizations;
		}

	}

	public static abstract class AbstractMenuItemCustomization extends AbstractCustomization
			implements IMenuElementCustomization {
		private static final long serialVersionUID = 1L;

		protected String name;
		protected ResourcePath iconImagePath;

		@Override
		public abstract AbstractMenuItem createMenuElement();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

	}

	public static abstract class AbstractBuiltInActionMenuItemCustomization extends AbstractMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		public abstract AbstractBuiltInActionMenuItem createMenuElement();

	}

	public static class ExitMenuItemCustomization extends AbstractBuiltInActionMenuItemCustomization {
		private static final long serialVersionUID = 1L;

		@Override
		public AbstractBuiltInActionMenuItem createMenuElement() {
			CloseWindowMenuItem result = new CloseWindowMenuItem();
			result.setName(name);
			result.setIconImagePath(iconImagePath);
			return result;
		}

	}

	public static abstract class AbstractFileMenuItemCustomization extends AbstractBuiltInActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		protected FileBrowserConfiguration fileBrowserConfiguration = new FileBrowserConfiguration();

		public FileBrowserConfiguration getFileBrowserConfiguration() {
			return fileBrowserConfiguration;
		}

		public void setFileBrowserConfiguration(FileBrowserConfiguration fileBrowserConfiguration) {
			this.fileBrowserConfiguration = fileBrowserConfiguration;
		}

	}

	public static abstract class AbstractSaveMenuItemCustomization extends AbstractFileMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

	}

	public static class HelpMenuItemCustomization extends AbstractBuiltInActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		@Override
		public AbstractBuiltInActionMenuItem createMenuElement() {
			HelpMenuItem result = new HelpMenuItem();
			result.setName(name);
			result.setIconImagePath(iconImagePath);
			return result;
		}

	}

	public static class OpenMenuItemCustomization extends AbstractFileMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		@Override
		public AbstractBuiltInActionMenuItem createMenuElement() {
			OpenMenuItem result = new OpenMenuItem();
			result.setName(name);
			result.setIconImagePath(iconImagePath);
			result.setFileBrowserConfiguration(fileBrowserConfiguration);
			return result;
		}
	}

	public static class RedoMenuItemCustomization extends AbstractBuiltInActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		@Override
		public AbstractBuiltInActionMenuItem createMenuElement() {
			RedoMenuItem result = new RedoMenuItem();
			result.setName(name);
			result.setIconImagePath(iconImagePath);
			return result;
		}
	}

	public static class ResetMenuItemCustomization extends AbstractBuiltInActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		@Override
		public AbstractBuiltInActionMenuItem createMenuElement() {
			ResetMenuItem result = new ResetMenuItem();
			result.setName(name);
			result.setIconImagePath(iconImagePath);
			return result;
		}

	}

	public static class SaveAsMenuItemCustomization extends AbstractSaveMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		@Override
		public AbstractBuiltInActionMenuItem createMenuElement() {
			SaveAsMenuItem result = new SaveAsMenuItem();
			result.setName(name);
			result.setIconImagePath(iconImagePath);
			result.setFileBrowserConfiguration(fileBrowserConfiguration);
			return result;
		}

	}

	public static class SaveMenuItemCustomization extends AbstractSaveMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		@Override
		public AbstractBuiltInActionMenuItem createMenuElement() {
			SaveMenuItem result = new SaveMenuItem();
			result.setName(name);
			result.setIconImagePath(iconImagePath);
			result.setFileBrowserConfiguration(fileBrowserConfiguration);
			return result;
		}
	}

	public static class UndoMenuItemCustomization extends AbstractBuiltInActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		@Override
		public AbstractBuiltInActionMenuItem createMenuElement() {
			UndoMenuItem result = new UndoMenuItem();
			result.setName(name);
			result.setIconImagePath(iconImagePath);
			return result;
		}

	}

	public static class MenuCustomization extends AbstractMenuItemCustomization
			implements IMenuItemContainerCustomization {
		private static final long serialVersionUID = 1L;

		protected List<AbstractMenuItemCustomization> itemCustomizations = new ArrayList<AbstractMenuItemCustomization>();
		protected List<MenuItemCategoryCustomization> itemCategoryCustomizations = new ArrayList<MenuItemCategoryCustomization>();

		@Override
		public Menu createMenuElement() {
			Menu result = new Menu();
			result.setName(name);
			for (MenuItemCategoryCustomization menuItemCategoryCustomization : itemCategoryCustomizations) {
				result.getItemCategories().add(menuItemCategoryCustomization.createMenuElement());
			}
			for (AbstractMenuItemCustomization menuItemCustomization : itemCustomizations) {
				result.getItems().add(menuItemCustomization.createMenuElement());
			}
			return result;
		}

		@XmlElements({ @XmlElement(name = "menu", type = Menu.class),
				@XmlElement(name = "exitMenuItem", type = ExitMenuItemCustomization.class),
				@XmlElement(name = "helpMenuItem", type = HelpMenuItemCustomization.class),
				@XmlElement(name = "undoMenuItem", type = UndoMenuItemCustomization.class),
				@XmlElement(name = "redoMenuItem", type = RedoMenuItemCustomization.class),
				@XmlElement(name = "resetMenuItem", type = ResetMenuItemCustomization.class),
				@XmlElement(name = "openMenuItem", type = OpenMenuItemCustomization.class),
				@XmlElement(name = "saveMenuItem", type = SaveMenuItemCustomization.class),
				@XmlElement(name = "saveAsMenuItem", type = SaveAsMenuItemCustomization.class) })
		public List<AbstractMenuItemCustomization> getItemCustomizations() {
			return itemCustomizations;
		}

		public void setItemCustomizations(List<AbstractMenuItemCustomization> itemCustomizations) {
			this.itemCustomizations = itemCustomizations;
		}

		@XmlElement(name = "itemCategories")
		public List<MenuItemCategoryCustomization> getItemCategoryCustomizations() {
			return itemCategoryCustomizations;
		}

		public void setItemCategoryCustomizations(List<MenuItemCategoryCustomization> itemCategoryCustomizations) {
			this.itemCategoryCustomizations = itemCategoryCustomizations;
		}

	}

	public static class MenuModelCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected List<MenuCustomization> menuCustomizations = new ArrayList<MenuCustomization>();

		public MenuModel createMenuModel() {
			MenuModel result = new MenuModel();
			for (MenuCustomization menuCustomization : menuCustomizations) {
				result.getMenus().add(menuCustomization.createMenuElement());
			}
			return result;
		}

		@XmlElement(name = "menus")
		public List<MenuCustomization> getMenuCustomizations() {
			return menuCustomizations;
		}

		public void setMenuCustomizations(List<MenuCustomization> menuCustomizations) {
			this.menuCustomizations = menuCustomizations;
		}

	}

	public enum FormSizeUnit {
		PIXELS, SCREEN_PERCENT
	}

	public static class FormSizeCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected FormSizeUnit unit = FormSizeUnit.SCREEN_PERCENT;
		protected int value = 50;

		public FormSizeUnit getUnit() {
			return unit;
		}

		public void setUnit(FormSizeUnit unit) {
			this.unit = unit;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + value;
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
			FormSizeCustomization other = (FormSizeCustomization) obj;
			if (unit != other.unit)
				return false;
			if (value != other.value)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FormSizeCustomization [unit=" + unit + ", value=" + value + "]";
		}

	}

	public static class ApplicationCustomization extends AbstractInfoCustomization {
		private static final long serialVersionUID = 1L;

		protected String applicationName;
		protected String customApplicationCaption;
		protected String onlineHelp;
		protected ResourcePath mainBackgroundImagePath;
		protected ColorSpecification mainForegroundColor;
		protected ColorSpecification mainBackgroundColor;
		protected ColorSpecification mainBorderColor;
		protected ResourcePath buttonBackgroundImagePath;
		protected ColorSpecification buttonForegroundColor;
		protected ColorSpecification buttonBackgroundColor;
		protected ColorSpecification buttonBorderColor;
		protected ColorSpecification titleForegroundColor;
		protected ColorSpecification titleBackgroundColor;
		protected ResourcePath iconImagePath;
		protected boolean systemIntegrationCrossPlatform;

		public String getApplicationName() {
			return applicationName;
		}

		public void setApplicationName(String applicationName) {
			this.applicationName = applicationName;
		}

		public boolean isSystemIntegrationCrossPlatform() {
			return systemIntegrationCrossPlatform;
		}

		public void setSystemIntegrationCrossPlatform(boolean systemIntegrationCrossPlatform) {
			this.systemIntegrationCrossPlatform = systemIntegrationCrossPlatform;
		}

		public String getCustomApplicationCaption() {
			return customApplicationCaption;
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

		public void setCustomApplicationCaption(String customApplicationCaption) {
			this.customApplicationCaption = customApplicationCaption;
		}

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
		}

		public ResourcePath getMainBackgroundImagePath() {
			return mainBackgroundImagePath;
		}

		public void setMainBackgroundImagePath(ResourcePath mainBackgroundImagePath) {
			this.mainBackgroundImagePath = mainBackgroundImagePath;
		}

		public ColorSpecification getMainForegroundColor() {
			return mainForegroundColor;
		}

		public void setMainForegroundColor(ColorSpecification mainForegroundColor) {
			this.mainForegroundColor = mainForegroundColor;
		}

		public ColorSpecification getMainBackgroundColor() {
			return mainBackgroundColor;
		}

		public void setMainBackgroundColor(ColorSpecification mainBackgroundColor) {
			this.mainBackgroundColor = mainBackgroundColor;
		}

		public ResourcePath getButtonBackgroundImagePath() {
			return buttonBackgroundImagePath;
		}

		public ColorSpecification getMainBorderColor() {
			return mainBorderColor;
		}

		public void setMainBorderColor(ColorSpecification mainBorderColor) {
			this.mainBorderColor = mainBorderColor;
		}

		public void setButtonBackgroundImagePath(ResourcePath buttonBackgroundImagePath) {
			this.buttonBackgroundImagePath = buttonBackgroundImagePath;
		}

		public ColorSpecification getButtonForegroundColor() {
			return buttonForegroundColor;
		}

		public void setButtonForegroundColor(ColorSpecification buttonForegroundColor) {
			this.buttonForegroundColor = buttonForegroundColor;
		}

		public ColorSpecification getButtonBackgroundColor() {
			return buttonBackgroundColor;
		}

		public void setButtonBackgroundColor(ColorSpecification buttonBackgroundColor) {
			this.buttonBackgroundColor = buttonBackgroundColor;
		}

		public ColorSpecification getButtonBorderColor() {
			return buttonBorderColor;
		}

		public void setButtonBorderColor(ColorSpecification buttonBorderColor) {
			this.buttonBorderColor = buttonBorderColor;
		}

		public ColorSpecification getTitleForegroundColor() {
			return titleForegroundColor;
		}

		public void setTitleForegroundColor(ColorSpecification titleForegroundColor) {
			this.titleForegroundColor = titleForegroundColor;
		}

		public ColorSpecification getTitleBackgroundColor() {
			return titleBackgroundColor;
		}

		public void setTitleBackgroundColor(ColorSpecification titleBackgroundColor) {
			this.titleBackgroundColor = titleBackgroundColor;
		}

		@Override
		public String toString() {
			return "ApplicationCustomization []";
		}

	}

	public static class TypeCustomization extends AbstractInfoCustomization implements Comparable<TypeCustomization> {
		private static final long serialVersionUID = 1L;

		protected String typeName;
		protected String customTypeCaption;
		protected String onlineHelp;
		protected List<FieldCustomization> fieldsCustomizations = new ArrayList<InfoCustomizations.FieldCustomization>();
		protected List<MethodCustomization> methodsCustomizations = new ArrayList<InfoCustomizations.MethodCustomization>();
		protected List<String> customFieldsOrder;
		protected List<String> customMethodsOrder;
		protected List<CustomizationCategory> memberCategories = new ArrayList<CustomizationCategory>();
		protected boolean undoManagementHidden = false;
		protected boolean immutableForced = false;
		protected boolean abstractForced = false;
		protected List<ITypeInfoFinder> polymorphicSubTypeFinders = new ArrayList<ITypeInfoFinder>();
		protected ResourcePath iconImagePath;
		protected ITypeInfo.FieldsLayout fieldsLayout;
		protected ITypeInfo.MethodsLayout methodsLayout;
		protected ITypeInfo.CategoriesStyle categoriesStyle;
		protected MenuModelCustomization menuModelCustomization = new MenuModelCustomization();
		protected boolean anyDefaultObjectMemberIncluded = false;
		protected List<VirtualFieldDeclaration> virtualFieldDeclarations = new ArrayList<VirtualFieldDeclaration>();
		protected FormSizeCustomization formWidth;
		protected FormSizeCustomization formHeight;
		protected ResourcePath formBackgroundImagePath;
		protected ColorSpecification formForegroundColor;
		protected ColorSpecification formBackgroundColor;

		@Override
		public boolean isInitial() {
			TypeCustomization defaultTypeCustomization = new TypeCustomization();
			defaultTypeCustomization.typeName = typeName;
			return isSimilar(this, defaultTypeCustomization, "typeName");
		}

		public ITypeInfo.CategoriesStyle getCategoriesStyle() {
			return categoriesStyle;
		}

		public void setCategoriesStyle(ITypeInfo.CategoriesStyle categoriesStyle) {
			this.categoriesStyle = categoriesStyle;
		}

		public ResourcePath getFormBackgroundImagePath() {
			return formBackgroundImagePath;
		}

		public void setFormBackgroundImagePath(ResourcePath formBackgroundImagePath) {
			this.formBackgroundImagePath = formBackgroundImagePath;
		}

		public ColorSpecification getFormBackgroundColor() {
			return formBackgroundColor;
		}

		public void setFormBackgroundColor(ColorSpecification formBackgroundColor) {
			this.formBackgroundColor = formBackgroundColor;
		}

		public ColorSpecification getFormForegroundColor() {
			return formForegroundColor;
		}

		public void setFormForegroundColor(ColorSpecification formForegroundColor) {
			this.formForegroundColor = formForegroundColor;
		}

		public FormSizeCustomization getFormWidth() {
			return formWidth;
		}

		public void setFormWidth(FormSizeCustomization formWidth) {
			this.formWidth = formWidth;
		}

		public FormSizeCustomization getFormHeight() {
			return formHeight;
		}

		public void setFormHeight(FormSizeCustomization formHeight) {
			this.formHeight = formHeight;
		}

		public List<VirtualFieldDeclaration> getVirtualFieldDeclarations() {
			return virtualFieldDeclarations;
		}

		public void setVirtualFieldDeclarations(List<VirtualFieldDeclaration> virtualFieldDeclarations) {
			this.virtualFieldDeclarations = virtualFieldDeclarations;
		}

		public boolean isAnyDefaultObjectMemberIncluded() {
			return anyDefaultObjectMemberIncluded;
		}

		public void setAnyDefaultObjectMemberIncluded(boolean anyDefaultObjectMemberIncluded) {
			this.anyDefaultObjectMemberIncluded = anyDefaultObjectMemberIncluded;
		}

		@XmlElement(name = "menuModel")
		public MenuModelCustomization getMenuModelCustomization() {
			return menuModelCustomization;
		}

		public void setMenuModelCustomization(MenuModelCustomization menuModelCustomization) {
			this.menuModelCustomization = menuModelCustomization;
		}

		public ITypeInfo.FieldsLayout getFieldsLayout() {
			return fieldsLayout;
		}

		public void setFieldsLayout(ITypeInfo.FieldsLayout fieldsLayout) {
			this.fieldsLayout = fieldsLayout;
		}

		public ITypeInfo.MethodsLayout getMethodsLayout() {
			return methodsLayout;
		}

		public void setMethodsLayout(ITypeInfo.MethodsLayout methodsLayout) {
			this.methodsLayout = methodsLayout;
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

		// @XmlIDREF
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

	public static class ConversionMethodFinder extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String conversionClassName;
		protected String conversionMethodSignature;

		public String getConversionClassName() {
			return conversionClassName;
		}

		public void setConversionClassName(String conversionClassName) {
			this.conversionClassName = conversionClassName;
		}

		public String getConversionMethodSignature() {
			return conversionMethodSignature;
		}

		public void setConversionMethodSignature(String conversionMethodSignature) {
			this.conversionMethodSignature = conversionMethodSignature;
		}

		public List<String> getConversionMethodSignatureOptions() {
			Class<?> conversionClass;
			try {
				conversionClass = ClassUtils.getCachedClassforName(conversionClassName);
			} catch (Exception e) {
				return null;
			}
			List<String> result = new ArrayList<String>();
			for (Constructor<?> ctor : conversionClass.getConstructors()) {
				if (ctor.getParameterTypes().length == 1) {
					result.add(ReflectionUIUtils.buildMethodSignature(new DefaultConstructorInfo(INTROSPECTOR, ctor)));
				}
			}
			for (Method method : conversionClass.getMethods()) {
				if (Modifier.isStatic(method.getModifiers())) {
					if (method.getParameterTypes().length == 1) {
						if (!method.getReturnType().equals(void.class)) {
							result.add(ReflectionUIUtils
									.buildMethodSignature(new DefaultMethodInfo(INTROSPECTOR, method)));
						}
					}
				} else {
					if (method.getParameterTypes().length == 0) {
						if (!method.getReturnType().equals(void.class)) {
							result.add(ReflectionUIUtils
									.buildMethodSignature(new DefaultMethodInfo(INTROSPECTOR, method)));
						}
					}
				}
			}
			Collections.sort(result);
			return result;
		}

		public Filter<Object> find() {
			if ((conversionClassName == null) || (conversionClassName.length() == 0)) {
				return null;
			}
			try {
				final Class<?> conversionClass = ClassUtils.getCachedClassforName(conversionClassName);
				if ((conversionMethodSignature == null) || (conversionMethodSignature.length() == 0)) {
					throw new ReflectionUIError("Conversion method not specified!");
				}
				final String conversionMethodName = ReflectionUIUtils
						.extractMethodNameFromSignature(conversionMethodSignature);
				String[] conversionMethodParameterTypeNames = ReflectionUIUtils
						.extractMethodParameterTypeNamesFromSignature(conversionMethodSignature);
				final Class<?>[] conversionMethodParameterTypes = new Class<?>[conversionMethodParameterTypeNames.length];
				for (int i = 0; i < conversionMethodParameterTypeNames.length; i++) {
					conversionMethodParameterTypes[i] = ClassUtils
							.getCachedClassforName(conversionMethodParameterTypeNames[i]);
				}
				if (conversionMethodName == null) {
					return new ConstructorBasedConverter(conversionClass, conversionMethodParameterTypes);
				} else {
					return new MethodBasedConverter(conversionClass, conversionMethodName,
							conversionMethodParameterTypes);
				}
			} catch (Throwable t) {
				throw new ReflectionUIError(t);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((conversionClassName == null) ? 0 : conversionClassName.hashCode());
			result = prime * result + ((conversionMethodSignature == null) ? 0 : conversionMethodSignature.hashCode());
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
			ConversionMethodFinder other = (ConversionMethodFinder) obj;
			if (conversionClassName == null) {
				if (other.conversionClassName != null)
					return false;
			} else if (!conversionClassName.equals(other.conversionClassName))
				return false;
			if (conversionMethodSignature == null) {
				if (other.conversionMethodSignature != null)
					return false;
			} else if (!conversionMethodSignature.equals(other.conversionMethodSignature))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ConversionMethodFinder [conversionClassName=" + conversionClassName + ", conversionMethodSignature="
					+ conversionMethodSignature + "]";
		}

		protected static class MethodBasedConverter implements Filter<Object> {
			private Class<?> theClass;
			private String methodName;
			private Class<?>[] parameterTypes;

			public MethodBasedConverter(Class<?> theClass, String methodName, Class<?>[] parameterTypes) {
				this.theClass = theClass;
				this.methodName = methodName;
				this.parameterTypes = parameterTypes;
			}

			@Override
			public Object get(Object obj) {
				try {
					Method method = theClass.getMethod(methodName, parameterTypes);
					if (Modifier.isStatic(method.getModifiers())) {
						return method.invoke(null, obj);
					} else {
						return method.invoke(obj);
					}
				} catch (InvocationTargetException e) {
					throw new ReflectionUIError(e.getTargetException());
				} catch (Exception e) {
					throw new ReflectionUIError("Failed to convert '" + obj + "' with " + this + ": " + e.toString(),
							e);
				}
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((theClass == null) ? 0 : theClass.hashCode());
				result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
				result = prime * result + Arrays.hashCode(parameterTypes);
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
				MethodBasedConverter other = (MethodBasedConverter) obj;
				if (theClass == null) {
					if (other.theClass != null)
						return false;
				} else if (!theClass.equals(other.theClass))
					return false;
				if (methodName == null) {
					if (other.methodName != null)
						return false;
				} else if (!methodName.equals(other.methodName))
					return false;
				if (!Arrays.equals(parameterTypes, other.parameterTypes))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "MethodBasedConverter [theClass=" + theClass + ", methodName=" + methodName + ", parameterTypes="
						+ Arrays.toString(parameterTypes) + "]";
			}

		}

		protected static class ConstructorBasedConverter implements Filter<Object> {
			private Class<?> theClass;
			private Class<?>[] parameterTypes;

			public ConstructorBasedConverter(Class<?> theClass, Class<?>[] parameterTypes) {
				this.theClass = theClass;
				this.parameterTypes = parameterTypes;
			}

			@Override
			public Object get(Object obj) {
				try {
					return theClass.getDeclaredConstructor(parameterTypes).newInstance(obj);
				} catch (InvocationTargetException e) {
					throw new ReflectionUIError(e.getTargetException());
				} catch (Exception e) {
					throw new ReflectionUIError("Failed to convert '" + obj + "' with " + this + ": " + e.toString(),
							e);
				}
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((theClass == null) ? 0 : theClass.hashCode());
				result = prime * result + Arrays.hashCode(parameterTypes);
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
				ConstructorBasedConverter other = (ConstructorBasedConverter) obj;
				if (theClass == null) {
					if (other.theClass != null)
						return false;
				} else if (!theClass.equals(other.theClass))
					return false;
				if (!Arrays.equals(parameterTypes, other.parameterTypes))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "ConstructorBasedConverter [theClass=" + theClass + ", parameterTypes="
						+ Arrays.toString(parameterTypes) + "]";
			}

		};

	}

	public static class TypeConversion extends Mapping {
		private static final long serialVersionUID = 1L;

		protected ITypeInfoFinder newTypeFinder;
		protected boolean nullValueConverted = false;

		public boolean isNullValueConverted() {
			return nullValueConverted;
		}

		public void setNullValueConverted(boolean nullValueConverted) {
			this.nullValueConverted = nullValueConverted;
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getNewTypeFinder() {
			return newTypeFinder;
		}

		public void setNewTypeFinder(ITypeInfoFinder newTypeFinder) {
			this.newTypeFinder = newTypeFinder;
		}

		public ITypeInfo findNewType(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier) {
			if (newTypeFinder != null) {
				return newTypeFinder.find(reflectionUI, specificitiesIdentifier);
			} else {
				return reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class, specificitiesIdentifier));
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((newTypeFinder == null) ? 0 : newTypeFinder.hashCode());
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
			TypeConversion other = (TypeConversion) obj;
			if (newTypeFinder == null) {
				if (other.newTypeFinder != null)
					return false;
			} else if (!newTypeFinder.equals(other.newTypeFinder))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TypeConversion [newTypeFinder=" + newTypeFinder + ", preMapping=" + preMapping
					+ ", conversionMethodFinder=" + conversionMethodFinder + ", reverseConversionMethodFinder="
					+ reverseConversionMethodFinder + "]";
		}

	}

	public static class Mapping extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected Mapping preMapping;
		protected ConversionMethodFinder conversionMethodFinder;
		protected ConversionMethodFinder reverseConversionMethodFinder;

		public Filter<Object> buildOverallConversionMethod() {
			Filter<Object> result = null;
			if (conversionMethodFinder != null) {
				result = conversionMethodFinder.find();
			}
			if (preMapping != null) {
				Filter<Object> preConversionMethod = preMapping.buildOverallConversionMethod();
				if (preConversionMethod != null) {
					if (result == null) {
						result = preConversionMethod;
					} else {
						result = new Filter.Chain<Object>(preConversionMethod, result);
					}
				}
			}
			return result;
		}

		public Filter<Object> buildOverallReverseConversionMethod() {
			Filter<Object> result = null;
			if (reverseConversionMethodFinder != null) {
				result = reverseConversionMethodFinder.find();
			}
			if (preMapping != null) {
				Filter<Object> preReverseConversionMethod = preMapping.buildOverallReverseConversionMethod();
				if (preReverseConversionMethod != null) {
					if (result == null) {
						result = preReverseConversionMethod;
					} else {
						result = new Filter.Chain<Object>(preReverseConversionMethod, result);
					}
				}
			}
			return result;
		}

		public ConversionMethodFinder getConversionMethodFinder() {
			return conversionMethodFinder;
		}

		public void setConversionMethodFinder(ConversionMethodFinder conversionMethodFinder) {
			this.conversionMethodFinder = conversionMethodFinder;
		}

		public ConversionMethodFinder getReverseConversionMethodFinder() {
			return reverseConversionMethodFinder;
		}

		public void setReverseConversionMethodFinder(ConversionMethodFinder reverseConversionMethodFinder) {
			this.reverseConversionMethodFinder = reverseConversionMethodFinder;
		}

		public Mapping getPreMapping() {
			return preMapping;
		}

		public void setPreMapping(Mapping preMapping) {
			this.preMapping = preMapping;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((conversionMethodFinder == null) ? 0 : conversionMethodFinder.hashCode());
			result = prime * result + ((preMapping == null) ? 0 : preMapping.hashCode());
			result = prime * result
					+ ((reverseConversionMethodFinder == null) ? 0 : reverseConversionMethodFinder.hashCode());
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
			TypeConversion other = (TypeConversion) obj;
			if (conversionMethodFinder == null) {
				if (other.conversionMethodFinder != null)
					return false;
			} else if (!conversionMethodFinder.equals(other.conversionMethodFinder))
				return false;
			if (preMapping == null) {
				if (other.preMapping != null)
					return false;
			} else if (!preMapping.equals(other.preMapping))
				return false;
			if (reverseConversionMethodFinder == null) {
				if (other.reverseConversionMethodFinder != null)
					return false;
			} else if (!reverseConversionMethodFinder.equals(other.reverseConversionMethodFinder))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Mapping [preMapping=" + preMapping + ", conversionMethodFinder=" + conversionMethodFinder
					+ ", reverseConversionMethodFinder=" + reverseConversionMethodFinder + "]";
		}

	}

	public static class FieldCustomization extends AbstractMemberCustomization
			implements Comparable<FieldCustomization> {
		private static final long serialVersionUID = 1L;

		protected String fieldName;
		protected String customFieldCaption;
		protected boolean nullValueDistinctForced = false;
		protected boolean getOnlyForced = false;
		protected String customSetterSignature;
		protected String valueOptionsFieldName;
		protected ValueReturnMode customValueReturnMode;
		protected String nullValueLabel;
		protected String encapsulationFieldName;
		protected boolean getterGenerated;
		protected boolean setterGenerated;
		protected boolean displayedAsSingletonList = false;
		protected boolean nullStatusFieldExported = false;
		protected String importedNullStatusFieldName;
		protected FieldTypeSpecificities specificTypeCustomizations = new FieldTypeSpecificities();
		protected boolean formControlEmbeddingForced = false;
		protected boolean formControlCreationForced = false;
		protected TypeConversion typeConversion;
		protected TextualStorage nullReplacement = new TextualStorage();
		protected boolean duplicateGenerated = false;
		protected Long autoUpdatePeriodMilliseconds;
		protected Double displayAreaHorizontalWeight;
		protected Double displayAreaVerticalWeight;

		@Override
		public boolean isInitial() {
			FieldCustomization defaultFieldCustomization = new FieldCustomization();
			defaultFieldCustomization.fieldName = fieldName;
			return isSimilar(this, defaultFieldCustomization);
		}

		public Double getDisplayAreaHorizontalWeight() {
			return displayAreaHorizontalWeight;
		}

		public void setDisplayAreaHorizontalWeight(Double displayAreaHorizontalWeight) {
			this.displayAreaHorizontalWeight = displayAreaHorizontalWeight;
		}

		public Double getDisplayAreaVerticalWeight() {
			return displayAreaVerticalWeight;
		}

		public void setDisplayAreaVerticalWeight(Double displayAreaVerticalWeight) {
			this.displayAreaVerticalWeight = displayAreaVerticalWeight;
		}

		public Long getAutoUpdatePeriodMilliseconds() {
			return autoUpdatePeriodMilliseconds;
		}

		public void setAutoUpdatePeriodMilliseconds(Long autoUpdatePeriodMilliseconds) {
			this.autoUpdatePeriodMilliseconds = autoUpdatePeriodMilliseconds;
		}

		public boolean isDuplicateGenerated() {
			return duplicateGenerated;
		}

		public void setDuplicateGenerated(boolean duplicateGenerated) {
			this.duplicateGenerated = duplicateGenerated;
		}

		public TextualStorage getNullReplacement() {
			return nullReplacement;
		}

		public void setNullReplacement(TextualStorage nullReplacement) {
			this.nullReplacement = nullReplacement;
		}

		public TypeConversion getTypeConversion() {
			return typeConversion;
		}

		public void setTypeConversion(TypeConversion typeConversion) {
			this.typeConversion = typeConversion;
		}

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

		public boolean isNullStatusFieldExported() {
			return nullStatusFieldExported;
		}

		public void setNullStatusFieldExported(boolean nullStatusFieldExported) {
			this.nullStatusFieldExported = nullStatusFieldExported;
		}

		public String getImportedNullStatusFieldName() {
			return importedNullStatusFieldName;
		}

		public void setImportedNullStatusFieldName(String importedNullStatusFieldName) {
			this.importedNullStatusFieldName = importedNullStatusFieldName;
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

		public boolean isNullValueDistinctForced() {
			return nullValueDistinctForced;
		}

		public void setNullValueDistinctForced(boolean nullValueDistinctForced) {
			this.nullValueDistinctForced = nullValueDistinctForced;
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

	public static class TextualStorage extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String data;
		protected Mapping preConversion;

		public TextualStorage() {
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		public Mapping getPreConversion() {
			return preConversion;
		}

		public void setPreConversion(Mapping preConversion) {
			this.preConversion = preConversion;
		}

		public void save(Object object) {
			if (object == null) {
				this.data = null;
			} else {
				if (preConversion != null) {
					Filter<Object> conversionMethod = preConversion.buildOverallConversionMethod();
					object = conversionMethod.get(object);
				}
				this.data = ReflectionUIUtils.serializeToHexaText(object);
			}
		}

		public Object load() {
			if (data == null) {
				return null;
			} else {
				Object result = ReflectionUIUtils.deserializeFromHexaText(data);
				if (preConversion != null) {
					Filter<Object> reverseConversionMethod = preConversion.buildOverallReverseConversionMethod();
					result = reverseConversionMethod.get(result);
				}
				return result;
			}
		}

	}

	public static class MethodCustomization extends AbstractMemberCustomization
			implements Comparable<MethodCustomization> {
		private static final long serialVersionUID = 1L;

		protected String methodSignature;
		protected String customMethodCaption;
		protected boolean readOnlyForced = false;
		protected boolean validating = false;
		protected boolean runWhenObjectShown = false;
		protected boolean runWhenObjectHidden = false;
		protected List<ParameterCustomization> parametersCustomizations = new ArrayList<InfoCustomizations.ParameterCustomization>();
		protected ValueReturnMode customValueReturnMode;
		protected String nullReturnValueLabel;
		protected boolean returnValueFieldGenerated = false;
		protected MethodReturnValueTypeSpecificities specificReturnValueTypeCustomizations = new MethodReturnValueTypeSpecificities();
		protected boolean detachedReturnValueForced = false;
		protected String encapsulationFieldName;
		protected List<String> parameterizedFieldNames = new ArrayList<String>();
		protected ResourcePath iconImagePath;
		protected IMenuItemContainerCustomization menuLocation;
		protected boolean ignoredReturnValueForced = false;
		protected List<TextualStorage> serializedInvocationDatas = new ArrayList<TextualStorage>();
		protected boolean duplicateGenerated = false;
		protected String confirmationMessage;
		protected String parametersValidationCustomCaption;

		@Override
		public boolean isInitial() {
			MethodCustomization defaultMethodCustomization = new MethodCustomization();
			defaultMethodCustomization.methodSignature = methodSignature;
			return isSimilar(this, defaultMethodCustomization);
		}

		public String getParametersValidationCustomCaption() {
			return parametersValidationCustomCaption;
		}

		public void setParametersValidationCustomCaption(String parametersValidationCustomCaption) {
			this.parametersValidationCustomCaption = parametersValidationCustomCaption;
		}

		public boolean isRunWhenObjectShown() {
			return runWhenObjectShown;
		}

		public void setRunWhenObjectShown(boolean runWhenObjectShown) {
			this.runWhenObjectShown = runWhenObjectShown;
		}

		public boolean isRunWhenObjectHidden() {
			return runWhenObjectHidden;
		}

		public void setRunWhenObjectHidden(boolean runWhenObjectHidden) {
			this.runWhenObjectHidden = runWhenObjectHidden;
		}

		public String getConfirmationMessage() {
			return confirmationMessage;
		}

		public void setConfirmationMessage(String confirmationMessage) {
			this.confirmationMessage = confirmationMessage;
		}

		public boolean isDuplicateGenerated() {
			return duplicateGenerated;
		}

		public void setDuplicateGenerated(boolean duplicateGenerated) {
			this.duplicateGenerated = duplicateGenerated;
		}

		public List<TextualStorage> getSerializedInvocationDatas() {
			return serializedInvocationDatas;
		}

		public void setSerializedInvocationDatas(List<TextualStorage> serializedInvocationDatas) {
			this.serializedInvocationDatas = serializedInvocationDatas;
		}

		public boolean isIgnoredReturnValueForced() {
			return ignoredReturnValueForced;
		}

		public void setIgnoredReturnValueForced(boolean ignoredReturnValueForced) {
			this.ignoredReturnValueForced = ignoredReturnValueForced;
		}

		@XmlElements({ @XmlElement(name = "menu", type = MenuCustomization.class),
				@XmlElement(name = "menuItemCategory", type = MenuItemCategoryCustomization.class) })
		public IMenuItemContainerCustomization getMenuLocation() {
			return menuLocation;
		}

		public void setMenuLocation(IMenuItemContainerCustomization menuLocation) {
			this.menuLocation = menuLocation;
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

		public List<String> getParameterizedFieldNames() {
			return parameterizedFieldNames;
		}

		public void setParameterizedFieldNames(List<String> parameterizedFieldNames) {
			this.parameterizedFieldNames = parameterizedFieldNames;
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
		protected boolean nullValueDistinctForced = false;
		protected String onlineHelp;
		protected boolean displayedAsField;
		protected TextualStorage defaultValue = new TextualStorage();

		public TextualStorage getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(TextualStorage defaultValue) {
			this.defaultValue = defaultValue;
		}

		public boolean isDisplayedAsField() {
			return displayedAsField;
		}

		public void setDisplayedAsField(boolean displayedAsField) {
			this.displayedAsField = displayedAsField;
		}

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

		public boolean isNullValueDistinctForced() {
			return nullValueDistinctForced;
		}

		public void setNullValueDistinctForced(boolean nullValueDistinctForced) {
			this.nullValueDistinctForced = nullValueDistinctForced;
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
		protected ResourcePath iconImagePath;

		@Override
		public boolean isInitial() {
			EnumerationItemCustomization defaultEnumerationItemCustomization = new EnumerationItemCustomization();
			defaultEnumerationItemCustomization.itemName = itemName;
			return isSimilar(this, defaultEnumerationItemCustomization);

		}

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

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
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

		@Override
		public boolean isInitial() {
			EnumerationCustomization defaultEnumerationCustomization = new EnumerationCustomization();
			defaultEnumerationCustomization.enumerationTypeName = enumerationTypeName;
			return isSimilar(this, defaultEnumerationCustomization);
		}

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

	public enum ListLengthUnit {
		PIXELS, SCREEN_PERCENT
	}

	public static class ListLenghtCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected ListLengthUnit unit = ListLengthUnit.SCREEN_PERCENT;
		protected int value = 40;

		public ListLengthUnit getUnit() {
			return unit;
		}

		public void setUnit(ListLengthUnit unit) {
			this.unit = unit;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + value;
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
			ListLenghtCustomization other = (ListLenghtCustomization) obj;
			if (unit != other.unit)
				return false;
			if (value != other.value)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ListLenghtCustomization [unit=" + unit + ", value=" + value + "]";
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
		protected ListLenghtCustomization length = null;

		@Override
		public boolean isInitial() {
			ListCustomization defaultListCustomization = new ListCustomization();
			defaultListCustomization.listTypeName = listTypeName;
			defaultListCustomization.itemTypeName = itemTypeName;
			return isSimilar(this, defaultListCustomization);
		}

		public ListLenghtCustomization getLength() {
			return length;
		}

		public void setLength(ListLenghtCustomization length) {
			this.length = length;
		}

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

		@XmlElement(nillable = true)
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
		protected ITypeInfoFinder customBaseNodeTypeFinder;
		protected List<InfoFilter> excludedSubListFields = new ArrayList<InfoFilter>();

		public boolean isHeterogeneousTree() {
			return heterogeneousTree;
		}

		public void setHeterogeneousTree(boolean heterogeneousTree) {
			this.heterogeneousTree = heterogeneousTree;
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getCustomBaseNodeTypeFinder() {
			return customBaseNodeTypeFinder;
		}

		public void setCustomBaseNodeTypeFinder(ITypeInfoFinder customBaseNodeTypeFinder) {
			this.customBaseNodeTypeFinder = customBaseNodeTypeFinder;
		}

		public List<InfoFilter> getExcludedSubListFields() {
			return excludedSubListFields;
		}

		public void setExcludedSubListFields(List<InfoFilter> excludedSubListFields) {
			this.excludedSubListFields = excludedSubListFields;
		}

	}

	public static class ColumnCustomization extends AbstractCustomization implements Comparable<ColumnCustomization> {

		private static final long serialVersionUID = 1L;
		protected String columnName;
		protected String customCaption;
		protected boolean hidden = false;
		protected Integer minimalCharacterCount;

		@Override
		public boolean isInitial() {
			ColumnCustomization defaultColumnCustomization = new ColumnCustomization();
			defaultColumnCustomization.columnName = columnName;
			return isSimilar(this, defaultColumnCustomization);
		}

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
		ITypeInfo find(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier);

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

		public void validate() throws ClassNotFoundException {
			ClassUtils.getCachedClassforName(className);
		}

		@Override
		public ITypeInfo find(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier) {
			Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassforName(className);
			} catch (ClassNotFoundException e) {
				throw new ReflectionUIError(e);
			}
			return reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaType, specificitiesIdentifier));
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

		public void validate() throws ClassNotFoundException {
			ClassUtils.getCachedClassforName(implementationClassName);
		}

		@Override
		public ITypeInfo find(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier) {
			try {
				Class<?> implementationClass = ClassUtils.getCachedClassforName(implementationClassName);
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
