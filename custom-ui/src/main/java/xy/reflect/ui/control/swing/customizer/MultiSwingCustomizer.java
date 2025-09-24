
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
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.info.type.factory.InfoProxyFactoryChain;

public class MultiSwingCustomizer extends SwingRenderer {

	public static final String SWITCH_TO_MAIN_CUSTOMIZER = MultiSwingCustomizer.class.getName()
			+ ".SWITCH_TO_MAIN_CUSTOMIZER";

	protected String infoCustomizationsOutputFilePathPrefix;
	protected Function<Object, String> subCustomizationsSwitchSelector;

	protected Map<SubSwingCustomizer, String> switchBySubCustomizer = new WeakHashMap<SubSwingCustomizer, String>();

	public MultiSwingCustomizer(CustomizedUI customizedUI, String infoCustomizationsOutputFilePathPrefix,
			Function<Object, String> subCustomizationsSwitchSelector) {
		super(customizedUI);
		this.infoCustomizationsOutputFilePathPrefix = infoCustomizationsOutputFilePathPrefix;
		this.subCustomizationsSwitchSelector = subCustomizationsSwitchSelector;
	}

	@Override
	public CustomizedUI getReflectionUI() {
		return (CustomizedUI) super.getReflectionUI();
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

	protected String getSubInfoCustomizationsOutputFilePath(String switchIdentifier) {
		return (MultiSwingCustomizer.this.infoCustomizationsOutputFilePathPrefix != null)
				? (MultiSwingCustomizer.this.infoCustomizationsOutputFilePathPrefix
						+ ((switchIdentifier == SWITCH_TO_MAIN_CUSTOMIZER) ? "" : "-" + switchIdentifier))
				: null;
	}

	protected CustomizedUI obtainSubCustomizerUI(String switchIdentifier) {
		return (switchIdentifier == SWITCH_TO_MAIN_CUSTOMIZER) ? MultiSwingCustomizer.this.getReflectionUI()
				: createSubCustomizedUI(switchIdentifier);
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

	protected class SubSwingCustomizer extends SwingCustomizer {

		public SubSwingCustomizer(String switchIdentifier) {
			super(MultiSwingCustomizer.this.obtainSubCustomizerUI(switchIdentifier),
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

		@Override
		public ITypeInfo getTypeInfoAfterCustomizations(ITypeInfo type) {
			return parent.getReflectionUI().getTypeInfoAfterCustomizations(type);
		}

		@Override
		public ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
			return parent.getReflectionUI().getTypeInfoBeforeCustomizations(type);
		}

		@Override
		public IApplicationInfo getApplicationInfoAfterCustomizations(IApplicationInfo appInfo) {
			return parent.getReflectionUI().getApplicationInfoAfterCustomizations(appInfo);
		}

		@Override
		public IApplicationInfo getApplicationInfoBeforeCustomizations(IApplicationInfo appInfo) {
			return parent.getReflectionUI().getApplicationInfoBeforeCustomizations(appInfo);
		}

		@Override
		public InfoProxyFactoryChain getInfoCustomizationsFactory() {
			return new InfoProxyFactoryChain(parent.getReflectionUI().getInfoCustomizationsFactory(),
					getSubInfoCustomizationsFactory());

		}

		protected IInfoProxyFactory getSubInfoCustomizationsFactory() {
			return super.getInfoCustomizationsFactory();
		}

		@Override
		public String toString() {
			return "SubCustomizedUI [of=" + parent + ", switchIdentifier=" + switchIdentifier + "]";
		}

	}

}
