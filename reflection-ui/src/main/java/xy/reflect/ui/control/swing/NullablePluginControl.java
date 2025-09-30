package xy.reflect.ui.control.swing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.RejectedFieldControlInputException;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.builder.AbstractEditorFormBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that adds a "nullable" facet (allowing to display/set/unset the
 * null value) to field plugin controls that don't have it.
 * 
 * @author olitank
 *
 */
public class NullablePluginControl extends NullableControl {

	private static final long serialVersionUID = 1L;

	protected IFieldControlPlugin plugin;
	protected Serializable pluginConfiguration;
	protected Map<String, Object> pluginProperties;

	public NullablePluginControl(SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, input);
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			retrievePluginProperties();
		}
		return super.refreshUI(refreshStructure);
	}

	protected void retrievePluginProperties() {
		plugin = SwingRendererUtils.getCurrentFieldControlPlugin(swingRenderer, data.getType().getSpecificProperties(),
				input);
		if (plugin == null) {
			throw new RejectedFieldControlInputException();
		}
		pluginConfiguration = ReflectionUIUtils
				.getFieldControlPluginConfiguration(data.getType().getSpecificProperties(), plugin.getIdentifier());
		pluginProperties = new HashMap<String, Object>();
		ReflectionUIUtils.updateFieldControlPluginValues(pluginProperties, plugin.getIdentifier(), pluginConfiguration);
		ReflectionUIUtils.setFieldControlPluginManagementDisabled(pluginProperties, true);
	}

	@Override
	protected AbstractEditorFormBuilder createSubFormBuilder(SwingRenderer swingRenderer, IFieldControlInput input,
			IContext subContext, Listener<Throwable> commitExceptionHandler) {
		return new PluginControlContainerBuilder(swingRenderer, this, input, subContext, commitExceptionHandler);
	}

	protected class PluginControlContainerBuilder extends SubFormBuilder {

		public PluginControlContainerBuilder(SwingRenderer swingRenderer, NullableControl ownerComponent,
				IFieldControlInput input, IContext subContext, Listener<Throwable> commitExceptionHandler) {
			super(swingRenderer, ownerComponent, input, subContext, commitExceptionHandler);
		}

		@Override
		protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
			return new TypeInfoSourceProxy(super.getEncapsulatedFieldDeclaredTypeSource()) {

				@Override
				protected String getTypeInfoProxyFactoryIdentifier() {
					return "FieldControlPluginSettingsCopyingTypeInfoProxyFactory [pluginIdentifier="
							+ plugin.getIdentifier() + ", parentContext=" + getContext().getIdentifier() + "]";
				}

				@Override
				public ITypeInfo buildTypeInfo(ReflectionUI reflectionUI) {
					return new InfoProxyFactory() {

						@Override
						protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
							Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
							result.putAll(pluginProperties);
							return result;
						}
					}.wrapTypeInfo(super.buildTypeInfo(reflectionUI));
				}

			};
		}
	}
}