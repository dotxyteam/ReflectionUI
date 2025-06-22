import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.info.custom.InfoCustomizations;

public class EditorTest {

	public static void main(String[] args) {
		InfoCustomizations infoCustomizations = new InfoCustomizations();
		CustomizedUI reflectionUI = new CustomizedUI(infoCustomizations);
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-ui.project.directory", "./") + "unpackaged-src/default.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new Test(), null, null);
			}
		});
	}

	public static class Test {
		private Integer systematicallyReplacedDirectValue = 0;
		private Test2 nonReplacedDirectValue = new Test2();
		private List<Integer> systematicallyReplacedDirectList = new ArrayList<Integer>(Arrays.asList(1, 2, 3));

		public Integer getSystematicallyReplacedDirectValue() {
			return systematicallyReplacedDirectValue;
		}

		public void setSystematicallyReplacedDirectValue(Integer systematicallyReplacedDirectValue) {
			this.systematicallyReplacedDirectValue = systematicallyReplacedDirectValue;
		}

		public Test2 getNonReplacedDirectValue() {
			return nonReplacedDirectValue;
		}

		public void setNonReplacedDirectValue(Test2 nonReplacedDirectValue) {
			this.nonReplacedDirectValue = nonReplacedDirectValue;
		}

		public List<Integer> getSystematicallyReplacedDirectList() {
			return systematicallyReplacedDirectList;
		}

		public void setSystematicallyReplacedDirectList(List<Integer> systematicallyReplacedDirectList) {
			this.systematicallyReplacedDirectList = systematicallyReplacedDirectList;
		}
	}

	public static class Test2 {
		private String value = "azerty";

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}
}
