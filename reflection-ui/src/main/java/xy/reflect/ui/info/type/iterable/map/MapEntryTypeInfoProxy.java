package xy.reflect.ui.info.type.iterable.map;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfoProxy;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransaction;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

/**
 * Map entry type information proxy class. The methods in this class should be
 * overriden to provide custom information.
 * 
 * @author olitank
 *
 */

public class MapEntryTypeInfoProxy extends AbstractInfoProxy implements IMapEntryTypeInfo {

	protected IMapEntryTypeInfo base;

	public MapEntryTypeInfoProxy(IMapEntryTypeInfo base) {
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public IFieldInfo getKeyField() {
		return base.getKeyField();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public IFieldInfo getValueField() {
		return base.getValueField();
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

	public void validate(Object object, ValidationSession session) throws Exception {
		base.validate(object, session);
	}

	public boolean canCopy(Object object) {
		return base.canCopy(object);
	}

	public Object copy(Object object) {
		return base.copy(object);
	}

	public boolean isModificationStackAccessible() {
		return base.isModificationStackAccessible();
	}

	public ResourcePath getIconImagePath(Object object) {
		return base.getIconImagePath(object);
	}

	public FieldsLayout getFieldsLayout() {
		return base.getFieldsLayout();
	}

	public MethodsLayout getMethodsLayout() {
		return base.getMethodsLayout();
	}

	public MenuModel getMenuModel() {
		return base.getMenuModel();
	}

	public boolean onFormVisibilityChange(Object object, boolean visible) {
		return base.onFormVisibilityChange(object, visible);
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

	public ColorSpecification getFormBackgroundColor() {
		return base.getFormBackgroundColor();
	}

	public ColorSpecification getFormForegroundColor() {
		return base.getFormForegroundColor();
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

	public ColorSpecification getFormEditorForegroundColor() {
		return base.getFormEditorForegroundColor();
	}

	public ColorSpecification getFormEditorBackgroundColor() {
		return base.getFormEditorBackgroundColor();
	}

	public ColorSpecification getFormButtonBackgroundColor() {
		return base.getFormButtonBackgroundColor();
	}

	public ColorSpecification getFormButtonForegroundColor() {
		return base.getFormButtonForegroundColor();
	}

	public ResourcePath getFormButtonBackgroundImagePath() {
		return base.getFormButtonBackgroundImagePath();
	}

	public ColorSpecification getFormButtonBorderColor() {
		return base.getFormButtonBorderColor();
	}

	public boolean isValidationRequired() {
		return base.isValidationRequired();
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
		MapEntryTypeInfoProxy other = (MapEntryTypeInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MapEntryTypeInfoProxy [name=" + getName() + ", base=" + base + "]";
	}

}
