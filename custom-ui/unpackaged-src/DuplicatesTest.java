
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

public class DuplicatesTest {

	public static void main(String[] args) {
		System.setProperty(MoreSystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH, "unpackaged-src/duplicates.icu");
		SwingCustomizer.getDefault().openObjectFrame("123", "test", null);
	}
}
