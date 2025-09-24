package xy.reflect.ui.info.type.source;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIUtils;

public class NewFieldControlPluginTypeInfoSourceProxy extends TypeInfoSourceProxy {

	protected String pluginIdentifier;
	protected Serializable pluginConfiguration;
	protected boolean pluginManagementDisabled;
	protected String typeInfoProxyFactoryIdentifier;

	public NewFieldControlPluginTypeInfoSourceProxy(ITypeInfoSource base, String pluginIdentifier,
			Serializable pluginConfiguration, boolean pluginManagementDisabled, String typeInfoProxyFactoryIdentifier) {
		super(base);
		this.pluginIdentifier = pluginIdentifier;
		this.pluginConfiguration = pluginConfiguration;
		this.pluginManagementDisabled = pluginManagementDisabled;
		this.typeInfoProxyFactoryIdentifier = typeInfoProxyFactoryIdentifier;
	}

	@Override
	protected String getTypeInfoProxyFactoryIdentifier() {
		return typeInfoProxyFactoryIdentifier;
	}

	@Override
	protected InfoProxyFactory createTypeInfoProxyFactory() {
		return new TypeInfoProxyFactory() {
			@Override
			protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
				Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
				ReflectionUIUtils.updateFieldControlPluginValues(result, pluginIdentifier, pluginConfiguration);
				ReflectionUIUtils.setFieldControlPluginManagementDisabled(result, true);
				return result;
			}
		};
	}
}