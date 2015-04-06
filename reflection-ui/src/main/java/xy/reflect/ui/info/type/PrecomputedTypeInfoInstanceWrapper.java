package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.IListTypeInfo.IListAction;
import xy.reflect.ui.info.type.IListTypeInfo.ItemPosition;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PrecomputedTypeInfoInstanceWrapper {

	private Object instance;
	private ITypeInfo precomputedType;

	public PrecomputedTypeInfoInstanceWrapper(Object instance,
			ITypeInfo precomputedType) {
		this.instance = instance;
		this.precomputedType = precomputedType;
	}

	public PrecomputedTypeInfoSource getPrecomputedTypeInfoSource() {
		return new PrecomputedTypeInfoSource(new TypeInfoProxyConfiguration() {

			@Override
			protected Object getValue(Object object, IFieldInfo field,
					ITypeInfo containingType) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.getValue(object, field, containingType);
			}

			@Override
			protected void setValue(Object object, Object value,
					IFieldInfo field, ITypeInfo containingType) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				super.setValue(object, value, field, containingType);
			}

			@Override
			protected Object invoke(Object object,
					Map<Integer, Object> valueByParameterPosition,
					IMethodInfo method, ITypeInfo containingType) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.invoke(object, valueByParameterPosition, method,
						containingType);
			}

			@Override
			protected void validate(ITypeInfo type, Object object)
					throws Exception {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				super.validate(type, object);
			}

			@Override
			protected String toString(ITypeInfo type, Object object) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.toString(type, object);
			}

			@Override
			protected List<IListAction> getSpecificListActions(
					IListTypeInfo type, Object object, IFieldInfo field,
					List<? extends ItemPosition> selection) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.getSpecificListActions(type, object, field,
						selection);
			}

			@Override
			protected Object fromListValue(IListTypeInfo type,
					Object[] listValue) {
				Object result = super.fromListValue(type, listValue);
				return new PrecomputedTypeInfoInstanceWrapper(result,
						precomputedType);
			}

			@Override
			protected Object fromBoolean(Boolean b, IBooleanTypeInfo type) {
				Object result = super.fromBoolean(b, type);
				return new PrecomputedTypeInfoInstanceWrapper(result,
						precomputedType);
			}

			@Override
			protected Object fromText(String text, ITextualTypeInfo type) {
				Object result = super.fromText(text, type);
				return new PrecomputedTypeInfoInstanceWrapper(result,
						precomputedType);
			}

			@Override
			protected String formatEnumerationItem(Object object,
					IEnumerationTypeInfo type) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.formatEnumerationItem(object, type);
			}

			@Override
			protected Object[] toListValue(IListTypeInfo type, Object object) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.toListValue(type, object);
			}

			@Override
			protected Component createFieldControl(ITypeInfo type,
					Object object, IFieldInfo field) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.createFieldControl(type, object, field);
			}

			@Override
			protected boolean supportsInstance(ITypeInfo type, Object object) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.supportsInstance(type, object);
			}

			@Override
			protected Boolean toBoolean(Object object, IBooleanTypeInfo type) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.toBoolean(object, type);
			}

			@Override
			protected String toText(Object object, ITextualTypeInfo type) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.toText(object, type);
			}

			@Override
			protected Component createNonNullFieldValueControl(Object object,
					IFieldInfo field, FileTypeInfo type) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super
						.createNonNullFieldValueControl(object, field, type);
			}

			@Override
			protected void validateParameters(IMethodInfo method,
					ITypeInfo containingType, Object object,
					Map<Integer, Object> valueByParameterPosition)
					throws Exception {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				super.validateParameters(method, containingType, object,
						valueByParameterPosition);
			}

			@Override
			protected IModification getUndoModification(IMethodInfo method,
					ITypeInfo containingType, Object object,
					Map<Integer, Object> valueByParameterPosition) {
				// TODO Auto-generated method stub
				return super.getUndoModification(method, containingType,
						object, valueByParameterPosition);
			}

		}.get(precomputedType));
	}

	public Object getInstance() {
		return instance;
	}

	@Override
	public int hashCode() {
		return instance.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PrecomputedTypeInfoInstanceWrapper)) {
			return false;
		}
		PrecomputedTypeInfoInstanceWrapper other = (PrecomputedTypeInfoInstanceWrapper) obj;
		if (!ReflectionUIUtils.equalsOrBothNull(instance, other.instance)) {
			return false;
		}
		if (!precomputedType.equals(other.precomputedType)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return instance.toString();
	}

}
