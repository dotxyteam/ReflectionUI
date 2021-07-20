
package xy.reflect.ui.info.type.factory;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransactionInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ArrayAccessor;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Factory that generates through {@link #getInstance(Accessor)} or
 * {@link #getInstance(Object[])} virtual objects that encapsulate the provided
 * objects. It also generates the virtual object type information.
 * 
 * @author olitank
 *
 */
public class EncapsulatedObjectFactory {

	public static final String IS_ENCAPSULATION_FIELD_PROPERTY_KEY = EncapsulatedObjectFactory.class.getName()
			+ ".IS_ENCAPSULATION_FIELD";

	protected ReflectionUI reflectionUI;

	protected String typeName;
	protected String typeCaption = "";
	protected ResourcePath typeIconImagePath;
	protected Map<String, Object> typeSpecificProperties = new HashMap<String, Object>();
	protected boolean typeModificationStackAccessible = true;
	protected String typeOnlineHelp;

	protected String fieldName = "";
	protected ITypeInfo fieldType;
	protected String fieldCaption = "";
	protected boolean fieldGetOnly = false;
	protected boolean fieldTransient = false;
	protected boolean fieldNullValueDistinct = false;
	protected ValueReturnMode fieldValueReturnMode = ValueReturnMode.INDETERMINATE;
	protected Map<String, Object> fieldSpecificProperties = new HashMap<String, Object>();
	protected String fieldNullValueLabel;
	protected String fieldOnlineHelp;
	protected InfoCategory fieldCategory;
	protected long fieldAutoUpdatePeriodMilliseconds = -1;
	protected boolean fieldFormControlMandatory = false;
	protected boolean fieldFormControlEmbedded = false;
	protected IInfoFilter fieldFormControlFilter = IInfoFilter.DEFAULT;
	protected IInfoProxyFactory fieldTypeSpecificities;

	public EncapsulatedObjectFactory(ReflectionUI reflectionUI, String typeName, ITypeInfo fieldType) {
		this.reflectionUI = reflectionUI;
		this.typeName = typeName;
		this.fieldType = fieldType;
	}

	public EncapsulatedObjectFactory(ReflectionUI reflectionUI, ITypeInfo fieldType, String contextId,
			String typeCaption, String fieldCaption) {
		this(reflectionUI, "Encapsulation [context=" + contextId + ", fieldType=" + fieldType.getName() + "]",
				fieldType);
		this.fieldCaption = fieldCaption;
		this.typeCaption = typeCaption;
	}

	public Object getInstance(Accessor<Object> fieldValueAccessor) {
		return new PrecomputedTypeInstanceWrapper(new Instance(fieldValueAccessor), new TypeInfo());
	}

	public Object getInstance(Object[] fieldValueHolder) {
		return getInstance(new ArrayAccessor<Object>(fieldValueHolder));
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeCaption() {
		return typeCaption;
	}

	public void setTypeCaption(String typeCaption) {
		this.typeCaption = typeCaption;
	}

	public ResourcePath getTypeIconImagePath() {
		return typeIconImagePath;
	}

	public void setTypeIconImagePath(ResourcePath typeIconImagePath) {
		this.typeIconImagePath = typeIconImagePath;
	}

	public boolean isTypeModificationStackAccessible() {
		return typeModificationStackAccessible;
	}

	public void setTypeModificationStackAccessible(boolean modificationStackAccessible) {
		this.typeModificationStackAccessible = modificationStackAccessible;
	}

	public String getTypeOnlineHelp() {
		return typeOnlineHelp;
	}

	public void setTypeOnlineHelp(String typeOnlineHelp) {
		this.typeOnlineHelp = typeOnlineHelp;
	}

	public Map<String, Object> getTypeSpecificProperties() {
		return typeSpecificProperties;
	}

	public void setTypeSpecificProperties(Map<String, Object> typeSpecificProperties) {
		this.typeSpecificProperties = typeSpecificProperties;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldCaption() {
		return fieldCaption;
	}

	public void setFieldCaption(String fieldCaption) {
		this.fieldCaption = fieldCaption;
	}

	public boolean isFieldGetOnly() {
		return fieldGetOnly;
	}

	public void setFieldGetOnly(boolean fieldGetOnly) {
		this.fieldGetOnly = fieldGetOnly;
	}

	public boolean isFieldNullValueDistinct() {
		return fieldNullValueDistinct;
	}

	public void setFieldNullValueDistinct(boolean fieldNullValueDistinct) {
		this.fieldNullValueDistinct = fieldNullValueDistinct;
	}

	public String getFieldNullValueLabel() {
		return fieldNullValueLabel;
	}

	public void setFieldNullValueLabel(String fieldNullValueLabel) {
		this.fieldNullValueLabel = fieldNullValueLabel;
	}

	public String getFieldOnlineHelp() {
		return fieldOnlineHelp;
	}

	public void setFieldOnlineHelp(String fieldOnlineHelp) {
		this.fieldOnlineHelp = fieldOnlineHelp;
	}

	public InfoCategory getFieldCategory() {
		return fieldCategory;
	}

	public void setFieldCategory(InfoCategory fieldCategory) {
		this.fieldCategory = fieldCategory;
	}

	public long getFieldAutoUpdatePeriodMilliseconds() {
		return fieldAutoUpdatePeriodMilliseconds;
	}

	public void setFieldAutoUpdatePeriodMilliseconds(long fieldAutoUpdatePeriodMilliseconds) {
		this.fieldAutoUpdatePeriodMilliseconds = fieldAutoUpdatePeriodMilliseconds;
	}

	public ValueReturnMode getFieldValueReturnMode() {
		return fieldValueReturnMode;
	}

	public void setFieldValueReturnMode(ValueReturnMode fieldValueReturnMode) {
		this.fieldValueReturnMode = fieldValueReturnMode;
	}

	public boolean isFieldFormControlMandatory() {
		return fieldFormControlMandatory;
	}

	public void setFieldFormControlMandatory(boolean fieldFormControlMandatory) {
		this.fieldFormControlMandatory = fieldFormControlMandatory;
	}

	public boolean isFieldFormControlEmbedded() {
		return fieldFormControlEmbedded;
	}

	public void setFieldFormControlEmbedded(boolean fieldFormControlEmbedded) {
		this.fieldFormControlEmbedded = fieldFormControlEmbedded;
	}

	public IInfoFilter getFieldFormControlFilter() {
		return fieldFormControlFilter;
	}

	public void setFieldFormControlFilter(IInfoFilter fieldFormControlFilter) {
		this.fieldFormControlFilter = fieldFormControlFilter;
	}

	public Map<String, Object> getFieldSpecificProperties() {
		return fieldSpecificProperties;
	}

	public void setFieldSpecificProperties(Map<String, Object> fieldSpecificProperties) {
		this.fieldSpecificProperties = fieldSpecificProperties;
	}

	public IInfoProxyFactory getFieldTypeSpecificities() {
		return fieldTypeSpecificities;
	}

	public void setFieldTypeSpecificities(IInfoProxyFactory fieldTypeSpecificities) {
		this.fieldTypeSpecificities = fieldTypeSpecificities;
	}

	public ITypeInfo getFieldType() {
		return fieldType;
	}

	protected IFieldInfo createValueField() {
		return new ValueFieldInfo();
	}

	protected boolean hasFieldValueOptions() {
		return false;
	}

	protected Object[] getFieldValueOptions() {
		return null;
	}

	protected Runnable getFieldCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (fieldAutoUpdatePeriodMilliseconds ^ (fieldAutoUpdatePeriodMilliseconds >>> 32));
		result = prime * result + ((fieldCaption == null) ? 0 : fieldCaption.hashCode());
		result = prime * result + ((fieldCategory == null) ? 0 : fieldCategory.hashCode());
		result = prime * result + (fieldFormControlEmbedded ? 1231 : 1237);
		result = prime * result + ((fieldFormControlFilter == null) ? 0 : fieldFormControlFilter.hashCode());
		result = prime * result + (fieldFormControlMandatory ? 1231 : 1237);
		result = prime * result + (fieldGetOnly ? 1231 : 1237);
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + (fieldNullValueDistinct ? 1231 : 1237);
		result = prime * result + ((fieldNullValueLabel == null) ? 0 : fieldNullValueLabel.hashCode());
		result = prime * result + ((fieldOnlineHelp == null) ? 0 : fieldOnlineHelp.hashCode());
		result = prime * result + ((fieldSpecificProperties == null) ? 0 : fieldSpecificProperties.hashCode());
		result = prime * result + (fieldTransient ? 1231 : 1237);
		result = prime * result + ((fieldType == null) ? 0 : fieldType.hashCode());
		result = prime * result + ((fieldTypeSpecificities == null) ? 0 : fieldTypeSpecificities.hashCode());
		result = prime * result + ((fieldValueReturnMode == null) ? 0 : fieldValueReturnMode.hashCode());
		result = prime * result + ((typeCaption == null) ? 0 : typeCaption.hashCode());
		result = prime * result + (typeModificationStackAccessible ? 1231 : 1237);
		result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
		result = prime * result + ((typeOnlineHelp == null) ? 0 : typeOnlineHelp.hashCode());
		result = prime * result + ((typeSpecificProperties == null) ? 0 : typeSpecificProperties.hashCode());
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
		EncapsulatedObjectFactory other = (EncapsulatedObjectFactory) obj;
		if (fieldAutoUpdatePeriodMilliseconds != other.fieldAutoUpdatePeriodMilliseconds)
			return false;
		if (fieldCaption == null) {
			if (other.fieldCaption != null)
				return false;
		} else if (!fieldCaption.equals(other.fieldCaption))
			return false;
		if (fieldCategory == null) {
			if (other.fieldCategory != null)
				return false;
		} else if (!fieldCategory.equals(other.fieldCategory))
			return false;
		if (fieldFormControlEmbedded != other.fieldFormControlEmbedded)
			return false;
		if (fieldFormControlFilter == null) {
			if (other.fieldFormControlFilter != null)
				return false;
		} else if (!fieldFormControlFilter.equals(other.fieldFormControlFilter))
			return false;
		if (fieldFormControlMandatory != other.fieldFormControlMandatory)
			return false;
		if (fieldGetOnly != other.fieldGetOnly)
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (fieldNullValueDistinct != other.fieldNullValueDistinct)
			return false;
		if (fieldNullValueLabel == null) {
			if (other.fieldNullValueLabel != null)
				return false;
		} else if (!fieldNullValueLabel.equals(other.fieldNullValueLabel))
			return false;
		if (fieldOnlineHelp == null) {
			if (other.fieldOnlineHelp != null)
				return false;
		} else if (!fieldOnlineHelp.equals(other.fieldOnlineHelp))
			return false;
		if (fieldSpecificProperties == null) {
			if (other.fieldSpecificProperties != null)
				return false;
		} else if (!fieldSpecificProperties.equals(other.fieldSpecificProperties))
			return false;
		if (fieldTransient != other.fieldTransient)
			return false;
		if (fieldType == null) {
			if (other.fieldType != null)
				return false;
		} else if (!fieldType.equals(other.fieldType))
			return false;
		if (fieldTypeSpecificities == null) {
			if (other.fieldTypeSpecificities != null)
				return false;
		} else if (!fieldTypeSpecificities.equals(other.fieldTypeSpecificities))
			return false;
		if (fieldValueReturnMode != other.fieldValueReturnMode)
			return false;
		if (typeCaption == null) {
			if (other.typeCaption != null)
				return false;
		} else if (!typeCaption.equals(other.typeCaption))
			return false;
		if (typeModificationStackAccessible != other.typeModificationStackAccessible)
			return false;
		if (typeName == null) {
			if (other.typeName != null)
				return false;
		} else if (!typeName.equals(other.typeName))
			return false;
		if (typeOnlineHelp == null) {
			if (other.typeOnlineHelp != null)
				return false;
		} else if (!typeOnlineHelp.equals(other.typeOnlineHelp))
			return false;
		if (typeSpecificProperties == null) {
			if (other.typeSpecificProperties != null)
				return false;
		} else if (!typeSpecificProperties.equals(other.typeSpecificProperties))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return EncapsulatedObjectFactory.class.getSimpleName() + " [typeCaption=" + typeCaption + ", fieldType="
				+ fieldType + ", fieldCaption=" + fieldCaption + "]";
	}

	public class TypeInfo extends AbstractInfo implements ITypeInfo {

		protected IFieldInfo valueField;
		protected List<IMethodInfo> constructors;

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(TypeInfo.this, null);
		}

		@Override
		public ITransactionInfo getTransaction(Object object) {
			return null;
		}

		@Override
		public CategoriesStyle getCategoriesStyle() {
			return CategoriesStyle.getDefault();
		}

		@Override
		public ResourcePath getFormBackgroundImagePath() {
			return null;
		}

		@Override
		public ColorSpecification getFormBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormBorderColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormButtonBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormButtonForegroundColor() {
			return null;
		}

		@Override
		public ResourcePath getFormButtonBackgroundImagePath() {
			return null;
		}

		@Override
		public ColorSpecification getFormButtonBorderColor() {
			return null;
		}

		@Override
		public ColorSpecification getCategoriesBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getCategoriesForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorsForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorsBackgroundColor() {
			return null;
		}

		@Override
		public Dimension getFormPreferredSize() {
			return null;
		}

		@Override
		public int getFormSpacing() {
			return ITypeInfo.DEFAULT_FORM_SPACING;
		}

		@Override
		public boolean onFormVisibilityChange(Object object, boolean visible) {
			return false;
		}

		@Override
		public boolean canPersist() {
			return false;
		}

		@Override
		public void save(Object object, OutputStream out) {
		}

		@Override
		public void load(Object object, InputStream in) {
		}

		@Override
		public String getName() {
			return typeName;
		}

		@Override
		public String getCaption() {
			return typeCaption;
		}

		@Override
		public FieldsLayout getFieldsLayout() {
			return FieldsLayout.VERTICAL_FLOW;
		}

		@Override
		public MethodsLayout getMethodsLayout() {
			return MethodsLayout.HORIZONTAL_FLOW;
		}

		@Override
		public MenuModel getMenuModel() {
			return new MenuModel();
		}

		@Override
		public ResourcePath getIconImagePath() {
			return typeIconImagePath;
		}

		@Override
		public String getOnlineHelp() {
			return typeOnlineHelp;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return typeSpecificProperties;
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isImmutable() {
			return false;
		}

		@Override
		public boolean isConcrete() {
			return fieldType.isConcrete();
		}

		@Override
		public boolean isModificationStackAccessible() {
			return typeModificationStackAccessible;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			if (constructors == null) {
				constructors = new ArrayList<IMethodInfo>();
				for (IMethodInfo ctor : fieldType.getConstructors()) {
					constructors.add(new MethodInfoProxy(ctor) {

						ITypeInfo returnValueType;

						@Override
						public Object invoke(Object ignore, InvocationData invocationData) {
							return getInstance(Accessor.returning(super.invoke(ignore, invocationData), true));
						}

						@Override
						public ITypeInfo getReturnValueType() {
							if (returnValueType == null) {
								returnValueType = reflectionUI.buildTypeInfo(TypeInfo.this.getSource());
							}
							return returnValueType;
						}

						@Override
						public String getSignature() {
							return ReflectionUIUtils.buildMethodSignature(this);
						}

					});
				}
			}
			return constructors;
		}

		@Override
		public List<IFieldInfo> getFields() {
			if (valueField == null) {
				valueField = createValueField();
			}
			return Collections.<IFieldInfo>singletonList(valueField);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public boolean supports(Object object) {
			if (!(object instanceof Instance)) {
				return false;
			}
			if (!getFactory().equals(((Instance) object).getFactory())) {
				return false;
			}
			return true;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public boolean canCopy(Object object) {
			Instance instance = (Instance) object;
			return ReflectionUIUtils.canCopy(reflectionUI, instance.getValue());
		}

		@Override
		public Object copy(Object object) {
			Instance instance = (Instance) object;
			Object instanceValueCopy = ReflectionUIUtils.copy(reflectionUI, instance.getValue());
			return getInstance(new Object[] { instanceValueCopy });
		}

		@Override
		public void validate(Object object) throws Exception {
		}

		public EncapsulatedObjectFactory getFactory() {
			return EncapsulatedObjectFactory.this;
		}

		@Override
		public String toString(Object object) {
			Instance instance = (Instance) object;
			return ReflectionUIUtils.toString(reflectionUI, instance.getValue());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getFactory().hashCode();
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
			TypeInfo other = (TypeInfo) obj;
			if (!getFactory().equals(other.getFactory()))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TypeInfo [of=" + getFactory() + "]";
		}

	}

	public class Instance {
		protected Accessor<Object> fieldValueAccessor;

		public Instance(final Object[] fieldValueHolder) {
			this(new ArrayAccessor<Object>(fieldValueHolder));
		}

		public Instance(Accessor<Object> fieldValueAccessor) {
			this.fieldValueAccessor = fieldValueAccessor;
		}

		protected void checkValue(Object value) {
			if ((value != null) && !fieldType.supports(value)) {
				throw new ReflectionUIError();
			}
		}

		public Object getValue() {
			Object value = fieldValueAccessor.get();
			checkValue(value);
			return value;
		}

		public void setValue(Object value) {
			checkValue(value);
			fieldValueAccessor.set(value);
		}

		public EncapsulatedObjectFactory getFactory() {
			return EncapsulatedObjectFactory.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getFactory().hashCode();
			result = prime * result + ((fieldValueAccessor == null) ? 0 : fieldValueAccessor.hashCode());
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
			Instance other = (Instance) obj;
			if (!getFactory().equals(other.getFactory()))
				return false;
			if (fieldValueAccessor == null) {
				if (other.fieldValueAccessor != null)
					return false;
			} else if (!fieldValueAccessor.equals(other.fieldValueAccessor))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Encapsulated [fieldValueAccessor=" + fieldValueAccessor + ", factory=" + getFactory() + "]";
		}

	}

	public class ValueFieldInfo extends AbstractInfo implements IFieldInfo {

		protected ITypeInfo type;

		@Override
		public String getName() {
			return fieldName;
		}

		@Override
		public double getDisplayAreaHorizontalWeight() {
			return 1.0;
		}

		@Override
		public double getDisplayAreaVerticalWeight() {
			return 1.0;
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean visible) {
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public String getCaption() {
			return fieldCaption;
		}

		@Override
		public void setValue(Object object, Object value) {
			Instance instance = (Instance) object;
			instance.setValue(value);
		}

		@Override
		public boolean isGetOnly() {
			return fieldGetOnly;
		}

		@Override
		public boolean isTransient() {
			return fieldTransient;
		}

		@Override
		public boolean isNullValueDistinct() {
			return fieldNullValueDistinct;
		}

		@Override
		public String getNullValueLabel() {
			return fieldNullValueLabel;
		}

		@Override
		public boolean isFormControlMandatory() {
			return fieldFormControlMandatory;
		}

		@Override
		public boolean isFormControlEmbedded() {
			return fieldFormControlEmbedded;
		}

		@Override
		public IInfoFilter getFormControlFilter() {
			return fieldFormControlFilter;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			Map<String, Object> result = new HashMap<String, Object>(fieldSpecificProperties);
			result.put(IS_ENCAPSULATION_FIELD_PROPERTY_KEY, true);
			return result;
		}

		@Override
		public Object getValue(Object object) {
			Instance instance = (Instance) object;
			return instance.getValue();
		}

		@Override
		public ITypeInfo getType() {
			if (type == null) {
				type = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(fieldType.getSource()) {
					@Override
					public SpecificitiesIdentifier getSpecificitiesIdentifier() {
						return new SpecificitiesIdentifier(typeName, ValueFieldInfo.this.getName());
					}

					@Override
					protected String getTypeInfoProxyFactoryIdentifier() {
						return "FieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", containingType="
								+ typeName + "]";
					}
				});
			}
			return type;
		}

		@Override
		public List<IMethodInfo> getAlternativeConstructors(Object object) {
			return null;
		}

		@Override
		public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
			return null;
		}

		@Override
		public String getOnlineHelp() {
			return fieldOnlineHelp;
		}

		@Override
		public boolean hasValueOptions(Object object) {
			return hasFieldValueOptions();
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return getFieldValueOptions();
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			return null;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return fieldValueReturnMode;
		}

		@Override
		public InfoCategory getCategory() {
			return fieldCategory;
		}

		@Override
		public long getAutoUpdatePeriodMilliseconds() {
			return fieldAutoUpdatePeriodMilliseconds;
		}

		public EncapsulatedObjectFactory getFactory() {
			return EncapsulatedObjectFactory.this;
		}

		@Override
		public int hashCode() {
			return getFactory().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ValueFieldInfo)) {
				return false;
			}
			ValueFieldInfo other = (ValueFieldInfo) obj;
			if (!getFactory().equals(other.getFactory())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "ValueField [of=" + getFactory() + "]";
		}

	}

}
