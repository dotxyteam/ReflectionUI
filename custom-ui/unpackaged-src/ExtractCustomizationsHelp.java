import java.lang.reflect.Field;
import java.util.Arrays;

import xy.reflect.ui.control.swing.customizer.CustomizationTools;
import xy.reflect.ui.control.swing.customizer.CustomizationToolsUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.info.custom.InfoCustomizations.EnumerationCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.field.CapsuleFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

public class ExtractCustomizationsHelp {

	public static void main(String[] args) {
		CustomizationTools tools = SwingCustomizer.getDefault().getCustomizationTools();
		CustomizationToolsUI toolsUI = tools.getToolsUI();
		for (Class<?> c : Arrays.asList(TypeCustomization.class, FieldCustomization.class, MethodCustomization.class,
				ListCustomization.class, EnumerationCustomization.class)) {
			ITypeInfo typeCustomizationType = toolsUI.buildTypeInfo(new JavaTypeInfoSource(toolsUI, c, null));
			printTypeHelp(typeCustomizationType);
		}
	}

	private static void printTypeHelp(ITypeInfo typeCustomizationType) {
		System.out.println("<H3>" + typeCustomizationType.getCaption() + "</H3>");
		System.out.println("<UL>");
		for (IFieldInfo field : typeCustomizationType.getFields()) {
			printFieldHelp(field);
		}
		System.out.println("</UL>");
	}

	private static void printFieldHelp(IFieldInfo field) {
		if (field.getOnlineHelp() != null) {
			if (field.getCaption().trim().length() > 0) {
				System.out.println("<LI>" + "<B>" + field.getCaption() + ": " + "</B>" + MiscUtils
						.escapeHTML(field.getOnlineHelp().replace("\n", " ").replace("  ", " "), true) + "</LI>");
			}
		} else {
			CapsuleFieldInfo capsuleField = getCapsuleField(field);
			if (capsuleField != null) {
				for (IFieldInfo subField : capsuleField.getType().getFields()) {
					printFieldHelp(subField);
				}
			}
		}
	}

	private static CapsuleFieldInfo getCapsuleField(IFieldInfo field) {
		while (true) {
			try {
				field = (IFieldInfo) getInaccesibleFieldValue(field.getClass(), field, "base");
			} catch (Exception e) {
				break;
			}
			if (field instanceof CapsuleFieldInfo) {
				return (CapsuleFieldInfo) field;
			}
			if (field == null) {
				break;
			}
		}
		return null;
	}

	public static Object getInaccesibleFieldValue(Class<?> clazz, Object obj, String fieldName) {
		try {
			Field field;
			while (true) {
				try {
					field = clazz.getDeclaredField(fieldName);
					break;
				} catch (NoSuchFieldException e) {
					clazz = clazz.getSuperclass();
					if (clazz == null) {
						throw e;
					}
				}
			}
			field.setAccessible(true);
			return field.get(obj);
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
	}
}
