
package xy.reflect.ui.info.field;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransactionInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Virtual list field that aggregates many field values to simulate a list
 * value. The number of list items is equal to the number of provided fields.
 * 
 * @author olitank
 *
 */
public class MultipleFieldsAsListFieldInfo extends AbstractInfo implements IFieldInfo {

	protected List<IFieldInfo> fields;
	protected ReflectionUI reflectionUI;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public MultipleFieldsAsListFieldInfo(ReflectionUI reflectionUI, List<IFieldInfo> fields, ITypeInfo containingType) {
		this.reflectionUI = reflectionUI;
		this.fields = fields;
		this.containingType = containingType;
	}

	public String getItemTitle(IFieldInfo field) {
		return field.getCaption();
	}

	@Override
	public String getCaption() {
		StringBuilder result = new StringBuilder(MultipleFieldsAsListFieldInfo.class.getSimpleName());
		result.append("List Containing ");
		int i = 0;
		for (IFieldInfo field : fields) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(field.getCaption());
			i++;
		}
		return result.toString();
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI
					.buildTypeInfo(new PrecomputedTypeInstanceWrapper.TypeInfoSource(new ValueListTypeInfo()));
		}
		return type;
	}

	@Override
	public Object getValue(Object object) {
		List<Object> result = new ArrayList<Object>();
		for (IFieldInfo field : fields) {
			result.add(
					new PrecomputedTypeInstanceWrapper(getListItem(object, field), new ValueListItemTypeInfo(field)));
		}
		return new PrecomputedTypeInstanceWrapper(result, new ValueListTypeInfo());
	}

	protected ValueListItem getListItem(Object object, IFieldInfo listFieldInfo) {
		return new ValueListItem(object, listFieldInfo);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
		throw new ReflectionUIError();
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
	public boolean isGetOnly() {
		return true;
	}

	@Override
	public boolean isTransient() {
		return true;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getName() {
		StringBuilder result = new StringBuilder("multipleFieldsAsList [fields=");
		int i = 0;
		for (IFieldInfo field : fields) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(field.getName());
			i++;
		}
		result.append("]");
		return result.toString();
	}

	@Override
	public boolean isHidden() {
		return false;
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
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public boolean isFormControlMandatory() {
		return false;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return false;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		return fields.hashCode();
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
		return fields.equals(((MultipleFieldsAsListFieldInfo) obj).fields);
	}

	@Override
	public String toString() {
		return "MultipleFieldAsListField [fields=" + fields + "]";
	}

	public class ValueListItem {

		protected Object object;
		protected IFieldInfo field;

		public ValueListItem(Object object, IFieldInfo field) {
			this.object = object;
			this.field = field;
		}

		public Object getObject() {
			return object;
		}

		public IFieldInfo getField() {
			return field;
		}

		public MultipleFieldsAsListFieldInfo getSourceField() {
			return MultipleFieldsAsListFieldInfo.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getSourceField().hashCode();
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			result = prime * result + ((object == null) ? 0 : object.hashCode());
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
			ValueListItem other = (ValueListItem) obj;
			if (!getSourceField().equals(other.getSourceField()))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getItemTitle(field);
		}

	}

	public class ValueListTypeInfo extends StandardCollectionTypeInfo {

		public ValueListTypeInfo() {
			super(new JavaTypeInfoSource(MultipleFieldsAsListFieldInfo.this.reflectionUI, ArrayList.class, null),
					MultipleFieldsAsListFieldInfo.this.reflectionUI
							.buildTypeInfo(new PrecomputedTypeInstanceWrapper.TypeInfoSource(new DefaultTypeInfo(
									new JavaTypeInfoSource(MultipleFieldsAsListFieldInfo.this.reflectionUI,
											ValueListItem.class, null)))));
		}

		@Override
		public String getName() {
			return "ValueListTypeInfo [of=" + MultipleFieldsAsListFieldInfo.this.getName() + "]";
		}

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this, new SpecificitiesIdentifier(containingType.getName(),
					MultipleFieldsAsListFieldInfo.this.getName()));
		}

		@Override
		public boolean isInsertionAllowed() {
			return false;
		}

		@Override
		public boolean isRemovalAllowed() {
			return false;
		}

		@Override
		public boolean isOrdered() {
			return false;
		}

		@Override
		public boolean canViewItemDetails() {
			return false;
		}

		@Override
		public String toString() {
			return "ValueListTypeInfo [of=" + MultipleFieldsAsListFieldInfo.this + "]";
		}

	}

	public class ValueListItemTypeInfo extends AbstractInfo implements ITypeInfo {

		protected IFieldInfo field;

		public ValueListItemTypeInfo(IFieldInfo field) {
			this.field = field;
		}

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this, null);
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
		public ColorSpecification getFormEditorsForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorsBackgroundColor() {
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
		public Dimension getFormPreferredSize() {
			return null;
		}

		@Override
		public int getFormSpacing() {
			return ITypeInfo.DEFAULT_FORM_SPACING;
		}

		@Override
		public boolean canPersist() {
			return false;
		}

		@Override
		public boolean onFormVisibilityChange(Object object, boolean visible) {
			return false;
		}

		@Override
		public void save(Object object, OutputStream out) {
		}

		@Override
		public void load(Object object, InputStream in) {
		}

		@Override
		public ResourcePath getIconImagePath() {
			return null;
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
		public String getCaption() {
			return getItemTitle(field);
		}

		public IFieldInfo getDetailsField() {
			return new ValueListItemDetailsFieldInfo(field);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.<IFieldInfo>singletonList(getDetailsField());
		}

		@Override
		public String getName() {
			return "ValueListItemTypeInfo [of=" + MultipleFieldsAsListFieldInfo.this.getName() + ", field="
					+ field.getName() + "]";
		}

		@Override
		public String getOnlineHelp() {
			return field.getOnlineHelp();
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return field.getSpecificProperties();
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
			return true;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.emptyList();
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public boolean supports(Object object) {
			if (!(object instanceof ValueListItem)) {
				return false;
			}
			if (!getSourceField().equals(((ValueListItem) object).getSourceField())) {
				return false;
			}
			return true;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public String toString(Object object) {
			return getItemTitle(field);
		}

		@Override
		public void validate(Object object) throws Exception {
		}

		@Override
		public boolean canCopy(Object object) {
			return false;
		}

		@Override
		public Object copy(Object object) {
			throw new ReflectionUIError();
		}

		@Override
		public boolean isModificationStackAccessible() {
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getSourceField().hashCode();
			result = prime * result + ((field == null) ? 0 : field.hashCode());
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
			ValueListItemTypeInfo other = (ValueListItemTypeInfo) obj;
			if (!getSourceField().equals(other.getSourceField()))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			return true;
		}

		public MultipleFieldsAsListFieldInfo getSourceField() {
			return MultipleFieldsAsListFieldInfo.this;
		}

		@Override
		public String toString() {
			return "ValueListItemTypeInfo [of=" + MultipleFieldsAsListFieldInfo.this + ", field=" + field + "]";
		}

	}

	public class ValueListItemDetailsFieldInfo extends FieldInfoProxy {

		public ValueListItemDetailsFieldInfo(IFieldInfo field) {
			super(field);
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(base.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return new SpecificitiesIdentifier(
							new ValueListItemTypeInfo(ValueListItemDetailsFieldInfo.this.base).getName(),
							ValueListItemDetailsFieldInfo.this.getName());
				}

				@Override
				protected String getTypeInfoProxyFactoryIdentifier() {
					return "FieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", parent="
							+ MultipleFieldsAsListFieldInfo.this.getName() + ", containingType="
							+ containingType.getName() + "]";
				}
			});
		}

		@Override
		public String getCaption() {
			return getItemTitle(base);
		}

		@Override
		public Object getValue(Object object) {
			ValueListItem valueListItem = (ValueListItem) object;
			object = valueListItem.getObject();
			return super.getValue(object);
		}

		@Override
		public void setValue(Object object, Object value) {
			ValueListItem valueListItem = (ValueListItem) object;
			object = valueListItem.getObject();
			super.setValue(object, value);
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			ValueListItem valueListItem = (ValueListItem) object;
			object = valueListItem.getObject();
			return super.getNextUpdateCustomUndoJob(object, value);
		}

		@Override
		public boolean hasValueOptions(Object object) {
			ValueListItem valueListItem = (ValueListItem) object;
			object = valueListItem.getObject();
			return super.hasValueOptions(object);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			ValueListItem valueListItem = (ValueListItem) object;
			object = valueListItem.getObject();
			return super.getValueOptions(object);
		}

		@Override
		public List<IMethodInfo> getAlternativeConstructors(Object object) {
			ValueListItem valueListItem = (ValueListItem) object;
			object = valueListItem.getObject();
			return super.getAlternativeConstructors(object);
		}

		@Override
		public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
			ValueListItem valueListItem = (ValueListItem) object;
			object = valueListItem.getObject();
			return super.getAlternativeListItemConstructors(object);
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean visible) {
			ValueListItem valueListItem = (ValueListItem) object;
			object = valueListItem.getObject();
			super.onControlVisibilityChange(object, visible);
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public String toString() {
			return "ValueListItemDetailsFieldInfo [of=" + MultipleFieldsAsListFieldInfo.this + ", field=" + base + "]";
		}
	}

}
