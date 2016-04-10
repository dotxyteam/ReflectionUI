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
import xy.reflect.ui.info.type.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.map.IMapEntryTypeInfo;
import xy.reflect.ui.info.type.iterable.util.IListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.structure.IListStructuralInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings("unused")
public class TypeInfoProxyConfiguration {

	private static final String DEBUG_INFO_ENCLOSING_METHODS = TypeInfoProxyConfiguration.class.getName()
			+ "#DEBUG_INFO_ENCLOSING_METHODS";
	protected StackTraceElement[] instanciationTrace = ReflectionUIUtils.createDebugStackTrace(1);

	public ITypeInfo get(final ITypeInfo type) {
		if (type instanceof IListTypeInfo) {
			return new ListTypeInfoProxy((IListTypeInfo) type);
		} else if (type instanceof IEnumerationTypeInfo) {
			return new EnumerationTypeInfoProxy((IEnumerationTypeInfo) type);
		} else if (type instanceof IMapEntryTypeInfo) {
			return new MapEntryTypeInfoProxy((IMapEntryTypeInfo) type);
		} else {
			return new BasicTypeInfoProxy(type);
		}
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

	protected Image getIconImage(ITypeInfo type, Object object) {
		return type.getIconImage(object);
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
			result.add(new ParameterInfoProxy(param, method, containingType));
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

	protected Object fromArray(IListTypeInfo type, Object[] listValue) {
		return type.fromArray(listValue);
	}

	protected boolean canInstanciateFromArray(IListTypeInfo type) {
		return type.canInstanciateFromArray();
	}

	protected List<IListAction> getSpecificListActions(IListTypeInfo type, Object object, IFieldInfo field,
			List<? extends ItemPosition> selection) {
		return type.getSpecificActions(object, field, selection);
	}

	protected List<IMethodInfo> getSpecificItemConstructors(IListTypeInfo type, Object object, IFieldInfo field) {
		return type.getObjectSpecificItemConstructors(object, field);
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

	protected Object[] toArray(IListTypeInfo type, Object object) {
		return type.toArray(object);
	}

	protected List<IMethodInfo> getConstructors(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo constructor : type.getConstructors()) {
			result.add(new MethodInfoProxy(constructor, type));
		}
		return result;
	}

	protected List<IFieldInfo> getFields(ITypeInfo type) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo field : type.getFields()) {
			result.add(new FieldInfoProxy(field, type));
		}
		return result;
	}

	protected List<IMethodInfo> getMethods(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo method : type.getMethods()) {
			result.add(new MethodInfoProxy(method, type));
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
		return new FieldInfoProxy(type.getKeyField(), type);
	}

	protected IFieldInfo getValueField(IMapEntryTypeInfo type) {
		return new FieldInfoProxy(type.getValueField(), type);
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
		Map<String, Object> result = new HashMap<String, Object>(type.getSpecificProperties());
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

	protected Component createFieldControl(ITypeInfo type, Object object, IFieldInfo field) {
		return type.createFieldControl(object, field);
	}

	protected boolean hasCustomFieldControl(ITypeInfo type, Object object, IFieldInfo field) {
		return type.hasCustomFieldControl(object, field);
	}

	protected String getOnlineHelp(IMethodInfo method, ITypeInfo containingType) {
		return method.getOnlineHelp();
	}

	protected Map<String, Object> getSpecificProperties(IMethodInfo method, ITypeInfo containingType) {
		return method.getSpecificProperties();
	}

	protected void validateParameters(IMethodInfo method, ITypeInfo containingType, Object object,
			InvocationData invocationData) throws Exception {
		method.validateParameters(object, invocationData);
	}

	protected IModification getUndoModification(IMethodInfo method, ITypeInfo containingType, Object object,
			InvocationData invocationData) {
		return method.getUndoModification(object, invocationData);
	}

	protected String formatEnumerationItem(Object object, IEnumerationTypeInfo type) {
		return type.formatEnumerationItem(object);
	}

	private class BasicTypeInfoProxy implements ITypeInfo {

		protected TypeInfoProxyConfiguration proxyConfiguration = TypeInfoProxyConfiguration.this;
		protected List<Method> debugInfoEnclosingMethods;
		protected ITypeInfo type;

		public BasicTypeInfoProxy(ITypeInfo type) {
			this.type = type;
			@SuppressWarnings("unchecked")
			List<Method> list = (List<Method>) TypeInfoProxyConfiguration.this.getSpecificProperties(type)
					.get(DEBUG_INFO_ENCLOSING_METHODS);
			this.debugInfoEnclosingMethods = list;
		}

		@Override
		public String getName() {
			return TypeInfoProxyConfiguration.this.getName(type);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxyConfiguration.this.getCaption(type);
		}

		@Override
		public boolean supportsInstance(Object object) {
			return TypeInfoProxyConfiguration.this.supportsInstance(type, object);
		}

		@Override
		public boolean isConcrete() {
			return TypeInfoProxyConfiguration.this.isConcrete(type);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return TypeInfoProxyConfiguration.this.getPolymorphicInstanceSubTypes(type);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return TypeInfoProxyConfiguration.this.getMethods(type);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return TypeInfoProxyConfiguration.this.getFields(type);
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return TypeInfoProxyConfiguration.this.getConstructors(type);
		}

		@Override
		public String toString(Object object) {
			return TypeInfoProxyConfiguration.this.toString(type, object);
		}

		@Override
		public Image getIconImage(Object object) {
			return TypeInfoProxyConfiguration.this.getIconImage(type, object);
		}

		public TypeInfoProxyConfiguration getTypeInfoProxyConfiguration() {
			return TypeInfoProxyConfiguration.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getTypeInfoProxyConfiguration().hashCode();
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
			BasicTypeInfoProxy other = (BasicTypeInfoProxy) obj;
			if (!getTypeInfoProxyConfiguration().equals(other.getTypeInfoProxyConfiguration()))
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
			return TypeInfoProxyConfiguration.this.toString(type);
		}

		@Override
		public String getOnlineHelp() {
			return TypeInfoProxyConfiguration.this.getOnlineHelp(type);
		}

		@Override
		public void validate(Object object) throws Exception {
			TypeInfoProxyConfiguration.this.validate(type, object);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return TypeInfoProxyConfiguration.this.getSpecificProperties(type);
		}

		@Override
		public Component createFieldControl(Object object, IFieldInfo field) {
			return TypeInfoProxyConfiguration.this.createFieldControl(type, object, field);
		}

		@Override
		public boolean hasCustomFieldControl(Object object, IFieldInfo field) {
			return TypeInfoProxyConfiguration.this.hasCustomFieldControl(type, object, field);
		}
	}

	private class ListTypeInfoProxy extends BasicTypeInfoProxy implements IListTypeInfo {

		public ListTypeInfoProxy(IListTypeInfo type) {
			super(type);
		}

		@Override
		public Object[] toArray(Object listValue) {
			return TypeInfoProxyConfiguration.this.toArray((IListTypeInfo) type, listValue);
		}

		@Override
		public boolean isOrdered() {
			return TypeInfoProxyConfiguration.this.isOrdered((IListTypeInfo) type);
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return TypeInfoProxyConfiguration.this.getStructuralInfo((IListTypeInfo) type);
		}

		@Override
		public ITypeInfo getItemType() {
			return TypeInfoProxyConfiguration.this.getItemType((IListTypeInfo) type);
		}

		@Override
		public Object fromArray(Object[] array) {
			return TypeInfoProxyConfiguration.this.fromArray((IListTypeInfo) type, array);
		}

		@Override
		public boolean canInstanciateFromArray() {
			return TypeInfoProxyConfiguration.this.canInstanciateFromArray((IListTypeInfo) type);
		}

		@Override
		public List<IListAction> getSpecificActions(Object object, IFieldInfo field,
				List<? extends ItemPosition> selection) {
			return TypeInfoProxyConfiguration.this.getSpecificListActions((IListTypeInfo) type, object, field,
					selection);
		}

		@Override
		public List<IMethodInfo> getObjectSpecificItemConstructors(Object object, IFieldInfo field) {
			return TypeInfoProxyConfiguration.this.getSpecificItemConstructors((IListTypeInfo) type, object, field);
		}

	}

	private class EnumerationTypeInfoProxy extends BasicTypeInfoProxy implements IEnumerationTypeInfo {

		public EnumerationTypeInfoProxy(IEnumerationTypeInfo type) {
			super(type);
		}

		@Override
		public Object[] getPossibleValues() {
			return TypeInfoProxyConfiguration.this.getPossibleValues((IEnumerationTypeInfo) type);
		}

		@Override
		public String formatEnumerationItem(Object object) {
			return TypeInfoProxyConfiguration.this.formatEnumerationItem(object, (IEnumerationTypeInfo) type);
		}

	}

	private class MapEntryTypeInfoProxy extends BasicTypeInfoProxy implements IMapEntryTypeInfo {

		public MapEntryTypeInfoProxy(IMapEntryTypeInfo type) {
			super(type);
		}

		@Override
		public IFieldInfo getKeyField() {
			return TypeInfoProxyConfiguration.this.getKeyField((IMapEntryTypeInfo) type);
		}

		@Override
		public IFieldInfo getValueField() {
			return TypeInfoProxyConfiguration.this.getValueField((IMapEntryTypeInfo) type);
		}

	}

	private class FieldInfoProxy implements IFieldInfo {

		protected TypeInfoProxyConfiguration proxyConfiguration = TypeInfoProxyConfiguration.this;

		protected IFieldInfo field;
		protected ITypeInfo containingType;

		private List<Method> debugInfoEnclosingMethods;

		public FieldInfoProxy(IFieldInfo field, ITypeInfo containingType) {
			this.field = field;
			this.containingType = containingType;
			@SuppressWarnings("unchecked")
			List<Method> list = (List<Method>) TypeInfoProxyConfiguration.this
					.getSpecificProperties(field, containingType).get(DEBUG_INFO_ENCLOSING_METHODS);
			this.debugInfoEnclosingMethods = list;
		}

		@Override
		public String getName() {
			return TypeInfoProxyConfiguration.this.getName(field, containingType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxyConfiguration.this.getCaption(field, containingType);
		}

		@Override
		public void setValue(Object object, Object value) {
			TypeInfoProxyConfiguration.this.setValue(object, value, field, containingType);
		}

		@Override
		public boolean isGetOnly() {
			return TypeInfoProxyConfiguration.this.isGetOnly(field, containingType);
		}

		@Override
		public boolean isNullable() {
			return TypeInfoProxyConfiguration.this.isNullable(field, containingType);
		}

		@Override
		public Object getValue(Object object) {
			return TypeInfoProxyConfiguration.this.getValue(object, field, containingType);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return TypeInfoProxyConfiguration.this.getValueOptions(object, field, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return TypeInfoProxyConfiguration.this.getType(field, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return TypeInfoProxyConfiguration.this.getCategory(field, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return TypeInfoProxyConfiguration.this.getOnlineHelp(field, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return TypeInfoProxyConfiguration.this.getSpecificProperties(field, containingType);
		}

		@Override
		public String toString() {
			return TypeInfoProxyConfiguration.this.toString(field, containingType);
		}

		@Override
		public int hashCode() {
			return TypeInfoProxyConfiguration.this.hashCode(field, containingType);
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
			return TypeInfoProxyConfiguration.this.equals(field, containingType, ((FieldInfoProxy) obj).field,
					((FieldInfoProxy) obj).containingType);
		}

	}

	private class MethodInfoProxy implements IMethodInfo {

		protected TypeInfoProxyConfiguration proxyConfiguration = TypeInfoProxyConfiguration.this;

		protected IMethodInfo method;
		protected ITypeInfo containingType;

		private List<Method> debugInfoEnclosingMethods;

		public MethodInfoProxy(IMethodInfo method, ITypeInfo containingType) {
			this.method = method;
			this.containingType = containingType;
			@SuppressWarnings("unchecked")
			List<Method> list = (List<Method>) TypeInfoProxyConfiguration.this.getSpecificProperties(method, containingType)
					.get(DEBUG_INFO_ENCLOSING_METHODS);
			this.debugInfoEnclosingMethods = list;		
		}

		@Override
		public String getName() {
			return TypeInfoProxyConfiguration.this.getName(method, containingType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxyConfiguration.this.getCaption(method, containingType);
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return TypeInfoProxyConfiguration.this.getReturnValueType(method, containingType);
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return TypeInfoProxyConfiguration.this.getParameters(method, containingType);
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return TypeInfoProxyConfiguration.this.invoke(object, invocationData, method, containingType);
		}

		@Override
		public boolean isReadOnly() {
			return TypeInfoProxyConfiguration.this.isReadOnly(method, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return TypeInfoProxyConfiguration.this.getCategory(method, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return TypeInfoProxyConfiguration.this.getOnlineHelp(method, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return TypeInfoProxyConfiguration.this.getSpecificProperties(method, containingType);
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
			TypeInfoProxyConfiguration.this.validateParameters(method, containingType, object, invocationData);
		}

		@Override
		public IModification getUndoModification(Object object, InvocationData invocationData) {
			return TypeInfoProxyConfiguration.this.getUndoModification(method, containingType, object, invocationData);
		}

		@Override
		public String toString() {
			return TypeInfoProxyConfiguration.this.toString(method, containingType);
		}

		@Override
		public int hashCode() {
			return TypeInfoProxyConfiguration.this.hashCode(method, containingType);
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
			return TypeInfoProxyConfiguration.this.equals(method, containingType, ((MethodInfoProxy) obj).method,
					((MethodInfoProxy) obj).containingType);
		}

	}

	private class ParameterInfoProxy implements IParameterInfo {

		protected StackTraceElement[] instanciationTrace = ReflectionUIUtils.createDebugStackTrace(1);

		protected IParameterInfo param;
		protected IMethodInfo method;
		protected ITypeInfo containingType;

		public ParameterInfoProxy(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
			this.param = param;
			this.method = method;
			this.containingType = containingType;
		}

		@Override
		public String getName() {
			return TypeInfoProxyConfiguration.this.getName(param, method, containingType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxyConfiguration.this.getCaption(param, method, containingType);
		}

		@Override
		public boolean isNullable() {
			return TypeInfoProxyConfiguration.this.isNullable(param, method, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return TypeInfoProxyConfiguration.this.getType(param, method, containingType);
		}

		@Override
		public int getPosition() {
			return TypeInfoProxyConfiguration.this.getPosition(param, method, containingType);
		}

		@Override
		public Object getDefaultValue() {
			return TypeInfoProxyConfiguration.this.getDefaultValue(param, method, containingType);
		}

		@Override
		public String toString() {
			return TypeInfoProxyConfiguration.this.toString(param, method, containingType);
		}

		@Override
		public String getOnlineHelp() {
			return TypeInfoProxyConfiguration.this.getOnlineHelp(param, method, containingType);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return TypeInfoProxyConfiguration.this.getSpecificProperties(param, method, containingType);
		}

		@Override
		public int hashCode() {
			return TypeInfoProxyConfiguration.this.hashCode(param, method, containingType);
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
			return TypeInfoProxyConfiguration.this.equals(param, method, containingType,
					((ParameterInfoProxy) obj).param, ((ParameterInfoProxy) obj).method,
					((ParameterInfoProxy) obj).containingType);
		}

	}

}
