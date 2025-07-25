
package xy.reflect.ui.info.custom;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.stream.StreamSource;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.MembersCapsuleFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MethodReturnValueAsFieldInfo;
import xy.reflect.ui.info.field.ParameterAsFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.IMenuItemContainerInfo;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.PresetInvocationDataMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo.ItemCreationMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo.ToolsLocation;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.EmbeddedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.util.IDynamicListFeauture.DisplayMode;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.IOUtils;
import xy.reflect.ui.util.KeyboardKey;
import xy.reflect.ui.util.KeyboardShortcut;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.Wildcard;

/**
 * This class allows to specify declarative customizations of abstract UI model
 * elements. Online help is available from the CustomUI sub-project.
 * 
 * @author olitank
 *
 */
@XmlRootElement
public class InfoCustomizations implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String UID_FIELD_NAME = "uniqueIdentifier";
	public static final Object INITIAL_STATE_FIELD_NAME = "initial";

	public static InfoCustomizations defaultInstance;

	protected ApplicationCustomization applicationCustomization = new ApplicationCustomization();
	protected List<TypeCustomization> typeCustomizations = new ArrayList<InfoCustomizations.TypeCustomization>();
	protected List<ListCustomization> listCustomizations = new ArrayList<InfoCustomizations.ListCustomization>();
	protected List<EnumerationCustomization> enumerationCustomizations = new ArrayList<InfoCustomizations.EnumerationCustomization>();
	protected String typeCustomizationsFilterPattern;
	protected String listCustomizationsFilterPattern;
	protected String enumerationCustomizationsFilterPattern;

	protected transient Migrator migrator = new Migrator();

	/**
	 * @return the default instance of this class. Note that it may try to load the
	 *         default customization file according these system properties:
	 *         {@link SystemProperties#DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE} and
	 *         {@link SystemProperties#DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH}
	 */
	public static InfoCustomizations getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new InfoCustomizations() {

				private static final long serialVersionUID = 1L;

				@Override
				public String toString() {
					return "InfoCustomizations.DEFAULT";
				}
			};
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

	/**
	 * The default constructor. Builds an empty instance.
	 */
	public InfoCustomizations() {
	}

	public ApplicationCustomization getApplicationCustomization() {
		return applicationCustomization;
	}

	public void setApplicationCustomization(ApplicationCustomization applicationCustomization) {
		this.applicationCustomization = applicationCustomization;
	}

	@XmlElement(name = "typeCustomization")
	public List<TypeCustomization> getTypeCustomizations() {
		return typeCustomizations;
	}

	public void setTypeCustomizations(List<TypeCustomization> typeCustomizations) {
		this.typeCustomizations = typeCustomizations;
	}

	@XmlElement(name = "listCustomization")
	public List<ListCustomization> getListCustomizations() {
		return listCustomizations;
	}

	public void setListCustomizations(List<ListCustomization> listCustomizations) {
		this.listCustomizations = listCustomizations;
	}

	@XmlElement(name = "enumerationCustomization")
	public List<EnumerationCustomization> getEnumerationCustomizations() {
		return enumerationCustomizations;
	}

	public void setEnumerationCustomizations(List<EnumerationCustomization> enumerationCustomizations) {
		this.enumerationCustomizations = enumerationCustomizations;
	}

	@XmlTransient
	public String getTypeCustomizationsFilterPattern() {
		return typeCustomizationsFilterPattern;
	}

	public void setTypeCustomizationsFilterPattern(String typeCustomizationsFilterPattern) {
		this.typeCustomizationsFilterPattern = typeCustomizationsFilterPattern;
	}

	@XmlTransient
	public String getEnumerationCustomizationsFilterPattern() {
		return enumerationCustomizationsFilterPattern;
	}

	public void setEnumerationCustomizationsFilterPattern(String enumerationCustomizationsFilterPattern) {
		this.enumerationCustomizationsFilterPattern = enumerationCustomizationsFilterPattern;
	}

	@XmlTransient
	public String getListCustomizationsFilterPattern() {
		return listCustomizationsFilterPattern;
	}

	public void setListCustomizationsFilterPattern(String listCustomizationsFilterPattern) {
		this.listCustomizationsFilterPattern = listCustomizationsFilterPattern;
	}

	protected Predicate<TypeCustomization> getTypeCustomizationsFilter() {
		return new Predicate<InfoCustomizations.TypeCustomization>() {
			@Override
			public boolean test(TypeCustomization tc) {
				return (typeCustomizationsFilterPattern == null) || (typeCustomizationsFilterPattern.length() == 0)
						|| Wildcard.match(tc.getTypeName(), "*" + typeCustomizationsFilterPattern + "*");
			}
		};
	}

	@XmlTransient
	public SortedSet<TypeCustomization> getFilteredTypeCustomizations() {
		return new TreeSet<InfoCustomizations.TypeCustomization>(MiscUtils
				.getFilteredSet(new HashSet<TypeCustomization>(typeCustomizations), getTypeCustomizationsFilter()));
	}

	public void setFilteredTypeCustomizations(SortedSet<TypeCustomization> typeCustomizations) {
		this.typeCustomizations = new ArrayList<InfoCustomizations.TypeCustomization>(
				MiscUtils.inferNewNonFilteredSet(new HashSet<TypeCustomization>(this.typeCustomizations),
						getTypeCustomizationsFilter(), typeCustomizations));
	}

	protected Predicate<EnumerationCustomization> getEnumerationCustomizationsFilter() {
		return new Predicate<InfoCustomizations.EnumerationCustomization>() {
			@Override
			public boolean test(EnumerationCustomization ec) {
				return (enumerationCustomizationsFilterPattern == null)
						|| (enumerationCustomizationsFilterPattern.length() == 0) || Wildcard
								.match(ec.getEnumerationTypeName(), "*" + enumerationCustomizationsFilterPattern + "*");
			}
		};
	}

	@XmlTransient
	public SortedSet<EnumerationCustomization> getFilteredEnumerationCustomizations() {
		return new TreeSet<InfoCustomizations.EnumerationCustomization>(
				MiscUtils.getFilteredSet(new HashSet<EnumerationCustomization>(enumerationCustomizations),
						getEnumerationCustomizationsFilter()));
	}

	public void setFilteredEnumerationCustomizations(SortedSet<EnumerationCustomization> enumerationCustomizations) {
		this.enumerationCustomizations = new ArrayList<InfoCustomizations.EnumerationCustomization>(
				MiscUtils.inferNewNonFilteredSet(new HashSet<EnumerationCustomization>(this.enumerationCustomizations),
						getEnumerationCustomizationsFilter(), enumerationCustomizations));
	}

	protected Predicate<ListCustomization> getListCustomizationsFilter() {
		return new Predicate<InfoCustomizations.ListCustomization>() {
			@Override
			public boolean test(ListCustomization lc) {
				return (listCustomizationsFilterPattern == null) || (listCustomizationsFilterPattern.length() == 0)
						|| Wildcard.match(lc.getListTypeName(), "*" + listCustomizationsFilterPattern + "*");
			}
		};
	}

	@XmlTransient
	public SortedSet<ListCustomization> getFilteredListCustomizations() {
		return new TreeSet<InfoCustomizations.ListCustomization>(MiscUtils
				.getFilteredSet(new HashSet<ListCustomization>(listCustomizations), getListCustomizationsFilter()));
	}

	public void setFilteredListCustomizations(SortedSet<ListCustomization> listCustomizations) {
		this.listCustomizations = new ArrayList<InfoCustomizations.ListCustomization>(
				MiscUtils.inferNewNonFilteredSet(new HashSet<ListCustomization>(this.listCustomizations),
						getListCustomizationsFilter(), listCustomizations));
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
			XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
			XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StreamSource(input));
			xmlStreamReader = new StreamReaderDelegate(xmlStreamReader) {
				@Override
				public String getLocalName() {
					String localName = super.getLocalName();
					String migratedLocalName = migrator.migrateXMLLocalName(localName);
					if (migratedLocalName != null) {
						localName = migratedLocalName;
					}
					return localName;
				}
			};
			loaded = (InfoCustomizations) jaxbUnmarshaller.unmarshal(xmlStreamReader);
		} catch (Exception e) {
			throw new IOException(e);
		}
		applicationCustomization = loaded.applicationCustomization;
		typeCustomizations = loaded.typeCustomizations;
		listCustomizations = loaded.listCustomizations;
		enumerationCustomizations = loaded.enumerationCustomizations;

		fillXMLSerializationGap();
		migrator.migrate();
	}

	protected void fillXMLSerializationGap() {
		for (TypeCustomization t : typeCustomizations) {
			for (MethodCustomization mc : t.methodsCustomizations) {
				if (mc.menuLocation != null) {
					for (IMenuItemContainerCustomization container : InfoCustomizations
							.getAllMenuItemContainerCustomizations(t)) {
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
		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		saveToStream(memoryStream, debugLogListener);
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

	@SuppressWarnings("unchecked")
	public void saveToStream(OutputStream output, Listener<String> debugLogListener) throws IOException {
		InfoCustomizations toSave = new InfoCustomizations();
		toSave.applicationCustomization = (ApplicationCustomization) IOUtils
				.copyThroughSerialization((Serializable) applicationCustomization);
		toSave.typeCustomizations = (List<TypeCustomization>) IOUtils
				.copyThroughSerialization((Serializable) typeCustomizations);
		toSave.listCustomizations = (List<ListCustomization>) IOUtils
				.copyThroughSerialization((Serializable) listCustomizations);
		toSave.enumerationCustomizations = (List<EnumerationCustomization>) IOUtils
				.copyThroughSerialization((Serializable) enumerationCustomizations);
		InfoCustomizations.clean(toSave, debugLogListener);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(InfoCustomizations.class);
			javax.xml.bind.Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			jaxbMarshaller.marshal(toSave, output);
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

	public static void clean(InfoCustomizations infoCustomizations, Listener<String> debugLogListener) {
		for (TypeCustomization tc : new ArrayList<TypeCustomization>(infoCustomizations.getTypeCustomizations())) {
			for (FieldCustomization fc : new ArrayList<FieldCustomization>(tc.getFieldsCustomizations())) {
				clean(fc.getSpecificTypeCustomizations(), debugLogListener);
				if (fc.isInitial()) {
					tc.getFieldsCustomizations().remove(fc);
					continue;
				}
			}
			for (MethodCustomization mc : new ArrayList<MethodCustomization>(tc.getMethodsCustomizations())) {
				if (mc.isInitial()) {
					tc.getMethodsCustomizations().remove(mc);
					continue;
				}
			}
			if (tc.isInitial()) {
				if (debugLogListener != null) {
					debugLogListener
							.handle(InfoCustomizations.class.getName() + ": Serialization cleanup: Excluding " + tc);
				}
				infoCustomizations.getTypeCustomizations().remove(tc);
				continue;
			}

		}
		for (ListCustomization lc : new ArrayList<ListCustomization>(infoCustomizations.getListCustomizations())) {
			for (ColumnCustomization cc : new ArrayList<ColumnCustomization>(lc.getColumnCustomizations())) {
				if (cc.isInitial()) {
					lc.getColumnCustomizations().remove(cc);
					continue;
				}
			}
			if (lc.isInitial()) {
				if (debugLogListener != null) {
					debugLogListener
							.handle(InfoCustomizations.class.getName() + ": Serialization cleanup: Excluding " + lc);
				}
				infoCustomizations.getListCustomizations().remove(lc);
				continue;
			}

		}
		for (EnumerationCustomization ec : new ArrayList<EnumerationCustomization>(
				infoCustomizations.getEnumerationCustomizations())) {
			for (EnumerationItemCustomization ic : new ArrayList<EnumerationItemCustomization>(
					ec.getItemCustomizations())) {
				if (ic.isInitial()) {
					ec.getItemCustomizations().remove(ic);
					continue;
				}
			}
			if (ec.isInitial()) {
				if (debugLogListener != null) {
					debugLogListener
							.handle(InfoCustomizations.class.getName() + ": Serialization cleanup: Excluding " + ec);
				}
				infoCustomizations.getEnumerationCustomizations().remove(ec);
				continue;
			}
		}
	}

	public static boolean isSimilar(final AbstractCustomization c1, final AbstractCustomization c2,
			final String... excludedFieldNames) {
		return ReflectionUIUtils.equalsAccordingInfos(c1, c2, ReflectionUI.getDefault(), new IInfoFilter() {

			@Override
			public IMethodInfo apply(IMethodInfo method) {
				return method;
			}

			@Override
			public IFieldInfo apply(IFieldInfo field) {
				if (field.getName().equals(UID_FIELD_NAME)) {
					return null;
				}
				if (field.getName().equals(INITIAL_STATE_FIELD_NAME)) {
					return null;
				}
				if (Arrays.asList(excludedFieldNames).contains(field.getName())) {
					return null;
				}
				return field;
			}
		});
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
			if (item instanceof IMenuItemContainerInfo) {
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

	public static List<String> getMemberCategoryNameOptions(InfoCustomizations infoCustomizations,
			AbstractMemberCustomization m) {
		TypeCustomization tc = findParentTypeCustomization(infoCustomizations, m);
		List<String> result = new ArrayList<String>();
		for (CustomizationCategory c : tc.getMemberCategories()) {
			result.add(c.getName());
		}
		return result;
	}

	public static TypeCustomization findParentTypeCustomization(InfoCustomizations infoCustomizations,
			AbstractMemberCustomization memberCustumization) {
		for (TypeCustomization tc : getTypeCustomizationsPlusFieldSpecificities(infoCustomizations)) {
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

	public static List<TypeCustomization> getTypeCustomizationsPlusFieldSpecificities(
			InfoCustomizations infoCustomizations) {
		List<TypeCustomization> result = new ArrayList<TypeCustomization>();
		for (TypeCustomization tc : infoCustomizations.getTypeCustomizations()) {
			result.add(tc);
			for (FieldCustomization fc : tc.getFieldsCustomizations()) {
				result.addAll(getTypeCustomizationsPlusFieldSpecificities(fc.getSpecificTypeCustomizations()));
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
			for (ParameterCustomization p : m.getParametersCustomizations()) {
				if (paramName.equals(p.getParameterName())) {
					return p;
				}
			}
			if (createIfNotFound) {
				ParameterCustomization p = new ParameterCustomization();
				p.setParameterName(paramName);
				m.getParametersCustomizations().add(p);
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
			for (FieldCustomization f : t.getFieldsCustomizations()) {
				if (fieldName.equals(f.getFieldName())) {
					return f;
				}
			}
			if (createIfNotFound) {
				FieldCustomization f = new FieldCustomization();
				f.setFieldName(fieldName);
				t.getFieldsCustomizations().add(f);
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
			for (MethodCustomization m : t.getMethodsCustomizations()) {
				if (methodSignature.equals(m.getMethodSignature())) {
					return m;
				}
			}
			if (createIfNotFound) {
				MethodCustomization m = new MethodCustomization();
				m.setMethodSignature(methodSignature);
				t.getMethodsCustomizations().add(m);
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
		for (TypeCustomization t : infoCustomizations.getTypeCustomizations()) {
			if (typeName.equals(t.getTypeName())) {
				return t;
			}
		}
		if (createIfNotFound) {
			TypeCustomization t = new TypeCustomization();
			t.setTypeName(typeName);
			infoCustomizations.getTypeCustomizations().add(t);
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
		for (ListCustomization l : infoCustomizations.getListCustomizations()) {
			if (listTypeName.equals(l.getListTypeName())) {
				if (MiscUtils.equalsOrBothNull(l.getItemTypeName(), itemTypeName)) {
					return l;
				}
			}
		}
		if (createIfNotFound) {
			ListCustomization l = new ListCustomization();
			l.setListTypeName(listTypeName);
			l.setItemTypeName(itemTypeName);
			infoCustomizations.getListCustomizations().add(l);
			return l;
		}
		return null;
	}

	public static ColumnCustomization getColumnCustomization(ListCustomization l, String columnName) {
		return getColumnCustomization(l, columnName, areInfoCustomizationsCreatedIfNotFound());
	}

	public static ColumnCustomization getColumnCustomization(ListCustomization l, String columnName,
			boolean createIfNotFound) {
		for (ColumnCustomization c : l.getColumnCustomizations()) {
			if (columnName.equals(c.getColumnName())) {
				return c;
			}
		}
		if (createIfNotFound) {
			ColumnCustomization c = new ColumnCustomization();
			c.setColumnName(columnName);
			l.getColumnCustomizations().add(c);
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
		for (EnumerationItemCustomization i : e.getItemCustomizations()) {
			if (enumItemName.equals(i.getItemName())) {
				return i;
			}
		}
		if (createIfNotFound) {
			EnumerationItemCustomization i = new EnumerationItemCustomization();
			i.setItemName(enumItemName);
			e.getItemCustomizations().add(i);
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
		for (EnumerationCustomization e : infoCustomizations.getEnumerationCustomizations()) {
			if (enumTypeName.equals(e.getEnumerationTypeName())) {
				return e;
			}
		}
		if (createIfNotFound) {
			EnumerationCustomization e = new EnumerationCustomization();
			e.setEnumerationTypeName(enumTypeName);
			infoCustomizations.getEnumerationCustomizations().add(e);
			return e;
		}
		return null;
	}

	public static <I extends IInfo> List<String> getInfosOrderAfterMove(List<I> list, I info, int offset) {
		int infoIndex = list.indexOf(info);
		int newInfoIndex = -1;
		int offsetSign = ((offset > 0) ? 1 : -1);
		InfoCategory infoCategory = ReflectionUIUtils.getCategory(info);
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
				InfoCategory otherInfoCategory = ReflectionUIUtils.getCategory(otherInfo);
				if (MiscUtils.equalsOrBothNull(infoCategory, otherInfoCategory)) {
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
				throw new ReflectionUIError("Cannot move item: 'getName()' method returned <null> for item n�"
						+ (list.indexOf(info2) + 1) + " (caption='" + info2.getCaption() + "')");
			}
			newOrder.add(name);
		}
		return newOrder;
	}

	public static abstract class AbstractCustomization implements Serializable {
		private static final long serialVersionUID = 1L;

		public boolean isInitial() {
			try {
				return InfoCustomizations.isSimilar(this, getClass().newInstance());
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

	public static abstract class AbstractVirtualFieldDeclaration extends AbstractCustomization {

		private static final long serialVersionUID = 1L;

		protected String fieldName;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public void validate() throws Exception {
			if ((fieldName == null) || (fieldName.length() == 0)) {
				throw new IllegalStateException("Field name not provided");
			}
		}

	}

	public static class VirtualFieldDeclaration extends AbstractVirtualFieldDeclaration {
		private static final long serialVersionUID = 1L;

		protected ITypeInfoFinder fieldTypeFinder;

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getFieldTypeFinder() {
			return fieldTypeFinder;
		}

		public void setFieldTypeFinder(ITypeInfoFinder fieldTypeFinder) {
			this.fieldTypeFinder = fieldTypeFinder;
		}

		public void validate() throws Exception {
			super.validate();
			if (fieldTypeFinder == null) {
				throw new IllegalStateException("Field type not provided");
			}
		}

	}

	public static class ImplicitListFieldDeclaration extends AbstractVirtualFieldDeclaration {
		private static final long serialVersionUID = 1L;

		protected ITypeInfoFinder itemTypeFinder;
		protected String createMethodName;
		protected String getMethodName;
		protected String addMethodName;
		protected String removeMethodName;
		protected String sizeFieldName;

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getItemTypeFinder() {
			return itemTypeFinder;
		}

		public void setItemTypeFinder(ITypeInfoFinder itemTypeFinder) {
			this.itemTypeFinder = itemTypeFinder;
		}

		public String getCreateMethodName() {
			return createMethodName;
		}

		public void setCreateMethodName(String createMethodName) {
			this.createMethodName = createMethodName;
		}

		public String getGetMethodName() {
			return getMethodName;
		}

		public void setGetMethodName(String getMethodName) {
			this.getMethodName = getMethodName;
		}

		public String getAddMethodName() {
			return addMethodName;
		}

		public void setAddMethodName(String addMethodName) {
			this.addMethodName = addMethodName;
		}

		public String getRemoveMethodName() {
			return removeMethodName;
		}

		public void setRemoveMethodName(String removeMethodName) {
			this.removeMethodName = removeMethodName;
		}

		public String getSizeFieldName() {
			return sizeFieldName;
		}

		public void setSizeFieldName(String sizeFieldName) {
			this.sizeFieldName = sizeFieldName;
		}

		public void validate() throws Exception {
			super.validate();
			if (itemTypeFinder == null) {
				throw new IllegalStateException("Item type not provided");
			}
			if ((getMethodName == null) || (getMethodName.length() == 0)) {
				throw new IllegalStateException("'Get' method name not provided");
			}
			if ((sizeFieldName == null) || (sizeFieldName.length() == 0)) {
				throw new IllegalStateException("Size field name not provided");
			}
		}

	}

	public static class ImportedFieldDeclaration extends AbstractCustomization {

		private static final long serialVersionUID = 1L;

		protected ITypeInfoFinder sourceTypeFinder;
		protected String sourceFieldName;
		protected String targetFieldName;

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getSourceTypeFinder() {
			return sourceTypeFinder;
		}

		public void setSourceTypeFinder(ITypeInfoFinder sourceTypeFinder) {
			this.sourceTypeFinder = sourceTypeFinder;
		}

		public String getTargetFieldName() {
			return targetFieldName;
		}

		public void setTargetFieldName(String targetFieldName) {
			this.targetFieldName = targetFieldName;
		}

		public String getSourceFieldName() {
			return sourceFieldName;
		}

		public void setSourceFieldName(String sourceFieldName) {
			this.sourceFieldName = sourceFieldName;
		}

		public void validate() throws Exception {
			if (sourceTypeFinder == null) {
				throw new IllegalStateException("Source type not provided");
			}
			if ((sourceFieldName == null) || (sourceFieldName.length() == 0)) {
				throw new IllegalStateException("Source field name not provided");
			}
		}
	}

	public static class ImportedMethodDeclaration extends AbstractCustomization {

		private static final long serialVersionUID = 1L;

		protected ITypeInfoFinder sourceTypeFinder;
		protected String sourceMethodSignature;
		protected String targetMethodName;

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getSourceTypeFinder() {
			return sourceTypeFinder;
		}

		public void setSourceTypeFinder(ITypeInfoFinder sourceTypeFinder) {
			this.sourceTypeFinder = sourceTypeFinder;
		}

		public String getTargetMethodName() {
			return targetMethodName;
		}

		public void setTargetMethodName(String targetMethodName) {
			this.targetMethodName = targetMethodName;
		}

		public String getSourceMethodSignature() {
			return sourceMethodSignature;
		}

		public void setSourceMethodSignature(String sourceMethodSignature) {
			this.sourceMethodSignature = sourceMethodSignature;
		}

		public void validate() throws Exception {
			if (sourceTypeFinder == null) {
				throw new IllegalStateException("Source type not provided");
			}
			if ((sourceMethodSignature == null) || (sourceMethodSignature.length() == 0)) {
				throw new IllegalStateException("Source method signature not provided");
			}
		}
	}

	public static interface IMenuElementCustomization {

		public String getName();

	}

	public static interface IMenuItemContainerCustomization extends IMenuElementCustomization {
		public List<AbstractMenuItemCustomization> getItemCustomizations();
	}

	public static class MenuItemCategoryCustomization extends AbstractCustomization
			implements IMenuItemContainerCustomization {
		private static final long serialVersionUID = 1L;

		protected String name;
		protected List<AbstractMenuItemCustomization> itemCustomizations = new ArrayList<AbstractMenuItemCustomization>();

		public MenuItemCategoryCustomization() {
			name = "Category";
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlElements({ @XmlElement(name = "menu", type = MenuCustomization.class),
				@XmlElement(name = "exitMenuItem", type = ExitMenuItemCustomization.class),
				@XmlElement(name = "helpMenuItem", type = HelpMenuItemCustomization.class),
				@XmlElement(name = "undoMenuItem", type = UndoMenuItemCustomization.class),
				@XmlElement(name = "redoMenuItem", type = RedoMenuItemCustomization.class),
				@XmlElement(name = "resetMenuItem", type = ResetMenuItemCustomization.class),
				@XmlElement(name = "renewMenuItem", type = RenewMenuItemCustomization.class),
				@XmlElement(name = "openMenuItem", type = OpenMenuItemCustomization.class),
				@XmlElement(name = "saveMenuItem", type = SaveMenuItemCustomization.class),
				@XmlElement(name = "saveAsMenuItem", type = SaveAsMenuItemCustomization.class) })
		public List<AbstractMenuItemCustomization> getItemCustomizations() {
			return itemCustomizations;
		}

		public void setItemCustomizations(List<AbstractMenuItemCustomization> itemCustomizations) {
			this.itemCustomizations = itemCustomizations;
		}

		public void validate() {
			if ((name == null) || (name.trim().length() == 0)) {
				throw new ReflectionUIError("Name not provided");
			}
		}

	}

	public static abstract class AbstractMenuItemCustomization extends AbstractCustomization
			implements IMenuElementCustomization {
		private static final long serialVersionUID = 1L;

		protected String name = "";
		protected ResourcePath iconImagePath;

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

		public void validate() {
			if ((name == null) || (name.trim().length() == 0)) {
				throw new ReflectionUIError("Name not provided");
			}
		}

	}

	public static abstract class AbstractStandardActionMenuItemCustomization extends AbstractMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		protected KeyboardShortcut keyboardShortcut;

		public KeyboardShortcut getKeyboardShortcut() {
			return keyboardShortcut;
		}

		public void setKeyboardShortcut(KeyboardShortcut keyboardShortcut) {
			this.keyboardShortcut = keyboardShortcut;
		}
	}

	public static class ExitMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {
		private static final long serialVersionUID = 1L;

		public ExitMenuItemCustomization() {
			name = "Exit";
		}

	}

	public static abstract class AbstractFileMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

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

	public static class HelpMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		public HelpMenuItemCustomization() {
			name = "Help";
		}

	}

	public static class RenewMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		public RenewMenuItemCustomization() {
			name = "New";
		}
	}

	public static class OpenMenuItemCustomization extends AbstractFileMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		public OpenMenuItemCustomization() {
			name = "Open...";
			fileBrowserConfiguration.actionTitle = "Open";
		}
	}

	public static class SaveAsMenuItemCustomization extends AbstractSaveMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		public SaveAsMenuItemCustomization() {
			name = "Save As...";
			fileBrowserConfiguration.actionTitle = "Save";
		}

	}

	public static class SaveMenuItemCustomization extends AbstractSaveMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		public SaveMenuItemCustomization() {
			name = "Save";
			fileBrowserConfiguration.actionTitle = "Save";
			keyboardShortcut = new KeyboardShortcut(KeyEvent.VK_S, false, true, false, false, false);
		}

	}

	public static class RedoMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		public RedoMenuItemCustomization() {
			name = "Redo";
			keyboardShortcut = new KeyboardShortcut(KeyEvent.VK_Y, false, true, false, false, false);
		}

	}

	public static class ResetMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		public ResetMenuItemCustomization() {
			name = "Reset";
		}

	}

	public static class UndoMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		public UndoMenuItemCustomization() {
			name = "Undo";
			keyboardShortcut = new KeyboardShortcut(KeyEvent.VK_Z, false, true, false, false, false);
		}

	}

	public static class MenuCustomization extends AbstractMenuItemCustomization
			implements IMenuItemContainerCustomization {
		private static final long serialVersionUID = 1L;

		protected List<AbstractMenuItemCustomization> itemCustomizations = new ArrayList<AbstractMenuItemCustomization>();
		protected List<MenuItemCategoryCustomization> itemCategoryCustomizations = new ArrayList<MenuItemCategoryCustomization>();
		protected KeyboardKey mnemonicKey;

		public MenuCustomization() {
			name = "Menu";
		}

		@XmlElements({ @XmlElement(name = "menu", type = MenuCustomization.class),
				@XmlElement(name = "exitMenuItem", type = ExitMenuItemCustomization.class),
				@XmlElement(name = "helpMenuItem", type = HelpMenuItemCustomization.class),
				@XmlElement(name = "undoMenuItem", type = UndoMenuItemCustomization.class),
				@XmlElement(name = "redoMenuItem", type = RedoMenuItemCustomization.class),
				@XmlElement(name = "resetMenuItem", type = ResetMenuItemCustomization.class),
				@XmlElement(name = "renewMenuItem", type = RenewMenuItemCustomization.class),
				@XmlElement(name = "openMenuItem", type = OpenMenuItemCustomization.class),
				@XmlElement(name = "saveMenuItem", type = SaveMenuItemCustomization.class),
				@XmlElement(name = "saveAsMenuItem", type = SaveAsMenuItemCustomization.class) })
		public List<AbstractMenuItemCustomization> getItemCustomizations() {
			return itemCustomizations;
		}

		public void setItemCustomizations(List<AbstractMenuItemCustomization> itemCustomizations) {
			this.itemCustomizations = itemCustomizations;
		}

		@XmlElement(name = "itemCategory")
		public List<MenuItemCategoryCustomization> getItemCategoryCustomizations() {
			return itemCategoryCustomizations;
		}

		public void setItemCategoryCustomizations(List<MenuItemCategoryCustomization> itemCategoryCustomizations) {
			this.itemCategoryCustomizations = itemCategoryCustomizations;
		}

		public KeyboardKey getMnemonicKey() {
			return mnemonicKey;
		}

		public void setMnemonicKey(KeyboardKey mnemonicKey) {
			this.mnemonicKey = mnemonicKey;
		}

		public void validate() {
			if ((name == null) || (name.trim().length() == 0)) {
				throw new ReflectionUIError("Name not provided");
			}
		}

	}

	public static class MenuModelCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected List<MenuCustomization> menuCustomizations = new ArrayList<MenuCustomization>();

		@XmlElement(name = "menu")
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

		protected FormSizeUnit unit = FormSizeUnit.PIXELS;
		protected int value = 500;

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
		protected ColorSpecification mainEditorBackgroundColor;
		protected ColorSpecification mainEditorForegroundColor;
		protected ResourcePath mainButtonBackgroundImagePath;
		protected ColorSpecification mainButtonForegroundColor;
		protected ColorSpecification mainButtonBackgroundColor;
		protected ColorSpecification mainButtonBorderColor;
		protected ColorSpecification titleForegroundColor;
		protected ColorSpecification titleBackgroundColor;
		protected ResourcePath iconImagePath;
		protected boolean systemIntegrationCrossPlatform;
		protected ResourcePath titleCustomFontResourcePath;
		protected ResourcePath labelCustomFontResourcePath;
		protected ResourcePath editorCustomFontResourcePath;
		protected ResourcePath buttonCustomFontResourcePath;

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

		public ColorSpecification getMainEditorBackgroundColor() {
			return mainEditorBackgroundColor;
		}

		public void setMainEditorBackgroundColor(ColorSpecification mainEditorBackgroundColor) {
			this.mainEditorBackgroundColor = mainEditorBackgroundColor;
		}

		public ColorSpecification getMainEditorForegroundColor() {
			return mainEditorForegroundColor;
		}

		public void setMainEditorForegroundColor(ColorSpecification mainEditorForegroundColor) {
			this.mainEditorForegroundColor = mainEditorForegroundColor;
		}

		public ColorSpecification getMainBorderColor() {
			return mainBorderColor;
		}

		public void setMainBorderColor(ColorSpecification mainBorderColor) {
			this.mainBorderColor = mainBorderColor;
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

		public ResourcePath getMainButtonBackgroundImagePath() {
			return mainButtonBackgroundImagePath;
		}

		public void setMainButtonBackgroundImagePath(ResourcePath mainButtonBackgroundImagePath) {
			this.mainButtonBackgroundImagePath = mainButtonBackgroundImagePath;
		}

		public ColorSpecification getMainButtonForegroundColor() {
			return mainButtonForegroundColor;
		}

		public void setMainButtonForegroundColor(ColorSpecification mainButtonForegroundColor) {
			this.mainButtonForegroundColor = mainButtonForegroundColor;
		}

		public ColorSpecification getMainButtonBackgroundColor() {
			return mainButtonBackgroundColor;
		}

		public void setMainButtonBackgroundColor(ColorSpecification mainButtonBackgroundColor) {
			this.mainButtonBackgroundColor = mainButtonBackgroundColor;
		}

		public ColorSpecification getMainButtonBorderColor() {
			return mainButtonBorderColor;
		}

		public void setMainButtonBorderColor(ColorSpecification mainButtonBorderColor) {
			this.mainButtonBorderColor = mainButtonBorderColor;
		}

		public ResourcePath getTitleCustomFontResourcePath() {
			return titleCustomFontResourcePath;
		}

		public void setTitleCustomFontResourcePath(ResourcePath titleCustomFontResourcePath) {
			this.titleCustomFontResourcePath = titleCustomFontResourcePath;
		}

		public ResourcePath getLabelCustomFontResourcePath() {
			return labelCustomFontResourcePath;
		}

		public void setLabelCustomFontResourcePath(ResourcePath labelCustomFontResourcePath) {
			this.labelCustomFontResourcePath = labelCustomFontResourcePath;
		}

		public ResourcePath getEditorCustomFontResourcePath() {
			return editorCustomFontResourcePath;
		}

		public void setEditorCustomFontResourcePath(ResourcePath editorCustomFontResourcePath) {
			this.editorCustomFontResourcePath = editorCustomFontResourcePath;
		}

		public ResourcePath getButtonCustomFontResourcePath() {
			return buttonCustomFontResourcePath;
		}

		public void setButtonCustomFontResourcePath(ResourcePath buttonCustomFontResourcePath) {
			this.buttonCustomFontResourcePath = buttonCustomFontResourcePath;
		}

		@Override
		public String toString() {
			return "ApplicationCustomization []";
		}

	}

	public static class TypeCustomization extends AbstractInfoCustomization implements Comparable<TypeCustomization> {
		private static final long serialVersionUID = 1L;

		protected String typeName;
		protected String baseTypeName;
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
		protected boolean anyPersistenceMemberIncluded = false;
		protected List<AbstractVirtualFieldDeclaration> virtualFieldDeclarations = new ArrayList<AbstractVirtualFieldDeclaration>();
		protected FormSizeCustomization formWidth;
		protected FormSizeCustomization formHeight;
		protected ResourcePath formBackgroundImagePath;
		protected ColorSpecification formForegroundColor;
		protected ColorSpecification formBackgroundColor;
		protected ColorSpecification formBorderColor;
		protected ColorSpecification formEditorForegroundColor;
		protected ColorSpecification formEditorBackgroundColor;
		protected ColorSpecification categoriesForegroundColor;
		protected ColorSpecification categoriesBackgroundColor;
		protected ColorSpecification formButtonBackgroundColor;
		protected ColorSpecification formButtonForegroundColor;
		protected ColorSpecification formButtonBorderColor;
		protected ResourcePath formButtonBackgroundImagePath;
		protected String savingMethodName;
		protected String loadingMethodName;
		protected boolean copyForbidden = false;
		protected Integer formSpacing;
		protected boolean validationRequirementForced = false;
		protected List<ImportedFieldDeclaration> importedFieldDeclarations = new ArrayList<ImportedFieldDeclaration>();
		protected List<ImportedMethodDeclaration> importedMethodDeclarations = new ArrayList<ImportedMethodDeclaration>();

		@Override
		public boolean isInitial() {
			TypeCustomization defaultTypeCustomization = new TypeCustomization();
			defaultTypeCustomization.typeName = typeName;
			return InfoCustomizations.isSimilar(this, defaultTypeCustomization, "typeName");
		}

		@XmlElement(name = "importedFieldDeclaration")
		public List<ImportedFieldDeclaration> getImportedFieldDeclarations() {
			return importedFieldDeclarations;
		}

		public void setImportedFieldDeclarations(List<ImportedFieldDeclaration> importedFieldDeclarations) {
			this.importedFieldDeclarations = importedFieldDeclarations;
		}

		@XmlElement(name = "importedMethodDeclaration")
		public List<ImportedMethodDeclaration> getImportedMethodDeclarations() {
			return importedMethodDeclarations;
		}

		public void setImportedMethodDeclarations(List<ImportedMethodDeclaration> importedMethodDeclarations) {
			this.importedMethodDeclarations = importedMethodDeclarations;
		}

		public boolean isValidationRequirementForced() {
			return validationRequirementForced;
		}

		public void setValidationRequirementForced(boolean validationRequirementForced) {
			this.validationRequirementForced = validationRequirementForced;
		}

		public Integer getFormSpacing() {
			return formSpacing;
		}

		public void setFormSpacing(Integer formSpacing) {
			this.formSpacing = formSpacing;
		}

		public String getBaseTypeName() {
			return baseTypeName;
		}

		public void setBaseTypeName(String baseTypeName) {
			this.baseTypeName = baseTypeName;
		}

		public boolean isCopyForbidden() {
			return copyForbidden;
		}

		public void setCopyForbidden(boolean copyForbidden) {
			this.copyForbidden = copyForbidden;
		}

		public String getSavingMethodName() {
			return savingMethodName;
		}

		public void setSavingMethodName(String savingMethodName) {
			this.savingMethodName = savingMethodName;
		}

		public List<String> getSavingMethodNameOptions() {
			Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassForName(typeName);
			} catch (ClassNotFoundException e) {
				return Collections.emptyList();
			}
			List<String> result = new ArrayList<String>();
			for (Method method : javaType.getMethods()) {
				if (Modifier.isStatic(method.getModifiers())) {
					continue;
				}
				if (!method.getReturnType().equals(void.class)) {
					continue;
				}
				List<Parameter> parameters = ClassUtils.getJavaParameters(method);
				if (parameters.size() != 1) {
					continue;
				}
				if (!parameters.get(0).getType().equals(File.class)) {
					continue;
				}
				result.add(method.getName());
			}
			Collections.sort(result);
			return result;
		}

		public String getLoadingMethodName() {
			return loadingMethodName;
		}

		public void setLoadingMethodName(String loadingMethodName) {
			this.loadingMethodName = loadingMethodName;
		}

		public List<String> getLoadingMethodNameOptions() {
			Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassForName(typeName);
			} catch (ClassNotFoundException e) {
				return Collections.emptyList();
			}
			List<String> result = new ArrayList<String>();
			for (Method method : javaType.getMethods()) {
				if (Modifier.isStatic(method.getModifiers())) {
					continue;
				}
				if (!method.getReturnType().equals(void.class)) {
					continue;
				}
				List<Parameter> parameters = ClassUtils.getJavaParameters(method);
				if (parameters.size() != 1) {
					continue;
				}
				if (!parameters.get(0).getType().equals(File.class)) {
					continue;
				}
				result.add(method.getName());
			}
			Collections.sort(result);
			return result;
		}

		public boolean isAnyPersistenceMemberIncluded() {
			return anyPersistenceMemberIncluded;
		}

		public void setAnyPersistenceMemberIncluded(boolean anyPersistenceMemberIncluded) {
			this.anyPersistenceMemberIncluded = anyPersistenceMemberIncluded;
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

		public ColorSpecification getFormBorderColor() {
			return formBorderColor;
		}

		public void setFormBorderColor(ColorSpecification formBorderColor) {
			this.formBorderColor = formBorderColor;
		}

		public ColorSpecification getFormEditorForegroundColor() {
			return formEditorForegroundColor;
		}

		public void setFormEditorForegroundColor(ColorSpecification formEditorForegroundColor) {
			this.formEditorForegroundColor = formEditorForegroundColor;
		}

		public ColorSpecification getFormEditorBackgroundColor() {
			return formEditorBackgroundColor;
		}

		public void setFormEditorBackgroundColor(ColorSpecification formEditorBackgroundColor) {
			this.formEditorBackgroundColor = formEditorBackgroundColor;
		}

		public ColorSpecification getFormButtonBackgroundColor() {
			return formButtonBackgroundColor;
		}

		public void setFormButtonBackgroundColor(ColorSpecification formButtonBackgroundColor) {
			this.formButtonBackgroundColor = formButtonBackgroundColor;
		}

		public ColorSpecification getFormButtonForegroundColor() {
			return formButtonForegroundColor;
		}

		public void setFormButtonForegroundColor(ColorSpecification formButtonForegroundColor) {
			this.formButtonForegroundColor = formButtonForegroundColor;
		}

		public ColorSpecification getFormButtonBorderColor() {
			return formButtonBorderColor;
		}

		public void setFormButtonBorderColor(ColorSpecification formButtonBorderColor) {
			this.formButtonBorderColor = formButtonBorderColor;
		}

		public ResourcePath getFormButtonBackgroundImagePath() {
			return formButtonBackgroundImagePath;
		}

		public void setFormButtonBackgroundImagePath(ResourcePath formButtonBackgroundImagePath) {
			this.formButtonBackgroundImagePath = formButtonBackgroundImagePath;
		}

		public ColorSpecification getCategoriesForegroundColor() {
			return categoriesForegroundColor;
		}

		public void setCategoriesForegroundColor(ColorSpecification categoriesForegroundColor) {
			this.categoriesForegroundColor = categoriesForegroundColor;
		}

		public ColorSpecification getCategoriesBackgroundColor() {
			return categoriesBackgroundColor;
		}

		public void setCategoriesBackgroundColor(ColorSpecification categoriesBackgroundColor) {
			this.categoriesBackgroundColor = categoriesBackgroundColor;
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

		@XmlElements({ @XmlElement(name = "virtualFieldDeclaration", type = VirtualFieldDeclaration.class),
				@XmlElement(name = "implicitListFieldDeclaration", type = ImplicitListFieldDeclaration.class) })
		public List<AbstractVirtualFieldDeclaration> getVirtualFieldDeclarations() {
			return virtualFieldDeclarations;
		}

		public void setVirtualFieldDeclarations(List<AbstractVirtualFieldDeclaration> virtualFieldDeclarations) {
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

		@XmlElement(name = "memberCategory")
		public List<CustomizationCategory> getMemberCategories() {
			return memberCategories;
		}

		public void setMemberCategories(List<CustomizationCategory> memberCategories) {
			this.memberCategories = memberCategories;
		}

		@XmlElement(name = "customFieldsOrderEntry")
		public List<String> getCustomFieldsOrder() {
			return customFieldsOrder;
		}

		public void setCustomFieldsOrder(List<String> customFieldsOrder) {
			this.customFieldsOrder = customFieldsOrder;
		}

		@XmlElement(name = "customMethodsOrderEntry")
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

		@XmlElement(name = "fieldsCustomization")
		public List<FieldCustomization> getFieldsCustomizations() {
			return fieldsCustomizations;
		}

		public void setFieldsCustomizations(List<FieldCustomization> fieldsCustomizations) {
			this.fieldsCustomizations = fieldsCustomizations;
		}

		@XmlElement(name = "methodsCustomization")
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
			return MiscUtils.compareNullables(typeName, o.typeName);
		}

		@Override
		public String toString() {
			return "TypeCustomization [typeName=" + typeName + "]";
		}

	}

	public static class CustomizationCategory extends AbstractCustomization implements Serializable {

		private static final long serialVersionUID = 1L;

		protected String name;
		protected String customCaption;
		protected ResourcePath iconImagePath;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCustomCaption() {
			return customCaption;
		}

		public void setCustomCaption(String customCaption) {
			this.customCaption = customCaption;
		}

		public String getCaption() {
			if (customCaption != null) {
				return customCaption;
			}
			return ReflectionUIUtils.identifierToCaption(name);
		}

		public void setCaption(String caption) {
			if (name == null) {
				this.name = caption;
				return;
			}
			this.customCaption = caption;
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((customCaption == null) ? 0 : customCaption.hashCode());
			result = prime * result + ((iconImagePath == null) ? 0 : iconImagePath.hashCode());
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
			CustomizationCategory other = (CustomizationCategory) obj;
			if (customCaption == null) {
				if (other.customCaption != null)
					return false;
			} else if (!customCaption.equals(other.customCaption))
				return false;
			if (iconImagePath == null) {
				if (other.iconImagePath != null)
					return false;
			} else if (!iconImagePath.equals(other.iconImagePath))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CustomizationCategory [name=" + name + ", customCaption=" + customCaption + ", iconImagePath="
					+ iconImagePath + "]";
		}

	}

	public static abstract class AbstractMemberCustomization extends AbstractInfoCustomization {
		private static final long serialVersionUID = 1L;

		protected String categoryName;
		protected boolean hidden = false;
		protected String onlineHelp;
		protected String relevanceStatusFieldName;

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public String getRelevanceStatusFieldName() {
			return relevanceStatusFieldName;
		}

		public void setRelevanceStatusFieldName(String relevanceStatusFieldName) {
			this.relevanceStatusFieldName = relevanceStatusFieldName;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}

		@Deprecated
		public String getCategoryCaption() {
			return categoryName;
		}

		@Deprecated
		public void setCategoryCaption(String categoryCaption) {
			this.categoryName = categoryCaption;
		}

		@Deprecated
		public CustomizationCategory getCategory() {
			if (categoryName == null) {
				return null;
			} else {
				CustomizationCategory result = new CustomizationCategory();
				result.setCaption(categoryName);
				return result;
			}
		}

		@Deprecated
		public void setCategory(CustomizationCategory category) {
			if (category == null) {
				this.categoryName = null;
			} else {
				this.categoryName = category.getCaption();
			}
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

	public static class ConversionMethodFinder extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected static final String NON_STATIC_METHOD_OWNER_AS_PARAMETER_NAME = "(The method owner)";

		protected String conversionClassName;
		protected String conversionMethodSignature;
		protected int chosenParameterPosition = -1;
		protected Map<Integer, TextualStorage> otherParameterValueStorages = new HashMap<Integer, InfoCustomizations.TextualStorage>();

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

		public int getChosenParameterPosition() {
			return chosenParameterPosition;
		}

		public void setChosenParameterPosition(int chosenParameterPosition) {
			this.chosenParameterPosition = chosenParameterPosition;
		}

		public Map<Integer, TextualStorage> getOtherParameterValueStorages() {
			return otherParameterValueStorages;
		}

		public void setOtherParameterValueStorages(Map<Integer, TextualStorage> otherParameterValueStorages) {
			this.otherParameterValueStorages = otherParameterValueStorages;
		}

		public List<String> getConversionMethodSignatureOptions() {
			Class<?> conversionClass;
			try {
				conversionClass = ClassUtils.getCachedClassForName(conversionClassName);
			} catch (Exception e) {
				return Collections.emptyList();
			}
			List<String> result = new ArrayList<String>();
			for (Constructor<?> ctor : conversionClass.getConstructors()) {
				if (ctor.getParameterTypes().length >= 1) {
					result.add(ReflectionUIUtils.buildMethodSignature(
							new DefaultConstructorInfo(ReflectionUI.getDefault(), ctor, conversionClass)));
				}
			}
			for (Method method : conversionClass.getMethods()) {
				if (Modifier.isStatic(method.getModifiers())) {
					if (method.getParameterTypes().length >= 1) {
						if (!method.getReturnType().equals(void.class)) {
							result.add(ReflectionUIUtils.buildMethodSignature(
									new DefaultMethodInfo(ReflectionUI.getDefault(), method, conversionClass)));
						}
					}
				} else {
					if (method.getParameterTypes().length >= 0) {
						if (!method.getReturnType().equals(void.class)) {
							result.add(ReflectionUIUtils.buildMethodSignature(
									new DefaultMethodInfo(ReflectionUI.getDefault(), method, conversionClass)));
						}
					}
				}
			}
			Collections.sort(result);
			return result;
		}

		public IConverter find() {
			if ((conversionClassName == null) || (conversionClassName.length() == 0)) {
				return null;
			}
			try {
				final Class<?> conversionClass = ClassUtils.getCachedClassForName(conversionClassName);
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
							.getCachedClassForName(conversionMethodParameterTypeNames[i]);
				}
				if (conversionMethodName == null) {
					throw new ReflectionUIError("Malformed method signature: '" + conversionMethodSignature + "'");
				}
				if (conversionMethodName.length() == 0) {
					return new ConstructorBasedConverter(this, conversionClass, conversionMethodParameterTypes);
				} else {
					return new MethodBasedConverter(this, conversionClass, conversionMethodName,
							conversionMethodParameterTypes);
				}
			} catch (Throwable t) {
				throw new ReflectionUIError(t);
			}
		}

		protected Executable getExecutable(Filter<Object> converter) {
			if (converter instanceof ConstructorBasedConverter) {
				return ((ConstructorBasedConverter) converter).getConstructor();
			} else if (converter instanceof MethodBasedConverter) {
				return ((MethodBasedConverter) converter).getMethod();
			} else {
				throw new ReflectionUIError();
			}
		}

		protected String getParameterName(Class<?> conversionClass, Executable executable, int parameterPosition) {
			if (executable instanceof Constructor) {
				DefaultConstructorInfo constructorInfo = new DefaultConstructorInfo(ReflectionUI.getDefault(),
						(Constructor<?>) executable, conversionClass);
				List<IParameterInfo> paramInfos = constructorInfo.getParameters();
				if (parameterPosition >= paramInfos.size()) {
					return null;
				}
				IParameterInfo paramInfo = paramInfos.get(parameterPosition);
				return paramInfo.getName();
			} else if (executable instanceof Method) {
				Method method = (Method) executable;
				DefaultMethodInfo methodInfo = new DefaultMethodInfo(ReflectionUI.getDefault(), method,
						conversionClass);
				List<IParameterInfo> paramInfos = methodInfo.getParameters();
				if (Modifier.isStatic(method.getModifiers())) {
					if (parameterPosition >= paramInfos.size()) {
						return null;
					}
					IParameterInfo paramInfo = paramInfos.get(parameterPosition);
					return paramInfo.getName();
				} else {
					if (parameterPosition == 0) {
						return NON_STATIC_METHOD_OWNER_AS_PARAMETER_NAME;
					}
					if ((parameterPosition - 1) >= paramInfos.size()) {
						return null;
					}
					IParameterInfo paramInfo = paramInfos.get((parameterPosition - 1));
					return paramInfo.getName();
				}
			} else {
				throw new ReflectionUIError();
			}
		}

		protected int getParameterCount(Executable executable) {
			if (executable instanceof Constructor) {
				Constructor<?> constructor = (Constructor<?>) executable;
				return constructor.getParameterTypes().length;
			} else if (executable instanceof Method) {
				Method method = (Method) executable;
				if (Modifier.isStatic(method.getModifiers())) {
					return method.getParameterTypes().length;
				} else {
					return method.getParameterTypes().length + 1;
				}
			} else {
				throw new ReflectionUIError();
			}
		}

		public boolean isParameterPluralityResolvingRequired() {
			Filter<Object> converter;
			try {
				converter = find();
			} catch (Throwable t) {
				return false;
			}
			if (converter == null) {
				return false;
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				return false;
			}
			return getParameterCount(executable) > 1;
		}

		public List<String> getChosenParameterNameOptions() {
			if (!isParameterPluralityResolvingRequired()) {
				return Collections.emptyList();
			}
			IConverter converter;
			try {
				converter = find();
			} catch (Throwable t) {
				return Collections.emptyList();
			}
			if (converter == null) {
				return Collections.emptyList();
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				return Collections.emptyList();
			}
			List<String> result = new ArrayList<String>();
			int paramCount = getParameterCount(executable);
			for (int paramPosition = 0; paramPosition < paramCount; paramPosition++) {
				result.add(getParameterName(converter.getConversionClass(), executable, paramPosition));
			}
			return result;
		}

		@XmlTransient
		public String getChosenParameterName() {
			if (!isParameterPluralityResolvingRequired()) {
				return null;
			}
			if (chosenParameterPosition == -1) {
				return null;
			}
			IConverter converter;
			try {
				converter = find();
			} catch (Throwable t) {
				return null;
			}
			if (converter == null) {
				return null;
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				return null;
			}
			return getParameterName(converter.getConversionClass(), executable, chosenParameterPosition);
		}

		public void setChosenParameterName(String parameterName) {
			if (!isParameterPluralityResolvingRequired()) {
				throw new ReflectionUIError();
			}
			IConverter converter;
			try {
				converter = find();
			} catch (Throwable t) {
				throw new ReflectionUIError(t);
			}
			if (converter == null) {
				throw new ReflectionUIError();
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				throw new ReflectionUIError();
			}
			int paramCount = getParameterCount(executable);
			for (int paramPosition = 0; paramPosition < paramCount; paramPosition++) {
				String currentParameterName = getParameterName(converter.getConversionClass(), executable,
						paramPosition);
				if (parameterName.equals(currentParameterName)) {
					chosenParameterPosition = paramPosition;
					break;
				}
			}
		}

		@XmlTransient
		public Map<String, TextualStorage> getOtherParameterValueStorageByName() {
			if (!isParameterPluralityResolvingRequired()) {
				return null;
			}
			IConverter converter;
			try {
				converter = find();
			} catch (Throwable t) {
				return null;
			}
			if (converter == null) {
				return null;
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				return null;
			}
			Map<String, TextualStorage> result = new LinkedHashMap<String, InfoCustomizations.TextualStorage>();
			int paramCount = getParameterCount(executable);
			for (int paramPosition = 0; paramPosition < paramCount; paramPosition++) {
				if (paramPosition == chosenParameterPosition) {
					continue;
				}
				TextualStorage storage = otherParameterValueStorages.get(paramPosition);
				if (storage == null) {
					storage = new TextualStorage();
				}
				result.put(getParameterName(converter.getConversionClass(), executable, paramPosition), storage);
			}
			return result;
		}

		public void setOtherParameterValueStorageByName(Map<String, TextualStorage> map) {
			if (!isParameterPluralityResolvingRequired()) {
				throw new ReflectionUIError();
			}
			IConverter converter;
			try {
				converter = find();
			} catch (Throwable t) {
				throw new ReflectionUIError(t);
			}
			if (converter == null) {
				throw new ReflectionUIError();
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				throw new ReflectionUIError();
			}
			otherParameterValueStorages.clear();
			int paramCount = getParameterCount(executable);
			for (int paramPosition = 0; paramPosition < paramCount; paramPosition++) {
				String parameterName = getParameterName(converter.getConversionClass(), executable, paramPosition);
				TextualStorage storage = map.get(parameterName);
				if (storage == null) {
					continue;
				}
				otherParameterValueStorages.put(paramPosition, storage);
			}
		}

		public void validate() {
			Filter<Object> converter;
			try {
				converter = find();
			} catch (Throwable t) {
				throw new ReflectionUIError(t);
			}
			if (converter == null) {
				return;
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				throw new ReflectionUIError("Failed to retrieve the conversion method/constructor!");
			}
			if (isParameterPluralityResolvingRequired()) {
				if (getChosenParameterName() == null) {
					throw new ReflectionUIError("Conversion input parameter not selected!");
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + chosenParameterPosition;
			result = prime * result + ((conversionClassName == null) ? 0 : conversionClassName.hashCode());
			result = prime * result + ((conversionMethodSignature == null) ? 0 : conversionMethodSignature.hashCode());
			result = prime * result
					+ ((otherParameterValueStorages == null) ? 0 : otherParameterValueStorages.hashCode());
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
			if (chosenParameterPosition != other.chosenParameterPosition)
				return false;
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
			if (otherParameterValueStorages == null) {
				if (other.otherParameterValueStorages != null)
					return false;
			} else if (!otherParameterValueStorages.equals(other.otherParameterValueStorages))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ConversionMethodFinder [conversionClassName=" + conversionClassName + ", conversionMethodSignature="
					+ conversionMethodSignature + ", chosenParameterPosition=" + chosenParameterPosition
					+ ", otherParameterValueStorages=" + otherParameterValueStorages + "]";
		}

		protected interface IConverter extends Filter<Object> {

			Class<?> getConversionClass();

		}

		protected static class MethodBasedConverter implements IConverter {

			protected Class<?> conversionClass;
			protected Method method;
			protected int parameterCount;
			protected boolean parameterPluralityResolvingRequired;
			protected int chosenParameterPosition;
			protected Map<Integer, TextualStorage> otherParameterValueStorages;

			public MethodBasedConverter(ConversionMethodFinder conversionMethodFinder, Class<?> theClass,
					String methodName, Class<?>[] parameterTypes) {
				this.conversionClass = theClass;
				try {
					this.method = theClass.getMethod(methodName, parameterTypes);
				} catch (NoSuchMethodException e) {
					throw new ReflectionUIError(e);
				} catch (SecurityException e) {
					throw new ReflectionUIError(e);
				}
				this.parameterCount = conversionMethodFinder.getParameterCount(method);
				this.parameterPluralityResolvingRequired = parameterCount > 1;
				this.otherParameterValueStorages = conversionMethodFinder.getOtherParameterValueStorages();
				this.chosenParameterPosition = conversionMethodFinder.getChosenParameterPosition();
			}

			@Override
			public Object get(Object obj) {
				try {
					Method method = getMethod();
					if (parameterPluralityResolvingRequired) {
						Object[] paramValues = new Object[parameterCount];
						for (int paramPosition = 0; paramPosition < paramValues.length; paramPosition++) {
							if (paramPosition == chosenParameterPosition) {
								paramValues[paramPosition] = obj;
							} else {
								TextualStorage storage = otherParameterValueStorages.get(paramPosition);
								if (storage == null) {
									continue;
								}
								paramValues[paramPosition] = storage.load();
							}
						}
						if (Modifier.isStatic(method.getModifiers())) {
							return method.invoke(null, paramValues);
						} else {
							return method.invoke(paramValues[0],
									Arrays.copyOfRange(paramValues, 1, paramValues.length));
						}
					} else {
						if (Modifier.isStatic(method.getModifiers())) {
							return method.invoke(null, obj);
						} else {
							return method.invoke(obj);
						}
					}
				} catch (InvocationTargetException e) {
					throw new ReflectionUIError(e.getTargetException());
				} catch (Exception e) {
					throw new ReflectionUIError("Failed to convert '" + obj + "' with " + this + ": " + e.toString(),
							e);
				}
			}

			@Override
			public Class<?> getConversionClass() {
				return conversionClass;
			}

			public Method getMethod() {
				return method;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + chosenParameterPosition;
				result = prime * result + ((method == null) ? 0 : method.hashCode());
				result = prime * result
						+ ((otherParameterValueStorages == null) ? 0 : otherParameterValueStorages.hashCode());
				result = prime * result + parameterCount;
				result = prime * result + (parameterPluralityResolvingRequired ? 1231 : 1237);
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
				if (chosenParameterPosition != other.chosenParameterPosition)
					return false;
				if (method == null) {
					if (other.method != null)
						return false;
				} else if (!method.equals(other.method))
					return false;
				if (otherParameterValueStorages == null) {
					if (other.otherParameterValueStorages != null)
						return false;
				} else if (!otherParameterValueStorages.equals(other.otherParameterValueStorages))
					return false;
				if (parameterCount != other.parameterCount)
					return false;
				if (parameterPluralityResolvingRequired != other.parameterPluralityResolvingRequired)
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "MethodBasedConverter [method=" + method + ", parameterCount=" + parameterCount
						+ ", parameterPluralityResolvingRequired=" + parameterPluralityResolvingRequired
						+ ", chosenParameterPosition=" + chosenParameterPosition + ", otherParameterValueStorages="
						+ otherParameterValueStorages + "]";
			}

		}

		protected static class ConstructorBasedConverter implements IConverter {

			protected Class<?> conversionClass;
			protected Constructor<?> constrcutor;
			protected int parameterCount;
			protected boolean parameterPluralityResolvingRequired;
			protected int chosenParameterPosition;
			protected Map<Integer, TextualStorage> otherParameterValueStorages;

			public ConstructorBasedConverter(ConversionMethodFinder conversionMethodFinder, Class<?> theClass,
					Class<?>[] parameterTypes) {
				this.conversionClass = theClass;
				try {
					this.constrcutor = theClass.getDeclaredConstructor(parameterTypes);
				} catch (NoSuchMethodException e) {
					throw new ReflectionUIError(e);
				} catch (SecurityException e) {
					throw new ReflectionUIError(e);
				}
				this.parameterCount = conversionMethodFinder.getParameterCount(constrcutor);
				this.parameterPluralityResolvingRequired = parameterCount > 1;
				this.otherParameterValueStorages = conversionMethodFinder.getOtherParameterValueStorages();
				this.chosenParameterPosition = conversionMethodFinder.getChosenParameterPosition();
			}

			@Override
			public Object get(Object obj) {
				try {
					Constructor<?> constructor = getConstructor();
					if (parameterPluralityResolvingRequired) {
						Object[] paramValues = new Object[parameterCount];
						for (int paramPosition = 0; paramPosition < paramValues.length; paramPosition++) {
							if (paramPosition == chosenParameterPosition) {
								paramValues[paramPosition] = obj;
							} else {
								TextualStorage storage = otherParameterValueStorages.get(paramPosition);
								if (storage == null) {
									continue;
								}
								paramValues[paramPosition] = storage.load();
							}
						}
						return constructor.newInstance(paramValues);
					} else {
						return constructor.newInstance(obj);
					}
				} catch (InvocationTargetException e) {
					throw new ReflectionUIError(e.getTargetException());
				} catch (Exception e) {
					throw new ReflectionUIError("Failed to convert '" + obj + "' with " + this + ": " + e.toString(),
							e);
				}
			}

			@Override
			public Class<?> getConversionClass() {
				return conversionClass;
			}

			public Constructor<?> getConstructor() {
				return constrcutor;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + chosenParameterPosition;
				result = prime * result + ((constrcutor == null) ? 0 : constrcutor.hashCode());
				result = prime * result
						+ ((otherParameterValueStorages == null) ? 0 : otherParameterValueStorages.hashCode());
				result = prime * result + parameterCount;
				result = prime * result + (parameterPluralityResolvingRequired ? 1231 : 1237);
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
				if (chosenParameterPosition != other.chosenParameterPosition)
					return false;
				if (constrcutor == null) {
					if (other.constrcutor != null)
						return false;
				} else if (!constrcutor.equals(other.constrcutor))
					return false;
				if (otherParameterValueStorages == null) {
					if (other.otherParameterValueStorages != null)
						return false;
				} else if (!otherParameterValueStorages.equals(other.otherParameterValueStorages))
					return false;
				if (parameterCount != other.parameterCount)
					return false;
				if (parameterPluralityResolvingRequired != other.parameterPluralityResolvingRequired)
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "ConstructorBasedConverter [constrcutor=" + constrcutor + ", parameterCount=" + parameterCount
						+ ", parameterPluralityResolvingRequired=" + parameterPluralityResolvingRequired
						+ ", chosenParameterPosition=" + chosenParameterPosition + ", otherParameterValueStorages="
						+ otherParameterValueStorages + "]";
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

		@XmlTransient
		public List<Mapping> getPreMappings() {
			List<Mapping> result = new ArrayList<InfoCustomizations.Mapping>();
			Mapping currentMapping = this;
			while (currentMapping.getPreMapping() != null) {
				result.add(currentMapping.getPreMapping());
				currentMapping = currentMapping.getPreMapping();
			}
			Collections.reverse(result);
			return result;
		}

		public void setPreMappings(List<Mapping> preMappings) {
			preMappings = new ArrayList<InfoCustomizations.Mapping>(preMappings);
			Collections.reverse(preMappings);
			Mapping currentMapping = this;
			for (Mapping preMapping : preMappings) {
				currentMapping.setPreMapping(preMapping);
				currentMapping = preMapping;
			}
			currentMapping.setPreMapping(null);
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
			result = prime * result + (nullValueConverted ? 1231 : 1237);
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
			if (nullValueConverted != other.nullValueConverted)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TypeConversion [newTypeFinder=" + newTypeFinder + ", nullValueConverted=" + nullValueConverted
					+ "]";
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
				Filter<Object> postReverseConversionMethod = preMapping.buildOverallReverseConversionMethod();
				if (postReverseConversionMethod != null) {
					if (result == null) {
						result = postReverseConversionMethod;
					} else {
						result = new Filter.Chain<Object>(result, postReverseConversionMethod);
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
			Mapping other = (Mapping) obj;
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
		protected boolean transientForced = false;
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
		protected Boolean displayAreaHorizontalFilling;
		protected Boolean displayAreaVerticalFilling;
		protected boolean valueValidityDetectionForced = false;

		@Override
		public boolean isInitial() {
			FieldCustomization defaultFieldCustomization = new FieldCustomization();
			defaultFieldCustomization.fieldName = fieldName;
			return InfoCustomizations.isSimilar(this, defaultFieldCustomization);
		}

		public boolean isValueValidityDetectionForced() {
			return valueValidityDetectionForced;
		}

		public void setValueValidityDetectionForced(boolean valueValidityDetectionForced) {
			this.valueValidityDetectionForced = valueValidityDetectionForced;
		}

		public Boolean getDisplayAreaHorizontalFilling() {
			return displayAreaHorizontalFilling;
		}

		public void setDisplayAreaHorizontalFilling(Boolean displayAreaHorizontalFilling) {
			this.displayAreaHorizontalFilling = displayAreaHorizontalFilling;
		}

		public Boolean getDisplayAreaVerticalFilling() {
			return displayAreaVerticalFilling;
		}

		public void setDisplayAreaVerticalFilling(Boolean displayAreaVerticalFilling) {
			this.displayAreaVerticalFilling = displayAreaVerticalFilling;
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

		public boolean isTransientForced() {
			return transientForced;
		}

		public void setTransientForced(boolean transientForced) {
			this.transientForced = transientForced;
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
			return MiscUtils.compareNullables(fieldName, o.fieldName);
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
				this.data = IOUtils.serializeToHexaText(object);
			}
		}

		public Object load() {
			if (data == null) {
				return null;
			} else {
				Object result = IOUtils.deserializeFromHexaText(data);
				if (preConversion != null) {
					Filter<Object> reverseConversionMethod = preConversion.buildOverallReverseConversionMethod();
					result = reverseConversionMethod.get(result);
				}
				return result;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			result = prime * result + ((preConversion == null) ? 0 : preConversion.hashCode());
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
			TextualStorage other = (TextualStorage) obj;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			if (preConversion == null) {
				if (other.preConversion != null)
					return false;
			} else if (!preConversion.equals(other.preConversion))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TextualStorage [data=" + ((data == null) ? null : MiscUtils.truncateNicely(data, 30))
					+ ", preConversion=" + preConversion + "]";
		}

	}

	public enum TransactionalRole {
		BEGIN, COMMIT, ROLLBACK
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
		protected boolean detachedReturnValueForced = false;
		protected boolean returnValueValidityDetectionForced = false;
		protected String encapsulationFieldName;
		protected List<String> parameterizedFieldNames = new ArrayList<String>();
		protected ResourcePath iconImagePath;
		protected IMenuItemContainerCustomization menuLocation;
		protected boolean ignoredReturnValueForced = false;
		protected List<TextualStorage> serializedInvocationDatas = new ArrayList<TextualStorage>();
		protected boolean duplicateGenerated = false;
		protected String confirmationMessage;
		protected String parametersValidationCustomCaption;
		protected TransactionalRole transactionalRole;
		protected String enablementStatusFieldName;
		protected String executionSuccessMessage;

		@Override
		public boolean isInitial() {
			for (ParameterCustomization pc : parametersCustomizations) {
				if (!pc.isInitial()) {
					return false;
				}
			}
			MethodCustomization defaultMethodCustomization = new MethodCustomization();
			defaultMethodCustomization.methodSignature = methodSignature;
			return InfoCustomizations.isSimilar(this, defaultMethodCustomization, "parametersCustomizations");
		}

		public String getExecutionSuccessMessage() {
			return executionSuccessMessage;
		}

		public void setExecutionSuccessMessage(String executionSuccessMessage) {
			this.executionSuccessMessage = executionSuccessMessage;
		}

		public String getEnablementStatusFieldName() {
			return enablementStatusFieldName;
		}

		public void setEnablementStatusFieldName(String enablementStatusFieldName) {
			this.enablementStatusFieldName = enablementStatusFieldName;
		}

		public TransactionalRole getTransactionalRole() {
			return transactionalRole;
		}

		public void setTransactionalRole(TransactionalRole transactionalRole) {
			this.transactionalRole = transactionalRole;
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

		@XmlElement(name = "serializedInvocationData")
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
			if (methodSignature == null) {
				return null;
			}
			return ReflectionUIUtils.extractMethodNameFromSignature(methodSignature);
		}

		public void setMethodName(String methodName) {
			if (methodSignature == null) {
				return;
			}
			String returnTypeName = ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(methodSignature);
			String[] parameterTypeNames = ReflectionUIUtils
					.extractMethodParameterTypeNamesFromSignature(methodSignature);
			this.methodSignature = ReflectionUIUtils.buildMethodSignature(returnTypeName, methodName,
					Arrays.asList(parameterTypeNames));
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

		@XmlElement(name = "parameterizedFieldName")
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

		public boolean isReturnValueValidityDetectionForced() {
			return returnValueValidityDetectionForced;
		}

		public void setReturnValueValidityDetectionForced(boolean returnValueValidityDetectionForced) {
			this.returnValueValidityDetectionForced = returnValueValidityDetectionForced;
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

		@XmlElement(name = "parametersCustomization")
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
			return MiscUtils.compareNullables(getMethodName(), o.getMethodName());
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

		@Override
		public boolean isInitial() {
			ParameterCustomization defaultParameterCustomization = new ParameterCustomization();
			defaultParameterCustomization.parameterName = parameterName;
			return InfoCustomizations.isSimilar(this, defaultParameterCustomization);
		}

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
			return MiscUtils.compareNullables(parameterName, o.parameterName);
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
		protected DisplayMode displayMode;

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

		public DisplayMode getDisplayMode() {
			return displayMode;
		}

		public void setDisplayMode(DisplayMode displayMode) {
			this.displayMode = displayMode;
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
		protected ResourcePath customIconImagePath;
		protected DisplayMode displayMode;

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

		public ResourcePath getCustomIconImagePath() {
			return customIconImagePath;
		}

		public void setCustomIconImagePath(ResourcePath customIconImagePath) {
			this.customIconImagePath = customIconImagePath;
		}

		public DisplayMode getDisplayMode() {
			return displayMode;
		}

		public void setDisplayMode(DisplayMode displayMode) {
			this.displayMode = displayMode;
		}

		public void validate() {
			if ((methodSignature != null) && (methodSignature.length() > 0)) {
				if (ReflectionUIUtils.extractMethodNameFromSignature(methodSignature) == null) {
					throw new ReflectionUIError("Malformed method signature: '" + methodSignature + "'");
				}
			}
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

	public static class InfoPattern extends AbstractCustomization {
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
			InfoPattern other = (InfoPattern) obj;
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
			return "InfoPattern [value=" + value + ", regularExpression=" + regularExpression + "]";
		}

	}

	public static class ListInstantiationOption extends AbstractCustomization {
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
		protected ListInstantiationOption listInstantiationOption;

		public ListInstantiationOption getListInstantiationOption() {
			return listInstantiationOption;
		}

		public void setListInstantiationOption(ListInstantiationOption listInstantiationOption) {
			this.listInstantiationOption = listInstantiationOption;
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

	public static class EnumerationItemCustomization extends AbstractInfoCustomization
			implements Comparable<EnumerationItemCustomization> {

		private static final long serialVersionUID = 1L;

		protected String itemName;
		protected String customCaption;
		protected boolean hidden;
		protected ResourcePath iconImagePath;
		protected String onlineHelp;

		@Override
		public boolean isInitial() {
			EnumerationItemCustomization defaultEnumerationItemCustomization = new EnumerationItemCustomization();
			defaultEnumerationItemCustomization.itemName = itemName;
			return InfoCustomizations.isSimilar(this, defaultEnumerationItemCustomization);

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

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		@Override
		public int compareTo(EnumerationItemCustomization o) {
			int result = MiscUtils.compareNullables(itemName, o.itemName);
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
		protected List<String> itemsCustomOrder;

		@Override
		public boolean isInitial() {
			EnumerationCustomization defaultEnumerationCustomization = new EnumerationCustomization();
			defaultEnumerationCustomization.enumerationTypeName = enumerationTypeName;
			return InfoCustomizations.isSimilar(this, defaultEnumerationCustomization);
		}

		public String getEnumerationTypeName() {
			return enumerationTypeName;
		}

		public void setEnumerationTypeName(String enumerationTypeName) {
			this.enumerationTypeName = enumerationTypeName;
		}

		@XmlElement(name = "itemCustomization")
		public List<EnumerationItemCustomization> getItemCustomizations() {
			return itemCustomizations;
		}

		public void setItemCustomizations(List<EnumerationItemCustomization> itemCustomizations) {
			this.itemCustomizations = itemCustomizations;
		}

		@XmlElement(name = "itemsCustomOrderEntry")
		public List<String> getItemsCustomOrder() {
			return itemsCustomOrder;
		}

		public void setItemsCustomOrder(List<String> itemsCustomOrder) {
			this.itemsCustomOrder = itemsCustomOrder;
		}

		public boolean isDynamicEnumerationForced() {
			return dynamicEnumerationForced;
		}

		public void setDynamicEnumerationForced(boolean dynamicEnumerationForced) {
			this.dynamicEnumerationForced = dynamicEnumerationForced;
		}

		@Override
		public int compareTo(EnumerationCustomization o) {
			int result = MiscUtils.compareNullables(enumerationTypeName, o.enumerationTypeName);
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

	public enum ControlSizeUnit {
		PIXELS, SCREEN_PERCENT
	}

	public static class ControlSizeCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected ControlSizeUnit unit = ControlSizeUnit.PIXELS;
		protected int value = 400;

		public ControlSizeUnit getUnit() {
			return unit;
		}

		public void setUnit(ControlSizeUnit unit) {
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
			ControlSizeCustomization other = (ControlSizeCustomization) obj;
			if (unit != other.unit)
				return false;
			if (value != other.value)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ControlSizeCustomization [unit=" + unit + ", value=" + value + "]";
		}

	}

	public static class ListCustomization extends AbstractCustomization implements Comparable<ListCustomization> {
		private static final long serialVersionUID = 1L;

		protected String listTypeName;
		protected String itemTypeName;
		protected boolean itemTypeColumnAdded = false;
		protected boolean positionColumnAdded = false;
		protected boolean fieldColumnsAdded = false;
		protected boolean stringValueColumnAdded = false;
		protected List<ColumnCustomization> columnCustomizations = new ArrayList<ColumnCustomization>();
		protected List<String> columnsCustomOrder;
		protected TreeStructureDiscoverySettings treeStructureDiscoverySettings;
		protected List<ListItemFieldShortcut> allowedItemFieldShortcuts = new ArrayList<ListItemFieldShortcut>();
		protected List<ListItemMethodShortcut> allowedItemMethodShortcuts = new ArrayList<ListItemMethodShortcut>();
		protected List<InfoPattern> methodsExcludedFromItemDetails = new ArrayList<InfoPattern>();
		protected List<InfoPattern> fieldsExcludedFromItemDetails = new ArrayList<InfoPattern>();
		protected boolean itemDetailsViewDisabled = false;
		protected ListEditOptions editOptions = new ListEditOptions();
		protected boolean listSorted = false;
		protected IListItemDetailsAccessMode customDetailsAccessMode;
		protected ControlSizeCustomization width = null;
		protected ControlSizeCustomization height = null;
		protected boolean itemNullValueAllowed = false;
		protected ItemCreationMode itemCreationMode = ItemCreationMode.UNDEFINED;
		protected ToolsLocation customToolsLocation;
		protected ValueReturnMode customItemReturnMode;
		protected ITypeInfoFinder customItemTypeFinder;
		protected String selectionTargetFieldName;
		protected boolean itemAutomaticPositioningManagementForced = false;

		@Override
		public boolean isInitial() {
			ListCustomization defaultListCustomization = new ListCustomization();
			defaultListCustomization.listTypeName = listTypeName;
			defaultListCustomization.itemTypeName = itemTypeName;
			return InfoCustomizations.isSimilar(this, defaultListCustomization);
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getCustomItemTypeFinder() {
			return customItemTypeFinder;
		}

		@Deprecated
		public ItemNodeValidityDetectionConfiguration getItemNodeValidityDetectionConfiguration() {
			if (treeStructureDiscoverySettings != null) {
				if (treeStructureDiscoverySettings.getItemValidabilityStatusFieldNamePattern() != null) {
					ItemNodeValidityDetectionConfiguration result = new ItemNodeValidityDetectionConfiguration();
					result.setEnablementStatusFieldNamePattern(
							treeStructureDiscoverySettings.getItemValidabilityStatusFieldNamePattern());
					return result;
				}
			}
			return null;
		}

		@Deprecated
		public void setItemNodeValidityDetectionConfiguration(
				ItemNodeValidityDetectionConfiguration itemNodeValidityDetectionConfiguration) {
			if (itemNodeValidityDetectionConfiguration != null) {
				if (itemNodeValidityDetectionConfiguration.getEnablementStatusFieldNamePattern() != null) {
					if (treeStructureDiscoverySettings == null) {
						treeStructureDiscoverySettings = new TreeStructureDiscoverySettings();
					}
					treeStructureDiscoverySettings.setItemValidabilityStatusFieldNamePattern(
							itemNodeValidityDetectionConfiguration.getEnablementStatusFieldNamePattern());
				}
			}
		}

		public ValueReturnMode getCustomItemReturnMode() {
			return customItemReturnMode;
		}

		public void setCustomItemReturnMode(ValueReturnMode customItemReturnMode) {
			this.customItemReturnMode = customItemReturnMode;
		}

		public void setCustomItemTypeFinder(ITypeInfoFinder customItemTypeFinder) {
			this.customItemTypeFinder = customItemTypeFinder;
		}

		public ItemCreationMode getItemCreationMode() {
			return itemCreationMode;
		}

		public void setItemCreationMode(ItemCreationMode itemCreationMode) {
			this.itemCreationMode = itemCreationMode;
		}

		public ToolsLocation getCustomToolsLocation() {
			return customToolsLocation;
		}

		public void setCustomToolsLocation(ToolsLocation customToolsLocation) {
			this.customToolsLocation = customToolsLocation;
		}

		public boolean isItemAutomaticPositioningManagementForced() {
			return itemAutomaticPositioningManagementForced;
		}

		public void setItemAutomaticPositioningManagementForced(boolean itemAutomaticPositioningManagementForced) {
			this.itemAutomaticPositioningManagementForced = itemAutomaticPositioningManagementForced;
		}

		public String getSelectionTargetFieldName() {
			return selectionTargetFieldName;
		}

		public void setSelectionTargetFieldName(String selectionTargetFieldName) {
			this.selectionTargetFieldName = selectionTargetFieldName;
		}

		public boolean isItemNullValueAllowed() {
			return itemNullValueAllowed;
		}

		public void setItemNullValueAllowed(boolean itemNullValueAllowed) {
			this.itemNullValueAllowed = itemNullValueAllowed;
		}

		public ControlSizeCustomization getWidth() {
			return width;
		}

		public void setWidth(ControlSizeCustomization width) {
			this.width = width;
		}

		public ControlSizeCustomization getHeight() {
			return height;
		}

		public void setHeight(ControlSizeCustomization height) {
			this.height = height;
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

		@XmlElement(name = "fieldExcludedFromItemDetails")
		public List<InfoPattern> getFieldsExcludedFromItemDetails() {
			return fieldsExcludedFromItemDetails;
		}

		public void setFieldsExcludedFromItemDetails(List<InfoPattern> fieldsExcludedFromItemDetails) {
			this.fieldsExcludedFromItemDetails = fieldsExcludedFromItemDetails;
		}

		@XmlElement(name = "methodExcludedFromItemDetails")
		public List<InfoPattern> getMethodsExcludedFromItemDetails() {
			return methodsExcludedFromItemDetails;
		}

		public void setMethodsExcludedFromItemDetails(List<InfoPattern> methods) {
			this.methodsExcludedFromItemDetails = methods;
		}

		@XmlElement(name = "allowedItemFieldShortcut")
		public List<ListItemFieldShortcut> getAllowedItemFieldShortcuts() {
			return allowedItemFieldShortcuts;
		}

		public void setAllowedItemFieldShortcuts(List<ListItemFieldShortcut> allowedItemFieldShortcuts) {
			this.allowedItemFieldShortcuts = allowedItemFieldShortcuts;
		}

		@XmlElement(name = "allowedItemMethodShortcut")
		public List<ListItemMethodShortcut> getAllowedItemMethodShortcuts() {
			return allowedItemMethodShortcuts;
		}

		public void setAllowedItemMethodShortcuts(List<ListItemMethodShortcut> allowedItemMethodShortcuts) {
			this.allowedItemMethodShortcuts = allowedItemMethodShortcuts;
		}

		@XmlElement(name = "columnsCustomOrderEntry")
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

		@XmlElement(name = "columnCustomization")
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
			int result = MiscUtils.compareNullables(listTypeName, o.listTypeName);
			if (result == 0) {
				result = MiscUtils.compareNullables(itemTypeName, o.itemTypeName);
			}
			return result;
		}

		@Override
		public String toString() {
			return "ListCustomization [listTypeName=" + listTypeName + ", itemTypeName=" + itemTypeName + "]";
		}

	}

	public static class ItemNodeValidityDetectionConfiguration extends AbstractCustomization {
		private static final long serialVersionUID = 1L;
		protected InfoPattern enablementStatusFieldNamePattern;

		public InfoPattern getEnablementStatusFieldNamePattern() {
			return enablementStatusFieldNamePattern;
		}

		public void setEnablementStatusFieldNamePattern(InfoPattern enablementStatusFieldNamePattern) {
			this.enablementStatusFieldNamePattern = enablementStatusFieldNamePattern;
		}

	}

	public static class TreeStructureDiscoverySettings extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected boolean heterogeneousTree = true;
		protected ITypeInfoFinder customBaseNodeTypeFinder;
		protected List<InfoPattern> excludedSubListFields = new ArrayList<InfoPattern>();
		protected boolean singleSubListFieldNameNeverDisplayedAsTreeNode = true;
		protected boolean subListFieldControlSlave = false;
		protected InfoPattern itemValidabilityStatusFieldNamePattern;

		public InfoPattern getItemValidabilityStatusFieldNamePattern() {
			return itemValidabilityStatusFieldNamePattern;
		}

		public void setItemValidabilityStatusFieldNamePattern(InfoPattern itemValidabilityStatusFieldNamePattern) {
			this.itemValidabilityStatusFieldNamePattern = itemValidabilityStatusFieldNamePattern;
		}

		public boolean isSingleSubListFieldNameNeverDisplayedAsTreeNode() {
			return singleSubListFieldNameNeverDisplayedAsTreeNode;
		}

		public void setSingleSubListFieldNameNeverDisplayedAsTreeNode(
				boolean singleSubListFieldNameNeverDisplayedAsTreeNode) {
			this.singleSubListFieldNameNeverDisplayedAsTreeNode = singleSubListFieldNameNeverDisplayedAsTreeNode;
		}

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

		@XmlElement(name = "excludedSubListField")
		public List<InfoPattern> getExcludedSubListFields() {
			return excludedSubListFields;
		}

		public void setExcludedSubListFields(List<InfoPattern> excludedSubListFields) {
			this.excludedSubListFields = excludedSubListFields;
		}

		public boolean isSubListFieldControlSlave() {
			return subListFieldControlSlave;
		}

		public void setSubListFieldControlSlave(boolean subListFieldControlSlave) {
			this.subListFieldControlSlave = subListFieldControlSlave;
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
			return InfoCustomizations.isSimilar(this, defaultColumnCustomization);
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
			return MiscUtils.compareNullables(columnName, o.columnName);
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
			if ((className == null) || (className.length() == 0)) {
				throw new ReflectionUIError("Class name not specified !");
			}
			ClassUtils.getCachedClassForName(className);
		}

		@Override
		public ITypeInfo find(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier) {
			Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassForName(className);
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
			if ((implementationClassName == null) || (implementationClassName.length() == 0)) {
				throw new ReflectionUIError("Implementation class name not specified !");
			}
			Class<?> implementationClass = ClassUtils.getCachedClassForName(implementationClassName);
			if (!ITypeInfo.class.isAssignableFrom(implementationClass)) {
				throw new ReflectionUIError("Class not implementing " + ITypeInfo.class.getName() + " !");
			}
			try {
				implementationClass.getConstructor();
			} catch (Exception e) {
				throw new ReflectionUIError(
						"Could not find a default constructor for the implementation class: " + e.toString(), e);
			}
		}

		@Override
		public ITypeInfo find(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier) {
			try {
				Class<?> implementationClass = ClassUtils.getCachedClassForName(implementationClassName);
				return (ITypeInfo) implementationClass.newInstance();
			} catch (Exception e) {
				throw new ReflectionUIError("Failed to instantiate the implementation class: " + e.toString(), e);
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

	/**
	 * Maintains backward compatibility as much as possible with
	 * {@link InfoCustomizations} files by detecting and upgrading specific
	 * customizations.
	 * 
	 * @author olitank
	 *
	 */
	protected class Migrator {

		public void migrate() {
			boolean migrated = false;
			for (TypeCustomization tc : getTypeCustomizations()) {
				if (migrate(tc)) {
					migrated = true;
				}
			}
			if (migrated) {
				System.err.println("[" + InfoCustomizations.class.getName() + "] DEPRECATION NOTICE: " + this
						+ " was migrated automatically. The customizations file must be saved to remain valid with future releases.");
			}
		}

		private boolean migrate(TypeCustomization tc) {
			boolean migrated = false;
			if (tc.getTypeName().contains("NonNullableInstance")) {
				tc.setTypeName(tc.getTypeName().replace("NonNullableInstance", "MutableInstance"));
				migrated = true;
			}
			for (FieldCustomization fc : tc.getFieldsCustomizations()) {
				if (migrate(fc, tc)) {
					migrated = true;
				}
			}
			for (MethodCustomization mc : tc.getMethodsCustomizations()) {
				if (migrate(mc, tc)) {
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean migrate(FieldCustomization fc, TypeCustomization containingTc) {
			boolean migrated = false;
			if (fc.getNullReplacement() != null) {
				if (fixHistoricalImageIconClassSwappingIssue(fc.getNullReplacement())) {
					migrated = true;
				}
			}
			for (TypeCustomization fieldTc : fc.getSpecificTypeCustomizations().getTypeCustomizations()) {
				if (migrate(fieldTc)) {
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean migrate(MethodCustomization mc, TypeCustomization containingTc) {
			boolean migrated = false;
			if (mc.isReturnValueFieldGenerated()) {
				if (upgradeReturnValueFieldName(mc, containingTc)) {
					migrated = true;
				}
			}
			if (mc.getSerializedInvocationDatas().size() > 0) {
				if (upgradePresetMethodNames(mc, containingTc)) {
					migrated = true;
				}
			}
			for (ParameterCustomization pc : mc.getParametersCustomizations()) {
				if (migrate(pc, mc, containingTc)) {
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean migrate(ParameterCustomization pc, MethodCustomization mc, TypeCustomization containingTc) {
			boolean migrated = false;
			if (pc.isDisplayedAsField()) {
				if (upgradeParameterFieldName(pc, mc, containingTc)) {
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean upgradeParameterFieldName(ParameterCustomization pc, MethodCustomization mc,
				TypeCustomization containingTc) {
			boolean migrated = false;
			for (FieldCustomization siblingFc : containingTc.getFieldsCustomizations()) {
				if (siblingFc.getFieldName().equals(ParameterAsFieldInfo
						.buildLegacyParameterFieldName(mc.getMethodName(), pc.getParameterName()))) {
					String oldFieldName = siblingFc.getFieldName();
					siblingFc.setFieldName(ParameterAsFieldInfo.buildParameterFieldName(mc.getMethodSignature(),
							pc.getParameterName()));
					if (containingTc.getCustomFieldsOrder() != null) {
						MiscUtils.replaceItem(containingTc.getCustomFieldsOrder(), oldFieldName,
								siblingFc.getFieldName());
					}
					if (siblingFc.getEncapsulationFieldName() != null) {
						FieldCustomization capsuleFc = getFieldCustomization(containingTc,
								siblingFc.getEncapsulationFieldName(), false);
						if (capsuleFc != null) {
							String capsuleTypeName = MembersCapsuleFieldInfo
									.buildTypeName(siblingFc.getEncapsulationFieldName(), containingTc.getTypeName());
							TypeCustomization capsuleTc = getTypeCustomization(InfoCustomizations.this, capsuleTypeName,
									false);
							if (capsuleTc != null) {
								upgradeParameterFieldName(pc, mc, capsuleTc);
							}
						}
					}
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean upgradePresetMethodNames(MethodCustomization mc, TypeCustomization containingTc) {
			boolean migrated = false;
			List<TextualStorage> serializedInvocationDatas = mc.getSerializedInvocationDatas();
			for (int invocationDataIndex = 0; invocationDataIndex < serializedInvocationDatas
					.size(); invocationDataIndex++) {
				for (MethodCustomization siblingMc : containingTc.getMethodsCustomizations()) {
					if (siblingMc.getMethodName().equals(PresetInvocationDataMethodInfo
							.buildLegacyPresetMethodName(mc.getMethodName(), invocationDataIndex))) {
						String oldMethodSignature = siblingMc.getMethodSignature();
						siblingMc.setMethodName(PresetInvocationDataMethodInfo
								.buildPresetMethodName(mc.getMethodSignature(), invocationDataIndex));
						if (containingTc.getCustomMethodsOrder() != null) {
							MiscUtils.replaceItem(containingTc.getCustomMethodsOrder(), oldMethodSignature,
									siblingMc.getMethodSignature());
						}
						if (siblingMc.getEncapsulationFieldName() != null) {
							FieldCustomization capsuleFc = getFieldCustomization(containingTc,
									siblingMc.getEncapsulationFieldName(), false);
							if (capsuleFc != null) {
								String capsuleTypeName = MembersCapsuleFieldInfo.buildTypeName(
										siblingMc.getEncapsulationFieldName(), containingTc.getTypeName());
								TypeCustomization capsuleTc = getTypeCustomization(InfoCustomizations.this,
										capsuleTypeName, false);
								if (capsuleTc != null) {
									upgradePresetMethodNames(mc, capsuleTc);
								}
							}
						}
						migrated = true;
					}
				}
			}
			return migrated;
		}

		private boolean upgradeReturnValueFieldName(MethodCustomization mc, TypeCustomization containingTc) {
			boolean migrated = false;
			for (FieldCustomization siblingFc : containingTc.getFieldsCustomizations()) {
				if (siblingFc.getFieldName()
						.equals(MethodReturnValueAsFieldInfo.buildLegacyReturnValueFieldName(mc.getMethodName()))) {
					String oldFieldName = siblingFc.getFieldName();
					siblingFc.setFieldName(
							MethodReturnValueAsFieldInfo.buildMethodReturnValueFieldName(mc.getMethodSignature()));
					if (containingTc.getCustomFieldsOrder() != null) {
						MiscUtils.replaceItem(containingTc.getCustomFieldsOrder(), oldFieldName,
								siblingFc.getFieldName());
					}
					if (siblingFc.getEncapsulationFieldName() != null) {
						FieldCustomization capsuleFc = getFieldCustomization(containingTc,
								siblingFc.getEncapsulationFieldName(), false);
						if (capsuleFc != null) {
							String capsuleTypeName = MembersCapsuleFieldInfo
									.buildTypeName(siblingFc.getEncapsulationFieldName(), containingTc.getTypeName());
							TypeCustomization capsuleTc = getTypeCustomization(InfoCustomizations.this, capsuleTypeName,
									false);
							if (capsuleTc != null) {
								upgradeReturnValueFieldName(mc, capsuleTc);
							}
						}
					}
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean fixHistoricalImageIconClassSwappingIssue(TextualStorage textualStorage) {
			if (textualStorage.getData() != null) {
				Mapping preConversion = textualStorage.getPreConversion();
				if (preConversion != null) {
					ConversionMethodFinder reverseConversionMethodFinder = preConversion
							.getReverseConversionMethodFinder();
					if (reverseConversionMethodFinder != null) {
						if (xy.reflect.ui.util.ImageIcon.class.getName()
								.equals(reverseConversionMethodFinder.getConversionClassName())) {
							String reverseConversionMethodSignature = reverseConversionMethodFinder
									.getConversionMethodSignature();
							if (reverseConversionMethodSignature != null) {
								if (ReflectionUIUtils.buildMethodSignature(Image.class.getName(), "getImage",
										Collections.emptyList()).equals(reverseConversionMethodSignature)) {
									try {
										byte[] binary = DatatypeConverter.parseBase64Binary(textualStorage.getData());
										try {
											ByteArrayInputStream bais = new ByteArrayInputStream(binary);
											ObjectInputStream ois = new ObjectInputStream(bais);
											if (!(ois.readObject() instanceof xy.reflect.ui.util.ImageIcon)) {
												throw new ClassCastException();
											}
										} catch (Exception e) {
											ByteArrayInputStream bais = new ByteArrayInputStream(binary);
											ObjectInputStream ois = IOUtils.getClassSwappingObjectInputStream(bais,
													javax.swing.ImageIcon.class.getName(),
													xy.reflect.ui.util.ImageIcon.class.getName());
											xy.reflect.ui.util.ImageIcon xyIcon = (xy.reflect.ui.util.ImageIcon) ois
													.readObject();
											Filter<Object> reverseConversionMethod = preConversion
													.buildOverallReverseConversionMethod();
											Image image = (Image) reverseConversionMethod.get(xyIcon);
											textualStorage.save(image);
											return true;
										}
									} catch (Exception e) {
										throw new ReflectionUIError(e);
									}
								}
							}
						}
					}
				}
			}
			return false;
		}

		public String migrateXMLLocalName(String localName) {
			if ("appplicationCustomization".equals(localName)) {
				return "applicationCustomization";
			} else if ("columnCustomizations".equals(localName)) {
				return "columnCustomization";
			} else if ("enumerationCustomizations".equals(localName)) {
				return "enumerationCustomization";
			} else if ("fieldsCustomizations".equals(localName)) {
				return "fieldsCustomization";
			} else if ("itemCustomizations".equals(localName)) {
				return "itemCustomization";
			} else if ("listCustomizations".equals(localName)) {
				return "listCustomization";
			} else if ("memberCategories".equals(localName)) {
				return "memberCategory";
			} else if ("methodsCustomizations".equals(localName)) {
				return "methodsCustomization";
			} else if ("parametersCustomizations".equals(localName)) {
				return "parametersCustomization";
			} else if ("serializedInvocationDatas".equals(localName)) {
				return "serializedInvocationData";
			} else if ("typeCustomizations".equals(localName)) {
				return "typeCustomization";
			} else if ("virtualFieldDeclarations".equals(localName)) {
				return "virtualFieldDeclaration";
			} else if ("implicitListFieldDeclarations".equals(localName)) {
				return "implicitListFieldDeclaration";
			} else if ("importedFieldDeclarations".equals(localName)) {
				return "importedFieldDeclaration";
			} else if ("importedMethodDeclarations".equals(localName)) {
				return "importedMethodDeclaration";
			} else if ("excludedSubListFields".equals(localName)) {
				return "excludedSubListField";
			} else if ("fieldsExcludedFromItemDetails".equals(localName)) {
				return "fieldExcludedFromItemDetails";
			} else if ("methodsExcludedFromItemDetails".equals(localName)) {
				return "methodExcludedFromItemDetails";
			} else if ("polymorphicSubTypeFinders".equals(localName)) {
				return "polymorphicSubTypeFinder";
			} else if ("allowedItemFieldShortcuts".equals(localName)) {
				return "allowedItemFieldShortcut";
			} else if ("allowedItemMethodShortcuts".equals(localName)) {
				return "allowedItemMethodShortcut";
			} else if ("menus".equals(localName)) {
				return "menu";
			} else if ("itemCategories".equals(localName)) {
				return "itemCategory";
			} else if ("parameterizedFieldNames".equals(localName)) {
				return "parameterizedFieldName";
			} else if ("itemsCustomOrder".equals(localName)) {
				return "itemsCustomOrderEntry";
			} else if ("columnsCustomOrder".equals(localName)) {
				return "columnsCustomOrderEntry";
			} else if ("customFieldsOrder".equals(localName)) {
				return "customFieldsOrderEntry";
			} else if ("customMethodsOrder".equals(localName)) {
				return "customMethodsOrderEntry";
			} else {
				return null;
			}
		}

	}

}
