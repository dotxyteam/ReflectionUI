
package xy.reflect.ui.control.swing.customizer;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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

	protected Map<String, SubSwingCustomizer> subCustomizerBySwitch = new HashMap<String, SubSwingCustomizer>();

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

	public Map<String, ? extends SwingCustomizer> getSubCustomizerBySwitch() {
		return subCustomizerBySwitch;
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
		SubSwingCustomizer result = subCustomizerBySwitch.get(switchIdentifier);
		if (result == null) {
			result = createSubCustomizer(switchIdentifier);
			subCustomizerBySwitch.put(switchIdentifier, result);
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
		return new SubCustomizedUI(switchIdentifier);
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
		for (SubSwingCustomizer subCustomizer : subCustomizerBySwitch.values()) {
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

	}

	protected class SubCustomizedUI extends CustomizedUI {

		protected String switchIdentifier;

		public SubCustomizedUI(String switchIdentifier) {
			this.switchIdentifier = switchIdentifier;
		}

		@Override
		public ITypeInfo getTypeInfoAfterCustomizations(ITypeInfo type) {
			return MultiSwingCustomizer.this.getReflectionUI().getTypeInfoAfterCustomizations(type);
		}

		@Override
		public ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
			return MultiSwingCustomizer.this.getReflectionUI().getTypeInfoBeforeCustomizations(type);
		}

		@Override
		public IApplicationInfo getApplicationInfoAfterCustomizations(IApplicationInfo appInfo) {
			return MultiSwingCustomizer.this.getReflectionUI().getApplicationInfoAfterCustomizations(appInfo);
		}

		@Override
		public IApplicationInfo getApplicationInfoBeforeCustomizations(IApplicationInfo appInfo) {
			return MultiSwingCustomizer.this.getReflectionUI().getApplicationInfoBeforeCustomizations(appInfo);
		}

		@Override
		public InfoProxyFactoryChain getInfoCustomizationsFactory() {
			return new InfoProxyFactoryChain(MultiSwingCustomizer.this.getReflectionUI().getInfoCustomizationsFactory(),
					getSubInfoCustomizationsFactory());

		}

		protected IInfoProxyFactory getSubInfoCustomizationsFactory() {
			return super.getInfoCustomizationsFactory();
		}

		@Override
		public String toString() {
			return "SubCustomizedUI [of=" + MultiSwingCustomizer.this + ", switchIdentifier=" + switchIdentifier + "]";
		}

	}

}
