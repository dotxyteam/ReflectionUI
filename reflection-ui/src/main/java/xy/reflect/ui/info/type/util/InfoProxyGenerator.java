package xy.reflect.ui.info.type.util;

import java.awt.Component;
import java.awt.Image;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.map.IMapEntryTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings("unused")
public class InfoProxyGenerator {

	private static final String DEBUG_INFO_ENCLOSING_METHODS = InfoProxyGenerator.class.getName()
			+ "#DEBUG_INFO_ENCLOSING_METHODS";
	protected StackTraceElement[] instanciationTrace = ReflectionUIUtils.createDebugStackTrace(1);

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

	public ITypeInfo getUnderProxy(final ITypeInfo type) {
		GeneratedBasicTypeInfoProxy proxy = (GeneratedBasicTypeInfoProxy) type;
		if (!proxy.generator.equals(InfoProxyGenerator.this)) {
			throw new ReflectionUIError();
		}
		return proxy.type;
	}

	public IFieldInfo getUnderProxy(final IFieldInfo field) {
		GeneratedFieldInfoProxy proxy = (GeneratedFieldInfoProxy) field;
		if (!proxy.generator.equals(InfoProxyGenerator.this)) {
			throw new ReflectionUIError();
		}
		return proxy.field;
	}

	public IMethodInfo getUnderProxy(final IMethodInfo method) {
		GeneratedMethodInfoProxy proxy = (GeneratedMethodInfoProxy) method;
		if (!proxy.generator.equals(InfoProxyGenerator.this)) {
			throw new ReflectionUIError();
		}
		return proxy.method;
	}

	public IParameterInfo getUnderProxy(final IParameterInfo param) {
		GeneratedParameterInfoProxy proxy = (GeneratedParameterInfoProxy) param;
		if (!proxy.generator.equals(InfoProxyGenerator.this)) {
			throw new ReflectionUIError();
		}
		return proxy.param;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getClass().hashCode();
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
		return true;
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
		return param.getType();
	}

	protected boolean isNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.isNullable();
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
		return field.getType();
	}

	protected Object getValue(Object object, IFieldInfo field, ITypeInfo containingType) {
		return field.getValue(object);
	}

	protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
		return field.getValueOptions(object);
	}

	protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
		return field.isNullable();
	}

	protected boolean isGetOnly(IFieldInfo field, ITypeInfo containingType) {
		return field.isGetOnly();
	}

	protected void setValue(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
		field.setValue(object, value);
	}

	protected String toString(ITypeInfo type, Object object) {
		return type.toString(object);
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

	protected Object invoke(Object object, InvocationData invocationData, IMethodInfo method,
			ITypeInfo containingType) {
		return method.invoke(object, invocationData);
	}

	protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo containingType) {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>();
		for (IParameterInfo param : method.getParameters()) {
			result.add(new GeneratedParameterInfoProxy(param, method, containingType));
		}
		return result;
	}

	protected ITypeInfo getReturnValueType(IMethodInfo method, ITypeInfo containingType) {
		return method.getReturnValueType();
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

	protected void fromArray(IListTypeInfo type, Object listValue, Object[] array) {
		type.replaceContent(listValue, array);
	}

	protected Object fromArray(IListTypeInfo type, Object[] array) {
		return type.fromArray(array);
	}

	protected boolean canInstanciateFromArray(IListTypeInfo type) {
		return type.canInstanciateFromArray();
	}

	protected List<AbstractListAction> getSpecificListActions(IListTypeInfo type, Object object, IFieldInfo field,
			List<? extends ItemPosition> selection) {
		return type.getSpecificActions(object, field, selection);
	}

	protected List<IMethodInfo> getSpecificItemConstructors(IListTypeInfo type, Object object, IFieldInfo field) {
		return type.getObjectSpecificItemConstructors(object, field);
	}

	public boolean isStructureMutable(IListTypeInfo type) {
		return type.canReplaceContent();
	}

	protected ITypeInfo getItemType(IListTypeInfo type) {
		return type.getItemType();
	}

	protected IListStructuralInfo getStructuralInfo(IListTypeInfo type) {
		return type.getStructuralInfo();
	}

	protected boolean isOrdered(IListTypeInfo type) {
		return type.isOrdered();
	}

	protected boolean canAdd(IListTypeInfo type) {
		return type.canAdd();
	}

	protected boolean canRemove(IListTypeInfo type) {
		return type.canRemove();
	}
	
	protected boolean canViewItemDetails(IListTypeInfo type) {
		return type.canViewItemDetails();
	}

	protected Object[] toArray(IListTypeInfo type, Object object) {
		return type.toArray(object);
	}

	protected List<IMethodInfo> getConstructors(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo constructor : type.getConstructors()) {
			result.add(new GeneratedMethodInfoProxy(constructor, type));
		}
		return result;
	}

	protected List<IFieldInfo> getFields(ITypeInfo type) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo field : type.getFields()) {
			result.add(new GeneratedFieldInfoProxy(field, type));
		}
		return result;
	}

	protected List<IMethodInfo> getMethods(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo method : type.getMethods()) {
			result.add(new GeneratedMethodInfoProxy(method, type));
		}
		return result;
	}

	protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
		return type.getPolymorphicInstanceSubTypes();
	}

	protected boolean isConcrete(ITypeInfo type) {
		return type.isConcrete();
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

	protected int hashCode(ITypeInfo type) {
		return type.hashCode();
	}

	protected boolean equals(IParameterInfo param, IMethodInfo method, ITypeInfo containingType, IParameterInfo param2,
			IMethodInfo method2, ITypeInfo containingType2) {
		return param.equals(param2);
	}

	protected int hashCode(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.hashCode();
	}

	protected String toString(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.toString();
	}

	protected boolean equals(IMethodInfo method, ITypeInfo containingType, IMethodInfo method2,
			ITypeInfo containingType2) {
		return method.equals(method2);
	}

	protected int hashCode(IMethodInfo method, ITypeInfo containingType) {
		return method.hashCode();
	}

	protected String toString(IMethodInfo method, ITypeInfo containingType) {
		return method.toString();
	}

	protected String toString(IFieldInfo field, ITypeInfo containingType) {
		return field.toString();
	}

	protected int hashCode(IFieldInfo field, ITypeInfo containingType) {
		return field.hashCode();
	}

	protected boolean equals(IFieldInfo field, ITypeInfo containingType, IFieldInfo field2, ITypeInfo containingType2) {
		return field.equals(field2);
	}

	protected String toString(ITypeInfo type) {
		return type.toString();
	}

	protected boolean equals(ITypeInfo type1, ITypeInfo type2) {
		return type1.equals(type2);
	}

	protected String getOnlineHelp(IFieldInfo field, ITypeInfo containingType) {
		return field.getOnlineHelp();
	}

	protected Map<String, Object> getSpecificProperties(IFieldInfo field, ITypeInfo containingType) {
		Map<String, Object> result = new HashMap<String, Object>(field.getSpecificProperties());
		Method method = getDebugInfoEnclosingMethod();
		if (method != null) {
			List<Method> methodList = new ArrayList<Method>();
			methodList.add(method);
			@SuppressWarnings("unchecked")
			List<Method> previousMethods = (List<Method>) result.get(DEBUG_INFO_ENCLOSING_METHODS);
			if (previousMethods != null) {
				methodList.addAll(previousMethods);
			}
			result.put(DEBUG_INFO_ENCLOSING_METHODS, methodList);
		}
		return result;
	}

	protected String getOnlineHelp(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return param.getOnlineHelp();
	}

	protected Map<String, Object> getSpecificProperties(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		Map<String, Object> result = new HashMap<String, Object>(param.getSpecificProperties());
		Method javaMethod = getDebugInfoEnclosingMethod();
		if (javaMethod != null) {
			List<Method> methodList = new ArrayList<Method>();
			methodList.add(javaMethod);
			@SuppressWarnings("unchecked")
			List<Method> previousMethods = (List<Method>) result.get(DEBUG_INFO_ENCLOSING_METHODS);
			if (previousMethods != null) {
				methodList.addAll(previousMethods);
			}
			result.put(DEBUG_INFO_ENCLOSING_METHODS, methodList);
		}
		return result;
	}

	protected String getOnlineHelp(ITypeInfo type) {
		return type.getOnlineHelp();
	}

	protected void validate(ITypeInfo type, Object object) throws Exception {
		type.validate(object);
	}

	protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
		Map<String, Object> result = new HashMap<String, Object>(type.getSpecificProperties());
		Method javaMethod = getDebugInfoEnclosingMethod();
		if (javaMethod != null) {
			List<Method> methodList = new ArrayList<Method>();
			methodList.add(javaMethod);
			@SuppressWarnings("unchecked")
			List<Method> previousMethods = (List<Method>) result.get(DEBUG_INFO_ENCLOSING_METHODS);
			if (previousMethods != null) {
				methodList.addAll(previousMethods);
			}
			result.put(DEBUG_INFO_ENCLOSING_METHODS, methodList);
		}
		return result;
	}

	protected String getOnlineHelp(IMethodInfo method, ITypeInfo containingType) {
		return method.getOnlineHelp();
	}

	protected Map<String, Object> getSpecificProperties(IMethodInfo method, ITypeInfo containingType) {
		Map<String, Object> result = new HashMap<String, Object>(method.getSpecificProperties());
		Method javaMethod = getDebugInfoEnclosingMethod();
		if (javaMethod != null) {
			List<Method> methodList = new ArrayList<Method>();
			methodList.add(javaMethod);
			@SuppressWarnings("unchecked")
			List<Method> previousMethods = (List<Method>) result.get(DEBUG_INFO_ENCLOSING_METHODS);
			if (previousMethods != null) {
				methodList.addAll(previousMethods);
			}
			result.put(DEBUG_INFO_ENCLOSING_METHODS, methodList);
		}
		return result;
	}

	protected void validateParameters(IMethodInfo method, ITypeInfo containingType, Object object,
			InvocationData invocationData) throws Exception {
		method.validateParameters(object, invocationData);
	}

	protected IModification getUndoModification(IMethodInfo method, ITypeInfo containingType, Object object,
			InvocationData invocationData) {
		return method.getUndoModification(object, invocationData);
	}

	protected IEnumerationItemInfo getValueInfo(Object object, IEnumerationTypeInfo type) {
		return type.getValueInfo(object);
	}

	private class GeneratedBasicTypeInfoProxy implements ITypeInfo {

		protected InfoProxyGenerator generator = InfoProxyGenerator.this;
		protected List<Method> debugInfoEnclosingMethods;
		protected ITypeInfo type;

		public GeneratedBasicTypeInfoProxy(ITypeInfo type) {
			this.type = type;
			@SuppressWarnings("unchecked")
			List<Method> list = (List<Method>) InfoProxyGenerator.this.getSpecificProperties(type)
					.get(DEBUG_INFO_ENCLOSING_METHODS);
			this.debugInfoEnclosingMethods = list;
		}

		public InfoProxyGenerator getGenerator() {
			return generator;
		}

		@Override
		public String getName() {
			return InfoProxyGenerator.this.getName(type);
		}

		@Override
		public String getCaption() {
			return InfoProxyGenerator.this.getCaption(type);
		}

		@Override
		public boolean supportsInstance(Object object) {
			return InfoProxyGenerator.this.supportsInstance(type, object);
		}

		@Override
		public boolean isConcrete() {
			return InfoProxyGenerator.this.isConcrete(type);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return InfoProxyGenerator.this.getPolymorphicInstanceSubTypes(type);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return InfoProxyGenerator.this.getMethods(type);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return InfoProxyGenerator.this.getFields(type);
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return InfoProxyGenerator.this.getConstructors(type);
		}

		@Override
		public String toString(Object object) {
			return InfoProxyGenerator.this.toString(type, object);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getGenerator().hashCode();
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			if (!getGenerator().equals(other.getGenerator()))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return InfoProxyGenerator.this.toString(type);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyGenerator.this.getOnlineHelp(type);
		}

		@Override
		public void validate(Object object) throws Exception {
			InfoProxyGenerator.this.validate(type, object);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyGenerator.this.getSpecificProperties(type);
		}
	}

	private class GeneratedListTypeInfoProxy extends GeneratedBasicTypeInfoProxy implements IListTypeInfo {

		public GeneratedListTypeInfoProxy(IListTypeInfo type) {
			super(type);
		}

		@Override
		public Object[] toArray(Object listValue) {
			return InfoProxyGenerator.this.toArray((IListTypeInfo) type, listValue);
		}

		@Override
		public boolean isOrdered() {
			return InfoProxyGenerator.this.isOrdered((IListTypeInfo) type);
		}
		
		@Override
		public boolean canAdd() {
			return InfoProxyGenerator.this.canAdd((IListTypeInfo) type);
		}

		@Override
		public boolean canRemove() {
			return InfoProxyGenerator.this.canRemove((IListTypeInfo) type);
		}
		
		@Override
		public boolean canViewItemDetails() {
			return InfoProxyGenerator.this.canViewItemDetails((IListTypeInfo) type);
		}


		@Override
		public IListStructuralInfo getStructuralInfo() {
			return InfoProxyGenerator.this.getStructuralInfo((IListTypeInfo) type);
		}

		@Override
		public ITypeInfo getItemType() {
			return InfoProxyGenerator.this.getItemType((IListTypeInfo) type);
		}

		@Override
		public void replaceContent(Object listValue, Object[] array) {
			InfoProxyGenerator.this.fromArray((IListTypeInfo) type, listValue, array);
		}

		@Override
		public Object fromArray(Object[] array) {
			return InfoProxyGenerator.this.fromArray((IListTypeInfo) type, array);
		}

		@Override
		public boolean canInstanciateFromArray() {
			return InfoProxyGenerator.this.canInstanciateFromArray((IListTypeInfo) type);
		}

		@Override
		public List<AbstractListAction> getSpecificActions(Object object, IFieldInfo field,
				List<? extends ItemPosition> selection) {
			return InfoProxyGenerator.this.getSpecificListActions((IListTypeInfo) type, object, field, selection);
		}

		@Override
		public List<IMethodInfo> getObjectSpecificItemConstructors(Object object, IFieldInfo field) {
			return InfoProxyGenerator.this.getSpecificItemConstructors((IListTypeInfo) type, object, field);
		}

		@Override
		public boolean canReplaceContent() {
			return InfoProxyGenerator.this.isStructureMutable((IListTypeInfo) type);
		}

	}

	private class GeneratedEnumerationTypeInfoProxy extends GeneratedBasicTypeInfoProxy
			implements IEnumerationTypeInfo {

		public GeneratedEnumerationTypeInfoProxy(IEnumerationTypeInfo type) {
			super(type);
		}

		@Override
		public Object[] getPossibleValues() {
			return InfoProxyGenerator.this.getPossibleValues((IEnumerationTypeInfo) type);
		}

		@Override
		public IEnumerationItemInfo getValueInfo(Object object) {
			return InfoProxyGenerator.this.getValueInfo(object, (IEnumerationTypeInfo) type);
		}

	}

	private class GeneratedMapEntryTypeInfoProxy extends GeneratedBasicTypeInfoProxy implements IMapEntryTypeInfo {

		public GeneratedMapEntryTypeInfoProxy(IMapEntryTypeInfo type) {
			super(type);
		}

		@Override
		public IFieldInfo getKeyField() {
			return InfoProxyGenerator.this.getKeyField((IMapEntryTypeInfo) type);
		}

		@Override
		public IFieldInfo getValueField() {
			return InfoProxyGenerator.this.getValueField((IMapEntryTypeInfo) type);
		}

	}

	private class GeneratedFieldInfoProxy implements IFieldInfo {

		protected InfoProxyGenerator generator = InfoProxyGenerator.this;

		protected IFieldInfo field;
		protected ITypeInfo containingType;

		protected List<Method> debugInfoEnclosingMethods;

		public GeneratedFieldInfoProxy(IFieldInfo field, ITypeInfo containingType) {
			this.field = field;
			this.containingType = containingType;
			@SuppressWarnings("unchecked")
			List<Method> list = (List<Method>) InfoProxyGenerator.this.getSpecificProperties(field, containingType)
					.get(DEBUG_INFO_ENCLOSING_METHODS);
			this.debugInfoEnclosingMethods = list;
		}

		@Override
		public String getName() {
			return InfoProxyGenerator.this.getName(field, containingType);
		}

		@Override
		public String getCaption() {
			return InfoProxyGenerator.this.getCaption(field, containingType);
		}

		@Override
		public void setValue(Object object, Object value) {
			InfoProxyGenerator.this.setValue(object, value, field, containingType);
		}

		@Override
		public boolean isGetOnly() {
			return InfoProxyGenerator.this.isGetOnly(field, containingType);
		}

		@Override
		public boolean isNullable() {
			return InfoProxyGenerator.this.isNullable(field, containingType);
		}

		@Override
		public Object getValue(Object object) {
			return InfoProxyGenerator.this.getValue(object, field, containingType);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return InfoProxyGenerator.this.getValueOptions(object, field, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return InfoProxyGenerator.this.getType(field, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return InfoProxyGenerator.this.getCategory(field, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyGenerator.this.getOnlineHelp(field, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyGenerator.this.getSpecificProperties(field, containingType);
		}

		@Override
		public String toString() {
			return InfoProxyGenerator.this.toString(field, containingType);
		}

		@Override
		public int hashCode() {
			return InfoProxyGenerator.this.hashCode(field, containingType);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			return InfoProxyGenerator.this.equals(field, containingType, ((GeneratedFieldInfoProxy) obj).field,
					((GeneratedFieldInfoProxy) obj).containingType);
		}

	}

	private class GeneratedMethodInfoProxy implements IMethodInfo {

		protected InfoProxyGenerator generator = InfoProxyGenerator.this;

		protected IMethodInfo method;
		protected ITypeInfo containingType;

		protected List<Method> debugInfoEnclosingMethods;

		public GeneratedMethodInfoProxy(IMethodInfo method, ITypeInfo containingType) {
			this.method = method;
			this.containingType = containingType;
			@SuppressWarnings("unchecked")
			List<Method> list = (List<Method>) InfoProxyGenerator.this.getSpecificProperties(method, containingType)
					.get(DEBUG_INFO_ENCLOSING_METHODS);
			this.debugInfoEnclosingMethods = list;
		}

		@Override
		public String getName() {
			return InfoProxyGenerator.this.getName(method, containingType);
		}

		@Override
		public String getCaption() {
			return InfoProxyGenerator.this.getCaption(method, containingType);
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return InfoProxyGenerator.this.getReturnValueType(method, containingType);
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return InfoProxyGenerator.this.getParameters(method, containingType);
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return InfoProxyGenerator.this.invoke(object, invocationData, method, containingType);
		}

		@Override
		public boolean isReadOnly() {
			return InfoProxyGenerator.this.isReadOnly(method, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return InfoProxyGenerator.this.getCategory(method, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyGenerator.this.getOnlineHelp(method, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyGenerator.this.getSpecificProperties(method, containingType);
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
			InfoProxyGenerator.this.validateParameters(method, containingType, object, invocationData);
		}

		@Override
		public IModification getUndoModification(Object object, InvocationData invocationData) {
			return InfoProxyGenerator.this.getUndoModification(method, containingType, object, invocationData);
		}

		@Override
		public String toString() {
			return InfoProxyGenerator.this.toString(method, containingType);
		}

		@Override
		public int hashCode() {
			return InfoProxyGenerator.this.hashCode(method, containingType);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			return InfoProxyGenerator.this.equals(method, containingType, ((GeneratedMethodInfoProxy) obj).method,
					((GeneratedMethodInfoProxy) obj).containingType);
		}

	}

	private class GeneratedParameterInfoProxy implements IParameterInfo {

		protected InfoProxyGenerator generator = InfoProxyGenerator.this;

		protected IParameterInfo param;
		protected IMethodInfo method;
		protected ITypeInfo containingType;

		protected List<Method> debugInfoEnclosingMethods;

		public GeneratedParameterInfoProxy(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			this.param = param;
			this.method = method;
			this.containingType = containingType;
			@SuppressWarnings("unchecked")
			List<Method> list = (List<Method>) InfoProxyGenerator.this
					.getSpecificProperties(param, method, containingType).get(DEBUG_INFO_ENCLOSING_METHODS);
			this.debugInfoEnclosingMethods = list;
		}

		@Override
		public String getName() {
			return InfoProxyGenerator.this.getName(param, method, containingType);
		}

		@Override
		public String getCaption() {
			return InfoProxyGenerator.this.getCaption(param, method, containingType);
		}

		@Override
		public boolean isNullable() {
			return InfoProxyGenerator.this.isNullable(param, method, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return InfoProxyGenerator.this.getType(param, method, containingType);
		}

		@Override
		public int getPosition() {
			return InfoProxyGenerator.this.getPosition(param, method, containingType);
		}

		@Override
		public Object getDefaultValue() {
			return InfoProxyGenerator.this.getDefaultValue(param, method, containingType);
		}

		@Override
		public String toString() {
			return InfoProxyGenerator.this.toString(param, method, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return InfoProxyGenerator.this.getOnlineHelp(param, method, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return InfoProxyGenerator.this.getSpecificProperties(param, method, containingType);
		}

		@Override
		public int hashCode() {
			return InfoProxyGenerator.this.hashCode(param, method, containingType);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			return InfoProxyGenerator.this.equals(param, method, containingType,
					((GeneratedParameterInfoProxy) obj).param, ((GeneratedParameterInfoProxy) obj).method,
					((GeneratedParameterInfoProxy) obj).containingType);
		}

	}

}
