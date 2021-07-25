package xy.reflect.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import xy.reflect.ui.control.swing.customizer.CustomizationToolsUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.ListLenghtCustomization;
import xy.reflect.ui.info.field.CapsuleFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.IOUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TestToolsMessages {

	private static String[] englishWords;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		englishWords = IOUtils.read(TestToolsMessages.class.getResourceAsStream("words.en")).toLowerCase()
				.split("[\\n\\r]+");
	}

	private boolean checkSpell(String sentence, String typeName, String memberName) {
		String[] words = sentence.split("[^\\p{L}]+");
		Set<String> misspelledWords = new HashSet<String>();
		for (String word : words) {
			word = word.toLowerCase();
			if (!Arrays.asList(englishWords).contains(word)) {
				if (word.length() == 0) {
					continue;
				}
				misspelledWords.add(word);
			}
		}
		if (misspelledWords.size() > 0) {
			String errorMsg = "\n- Incorrect sentence: " + sentence + "\n	=> misspelledWords=" + misspelledWords
					+ "\n	=> context: type=" + typeName + "; member=" + memberName;
			System.err.println(errorMsg);
			return false;
		} else {
			return true;
		}
	}

	@Test
	public void testSpells() throws Exception {
		/*
		 * Good websites: http://itpro.cz/juniconv/ http://french.typeit.org/
		 */
		CustomizationToolsUI toolsUI = SwingCustomizer.getDefault().getCustomizationTools().getToolsUI();
		boolean correctSpells = true;
		for (Class<?> c : InfoCustomizations.class.getDeclaredClasses()) {
			if (Modifier.isAbstract(c.getModifiers())) {
				continue;
			}
			ITypeInfo typeCustomizationType = toolsUI.buildTypeInfo(new JavaTypeInfoSource(toolsUI, c, null));
			correctSpells = testSpells(typeCustomizationType) && correctSpells;
		}
		if (!correctSpells) {
			Assert.fail("Incorrect spells detected !");
		}
	}

	private boolean testSpells(ITypeInfo type) {
		boolean correctSpells = true;
		if (!Arrays.asList(ListLenghtCustomization.class.getName()).contains(type.getName())) {
			correctSpells = checkSpell(type.getCaption(), type.getName(), "<caption>") && correctSpells;
		}
		if (type.getOnlineHelp() != null) {
			correctSpells = checkSpell(type.getOnlineHelp(), type.getName(), "<onlineHelp>") && correctSpells;
		}
		for (IFieldInfo field : type.getFields()) {
			correctSpells = testSpells(field, type) && correctSpells;
		}
		for (IMethodInfo method : type.getMethods()) {
			correctSpells = testSpells(method, type) && correctSpells;
		}
		return correctSpells;
	}

	private boolean testSpells(IFieldInfo field, ITypeInfo containingType) {
		if (field.isHidden()) {
			return true;
		}
		boolean correctSpells = true;
		correctSpells = checkSpell(field.getCaption(), containingType.getName(), field.getName() + ".<caption>")
				&& correctSpells;
		if (field.getOnlineHelp() != null) {
			correctSpells = checkSpell(field.getOnlineHelp(), containingType.getName(),
					field.getName() + ".<onlineHelp>") && correctSpells;
		}
		if (field.getCategory() != null) {
			correctSpells = checkSpell(field.getCategory().getCaption(), containingType.getName(),
					field.getName() + ".<category>") && correctSpells;
		}
		CapsuleFieldInfo capsuleField = getCapsuleField(field);
		if (capsuleField != null) {
			for (IFieldInfo subField : capsuleField.getType().getFields()) {
				correctSpells = testSpells(subField, capsuleField.getType()) && correctSpells;
			}
		}
		return correctSpells;
	}

	private boolean testSpells(IMethodInfo method, ITypeInfo containingType) {
		if (method.isHidden()) {
			return true;
		}
		boolean correctSpells = true;
		String methodDescription = ReflectionUIUtils.formatMethodControlTooltipText(method.getCaption(),
				method.getOnlineHelp(), method.getParameters());
		if (methodDescription != null) {
			correctSpells = checkSpell(methodDescription, containingType.getName(), method.getSignature())
					&& correctSpells;
		}
		if (method.getCategory() != null) {
			correctSpells = checkSpell(method.getCategory().getCaption(), containingType.getName(),
					method.getSignature() + ".<category>") && correctSpells;
		}
		return correctSpells;
	}

	private CapsuleFieldInfo getCapsuleField(IFieldInfo field) {
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

	public Object getInaccesibleFieldValue(Class<?> clazz, Object obj, String fieldName) {
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
