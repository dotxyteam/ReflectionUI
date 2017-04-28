package xy.reflect.ui.info.type.factory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.FieldsLayout;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.map.IMapEntryTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.menu.IMenuElementPosition;
import xy.reflect.ui.menu.Menu;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.info.method.InvocationData;

public class TypeInfoProxyFactory implements ITypeInfoProxyFactory {

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
		TypeInfoProxyFactory other = (TypeInfoProxyFactory) obj;
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
	public ITypeInfo get(final ITypeInfo type) {

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

	protected ITypeInfo wrapSubType(ITypeInfo type) {
		return type;
	}

	protected ITypeInfo wrapItemType(ITypeInfo type) {
		return type;
	}

	protected ITypeInfo wrapMethodReturnValueType(ITypeInfo type) {
		return type;
	}

	protected ITypeInfo wrapParameterType(ITypeInfo type) {
		return type;
	}

	protected ITypeInfo wrapFieldType(ITypeInfo type) {
		return type;
	}

	public ITypeInfo getUnderProxy(final ITypeInfo type) {
		GeneratedBasicTypeInfoProxy proxy = (GeneratedBasicTypeInfoProxy) type;
		if (!proxy.factory.equals(TypeInfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	public IFieldInfo getUnderProxy(final IFieldInfo field) {
		GeneratedFieldInfoProxy proxy = (GeneratedFieldInfoProxy) field;
		if (!proxy.factory.equals(TypeInfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	public IMethodInfo getUnderProxy(final IMethodInfo method) {
		GeneratedMethodInfoProxy proxy = (GeneratedMethodInfoProxy) method;
		if (!proxy.factory.equals(TypeInfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	public IParameterInfo getUnderProxy(final IParameterInfo param) {
		GeneratedParameterInfoProxy proxy = (GeneratedParameterInfoProxy) param;
		if (!proxy.factory.equals(TypeInfoProxyFactory.this)) {
			throw new ReflectionUIError();
		}
		return proxy.base;
	}

	protected Method getDebugInfoEnclosingMethod() {
		return getClass().getEnclosingMethod();
	}

	protected Object getDefaultValue(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.getDefaultValue();
	}

	protected int getPosition(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.getPosition();
	}

	protected ITypeInfo getType(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return wrapParameterType(param.getType());
	}

	protected boolean isValueNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.isValueNullable();
	}

	protected String getCaption(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.getCaption();
	}

	protected String getName(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.getName();
	}

	protected InfoCategory getCategory(IFieldInfo field, ITypeInfo containingType) {
		return field.getCategory();
	}

	protected ITypeInfo getType(IFieldInfo field, ITypeInfo containingType) {
		return wrapFieldType(field.getType());
	}

	protected ITypeInfoProxyFactory getTypeSpecificities(IFieldInfo field, ITypeInfo containingType) {
		return field.getTypeSpecificities();
	}

	protected Object getValue(Object object, IFieldInfo field, ITypeInfo containingType) {
		return field.getValue(object);
	}

	protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
		return field.getValueOptions(object);
	}

	protected boolean isValueNullable(IFieldInfo field, ITypeInfo containingType) {
		return field.isValueNullable();
	}

	protected String getNullValueLabel(IFieldInfo field, ITypeInfo containingType) {
		return field.getNullValueLabel();
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

	protected Runnable getCustomUndoUpdateJob(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
		return field.getCustomUndoUpdateJob(object, value);
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

	protected String getIconImagePath(ITypeInfo type) {
		return type.getIconImagePath();
	}

	protected FieldsLayout getFieldsLayout(ITypeInfo type) {
		return type.getFieldsLayout();
	}

	protected List<Menu> getMenus(ITypeInfo type) {
		return type.getMenus();
	}

	protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
		return field.getCaption();
	}

	protected String getName(IFieldInfo field, ITypeInfo containingType) {
		return field.getName();
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

	protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo containingType) {
		return method.getParameters();
	}

	protected IMenuElementPosition getMenuItemPosition(IMethodInfo method, ITypeInfo containingType) {
		return method.getMenuItemPosition();
	}

	protected ITypeInfo getReturnValueType(IMethodInfo method, ITypeInfo containingType) {
		return wrapMethodReturnValueType(method.getReturnValueType());
	}

	protected ITypeInfoProxyFactory getReturnValueTypeSpecificities(IMethodInfo method, ITypeInfo containingType) {
		return method.getReturnValueTypeSpecificities();
	}

	protected boolean isReturnValueNullable(IMethodInfo method, ITypeInfo containingType) {
		return method.isReturnValueNullable();
	}

	protected boolean isReturnValueDetached(IMethodInfo method, ITypeInfo containingType) {
		return method.isReturnValueDetached();
	}

	protected String getCaption(IMethodInfo method, ITypeInfo containingType) {
		return method.getCaption();
	}

	protected String getName(IMethodInfo method, ITypeInfo containingType) {
		return method.getName();
	}

	protected Object[] getPossibleValues(IEnumerationTypeInfo type) {
		return type.getPossibleValues();
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

	protected List<AbstractListAction> getDynamicActions(IListTypeInfo type, ItemPosition anyRootListItemPosition,
			List<? extends ItemPosition> selection) {
		return type.getDynamicActions(anyRootListItemPosition, selection);
	}

	protected List<AbstractListProperty> getDynamicProperties(IListTypeInfo type, ItemPosition anyRootListItemPosition,
			List<? extends ItemPosition> selection) {
		return type.getDynamicProperties(anyRootListItemPosition, selection);
	}

	protected List<IMethodInfo> getAdditionalItemConstructors(IListTypeInfo type, Object listValue) {
		return type.getAdditionalItemConstructors(listValue);
	}

	protected ITypeInfo getItemType(IListTypeInfo type) {
		return wrapItemType(type.getItemType());
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

	protected boolean isItemNullable(IListTypeInfo type) {
		return type.isItemNullable();
	}

	protected boolean isItemConstructorSelectable(IListTypeInfo type) {
		return type.isItemConstructorSelectable();
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

	protected boolean isModificationStackAccessible(ITypeInfo type) {
		return type.isModificationStackAccessible();
	}

	protected boolean supportsInstance(ITypeInfo type, Object object) {
		return type.supportsInstance(object);
	}

	protected String getCaption(ITypeInfo type) {
		return type.getCaption();
	}

	protected String getName(ITypeInfo type) {
		return type.getName();
	}

	protected IFieldInfo getKeyField(IMapEntryTypeInfo type) {
		return new GeneratedFieldInfoProxy(type.getKeyField(), type);
	}

	protected IFieldInfo getValueField(IMapEntryTypeInfo type) {
		return new GeneratedFieldInfoProxy(type.getValueField(), type);
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

	protected String getIconImagePath(IMethodInfo method, ITypeInfo containingType) {
		return method.getIconImagePath();
	}

	protected Map<String, Object> getSpecificProperties(IMethodInfo method, ITypeInfo containingType) {
		return method.getSpecificProperties();
	}

	protected void validateParameters(IMethodInfo method, ITypeInfo containingType, Object object,
			InvocationData invocationData) throws Exception {
		method.validateParameters(object, invocationData);
	}

	protected Runnable getUndoModification(IMethodInfo method, ITypeInfo containingType, Object object,
			InvocationData invocationData) {
		return method.getUndoJob(object, invocationData);
	}

	protected IEnumerationItemInfo getValueInfo(IEnumerationTypeInfo type, Object object) {
		return type.getValueInfo(object);
	}

	protected String getName(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getName();
	}

	protected Map<String, Object> getSpecificProperties(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getSpecificProperties();
	}

	protected String getIconImagePath(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getIconImagePath();
	}

	protected String getOnlineHelp(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getOnlineHelp();
	}

	protected String getCaption(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		return info.getCaption();
	}

	private class GeneratedBasicTypeInfoProxy implements ITypeInfo {

		protected String GENERATED_PROXY_FACTORY_LIST_KEY = TypeInfoProxyFactory.class.getName()
				+ "GENERATED_PROXY_FACTORY_LIST";

		protected TypeInfoProxyFactory factory = TypeInfoProxyFactory.this;
		protected ITypeInfo base;

		public GeneratedBasicTypeInfoProxy(ITypeInfo type) {
			this.base = type;
			check();
		}

		public TypeInfoProxyFactory getFactory() {
			return factory;
		}

		@Override
		public String getName() {
			return TypeInfoProxyFactory.this.getName(base);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxyFactory.this.getCaption(base);
		}

		@Override
		public boolean supportsInstance(Object object) {
			return TypeInfoProxyFactory.this.supportsInstance(base, object);
		}

		@Override
		public boolean isConcrete() {
			return TypeInfoProxyFactory.this.isConcrete(base);
		}

		@Override
		public boolean isPrimitive() {
			return TypeInfoProxyFactory.this.isPrimitive(base);
		}

		@Override
		public boolean isImmutable() {
			return TypeInfoProxyFactory.this.isImmutable(base);
		}

		@Override
		public boolean isModificationStackAccessible() {
			return TypeInfoProxyFactory.this.isModificationStackAccessible(base);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			List<ITypeInfo> result = new ArrayList<ITypeInfo>();
			for (ITypeInfo subType : TypeInfoProxyFactory.this.getPolymorphicInstanceSubTypes(base)) {
				result.add(wrapSubType(subType));
			}
			return result;
		}

		@Override
		public List<IMethodInfo> getMethods() {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo method : TypeInfoProxyFactory.this.getMethods(base)) {
				result.add(new GeneratedMethodInfoProxy(method, base));
			}
			return result;
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (IFieldInfo field : TypeInfoProxyFactory.this.getFields(base)) {
				result.add(new GeneratedFieldInfoProxy(field, base));
			}
			return result;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo constructor : TypeInfoProxyFactory.this.getConstructors(base)) {
				result.add(new GeneratedConstructorInfoProxy(constructor, base));
			}
			return result;
		}

		@Override
		public String toString(Object object) {
			return TypeInfoProxyFactory.this.toString(base, object);
		}

		@Override
		public boolean canCopy(Object object) {
			return TypeInfoProxyFactory.this.canCopy(base, object);
		}

		@Override
		public Object copy(Object object) {
			return TypeInfoProxyFactory.this.copy(base, object);
		}

		@Override
		public String getIconImagePath() {
			return TypeInfoProxyFactory.this.getIconImagePath(base);
		}

		@Override
		public FieldsLayout getFieldsLayout() {
			return TypeInfoProxyFactory.this.getFieldsLayout(base);
		}

		@Override
		public List<Menu> getMenus() {
			return TypeInfoProxyFactory.this.getMenus(base);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getFactory().hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
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
			GeneratedBasicTypeInfoProxy other = (GeneratedBasicTypeInfoProxy) obj;
			if (!getFactory().equals(other.getFactory()))
				return false;
			if (base == null) {
				if (other.base != null)
					return false;
			} else if (!base.equals(other.base))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GeneratedBasicTypeInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base + "]";
		}

		@Override
		public String getOnlineHelp() {
			return TypeInfoProxyFactory.this.getOnlineHelp(base);
		}

		@Override
		public void validate(Object object) throws Exception {
			TypeInfoProxyFactory.this.validate(base, object);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			Map<String, Object> result = new HashMap<String, Object>(
					TypeInfoProxyFactory.this.getSpecificProperties(base));
			@SuppressWarnings("unchecked")
			List<TypeInfoProxyFactory> factories = (List<TypeInfoProxyFactory>) base.getSpecificProperties()
					.get(GENERATED_PROXY_FACTORY_LIST_KEY);
			if (factories == null) {
				factories = new ArrayList<TypeInfoProxyFactory>();
			}
			factories.add(TypeInfoProxyFactory.this);
			result.put(GENERATED_PROXY_FACTORY_LIST_KEY, factories);
			return result;
		}

		private void check() {
			@SuppressWarnings("unchecked")
			List<TypeInfoProxyFactory> factories = (List<TypeInfoProxyFactory>) base.getSpecificProperties()
					.get(GENERATED_PROXY_FACTORY_LIST_KEY);
			if (factories != null) {
				List<String> factoryIds = new ArrayList<String>();
				for (TypeInfoProxyFactory factory : factories) {
					factoryIds.add(factory.getIdentifier());
				}
				Collections.reverse(factoryIds);
				if (factoryIds.contains(factory.getIdentifier())) {
					StringBuilder msg = new StringBuilder();
					msg.append("Duplicate proxy creation detected:" + "\nNew proxy factory identifier:\n- "
							+ TypeInfoProxyFactory.this.getIdentifier() + "\nExisting factories identifers:\n");
					for (String id : factoryIds) {
						msg.append("- " + id + "\n");
					}
					msg.append(
							"If the factories actually differ, please override the getIdentifier() method to differenciate them");
					throw new ReflectionUIError(msg.toString());
				}
			}
		}

	}

	private class GeneratedListTypeInfoProxy extends GeneratedBasicTypeInfoProxy implements IListTypeInfo {

		public GeneratedListTypeInfoProxy(IListTypeInfo type) {
			super(type);
		}

		@Override
		public boolean isItemConstructorSelectable() {
			return TypeInfoProxyFactory.this.isItemConstructorSelectable((IListTypeInfo) base);
		}

		@Override
		public Object[] toArray(Object listValue) {
			return TypeInfoProxyFactory.this.toArray((IListTypeInfo) base, listValue);
		}

		@Override
		public boolean isItemNullable() {
			return TypeInfoProxyFactory.this.isItemNullable((IListTypeInfo) base);
		}

		@Override
		public ValueReturnMode getItemReturnMode() {
			return TypeInfoProxyFactory.this.getItemReturnMode((IListTypeInfo) base);
		}

		@Override
		public boolean isOrdered() {
			return TypeInfoProxyFactory.this.isOrdered((IListTypeInfo) base);
		}

		@Override
		public boolean isInsertionAllowed() {
			return TypeInfoProxyFactory.this.isInsertionAllowed((IListTypeInfo) base);
		}

		@Override
		public boolean isRemovalAllowed() {
			return TypeInfoProxyFactory.this.isRemovalAllowed((IListTypeInfo) base);
		}

		@Override
		public boolean canViewItemDetails() {
			return TypeInfoProxyFactory.this.canViewItemDetails((IListTypeInfo) base);
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return TypeInfoProxyFactory.this.getStructuralInfo((IListTypeInfo) base);
		}

		@Override
		public IListItemDetailsAccessMode getDetailsAccessMode() {
			return TypeInfoProxyFactory.this.getDetailsAccessMode((IListTypeInfo) base);
		}

		@Override
		public ITypeInfo getItemType() {
			return TypeInfoProxyFactory.this.getItemType((IListTypeInfo) base);
		}

		@Override
		public void replaceContent(Object listValue, Object[] array) {
			TypeInfoProxyFactory.this.replaceContent((IListTypeInfo) base, listValue, array);
		}

		@Override
		public Object fromArray(Object[] array) {
			return TypeInfoProxyFactory.this.fromArray((IListTypeInfo) base, array);
		}

		@Override
		public boolean canInstanciateFromArray() {
			return TypeInfoProxyFactory.this.canInstanciateFromArray((IListTypeInfo) base);
		}

		@Override
		public boolean canReplaceContent() {
			return TypeInfoProxyFactory.this.canReplaceContent((IListTypeInfo) base);
		}

		@Override
		public List<AbstractListAction> getDynamicActions(ItemPosition anyRootListItemPosition,
				List<? extends ItemPosition> selection) {
			return TypeInfoProxyFactory.this.getDynamicActions((IListTypeInfo) base, anyRootListItemPosition,
					selection);
		}

		@Override
		public List<AbstractListProperty> getDynamicProperties(ItemPosition anyRootListItemPosition,
				List<? extends ItemPosition> selection) {
			return TypeInfoProxyFactory.this.getDynamicProperties((IListTypeInfo) base, anyRootListItemPosition,
					selection);
		}

		@Override
		public List<IMethodInfo> getAdditionalItemConstructors(Object listValue) {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo constructor : TypeInfoProxyFactory.this.getAdditionalItemConstructors((IListTypeInfo) base,
					listValue)) {
				result.add(new GeneratedConstructorInfoProxy(constructor, base));
			}
			return result;
		}

		@Override
		public String toString() {
			return "GeneratedListTypeInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base + "]";
		}

	}

	private class GeneratedEnumerationTypeInfoProxy extends GeneratedBasicTypeInfoProxy
			implements IEnumerationTypeInfo {

		public GeneratedEnumerationTypeInfoProxy(IEnumerationTypeInfo type) {
			super(type);
		}

		@Override
		public boolean isDynamicEnumeration() {
			return TypeInfoProxyFactory.this.isDynamicEnumeration((IEnumerationTypeInfo) base);
		}

		@Override
		public Object[] getPossibleValues() {
			return TypeInfoProxyFactory.this.getPossibleValues((IEnumerationTypeInfo) base);
		}

		@Override
		public IEnumerationItemInfo getValueInfo(Object object) {
			IEnumerationItemInfo itemInfo = TypeInfoProxyFactory.this.getValueInfo((IEnumerationTypeInfo) base, object);
			return new GeneratedEnumerationItemInfoProxy(itemInfo, base);
		}

		@Override
		public String toString() {
			return "GeneratedEnumerationTypeInfoProxy [name=" + getName() + ",factory=" + factory + ", base=" + base
					+ "]";
		}

	}

	private class GeneratedMapEntryTypeInfoProxy extends GeneratedBasicTypeInfoProxy implements IMapEntryTypeInfo {

		public GeneratedMapEntryTypeInfoProxy(IMapEntryTypeInfo type) {
			super(type);
		}

		@Override
		public IFieldInfo getKeyField() {
			return TypeInfoProxyFactory.this.getKeyField((IMapEntryTypeInfo) base);
		}

		@Override
		public IFieldInfo getValueField() {
			return TypeInfoProxyFactory.this.getValueField((IMapEntryTypeInfo) base);
		}

		@Override
		public String toString() {
			return "GeneratedMapEntryTypeInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base
					+ "]";
		}

	}

	private class GeneratedFieldInfoProxy implements IFieldInfo {

		protected TypeInfoProxyFactory factory = TypeInfoProxyFactory.this;

		protected IFieldInfo base;
		protected ITypeInfo containingType;

		public GeneratedFieldInfoProxy(IFieldInfo field, ITypeInfo containingType) {
			this.base = field;
			this.containingType = containingType;
		}

		@Override
		public String getName() {
			return TypeInfoProxyFactory.this.getName(base, containingType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxyFactory.this.getCaption(base, containingType);
		}

		@Override
		public void setValue(Object object, Object value) {
			TypeInfoProxyFactory.this.setValue(object, value, base, containingType);
		}

		@Override
		public Runnable getCustomUndoUpdateJob(Object object, Object value) {
			return TypeInfoProxyFactory.this.getCustomUndoUpdateJob(object, value, base, containingType);
		}

		@Override
		public boolean isGetOnly() {
			return TypeInfoProxyFactory.this.isGetOnly(base, containingType);
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return TypeInfoProxyFactory.this.getValueReturnMode(base, containingType);
		}

		@Override
		public boolean isValueNullable() {
			return TypeInfoProxyFactory.this.isValueNullable(base, containingType);
		}

		@Override
		public String getNullValueLabel() {
			return TypeInfoProxyFactory.this.getNullValueLabel(base, containingType);

		}

		@Override
		public Object getValue(Object object) {
			return TypeInfoProxyFactory.this.getValue(object, base, containingType);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return TypeInfoProxyFactory.this.getValueOptions(object, base, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return TypeInfoProxyFactory.this.getType(base, containingType);
		}

		@Override
		public ITypeInfoProxyFactory getTypeSpecificities() {
			return TypeInfoProxyFactory.this.getTypeSpecificities(base, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return TypeInfoProxyFactory.this.getCategory(base, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return TypeInfoProxyFactory.this.getOnlineHelp(base, containingType);
		}

		@Override
		public boolean isFormControlMandatory() {
			return TypeInfoProxyFactory.this.isFormControlMandatory(base, containingType);
		}

		@Override
		public boolean isFormControlEmbedded() {
			return TypeInfoProxyFactory.this.isFormControlEmbedded(base, containingType);
		}

		@Override
		public IInfoFilter getFormControlFilter() {
			return TypeInfoProxyFactory.this.getFormControlFilter(base, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return TypeInfoProxyFactory.this.getSpecificProperties(base, containingType);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
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
			GeneratedFieldInfoProxy other = (GeneratedFieldInfoProxy) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
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

		private TypeInfoProxyFactory getOuterType() {
			return TypeInfoProxyFactory.this;
		}

		@Override
		public String toString() {
			return "GeneratedFieldInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base + "]";
		}

	}

	private class GeneratedMethodInfoProxy implements IMethodInfo {

		protected TypeInfoProxyFactory factory = TypeInfoProxyFactory.this;

		protected IMethodInfo base;
		protected ITypeInfo containingType;

		public GeneratedMethodInfoProxy(IMethodInfo method, ITypeInfo containingType) {
			this.base = method;
			this.containingType = containingType;
		}

		@Override
		public String getName() {
			return TypeInfoProxyFactory.this.getName(base, containingType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxyFactory.this.getCaption(base, containingType);
		}

		@Override
		public IMenuElementPosition getMenuItemPosition() {
			return TypeInfoProxyFactory.this.getMenuItemPosition(base, containingType);
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return TypeInfoProxyFactory.this.getReturnValueType(base, containingType);
		}

		@Override
		public ITypeInfoProxyFactory getReturnValueTypeSpecificities() {
			return TypeInfoProxyFactory.this.getReturnValueTypeSpecificities(base, containingType);
		}

		@Override
		public boolean isReturnValueNullable() {
			return TypeInfoProxyFactory.this.isReturnValueNullable(base, containingType);
		}

		@Override
		public boolean isReturnValueDetached() {
			return TypeInfoProxyFactory.this.isReturnValueDetached(base, containingType);
		}

		@Override
		public List<IParameterInfo> getParameters() {
			List<IParameterInfo> result = new ArrayList<IParameterInfo>();
			for (IParameterInfo param : TypeInfoProxyFactory.this.getParameters(base, containingType)) {
				result.add(new GeneratedParameterInfoProxy(param, base, containingType));
			}
			return result;
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return TypeInfoProxyFactory.this.invoke(object, invocationData, base, containingType);
		}

		@Override
		public boolean isReadOnly() {
			return TypeInfoProxyFactory.this.isReadOnly(base, containingType);
		}

		@Override
		public String getNullReturnValueLabel() {
			return TypeInfoProxyFactory.this.getNullReturnValueLabel(base, containingType);
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return TypeInfoProxyFactory.this.getValueReturnMode(base, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return TypeInfoProxyFactory.this.getCategory(base, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return TypeInfoProxyFactory.this.getOnlineHelp(base, containingType);
		}

		@Override
		public String getIconImagePath() {
			return TypeInfoProxyFactory.this.getIconImagePath(base, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return TypeInfoProxyFactory.this.getSpecificProperties(base, containingType);
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
			TypeInfoProxyFactory.this.validateParameters(base, containingType, object, invocationData);
		}

		@Override
		public Runnable getUndoJob(Object object, InvocationData invocationData) {
			return TypeInfoProxyFactory.this.getUndoModification(base, containingType, object, invocationData);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
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
			GeneratedMethodInfoProxy other = (GeneratedMethodInfoProxy) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
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

		private TypeInfoProxyFactory getOuterType() {
			return TypeInfoProxyFactory.this;
		}

		@Override
		public String toString() {
			return "GeneratedMethodInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base + "]";
		}

	}

	private class GeneratedConstructorInfoProxy extends GeneratedMethodInfoProxy {

		public GeneratedConstructorInfoProxy(IMethodInfo ctor, ITypeInfo containingType) {
			super(ctor, containingType);
		}

	}

	private class GeneratedParameterInfoProxy implements IParameterInfo {

		protected TypeInfoProxyFactory factory = TypeInfoProxyFactory.this;

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
			return TypeInfoProxyFactory.this.getName(base, method, containingType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxyFactory.this.getCaption(base, method, containingType);
		}

		@Override
		public boolean isValueNullable() {
			return TypeInfoProxyFactory.this.isValueNullable(base, method, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return TypeInfoProxyFactory.this.getType(base, method, containingType);
		}

		@Override
		public int getPosition() {
			return TypeInfoProxyFactory.this.getPosition(base, method, containingType);
		}

		@Override
		public Object getDefaultValue() {
			return TypeInfoProxyFactory.this.getDefaultValue(base, method, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return TypeInfoProxyFactory.this.getOnlineHelp(base, method, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return TypeInfoProxyFactory.this.getSpecificProperties(base, method, containingType);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GeneratedParameterInfoProxy other = (GeneratedParameterInfoProxy) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
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

		private TypeInfoProxyFactory getOuterType() {
			return TypeInfoProxyFactory.this;
		}

		@Override
		public String toString() {
			return "GeneratedParameterInfoProxy [name=" + getName() + ", factory=" + factory + ", base=" + base + "]";
		}

	}

	private class GeneratedEnumerationItemInfoProxy implements IEnumerationItemInfo {

		protected TypeInfoProxyFactory factory = TypeInfoProxyFactory.this;

		protected IEnumerationItemInfo base;
		protected ITypeInfo parentEnumType;

		public GeneratedEnumerationItemInfoProxy(IEnumerationItemInfo base, ITypeInfo parentEnumType) {
			this.base = base;
			this.parentEnumType = parentEnumType;
		}

		@Override
		public String getName() {
			return TypeInfoProxyFactory.this.getName(base, parentEnumType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxyFactory.this.getCaption(base, parentEnumType);
		}

		@Override
		public String getOnlineHelp() {
			return TypeInfoProxyFactory.this.getOnlineHelp(base, parentEnumType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return TypeInfoProxyFactory.this.getSpecificProperties(base, parentEnumType);
		}

		@Override
		public String getIconImagePath() {
			return TypeInfoProxyFactory.this.getIconImagePath(base, parentEnumType);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((parentEnumType == null) ? 0 : parentEnumType.hashCode());
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
			GeneratedEnumerationItemInfoProxy other = (GeneratedEnumerationItemInfoProxy) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (base == null) {
				if (other.base != null)
					return false;
			} else if (!base.equals(other.base))
				return false;
			if (parentEnumType == null) {
				if (other.parentEnumType != null)
					return false;
			} else if (!parentEnumType.equals(other.parentEnumType))
				return false;
			return true;
		}

		private TypeInfoProxyFactory getOuterType() {
			return TypeInfoProxyFactory.this;
		}

		@Override
		public String toString() {
			return "GeneratedEnumerationItemInfoProxy [name=" + getName() + ", factory=" + factory + ",base=" + base
					+ "]";
		}

	}

}
