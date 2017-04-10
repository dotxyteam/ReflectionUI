package xy.reflect.ui.control.swing.editor;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.customization.SwingCustomizer;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractEditFormBuilder {

	protected Object initialObjectValue;
	protected boolean objectValueInitialized = false;
	protected boolean objectValueReplaced = false;
	protected Accessor<Object> encapsulatedObjectValueAccessor;

	public abstract SwingRenderer getSwingRenderer();

	public abstract ModificationStack getParentObjectModificationStack();

	public abstract IInfo getCumulatedModificationsTarget();

	public abstract String getCumulatedModificationsTitle();

	public abstract boolean canCommit();

	public abstract IModification createCommitModification(Object newObjectValue);

	public abstract ITypeInfo getObjectDeclaredType();

	public abstract ValueReturnMode getObjectValueReturnMode();

	public abstract boolean isObjectValueNullable();

	public abstract Object getInitialObjectValue();

	public abstract IInfoFilter getObjectFormFilter();

	public abstract boolean isObjectFormExpanded();

	public abstract String getContextIdentifier();

	public abstract String getSubContextIdentifier();

	protected void ensureObjectValueIsInitialized() {
		if (objectValueInitialized) {
			return;
		}
		encapsulatedObjectValueAccessor = new Accessor<Object>() {

			Object object = initialObjectValue = getInitialObjectValue();

			@Override
			public Object get() {
				return object;
			}

			@Override
			public void set(Object t) {
				object = t;
				objectValueReplaced = true;
			}

		};
		objectValueInitialized = true;
	}

	public boolean isObjectValueInitialized() {
		return objectValueInitialized;
	}

	public boolean isObjectValueReplaced() {
		return objectValueReplaced;
	}

	public Object getCurrentObjectValue() {
		ensureObjectValueIsInitialized();
		return encapsulatedObjectValueAccessor.get();
	}

	public Object getEncapsulatedObject() {
		ensureObjectValueIsInitialized();
		return getEncapsulation().getInstance(encapsulatedObjectValueAccessor);
	}

	public boolean canReplaceObjectValue() {
		return canCommit();
	}

	protected String getCustomEncapsulationFieldName() {
		return null;
	}

	public String getEncapsulationTypeName() {
		String contextDeclaraion;
		{
			String contextIdentifier = getContextIdentifier();
			if (contextIdentifier == null) {
				contextDeclaraion = "";
			} else {
				contextDeclaraion = "context=" + contextIdentifier + ", ";
			}
		}
		String subContextDeclaraion;
		{
			String subContextIdentifier = getSubContextIdentifier();
			if (subContextIdentifier == null) {
				subContextDeclaraion = "";
			} else {
				subContextDeclaraion = "subContext=" + subContextIdentifier + ", ";
			}
		}
		return "Encapsulation [" + contextDeclaraion + subContextDeclaraion + "encapsulatedObjectType="
				+ getEncapsulationFieldType().getName() + "]";
	}

	public EncapsulatedObjectFactory getEncapsulation() {
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(getSwingRenderer().getReflectionUI(),
				getEncapsulationTypeName(), getEncapsulationFieldType());
		result.setTypeModificationStackAccessible(!isInReadOnlyMode());
		result.setTypeCaption(getEncapsulationTypeCaption());
		result.setFieldCaption(getEncapsulatedFieldCaption());
		Map<String, Object> typeSpecificProperties = new HashMap<String, Object>();
		{
			typeSpecificProperties.put(SwingCustomizer.CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY,
					!isEncapsulationTypeCustomizationAllowed());
			result.setTypeSpecificProperties(typeSpecificProperties);
		}
		String customEncapsulationFieldName = getCustomEncapsulationFieldName();
		{
			if (customEncapsulationFieldName != null) {
				result.setFieldName(customEncapsulationFieldName);
			}
		}
		result.setFieldGetOnly(hasParentObject() ? !canReplaceObjectValue() : false);
		result.setFieldNullable(isObjectValueNullable());
		result.setFieldValueReturnMode(
				hasParentObject() ? getObjectValueReturnMode() : ValueReturnMode.DIRECT_OR_PROXY);
		Map<String, Object> fieldSpecificProperties = new HashMap<String, Object>();
		{
			DesktopSpecificProperty.setSubFormExpanded(fieldSpecificProperties, isObjectFormExpanded());
			DesktopSpecificProperty.setFilter(fieldSpecificProperties, getObjectFormFilter());
			DesktopSpecificProperty.setIconImage(fieldSpecificProperties, SwingRendererUtils
					.findIconImage(getSwingRenderer(), result.getFieldType().getSpecificProperties()));
			result.setFieldSpecificProperties(fieldSpecificProperties);
		}
		return result;
	}

	protected boolean isEncapsulationTypeCustomizationAllowed() {
		return true;
	}

	public String getEncapsulatedFieldCaption() {
		return "";
	}

	public ITypeInfo getEncapsulationFieldType() {
		ITypeInfo result = getObjectDeclaredType();
		if (result != null) {
			return result;
		}
		ensureObjectValueIsInitialized();
		if (initialObjectValue != null) {
			return getSwingRenderer().getReflectionUI()
					.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(initialObjectValue));
		}
		return getSwingRenderer().getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Object.class));
	}

	public String getEncapsulationTypeCaption() {
		return getEncapsulationFieldType().getCaption();
	}

	public boolean isObjectFormEmpty() {
		Object encapsulatedObject = getEncapsulatedObject();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsulatedObject));
		IFieldInfo encapsulatedObjectField = encapsulatedObjectType.getFields().get(0);
		if (encapsulatedObjectField.isNullable()) {
			return false;
		}
		Object object = getCurrentObjectValue();
		if (object == null) {
			getSwingRenderer().getReflectionUI()
					.logError("Invalid value: <null> retrieved from a non-nullable field: " + encapsulatedObjectField);
			return false;
		}
		ITypeInfo actualObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(object));
		if (!SwingRendererUtils.isFormEmpty(actualObjectType, getObjectFormFilter(), getSwingRenderer())) {
			return false;
		}
		return true;
	}

	public JPanel createForm(boolean realTimeLinkWithParent) {
		Object encapsulated = getEncapsulatedObject();
		JPanel result = getSwingRenderer().createForm(encapsulated);
		if (realTimeLinkWithParent) {
			if (canPotentiallyModifyParentObject()) {
				forwardEditFormModificationsToParentObject(result);
			}
			if (isInReadOnlyMode()) {
				refreshEditFormOnModification(result);
			}
		}
		return result;
	}

	protected boolean hasParentObject() {
		return getParentObjectModificationStack() != null;
	}

	public boolean canPotentiallyModifyParentObject() {
		if (!hasParentObject()) {
			return false;
		}
		ensureObjectValueIsInitialized();
		return ReflectionUIUtils.canPotentiallyIntegrateSubModifications(getSwingRenderer().getReflectionUI(),
				initialObjectValue, getObjectValueReturnMode(), canCommit());
	}

	public boolean isInReadOnlyMode() {
		if (hasParentObject()) {
			if (!canPotentiallyModifyParentObject()) {
				return true;
			}
		}
		return false;
	}

	protected void refreshEditFormOnModification(final JPanel form) {
		ModificationStack childModificationStack = getSwingRenderer().getModificationStackByForm().get(form);
		childModificationStack.addListener(new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				refreshEditForm(form);
			}
		});
	}

	public void refreshEditForm(JPanel form) {
		encapsulatedObjectValueAccessor.set(getInitialObjectValue());
		getSwingRenderer().refreshAllFieldControls(form, false);
	}

	protected boolean shouldAcceptNewObjectValue(Object value) {
		return true;
	}

	protected void forwardEditFormModificationsToParentObject(final JPanel form) {
		Accessor<Boolean> childModifAcceptedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return shouldAcceptNewObjectValue(getCurrentObjectValue());
			}
		};
		Accessor<ValueReturnMode> childValueReturnModeGetter = new Accessor<ValueReturnMode>() {
			@Override
			public ValueReturnMode get() {
				return getObjectValueReturnMode();
			}
		};
		Accessor<Boolean> childValueReplacedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return (isObjectValueReplaced());
			}
		};
		Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
			@Override
			public IModification get() {
				if (!canCommit()) {
					return null;
				}
				return createCommitModification(getCurrentObjectValue());
			}
		};
		Accessor<IInfo> childModifTargetGetter = new Accessor<IInfo>() {
			@Override
			public IInfo get() {
				return getCumulatedModificationsTarget();
			}
		};
		Accessor<String> childModifTitleGetter = new Accessor<String>() {
			@Override
			public String get() {
				return getCumulatedModificationsTitle();
			}
		};
		Accessor<ModificationStack> parentModifStackGetter = new Accessor<ModificationStack>() {

			@Override
			public ModificationStack get() {
				ModificationStack result = getParentObjectModificationStack();
				if (result == null) {
					throw new ReflectionUIError();
				}
				return result;
			}
		};
		SwingRendererUtils.forwardSubModifications(getSwingRenderer(), form, childModifAcceptedGetter,
				childValueReturnModeGetter, childValueReplacedGetter, commitModifGetter, childModifTargetGetter,
				childModifTitleGetter, parentModifStackGetter);
	}
}