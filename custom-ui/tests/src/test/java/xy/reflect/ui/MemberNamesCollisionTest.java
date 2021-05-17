package xy.reflect.ui;

import java.io.File;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;

public class MemberNamesCollisionTest {

	public static void main(String[] args) throws Exception {

		CustomizedUI reflectionUI = new CustomizedUI();
		File tmpCustomizationsFile = File.createTempFile(MemberNamesCollisionTest.class.getName(), ".icu");
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
				swingCustomizer.openObjectFrame(new Sub());
			}
		});
	}

	public static class BaseBaseBase {
		public static String anyBaseStatic;
		public static String staticAndNonStaticAleternatively;

		public static void anyBaseStatic() {
		}
	}

	public static class BaseBase extends BaseBaseBase {
		public static String anyBaseStatic;
		public String staticAndNonStaticAleternatively;

		public static void anyBaseStatic() {
		}
	}

	public static class Base extends BaseBase {
		public static String anyBaseStatic;
		public static String staticAndNonStaticAleternatively;

		public static void anyBaseStatic() {
		}

		public static String baseStaticOrSubNonStatic;

		public void baseNonStaticOrSubNonStatic() {

		}
	}

	public static class Sub extends Base {
		private static String staticAccess;
		public String staticAndNonStaticAleternatively;
		private static String instanceAccess;
		private static String staticGetAndInstanceSet;
		private static String staticSetAndInstanceGet;

		public String baseStaticOrSubNonStatic;

		public void baseNonStaticOrSubNonStatic() {
		}

		public static String getStaticAccess() {
			return staticAccess;
		}

		public static void setStaticAccess(String s) {
			staticAccess = s;
		}

		public static String getInstanceAccess() {
			return instanceAccess;
		}

		public static void setInstanceAccess(String instanceAccess) {
			Sub.instanceAccess = instanceAccess;
		}

		public static String getStaticGetAndInstanceSet() {
			return staticGetAndInstanceSet;
		}

		public void setStaticGetAndInstanceSet(String staticGetAndInstanceSet) {
			Sub.staticGetAndInstanceSet = staticGetAndInstanceSet;
		}

		public String getStaticSetAndInstanceGet() {
			return staticSetAndInstanceGet;
		}

		public static void setStaticSetAndInstanceGet(String staticSetAndInstanceGet) {
			Sub.staticSetAndInstanceGet = staticSetAndInstanceGet;
		}

	}

}
