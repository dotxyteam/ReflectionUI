import java.util.Arrays;

import xy.reflect.ui.control.swing.customizer.CustomizationTools; 
import xy.reflect.ui.control.swing.customizer.CustomizationToolsUI; 
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.info.custom.InfoCustomizations.EnumerationCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

public class ExtractCustomizationsHelp { 

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		CustomizationTools tools = SwingCustomizer.getDefault().getCustomizationTools();
		CustomizationToolsUI toolsUI = tools.getToolsUI();
		for (Class<?> c : Arrays.asList(TypeCustomization.class, FieldCustomization.class, MethodCustomization.class,
				ListCustomization.class, EnumerationCustomization.class)) {
			ITypeInfo typeCustomizationType = toolsUI.getTypeInfo(new JavaTypeInfoSource(c, null));
			printTypeHelp(typeCustomizationType);
		}
	}

	private static void printTypeHelp(ITypeInfo typeCustomizationType) {
		System.out.println("<H1>" + typeCustomizationType.getCaption() + "</H1>");
		System.out.println("<UL>");
		for (IFieldInfo field : typeCustomizationType.getFields()) {
			if (field.getOnlineHelp() != null) {
				if (field.getCaption().trim().length() > 0) {
					System.out.println("<LI>" + "<B>" + field.getCaption() + ": " + "</B>"+ field.getOnlineHelp() + "</LI>");
				}
			}
		}
		System.out.println("</UL>");
	}

}
