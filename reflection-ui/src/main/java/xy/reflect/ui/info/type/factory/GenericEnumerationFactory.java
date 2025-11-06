
package xy.reflect.ui.info.type.factory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransaction;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.IDerivedInstance;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Factory that generates virtual enumeration type information from the
 * specified list items (accessible via the {@link Iterable} object).
 * 
 * @author olitank
 *
 */
public class GenericEnumerationFactory {

	protected ReflectionUI reflectionUI;
	protected Iterable<?> iterable;
	protected String enumerationTypeName;
	protected String typeCaption;
	protected boolean dynamicEnumeration;
	protected List<Object> bufferedItems;
	protected boolean nullSupported;

	public GenericEnumerationFactory(ReflectionUI reflectionUI, Iterable<?> iterable, String enumerationTypeName,
			String typeCaption, boolean dynamicEnumeration, boolean nullSupported) {
		this.reflectionUI = reflectionUI;
		this.iterable = iterable;
		this.enumerationTypeName = enumerationTypeName;
		this.typeCaption = typeCaption;
		this.dynamicEnumeration = dynamicEnumeration;
		this.nullSupported = nullSupported;
	}

	public GenericEnumerationFactory(ReflectionUI reflectionUI, Object[] array, String enumerationTypeName,
			String typeCaption) {
		this(reflectionUI, Arrays.asList(array), enumerationTypeName, typeCaption, false,
				Arrays.asList(array).contains(null));
	}

	protected Map<String, Object> getItemSpecificProperties(Object item) {
		return Collections.emptyMap();
	}

	protected String getItemOnlineHelp(Object item) {
		return null;
	}

	protected String getItemName(Object item) {
		return "Item [value=" + ReflectionUIUtils.toString(reflectionUI, item) + "]";
	}

	protected String getItemCaption(Object item) {
		return ReflectionUIUtils.toString(reflectionUI, item);
	}

	protected ResourcePath getItemIconImagePath(Object item) {
		return ReflectionUIUtils.getIconImagePath(reflectionUI, item);
	}

	protected List<Object> getOrLoadItems() {
		if (bufferedItems == null) {
			List<Object> result = new ArrayList<Object>();
			for (Object item : iterable) {
				result.add(item);
			}
			bufferedItems = result;
		}
		return bufferedItems;
	}

	public void resetItemCache() {
		bufferedItems = null;
	}

	public Object getItemInstance(Object item) {
		if (item == null) {
			if (!getOrLoadItems().contains(null)) {
				return null;
			}
		}
		return new PrecomputedTypeInstanceWrapper(new Instance(item), new TypeInfo());
	}

	public Object getInstanceItem(Object object) {
		if (object == null) {
			if (getOrLoadItems().contains(null)) {
				throw new ReflectionUIError(
						"Raw null value not supported since null is a valid item (<null> item instance expected instead) !");
			}
			return null;
		}
		Instance instance = (Instance) ((PrecomputedTypeInstanceWrapper) object).getInstance();
		if (!instance.getFactory().equals(this)) {
			throw new ReflectionUIError();
		}
		return instance.getArrayItem();
	}

	public ITypeInfoSource getInstanceTypeInfoSource(SpecificitiesIdentifier specificitiesIdentifier) {
		return new PrecomputedTypeInstanceWrapper.TypeInfoSource(new TypeInfo()) {
			@Override
			public SpecificitiesIdentifier getSpecificitiesIdentifier() {
				return specificitiesIdentifier;
			}
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + iterable.hashCode();
		result = prime * result + ((typeCaption == null) ? 0 : typeCaption.hashCode());
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
		GenericEnumerationFactory other = (GenericEnumerationFactory) obj;
		if (!iterable.equals(other.iterable))
			return false;
		if (typeCaption == null) {
			if (other.typeCaption != null)
				return false;
		} else if (!typeCaption.equals(other.typeCaption))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GenericEnumerationFactory [enumerationTypeName=" + enumerationTypeName + ", typeCaption=" + typeCaption
				+ "]";
	}

	public class Instance implements IDerivedInstance{
		protected Object item;

		public Instance(Object item) {
			this.item = item;
		}

		@Override
		public Object getBaseInstance() {
			return item;
		}

		public Object getArrayItem() {
			return item;
		}

		public GenericEnumerationFactory getFactory() {
			return GenericEnumerationFactory.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getFactory().hashCode();
			result = prime * result + ((item == null) ? 0 : item.hashCode());
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
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GenericEnumerationInstance [item=" + item + "]";
		}

	}

	public class TypeInfo extends AbstractInfo implements IEnumerationTypeInfo {

		@Override
		public ITypeInfo getParent() {
			return null;
		}

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(TypeInfo.this, null);
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
			return CategoriesStyle.getDefault(reflectionUI, reflectionUI.getTypeInfo(getSource()));
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
		public ColorSpecification getFormEditorForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorBackgroundColor() {
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
		public void onFormVisibilityChange(Object object, boolean visible) {
		}

		@Override
		public void onFormCreation(Object object, boolean beforeOrAFter) {
		}

		@Override
		public boolean canPersist() {
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
		public boolean isDynamicEnumeration() {
			return dynamicEnumeration;
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
		public ResourcePath getIconImagePath(Object object) {
			return null;
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isImmutable() {
			return true;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getName() {
			return enumerationTypeName;
		}

		@Override
		public String getCaption() {
			return typeCaption;
		}

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public boolean isModificationStackAccessible() {
			return false;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public boolean isPolymorphicInstanceAbstractTypeOptionAllowed() {
			return false;
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.emptyList();
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			final List<Object> items = getOrLoadItems();
			if (items.size() == 0) {
				return Collections.emptyList();
			} else {
				return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

					@Override
					public ITypeInfo getReturnValueType() {
						return reflectionUI.getTypeInfo(TypeInfo.this.getSource());
					}

					@Override
					public Object invoke(Object ignore, InvocationData invocationData) {
						return new Instance(items.get(0));
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}
				});
			}
		}

		@Override
		public Object[] getValues() {
			List<Object> result = new ArrayList<Object>();
			resetItemCache();
			for (Object item : getOrLoadItems()) {
				result.add(new Instance(item));
			}
			return result.toArray();
		}

		@Override
		public IEnumerationItemInfo getValueInfo(final Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			Instance instance = (Instance) object;
			return new ItemInfo(instance.getArrayItem());
		}

		@Override
		public boolean supports(Object object) {
			if (object == null) {
				return nullSupported;
			}
			if (!(object instanceof Instance)) {
				return false;
			}
			Instance instance = (Instance) object;
			if (!instance.getFactory().equals(getFactory())) {
				return false;
			}
			return true;
		}

		@Override
		public void validate(Object object, ValidationSession session) throws Exception {
			ReflectionUIUtils.checkInstance(this, object);
		}

		@Override
		public boolean canCopy(Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			return false;
		}

		@Override
		public Object copy(Object object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString(Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			Instance instance = (Instance) object;
			return ReflectionUIUtils.toString(reflectionUI, instance.getArrayItem());
		}

		public GenericEnumerationFactory getFactory() {
			return GenericEnumerationFactory.this;
		}

		@Override
		public int hashCode() {
			return getFactory().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			if (!getFactory().equals(((TypeInfo) obj).getFactory())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "TypeInfo [of=" + getFactory() + "]";
		}

	}

	public class ItemInfo implements IEnumerationItemInfo {

		protected Object item;

		public ItemInfo(Object item) {
			this.item = item;
		}

		public Object getValue() {
			return item;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return getItemSpecificProperties(item);
		}

		@Override
		public ResourcePath getIconImagePath() {
			return getItemIconImagePath(item);
		}

		@Override
		public String getOnlineHelp() {
			return getItemOnlineHelp(item);
		}

		@Override
		public String getName() {
			return getItemName(item);
		}

		@Override
		public String getCaption() {
			return getItemCaption(item);
		}

		public GenericEnumerationFactory getFactory() {
			return GenericEnumerationFactory.this;
		}

		@Override
		public String toString() {
			return "ItemInfo [of=" + getFactory() + ", item=" + item + "]";
		}

	}
}
