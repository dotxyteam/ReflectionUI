/*
 * 
 */
package xy.reflect.ui.control;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

/**
 * Base class of common implementations of {@link IFieldControlData}.
 * 
 * @author olitank
 *
 */
public abstract class AbstractFieldControlData implements IFieldControlData {

	protected ReflectionUI reflectionUI;

	/**
	 * @return the underlying object.
	 */
	protected abstract Object getObject();

	/**
	 * @return the underlying field information.
	 */
	protected abstract IFieldInfo getField();

	public AbstractFieldControlData(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	public Object getValue() {
		return getField().getValue(getObject());
	}

	@Override
	public void setValue(Object value) {
		getField().setValue(getObject(), value);
	}

	@Override
	public String getCaption() {
		return getField().getCaption();
	}

	@Override
	public String getOnlineHelp() {
		return getField().getOnlineHelp();
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object newValue) {
		return getField().getNextUpdateCustomUndoJob(getObject(), newValue);
	}

	@Override
	public ITypeInfo getType() {
		final IFieldInfo field = getField();
		final Object object = getObject();
		ITypeInfo result = field.getType();
		if (field.getAlternativeConstructors(object) != null) {
			result = new FieldAlternativeConstructorsInstaller(reflectionUI, object, field).wrapTypeInfo(result);
		}
		final List<IMethodInfo> alternativeListItemConstructors = field.getAlternativeListItemConstructors(object);
		if (alternativeListItemConstructors != null) {
			result = new FieldAlternativeListItemConstructorsInstaller(reflectionUI, object, field)
					.wrapTypeInfo(result);
		}

		return result;
	}

	@Override
	public boolean isGetOnly() {
		return getField().isGetOnly();
	}

	@Override
	public boolean isTransient() {
		return getField().isTransient();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return getField().getValueReturnMode();
	}

	@Override
	public boolean isNullValueDistinct() {
		return getField().isNullValueDistinct();
	}

	@Override
	public String getNullValueLabel() {
		return getField().getNullValueLabel();
	}

	public boolean isFormControlMandatory() {
		return getField().isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return getField().isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return getField().getFormControlFilter();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return getField().getSpecificProperties();
	}

	@Override
	public ColorSpecification getLabelForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormForegroundColor() != null) {
				return type.getFormForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainForegroundColor();
	}

	@Override
	public ColorSpecification getBorderColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormBorderColor() != null) {
				return type.getFormBorderColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainBorderColor();
	}

	@Override
	public ColorSpecification getEditorForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormEditorsForegroundColor() != null) {
				return type.getFormEditorsForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainEditorForegroundColor();
	}

	@Override
	public ColorSpecification getEditorBackgroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormEditorsBackgroundColor() != null) {
				return type.getFormEditorsBackgroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainEditorBackgroundColor();
	}

	@Override
	public ResourcePath getButtonBackgroundImagePath() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBackgroundImagePath() != null) {
				return type.getFormButtonBackgroundImagePath();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath();
	}

	@Override
	public ColorSpecification getButtonBackgroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBackgroundColor() != null) {
				return type.getFormButtonBackgroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBackgroundColor();
	}

	@Override
	public ColorSpecification getButtonForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonForegroundColor() != null) {
				return type.getFormButtonForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonForegroundColor();
	}

	@Override
	public ColorSpecification getButtonBorderColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBorderColor() != null) {
				return type.getFormButtonBorderColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBorderColor();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
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
		AbstractFieldControlData other = (AbstractFieldControlData) obj;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		if (getField() == null) {
			if (other.getField() != null)
				return false;
		} else if (!getField().equals(other.getField()))
			return false;
		if (getObject() == null) {
			if (other.getObject() != null)
				return false;
		} else if (!getObject().equals(other.getObject()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldControlData [object=" + getObject() + ", field=" + getField() + "]";
	}

	public static class FieldAlternativeConstructorsInstaller extends InfoProxyFactory {

		protected ReflectionUI reflectionUI;
		protected Object object;
		protected IFieldInfo field;

		public FieldAlternativeConstructorsInstaller(ReflectionUI reflectionUI, Object object, IFieldInfo field) {
			this.reflectionUI = reflectionUI;
			this.object = object;
			this.field = field;
		}

		protected String getContainingTypeName() {
			ITypeInfo containingType = (object == null) ? null
					: reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
			return ((containingType == null) ? "<unknown>" : containingType.getName());
		}

		protected String getFieldName() {
			return field.getName();
		}

		@Override
		public String getIdentifier() {
			return "FieldAlternativeConstructorsInstaller [field=" + getFieldName() + ", containingType="
					+ getContainingTypeName() + "]";
		}

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo type) {
			return field.getAlternativeConstructors(object);
		}

	}

	public static class FieldAlternativeListItemConstructorsInstaller extends InfoProxyFactory {

		protected ReflectionUI reflectionUI;
		protected Object object;
		protected IFieldInfo field;

		public FieldAlternativeListItemConstructorsInstaller(ReflectionUI reflectionUI, Object object,
				IFieldInfo field) {
			this.reflectionUI = reflectionUI;
			this.object = object;
			this.field = field;
		}

		protected String getContainingTypeName() {
			ITypeInfo containingType = (object == null) ? null
					: reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
			return ((containingType == null) ? "<unknown>" : containingType.getName());
		}

		protected String getFieldName() {
			return field.getName();
		}

		@Override
		public String getIdentifier() {
			return "FieldAlternativeListItemConstructorsInstaller [field=" + getFieldName() + ", containingType="
					+ getContainingTypeName() + "]";
		}

		@Override
		protected ITypeInfo getItemType(IListTypeInfo type) {
			ITypeInfo result = super.getItemType(type);
			if (result == null) {
				result = reflectionUI.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, Object.class, null));
			}
			result = new InfoProxyFactory() {

				@Override
				public String getIdentifier() {
					return "ItemConstructorsInstaller [parent=FieldAlternativeListItemConstructorsInstaller [field="
							+ getFieldName() + ", containingType=" + getContainingTypeName() + "]]";
				}

				@Override
				protected List<IMethodInfo> getConstructors(ITypeInfo type) {
					return field.getAlternativeListItemConstructors(object);
				}

			}.wrapTypeInfo(result);
			return result;
		}
	}

}