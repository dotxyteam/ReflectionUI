
package xy.reflect.ui.info.parameter;

import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;

/**
 * Virtual parameter allowing to view/update the underlying field value.
 * 
 * @author olitank
 *
 */
public class FieldAsParameterInfo implements IParameterInfo {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo field;
	protected int position;
	protected ITypeInfo type;

	public FieldAsParameterInfo(ReflectionUI reflectionUI, IFieldInfo field, int position) {
		this.reflectionUI = reflectionUI;
		this.field = field;
		this.position = position;
	}

	public IFieldInfo getSourceField() {
		return field;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return field.getSpecificProperties();
	}

	@Override
	public String getOnlineHelp() {
		return field.getOnlineHelp();
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public String getCaption() {
		return field.getCaption();
	}

	@Override
	public boolean isNullValueDistinct() {
		return field.isNullValueDistinct();
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(field.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return null;
				}

				@Override
				protected String getTypeInfoProxyFactoryIdentifier() {
					return "ParameterTypeInfoProxyFactory [of=" + getClass().getName() + ", baseField="
							+ field.getName() + ", position=" + position + "]";
				}
			});
		}
		return type;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public Object getDefaultValue(Object object) {
		return field.getValue(object);
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return field.hasValueOptions(object);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return field.getValueOptions(object);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + position;
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
		FieldAsParameterInfo other = (FieldAsParameterInfo) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (position != other.position)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldAsParameterInfo [field=" + field + ", position=" + position + "]";
	}

}
