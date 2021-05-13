import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

public class MemberNamesCollisionTest {

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

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingCustomizer.getDefault().openObjectFrame(new Sub());
			}
		});
	}

}
