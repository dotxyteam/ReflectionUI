
package xy.reflect.ui.info.field;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransaction;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
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
	protected ITypeInfo objectType;

	public MultipleFieldsAsListFieldInfo(ReflectionUI reflectionUI, List<IFieldInfo> fields, ITypeInfo objectType) {
		this.reflectionUI = reflectionUI;
		this.fields = fields;
		this.objectType = objectType;
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
		return reflectionUI.getTypeInfo(new PrecomputedTypeInstanceWrapper.TypeInfoSource(new ListTypeInfo()));
	}

	@Override
	public Object getValue(Object object) {
		List<Object> result = new ArrayList<Object>();
		for (IFieldInfo field : fields) {
			result.add(new PrecomputedTypeInstanceWrapper(getListItem(object, field), new ListItemTypeInfo(field)));
		}
		return new PrecomputedTypeInstanceWrapper(result, new ListTypeInfo());
	}

	protected ListItem getListItem(Object object, IFieldInfo listFieldInfo) {
		return new ListItem(object, listFieldInfo);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public Runnable getPreviousUpdateCustomRedoJob(Object object, Object newValue) {
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
	public boolean isRelevant(Object object) {
		return true;
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 0.0;
	}

	@Override
	public boolean isDisplayAreaHorizontallyFilled() {
		return true;
	}

	@Override
	public boolean isDisplayAreaVerticallyFilled() {
		return false;
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
	public boolean isControlValueValiditionEnabled() {
		return false;
	}

	@Override
	public IValidationJob getValueAbstractFormValidationJob(Object object) {
		return null;
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

	public class ListItem {

		protected Object object;
		protected IFieldInfo field;

		public ListItem(Object object, IFieldInfo field) {
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
			ListItem other = (ListItem) obj;
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

	public class ListTypeInfo extends StandardCollectionTypeInfo {

		public ListTypeInfo() {
			super(MultipleFieldsAsListFieldInfo.this.reflectionUI, new JavaTypeInfoSource(ArrayList.class, null),
					MultipleFieldsAsListFieldInfo.this.reflectionUI
							.getTypeInfo(new PrecomputedTypeInstanceWrapper.TypeInfoSource(
									new DefaultTypeInfo(MultipleFieldsAsListFieldInfo.this.reflectionUI,
											new JavaTypeInfoSource(ListItem.class, null)))));
		}

		@Override
		public String getName() {
			return "ListTypeInfo [of=" + MultipleFieldsAsListFieldInfo.this.getName() + "]";
		}

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this,
					new SpecificitiesIdentifier(objectType.getName(), MultipleFieldsAsListFieldInfo.this.getName()));
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
		public boolean isMoveAllowed() {
			return false;
		}

		@Override
		public boolean canViewItemDetails() {
			return false;
		}

		@Override
		public String toString() {
			return "ListTypeInfo [of=" + MultipleFieldsAsListFieldInfo.this + "]";
		}

	}

	public class ListItemTypeInfo extends AbstractInfo implements ITypeInfo {

		protected IFieldInfo field;

		public ListItemTypeInfo(IFieldInfo field) {
			this.field = field;
		}

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this, null);
		}

		@Override
		public boolean isValidationRequired() {
			return false;
		}

		@Override
		public ITransaction createTransaction(Object object) {
			return null;
		}

		@Override
		public void onFormRefresh(Object object) {
		}

		@Override
		public Runnable getLastFormRefreshStateRestorationJob(Object object) {
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
		public ColorSpecification getFormEditorForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorBackgroundColor() {
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
		public int getFormPreferredWidth() {
			return -1;
		}

		@Override
		public int getFormPreferredHeight() {
			return -1;
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
		public void save(Object object, File outputFile) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void load(Object object, File inputFile) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ResourcePath getIconImagePath(Object object) {
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
			return new ListItemDetailsFieldInfo(field);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.<IFieldInfo>singletonList(getDetailsField());
		}

		@Override
		public String getName() {
			return "ListItemTypeInfo [of=" + MultipleFieldsAsListFieldInfo.this.getName() + ", field=" + field.getName()
					+ "]";
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
			if (!(object instanceof ListItem)) {
				return false;
			}
			if (!getSourceField().equals(((ListItem) object).getSourceField())) {
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
		public void validate(Object object, ValidationSession session) throws Exception {
		}

		@Override
		public boolean canCopy(Object object) {
			return false;
		}

		@Override
		public Object copy(Object object) {
			throw new UnsupportedOperationException();
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
			ListItemTypeInfo other = (ListItemTypeInfo) obj;
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
			return "ListItemTypeInfo [of=" + MultipleFieldsAsListFieldInfo.this + ", field=" + field + "]";
		}

	}

	public class ListItemDetailsFieldInfo extends FieldInfoProxy {

		public ListItemDetailsFieldInfo(IFieldInfo field) {
			super(field);
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.getTypeInfo(new TypeInfoSourceProxy(base.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return new SpecificitiesIdentifier(
							new ListItemTypeInfo(ListItemDetailsFieldInfo.this.base).getName(),
							ListItemDetailsFieldInfo.this.getName());
				}

				@Override
				protected String getTypeInfoProxyFactoryIdentifier() {
					return "FieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", parent="
							+ MultipleFieldsAsListFieldInfo.this.getName() + ", objectType=" + objectType.getName()
							+ "]";
				}
			});
		}

		@Override
		public String getCaption() {
			return getItemTitle(base);
		}

		@Override
		public boolean isRelevant(Object object) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			return super.isRelevant(object);
		}

		@Override
		public Object getValue(Object object) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			return super.getValue(object);
		}

		@Override
		public void setValue(Object object, Object value) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			super.setValue(object, value);
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			return super.getNextUpdateCustomUndoJob(object, value);
		}

		@Override
		public Runnable getPreviousUpdateCustomRedoJob(Object object, Object value) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			return super.getPreviousUpdateCustomRedoJob(object, value);
		}

		@Override
		public boolean hasValueOptions(Object object) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			return super.hasValueOptions(object);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			return super.getValueOptions(object);
		}

		@Override
		public List<IMethodInfo> getAlternativeConstructors(Object object) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			return super.getAlternativeConstructors(object);
		}

		@Override
		public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			return super.getAlternativeListItemConstructors(object);
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean visible) {
			ListItem valueListItem = (ListItem) object;
			object = valueListItem.getObject();
			super.onControlVisibilityChange(object, visible);
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public String toString() {
			return "ListItemDetailsFieldInfo [of=" + MultipleFieldsAsListFieldInfo.this + ", field=" + base + "]";
		}
	}

}
