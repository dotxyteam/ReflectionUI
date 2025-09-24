
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

/**
 * This class is a subclass of {@link SwingRenderer} that manages and delegates
 * its essential functions (mainly {@link SwingRenderer#createForm(Object)}) to
 * multiple internal instances of {@link SwingCustomizer} (actually
 * {@link SubSwingCustomizer}). It allows to distribute
 * {@link InfoCustomizations} into separate files that can be edited
 * independently.
 * 
 * During the rendering process, global {@link InfoCustomizations} are applied
 * before eventual specific {@link InfoCustomizations} provided by a
 * {@link SubSwingCustomizer} gets applied.
 * 
 * A distribution function ({@link #subCustomizationsSwitchSelector}) must
 * therefore be provided. This function is used to identify from each rendered
 * object and sub-object, a switch that selects the {@link SubSwingCustomizer}
 * to use in order to generate the UI components. This
 * {@link SubSwingCustomizer} is then used to recursively render the object
 * hierarchy until a new switch associated with a different
 * {@link SubSwingCustomizer} is returned by the distribution function. Note
 * that as long as the distribution function returns null, the renderer used
 * remains the same.
 * 
 * @author olitank
 *
 */
public class MultiSwingCustomizer extends SwingRenderer {

	/**
	 * Identifier that can be returned by the
	 * {@link #subCustomizationsSwitchSelector} function in order to apply
	 * exclusively the global {@link InfoCustomizations} during the rendering
	 * process.
	 */
	public static final String SWITCH_TO_GLOBAL_EXCLUSIVE_CUSTOMIZATIONS = MultiSwingCustomizer.class.getName()
			+ ".SWITCH_TO_GLOBAL_EXCLUSIVE_CUSTOMIZATIONS";

	protected String globalInfoCustomizationsOutputFilePath;
	protected Function<Object, String> subCustomizationsSwitchSelector;

	protected Map<SubSwingCustomizer, String> switchBySubCustomizer = new WeakHashMap<SubSwingCustomizer, String>();

	/**
	 * The constructor.
	 * 
	 * @param globalInfoCustomizationsOutputFilePath The path to the main global
	 *                                               {@link SubSwingCustomizer}
	 *                                               {@link InfoCustomizations}
	 *                                               file.
	 * @param subCustomizationsSwitchSelector        The object ->
	 *                                               {@link SubSwingCustomizer}
	 *                                               distribution function.
	 */
	public MultiSwingCustomizer(String globalInfoCustomizationsOutputFilePath,
			Function<Object, String> subCustomizationsSwitchSelector) {
		super(null);
		reflectionUI = createSubCustomizedUI(null);
		this.globalInfoCustomizationsOutputFilePath = globalInfoCustomizationsOutputFilePath;
		this.subCustomizationsSwitchSelector = subCustomizationsSwitchSelector;
	}

	/**
	 * @return The path to the main global {@link SubSwingCustomizer}
	 *         {@link InfoCustomizations} file.
	 */
	public String getGlobalInfoCustomizationsOutputFilePath() {
		return globalInfoCustomizationsOutputFilePath;
	}

	protected String getSubInfoCustomizationsOutputFilePath(String switchIdentifier) {
		File mainInfoCustomizationsOutputFile = new File(globalInfoCustomizationsOutputFilePath);
		String fileNamePrefix = (switchIdentifier == SWITCH_TO_GLOBAL_EXCLUSIVE_CUSTOMIZATIONS) ? ""
				: "-" + switchIdentifier;
		return new File(mainInfoCustomizationsOutputFile.getParentFile(),
				fileNamePrefix + mainInfoCustomizationsOutputFile.getName()).getPath();
	}

	@Override
	public SubCustomizedUI getReflectionUI() {
		return (SubCustomizedUI) super.getReflectionUI();
	}

	/**
	 * @return The current map of switch identifiers (returned by
	 *         {@link #subCustomizationsSwitchSelector}) and their associated
	 *         {@link SubSwingCustomizer}. Note that the available entries depend on
	 *         the previously rendered objects. Unused entries may get
	 *         garbage-collected and then removed over time.
	 */
	public Map<String, SubSwingCustomizer> getSubCustomizerBySwitch() {
		return Collections.unmodifiableMap(switchBySubCustomizer.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (value1, value2) -> value1)));
	}

	protected SubSwingCustomizer switchSubCustomizer(SubSwingCustomizer current, Object object) {
		String switchIdentifier = subCustomizationsSwitchSelector.apply(object);
		if (switchIdentifier == null) {
			if (current != null) {
				return current;
			} else {
				switchIdentifier = SWITCH_TO_GLOBAL_EXCLUSIVE_CUSTOMIZATIONS;
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
		for (SubSwingCustomizer subCustomizer : switchBySubCustomizer.keySet()) {
			if (subCustomizer.isSubRenderedForm(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This is a subclass of {@link CustomizedUI} that holds (for its parent
	 * {@link MultiSwingCustomizer}) the {@link InfoCustomizations} associated with
	 * a specific {@link SubSwingCustomizer} switch identifier.
	 * 
	 * @author olitank
	 *
	 */
	public class SubCustomizedUI extends CustomizedUI {

		protected String switchIdentifier;

		public SubCustomizedUI(String switchIdentifier) {
			this.switchIdentifier = switchIdentifier;
		}

		public MultiSwingCustomizer getParent() {
			return MultiSwingCustomizer.this;
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
			return "SubCustomizedUI [of=" + getParent() + ", switchIdentifier=" + switchIdentifier + "]";
		}

	}

	/**
	 * This class is a subclass of {@link SwingCustomizer} that manages (for its
	 * parent {@link MultiSwingCustomizer}) the {@link InfoCustomizations}
	 * associated with a specific switch identifier returned by
	 * {@link MultiSwingCustomizer#subCustomizationsSwitchSelector}.
	 * 
	 * @author olitank
	 *
	 */
	public class SubSwingCustomizer extends SwingCustomizer {

		public SubSwingCustomizer(String switchIdentifier) {
			super((switchIdentifier == SWITCH_TO_GLOBAL_EXCLUSIVE_CUSTOMIZATIONS)
					? MultiSwingCustomizer.this.getReflectionUI()
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
