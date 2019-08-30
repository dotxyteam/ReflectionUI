
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.info.custom.InfoCustomizations;

public class ReflectionUITest2 {

	public static void main(String[] args) {
		InfoCustomizations infoCustomizations = new InfoCustomizations();
		CustomizedUI reflectionUI = new CustomizedUI(infoCustomizations);
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "unpackaged-src/default.icu");
		renderer.openObjectFrame(new ReflectionUITest2(), null, null);
	}

	public String textControlTest = "azerty";

	public String getTextControlGetOnlyTest() {
		return "azerty";
	};

	public boolean checkBoxControlTest = true;

	public boolean getCheckBoxControlGetOnlyTest() {
		return true;
	};

	public Date embeddedFormTest = new Date();

	public Date getEmbeddedFormGetOnlyTest() {
		return new Date();
	};

	enum TheEnum {
		enumItem1, enumItem2
	}

	public TheEnum enumerationControlTest = TheEnum.enumItem1;

	public TheEnum getEnumerationControlGetOnlyTest() {
		return TheEnum.enumItem1;
	};

	public List<String> listControlTest = Arrays.asList("a", "z");

	public List<String> getListControlTestGetOnlyTest() {
		return Arrays.asList("a", "z");
	};

	public Object nullableControlTest = null;

	public Object getNullableControlGetOnlyTest() {
		return null;
	};

	public static class Base {

	}

	public static class Derived1 extends Base {

	}

	public static class Derived2 extends Base {

	}

	public Base polymorphicControlTest = new Derived1();

	public Base getPolymorphicControlGetOnlyTest() {
		return new Derived1();
	};

	public int primitiveValueControlTest = 0;

	public int getPrimitiveValueControlGetOnlyTest() {
		return 0;
	};

	public Color colorPickerTest = new Color(0, 0, 0);

	public Color getColorPickerGetOnlyTest() {
		return new Color(0, 0, 0);
	};

	public boolean customCheckBoxTest = true;

	public boolean getCustomCheckBoxGetOnlyTest() {
		return true;
	};

	public Date datePickerTest = new Date();

	public Date getDatePickerGetOnlyTest() {
		return new Date();
	};

	public Date dateTimePickerTest = new Date();

	public Date getDateTimePickerGetOnlyTest() {
		return new Date();
	};

	public List<String> detailedListControlTest = Arrays.asList("a", "z");

	public List<String> getDetailedListControlTestGetOnlyTest() {
		return Arrays.asList("a", "z");
	};

	public File fileBrowserTest = new File(".");

	public File getFileBrowserGetOnlyTest() {
		return new File(".");
	};

	public String htmlTest = "azerty";

	public String getHtmlGetOnlyTest() {
		return "azerty";
	};

	public String singleLineTextTest = "";

	public String getSingleLineTextGetOnlyTest() {
		return "";
	};

	public Image imageViewTest = null;

	public Image getImageViewGetOnlyTest() {
		return null;
	};

	public TheEnum optionButtonsTest = TheEnum.enumItem1;

	public TheEnum getOptionButtonsGetOnlyTest() {
		return TheEnum.enumItem1;
	};

	public String passwordFieldTest = "azerty";

	public String getPasswordFieldGetOnlyTest() {
		return "azerty";
	};

	public int sliderTest = 0;

	public int getSliderGetOnlyTest() {
		return 0;
	};

	public int spinnerTest = 0;

	public int getSpinnerGetOnlyTest() {
		return 0;
	};

	public String styledTextTest = "azerty";

	public String getStyledTextGetOnlyTest() {
		return "azerty";
	};

	public Date dialogAccessControlTest = new Date();

	public Date getDialogAccessControlGetOnlyTest() {
		return new Date();
	};

	public void buttonTest() {

	}
	
	public void exceptionTest() {
		throw new RuntimeException();
	}
}
