
package xy.reflect.ui.control.swing.customizer;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.info.type.factory.InfoCustomizationsFactory;
import xy.reflect.ui.info.type.factory.InfoProxyFactoryChain;

public class MultiSwingCustomizer extends SwingRenderer {

	public static final String SWITCH_TO_MAIN_CUSTOMIZER = MultiSwingCustomizer.class.getName()
			+ ".SWITCH_TO_MAIN_CUSTOMIZER";

	protected String mainInfoCustomizationsOutputFilePath;
	protected Function<Object, String> subCustomizationsSwitchSelector;

	protected Map<SubSwingCustomizer, String> switchBySubCustomizer = new WeakHashMap<SubSwingCustomizer, String>();

	public MultiSwingCustomizer(String mainInfoCustomizationsOutputFilePath,
			Function<Object, String> subCustomizationsSwitchSelector) {
		super(null);
		reflectionUI = createSubCustomizedUI(null);
		this.mainInfoCustomizationsOutputFilePath = mainInfoCustomizationsOutputFilePath;
		this.subCustomizationsSwitchSelector = subCustomizationsSwitchSelector;
	}

	public String getMainInfoCustomizationsOutputFilePath() {
		return mainInfoCustomizationsOutputFilePath;
	}

	protected String getSubInfoCustomizationsOutputFilePath(String switchIdentifier) {
		File mainInfoCustomizationsOutputFile = new File(mainInfoCustomizationsOutputFilePath);
		String fileNamePrefix = (switchIdentifier == SWITCH_TO_MAIN_CUSTOMIZER) ? "" : "-" + switchIdentifier;
		return new File(mainInfoCustomizationsOutputFile.getParentFile(),
				fileNamePrefix + mainInfoCustomizationsOutputFile.getName()).getPath();
	}

	@Override
	public SubCustomizedUI getReflectionUI() {
		return (SubCustomizedUI) super.getReflectionUI();
	}

	public Map<String, SubSwingCustomizer> getSubCustomizerBySwitch() {
		return Collections.unmodifiableMap(switchBySubCustomizer.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));
	}

	protected SubSwingCustomizer switchSubCustomizer(SubSwingCustomizer current, Object object) {
		String switchIdentifier = subCustomizationsSwitchSelector.apply(object);
		if (switchIdentifier == null) {
			if (current != null) {
				return current;
			} else {
				switchIdentifier = SWITCH_TO_MAIN_CUSTOMIZER;
			}
		}
		SubSwingCustomizer result = getSubCustomizerBySwitch().get(switchIdentifier);
		if (result == null) {
			result = createSubCustomizer(switchIdentifier);
			switchBySubCustomizer.put(result, switchIdentifier);
		}
		return result;
	}

	protected SubSwingCustomizer createSubCustomizer(String switchIdentifier) {
		return new SubSwingCustomizer(switchIdentifier);
	}

	protected SubCustomizedUI createSubCustomizedUI(String switchIdentifier) {
		return new SubCustomizedUI(this, switchIdentifier);
	}

	@Override
	public Form createForm(Object object, IInfoFilter infoFilter) {
		return switchSubCustomizer(null, object).createForm(object, infoFilter);
	}

	@Override
	public boolean isRenderedForm(Component c) {
		if (super.isRenderedForm(c)) {
			return true;
		}
		for (SubSwingCustomizer subCustomizer : switchBySubCustomizer.keySet()) {
			if (subCustomizer.isSubRenderedForm(c)) {
				return true;
			}
		}
		return false;
	}

	public static class SubCustomizedUI extends CustomizedUI {

		protected MultiSwingCustomizer parent;
		protected String switchIdentifier;

		public SubCustomizedUI(MultiSwingCustomizer parent, String switchIdentifier) {
			this.parent = parent;
			this.switchIdentifier = switchIdentifier;
		}

		public MultiSwingCustomizer getParent() {
			return parent;
		}

		public String getSwitchIdentifier() {
			return switchIdentifier;
		}

		public CustomizedUI getRoot() {
			return getParent().getReflectionUI();
		}

		public boolean isRoot() {
			return getRoot() == this;
		}

		@Override
		public InfoProxyFactoryChain getInfoCustomizationsFactory() {
			return new InfoProxyFactoryChain(getMainInfoCustomizationsFactory(), getSubInfoCustomizationsFactory());

		}

		protected IInfoProxyFactory getMainInfoCustomizationsFactory() {
			if (isRoot()) {
				return IInfoProxyFactory.NULL_INFO_PROXY_FACTORY;
			}
			return new InfoCustomizationsFactory(this) {

				@Override
				public String getIdentifier() {
					return "MainCustomizationsFactory [of=" + switchIdentifier + "]";
				}

				@Override
				protected IInfoProxyFactory getInfoCustomizationsSetupFactory() {
					return getRoot().getInfoCustomizationsSetupFactory();
				}

				@Override
				protected InfoCustomizations accessInfoCustomizations() {
					return getRoot().getInfoCustomizations();
				}
			};
		}

		protected IInfoProxyFactory getSubInfoCustomizationsFactory() {
			return new InfoCustomizationsFactory(this) {

				@Override
				public String getIdentifier() {
					return "SubInfoCustomizationsFactory [of=" + SubCustomizedUI.this.toString() + "]";
				}

				@Override
				protected IInfoProxyFactory getInfoCustomizationsSetupFactory() {
					return SubCustomizedUI.this.getInfoCustomizationsSetupFactory();
				}

				@Override
				protected InfoCustomizations accessInfoCustomizations() {
					return SubCustomizedUI.this.getInfoCustomizations();
				}

			};
		}

		@Override
		public String toString() {
			return "SubCustomizedUI [of=" + parent + ", switchIdentifier=" + switchIdentifier + "]";
		}

	}

	protected class SubSwingCustomizer extends SwingCustomizer {

		public SubSwingCustomizer(String switchIdentifier) {
			super((switchIdentifier == SWITCH_TO_MAIN_CUSTOMIZER) ? MultiSwingCustomizer.this.getReflectionUI()
					: createSubCustomizedUI(switchIdentifier),
					MultiSwingCustomizer.this.getSubInfoCustomizationsOutputFilePath(switchIdentifier));
		}

		protected CustomizingForm subCreateForm(Object object, IInfoFilter infoFilter) {
			return super.createForm(object, infoFilter);
		}

		@Override
		public final CustomizingForm createForm(Object object, IInfoFilter infoFilter) {
			return switchSubCustomizer(this, object).subCreateForm(object, infoFilter);
		}

		public boolean isSubRenderedForm(Component c) {
			return super.isRenderedForm(c);
		}

		@Override
		public boolean isRenderedForm(Component c) {
			return MultiSwingCustomizer.this.isRenderedForm(c);
		}

		@Override
		protected void synchronizeInfoCustomizationsWithFile(String filePath) {
			File file = new File(filePath);
			if (!file.exists()) {
				return;
			}
			super.synchronizeInfoCustomizationsWithFile(filePath);
		}

	}

}
