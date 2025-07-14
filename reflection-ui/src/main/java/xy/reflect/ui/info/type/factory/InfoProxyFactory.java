
package xy.reflect.ui.info.type.factory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfoProxy;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransaction;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.CategoriesStyle;
import xy.reflect.ui.info.type.ITypeInfo.FieldsLayout;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
import xy.reflect.ui.info.type.ITypeInfo.MethodsLayout;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo.ItemCreationMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo.ToolsLocation;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.map.IMapEntryTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.info.method.InvocationData;

/**
 * This is the default implementation of abstract UI model transformers.
 * 
 * By default this class generates proxies that behave exactly like the wrapped
 * objects. In order to change the generated UI behavior, the appropriate
 * methods of this class must be overridden.
 * 
 * By convention these methods have the same name as the methods of the
 * underlying abstract UI model elements that they affect. In addition, they
 * have parameters that specify the invocation context.
 * 
 * Ex: Overriding {@link #getCaption(IFieldInfo, ITypeInfo)} allows to change
 * the behavior of {@link IFieldInfo#getCaption()} according to the parent
 * {@link ITypeInfo} instance.
 * 
 * Note: To avoid accidentally wrapping the same object with the same proxy
 * multiple times, a check is made by comparing the return values of
 * {@link #getIdentifier ()} for the factories that generated each proxy. If a
 * duplicate is found then an exception is thrown. It may then be necessary to
 * override {@link #getIdentifier ()} to distinguish between two presumed
 * identical proxies.
 * 
 * @author olitank
 *
 */
public class InfoProxyFactory implements IInfoProxyFactory {

	protected static final String ACTIVE_FACTORIES_KEY = InfoProxyFactory.class.getName() + "ACTIVE_FACTORIES";

	@SuppressWarnings("unchecked")
	public static List<InfoProxyFactory> listFactories(IApplicationInfo application) {
		return (List<InfoProxyFactory>) application.getSpecificProperties().get(ACTIVE_FACTORIES_KEY);
	}

	@SuppressWarnings("unchecked")
	public static List<InfoProxyFactory> listFactories(ITypeInfo type) {
		return (List<InfoProxyFactory>) type.getSpecificProperties().get(ACTIVE_FACTORIES_KEY);
	}

	/**
	 * @return the identifier of this proxy factory.
	 */
	public String getIdentifier() {
		return getClass().getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InfoProxyFactory other = (InfoProxyFactory) obj;
		if (getIdentifier() == null) {
			if (other.getIdentifier() != null)
				return false;
		} else if (!getIdentifier().equals(other.getIdentifier()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TypeInfoProxyFactory [id=" + getIdentifier() + "]";
	}

	@Override
	public IApplicationInfo wrapApplicationInfo(IApplicationInfo appInfo) {
		return new GeneratedApplicationInfoProxy(appInfo);
	}

	@Override
	public ITypeInfo wrapTypeInfo(final ITypeInfo type) {
		if (type instanceof IListTypeInfo) {
			return new GeneratedListTypeInfoProxy((IListTypeInfo) type);
		} else if (type instanceof IEnumerationTypeInfo) {
			return new GeneratedEnumerationTypeInfoProxy((IEnumerationTypeInfo) type);
		} else if (type instanceof IMapEntryTypeInfo) {
			return new GeneratedMapEntryTypeInfoProxy((IMapEntryTypeInfo) type);
		} else {
			return new GeneratedBasicTypeInfoProxy(type);
		}
	}

	/**
	 * @param type
	 * @return the given the information (unless overridden).
	 */
	protected ITypeInfo wrapSubTypeInfo(ITypeInfo type) {
		return type;
	}

	/**
	 * @param type
	 * @return the given the information (unless overridden).
	 */
	protected ITypeInfo wrapItemTypeInfo(ITypeInfo type) {
		return type;
	}

	/**
	 * @param type
	 * @return the given the information (unless overridden).
	 */
	protected ITypeInfo wrapMethodReturnValueTypeInfo(ITypeInfo type) {
		return type;
	}

	/**
	 * @param type
	 * @return the given the information (unless overridden).
	 */
	protected ITypeInfo wrapParameterTypeInfo(ITypeInfo type) {
		return type;
	}

	/**
	 * @param type
	 * @return the given the information (unless overridden).
	 */
	protected ITypeInfo wrapFieldTypeInfo(ITypeInfo type) {
		return type;
	}

	/**
	 * @param field      The field information for which a proxy must be generated.
	 * @param objectType The parent object type information.
	 * @return a proxy of the given field information.
	 */
	public IFieldInfo wrapFieldInfo(IFieldInfo field, ITypeInfo objectType) {
		return new GeneratedFieldInfoProxy(field, objectType);
	}

	/**
	 * @param param      The parameter information for which a proxy must be
	 *                   generated.
	 * @param method     The parent method information.
	 * @param objectType The parent object type information.
	 * @return a proxy of the given parameter information.
	 */
	public IParameterInfo wrapParameterInfo(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return new GeneratedParameterInfoProxy(param, method, objectType);
	}

	/**
	 * @param itemInfo       The enumeration item information for which a proxy must
	 *                       be generated.
	 * @param parentEnumType The parent enumeration type information.
	 * @return a proxy of the given enumeration item information.
	 */
	public IEnumerationItemInfo wrapEnumerationItemInfo(IEnumerationItemInfo itemInfo,
			IEnumerationTypeInfo parentEnumType) {
		return new GeneratedEnumerationItemInfoProxy(itemInfo, parentEnumType);
	}

	/**
	 * @param constructor The constructor information for which a proxy must be
	 *                    generated.
	 * @param objectType  The parent object type information.
	 * @return a proxy of the given constructor information.
	 */
	public IMethodInfo wrapConstructorInfo(IMethodInfo constructor, ITypeInfo objectType) {
		return new GeneratedConstructorInfoProxy(constructor, objectType);
	}

	/**
	 * @param method     The method information for which a proxy must be generated.
	 * @param objectType The parent object type information.
	 * @return a proxy of the given method information.
	 */
	public IMethodInfo wrapMethodInfo(IMethodInfo method, ITypeInfo objectType) {
		return new GeneratedMethodInfoProxy(method, objectType);
	}

	@Override
	public IApplicationInfo unwrapApplicationInfo(final IApplicationInfo appInfo) {
		GeneratedApplicationInfoProxy proxy = (GeneratedApplicationInfoProxy) appInfo;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	@Override
	public ITypeInfo unwrapTypeInfo(final ITypeInfo type) {
		GeneratedBasicTypeInfoProxy proxy = (GeneratedBasicTypeInfoProxy) type;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	/**
	 * @param field The field information proxy.
	 * @return the field information behind by the given proxy. Note that this proxy
	 *         must have been generated by the current factory.
	 */
	public IFieldInfo unwrapFieldInfo(final IFieldInfo field) {
		GeneratedFieldInfoProxy proxy = (GeneratedFieldInfoProxy) field;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	/**
	 * @param method The method information proxy.
	 * @return the method information behind by the given proxy. Note that this
	 *         proxy must have been generated by the current factory.
	 */
	public IMethodInfo unwrapMethodInfo(final IMethodInfo method) {
		GeneratedMethodInfoProxy proxy = (GeneratedMethodInfoProxy) method;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	/**
	 * @param param The parameter information proxy.
	 * @return the parameter information behind by the given proxy. Note that this
	 *         proxy must have been generated by the current factory.
	 */
	public IParameterInfo unwrapParameterInfo(final IParameterInfo param) {
		GeneratedParameterInfoProxy proxy = (GeneratedParameterInfoProxy) param;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	/**
	 * @param type The type information to analyze.
	 * @return whether the given type information is a proxy generated by the
	 *         current factory.
	 */
	public boolean isWrappingTypeInfo(final ITypeInfo type) {
		if (!(type instanceof GeneratedBasicTypeInfoProxy)) {
			return false;
		}
		GeneratedBasicTypeInfoProxy proxy = (GeneratedBasicTypeInfoProxy) type;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			return false;
		}
		return true;
	}

	/**
	 * @param field The field information to analyze.
	 * @return whether the given field information is a proxy generated by the
	 *         current factory.
	 */
	public boolean isWrappingFieldInfo(final IFieldInfo field) {
		if (!(field instanceof GeneratedFieldInfoProxy)) {
			return false;
		}
		GeneratedFieldInfoProxy proxy = (GeneratedFieldInfoProxy) field;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			return false;
		}
		return true;
	}

	/**
	 * @param method The method information to analyze.
	 * @return whether the given method information is a proxy generated by the
	 *         current factory.
	 */
	public boolean isWrappingMethodInfo(final IMethodInfo method) {
		if (!(method instanceof GeneratedMethodInfoProxy)) {
			return false;
		}
		GeneratedMethodInfoProxy proxy = (GeneratedMethodInfoProxy) method;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			return false;
		}
		return true;
	}

	/**
	 * @param param The parameter information to analyze.
	 * @return whether the given parameter information is a proxy generated by the
	 *         current factory.
	 */
	public boolean isWrappingParameterInfo(final IParameterInfo param) {
		if (!(param instanceof GeneratedParameterInfoProxy)) {
			return false;
		}
		GeneratedParameterInfoProxy proxy = (GeneratedParameterInfoProxy) param;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			return false;
		}
		return true;
	}

	/**
	 * @param field      The field information.
	 * @param object     Parameter of
	 *                   {@link IFieldInfo#getValueAbstractFormValidationJob(Object)}.
	 * @param objectType The parent type information.
	 * @return the result of
	 *         {@link IFieldInfo#getValueAbstractFormValidationJob(Object)}
	 *         unless overridden.
	 */
	protected IValidationJob getValueAbstractFormValidationJob(IFieldInfo field, Object object,
			ITypeInfo objectType) {
		return field.getValueAbstractFormValidationJob(object);
	}

	/**
	 * @param method      The method information.
	 * @param object      Parameter of
	 *                    {@link IMethodInfo#getReturnValueAbstractFormValidationJob(Object, Object)}.
	 * @param returnValue Parameter of
	 *                    {@link IMethodInfo#getReturnValueAbstractFormValidationJob(Object, Object)}.
	 * @param objectType  The parent type information.
	 * @return the result of
	 *         {@link IMethodInfo#getReturnValueAbstractFormValidationJob(Object, Object)}
	 *         unless overridden.
	 */
	protected IValidationJob getReturnValueAbstractFormValidationJob(IMethodInfo method, Object object,
			Object returnValue, ITypeInfo objectType) {
		return method.getReturnValueAbstractFormValidationJob(object, returnValue);
	}

	/**
	 * @param listType     The list type information.
	 * @param itemPosition Parameter of
	 *                     {@link IListTypeInfo#getListItemAbstractFormValidationJob(ItemPosition)}.
	 * @return the result of
	 *         {@link IListTypeInfo#getListItemAbstractFormValidationJob(ItemPosition)}
	 *         unless overridden.
	 */
	protected IValidationJob getListItemAbstractFormValidationJob(IListTypeInfo listType,
			ItemPosition itemPosition) {
		return listType.getListItemAbstractFormValidationJob(itemPosition);
	}

	/**
	 * @param param      The parameter information.
	 * @param object     Parameter of
	 *                   {@link IParameterInfo#getDefaultValue(Object)}.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#getDefaultValue(Object)} unless
	 *         overridden.
	 */
	protected Object getDefaultValue(IParameterInfo param, Object object, IMethodInfo method, ITypeInfo objectType) {
		return param.getDefaultValue(object);
	}

	/**
	 * @param param      The parameter information.
	 * @param object     Parameter of
	 *                   {@link IParameterInfo#hasValueOptions(Object)}.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#hasValueOptions(Object)} unless
	 *         overridden.
	 */
	protected boolean hasValueOptions(IParameterInfo param, Object object, IMethodInfo method, ITypeInfo objectType) {
		return param.hasValueOptions(object);
	}

	/**
	 * @param param      The parameter information.
	 * @param object     Parameter of
	 *                   {@link IParameterInfo#getValueOptions(Object)}.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#getValueOptions(Object)} unless
	 *         overridden.
	 */
	protected Object[] getValueOptions(IParameterInfo param, Object object, IMethodInfo method, ITypeInfo objectType) {
		return param.getValueOptions(object);
	}

	/**
	 * @param param      The parameter information.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#getPosition()} unless overridden.
	 */
	protected int getPosition(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return param.getPosition();
	}

	/**
	 * @param param      The parameter information.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#getType()} unless overridden.
	 */
	protected ITypeInfo getType(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return wrapParameterTypeInfo(param.getType());
	}

	/**
	 * @param param      The parameter information.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#isNullValueDistinct()} unless
	 *         overridden.
	 */
	protected boolean isNullValueDistinct(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return param.isNullValueDistinct();
	}

	/**
	 * @param param      The parameter information.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#getCaption()} unless overridden.
	 */
	protected String getCaption(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return param.getCaption();
	}

	/**
	 * @param param      The parameter information.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#isHidden()} unless overridden.
	 */
	protected boolean isHidden(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return param.isHidden();
	}

	/**
	 * @param param      The parameter information.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#getName()} unless overridden.
	 */
	protected String getName(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return param.getName();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getCategory()} unless overridden.
	 */
	protected InfoCategory getCategory(IFieldInfo field, ITypeInfo objectType) {
		return field.getCategory();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#wrapFieldTypeInfo()} unless
	 *         overridden.
	 */
	protected ITypeInfo getType(IFieldInfo field, ITypeInfo objectType) {
		return wrapFieldTypeInfo(field.getType());
	}

	/**
	 * @param field      The field information.
	 * @param object     Parameter of
	 *                   {@link IFieldInfo#getAlternativeConstructors(Object)}.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getAlternativeConstructors(Object)}
	 *         unless overridden.
	 */
	protected List<IMethodInfo> getAlternativeConstructors(IFieldInfo field, Object object, ITypeInfo objectType) {
		return field.getAlternativeConstructors(object);
	}

	/**
	 * @param field      The field information.
	 * @param object     Parameter of
	 *                   {@link IFieldInfo#getAlternativeListItemConstructors(Object)}.
	 * @param objectType The parent type information.
	 * @return the result of
	 *         {@link IFieldInfo#getAlternativeListItemConstructors(Object)} unless
	 *         overridden.
	 */
	protected List<IMethodInfo> getAlternativeListItemConstructors(IFieldInfo field, Object object,
			ITypeInfo objectType) {
		return field.getAlternativeListItemConstructors(object);
	}

	/**
	 * @param field      The field information.
	 * @param object     Parameter of {@link IFieldInfo#getValue(Object)}.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getValue(Object)} unless overridden.
	 */
	protected Object getValue(IFieldInfo field, Object object, ITypeInfo objectType) {
		return field.getValue(object);
	}

	/**
	 * @param field      The field information.
	 * @param object     Parameter of {@link IFieldInfo#hasValueOptions(Object)}.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#hasValueOptions(Object)} unless
	 *         overridden.
	 */
	protected boolean hasValueOptions(IFieldInfo field, Object object, ITypeInfo objectType) {
		return field.hasValueOptions(object);
	}

	/**
	 * @param field      The field information.
	 * @param object     Parameter of {@link IFieldInfo#getValueOptions(Object)}.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getValueOptions(Object)} unless
	 *         overridden.
	 */
	protected Object[] getValueOptions(IFieldInfo field, Object object, ITypeInfo objectType) {
		return field.getValueOptions(object);
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isNullValueDistinct()} unless
	 *         overridden.
	 */
	protected boolean isNullValueDistinct(IFieldInfo field, ITypeInfo objectType) {
		return field.isNullValueDistinct();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getNullValueLabel()} unless
	 *         overridden.
	 */
	protected String getNullValueLabel(IFieldInfo field, ITypeInfo objectType) {
		return field.getNullValueLabel();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isTransient()} unless overridden.
	 */
	protected boolean isTransient(IFieldInfo field, ITypeInfo objectType) {
		return field.isTransient();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isGetOnly()} unless overridden.
	 */
	protected boolean isGetOnly(IFieldInfo field, ITypeInfo objectType) {
		return field.isGetOnly();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getValueReturnMode()} unless
	 *         overridden.
	 */
	protected ValueReturnMode getValueReturnMode(IFieldInfo field, ITypeInfo objectType) {
		return field.getValueReturnMode();
	}

	/**
	 * Executes {@link IFieldInfo#setValue(Object, Object)} unless overridden.
	 * 
	 * @param field      The field information.
	 * @param object     Parameter of {@link IFieldInfo#setValue(Object, Object)}.
	 * @param value      Parameter of {@link IFieldInfo#setValue(Object, Object)}.
	 * @param objectType The parent type information.
	 */
	protected void setValue(IFieldInfo field, Object object, Object value, ITypeInfo objectType) {
		field.setValue(object, value);
	}

	/**
	 * @param field      The field information.
	 * @param object     Parameter of
	 *                   {@link IFieldInfo#getNextUpdateCustomUndoJob(Object, Object)}.
	 * @param value      Parameter of
	 *                   {@link IFieldInfo#getNextUpdateCustomUndoJob(Object, Object)}.
	 * @param objectType The parent type information.
	 * @return the result of
	 *         {@link IFieldInfo#getNextUpdateCustomUndoJob(Object, Object)} unless
	 *         overridden.
	 */
	protected Runnable getNextUpdateCustomUndoJob(IFieldInfo field, Object object, Object value, ITypeInfo objectType) {
		return field.getNextUpdateCustomUndoJob(object, value);
	}

	/**
	 * @param field      The field information.
	 * @param object     Parameter of
	 *                   {@link IFieldInfo#getPreviousUpdateCustomRedoJob(Object, Object)}.
	 * @param value      Parameter of
	 *                   {@link IFieldInfo#getPreviousUpdateCustomRedoJob(Object, Object)}.
	 * @param objectType The parent type information.
	 * @return the result of
	 *         {@link IFieldInfo#getPreviousUpdateCustomRedoJob(Object, Object)}
	 *         unless overridden.
	 */
	protected Runnable getPreviousUpdateCustomRedoJob(IFieldInfo field, Object object, Object value,
			ITypeInfo objectType) {
		return field.getPreviousUpdateCustomRedoJob(object, value);
	}

	/**
	 * @param type   The type information.
	 * @param object Parameter of {@link ITypeInfo#toString(Object)}.
	 * @return the result of {@link ITypeInfo#toString(Object)} unless overridden.
	 */
	protected String toString(ITypeInfo type, Object object) {
		return type.toString(object);
	}

	/**
	 * @param type   The type information.
	 * @param object Parameter of {@link ITypeInfo#canCopy(Object)}.
	 * @return the result of {@link ITypeInfo#canCopy(Object)} unless overridden.
	 */
	protected boolean canCopy(ITypeInfo type, Object object) {
		return type.canCopy(object);
	}

	/**
	 * @param type   The type information.
	 * @param object Parameter of {@link ITypeInfo#copy(Object)}.
	 * @return the result of {@link ITypeInfo#copy(Object)} unless overridden.
	 */
	protected Object copy(ITypeInfo type, Object object) {
		return type.copy(object);
	}

	/**
	 * @param type   The type information.
	 * @param object Parameter of {@link ITypeInfo#getIconImagePath(Object)}.
	 * @return the result of {@link ITypeInfo#getIconImagePath(Object)} unless
	 *         overridden.
	 */
	protected ResourcePath getIconImagePath(ITypeInfo type, Object object) {
		return type.getIconImagePath(object);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFieldsLayout()} unless overridden.
	 */
	protected FieldsLayout getFieldsLayout(ITypeInfo type) {
		return type.getFieldsLayout();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getMethodsLayout()} unless overridden.
	 */
	protected MethodsLayout getMethodsLayout(ITypeInfo type) {
		return type.getMethodsLayout();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getMenuModel()} unless overridden.
	 */
	protected MenuModel getMenuModel(ITypeInfo type) {
		return type.getMenuModel();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getCaption()} unless overridden.
	 */
	protected String getCaption(IFieldInfo field, ITypeInfo objectType) {
		return field.getCaption();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getName()} unless overridden.
	 */
	protected String getName(IFieldInfo field, ITypeInfo objectType) {
		return field.getName();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isHidden()} unless overridden.
	 */
	protected boolean isHidden(IFieldInfo field, ITypeInfo objectType) {
		return field.isHidden();
	}

	/**
	 * @param field      The field information.
	 * @param object     Parameter of {@link IMethodInfo#isRelevant(Object)}.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isRelevant()} unless overridden.
	 */
	protected boolean isRelevant(IFieldInfo field, Object object, ITypeInfo objectType) {
		return field.isRelevant(object);
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getDisplayAreaHorizontalWeight()}
	 *         unless overridden.
	 */
	protected double getDisplayAreaHorizontalWeight(IFieldInfo field, ITypeInfo objectType) {
		return field.getDisplayAreaHorizontalWeight();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getDisplayAreaVerticalWeight()}
	 *         unless overridden.
	 */
	protected double getDisplayAreaVerticalWeight(IFieldInfo field, ITypeInfo objectType) {
		return field.getDisplayAreaVerticalWeight();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isDisplayAreaHorizontallyFilled()}
	 *         unless overridden.
	 */
	protected boolean isDisplayAreaHorizontallyFilled(IFieldInfo field, ITypeInfo objectType) {
		return field.isDisplayAreaHorizontallyFilled();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isDisplayAreaVerticallyFilled()}
	 *         unless overridden.
	 */
	protected boolean isDisplayAreaVerticallyFilled(IFieldInfo field, ITypeInfo objectType) {
		return field.isDisplayAreaVerticallyFilled();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getCategory()} unless overridden.
	 */
	protected InfoCategory getCategory(IMethodInfo method, ITypeInfo objectType) {
		return method.getCategory();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#isReadOnly()} unless overridden.
	 */
	protected boolean isReadOnly(IMethodInfo method, ITypeInfo objectType) {
		return method.isReadOnly();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getNullReturnValueLabel()} unless
	 *         overridden.
	 */
	protected String getNullReturnValueLabel(IMethodInfo method, ITypeInfo objectType) {
		return method.getNullReturnValueLabel();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getNullReturnValueLabel()} unless
	 *         overridden.
	 */
	protected ValueReturnMode getValueReturnMode(IMethodInfo method, ITypeInfo objectType) {
		return method.getValueReturnMode();
	}

	/**
	 * @param method         The method information.
	 * @param object         Parameter of
	 *                       {@link IMethodInfo#invoke(Object, InvocationData)}.
	 * @param invocationData Parameter of
	 *                       {@link IMethodInfo#invoke(Object, InvocationData)}.
	 * @param objectType     The parent type information.
	 * @return the result of {@link IMethodInfo#invoke(Object, InvocationData)}
	 *         unless overridden.
	 */
	protected Object invoke(IMethodInfo method, Object object, InvocationData invocationData, ITypeInfo objectType) {
		return method.invoke(object, invocationData);
	}

	/**
	 * @param method         The method information.
	 * @param object         Parameter of
	 *                       {@link IMethodInfo#getConfirmationMessage(Object, InvocationData)}.
	 * @param invocationData Parameter of
	 *                       {@link IMethodInfo#getConfirmationMessage(Object, InvocationData)}.
	 * @param objectType     The parent type information.
	 * @return the result of
	 *         {@link IMethodInfo#getConfirmationMessage(Object, InvocationData)}
	 *         unless overridden.
	 */
	protected String getConfirmationMessage(IMethodInfo method, Object object, InvocationData invocationData,
			ITypeInfo objectType) {
		return method.getConfirmationMessage(object, invocationData);
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getParameters()} unless overridden.
	 */
	protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo objectType) {
		return method.getParameters();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getReturnValueType()} unless
	 *         overridden.
	 */
	protected ITypeInfo getReturnValueType(IMethodInfo method, ITypeInfo objectType) {
		return wrapMethodReturnValueTypeInfo(method.getReturnValueType());
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#isNullReturnValueDistinct()} unless
	 *         overridden.
	 */
	protected boolean isNullReturnValueDistinct(IMethodInfo method, ITypeInfo objectType) {
		return method.isNullReturnValueDistinct();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#isReturnValueDetached()} unless
	 *         overridden.
	 */
	protected boolean isReturnValueDetached(IMethodInfo method, ITypeInfo objectType) {
		return method.isReturnValueDetached();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#isReturnValueIgnored()} unless
	 *         overridden.
	 */
	protected boolean isReturnValueIgnored(IMethodInfo method, ITypeInfo objectType) {
		return method.isReturnValueIgnored();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getCaption()} unless overridden.
	 */
	protected String getCaption(IMethodInfo method, ITypeInfo objectType) {
		return method.getCaption();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of
	 *         {@link IMethodInfo#getParametersValidationCustomCaption()} unless
	 *         overridden.
	 */
	protected String getParametersValidationCustomCaption(IMethodInfo method, ITypeInfo objectType) {
		return method.getParametersValidationCustomCaption();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getExecutionSuccessMessage()} unless
	 *         overridden.
	 */
	protected String getExecutionSuccessMessage(IMethodInfo method, ITypeInfo objectType) {
		return method.getExecutionSuccessMessage();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getName()} unless overridden.
	 */
	protected String getName(IMethodInfo method, ITypeInfo objectType) {
		return method.getName();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#isHidden()} unless overridden.
	 */
	protected boolean isHidden(IMethodInfo method, ITypeInfo objectType) {
		return method.isHidden();
	}

	/**
	 * @param method     The method information.
	 * @param object     Parameter of {@link IMethodInfo#isRelevant(Object)}.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#isRelevant()} unless overridden.
	 */
	protected boolean isRelevant(IMethodInfo method, Object object, ITypeInfo objectType) {
		return method.isRelevant(object);
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of
	 *         {@link IMethodInfo#isControlReturnValueValiditionEnabled()} unless
	 *         overridden.
	 */
	protected boolean isControlReturnValueValiditionEnabled(IMethodInfo method, ITypeInfo objectType) {
		return method.isControlReturnValueValiditionEnabled();
	}

	/**
	 * Executes {@link IMethodInfo#onControlVisibilityChange(Object, boolean)}
	 * unless overridden.
	 * 
	 * @param method     The method information.
	 * @param object     Parameter of
	 *                   {@link IMethodInfo#onControlVisibilityChange(Object, boolean)}.
	 * @param visible    Parameter of
	 *                   {@link IMethodInfo#onControlVisibilityChange(Object, boolean)}.
	 * @param objectType The parent type information.
	 */
	protected void onControlVisibilityChange(IMethodInfo method, Object object, boolean visible, ITypeInfo objectType) {
		method.onControlVisibilityChange(object, visible);
	}

	/**
	 * @param method     The method information.
	 * @param object     Parameter of {@link IMethodInfo#isEnabled(Object)}.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#isEnabled(Object)} unless
	 *         overridden.
	 */
	protected boolean isEnabled(IMethodInfo method, Object object, ITypeInfo objectType) {
		return method.isEnabled(object);
	}

	/**
	 * Executes {@link IFieldInfo#onControlVisibilityChange(Object, boolean)} unless
	 * overridden.
	 * 
	 * @param field      The field information.
	 * @param object     Parameter of
	 *                   {@link IFieldInfo#onControlVisibilityChange(Object, boolean)}.
	 * @param visible    Parameter of
	 *                   {@link IFieldInfo#onControlVisibilityChange(Object, boolean)}.
	 * @param objectType The parent type information.
	 */
	protected void onControlVisibilityChange(IFieldInfo field, Object object, boolean visible, ITypeInfo objectType) {
		field.onControlVisibilityChange(object, visible);
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getSignature()} unless overridden.
	 */
	protected String getSignature(IMethodInfo method, ITypeInfo objectType) {
		return method.getSignature();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IEnumerationTypeInfo#getValues()} unless
	 *         overridden.
	 */
	protected Object[] getValues(IEnumerationTypeInfo type) {
		return type.getValues();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IEnumerationTypeInfo#isDynamicEnumeration()}
	 *         unless overridden.
	 */
	protected boolean isDynamicEnumeration(IEnumerationTypeInfo type) {
		return type.isDynamicEnumeration();
	}

	/**
	 * Executes {@link IListTypeInfo#replaceContent(Object, Object[])} unless
	 * overridden.
	 * 
	 * @param type      The type information.
	 * @param listValue Parameter of
	 *                  {@link IListTypeInfo#replaceContent(Object, Object[])}.
	 * @param array     Parameter of
	 *                  {@link IListTypeInfo#replaceContent(Object, Object[])}.
	 */
	protected void replaceContent(IListTypeInfo type, Object listValue, Object[] array) {
		type.replaceContent(listValue, array);
	}

	/**
	 * @param type  The type information.
	 * @param array Parameter of {@link IListTypeInfo#fromArray(Object[]))}.
	 * @return the result of {@link IListTypeInfo#fromArray(Object[]))} unless
	 *         overridden.
	 */
	protected Object fromArray(IListTypeInfo type, Object[] array) {
		return type.fromArray(array);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#canInstantiateFromArray()} unless
	 *         overridden.
	 */
	protected boolean canInstantiateFromArray(IListTypeInfo type) {
		return type.canInstantiateFromArray();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#canReplaceContent()} unless
	 *         overridden.
	 */
	protected boolean canReplaceContent(IListTypeInfo type) {
		return type.canReplaceContent();
	}

	/**
	 * @param type                            The type information.
	 * @param selection                       Parameter of
	 *                                        {@link IListTypeInfo#getDynamicActions(List, Mapper)}.
	 * @param listModificationFactoryAccessor Parameter of
	 *                                        {@link IListTypeInfo#getDynamicActions(List, Mapper)}.
	 * @return the result of {@link IListTypeInfo#getDynamicActions(List, Mapper)}
	 *         unless overridden.
	 */
	protected List<IDynamicListAction> getDynamicActions(IListTypeInfo type, List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		return type.getDynamicActions(selection, listModificationFactoryAccessor);
	}

	/**
	 * @param type                            The type information.
	 * @param selection                       Parameter of
	 *                                        {@link IListTypeInfo#getDynamicProperties(List, Mapper)}.
	 * @param listModificationFactoryAccessor Parameter of
	 *                                        {@link IListTypeInfo#getDynamicProperties(List, Mapper)}.
	 * @return the result of
	 *         {@link IListTypeInfo#getDynamicProperties(List, Mapper)} unless
	 *         overridden.
	 */
	protected List<IDynamicListProperty> getDynamicProperties(IListTypeInfo type,
			List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		return type.getDynamicProperties(selection, listModificationFactoryAccessor);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#getItemType()} unless overridden.
	 */
	protected ITypeInfo getItemType(IListTypeInfo type) {
		return wrapItemTypeInfo(type.getItemType());
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#getStructuralInfo()} unless
	 *         overridden.
	 */
	protected IListStructuralInfo getStructuralInfo(IListTypeInfo type) {
		return type.getStructuralInfo();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#getDetailsAccessMode()} unless
	 *         overridden.
	 */
	protected IListItemDetailsAccessMode getDetailsAccessMode(IListTypeInfo type) {
		return type.getDetailsAccessMode();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#isItemNullValueSupported()} unless
	 *         overridden.
	 */
	protected boolean isItemNullValueSupported(IListTypeInfo type) {
		return type.isItemNullValueSupported();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#areItemsAutomaticallyPositioned()}
	 *         unless overridden.
	 */
	protected boolean areItemsAutomaticallyPositioned(IListTypeInfo type) {
		return type.areItemsAutomaticallyPositioned();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#isMoveAllowed()} unless
	 *         overridden.
	 */
	protected boolean isMoveAllowed(IListTypeInfo type) {
		return type.isMoveAllowed();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#getItemCreationMode()} unless
	 *         overridden.
	 */
	protected ItemCreationMode getItemCreationMode(IListTypeInfo type) {
		return type.getItemCreationMode();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#getItemReturnMode()} unless
	 *         overridden.
	 */
	protected ValueReturnMode getItemReturnMode(IListTypeInfo type) {
		return type.getItemReturnMode();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#getToolsLocation()} unless
	 *         overridden.
	 */
	protected ToolsLocation getToolsLocation(IListTypeInfo type) {
		return type.getToolsLocation();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#isInsertionAllowed()} unless
	 *         overridden.
	 */
	protected boolean isInsertionAllowed(IListTypeInfo type) {
		return type.isInsertionAllowed();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#isRemovalAllowed()} unless
	 *         overridden.
	 */
	protected boolean isRemovalAllowed(IListTypeInfo type) {
		return type.isRemovalAllowed();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IListTypeInfo#canViewItemDetails()} unless
	 *         overridden.
	 */
	protected boolean canViewItemDetails(IListTypeInfo type) {
		return type.canViewItemDetails();
	}

	/**
	 * @param type      The type information.
	 * @param listValue Parameter of {@link IListTypeInfo#toArray(Object)}.
	 * @return the result of {@link IListTypeInfo#toArray(Object)} unless
	 *         overridden.
	 */
	protected Object[] toArray(IListTypeInfo type, Object listValue) {
		return type.toArray(listValue);
	}

	/**
	 * @param type       The type information.
	 * @param objectType Parameter of
	 *                   {@link IListTypeInfo#isItemNodeValidityDetectionEnabled(ItemPosition)}.
	 * @return the result of
	 *         {@link IListTypeInfo#isItemNodeValidityDetectionEnabled(ItemPosition)}
	 *         unless overridden.
	 */
	protected boolean isItemNodeValidityDetectionEnabled(IListTypeInfo type, ItemPosition itemPosition) {
		return type.isItemNodeValidityDetectionEnabled(itemPosition);
	}

	/**
	 * @param type       The type information.
	 * @param objectType Parameter of
	 *                   {@link IListTypeInfo#getSelectionTargetField(ITypeInfo)}.
	 * @return the result of
	 *         {@link IListTypeInfo#getSelectionTargetField(ITypeInfo)} unless
	 *         overridden.
	 */
	protected IFieldInfo getSelectionTargetField(IListTypeInfo type, ITypeInfo objectType) {
		return type.getSelectionTargetField(objectType);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getConstructors()} unless overridden.
	 */
	protected List<IMethodInfo> getConstructors(ITypeInfo type) {
		return type.getConstructors();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFields()} unless overridden.
	 */
	protected List<IFieldInfo> getFields(ITypeInfo type) {
		return type.getFields();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getMethods()} unless overridden.
	 */
	protected List<IMethodInfo> getMethods(ITypeInfo type) {
		return type.getMethods();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getPolymorphicInstanceSubTypes()}
	 *         unless overridden.
	 */
	protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
		return type.getPolymorphicInstanceSubTypes();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#isConcrete()} unless overridden.
	 */
	protected boolean isConcrete(ITypeInfo type) {
		return type.isConcrete();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#isPrimitive()} unless overridden.
	 */
	protected boolean isPrimitive(ITypeInfo type) {
		return type.isPrimitive();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#isImmutable()} unless overridden.
	 */
	protected boolean isImmutable(ITypeInfo type) {
		return type.isImmutable();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#canPersist()} unless overridden.
	 */
	protected boolean canPersist(ITypeInfo type) {
		return type.canPersist();
	}

	/**
	 * Executes {@link ITypeInfo#save(Object, OutputStream)} unless overridden.
	 * 
	 * @param type       The type information.
	 * @param object     Parameter of {@link ITypeInfo#save(Object, File)}.
	 * @param outputFile Parameter of {@link ITypeInfo#save(Object, File)}.
	 */
	protected void save(ITypeInfo type, Object object, File outputFile) {
		type.save(object, outputFile);
	}

	/**
	 * Executes {@link ITypeInfo#load(Object, InputStream)} unless overridden.
	 * 
	 * @param type      The type information.
	 * @param object    Parameter of {@link ITypeInfo#load(Object, File)}.
	 * @param inputFile Parameter of {@link ITypeInfo#load(Object, File)}.
	 */
	protected void load(ITypeInfo type, Object object, File inputFile) {
		type.load(object, inputFile);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#isModificationStackAccessible()}
	 *         unless overridden.
	 */
	protected boolean isModificationStackAccessible(ITypeInfo type) {
		return type.isModificationStackAccessible();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#isValidationRequired()} unless
	 *         overridden.
	 */
	protected boolean isValidationRequired(ITypeInfo type) {
		return type.isValidationRequired();
	}

	/**
	 * @param type   The type information.
	 * @param object Parameter of {@link ITypeInfo#supports(Object)}.
	 * @return the result of {@link ITypeInfo#supports(Object)} unless overridden.
	 */
	protected boolean supports(ITypeInfo type, Object object) {
		return type.supports(object);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getCaption()} unless overridden.
	 */
	protected String getCaption(ITypeInfo type) {
		return type.getCaption();
	}

	/**
	 * @param type    The type information.
	 * @param object  Parameter of
	 *                {@link ITypeInfo#onFormVisibilityChange(Object, boolean)}.
	 * @param visible Parameter of
	 *                {@link ITypeInfo#onFormVisibilityChange(Object, boolean)}.
	 * @return the result of
	 *         {@link ITypeInfo#onFormVisibilityChange(Object, boolean)} unless
	 *         overridden.
	 */
	protected boolean onFormVisibilityChange(ITypeInfo type, Object object, boolean visible) {
		return type.onFormVisibilityChange(object, visible);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getName()} unless overridden.
	 */
	protected String getName(ITypeInfo type) {
		return type.getName();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getName()} unless overridden.
	 */
	protected String getName(IApplicationInfo appInfo) {
		return appInfo.getName();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getCaption()} unless
	 *         overridden.
	 */
	protected String getCaption(IApplicationInfo appInfo) {
		return appInfo.getCaption();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of
	 *         {@link IApplicationInfo#isSystemIntegrationCrossPlatform()} unless
	 *         overridden.
	 */
	protected boolean isSystemIntegrationCrossPlatform(IApplicationInfo appInfo) {
		return appInfo.isSystemIntegrationCrossPlatform();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getIconImagePath()} unless
	 *         overridden.
	 */
	protected ResourcePath getIconImagePath(IApplicationInfo appInfo) {
		return appInfo.getIconImagePath();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getOnlineHelp()} unless
	 *         overridden.
	 */
	protected String getOnlineHelp(IApplicationInfo appInfo) {
		return appInfo.getOnlineHelp();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getMainBackgroundColor()}
	 *         unless overridden.
	 */
	protected ColorSpecification getMainBackgroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainBackgroundColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getMainForegroundColor()}
	 *         unless overridden.
	 */
	protected ColorSpecification getMainForegroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainForegroundColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getMainBorderColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getMainBorderColor(IApplicationInfo appInfo) {
		return appInfo.getMainBorderColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getMainEditorBackgroundColor()}
	 *         unless overridden.
	 */
	protected ColorSpecification getMainEditorBackgroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainEditorBackgroundColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getMainEditorForegroundColor()}
	 *         unless overridden.
	 */
	protected ColorSpecification getMainEditorForegroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainEditorForegroundColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getMainBackgroundImagePath()}
	 *         unless overridden.
	 */
	protected ResourcePath getMainBackgroundImagePath(IApplicationInfo appInfo) {
		return appInfo.getMainBackgroundImagePath();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getMainButtonBackgroundColor()}
	 *         unless overridden.
	 */
	protected ColorSpecification getMainButtonBackgroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainButtonBackgroundColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of
	 *         {@link IApplicationInfo#getMainButtonBackgroundImagePath()} unless
	 *         overridden.
	 */
	protected ResourcePath getMainButtonBackgroundImagePath(IApplicationInfo appInfo) {
		return appInfo.getMainButtonBackgroundImagePath();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of
	 *         {@link IApplicationInfo#getButtonCustomFontResourcePath()} unless
	 *         overridden.
	 */
	protected ResourcePath getButtonCustomFontResourcePath(IApplicationInfo appInfo) {
		return appInfo.getButtonCustomFontResourcePath();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of
	 *         {@link IApplicationInfo#getLabelCustomFontResourcePath()} unless
	 *         overridden.
	 */
	protected ResourcePath getLabelCustomFontResourcePath(IApplicationInfo appInfo) {
		return appInfo.getLabelCustomFontResourcePath();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of
	 *         {@link IApplicationInfo#getEditorCustomFontResourcePath()} unless
	 *         overridden.
	 */
	protected ResourcePath getEditorCustomFontResourcePath(IApplicationInfo appInfo) {
		return appInfo.getEditorCustomFontResourcePath();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getMainButtonForegroundColor()}
	 *         unless overridden.
	 */
	protected ColorSpecification getMainButtonForegroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainButtonForegroundColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getMainButtonBorderColor()}
	 *         unless overridden.
	 */
	protected ColorSpecification getMainButtonBorderColor(IApplicationInfo appInfo) {
		return appInfo.getMainButtonBorderColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getTitleBackgroundColor()}
	 *         unless overridden.
	 */
	protected ColorSpecification getTitleBackgroundColor(IApplicationInfo appInfo) {
		return appInfo.getTitleBackgroundColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getTitleForegroundColor()}
	 *         unless overridden.
	 */
	protected ColorSpecification getTitleForegroundColor(IApplicationInfo appInfo) {
		return appInfo.getTitleForegroundColor();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of
	 *         {@link IApplicationInfo#getTitleCustomFontResourcePath()} unless
	 *         overridden.
	 */
	protected ResourcePath getTitleCustomFontResourcePath(IApplicationInfo appInfo) {
		return appInfo.getTitleCustomFontResourcePath();
	}

	/**
	 * @param appInfo The application information.
	 * @return the result of {@link IApplicationInfo#getSpecificProperties()} unless
	 *         overridden.
	 */
	protected Map<String, Object> getSpecificProperties(IApplicationInfo appInfo) {
		return appInfo.getSpecificProperties();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getSource()} unless overridden.
	 */
	protected ITypeInfoSource getSource(final ITypeInfo type) {
		return type.getSource();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getCategoriesStyle()} unless
	 *         overridden.
	 */
	protected CategoriesStyle getCategoriesStyle(ITypeInfo type) {
		return type.getCategoriesStyle();
	}

	/**
	 * @param type   The type information.
	 * @param object Parameter of {@link ITypeInfo#createTransaction(Object)}.
	 * @return the result of {@link ITypeInfo#createTransaction(Object)} unless
	 *         overridden.
	 */
	protected ITransaction createTransaction(ITypeInfo type, Object object) {
		return type.createTransaction(object);
	}

	/**
	 * Executes {@link ITypeInfo#onFormRefresh(Object)} unless overridden.
	 * 
	 * @param type   The type information.
	 * @param object Parameter of {@link ITypeInfo#onFormRefresh(Object)}.
	 */
	protected void onFormRefresh(ITypeInfo type, Object object) {
		type.onFormRefresh(object);
	}

	/**
	 * Executes {@link ITypeInfo#onFormRefresh(Object)} unless overridden.
	 * 
	 * @param type   The type information.
	 * @param object Parameter of
	 *               {@link ITypeInfo#getLastFormRefreshStateRestorationJob(Object)}.
	 * @return the result of
	 *         {@link ITypeInfo#getLastFormRefreshStateRestorationJob()} unless
	 *         overridden.
	 */
	protected Runnable getLastFormRefreshStateRestorationJob(ITypeInfo type, Object object) {
		return type.getLastFormRefreshStateRestorationJob(object);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormBackgroundImagePath()} unless
	 *         overridden.
	 */
	protected ResourcePath getFormBackgroundImagePath(ITypeInfo type) {
		return type.getFormBackgroundImagePath();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormBackgroundColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getFormBackgroundColor(ITypeInfo type) {
		return type.getFormBackgroundColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormForegroundColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getFormForegroundColor(ITypeInfo type) {
		return type.getFormForegroundColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormBorderColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getFormBorderColor(ITypeInfo type) {
		return type.getFormBorderColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormButtonForegroundColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getFormButtonForegroundColor(ITypeInfo type) {
		return type.getFormButtonForegroundColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormButtonBorderColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getFormButtonBorderColor(ITypeInfo type) {
		return type.getFormButtonBorderColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormButtonBackgroundImagePath()}
	 *         unless overridden.
	 */
	protected ResourcePath getFormButtonBackgroundImagePath(ITypeInfo type) {
		return type.getFormButtonBackgroundImagePath();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormButtonBackgroundColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getFormButtonBackgroundColor(ITypeInfo type) {
		return type.getFormButtonBackgroundColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getCategoriesForegroundColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getCategoriesForegroundColor(ITypeInfo type) {
		return type.getCategoriesForegroundColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getCategoriesBackgroundColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getCategoriesBackgroundColor(ITypeInfo type) {
		return type.getCategoriesBackgroundColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormEditorBackgroundColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getFormEditorBackgroundColor(ITypeInfo type) {
		return type.getFormEditorBackgroundColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormEditorForegroundColor()} unless
	 *         overridden.
	 */
	protected ColorSpecification getFormEditorForegroundColor(ITypeInfo type) {
		return type.getFormEditorForegroundColor();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormPreferredWidth()} unless
	 *         overridden.
	 */
	protected int getFormPreferredWidth(ITypeInfo type) {
		return type.getFormPreferredWidth();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormPreferredHeight()} unless
	 *         overridden.
	 */
	protected int getFormPreferredHeight(ITypeInfo type) {
		return type.getFormPreferredHeight();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getFormSpacing()} unless overridden.
	 */
	protected int getFormSpacing(ITypeInfo type) {
		return type.getFormSpacing();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IMapEntryTypeInfo#getKeyField()} unless
	 *         overridden.
	 */
	protected IFieldInfo getKeyField(IMapEntryTypeInfo type) {
		return wrapFieldInfo(type.getKeyField(), type);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link IMapEntryTypeInfo#getValueField()} unless
	 *         overridden.
	 */
	protected IFieldInfo getValueField(IMapEntryTypeInfo type) {
		return wrapFieldInfo(type.getValueField(), type);
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isFormControlMandatory()} unless
	 *         overridden.
	 */
	protected boolean isFormControlMandatory(IFieldInfo field, ITypeInfo objectType) {
		return field.isFormControlMandatory();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isFormControlEmbedded()} unless
	 *         overridden.
	 */
	protected boolean isFormControlEmbedded(IFieldInfo field, ITypeInfo objectType) {
		return field.isFormControlEmbedded();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getFormControlFilter()} unless
	 *         overridden.
	 */
	protected IInfoFilter getFormControlFilter(IFieldInfo field, ITypeInfo objectType) {
		return field.getFormControlFilter();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getAutoUpdatePeriodMilliseconds()}
	 *         unless overridden.
	 */
	protected long getAutoUpdatePeriodMilliseconds(IFieldInfo field, ITypeInfo objectType) {
		return field.getAutoUpdatePeriodMilliseconds();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#isControlValueValiditionEnabled()}
	 *         unless overridden.
	 */
	protected boolean isControlValueValiditionEnabled(IFieldInfo field, ITypeInfo objectType) {
		return field.isControlValueValiditionEnabled();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getOnlineHelp()} unless overridden.
	 */
	protected String getOnlineHelp(IFieldInfo field, ITypeInfo objectType) {
		return field.getOnlineHelp();
	}

	/**
	 * @param field      The field information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IFieldInfo#getSpecificProperties()} unless
	 *         overridden.
	 */
	protected Map<String, Object> getSpecificProperties(IFieldInfo field, ITypeInfo objectType) {
		return field.getSpecificProperties();
	}

	/**
	 * @param param      The parameter information.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#getOnlineHelp()} unless
	 *         overridden.
	 */
	protected String getOnlineHelp(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return param.getOnlineHelp();
	}

	/**
	 * @param param      The parameter information.
	 * @param method     The parent method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IParameterInfo#getSpecificProperties()} unless
	 *         overridden.
	 */
	protected Map<String, Object> getSpecificProperties(IParameterInfo param, IMethodInfo method,
			ITypeInfo objectType) {
		return param.getSpecificProperties();
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getOnlineHelp()} unless overridden.
	 */
	protected String getOnlineHelp(ITypeInfo type) {
		return type.getOnlineHelp();
	}

	/**
	 * @param type    The type information.
	 * @param object  Parameter of
	 *                {@link ITypeInfo#validate(Object, ValidationSession)}.
	 * @param session Parameter of
	 *                {@link ITypeInfo#validate(Object, ValidationSession)}.
	 * @throws Exception If {@link ITypeInfo#validate(Object)} throws the exception
	 *                   unless overridden.
	 */
	protected void validate(ITypeInfo type, Object object, ValidationSession session) throws Exception {
		type.validate(object, session);
	}

	/**
	 * @param type The type information.
	 * @return the result of {@link ITypeInfo#getSpecificProperties()} unless
	 *         overridden.
	 */
	protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
		return type.getSpecificProperties();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getOnlineHelp()} unless overridden.
	 */
	protected String getOnlineHelp(IMethodInfo method, ITypeInfo objectType) {
		return method.getOnlineHelp();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getIconImagePath()} unless
	 *         overridden.
	 */
	protected ResourcePath getIconImagePath(IMethodInfo method, ITypeInfo objectType) {
		return method.getIconImagePath();
	}

	/**
	 * @param method     The method information.
	 * @param objectType The parent type information.
	 * @return the result of {@link IMethodInfo#getSpecificProperties()} unless
	 *         overridden.
	 */
	protected Map<String, Object> getSpecificProperties(IMethodInfo method, ITypeInfo objectType) {
		return method.getSpecificProperties();
	}

	/**
	 * @param method         The method information.
	 * @param objectType     The parent type information.
	 * @param object         Parameter of
	 *                       {@link IMethodInfo#validateParameters(Object, InvocationData)}.
	 * @param invocationData Parameter of
	 *                       {@link IMethodInfo#validateParameters(Object, InvocationData)}.
	 * @throws Exception If
	 *                   {@link IMethodInfo#validateParameters(Object, InvocationData)}
	 *                   throws the exception unless overridden.
	 */
	protected void validateParameters(IMethodInfo method, ITypeInfo objectType, Object object,
			InvocationData invocationData) throws Exception {
		method.validateParameters(object, invocationData);
	}

	/**
	 * @param method         The method information.
	 * @param objectType     The parent type information.
	 * @param object         Parameter of
	 *                       {@link IMethodInfo#getNextInvocationUndoJob(Object, InvocationData)}.
	 * @param invocationData Parameter of
	 *                       {@link IMethodInfo#getNextInvocationUndoJob(Object, InvocationData)}.
	 * @return the result of
	 *         {@link IMethodInfo#getNextInvocationUndoJob(Object, InvocationData)}
	 *         unless overridden.
	 */
	protected Runnable getNextInvocationUndoJob(IMethodInfo method, ITypeInfo objectType, Object object,
			InvocationData invocationData) {
		return method.getNextInvocationUndoJob(object, invocationData);
	}

	/**
	 * @param method         The method information.
	 * @param objectType     The parent type information.
	 * @param object         Parameter of
	 *                       {@link IMethodInfo#getPreviousInvocationCustomRedoJob(Object, InvocationData)}.
	 * @param invocationData Parameter of
	 *                       {@link IMethodInfo#getPreviousInvocationCustomRedoJob(Object, InvocationData)}.
	 * @return the result of
	 *         {@link IMethodInfo#getPreviousInvocationCustomRedoJob(Object, InvocationData)}
	 *         unless overridden.
	 */
	protected Runnable getPreviousInvocationCustomRedoJob(IMethodInfo method, ITypeInfo objectType, Object object,
			InvocationData invocationData) {
		return method.getPreviousInvocationCustomRedoJob(object, invocationData);
	}

	/**
	 * @param type   The type information.
	 * @param object Parameter of {@link IEnumerationTypeInfo#getValueInfo(Object)}.
	 * @return the result of {@link IEnumerationTypeInfo#getValueInfo(Object)}
	 *         unless overridden.
	 */
	protected IEnumerationItemInfo getValueInfo(IEnumerationTypeInfo type, Object object) {
		return type.getValueInfo(object);
	}

	/**
	 * @param info           The enumeration item information.
	 * @param parentEnumType The parent enumeration type information.
	 * @return the result of {@link IEnumerationItemInfo#getName()} unless
	 *         overridden.
	 */
	protected String getName(IEnumerationItemInfo info, IEnumerationTypeInfo parentEnumType) {
		return info.getName();
	}

	/**
	 * @param info           The enumeration item information.
	 * @param parentEnumType The parent enumeration type information.
	 * @return the result of {@link IEnumerationItemInfo#getValue()} unless
	 *         overridden.
	 */
	protected Object getValue(IEnumerationItemInfo info, IEnumerationTypeInfo parentEnumType) {
		return info.getValue();
	}

	/**
	 * @param info           The enumeration item information.
	 * @param parentEnumType The parent enumeration type information.
	 * @return the result of {@link IEnumerationItemInfo#getSpecificProperties()}
	 *         unless overridden.
	 */
	protected Map<String, Object> getSpecificProperties(IEnumerationItemInfo info,
			IEnumerationTypeInfo parentEnumType) {
		return info.getSpecificProperties();
	}

	/**
	 * @param info           The enumeration item information.
	 * @param parentEnumType The parent enumeration type information.
	 * @return the result of {@link IEnumerationItemInfo#getIconImagePath()} unless
	 *         overridden.
	 */
	protected ResourcePath getIconImagePath(IEnumerationItemInfo info, IEnumerationTypeInfo parentEnumType) {
		return info.getIconImagePath();
	}

	/**
	 * @param info           The enumeration item information.
	 * @param parentEnumType The parent enumeration type information.
	 * @return the result of {@link IEnumerationItemInfo#getOnlineHelp()} unless
	 *         overridden.
	 */
	protected String getOnlineHelp(IEnumerationItemInfo info, IEnumerationTypeInfo parentEnumType) {
		return info.getOnlineHelp();
	}

	/**
	 * @param info           The enumeration item information.
	 * @param parentEnumType The parent enumeration type information.
	 * @return the result of {@link IEnumerationItemInfo#getCaption()} unless
	 *         overridden.
	 */
	protected String getCaption(IEnumerationItemInfo info, IEnumerationTypeInfo parentEnumType) {
		return info.getCaption();
	}

	public class GeneratedApplicationInfoProxy extends AbstractInfoProxy implements IApplicationInfo {

		protected InfoProxyFactory factory = InfoProxyFactory.this;
		protected IApplicationInfo base;

		public GeneratedApplicationInfoProxy(IApplicationInfo appInfo) {
			this.base = appInfo;
			checkActiveFactories();
		}

		public InfoProxyFactory getFactory() {
			return factory;
		}

		@Override
		public String getName() {
			return InfoProxyFactory.this.getName(base);
		}

		@Override
		public String getCaption() {
			return InfoProxyFactory.this.getCaption(base);
		}

		@Override
		public boolean isSystemIntegrationCrossPlatform() {
			return InfoProxyFactory.this.isSystemIntegrationCrossPlatform(base);
		}

		@Override
		public ResourcePath getIconImagePath() {
			return InfoProxyFactory.this.getIconImagePath(base);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyFactory.this.getOnlineHelp(base);
		}

		@Override
		public ColorSpecification getMainBackgroundColor() {
			return InfoProxyFactory.this.getMainBackgroundColor(base);
		}

		@Override
		public ColorSpecification getMainForegroundColor() {
			return InfoProxyFactory.this.getMainForegroundColor(base);
		}

		@Override
		public ColorSpecification getMainBorderColor() {
			return InfoProxyFactory.this.getMainBorderColor(base);
		}

		@Override
		public ResourcePath getMainBackgroundImagePath() {
			return InfoProxyFactory.this.getMainBackgroundImagePath(base);
		}

		@Override
		public ColorSpecification getMainEditorForegroundColor() {
			return InfoProxyFactory.this.getMainEditorForegroundColor(base);
		}

		@Override
		public ColorSpecification getMainEditorBackgroundColor() {
			return InfoProxyFactory.this.getMainEditorBackgroundColor(base);
		}

		@Override
		public ColorSpecification getMainButtonBackgroundColor() {
			return InfoProxyFactory.this.getMainButtonBackgroundColor(base);
		}

		@Override
		public ColorSpecification getMainButtonForegroundColor() {
			return InfoProxyFactory.this.getMainButtonForegroundColor(base);
		}

		@Override
		public ColorSpecification getMainButtonBorderColor() {
			return InfoProxyFactory.this.getMainButtonBorderColor(base);
		}

		@Override
		public ColorSpecification getTitleBackgroundColor() {
			return InfoProxyFactory.this.getTitleBackgroundColor(base);
		}

		@Override
		public ColorSpecification getTitleForegroundColor() {
			return InfoProxyFactory.this.getTitleForegroundColor(base);
		}

		@Override
		public ResourcePath getTitleCustomFontResourcePath() {
			return InfoProxyFactory.this.getTitleCustomFontResourcePath(base);
		}

		@Override
		public ResourcePath getMainButtonBackgroundImagePath() {
			return InfoProxyFactory.this.getMainButtonBackgroundImagePath(base);
		}

		@Override
		public ResourcePath getButtonCustomFontResourcePath() {
			return InfoProxyFactory.this.getButtonCustomFontResourcePath(base);
		}

		@Override
		public ResourcePath getLabelCustomFontResourcePath() {
			return InfoProxyFactory.this.getLabelCustomFontResourcePath(base);
		}

		@Override
		public ResourcePath getEditorCustomFontResourcePath() {
			return InfoProxyFactory.this.getEditorCustomFontResourcePath(base);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return traceActiveFactory(InfoProxyFactory.this.getSpecificProperties(base));
		}

		protected Map<String, Object> traceActiveFactory(Map<String, Object> specificProperties) {
			Map<String, Object> result = new HashMap<String, Object>(specificProperties);
			List<InfoProxyFactory> factories = listFactories(base);
			if (factories == null) {
				factories = new ArrayList<InfoProxyFactory>();
			}
			factories.add(InfoProxyFactory.this);
			result.put(ACTIVE_FACTORIES_KEY, factories);
			return result;
		}

		protected void checkActiveFactories() {
			List<InfoProxyFactory> factories = listFactories(base);
			if (factories != null) {
				List<String> factoryIds = new ArrayList<String>();
				for (InfoProxyFactory factory : factories) {
					factoryIds.add(factory.getIdentifier());
				}
				Collections.reverse(factoryIds);
				if (factoryIds.contains(factory.getIdentifier())) {
					StringBuilder msg = new StringBuilder();
					msg.append("Duplicate proxy creation detected:" + "\nNew proxy factory identifier:\n- "
							+ InfoProxyFactory.this.getIdentifier() + "\nExisting factories identifers:\n");
					for (String id : factoryIds) {
						msg.append("- " + id + "\n");
					}
					msg.append(
							"If the factories actually differ, please override the getIdentifier() method to differenciate them");
					throw new ReflectionUIError(msg.toString());
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			GeneratedApplicationInfoProxy other = (GeneratedApplicationInfoProxy) obj;
			if (base == null) {
				if (other.base != null)
					return false;
			} else if (!base.equals(other.base))
				return false;
			if (factory == null) {
				if (other.factory != null)
					return false;
			} else if (!factory.equals(other.factory))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GeneratedApplicationInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base + "]";
		}

	}

	public class GeneratedBasicTypeInfoProxy extends AbstractInfoProxy implements ITypeInfo {

		protected InfoProxyFactory factory = InfoProxyFactory.this;
		protected ITypeInfo base;
		protected List<IFieldInfo> fields;
		protected List<IMethodInfo> methods;
		protected List<IMethodInfo> constructors;

		public GeneratedBasicTypeInfoProxy(ITypeInfo type) {
			this.base = type;
			checkActiveFactories();
		}

		public InfoProxyFactory getFactory() {
			return factory;
		}

		@Override
		public String getName() {
			return InfoProxyFactory.this.getName(base);
		}

		@Override
		public ITypeInfoSource getSource() {
			return InfoProxyFactory.this.getSource(base);
		}

		@Override
		public ITransaction createTransaction(Object object) {
			return InfoProxyFactory.this.createTransaction(base, object);
		}

		@Override
		public void onFormRefresh(Object object) {
			InfoProxyFactory.this.onFormRefresh(base, object);
		}

		@Override
		public Runnable getLastFormRefreshStateRestorationJob(Object object) {
			return InfoProxyFactory.this.getLastFormRefreshStateRestorationJob(base, object);
		}

		@Override
		public CategoriesStyle getCategoriesStyle() {
			return InfoProxyFactory.this.getCategoriesStyle(base);
		}

		@Override
		public ResourcePath getFormBackgroundImagePath() {
			return InfoProxyFactory.this.getFormBackgroundImagePath(base);
		}

		@Override
		public ColorSpecification getFormBackgroundColor() {
			return InfoProxyFactory.this.getFormBackgroundColor(base);
		}

		@Override
		public ColorSpecification getFormForegroundColor() {
			return InfoProxyFactory.this.getFormForegroundColor(base);
		}

		@Override
		public ColorSpecification getFormBorderColor() {
			return InfoProxyFactory.this.getFormBorderColor(base);
		}

		@Override
		public ColorSpecification getFormButtonBackgroundColor() {
			return InfoProxyFactory.this.getFormButtonBackgroundColor(base);
		}

		@Override
		public ColorSpecification getFormButtonForegroundColor() {
			return InfoProxyFactory.this.getFormButtonForegroundColor(base);
		}

		@Override
		public ResourcePath getFormButtonBackgroundImagePath() {
			return InfoProxyFactory.this.getFormButtonBackgroundImagePath(base);
		}

		@Override
		public ColorSpecification getFormButtonBorderColor() {
			return InfoProxyFactory.this.getFormButtonBorderColor(base);
		}

		@Override
		public ColorSpecification getCategoriesBackgroundColor() {
			return InfoProxyFactory.this.getCategoriesBackgroundColor(base);
		}

		@Override
		public ColorSpecification getCategoriesForegroundColor() {
			return InfoProxyFactory.this.getCategoriesForegroundColor(base);
		}

		@Override
		public ColorSpecification getFormEditorForegroundColor() {
			return InfoProxyFactory.this.getFormEditorForegroundColor(base);
		}

		@Override
		public ColorSpecification getFormEditorBackgroundColor() {
			return InfoProxyFactory.this.getFormEditorBackgroundColor(base);
		}

		@Override
		public int getFormPreferredWidth() {
			return InfoProxyFactory.this.getFormPreferredWidth(base);
		}

		@Override
		public int getFormPreferredHeight() {
			return InfoProxyFactory.this.getFormPreferredHeight(base);
		}

		@Override
		public int getFormSpacing() {
			return InfoProxyFactory.this.getFormSpacing(base);
		}

		@Override
		public boolean onFormVisibilityChange(Object object, boolean visible) {
			return InfoProxyFactory.this.onFormVisibilityChange(base, object, visible);
		}

		@Override
		public String getCaption() {
			return InfoProxyFactory.this.getCaption(base);
		}

		@Override
		public boolean supports(Object object) {
			return InfoProxyFactory.this.supports(base, object);
		}

		@Override
		public boolean isConcrete() {
			return InfoProxyFactory.this.isConcrete(base);
		}

		@Override
		public boolean isPrimitive() {
			return InfoProxyFactory.this.isPrimitive(base);
		}

		@Override
		public boolean isImmutable() {
			return InfoProxyFactory.this.isImmutable(base);
		}

		@Override
		public boolean canPersist() {
			return InfoProxyFactory.this.canPersist(base);
		}

		@Override
		public void save(Object object, File outputFile) {
			InfoProxyFactory.this.save(base, object, outputFile);
		}

		@Override
		public void load(Object object, File inputFile) {
			InfoProxyFactory.this.load(base, object, inputFile);
		}

		@Override
		public boolean isModificationStackAccessible() {
			return InfoProxyFactory.this.isModificationStackAccessible(base);
		}

		@Override
		public boolean isValidationRequired() {
			return InfoProxyFactory.this.isValidationRequired(base);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			List<ITypeInfo> result = new ArrayList<ITypeInfo>();
			for (ITypeInfo subType : InfoProxyFactory.this.getPolymorphicInstanceSubTypes(base)) {
				result.add(wrapSubTypeInfo(subType));
			}
			return result;
		}

		@Override
		public List<IMethodInfo> getMethods() {
			if (methods == null) {
				methods = new ArrayList<IMethodInfo>();
				for (IMethodInfo method : InfoProxyFactory.this.getMethods(base)) {
					methods.add(wrapMethodInfo(method, base));
				}
			}
			return methods;
		}

		@Override
		public List<IFieldInfo> getFields() {
			if (fields == null) {
				fields = new ArrayList<IFieldInfo>();
				for (IFieldInfo field : InfoProxyFactory.this.getFields(base)) {
					fields.add(wrapFieldInfo(field, base));
				}
			}
			return fields;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			if (constructors == null) {
				constructors = new ArrayList<IMethodInfo>();
				for (IMethodInfo constructor : InfoProxyFactory.this.getConstructors(base)) {
					constructors.add(wrapConstructorInfo(constructor, base));
				}
			}
			return constructors;
		}

		@Override
		public String toString(Object object) {
			return InfoProxyFactory.this.toString(base, object);
		}

		@Override
		public boolean canCopy(Object object) {
			return InfoProxyFactory.this.canCopy(base, object);
		}

		@Override
		public Object copy(Object object) {
			return InfoProxyFactory.this.copy(base, object);
		}

		@Override
		public ResourcePath getIconImagePath(Object object) {
			return InfoProxyFactory.this.getIconImagePath(base, object);
		}

		@Override
		public FieldsLayout getFieldsLayout() {
			return InfoProxyFactory.this.getFieldsLayout(base);
		}

		@Override
		public MethodsLayout getMethodsLayout() {
			return InfoProxyFactory.this.getMethodsLayout(base);
		}

		@Override
		public MenuModel getMenuModel() {
			return InfoProxyFactory.this.getMenuModel(base);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyFactory.this.getOnlineHelp(base);
		}

		@Override
		public void validate(Object object, ValidationSession session) throws Exception {
			InfoProxyFactory.this.validate(base, object, session);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return traceActiveFactory(InfoProxyFactory.this.getSpecificProperties(base));
		}

		protected Map<String, Object> traceActiveFactory(Map<String, Object> specificProperties) {
			Map<String, Object> result = new HashMap<String, Object>(specificProperties);
			List<InfoProxyFactory> factories = listFactories(base);
			if (factories == null) {
				factories = new ArrayList<InfoProxyFactory>();
			}
			factories.add(InfoProxyFactory.this);
			result.put(ACTIVE_FACTORIES_KEY, factories);
			return result;
		}

		protected void checkActiveFactories() {
			List<InfoProxyFactory> factories = listFactories(base);
			if (factories != null) {
				List<String> factoryIds = new ArrayList<String>();
				for (InfoProxyFactory factory : factories) {
					factoryIds.add(factory.getIdentifier());
				}
				Collections.reverse(factoryIds);
				if (factoryIds.contains(factory.getIdentifier())) {
					StringBuilder msg = new StringBuilder();
					msg.append("Duplicate proxy creation detected:" + "\nNew proxy factory identifier:\n- "
							+ InfoProxyFactory.this.getIdentifier() + "\nExisting factories identifers:\n");
					for (String id : factoryIds) {
						msg.append("- " + id + "\n");
					}
					msg.append(
							"If the factories actually differ, please override the getIdentifier() method to differenciate them");
					throw new ReflectionUIError(msg.toString());
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			GeneratedBasicTypeInfoProxy other = (GeneratedBasicTypeInfoProxy) obj;
			if (base == null) {
				if (other.base != null)
					return false;
			} else if (!base.equals(other.base))
				return false;
			if (factory == null) {
				if (other.factory != null)
					return false;
			} else if (!factory.equals(other.factory))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GeneratedBasicTypeInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base + "]";
		}

	}

	public class GeneratedListTypeInfoProxy extends GeneratedBasicTypeInfoProxy implements IListTypeInfo {

		public GeneratedListTypeInfoProxy(IListTypeInfo type) {
			super(type);
		}

		@Override
		public IValidationJob getListItemAbstractFormValidationJob(ItemPosition itemPosition) {
			return InfoProxyFactory.this.getListItemAbstractFormValidationJob((IListTypeInfo) base, itemPosition);
		}

		@Override
		public boolean isItemNodeValidityDetectionEnabled(ItemPosition itemPosition) {
			return InfoProxyFactory.this.isItemNodeValidityDetectionEnabled((IListTypeInfo) base, itemPosition);
		}

		@Override
		public IFieldInfo getSelectionTargetField(ITypeInfo objectType) {
			return InfoProxyFactory.this.getSelectionTargetField((IListTypeInfo) base, objectType);
		}

		@Override
		public Object[] toArray(Object listValue) {
			return InfoProxyFactory.this.toArray((IListTypeInfo) base, listValue);
		}

		@Override
		public boolean isItemNullValueSupported() {
			return InfoProxyFactory.this.isItemNullValueSupported((IListTypeInfo) base);
		}

		@Override
		public boolean areItemsAutomaticallyPositioned() {
			return InfoProxyFactory.this.areItemsAutomaticallyPositioned((IListTypeInfo) base);
		}

		@Override
		public boolean isMoveAllowed() {
			return InfoProxyFactory.this.isMoveAllowed((IListTypeInfo) base);
		}

		@Override
		public ItemCreationMode getItemCreationMode() {
			return InfoProxyFactory.this.getItemCreationMode((IListTypeInfo) base);
		}

		@Override
		public ValueReturnMode getItemReturnMode() {
			return InfoProxyFactory.this.getItemReturnMode((IListTypeInfo) base);
		}

		@Override
		public ToolsLocation getToolsLocation() {
			return InfoProxyFactory.this.getToolsLocation((IListTypeInfo) base);
		}

		@Override
		public boolean isInsertionAllowed() {
			return InfoProxyFactory.this.isInsertionAllowed((IListTypeInfo) base);
		}

		@Override
		public boolean isRemovalAllowed() {
			return InfoProxyFactory.this.isRemovalAllowed((IListTypeInfo) base);
		}

		@Override
		public boolean canViewItemDetails() {
			return InfoProxyFactory.this.canViewItemDetails((IListTypeInfo) base);
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return InfoProxyFactory.this.getStructuralInfo((IListTypeInfo) base);
		}

		@Override
		public IListItemDetailsAccessMode getDetailsAccessMode() {
			return InfoProxyFactory.this.getDetailsAccessMode((IListTypeInfo) base);
		}

		@Override
		public ITypeInfo getItemType() {
			return InfoProxyFactory.this.getItemType((IListTypeInfo) base);
		}

		@Override
		public void replaceContent(Object listValue, Object[] array) {
			InfoProxyFactory.this.replaceContent((IListTypeInfo) base, listValue, array);
		}

		@Override
		public Object fromArray(Object[] array) {
			return InfoProxyFactory.this.fromArray((IListTypeInfo) base, array);
		}

		@Override
		public boolean canInstantiateFromArray() {
			return InfoProxyFactory.this.canInstantiateFromArray((IListTypeInfo) base);
		}

		@Override
		public boolean canReplaceContent() {
			return InfoProxyFactory.this.canReplaceContent((IListTypeInfo) base);
		}

		@Override
		public List<IDynamicListAction> getDynamicActions(List<? extends ItemPosition> selection,
				Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
			return InfoProxyFactory.this.getDynamicActions((IListTypeInfo) base, selection,
					listModificationFactoryAccessor);
		}

		@Override
		public List<IDynamicListProperty> getDynamicProperties(List<? extends ItemPosition> selection,
				Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
			return InfoProxyFactory.this.getDynamicProperties((IListTypeInfo) base, selection,
					listModificationFactoryAccessor);
		}

		@Override
		public String toString() {
			return "GeneratedListTypeInfoProxy [name=" + getName() + ", itemType=" + getItemType() + ", factory="
					+ factory + ", base=" + base + "]";
		}

	}

	public class GeneratedEnumerationTypeInfoProxy extends GeneratedBasicTypeInfoProxy implements IEnumerationTypeInfo {

		public GeneratedEnumerationTypeInfoProxy(IEnumerationTypeInfo type) {
			super(type);
		}

		@Override
		public boolean isDynamicEnumeration() {
			return InfoProxyFactory.this.isDynamicEnumeration((IEnumerationTypeInfo) base);
		}

		@Override
		public Object[] getValues() {
			return InfoProxyFactory.this.getValues((IEnumerationTypeInfo) base);
		}

		@Override
		public IEnumerationItemInfo getValueInfo(Object object) {
			IEnumerationItemInfo itemInfo = InfoProxyFactory.this.getValueInfo((IEnumerationTypeInfo) base, object);
			return wrapEnumerationItemInfo(itemInfo, (IEnumerationTypeInfo) base);
		}

		@Override
		public String toString() {
			return "GeneratedEnumerationTypeInfoProxy [name=" + getName() + ",factory=" + factory + ", base=" + base
					+ "]";
		}

	}

	public class GeneratedMapEntryTypeInfoProxy extends GeneratedBasicTypeInfoProxy implements IMapEntryTypeInfo {

		public GeneratedMapEntryTypeInfoProxy(IMapEntryTypeInfo type) {
			super(type);
		}

		@Override
		public IFieldInfo getKeyField() {
			return InfoProxyFactory.this.getKeyField((IMapEntryTypeInfo) base);
		}

		@Override
		public IFieldInfo getValueField() {
			return InfoProxyFactory.this.getValueField((IMapEntryTypeInfo) base);
		}

		@Override
		public String toString() {
			return "GeneratedMapEntryTypeInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base
					+ "]";
		}

	}

	public class GeneratedFieldInfoProxy extends AbstractInfoProxy implements IFieldInfo {

		protected InfoProxyFactory factory = InfoProxyFactory.this;

		protected IFieldInfo base;
		protected ITypeInfo objectType;

		public GeneratedFieldInfoProxy(IFieldInfo field, ITypeInfo objectType) {
			this.base = field;
			this.objectType = objectType;
		}

		@Override
		public String getName() {
			return InfoProxyFactory.this.getName(base, objectType);
		}

		@Override
		public boolean isHidden() {
			return InfoProxyFactory.this.isHidden(base, objectType);
		}

		@Override
		public boolean isRelevant(Object object) {
			return InfoProxyFactory.this.isRelevant(base, object, objectType);
		}

		@Override
		public IValidationJob getValueAbstractFormValidationJob(Object object) {
			return InfoProxyFactory.this.getValueAbstractFormValidationJob(base, object, objectType);
		}

		@Override
		public List<IMethodInfo> getAlternativeConstructors(Object object) {
			return InfoProxyFactory.this.getAlternativeConstructors(base, object, objectType);
		}

		@Override
		public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
			return InfoProxyFactory.this.getAlternativeListItemConstructors(base, object, objectType);
		}

		@Override
		public double getDisplayAreaHorizontalWeight() {
			return InfoProxyFactory.this.getDisplayAreaHorizontalWeight(base, objectType);
		}

		@Override
		public double getDisplayAreaVerticalWeight() {
			return InfoProxyFactory.this.getDisplayAreaVerticalWeight(base, objectType);
		}

		@Override
		public boolean isDisplayAreaHorizontallyFilled() {
			return InfoProxyFactory.this.isDisplayAreaHorizontallyFilled(base, objectType);
		}

		@Override
		public boolean isDisplayAreaVerticallyFilled() {
			return InfoProxyFactory.this.isDisplayAreaVerticallyFilled(base, objectType);
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean visible) {
			InfoProxyFactory.this.onControlVisibilityChange(base, object, visible, objectType);
		}

		@Override
		public String getCaption() {
			return InfoProxyFactory.this.getCaption(base, objectType);
		}

		@Override
		public void setValue(Object object, Object value) {
			InfoProxyFactory.this.setValue(base, object, value, objectType);
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			return InfoProxyFactory.this.getNextUpdateCustomUndoJob(base, object, value, objectType);
		}

		@Override
		public Runnable getPreviousUpdateCustomRedoJob(Object object, Object value) {
			return InfoProxyFactory.this.getPreviousUpdateCustomRedoJob(base, object, value, objectType);
		}

		@Override
		public boolean isGetOnly() {
			return InfoProxyFactory.this.isGetOnly(base, objectType);
		}

		@Override
		public boolean isTransient() {
			return InfoProxyFactory.this.isTransient(base, objectType);
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return InfoProxyFactory.this.getValueReturnMode(base, objectType);
		}

		@Override
		public boolean isNullValueDistinct() {
			return InfoProxyFactory.this.isNullValueDistinct(base, objectType);
		}

		@Override
		public String getNullValueLabel() {
			return InfoProxyFactory.this.getNullValueLabel(base, objectType);

		}

		@Override
		public Object getValue(Object object) {
			return InfoProxyFactory.this.getValue(base, object, objectType);
		}

		@Override
		public boolean hasValueOptions(Object object) {
			return InfoProxyFactory.this.hasValueOptions(base, object, objectType);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return InfoProxyFactory.this.getValueOptions(base, object, objectType);
		}

		@Override
		public ITypeInfo getType() {
			return InfoProxyFactory.this.getType(base, objectType);
		}

		@Override
		public InfoCategory getCategory() {
			return InfoProxyFactory.this.getCategory(base, objectType);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyFactory.this.getOnlineHelp(base, objectType);
		}

		@Override
		public boolean isFormControlMandatory() {
			return InfoProxyFactory.this.isFormControlMandatory(base, objectType);
		}

		@Override
		public boolean isFormControlEmbedded() {
			return InfoProxyFactory.this.isFormControlEmbedded(base, objectType);
		}

		@Override
		public IInfoFilter getFormControlFilter() {
			return InfoProxyFactory.this.getFormControlFilter(base, objectType);
		}

		public long getAutoUpdatePeriodMilliseconds() {
			return InfoProxyFactory.this.getAutoUpdatePeriodMilliseconds(base, objectType);
		}

		@Override
		public boolean isControlValueValiditionEnabled() {
			return InfoProxyFactory.this.isControlValueValiditionEnabled(base, objectType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyFactory.this.getSpecificProperties(base, objectType);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			GeneratedFieldInfoProxy other = (GeneratedFieldInfoProxy) obj;
			if (base == null) {
				if (other.base != null)
					return false;
			} else if (!base.equals(other.base))
				return false;
			if (objectType == null) {
				if (other.objectType != null)
					return false;
			} else if (!objectType.equals(other.objectType))
				return false;
			if (factory == null) {
				if (other.factory != null)
					return false;
			} else if (!factory.equals(other.factory))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GeneratedFieldInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base + "]";
		}

	}

	public class GeneratedMethodInfoProxy extends AbstractInfoProxy implements IMethodInfo {

		protected InfoProxyFactory factory = InfoProxyFactory.this;

		protected IMethodInfo base;
		protected ITypeInfo objectType;

		public GeneratedMethodInfoProxy(IMethodInfo method, ITypeInfo objectType) {
			this.base = method;
			this.objectType = objectType;
		}

		@Override
		public String getName() {
			return InfoProxyFactory.this.getName(base, objectType);
		}

		@Override
		public boolean isHidden() {
			return InfoProxyFactory.this.isHidden(base, objectType);
		}

		@Override
		public boolean isRelevant(Object object) {
			return InfoProxyFactory.this.isRelevant(base, object, objectType);
		}

		@Override
		public IValidationJob getReturnValueAbstractFormValidationJob(Object object, Object returnValue) {
			return InfoProxyFactory.this.getReturnValueAbstractFormValidationJob(base, object, returnValue,
					objectType);
		}

		@Override
		public boolean isControlReturnValueValiditionEnabled() {
			return InfoProxyFactory.this.isControlReturnValueValiditionEnabled(base, objectType);
		}

		@Override
		public boolean isEnabled(Object object) {
			return InfoProxyFactory.this.isEnabled(base, object, objectType);
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean visible) {
			InfoProxyFactory.this.onControlVisibilityChange(base, object, visible, objectType);
		}

		@Override
		public String getSignature() {
			return InfoProxyFactory.this.getSignature(base, objectType);
		}

		@Override
		public String getCaption() {
			return InfoProxyFactory.this.getCaption(base, objectType);
		}

		@Override
		public String getParametersValidationCustomCaption() {
			return InfoProxyFactory.this.getParametersValidationCustomCaption(base, objectType);
		}

		@Override
		public String getExecutionSuccessMessage() {
			return InfoProxyFactory.this.getExecutionSuccessMessage(base, objectType);
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return InfoProxyFactory.this.getReturnValueType(base, objectType);
		}

		@Override
		public boolean isNullReturnValueDistinct() {
			return InfoProxyFactory.this.isNullReturnValueDistinct(base, objectType);
		}

		@Override
		public boolean isReturnValueDetached() {
			return InfoProxyFactory.this.isReturnValueDetached(base, objectType);
		}

		@Override
		public boolean isReturnValueIgnored() {
			return InfoProxyFactory.this.isReturnValueIgnored(base, objectType);
		}

		@Override
		public List<IParameterInfo> getParameters() {
			List<IParameterInfo> result = new ArrayList<IParameterInfo>();
			for (IParameterInfo param : InfoProxyFactory.this.getParameters(base, objectType)) {
				result.add(wrapParameterInfo(param, base, objectType));
			}
			return result;
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return InfoProxyFactory.this.invoke(base, object, invocationData, objectType);
		}

		@Override
		public String getConfirmationMessage(Object object, InvocationData invocationData) {
			return InfoProxyFactory.this.getConfirmationMessage(base, object, invocationData, objectType);
		}

		@Override
		public boolean isReadOnly() {
			return InfoProxyFactory.this.isReadOnly(base, objectType);
		}

		@Override
		public String getNullReturnValueLabel() {
			return InfoProxyFactory.this.getNullReturnValueLabel(base, objectType);
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return InfoProxyFactory.this.getValueReturnMode(base, objectType);
		}

		@Override
		public InfoCategory getCategory() {
			return InfoProxyFactory.this.getCategory(base, objectType);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyFactory.this.getOnlineHelp(base, objectType);
		}

		@Override
		public ResourcePath getIconImagePath() {
			return InfoProxyFactory.this.getIconImagePath(base, objectType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyFactory.this.getSpecificProperties(base, objectType);
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
			InfoProxyFactory.this.validateParameters(base, objectType, object, invocationData);
		}

		@Override
		public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
			return InfoProxyFactory.this.getNextInvocationUndoJob(base, objectType, object, invocationData);
		}

		@Override
		public Runnable getPreviousInvocationCustomRedoJob(Object object, InvocationData invocationData) {
			return InfoProxyFactory.this.getPreviousInvocationCustomRedoJob(base, objectType, object, invocationData);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			GeneratedMethodInfoProxy other = (GeneratedMethodInfoProxy) obj;
			if (base == null) {
				if (other.base != null)
					return false;
			} else if (!base.equals(other.base))
				return false;
			if (objectType == null) {
				if (other.objectType != null)
					return false;
			} else if (!objectType.equals(other.objectType))
				return false;
			if (factory == null) {
				if (other.factory != null)
					return false;
			} else if (!factory.equals(other.factory))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GeneratedMethodInfoProxy [signature=" + getSignature() + ", factory=" + factory + ", base=" + base
					+ "]";
		}

	}

	public class GeneratedConstructorInfoProxy extends GeneratedMethodInfoProxy {

		public GeneratedConstructorInfoProxy(IMethodInfo ctor, ITypeInfo objectType) {
			super(ctor, objectType);
		}

	}

	public class GeneratedParameterInfoProxy extends AbstractInfoProxy implements IParameterInfo {

		protected InfoProxyFactory factory = InfoProxyFactory.this;

		protected IParameterInfo base;
		protected IMethodInfo method;
		protected ITypeInfo objectType;

		public GeneratedParameterInfoProxy(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
			this.base = param;
			this.method = method;
			this.objectType = objectType;
		}

		@Override
		public String getName() {
			return InfoProxyFactory.this.getName(base, method, objectType);
		}

		@Override
		public String getCaption() {
			return InfoProxyFactory.this.getCaption(base, method, objectType);
		}

		@Override
		public boolean isHidden() {
			return InfoProxyFactory.this.isHidden(base, method, objectType);
		}

		@Override
		public boolean isNullValueDistinct() {
			return InfoProxyFactory.this.isNullValueDistinct(base, method, objectType);
		}

		@Override
		public ITypeInfo getType() {
			return InfoProxyFactory.this.getType(base, method, objectType);
		}

		@Override
		public int getPosition() {
			return InfoProxyFactory.this.getPosition(base, method, objectType);
		}

		@Override
		public Object getDefaultValue(Object object) {
			return InfoProxyFactory.this.getDefaultValue(base, object, method, objectType);
		}

		@Override
		public boolean hasValueOptions(Object object) {
			return InfoProxyFactory.this.hasValueOptions(base, object, method, objectType);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return InfoProxyFactory.this.getValueOptions(base, object, method, objectType);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyFactory.this.getOnlineHelp(base, method, objectType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyFactory.this.getSpecificProperties(base, method, objectType);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
			result = prime * result + ((method == null) ? 0 : method.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			GeneratedParameterInfoProxy other = (GeneratedParameterInfoProxy) obj;
			if (base == null) {
				if (other.base != null)
					return false;
			} else if (!base.equals(other.base))
				return false;
			if (objectType == null) {
				if (other.objectType != null)
					return false;
			} else if (!objectType.equals(other.objectType))
				return false;
			if (factory == null) {
				if (other.factory != null)
					return false;
			} else if (!factory.equals(other.factory))
				return false;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GeneratedParameterInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base
					+ ", method=" + method + "]";
		}

	}

	public class GeneratedEnumerationItemInfoProxy extends AbstractInfoProxy implements IEnumerationItemInfo {

		protected InfoProxyFactory factory = InfoProxyFactory.this;

		protected IEnumerationItemInfo base;
		protected IEnumerationTypeInfo parentEnumType;

		public GeneratedEnumerationItemInfoProxy(IEnumerationItemInfo base, IEnumerationTypeInfo parentEnumType) {
			this.base = base;
			this.parentEnumType = parentEnumType;
		}

		@Override
		public String getName() {
			return InfoProxyFactory.this.getName(base, parentEnumType);
		}

		@Override
		public Object getValue() {
			return InfoProxyFactory.this.getValue(base, parentEnumType);
		}

		@Override
		public String getCaption() {
			return InfoProxyFactory.this.getCaption(base, parentEnumType);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyFactory.this.getOnlineHelp(base, parentEnumType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyFactory.this.getSpecificProperties(base, parentEnumType);
		}

		@Override
		public ResourcePath getIconImagePath() {
			return InfoProxyFactory.this.getIconImagePath(base, parentEnumType);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
			result = prime * result + ((parentEnumType == null) ? 0 : parentEnumType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			GeneratedEnumerationItemInfoProxy other = (GeneratedEnumerationItemInfoProxy) obj;
			if (base == null) {
				if (other.base != null)
					return false;
			} else if (!base.equals(other.base))
				return false;
			if (factory == null) {
				if (other.factory != null)
					return false;
			} else if (!factory.equals(other.factory))
				return false;
			if (parentEnumType == null) {
				if (other.parentEnumType != null)
					return false;
			} else if (!parentEnumType.equals(other.parentEnumType))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GeneratedEnumerationItemInfoProxy [name=" + getName() + ", factory=" + factory + ",base=" + base
					+ "]";
		}

	}

}
