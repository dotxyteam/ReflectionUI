package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;

public class MultipleFieldsAsListField   extends AbstractInfo implements IFieldInfo {

	protected List<IFieldInfo> fields;
	protected ReflectionUI reflectionUI;

	public MultipleFieldsAsListField(ReflectionUI reflectionUI, List<IFieldInfo> fields) {
		this.reflectionUI = reflectionUI;
		this.fields = fields;
	}

	public String getItemTitle(IFieldInfo field) {
		return field.getCaption();
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(new ValueListTypeInfo()));
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public String getCaption() {
		StringBuilder result = new StringBuilder(MultipleFieldsAsListField.class.getSimpleName());
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
	public Object getValue(Object object) {
		List<ValueListItem> result = new ArrayList<ValueListItem>();
		for (IFieldInfo field : fields) {
			ValueListItem listItem = getListItem(object, field);
			reflectionUI.registerPrecomputedTypeInfoObject(listItem, getListItemTypeInfo(field));
			result.add(listItem);
		}
		return result;
	}

	protected ValueListItem getListItem(Object object, IFieldInfo listFieldInfo) {
		return new ValueListItem(object, listFieldInfo);
	}

	protected ITypeInfo getListItemTypeInfo(IFieldInfo field) {
		return new ValueListItemTypeInfo(field);
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public boolean isGetOnly() {
		return false;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
	}

	@Override
	public void setValue(Object object, Object value) {
	}

	@Override
	public boolean isValueNullable() {
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
		StringBuilder result = new StringBuilder();
		int i = 0;
		for (IFieldInfo field : fields) {
			if (i > 0) {
				result.append("+");
			}
			result.append(field.getName());
			i++;
		}
		return result.toString();
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
		return fields.equals(((MultipleFieldsAsListField) obj).fields);
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((object == null) ? 0 : object.hashCode());
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
			ValueListItem other = (ValueListItem) obj;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getItemTitle(field);
		}

	}

	protected class ValueListTypeInfo extends StandardCollectionTypeInfo {

		public ValueListTypeInfo() {
			super(MultipleFieldsAsListField.this.reflectionUI, ArrayList.class, null);
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
	}

	protected class ValueListItemTypeInfo   extends AbstractInfo implements ITypeInfo {

		protected IFieldInfo field;

		public ValueListItemTypeInfo(IFieldInfo field) {
			this.field = field;
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
		public MenuModel getMenuModel() {
			return new MenuModel();
		}


		@Override
		public String getCaption() {
			return getItemTitle(field);
		}

		public IFieldInfo getDetailsField() {
			return new ValueListItemDetailsField(field);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.<IFieldInfo>singletonList(getDetailsField());
		}

		@Override
		public String getName() {
			return "ListItemTypeInfo [index=" + MultipleFieldsAsListField.this.fields.indexOf(field) + ", of="
					+ MultipleFieldsAsListField.this.getName() + "]";
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
		public boolean supportsInstance(Object object) {
			return object instanceof ValueListItem;
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
			result = prime * result + getOuterType().hashCode();
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
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			return true;
		}

		private MultipleFieldsAsListField getOuterType() {
			return MultipleFieldsAsListField.this;
		}

		@Override
		public String toString() {
			return "ListItemTypeInfo [index=" + MultipleFieldsAsListField.this.fields.indexOf(field) + ", of="
					+ getOuterType() + "]";
		}

	}

	protected class ValueListItemDetailsField extends FieldInfoProxy {

		public ValueListItemDetailsField(IFieldInfo field) {
			super(field);
		}

		@Override
		public String getCaption() {
			return getItemTitle(base);
		}

		@Override
		public Object getValue(Object object) {
			object = ((ValueListItem) object).getObject();
			return super.getValue(object);
		}

		@Override
		public void setValue(Object object, Object value) {
			object = ((ValueListItem) object).getObject();
			super.setValue(object, value);
		}

		@Override
		public Runnable getCustomUndoUpdateJob(Object object, Object value) {
			object = ((ValueListItem) object).getObject();
			return super.getCustomUndoUpdateJob(object, value);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			object = ((ValueListItem) object).getObject();
			return super.getValueOptions(object);
		}

	}

}
