package xy.reflect.ui.control.swing;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

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
	protected Accessor<Object> encapsulatedObjectValueAccessor = new Accessor<Object>() {

		Object object;

		@Override
		public Object get() {
			ensureObjectValueIsInitialized();
			return object;
		}

		@Override
		public void set(Object t) {
			object = t;
		}

	};

	public abstract SwingRenderer getSwingRenderer();

	public abstract String getEditorTitle();

	public abstract ModificationStack getParentModificationStack();

	public abstract IInfo getCumulatedModificationsTarget();

	public abstract String getCumulatedModificationsTitle();

	public abstract boolean canCommit();

	public abstract IModification createCommitModification(Object newObjectValue);

	public abstract ITypeInfo getObjectDeclaredType();

	public abstract ValueReturnMode getObjectValueReturnMode();

	public abstract boolean isObjectNullable();

	public abstract Object getInitialObjectValue();

	public abstract IInfoFilter getObjectFormFilter();

	public abstract boolean isObjectFormExpanded();

	protected void ensureObjectValueIsInitialized() {
		if (!objectValueInitialized) {
			encapsulatedObjectValueAccessor.set(initialObjectValue = getInitialObjectValue());
			objectValueInitialized = true;
		}

	}

	public Object getCurrentObjectValue() {
		return encapsulatedObjectValueAccessor.get();
	}

	public boolean isCurrentObjectValueNew() {
		return initialObjectValue != encapsulatedObjectValueAccessor.get();
	}

	public Object getEncapsulatedObject() {
		return getEncapsulation().getInstance(encapsulatedObjectValueAccessor);
	}

	public EncapsulatedObjectFactory getEncapsulation() {
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(getSwingRenderer().getReflectionUI(),
				getEncapsulatedFieldType());
		result.setTypeCaption(getEncapsulationTypeCaption());
		result.setTypeModificationStackAccessible(canPotentiallyModifyParent());
		result.setFieldCaption(getEncapsulatedFieldCaption());
		result.setFieldGetOnly(!canCommit());
		result.setFieldNullable(isObjectNullable());
		result.setFieldValueReturnMode(
				canPotentiallyModifyParent() ? ValueReturnMode.DIRECT_OR_PROXY : ValueReturnMode.CALCULATED);
		Map<String, Object> properties = new HashMap<String, Object>();
		{
			DesktopSpecificProperty.setSubFormExpanded(properties, isObjectFormExpanded());
			DesktopSpecificProperty.setFilter(properties, getObjectFormFilter());
			DesktopSpecificProperty.setIconImage(properties, SwingRendererUtils.findIconImage(getSwingRenderer(),
					result.getFieldType().getSpecificProperties()));
			result.setFieldSpecificProperties(properties);
		}
		return result;
	}

	public String getEncapsulatedFieldCaption() {
		return "";
	}

	public ITypeInfo getEncapsulatedFieldType() {
		ITypeInfo result = getObjectDeclaredType();
		if (result != null) {
			return result;
		}
		Object object = getCurrentObjectValue();
		if (object != null) {
			return getSwingRenderer().getReflectionUI()
					.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(object));
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
			throw new ReflectionUIError();
		}
		ITypeInfo actualObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(object));
		if (!SwingRendererUtils.isFormEmpty(actualObjectType, getObjectFormFilter(), getSwingRenderer())) {
			return false;
		}
		return true;
	}

	public JPanel createEditorPanel() {
		Object encapsulated = getEncapsulatedObject();
		final JPanel result = getSwingRenderer().createForm(encapsulated);
		if (canPotentiallyModifyParent()) {
			forwardEditorPanelModificationsToParent(result);
		} else {
			refreshEditorPanelOnModification(result);
		}
		return result;
	}

	public boolean canPotentiallyModifyParent() {
		ensureObjectValueIsInitialized();
		return ReflectionUIUtils.canPotentiallyIntegrateSubModifications(getSwingRenderer().getReflectionUI(),
				initialObjectValue, getObjectValueReturnMode(), canCommit());
	}

	protected void refreshEditorPanelOnModification(final JPanel panel) {
		ModificationStack childModificationStack = getSwingRenderer().getModificationStackByForm().get(panel);
		childModificationStack.addListener(new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				encapsulatedObjectValueAccessor.set(getInitialObjectValue());
				getSwingRenderer().refreshAllFieldControls(panel, false);
			}
		});
	}

	protected void forwardEditorPanelModificationsToParent(final JPanel panel) {
		Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
		Accessor<ValueReturnMode> childValueReturnModeGetter = new Accessor<ValueReturnMode>() {
			@Override
			public ValueReturnMode get() {
				return getObjectValueReturnMode();
			}
		};
		Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
			@Override
			public IModification get() {
				return createCommitModification(encapsulatedObjectValueAccessor.get());
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

			ModificationStack dummyParentModifStack = new ModificationStack("Do not forward modifications");

			@Override
			public ModificationStack get() {
				ModificationStack result = getParentModificationStack();
				if (result == null) {
					result = dummyParentModifStack;
				}
				return result;
			}
		};
		SwingRendererUtils.forwardSubModifications(getSwingRenderer(), panel, childModifAcceptedGetter,
				childValueReturnModeGetter, commitModifGetter, childModifTargetGetter, childModifTitleGetter,
				parentModifStackGetter);
	}

}