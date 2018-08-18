package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ControlPanel;

public class EmbeddedFormControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected Component textControl;
	protected Component iconControl;
	protected JButton button;
	protected Object subFormObject;
	protected Form subForm;
	protected IFieldControlInput input;

	public EmbeddedFormControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = retrieveData();
		setLayout(new BorderLayout());
		refreshUI(true);
	}

	protected IFieldControlData retrieveData() {
		return input.getControlData();
	}

	public Form getSubForm() {
		return subForm;
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(subForm, swingRenderer);
	}

	protected void forwardSubFormModifications() {
		if (!ReflectionUIUtils.canEditSeparateObjectValue(
				ReflectionUIUtils.isValueImmutable(swingRenderer.getReflectionUI(), subFormObject),
				data.getValueReturnMode(), !data.isGetOnly())) {
			ModificationStack childModifStack = subForm.getModificationStack();
			childModifStack.addListener(new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					refreshUI(false);
				}
			});
		} else {
			Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
			Accessor<ValueReturnMode> childValueReturnModeGetter = Accessor.returning(data.getValueReturnMode());
			Accessor<Boolean> childValueReplacedGetter = Accessor.returning(Boolean.FALSE);
			Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
				@Override
				public IModification get() {
					if (data.isGetOnly()) {
						return null;
					}
					return new ControlDataValueModification(data, subFormObject, input.getModificationsTarget());
				}
			};
			Accessor<IInfo> childModifTargetGetter = new Accessor<IInfo>() {
				@Override
				public IInfo get() {
					return input.getModificationsTarget();
				}
			};
			Accessor<String> childModifTitleGetter = new Accessor<String>() {
				@Override
				public String get() {
					return ControlDataValueModification.getTitle(input.getModificationsTarget());
				}
			};
			Accessor<ModificationStack> masterModifStackGetter = new Accessor<ModificationStack>() {
				@Override
				public ModificationStack get() {
					return input.getModificationStack();
				}
			};
			boolean exclusiveLinkWithParent = Boolean.TRUE.equals(input.getControlData().getSpecificProperties()
					.get(EncapsulatedObjectFactory.IS_ENCAPSULATION_FIELD_PROPERTY_KEY));
			subForm.setModificationStack(new SlaveModificationStack(swingRenderer, subForm, childModifAcceptedGetter,
					childValueReturnModeGetter, childValueReplacedGetter, commitModifGetter, childModifTargetGetter,
					childModifTitleGetter, masterModifStackGetter, exclusiveLinkWithParent));
		}
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			if (data.getCaption().length() > 0) {
				setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
				if (data.getFormForegroundColor() != null) {
					((TitledBorder) getBorder()).setTitleColor(SwingRendererUtils.getColor(data.getFormForegroundColor()));
				}
			} else {
				setBorder(null);
			}
		}
		if (subForm == null) {
			subFormObject = data.getValue();
			if (subFormObject == null) {
				throw new ReflectionUIError();
			}
			IInfoFilter filter = data.getFormControlFilter();
			subForm = swingRenderer.createForm(subFormObject, filter);
			add(subForm, BorderLayout.CENTER);
			forwardSubFormModifications();
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			final Object newSubFormObject = data.getValue();
			if (newSubFormObject == null) {
				throw new ReflectionUIError();
			}
			if (newSubFormObject == subFormObject) {
				subForm.refreshForm(refreshStructure);
			} else {
				final ITypeInfo subFormObjectType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
				final ITypeInfo newSubFormObjectType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newSubFormObject));
				if (subFormObjectType.equals(newSubFormObjectType)) {
					subForm.setObject(newSubFormObject);
					swingRenderer.showBusyDialogWhile(this, new Runnable() {
						@Override
						public void run() {
							subFormObjectType.onFormVisibilityChange(subFormObject, false);
							newSubFormObjectType.onFormVisibilityChange(newSubFormObject, true);
						}
					}, "Refreshing " + swingRenderer.getObjectTitle(newSubFormObject) + "...");
					subFormObject = newSubFormObject;
					subForm.refreshForm(refreshStructure);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean handlesModificationStackAndStress() {
		return true;
	}

	@Override
	public void validateSubForm() throws Exception {
		subForm.validateForm();
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
		subForm.addMenuContribution(menuModel);
	}

	@Override
	public String toString() {
		return "EmbeddedFormControl [data=" + data + "]";
	}

}
