package xy.reflect.ui;

import java.io.File;
import java.io.Serializable;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;

public class MenuCreationTest implements Serializable {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {

		CustomizedUI reflectionUI = new CustomizedUI();
		File tmpCustomizationsFile = File.createTempFile(MenuCreationTest.class.getName(), ".icu");
		tmpCustomizationsFile.deleteOnExit();
		reflectionUI.getInfoCustomizations().saveToFile(tmpCustomizationsFile, null);
		final SwingCustomizer swingCustomizer = new SwingCustomizer(reflectionUI, tmpCustomizationsFile.getPath()) {

			@Override
			public boolean isCustomizationsEditorEnabled() {
				return true;
			}

			@Override
			public CustomizationController createCustomizationController() {
				return new CustomizationController(this) {

					ModificationStack modificationStack = new ModificationStack(null);

					@Override
					protected void openWindow() {
						refreshCustomizedControlsOnModification();
					}

					@Override
					protected void closeWindow() {
					}

					@Override
					public ModificationStack getModificationStack() {
						return modificationStack;
					}

				};
			}

		};
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				swingCustomizer.openObjectFrame(new MenuCreationTest());
			}
		});
	}

	public String text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Praesent interdum. Cras eleifend luctus dui. Maecenas eros. Proin facilisis. Nullam massa. Donec sit amet odio. Donec porta. Suspendisse potenti. \n"
			+ "	Ut eget nibh. Duis sit amet augue. Phasellus ac nunc nec tellus congue ullamcorper. Cras quam. Aliquam eget nibh a arcu elementum sodales. Duis dui orci, sodales sit amet, placerat vel, vestibulum vitae, nibh. Pellentesque malesuada consectetuer eros. Nunc at dui. \n"
			+ "	Morbi tempor. Donec nisl. Proin id sapien. Curabitur purus. Fusce a eros non odio imperdiet tristique. Nam semper dui. Curabitur tempor ante ac lacus. Sed nisl. Mauris vestibulum. \n"
			+ "	Donec augue est, facilisis id, nonummy ac, tristique sed, nisl. Morbi est dolor, elementum quis, porttitor semper, lobortis vel, nulla. Maecenas libero. Nam vel magna in ligula commodo volutpat. Ut vel risus eget nibh vulputate placerat. Ut blandit enim nec eros. \n"
			+ "	Curabitur nisl. Integer dui. Suspendisse at turpis. Donec faucibus vehicula ligula. Nullam elementum. \n"
			+ "	Cras sapien. Nulla adipiscing. Praesent ut elit. Sed nisl arcu, vulputate semper, pretium vitae, fermentum a, dolor. Vestibulum vel lorem. \n"
			+ "	Cras scelerisque viverra tellus. Nunc tempor elementum mi. Maecenas sem. Phasellus fermentum. Nunc eu nulla. Nulla dui turpis, iaculis a, congue eu, condimentum non, pede. Curabitur placerat, quam id scelerisque faucibus, enim sem pretium wisi, non facilisis sem lacus nec quam. \n"
			+ "	Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Vivamus quis justo. Quisque lectus. Fusce vitae risus sit amet lorem feugiat semper. Proin ornare erat et quam. Donec euismod justo eu nibh. Suspendisse ultricies. Donec pede. Mauris dolor felis, dignissim non, tempus in, euismod eget, velit. Pellentesque massa dui, cursus eget, bibendum eget, facilisis et, tortor. Donec mattis, quam vel faucibus imperdiet, odio sem molestie sem, ut pellentesque tortor dolor in lorem. Nunc tempor venenatis lectus. Vestibulum at enim. \n"
			+ "	Aenean ligula magna, semper vitae, accumsan eu, viverra vel, pede. Duis auctor. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Pellentesque lobortis. Vivamus sem. \n"
			+ "	Morbi vehicula, lorem ac aliquam lobortis, mi eros condimentum lacus, ac pellentesque arcu urna eget metus. Sed vel enim. Suspendisse mauris dolor, dignissim sit amet, faucibus at, mollis at, lorem. Aliquam hendrerit ornare justo. Integer pede ligula, eleifend a, consequat sed, bibendum sed, tortor. \n"
			+ "	Aliquam lectus lorem, cursus eu, adipiscing ac, accumsan vel, felis. Fusce sed sapien at neque varius facilisis. Donec bibendum blandit erat. Nullam nunc neque, aliquam in, eleifend vitae, lobortis at, turpis. Proin convallis, est at rutrum venenatis, arcu sapien molestie eros, eu gravida dui ipsum sit amet tortor. Aenean dui. Vestibulum eu tortor. Donec id orci. Integer fermentum pharetra est. Nunc euismod sodales orci. Aenean wisi. In neque. \n"
			+ "	Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec eu arcu. Fusce velit. Phasellus eros. Nunc consectetuer euismod enim. Quisque magna enim, convallis non, bibendum eu, vestibulum vel, nulla. Etiam id ante. Aenean laoreet. Aenean cursus. \n"
			+ "	Integer bibendum odio non nunc. Ut posuere, leo vel suscipit pulvinar, magna nulla blandit lorem, vel consequat tortor enim vel pede. Sed turpis. Phasellus varius lacus ut libero. Praesent volutpat elit vel enim. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Suspendisse pretium ante ut erat. Nam ornare. Nam mollis tellus non nunc. Etiam vel diam. Praesent semper. Donec gravida ornare lacus. Quisque pellentesque, pede semper tincidunt gravida, eros est condimentum lorem, sed gravida orci risus sit amet mi. Curabitur elementum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Vivamus varius vehicula diam. Vestibulum pharetra. Maecenas euismod augue id turpis. Donec magna. Donec a quam. Duis viverra faucibus velit. Nam dictum ultricies diam. Vivamus est. Proin at pede. Donec semper wisi sed dolor sollicitudin venenatis. Integer imperdiet. Vivamus pulvinar elit semper lacus. ";

}
