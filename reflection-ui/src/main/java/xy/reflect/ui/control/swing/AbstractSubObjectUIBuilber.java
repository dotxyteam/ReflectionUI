package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
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

	protected abstract SwingRenderer getSwingRenderer();

	protected abstract Component getOwnerComponent();

	protected abstract String getSubObjectTitle(); 

	protected abstract ModificationStack getParentObjectModificationStack();

	protected abstract IInfo getSubObjectModificationTarget();

	protected abstract String getSubObjectModificationTitle();

	protected abstract IModification getUpdatedSubObjectCommitModification(Object newObjectValue);

	protected abstract ITypeInfo getSubObjectDeclaredType();

	protected abstract ValueReturnMode getSubObjectValueReturnMode();

	protected abstract boolean isSubObjectNullable();

	protected abstract Object getSubObject();

	protected abstract IInfoFilter getSubObjectFormFilter();

	protected Object initialSubObject = getSubObject();
	protected Object subObject = initialSubObject;
	protected boolean parentObjectModificationDetected;
	protected boolean dialogOKButtonPressed;

	protected boolean canPotentiallyModifyParentObject() {
		return ReflectionUIUtils.canPotentiallyIntegrateSubModifications(getSubObjectValueReturnMode(),
				canCommitUpdatedSubObject());
	}

	protected boolean canCommitUpdatedSubObject() {
		return getUpdatedSubObjectCommitModification(subObject) != null;
	}

	protected boolean isSubObjectNew() {
		return initialSubObject != subObject;
	}

	private Object getEncapsulatedSubObject() {
		return getSubObjectEncapsulation().getInstance(new Accessor<Object>() {

			boolean firstAccess = true;

			@Override
			public Object get() {
				if (firstAccess) {
					firstAccess = false;
				} else {
					if (!canPotentiallyModifyParentObject()) {
						subObject = getSubObject();
					}
				}
				return subObject;
			}

			@Override
			public void set(Object t) {
				subObject = t;
			}

		});
	}

	protected EncapsulatedObjectFactory getSubObjectEncapsulation() {
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(getSwingRenderer().getReflectionUI(),
				getSubObjectDeclaredType());
		result.setTypeCaption(getSubObjectTitle());
		result.setTypeModificationStackAccessible(canPotentiallyModifyParentObject());
		result.setFieldCaption("");
		result.setFieldGetOnly(!canPotentiallyModifyParentObject());
		result.setFieldNullable(isSubObjectNullable());
		result.setFieldValueReturnMode(
				canPotentiallyModifyParentObject() ? ValueReturnMode.SELF : ValueReturnMode.COPY);
		Map<String, Object> properties = new HashMap<String, Object>();
		{
			DesktopSpecificProperty.setSubFormExpanded(properties, true);
			DesktopSpecificProperty.setFilter(properties, getSubObjectFormFilter());
			result.setFieldSpecificProperties(properties);
		}
		return result;
	}

	public void showDialog() {
		ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(getSwingRenderer(), getOwnerComponent(),
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
			commitModif = getUpdatedSubObjectCommitModification(subObject);
		}
		boolean childModifAccepted = (!dialogBuilder.isCancellable()) || dialogBuilder.wasOkPressed();
		boolean childValueNew = isSubObjectNew();
		String childModifTitle = getSubObjectModificationTitle();
		parentObjectModificationDetected = ReflectionUIUtils.integrateSubModifications(
				getSwingRenderer().getReflectionUI(), parentModifStack, childModifStack, childModifAccepted,
				childValueReturnMode, childValueNew, commitModif, childModifTarget, childModifTitle);
	}

	public JPanel createSubForm() {
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
					updateSubForm(subForm);
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
					return getUpdatedSubObjectCommitModification(subObject);
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

	protected void updateSubForm(JPanel subForm) {
		getSwingRenderer().refreshAllFieldControls(subForm, false);

	}

	public boolean isParentObjectModificationDetected() {
		return parentObjectModificationDetected;
	}

}
