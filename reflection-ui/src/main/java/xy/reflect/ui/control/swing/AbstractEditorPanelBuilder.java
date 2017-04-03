package xy.reflect.ui.control.swing;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

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

public abstract class AbstractEditorPanelBuilder {

	protected Object initialObjectValue;
	protected boolean objectValueInitialized = false;
	protected boolean objectValueReplaced = false;
	protected Accessor<Object> encapsulatedObjectValueAccessor;

	public abstract SwingRenderer getSwingRenderer();

	public abstract String getEditorTitle();

	public abstract ModificationStack getParentModificationStack();

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

	protected String getCustomEncapsulationTypeName() {
		return null;
	}

	protected String getCustomEncapsulatedFieldName() {
		return null;
	}

	public EncapsulatedObjectFactory getEncapsulation() {
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(getSwingRenderer().getReflectionUI(),
				getEncapsulatedFieldType(), getEncapsulationTypeCaption(), getEncapsulatedFieldCaption());
		String customEncapsulationTypeName = getCustomEncapsulationTypeName();
		{
			if (customEncapsulationTypeName != null) {
				result.setTypeName(customEncapsulationTypeName);
			}
		}
		result.setTypeModificationStackAccessible(!isInReadOnlyMode());
		Map<String, Object> typeSpecificProperties = new HashMap<String, Object>();
		{
			typeSpecificProperties.put(SwingCustomizer.CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY,
					!isEncapsulationTypeCustomizationAllowed());
			result.setTypeSpecificProperties(typeSpecificProperties);
		}
		String customEncapsulatedFieldName = getCustomEncapsulatedFieldName();
		{
			if (customEncapsulatedFieldName != null) {
				result.setFieldName(customEncapsulatedFieldName);
			}
		}
		result.setFieldGetOnly(isInReadOnlyMode() || !canReplaceObjectValue());
		result.setFieldNullable(isObjectValueNullable());
		result.setFieldValueReturnMode(
				(!isInReadOnlyMode()) ? ValueReturnMode.DIRECT_OR_PROXY : ValueReturnMode.CALCULATED);
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
		return !isObjectFormExpanded() || isObjectValueNullable() || (getEncapsulatedFieldCaption().length() > 0);
	}

	public boolean isInReadOnlyMode() {
		if (isLinkedToParent()) {
			if (!canPotentiallyModifyParent()) {
				return true;
			}
		}
		return false;
	}

	public String getEncapsulatedFieldCaption() {
		return "";
	}

	public ITypeInfo getEncapsulatedFieldType() {
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
		return getEncapsulatedFieldType().getCaption();
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

	public JPanel createEditorPanel(boolean realTimeLinkWithParent) {
		Object encapsulated = getEncapsulatedObject();
		JPanel result = getSwingRenderer().createForm(encapsulated);
		if (realTimeLinkWithParent) {
			if (canPotentiallyModifyParent()) {
				forwardEditorPanelModificationsToParent(result);
			}
			if (isInReadOnlyMode()) {
				refreshEditorPanelOnModification(result);
			}
		}
		return result;
	}

	public boolean canPotentiallyModifyParent() {
		if (!isLinkedToParent()) {
			return false;
		}
		ensureObjectValueIsInitialized();
		return ReflectionUIUtils.canPotentiallyIntegrateSubModifications(getSwingRenderer().getReflectionUI(),
				initialObjectValue, getObjectValueReturnMode(), canCommit());
	}

	protected boolean isLinkedToParent() {
		return getParentModificationStack() != null;
	}

	protected void refreshEditorPanelOnModification(final JPanel panel) {
		ModificationStack childModificationStack = getSwingRenderer().getModificationStackByForm().get(panel);
		childModificationStack.addListener(new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				refreshEditorPanel(panel);
			}
		});
	}

	public void refreshEditorPanel(JPanel panel) {
		encapsulatedObjectValueAccessor.set(getInitialObjectValue());
		getSwingRenderer().refreshAllFieldControls(panel, false);
	}

	protected boolean isNewObjectValueAccepted(Object value) {
		return true;
	}

	protected void forwardEditorPanelModificationsToParent(final JPanel panel) {
		Accessor<Boolean> childModifAcceptedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return isNewObjectValueAccepted(getCurrentObjectValue());
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
				ModificationStack result = getParentModificationStack();
				if (result == null) {
					throw new ReflectionUIError();
				}
				return result;
			}
		};
		SwingRendererUtils.forwardSubModifications(getSwingRenderer(), panel, childModifAcceptedGetter,
				childValueReturnModeGetter, childValueReplacedGetter, commitModifGetter, childModifTargetGetter,
				childModifTitleGetter, parentModifStackGetter);
	}
}