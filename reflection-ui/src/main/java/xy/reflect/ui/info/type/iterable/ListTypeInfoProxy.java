package xy.reflect.ui.info.type.iterable;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfoProxy;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransaction;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.Mapper;

/**
 * List type information proxy class. The methods in this class should be
 * overridden to provide custom information.
 * 
 * @author olitank
 *
 */
public class ListTypeInfoProxy extends AbstractInfoProxy implements IListTypeInfo {

	protected IListTypeInfo base;

	public ListTypeInfoProxy(IListTypeInfo base) {
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public ITransaction createTransaction(Object object) {
		return base.createTransaction(object);
	}

	public void onFormRefresh(Object object) {
		base.onFormRefresh(object);
	}

	public Runnable getLastFormRefreshStateRestorationJob(Object object) {
		return base.getLastFormRefreshStateRestorationJob(object);
	}

	public ITypeInfoSource getSource() {
		return base.getSource();
	}

	public boolean canPersist() {
		return base.canPersist();
	}

	public void save(Object object, OutputStream out) {
		base.save(object, out);
	}

	public void load(Object object, InputStream in) {
		base.load(object, in);
	}

	public boolean isImmutable() {
		return base.isImmutable();
	}

	public boolean isPrimitive() {
		return base.isPrimitive();
	}

	public boolean isConcrete() {
		return base.isConcrete();
	}

	public List<IMethodInfo> getConstructors() {
		return base.getConstructors();
	}

	public List<IFieldInfo> getFields() {
		return base.getFields();
	}

	public List<IMethodInfo> getMethods() {
		return base.getMethods();
	}

	public boolean supports(Object object) {
		return base.supports(object);
	}

	public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
		return base.getPolymorphicInstanceSubTypes();
	}

	public String toString(Object object) {
		return base.toString(object);
	}

	public void validate(Object object) throws Exception {
		base.validate(object);
	}

	public boolean canCopy(Object object) {
		return base.canCopy(object);
	}

	public ITypeInfo getItemType() {
		return base.getItemType();
	}

	public Object copy(Object object) {
		return base.copy(object);
	}

	public Object[] toArray(Object listValue) {
		return base.toArray(listValue);
	}

	public boolean isModificationStackAccessible() {
		return base.isModificationStackAccessible();
	}

	public boolean canInstantiateFromArray() {
		return base.canInstantiateFromArray();
	}

	public ResourcePath getIconImagePath(Object object) {
		return base.getIconImagePath(object);
	}

	public Object fromArray(Object[] array) {
		return base.fromArray(array);
	}

	public FieldsLayout getFieldsLayout() {
		return base.getFieldsLayout();
	}

	public boolean canReplaceContent() {
		return base.canReplaceContent();
	}

	public MethodsLayout getMethodsLayout() {
		return base.getMethodsLayout();
	}

	public void replaceContent(Object listValue, Object[] array) {
		base.replaceContent(listValue, array);
	}

	public MenuModel getMenuModel() {
		return base.getMenuModel();
	}

	public boolean onFormVisibilityChange(Object object, boolean visible) {
		return base.onFormVisibilityChange(object, visible);
	}

	public IListStructuralInfo getStructuralInfo() {
		return base.getStructuralInfo();
	}

	public IListItemDetailsAccessMode getDetailsAccessMode() {
		return base.getDetailsAccessMode();
	}

	public int getFormPreferredWidth() {
		return base.getFormPreferredWidth();
	}

	public int getFormPreferredHeight() {
		return base.getFormPreferredHeight();
	}

	public int getFormSpacing() {
		return base.getFormSpacing();
	}

	public boolean isInsertionAllowed() {
		return base.isInsertionAllowed();
	}

	public boolean isMoveAllowed() {
		return base.isMoveAllowed();
	}

	public ColorSpecification getFormBackgroundColor() {
		return base.getFormBackgroundColor();
	}

	public boolean isRemovalAllowed() {
		return base.isRemovalAllowed();
	}

	public ColorSpecification getFormForegroundColor() {
		return base.getFormForegroundColor();
	}

	public boolean canViewItemDetails() {
		return base.canViewItemDetails();
	}

	public List<IDynamicListAction> getDynamicActions(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		return base.getDynamicActions(selection, listModificationFactoryAccessor);
	}

	public ColorSpecification getFormBorderColor() {
		return base.getFormBorderColor();
	}

	public ResourcePath getFormBackgroundImagePath() {
		return base.getFormBackgroundImagePath();
	}

	public CategoriesStyle getCategoriesStyle() {
		return base.getCategoriesStyle();
	}

	public ColorSpecification getCategoriesBackgroundColor() {
		return base.getCategoriesBackgroundColor();
	}

	public ColorSpecification getCategoriesForegroundColor() {
		return base.getCategoriesForegroundColor();
	}

	public List<IDynamicListProperty> getDynamicProperties(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		return base.getDynamicProperties(selection, listModificationFactoryAccessor);
	}

	public ColorSpecification getFormEditorForegroundColor() {
		return base.getFormEditorForegroundColor();
	}

	public ColorSpecification getFormEditorBackgroundColor() {
		return base.getFormEditorBackgroundColor();
	}

	public ColorSpecification getFormButtonBackgroundColor() {
		return base.getFormButtonBackgroundColor();
	}

	public boolean isItemNullValueSupported() {
		return base.isItemNullValueSupported();
	}

	public ColorSpecification getFormButtonForegroundColor() {
		return base.getFormButtonForegroundColor();
	}

	public ItemCreationMode getItemCreationMode() {
		return base.getItemCreationMode();
	}

	public ResourcePath getFormButtonBackgroundImagePath() {
		return base.getFormButtonBackgroundImagePath();
	}

	public ValueReturnMode getItemReturnMode() {
		return base.getItemReturnMode();
	}

	public ColorSpecification getFormButtonBorderColor() {
		return base.getFormButtonBorderColor();
	}

	public boolean isValidationRequired() {
		return base.isValidationRequired();
	}

	public IFieldInfo getSelectionTargetField(ITypeInfo objectType) {
		return base.getSelectionTargetField(objectType);
	}

	public boolean areItemsAutomaticallyPositioned() {
		return base.areItemsAutomaticallyPositioned();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListTypeInfoProxy other = (ListTypeInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ListTypeInfoProxy [name=" + getName() + ", base=" + base + "]";
	}
}
