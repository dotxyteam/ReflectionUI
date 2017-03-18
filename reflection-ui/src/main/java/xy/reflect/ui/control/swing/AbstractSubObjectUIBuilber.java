package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractSubObjectUIBuilber {

	public abstract SwingRenderer getSwingRenderer();

	public abstract Component getSubObjectOwnerComponent();

	public abstract String getSubObjectTitle();

	public abstract ModificationStack getParentObjectModificationStack();

	public abstract IInfo getSubObjectModificationTarget();

	public abstract String getSubObjectModificationTitle();

	public abstract boolean canCommitUpdatedSubObject();

	public abstract IModification getUpdatedSubObjectCommitModification(Object newObjectValue);

	public abstract ITypeInfo getSubObjectDeclaredType();

	public abstract ValueReturnMode getSubObjectValueReturnMode();

	public abstract boolean isSubObjectNullable();

	public abstract Object getInitialSubObjectValue();

	public abstract IInfoFilter getSubObjectFormFilter();

	public abstract boolean isSubObjectFormExpanded();

	protected Object initialSubObject;
	protected boolean parentObjectModificationDetected;
	protected boolean dialogOKButtonPressed;
	protected Accessor<Object> encapsulatedSubObjectAccessor = new Accessor<Object>() {

		boolean firstAccess = true;
		Object subObject;

		@Override
		public Object get() {
			if (firstAccess) {
				subObject = initialSubObject = getInitialSubObjectValue();
				firstAccess = false;
			} else {
				if (!canPotentiallyModifyParentObject()) {
					subObject = getInitialSubObjectValue();
				}
			}
			return subObject;
		}

		@Override
		public void set(Object t) {
			subObject = t;
		}

	};

	public Object getSubObject() {
		return encapsulatedSubObjectAccessor.get();
	}

	public boolean canPotentiallyModifyParentObject() {
		return ReflectionUIUtils.canPotentiallyIntegrateSubModifications(getSubObjectValueReturnMode(),
				canCommitUpdatedSubObject());
	}

	public boolean isSubObjectNew() {
		return initialSubObject != encapsulatedSubObjectAccessor.get();
	}

	public Object getEncapsulatedSubObject() {
		return getSubObjectEncapsulation().getInstance(encapsulatedSubObjectAccessor);
	}

	public EncapsulatedObjectFactory getSubObjectEncapsulation() {
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(getSwingRenderer().getReflectionUI(),
				getSubObjectDeclaredType());
		result.setTypeCaption(getSubObjectTitle());
		result.setTypeModificationStackAccessible(canPotentiallyModifyParentObject());
		result.setFieldCaption("");
		result.setFieldGetOnly(!canPotentiallyModifyParentObject());
		result.setFieldNullable(isSubObjectNullable());
		result.setFieldValueReturnMode(
				canPotentiallyModifyParentObject() ? ValueReturnMode.SELF_OR_PROXY : ValueReturnMode.COPY);
		Map<String, Object> properties = new HashMap<String, Object>();
		{
			DesktopSpecificProperty.setSubFormExpanded(properties, isSubObjectFormExpanded());
			DesktopSpecificProperty.setFilter(properties, getSubObjectFormFilter());
			result.setFieldSpecificProperties(properties);
		}
		return result;
	}

	public boolean isSubObjectFormEmpty() {
		Object encapsulatedSubObject = getEncapsulatedSubObject();
		ITypeInfo encapsulatedSubObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsulatedSubObject));
		IFieldInfo encapsulatedSubObjectField = encapsulatedSubObjectType.getFields().get(0);
		if (encapsulatedSubObjectField.isNullable()) {
			return false;
		}
		Object subObject = getSubObject();
		if (subObject == null) {
			return false;
		}
		ITypeInfo actualSubObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(subObject));
		if (!SwingRendererUtils.isFormEmpty(actualSubObjectType, getSubObjectFormFilter(), getSwingRenderer())) {
			return false;
		}
		return true;
	}

	public void showSubObjectDialog() {
		ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(getSwingRenderer(), getSubObjectOwnerComponent(),
				getEncapsulatedSubObject());
		getSwingRenderer().showDialog(dialogBuilder.build(), true);
		dialogOKButtonPressed = dialogBuilder.wasOkPressed();
		if (canPotentiallyModifyParentObject()) {
			impactParentObjectModificationStack(dialogBuilder);
		}
	}

	public boolean wasDialogOKButtonPressed() {
		return dialogOKButtonPressed;
	}

	protected void impactParentObjectModificationStack(ObjectDialogBuilder dialogBuilder) {
		ModificationStack parentModifStack = getParentObjectModificationStack();
		ModificationStack childModifStack = dialogBuilder.getModificationStack();
		IInfo childModifTarget = getSubObjectModificationTarget();
		ValueReturnMode childValueReturnMode = getSubObjectValueReturnMode();
		IModification commitModif;
		if (!canCommitUpdatedSubObject()) {
			commitModif = null;
		} else {
			commitModif = getUpdatedSubObjectCommitModification(encapsulatedSubObjectAccessor.get());
		}
		boolean childModifAccepted = (!dialogBuilder.isCancellable()) || dialogBuilder.wasOkPressed();
		boolean childValueNew = isSubObjectNew();
		String childModifTitle = getSubObjectModificationTitle();
		parentObjectModificationDetected = ReflectionUIUtils.integrateSubModifications(
				getSwingRenderer().getReflectionUI(), parentModifStack, childModifStack, childModifAccepted,
				childValueReturnMode, childValueNew, commitModif, childModifTarget, childModifTitle);
	}

	public JPanel createSubObjectForm() {
		Object encapsulated = getEncapsulatedSubObject();
		JPanel result = getSwingRenderer().createForm(encapsulated);
		getSwingRenderer().getBusyIndicationDisabledByForm().put(result, true);
		getSwingRenderer().updateStatusBarInBackground(result);
		forwardSubFormModifications(result);
		return result;
	}

	protected void forwardSubFormModifications(final JPanel subForm) {
		if (!canPotentiallyModifyParentObject()) {
			ModificationStack childModificationStack = getSwingRenderer().getModificationStackByForm().get(subForm);
			childModificationStack.addListener(new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					getSwingRenderer().refreshAllFieldControls(subForm, false);
				}
			});
		} else {
			Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
			Accessor<ValueReturnMode> childValueReturnModeGetter = new Accessor<ValueReturnMode>() {
				@Override
				public ValueReturnMode get() {
					return getSubObjectValueReturnMode();
				}
			};
			Accessor<Boolean> childValueNewGetter = new Accessor<Boolean>() {
				@Override
				public Boolean get() {
					return isSubObjectNew();
				}
			};
			Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
				@Override
				public IModification get() {
					return getUpdatedSubObjectCommitModification(encapsulatedSubObjectAccessor.get());
				}
			};
			Accessor<IInfo> childModifTargetGetter = new Accessor<IInfo>() {
				@Override
				public IInfo get() {
					return getSubObjectModificationTarget();
				}
			};
			Accessor<String> childModifTitleGetter = new Accessor<String>() {
				@Override
				public String get() {
					return getSubObjectModificationTitle();
				}
			};
			Accessor<ModificationStack> parentModifStackGetter = new Accessor<ModificationStack>() {
				@Override
				public ModificationStack get() {
					return getParentObjectModificationStack();
				}
			};
			SwingRendererUtils.forwardSubModifications(getSwingRenderer(), subForm, childModifAcceptedGetter,
					childValueReturnModeGetter, childValueNewGetter, commitModifGetter, childModifTargetGetter,
					childModifTitleGetter, parentModifStackGetter);
		}
	}

	public boolean isParentObjectModificationDetected() {
		return parentObjectModificationDetected;
	}

}
