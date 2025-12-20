
package xy.reflect.ui.control.swing.customizer;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.RenderingContext;
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
 * A distribution function ({@link #customizationsIdentifierSelector}) must
 * therefore be provided. This function is used to identify from each rendered
 * object and sub-object, a switch that selects the {@link SubSwingCustomizer}
 * (and its associated customizations) to use in order to generate the UI
 * components. This {@link SubSwingCustomizer} is then used to recursively
 * render the object hierarchy until a new switch associated with a different
 * {@link SubSwingCustomizer} is returned by the distribution function.
 * 
 * Note that the renderer associated with the
 * {@link #GLOBAL_EXCLUSIVE_CUSTOMIZATIONS} identifier is used by default, and
 * that as long as the distribution function returns null, the renderer used
 * remains the same.
 * 
 * @author olitank
 *
 */
public class MultiSwingCustomizer extends SwingRenderer {

	/**
	 * Identifier that can be returned by the
	 * {@link #customizationsIdentifierSelector} function in order to apply
	 * exclusively the global {@link InfoCustomizations} during the rendering
	 * process.
	 */
	public static final String GLOBAL_EXCLUSIVE_CUSTOMIZATIONS = MultiSwingCustomizer.class.getName()
			+ ".GLOBAL_EXCLUSIVE_CUSTOMIZATIONS";

	protected String globalInfoCustomizationsOutputFilePath;
	protected Function<Object, String> customizationsIdentifierSelector;

	protected Map<SubSwingCustomizer, String> identifierBySubCustomizer = new WeakHashMap<SubSwingCustomizer, String>();

	/**
	 * The constructor.
	 * 
	 * @param globalInfoCustomizationsOutputFilePath The path to the main global
	 *                                               {@link SubSwingCustomizer}
	 *                                               {@link InfoCustomizations}
	 *                                               file.
	 * @param customizationsIdentifierSelector       The object -&gt;
	 *                                               {@link SubSwingCustomizer}
	 *                                               distribution function.
	 */
	public MultiSwingCustomizer(String globalInfoCustomizationsOutputFilePath,
			Function<Object, String> customizationsIdentifierSelector) {
		super(null);
		reflectionUI = createSubCustomizedUI(null);
		this.globalInfoCustomizationsOutputFilePath = globalInfoCustomizationsOutputFilePath;
		this.customizationsIdentifierSelector = customizationsIdentifierSelector;
	}

	/**
	 * @param customizationsIdentifier A customizations identifier.
	 * @return The path to the {@link SubSwingCustomizer} {@link InfoCustomizations}
	 *         file associated with the given customizations identifier.
	 */
	public String getInfoCustomizationsOutputFilePath(String customizationsIdentifier) {
		File mainInfoCustomizationsOutputFile = new File(globalInfoCustomizationsOutputFilePath);
		String fileNamePrefix = (customizationsIdentifier == GLOBAL_EXCLUSIVE_CUSTOMIZATIONS) ? ""
				: (customizationsIdentifier + "-");
		return new File(mainInfoCustomizationsOutputFile.getParentFile(),
				fileNamePrefix + mainInfoCustomizationsOutputFile.getName()).getPath();
	}

	@Override
	public SubCustomizedUI getReflectionUI() {
		return (SubCustomizedUI) super.getReflectionUI();
	}

	/**
	 * @return The current map of customizations identifiers (returned by
	 *         {@link #customizationsIdentifierSelector}) and their associated
	 *         {@link SubSwingCustomizer}. Note that the available entries depend on
	 *         the previously rendered objects. Unused entries may get
	 *         garbage-collected and then removed over time.
	 */
	public Map<String, SubSwingCustomizer> getSubCustomizerByIdentifier() {
		return Collections.unmodifiableMap(identifierBySubCustomizer.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (value1, value2) -> value1)));
	}

	protected SubSwingCustomizer switchSubCustomizer(SubSwingCustomizer current, Object object) {
		String customizationsIdentifier = customizationsIdentifierSelector.apply(object);
		if (customizationsIdentifier == null) {
			if (current != null) {
				return current;
			} else {
				customizationsIdentifier = GLOBAL_EXCLUSIVE_CUSTOMIZATIONS;
			}
		}
		return obtainSubCustomizer(customizationsIdentifier);
	}

	/**
	 * @param customizationsIdentifier A customizations identifier.
	 * @return The {@link SubSwingCustomizer} instance associated with the given
	 *         customizations identifier. Note that if this instance does not
	 *         exists, it will then be created.
	 */
	public SubSwingCustomizer obtainSubCustomizer(String customizationsIdentifier) {
		SubSwingCustomizer result = getSubCustomizerByIdentifier().get(customizationsIdentifier);
		if (result == null) {
			result = createSubCustomizer(customizationsIdentifier);
			identifierBySubCustomizer.put(result, customizationsIdentifier);
		}
		return result;
	}

	protected SubSwingCustomizer createSubCustomizer(String customizationsIdentifier) {
		return new SubSwingCustomizer(customizationsIdentifier);
	}

	protected SubCustomizedUI createSubCustomizedUI(String customizationsIdentifier) {
		return new SubCustomizedUI(customizationsIdentifier);
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
		for (SubSwingCustomizer subCustomizer : identifierBySubCustomizer.keySet()) {
			if (subCustomizer.isSubRenderedForm(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This is a subclass of {@link CustomizedUI} that holds (for its parent
	 * {@link MultiSwingCustomizer}) the {@link InfoCustomizations} associated with
	 * a specific customizations identifier.
	 * 
	 * @author olitank
	 *
	 */
	public class SubCustomizedUI extends CustomizedUI {

		protected String customizationsIdentifier;

		public SubCustomizedUI(String customizationsIdentifier) {
			this.customizationsIdentifier = customizationsIdentifier;
		}

		public MultiSwingCustomizer getParent() {
			return MultiSwingCustomizer.this;
		}

		public String getCustomizationsIdentifier() {
			return customizationsIdentifier;
		}

		protected SubCustomizedUI getGlobalSubCustomizedUI() {
			return getParent().getReflectionUI();
		}

		@Override
		public ThreadLocal<RenderingContext> getThreadLocalRenderingContext() {
			if (getGlobalSubCustomizedUI() == this) {
				return super.getThreadLocalRenderingContext();
			}
			return getGlobalSubCustomizedUI().getThreadLocalRenderingContext();
		}

		@Override
		public InfoProxyFactoryChain createInfoCustomizationsFactory() {
			return new InfoProxyFactoryChain(getMainInfoCustomizationsFactory(), getSubInfoCustomizationsFactory());

		}

		protected IInfoProxyFactory getMainInfoCustomizationsFactory() {
			if (getGlobalSubCustomizedUI() == this) {
				return IInfoProxyFactory.NULL_INFO_PROXY_FACTORY;
			}
			return new InfoCustomizationsFactory(this) {

				@Override
				public String getIdentifier() {
					return "MainCustomizationsFactory [of=" + customizationsIdentifier + "]";
				}

				@Override
				protected IInfoProxyFactory getInfoCustomizationsSetupFactory() {
					return getGlobalSubCustomizedUI().getInfoCustomizationsSetupFactory();
				}

				@Override
				protected InfoCustomizations accessInfoCustomizations() {
					return getGlobalSubCustomizedUI().getInfoCustomizations();
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
			return "SubCustomizedUI [of=" + getParent() + ", customizationsIdentifier=" + customizationsIdentifier
					+ "]";
		}

	}

	/**
	 * This class is a subclass of {@link SwingCustomizer} that manages (for its
	 * parent {@link MultiSwingCustomizer}) the {@link InfoCustomizations}
	 * associated with a specific switch identifier returned by the
	 * {@link MultiSwingCustomizer#customizationsIdentifierSelector} function.
	 * 
	 * @author olitank
	 *
	 */
	public class SubSwingCustomizer extends SwingCustomizer {

		public SubSwingCustomizer(String customizationsIdentifier) {
			super((customizationsIdentifier == GLOBAL_EXCLUSIVE_CUSTOMIZATIONS)
					? MultiSwingCustomizer.this.getReflectionUI()
					: createSubCustomizedUI(customizationsIdentifier),
					MultiSwingCustomizer.this.getInfoCustomizationsOutputFilePath(customizationsIdentifier));
		}

		@Override
		public SubCustomizedUI getCustomizedUI() {
			return (SubCustomizedUI) super.getCustomizedUI();
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
				getReflectionUI().logDebug(
						"WARNING: " + InfoCustomizations.class.getSimpleName() + " file not found: " + filePath);
				return;
			}
			super.synchronizeInfoCustomizationsWithFile(filePath);
		}

	}

}
