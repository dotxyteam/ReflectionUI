
package xy.reflect.ui;

import java.util.Map;

import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldTypeSpecificities;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizationsFactory;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * This is a subclass of {@link ReflectionUI} that adapts its introspection
 * mechanics according to the given {@link InfoCustomizations} instance.
 * 
 * @author olitank
 *
 */
public class CustomizedUI extends ReflectionUI {

	protected static CustomizedUI defaultInstance;

	protected InfoCustomizations infoCustomizations;

	protected Map<ITypeInfo, ITypeInfo> customizedTypesCache = MiscUtils.newWeakValuesEqualityBasedMap();
	protected Object customizedTypesCacheMutex = new Object();

	/**
	 * @return the default instance of this class. This instance is constructed with
	 *         the {@link InfoCustomizations#getDefault()} return value.
	 */
	public static CustomizedUI getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new CustomizedUI(InfoCustomizations.getDefault());
		}
		return defaultInstance;
	}

	/**
	 * Constructs an instance of this class that will use the given customizations.
	 * 
	 * @param infoCustomizations The abstract UI model customizations specification
	 *                           object.
	 */
	public CustomizedUI(InfoCustomizations infoCustomizations) {
		this.infoCustomizations = infoCustomizations;
	}

	/**
	 * Constructs an instance of this class with empty customizations.
	 */
	public CustomizedUI() {
		this(new InfoCustomizations());
	}

	/**
	 * @return the cache that maps {@link ITypeInfo} non-customized instances to
	 *         customized instances.
	 */
	public Map<ITypeInfo, ITypeInfo> getCustomizedTypesCache() {
		return customizedTypesCache;
	}

	/**
	 * @return the abstract UI model customizations specification.
	 */
	public InfoCustomizations getInfoCustomizations() {
		return infoCustomizations;
	}

	@Override
	public final ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		ITypeInfo result = super.getTypeInfo(typeSource);
		synchronized (customizedTypesCacheMutex) {
			ITypeInfo customizedTypesCacheKey = result;
			ITypeInfo cachedResult = customizedTypesCache.get(customizedTypesCacheKey);
			if (cachedResult != null) {
				result = cachedResult;
			} else {
				result = getInfoCustomizationsSetupFactory().wrapTypeInfo(result);
				result = getTypeInfoBeforeCustomizations(result);
				result = getInfoCustomizationsFactory().wrapTypeInfo(result);
				SpecificitiesIdentifier specificitiesIdentifier = typeSource.getSpecificitiesIdentifier();
				if (specificitiesIdentifier != null) {
					result = getSpecificitiesFactory(specificitiesIdentifier).wrapTypeInfo(result);
				}
				result = getTypeInfoAfterCustomizations(result);
				customizedTypesCache.put(customizedTypesCacheKey, result);
			}
		}
		return result;
	}

	@Override
	public IApplicationInfo getApplicationInfo() {
		IApplicationInfo result = super.getApplicationInfo();
		result = getInfoCustomizationsSetupFactory().wrapApplicationInfo(result);
		result = getApplicationInfoBeforeCustomizations(result);
		result = getInfoCustomizationsFactory().wrapApplicationInfo(result);
		result = getApplicationInfoAfterCustomizations(result);
		return result;
	}

	/**
	 * @return the UI model proxy factory that will be used to provide specific
	 *         customizations (e.g.: specific to a field) for {@link ITypeInfo}
	 *         instances. This factory would be used after the one returned by
	 *         {@link #getInfoCustomizationsFactory()}.
	 */
	public InfoProxyFactory getSpecificitiesFactory(final SpecificitiesIdentifier specificitiesIdentifier) {
		return new InfoCustomizationsFactory(this) {
			@Override
			public String getIdentifier() {
				return "SpecificitiesFactory [of=" + CustomizedUI.this.toString() + ", specificitiesIdentifier="
						+ specificitiesIdentifier.toString() + "]";
			}

			@Override
			public InfoCustomizations getInfoCustomizations() {
				TypeCustomization typeCustomization = InfoCustomizations.getTypeCustomization(infoCustomizations,
						specificitiesIdentifier.getObjectTypeName());
				FieldCustomization fieldCustomization = InfoCustomizations.getFieldCustomization(typeCustomization,
						specificitiesIdentifier.getFieldName());
				FieldTypeSpecificities result = fieldCustomization.getSpecificTypeCustomizations();
				return result;
			}
		};
	}

	/**
	 * @return the UI model proxy factory that will be used to customize every UI
	 *         model. This factory will be used after calling
	 *         {@link #getTypeInfoBeforeCustomizations(ITypeInfo)} |
	 *         {@link #getApplicationInfoBeforeCustomizations(IApplicationInfo)} and
	 *         before calling {@link #getTypeInfoAfterCustomizations(ITypeInfo)} |
	 *         {@link #getApplicationInfoAfterCustomizations(IApplicationInfo)}.
	 */
	public InfoProxyFactory getInfoCustomizationsFactory() {
		return new InfoCustomizationsFactory(this) {

			@Override
			public String getIdentifier() {
				return "CustomizationsFactory [of=" + CustomizedUI.this.toString() + "]";
			}

			@Override
			public InfoCustomizations getInfoCustomizations() {
				return infoCustomizations;
			}
		};
	}

	/**
	 * @return the UI model proxy factory that will be used to prepare every UI
	 *         model for customizations. This factory will be used before calling
	 *         {@link #getApplicationInfoBeforeCustomizations(IApplicationInfo)}.
	 */
	public InfoProxyFactory getInfoCustomizationsSetupFactory() {
		return new InfoProxyFactory() {

			@Override
			public String getIdentifier() {
				return "CustomizationsSetupFactory [of=" + CustomizedUI.this.toString() + "]";
			}

			@Override
			protected ITypeInfo getType(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
				ITypeInfo result = param.getType();
				ITypeInfoSource source = result.getSource();
				if (source.getSpecificitiesIdentifier() != null) {
					throw new ReflectionUIError(
							"Invalid parameter type info: unexpected source specificities identifier: "
									+ source.getSpecificitiesIdentifier() + ", null value expected" + "\n"
									+ "parameter=" + param.getName() + ", method=" + method.getSignature()
									+ ", objectType=" + objectType.getName() + ", typeInfoSource=" + source);
				}
				return result;
			}

			@Override
			protected ITypeInfo getType(IFieldInfo field, ITypeInfo objectType) {
				ITypeInfo result = field.getType();
				ITypeInfoSource source = result.getSource();
				SpecificitiesIdentifier expectedSpecificitiesIdentifier = new SpecificitiesIdentifier(
						objectType.getName(), field.getName());
				if (!expectedSpecificitiesIdentifier.equals(source.getSpecificitiesIdentifier())) {
					throw new ReflectionUIError("Invalid field type info: unexpected source specificities identifier: "
							+ source.getSpecificitiesIdentifier() + ", expected: " + expectedSpecificitiesIdentifier
							+ "\n" + "typeInfoSource=" + source);
				}
				return result;
			}

			@Override
			protected ITypeInfo getReturnValueType(IMethodInfo method, ITypeInfo objectType) {
				ITypeInfo result = method.getReturnValueType();
				if (result == null) {
					return null;
				}
				ITypeInfoSource source = result.getSource();
				if (source.getSpecificitiesIdentifier() != null) {
					throw new ReflectionUIError("Invalid method type info: unexpected source specificities identifier: "
							+ source.getSpecificitiesIdentifier() + ", null value expected" + "\n" + "method="
							+ method.getName() + ", objectType=" + objectType.getName() + ", typeInfoSource=" + source);
				}
				return result;
			}

		};
	}

	/**
	 * This method allows to alter the given {@link ITypeInfo} object after applying
	 * the declarative customizations.
	 * 
	 * @param type The UI-oriented type information.
	 * @return a potentially proxied version of the input argument.
	 */
	protected ITypeInfo getTypeInfoAfterCustomizations(ITypeInfo type) {
		return type;
	}

	/**
	 * This method allows to alter the given {@link ITypeInfo} object before
	 * applying the declarative customizations. Note that the virtual types
	 * generated by the customizations can also be customized and thus altered by
	 * this method.
	 * 
	 * @param type The UI-oriented type information.
	 * @return a potentially proxied version of the input argument.
	 */
	protected ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
		return type;
	}

	/**
	 * This method allows to alter the given {@link IApplicationInfo} object after
	 * applying the declarative customizations.
	 * 
	 * @param appInfo The UI-oriented application information.
	 * @return a potentially proxied version of the input argument.
	 */
	protected IApplicationInfo getApplicationInfoAfterCustomizations(IApplicationInfo appInfo) {
		return appInfo;
	}

	/**
	 * This method allows to alter the given {@link IApplicationInfo} object before
	 * applying the declarative customizations.
	 * 
	 * @param appInfo The UI-oriented application information.
	 * @return a potentially proxied version of the input argument.
	 */
	protected IApplicationInfo getApplicationInfoBeforeCustomizations(IApplicationInfo appInfo) {
		return appInfo;
	}

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "CustomizedUI.DEFAULT";
		} else {
			return super.toString();
		}
	}

}
