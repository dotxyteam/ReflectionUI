
package xy.reflect.ui.info.type.factory;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfoProxy;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransactionInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
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
import xy.reflect.ui.info.type.ITypeInfo.MethodsLayout;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo.InitialItemValueCreationOption;
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
 * methods of this class must be overriden.
 * 
 * By convention these methods have the same name as the methods of the
 * underlying abstract UI model elements that they affect. In addition, they
 * have parameters that specify the invocation context.
 * 
 * Ex: Overriding {@link #getCaption(IFieldInfo, ITypeInfo)} allows to change
 * the behavior of {@link IFieldInfo#getCaption()} according to the containing
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

	public ITypeInfo wrapSubTypeInfo(ITypeInfo type) {
		return type;
	}

	public ITypeInfo wrapItemTypeInfo(ITypeInfo type) {
		return type;
	}

	public ITypeInfo wrapMethodReturnValueTypeInfo(ITypeInfo type) {
		return type;
	}

	public ITypeInfo wrapParameterTypeInfo(ITypeInfo type) {
		return type;
	}

	public ITypeInfo wrapFieldTypeInfo(ITypeInfo type) {
		return type;
	}

	public IFieldInfo wrapFieldInfo(IFieldInfo field, ITypeInfo containingType) {
		return new GeneratedFieldInfoProxy(field, containingType);
	}

	public IParameterInfo wrapParameterInfo(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return new GeneratedParameterInfoProxy(param, method, containingType);
	}

	public IEnumerationItemInfo wrapEnumerationItemInfo(IEnumerationItemInfo itemInfo, ITypeInfo parentEnumType) {
		return new GeneratedEnumerationItemInfoProxy(itemInfo, parentEnumType);
	}

	public IMethodInfo wrapConstructorInfo(IMethodInfo constructor, ITypeInfo containingType) {
		return new GeneratedConstructorInfoProxy(constructor, containingType);
	}

	public IMethodInfo wrapMethodInfo(IMethodInfo method, ITypeInfo containingType) {
		return new GeneratedMethodInfoProxy(method, containingType);
	}

	public ITypeInfo unwrapTypeInfo(final ITypeInfo type) {
		GeneratedBasicTypeInfoProxy proxy = (GeneratedBasicTypeInfoProxy) type;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	public IFieldInfo unwrapFieldInfo(final IFieldInfo field) {
		GeneratedFieldInfoProxy proxy = (GeneratedFieldInfoProxy) field;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	public IMethodInfo unwrapMethodInfo(final IMethodInfo method) {
		GeneratedMethodInfoProxy proxy = (GeneratedMethodInfoProxy) method;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	public IParameterInfo unwrapParameterInfo(final IParameterInfo param) {
		GeneratedParameterInfoProxy proxy = (GeneratedParameterInfoProxy) param;
		if (!proxy.factory.equals(InfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

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

	protected Method getDebugInfoEnclosingMethod() {
		return getClass().getEnclosingMethod();
	}

	protected Object getDefaultValue(IParameterInfo param, IMethodInfo method, ITypeInfo containingType,
			Object object) {
		return param.getDefaultValue(object);
	}

	protected boolean hasValueOptions(IParameterInfo param, IMethodInfo method, ITypeInfo containingType,
			Object object) {
		return param.hasValueOptions(object);
	}

	protected Object[] getValueOptions(IParameterInfo param, IMethodInfo method, ITypeInfo containingType,
			Object object) {
		return param.getValueOptions(object);
	}

	protected int getPosition(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.getPosition();
	}

	protected ITypeInfo getType(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return wrapParameterTypeInfo(param.getType());
	}

	protected boolean isNullValueDistinct(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.isNullValueDistinct();
	}

	protected String getCaption(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.getCaption();
	}

	protected boolean isHidden(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.isHidden();
	}

	protected String getName(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.getName();
	}

	protected InfoCategory getCategory(IFieldInfo field, ITypeInfo containingType) {
		return field.getCategory();
	}

	protected ITypeInfo getType(IFieldInfo field, ITypeInfo containingType) {
		return wrapFieldTypeInfo(field.getType());
	}

	protected List<IMethodInfo> getAlternativeConstructors(Object object, IFieldInfo field, ITypeInfo containingType) {
		return field.getAlternativeConstructors(object);
	}

	protected List<IMethodInfo> getAlternativeListItemConstructors(Object object, IFieldInfo field,
			ITypeInfo containingType) {
		return field.getAlternativeListItemConstructors(object);
	}

	protected Object getValue(Object object, IFieldInfo field, ITypeInfo containingType) {
		return field.getValue(object);
	}

	protected boolean hasValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
		return field.hasValueOptions(object);
	}

	protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
		return field.getValueOptions(object);
	}

	protected boolean isNullValueDistinct(IFieldInfo field, ITypeInfo containingType) {
		return field.isNullValueDistinct();
	}

	protected String getNullValueLabel(IFieldInfo field, ITypeInfo containingType) {
		return field.getNullValueLabel();
	}

	protected boolean isTransient(IFieldInfo field, ITypeInfo containingType) {
		return field.isTransient();
	}

	protected boolean isGetOnly(IFieldInfo field, ITypeInfo containingType) {
		return field.isGetOnly();
	}

	protected ValueReturnMode getValueReturnMode(IFieldInfo field, ITypeInfo containingType) {
		return field.getValueReturnMode();
	}

	protected void setValue(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
		field.setValue(object, value);
	}

	protected Runnable getNextUpdateCustomUndoJob(Object object, Object value, IFieldInfo field,
			ITypeInfo containingType) {
		return field.getNextUpdateCustomUndoJob(object, value);
	}

	protected String toString(ITypeInfo type, Object object) {
		return type.toString(object);
	}

	protected boolean canCopy(ITypeInfo type, Object object) {
		return type.canCopy(object);
	}

	protected Object copy(ITypeInfo type, Object object) {
		return type.copy(object);
	}

	protected ResourcePath getIconImagePath(ITypeInfo type) {
		return type.getIconImagePath();
	}

	protected FieldsLayout getFieldsLayout(ITypeInfo type) {
		return type.getFieldsLayout();
	}

	protected MethodsLayout getMethodsLayout(ITypeInfo type) {
		return type.getMethodsLayout();
	}

	protected MenuModel getMenuModel(ITypeInfo type) {
		return type.getMenuModel();
	}

	protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
		return field.getCaption();
	}

	protected String getName(IFieldInfo field, ITypeInfo containingType) {
		return field.getName();
	}

	protected boolean isHidden(IFieldInfo field, ITypeInfo containingType) {
		return field.isHidden();
	}

	protected double getDisplayAreaHorizontalWeight(IFieldInfo field, ITypeInfo containingType) {
		return field.getDisplayAreaHorizontalWeight();
	}

	protected double getDisplayAreaVerticalWeight(IFieldInfo field, ITypeInfo containingType) {
		return field.getDisplayAreaVerticalWeight();
	}

	protected InfoCategory getCategory(IMethodInfo method, ITypeInfo containingType) {
		return method.getCategory();
	}

	protected boolean isReadOnly(IMethodInfo method, ITypeInfo containingType) {
		return method.isReadOnly();
	}

	protected String getNullReturnValueLabel(IMethodInfo method, ITypeInfo containingType) {
		return method.getNullReturnValueLabel();
	}

	protected ValueReturnMode getValueReturnMode(IMethodInfo method, ITypeInfo containingType) {
		return method.getValueReturnMode();
	}

	protected Object invoke(Object object, InvocationData invocationData, IMethodInfo method,
			ITypeInfo containingType) {
		return method.invoke(object, invocationData);
	}

	protected String getConfirmationMessage(Object object, InvocationData invocationData, IMethodInfo method,
			ITypeInfo containingType) {
		return method.getConfirmationMessage(object, invocationData);
	}

	protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo containingType) {
		return method.getParameters();
	}

	protected ITypeInfo getReturnValueType(IMethodInfo method, ITypeInfo containingType) {
		return wrapMethodReturnValueTypeInfo(method.getReturnValueType());
	}

	protected boolean isNullReturnValueDistinct(IMethodInfo method, ITypeInfo containingType) {
		return method.isNullReturnValueDistinct();
	}

	protected boolean isReturnValueDetached(IMethodInfo method, ITypeInfo containingType) {
		return method.isReturnValueDetached();
	}

	protected boolean isReturnValueIgnored(IMethodInfo method, ITypeInfo containingType) {
		return method.isReturnValueIgnored();
	}

	protected String getCaption(IMethodInfo method, ITypeInfo containingType) {
		return method.getCaption();
	}

	protected String getParametersValidationCustomCaption(IMethodInfo method, ITypeInfo containingType) {
		return method.getParametersValidationCustomCaption();
	}

	protected String getName(IMethodInfo method, ITypeInfo containingType) {
		return method.getName();
	}

	protected boolean isHidden(IMethodInfo method, ITypeInfo containingType) {
		return method.isHidden();
	}

	protected void onControlVisibilityChange(Object object, boolean visible, IMethodInfo method,
			ITypeInfo containingType) {
		method.onControlVisibilityChange(object, visible);
	}

	protected boolean isEnabled(Object object, IMethodInfo method, ITypeInfo containingType) {
		return method.isEnabled(object);
	}

	protected void onControlVisibilityChange(Object object, boolean visible, IFieldInfo field,
			ITypeInfo containingType) {
		field.onControlVisibilityChange(object, visible);
	}

	protected String getSignature(IMethodInfo method, ITypeInfo containingType) {
		return method.getSignature();
	}

	protected Object[] getValues(IEnumerationTypeInfo type) {
		return type.getValues();
	}

	protected boolean isDynamicEnumeration(IEnumerationTypeInfo type) {
		return type.isDynamicEnumeration();
	}

	protected void replaceContent(IListTypeInfo type, Object listValue, Object[] array) {
		type.replaceContent(listValue, array);
	}

	protected Object fromArray(IListTypeInfo type, Object[] array) {
		return type.fromArray(array);
	}

	protected boolean canInstanciateFromArray(IListTypeInfo type) {
		return type.canInstanciateFromArray();
	}

	protected boolean canReplaceContent(IListTypeInfo type) {
		return type.canReplaceContent();
	}

	protected List<IDynamicListAction> getDynamicActions(IListTypeInfo type, List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		return type.getDynamicActions(selection, listModificationFactoryAccessor);
	}

	protected List<IDynamicListProperty> getDynamicProperties(IListTypeInfo type,
			List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		return type.getDynamicProperties(selection, listModificationFactoryAccessor);
	}

	protected ITypeInfo getItemType(IListTypeInfo type) {
		return wrapItemTypeInfo(type.getItemType());
	}

	protected IListStructuralInfo getStructuralInfo(IListTypeInfo type) {
		return type.getStructuralInfo();
	}

	protected IListItemDetailsAccessMode getDetailsAccessMode(IListTypeInfo type) {
		return type.getDetailsAccessMode();
	}

	protected boolean isOrdered(IListTypeInfo type) {
		return type.isOrdered();
	}

	protected boolean isItemNullValueSupported(IListTypeInfo type) {
		return type.isItemNullValueSupported();
	}

	protected InitialItemValueCreationOption getInitialItemValueCreationOption(IListTypeInfo type) {
		return type.getInitialItemValueCreationOption();
	}

	protected ValueReturnMode getItemReturnMode(IListTypeInfo type) {
		return type.getItemReturnMode();
	}

	protected boolean isInsertionAllowed(IListTypeInfo type) {
		return type.isInsertionAllowed();
	}

	protected boolean isRemovalAllowed(IListTypeInfo type) {
		return type.isRemovalAllowed();
	}

	protected boolean canViewItemDetails(IListTypeInfo type) {
		return type.canViewItemDetails();
	}

	protected Object[] toArray(IListTypeInfo type, Object listValue) {
		return type.toArray(listValue);
	}

	protected List<IMethodInfo> getConstructors(ITypeInfo type) {
		return type.getConstructors();
	}

	protected List<IFieldInfo> getFields(ITypeInfo type) {
		return type.getFields();
	}

	protected List<IMethodInfo> getMethods(ITypeInfo type) {
		return type.getMethods();
	}

	protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
		return type.getPolymorphicInstanceSubTypes();
	}

	protected boolean isConcrete(ITypeInfo type) {
		return type.isConcrete();
	}

	protected boolean isPrimitive(ITypeInfo type) {
		return type.isPrimitive();
	}

	protected boolean isImmutable(ITypeInfo type) {
		return type.isImmutable();
	}

	protected boolean canPersist(ITypeInfo type) {
		return type.canPersist();
	}

	protected void save(ITypeInfo type, Object object, OutputStream out) {
		type.save(object, out);
	}

	protected void load(ITypeInfo type, Object object, InputStream in) {
		type.load(object, in);
	}

	protected boolean isModificationStackAccessible(ITypeInfo type) {
		return type.isModificationStackAccessible();
	}

	protected boolean supports(ITypeInfo type, Object object) {
		return type.supports(object);
	}

	protected String getCaption(ITypeInfo type) {
		return type.getCaption();
	}

	protected boolean onFormVisibilityChange(ITypeInfo type, Object object, boolean visible) {
		return type.onFormVisibilityChange(object, visible);
	}

	protected String getName(ITypeInfo type) {
		return type.getName();
	}

	protected String getName(IApplicationInfo appInfo) {
		return appInfo.getName();
	}

	protected String getCaption(IApplicationInfo appInfo) {
		return appInfo.getCaption();
	}

	protected boolean isSystemIntegrationCrossPlatform(IApplicationInfo appInfo) {
		return appInfo.isSystemIntegrationCrossPlatform();
	}

	protected ResourcePath getIconImagePath(IApplicationInfo appInfo) {
		return appInfo.getIconImagePath();
	}

	protected String getOnlineHelp(IApplicationInfo appInfo) {
		return appInfo.getOnlineHelp();
	}

	protected ColorSpecification getMainBackgroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainBackgroundColor();
	}

	protected ColorSpecification getMainForegroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainForegroundColor();
	}

	protected ColorSpecification getMainBorderColor(IApplicationInfo appInfo) {
		return appInfo.getMainBorderColor();
	}

	protected ColorSpecification getMainEditorBackgroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainEditorBackgroundColor();
	}

	protected ColorSpecification getMainEditorForegroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainEditorForegroundColor();
	}

	protected ResourcePath getMainBackgroundImagePath(IApplicationInfo appInfo) {
		return appInfo.getMainBackgroundImagePath();
	}

	protected ColorSpecification getMainButtonBackgroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainButtonBackgroundColor();
	}

	protected ResourcePath getMainButtonBackgroundImagePath(IApplicationInfo appInfo) {
		return appInfo.getMainButtonBackgroundImagePath();
	}

	protected ColorSpecification getMainButtonForegroundColor(IApplicationInfo appInfo) {
		return appInfo.getMainButtonForegroundColor();
	}

	protected ColorSpecification getMainButtonBorderColor(IApplicationInfo appInfo) {
		return appInfo.getMainButtonBorderColor();
	}

	protected ColorSpecification getTitleBackgroundColor(IApplicationInfo appInfo) {
		return appInfo.getTitleBackgroundColor();
	}

	protected ColorSpecification getTitleForegroundColor(IApplicationInfo appInfo) {
		return appInfo.getTitleForegroundColor();
	}

	protected Map<String, Object> getSpecificProperties(IApplicationInfo appInfo) {
		return appInfo.getSpecificProperties();
	}

	protected ITypeInfoSource getSource(final ITypeInfo type) {
		return type.getSource();
	}

	protected CategoriesStyle getCategoriesStyle(ITypeInfo type) {
		return type.getCategoriesStyle();
	}

	protected ITransactionInfo getTransaction(ITypeInfo type, Object object) {
		return type.getTransaction(object);
	}

	protected ResourcePath getFormBackgroundImagePath(ITypeInfo type) {
		return type.getFormBackgroundImagePath();
	}

	protected ColorSpecification getFormBackgroundColor(ITypeInfo type) {
		return type.getFormBackgroundColor();
	}

	protected ColorSpecification getFormForegroundColor(ITypeInfo type) {
		return type.getFormForegroundColor();
	}

	protected ColorSpecification getFormBorderColor(ITypeInfo type) {
		return type.getFormBorderColor();
	}

	protected ColorSpecification getFormButtonForegroundColor(ITypeInfo type) {
		return type.getFormButtonForegroundColor();
	}

	protected ColorSpecification getFormButtonBorderColor(ITypeInfo type) {
		return type.getFormButtonBorderColor();
	}

	protected ResourcePath getFormButtonBackgroundImagePath(ITypeInfo type) {
		return type.getFormButtonBackgroundImagePath();
	}

	protected ColorSpecification getFormButtonBackgroundColor(ITypeInfo type) {
		return type.getFormButtonBackgroundColor();
	}

	protected ColorSpecification getCategoriesForegroundColor(ITypeInfo type) {
		return type.getCategoriesForegroundColor();
	}

	protected ColorSpecification getCategoriesBackgroundColor(ITypeInfo type) {
		return type.getCategoriesBackgroundColor();
	}

	protected ColorSpecification getFormEditorsBackgroundColor(ITypeInfo type) {
		return type.getFormEditorsBackgroundColor();
	}

	protected ColorSpecification getFormEditorsForegroundColor(ITypeInfo type) {
		return type.getFormEditorsForegroundColor();
	}

	protected Dimension getFormPreferredSize(ITypeInfo type) {
		return type.getFormPreferredSize();
	}

	protected int getFormSpacing(ITypeInfo type) {
		return type.getFormSpacing();
	}

	protected IFieldInfo getKeyField(IMapEntryTypeInfo type) {
		return wrapFieldInfo(type.getKeyField(), type);
	}

	protected IFieldInfo getValueField(IMapEntryTypeInfo type) {
		return wrapFieldInfo(type.getValueField(), type);
	}

	protected boolean isFormControlMandatory(IFieldInfo field, ITypeInfo containingType) {
		return field.isFormControlMandatory();
	}

	protected boolean isFormControlEmbedded(IFieldInfo field, ITypeInfo containingType) {
		return field.isFormControlEmbedded();
	}

	protected IInfoFilter getFormControlFilter(IFieldInfo field, ITypeInfo containingType) {
		return field.getFormControlFilter();
	}

	protected long getAutoUpdatePeriodMilliseconds(IFieldInfo field, ITypeInfo containingType) {
		return field.getAutoUpdatePeriodMilliseconds();
	}

	protected String getOnlineHelp(IFieldInfo field, ITypeInfo containingType) {
		return field.getOnlineHelp();
	}

	protected Map<String, Object> getSpecificProperties(IFieldInfo field, ITypeInfo containingType) {
		return field.getSpecificProperties();
	}

	protected String getOnlineHelp(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.getOnlineHelp();
	}

	protected Map<String, Object> getSpecificProperties(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.getSpecificProperties();
	}

	protected String getOnlineHelp(ITypeInfo type) {
		return type.getOnlineHelp();
	}

	protected void validate(ITypeInfo type, Object object) throws Exception {
		type.validate(object);
	}

	protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
		return type.getSpecificProperties();
	}

	protected String getOnlineHelp(IMethodInfo method, ITypeInfo containingType) {
		return method.getOnlineHelp();
	}

	protected ResourcePath getIconImagePath(IMethodInfo method, ITypeInfo containingType) {
		return method.getIconImagePath();
	}

	protected Map<String, Object> getSpecificProperties(IMethodInfo method, ITypeInfo containingType) {
		return method.getSpecificProperties();
	}

	protected void validateParameters(IMethodInfo method, ITypeInfo containingType, Object object,
			InvocationData invocationData) throws Exception {
		method.validateParameters(object, invocationData);
	}

	protected Runnable getNextInvocationUndoJob(IMethodInfo method, ITypeInfo containingType, Object object,
			InvocationData invocationData) {
		return method.getNextInvocationUndoJob(object, invocationData);
	}

	protected IEnumerationItemInfo getValueInfo(IEnumerationTypeInfo type, Object object) {
		return type.getValueInfo(object);
	}

	protected String getName(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getName();
	}

	protected Object getValue(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getValue();
	}

	protected Map<String, Object> getSpecificProperties(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getSpecificProperties();
	}

	protected ResourcePath getIconImagePath(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getIconImagePath();
	}

	protected String getOnlineHelp(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getOnlineHelp();
	}

	protected String getCaption(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getCaption();
	}

	protected boolean isFactoryTracedFor(ITypeInfo base) {
		return true;
	}

	protected boolean isFactoryTracedFor(IApplicationInfo base) {
		return true;
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
		public ResourcePath getMainButtonBackgroundImagePath() {
			return InfoProxyFactory.this.getMainButtonBackgroundImagePath(base);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return traceActiveFactory(InfoProxyFactory.this.getSpecificProperties(base));
		}

		private Map<String, Object> traceActiveFactory(Map<String, Object> specificProperties) {
			if (!InfoProxyFactory.this.isFactoryTracedFor(base)) {
				return specificProperties;
			}
			Map<String, Object> result = new HashMap<String, Object>(specificProperties);
			@SuppressWarnings("unchecked")
			List<InfoProxyFactory> factories = (List<InfoProxyFactory>) base.getSpecificProperties()
					.get(ACTIVE_FACTORIES_KEY);
			if (factories == null) {
				factories = new ArrayList<InfoProxyFactory>();
			}
			factories.add(InfoProxyFactory.this);
			result.put(ACTIVE_FACTORIES_KEY, factories);
			return result;
		}

		private void checkActiveFactories() {
			if (!InfoProxyFactory.this.isFactoryTracedFor(base)) {
				return;
			}
			@SuppressWarnings("unchecked")
			List<InfoProxyFactory> factories = (List<InfoProxyFactory>) base.getSpecificProperties()
					.get(ACTIVE_FACTORIES_KEY);
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
		public ITransactionInfo getTransaction(Object object) {
			return InfoProxyFactory.this.getTransaction(base, object);
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
		public ColorSpecification getFormEditorsForegroundColor() {
			return InfoProxyFactory.this.getFormEditorsForegroundColor(base);
		}

		@Override
		public ColorSpecification getFormEditorsBackgroundColor() {
			return InfoProxyFactory.this.getFormEditorsBackgroundColor(base);
		}

		@Override
		public Dimension getFormPreferredSize() {
			return InfoProxyFactory.this.getFormPreferredSize(base);
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
		public void save(Object object, OutputStream out) {
			InfoProxyFactory.this.save(base, object, out);
		}

		@Override
		public void load(Object object, InputStream in) {
			InfoProxyFactory.this.load(base, object, in);
		}

		@Override
		public boolean isModificationStackAccessible() {
			return InfoProxyFactory.this.isModificationStackAccessible(base);
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
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo method : InfoProxyFactory.this.getMethods(base)) {
				result.add(wrapMethodInfo(method, base));
			}
			return result;
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (IFieldInfo field : InfoProxyFactory.this.getFields(base)) {
				result.add(wrapFieldInfo(field, base));
			}
			return result;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo constructor : InfoProxyFactory.this.getConstructors(base)) {
				result.add(wrapConstructorInfo(constructor, base));
			}
			return result;
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
		public ResourcePath getIconImagePath() {
			return InfoProxyFactory.this.getIconImagePath(base);
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
		public void validate(Object object) throws Exception {
			InfoProxyFactory.this.validate(base, object);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return traceActiveFactory(InfoProxyFactory.this.getSpecificProperties(base));
		}

		private Map<String, Object> traceActiveFactory(Map<String, Object> specificProperties) {
			if (!InfoProxyFactory.this.isFactoryTracedFor(base)) {
				return specificProperties;
			}
			Map<String, Object> result = new HashMap<String, Object>(specificProperties);
			@SuppressWarnings("unchecked")
			List<InfoProxyFactory> factories = (List<InfoProxyFactory>) base.getSpecificProperties()
					.get(ACTIVE_FACTORIES_KEY);
			if (factories == null) {
				factories = new ArrayList<InfoProxyFactory>();
			}
			factories.add(InfoProxyFactory.this);
			result.put(ACTIVE_FACTORIES_KEY, factories);
			return result;
		}

		private void checkActiveFactories() {
			if (!InfoProxyFactory.this.isFactoryTracedFor(base)) {
				return;
			}
			@SuppressWarnings("unchecked")
			List<InfoProxyFactory> factories = (List<InfoProxyFactory>) base.getSpecificProperties()
					.get(ACTIVE_FACTORIES_KEY);
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
		public Object[] toArray(Object listValue) {
			return InfoProxyFactory.this.toArray((IListTypeInfo) base, listValue);
		}

		@Override
		public boolean isItemNullValueSupported() {
			return InfoProxyFactory.this.isItemNullValueSupported((IListTypeInfo) base);
		}

		@Override
		public InitialItemValueCreationOption getInitialItemValueCreationOption() {
			return InfoProxyFactory.this.getInitialItemValueCreationOption((IListTypeInfo) base);
		}

		@Override
		public ValueReturnMode getItemReturnMode() {
			return InfoProxyFactory.this.getItemReturnMode((IListTypeInfo) base);
		}

		@Override
		public boolean isOrdered() {
			return InfoProxyFactory.this.isOrdered((IListTypeInfo) base);
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
		public boolean canInstanciateFromArray() {
			return InfoProxyFactory.this.canInstanciateFromArray((IListTypeInfo) base);
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
			return wrapEnumerationItemInfo(itemInfo, base);
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
		protected ITypeInfo containingType;

		public GeneratedFieldInfoProxy(IFieldInfo field, ITypeInfo containingType) {
			this.base = field;
			this.containingType = containingType;
		}

		@Override
		public String getName() {
			return InfoProxyFactory.this.getName(base, containingType);
		}

		@Override
		public boolean isHidden() {
			return InfoProxyFactory.this.isHidden(base, containingType);
		}

		@Override
		public List<IMethodInfo> getAlternativeConstructors(Object object) {
			return InfoProxyFactory.this.getAlternativeConstructors(object, base, containingType);
		}

		@Override
		public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
			return InfoProxyFactory.this.getAlternativeListItemConstructors(object, base, containingType);
		}

		@Override
		public double getDisplayAreaHorizontalWeight() {
			return InfoProxyFactory.this.getDisplayAreaHorizontalWeight(base, containingType);
		}

		@Override
		public double getDisplayAreaVerticalWeight() {
			return InfoProxyFactory.this.getDisplayAreaVerticalWeight(base, containingType);
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean visible) {
			InfoProxyFactory.this.onControlVisibilityChange(object, visible, base, containingType);
		}

		@Override
		public String getCaption() {
			return InfoProxyFactory.this.getCaption(base, containingType);
		}

		@Override
		public void setValue(Object object, Object value) {
			InfoProxyFactory.this.setValue(object, value, base, containingType);
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			return InfoProxyFactory.this.getNextUpdateCustomUndoJob(object, value, base, containingType);
		}

		@Override
		public boolean isGetOnly() {
			return InfoProxyFactory.this.isGetOnly(base, containingType);
		}

		@Override
		public boolean isTransient() {
			return InfoProxyFactory.this.isTransient(base, containingType);
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return InfoProxyFactory.this.getValueReturnMode(base, containingType);
		}

		@Override
		public boolean isNullValueDistinct() {
			return InfoProxyFactory.this.isNullValueDistinct(base, containingType);
		}

		@Override
		public String getNullValueLabel() {
			return InfoProxyFactory.this.getNullValueLabel(base, containingType);

		}

		@Override
		public Object getValue(Object object) {
			return InfoProxyFactory.this.getValue(object, base, containingType);
		}

		@Override
		public boolean hasValueOptions(Object object) {
			return InfoProxyFactory.this.hasValueOptions(object, base, containingType);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return InfoProxyFactory.this.getValueOptions(object, base, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return InfoProxyFactory.this.getType(base, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return InfoProxyFactory.this.getCategory(base, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyFactory.this.getOnlineHelp(base, containingType);
		}

		@Override
		public boolean isFormControlMandatory() {
			return InfoProxyFactory.this.isFormControlMandatory(base, containingType);
		}

		@Override
		public boolean isFormControlEmbedded() {
			return InfoProxyFactory.this.isFormControlEmbedded(base, containingType);
		}

		@Override
		public IInfoFilter getFormControlFilter() {
			return InfoProxyFactory.this.getFormControlFilter(base, containingType);
		}

		public long getAutoUpdatePeriodMilliseconds() {
			return InfoProxyFactory.this.getAutoUpdatePeriodMilliseconds(base, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyFactory.this.getSpecificProperties(base, containingType);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
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
			if (containingType == null) {
				if (other.containingType != null)
					return false;
			} else if (!containingType.equals(other.containingType))
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
		protected ITypeInfo containingType;

		public GeneratedMethodInfoProxy(IMethodInfo method, ITypeInfo containingType) {
			this.base = method;
			this.containingType = containingType;
		}

		@Override
		public String getName() {
			return InfoProxyFactory.this.getName(base, containingType);
		}

		@Override
		public boolean isHidden() {
			return InfoProxyFactory.this.isHidden(base, containingType);
		}

		@Override
		public boolean isEnabled(Object object) {
			return InfoProxyFactory.this.isEnabled(object, base, containingType);
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean visible) {
			InfoProxyFactory.this.onControlVisibilityChange(object, visible, base, containingType);
		}

		@Override
		public String getSignature() {
			return InfoProxyFactory.this.getSignature(base, containingType);
		}

		@Override
		public String getCaption() {
			return InfoProxyFactory.this.getCaption(base, containingType);
		}

		@Override
		public String getParametersValidationCustomCaption() {
			return InfoProxyFactory.this.getParametersValidationCustomCaption(base, containingType);
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return InfoProxyFactory.this.getReturnValueType(base, containingType);
		}

		@Override
		public boolean isNullReturnValueDistinct() {
			return InfoProxyFactory.this.isNullReturnValueDistinct(base, containingType);
		}

		@Override
		public boolean isReturnValueDetached() {
			return InfoProxyFactory.this.isReturnValueDetached(base, containingType);
		}

		@Override
		public boolean isReturnValueIgnored() {
			return InfoProxyFactory.this.isReturnValueIgnored(base, containingType);
		}

		@Override
		public List<IParameterInfo> getParameters() {
			List<IParameterInfo> result = new ArrayList<IParameterInfo>();
			for (IParameterInfo param : InfoProxyFactory.this.getParameters(base, containingType)) {
				result.add(wrapParameterInfo(param, base, containingType));
			}
			return result;
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return InfoProxyFactory.this.invoke(object, invocationData, base, containingType);
		}

		@Override
		public String getConfirmationMessage(Object object, InvocationData invocationData) {
			return InfoProxyFactory.this.getConfirmationMessage(object, invocationData, base, containingType);
		}

		@Override
		public boolean isReadOnly() {
			return InfoProxyFactory.this.isReadOnly(base, containingType);
		}

		@Override
		public String getNullReturnValueLabel() {
			return InfoProxyFactory.this.getNullReturnValueLabel(base, containingType);
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return InfoProxyFactory.this.getValueReturnMode(base, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return InfoProxyFactory.this.getCategory(base, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyFactory.this.getOnlineHelp(base, containingType);
		}

		@Override
		public ResourcePath getIconImagePath() {
			return InfoProxyFactory.this.getIconImagePath(base, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyFactory.this.getSpecificProperties(base, containingType);
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
			InfoProxyFactory.this.validateParameters(base, containingType, object, invocationData);
		}

		@Override
		public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
			return InfoProxyFactory.this.getNextInvocationUndoJob(base, containingType, object, invocationData);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
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
			if (containingType == null) {
				if (other.containingType != null)
					return false;
			} else if (!containingType.equals(other.containingType))
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

		public GeneratedConstructorInfoProxy(IMethodInfo ctor, ITypeInfo containingType) {
			super(ctor, containingType);
		}

	}

	public class GeneratedParameterInfoProxy extends AbstractInfoProxy implements IParameterInfo {

		protected InfoProxyFactory factory = InfoProxyFactory.this;

		protected IParameterInfo base;
		protected IMethodInfo method;
		protected ITypeInfo containingType;

		public GeneratedParameterInfoProxy(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			this.base = param;
			this.method = method;
			this.containingType = containingType;
		}

		@Override
		public String getName() {
			return InfoProxyFactory.this.getName(base, method, containingType);
		}

		@Override
		public String getCaption() {
			return InfoProxyFactory.this.getCaption(base, method, containingType);
		}

		@Override
		public boolean isHidden() {
			return InfoProxyFactory.this.isHidden(base, method, containingType);
		}

		@Override
		public boolean isNullValueDistinct() {
			return InfoProxyFactory.this.isNullValueDistinct(base, method, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return InfoProxyFactory.this.getType(base, method, containingType);
		}

		@Override
		public int getPosition() {
			return InfoProxyFactory.this.getPosition(base, method, containingType);
		}

		@Override
		public Object getDefaultValue(Object object) {
			return InfoProxyFactory.this.getDefaultValue(base, method, containingType, object);
		}

		@Override
		public boolean hasValueOptions(Object object) {
			return InfoProxyFactory.this.hasValueOptions(base, method, containingType, object);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return InfoProxyFactory.this.getValueOptions(base, method, containingType, object);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyFactory.this.getOnlineHelp(base, method, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyFactory.this.getSpecificProperties(base, method, containingType);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
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
			if (containingType == null) {
				if (other.containingType != null)
					return false;
			} else if (!containingType.equals(other.containingType))
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
		protected ITypeInfo parentEnumType;

		public GeneratedEnumerationItemInfoProxy(IEnumerationItemInfo base, ITypeInfo parentEnumType) {
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
