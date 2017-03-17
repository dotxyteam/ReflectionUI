package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import xy.reflect.ui.control.input.IControlData;
import xy.reflect.ui.control.input.IControlInput;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.util.SwingRendererUtils;

public class EmbeddedFormControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IControlData data;

	protected Component textControl;
	protected Component iconControl;
	protected JButton button;
	protected Object subFormObject;
	protected JPanel subForm;
	protected IControlInput input;

	public EmbeddedFormControl(final SwingRenderer swingRenderer, IControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = retrieveData();
		setLayout(new BorderLayout());
		refreshUI();
	}

	protected IControlData retrieveData() {
		return input.getControlData();
	}

	public JPanel getSubForm() {
		return subForm;
	}

	@Override
	public Object getFocusDetails() {
		ITypeInfo subFormObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
		Object subFormFocusDetails = swingRenderer.getFormFocusDetails(subForm);
		if (subFormFocusDetails == null) {
			return null;
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("subFormObjectType", subFormObjectType);
		result.put("subFormFocusDetails", subFormFocusDetails);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		ITypeInfo subFormObjectType = (ITypeInfo) focusDetails.get("subFormObjectType");
		Object subFormFocusDetails = focusDetails.get("subFormFocusDetails");

		ITypeInfo currentSubFormObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
		if (subFormObjectType.equals(currentSubFormObjectType)) {
			swingRenderer.setFormFocusDetails(subForm, subFormFocusDetails);
		}
	}

	@Override
	public void requestFocus() {
		if (subForm != null) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = swingRenderer.getFieldControlPlaceHolders(subForm);
			if (fieldControlPlaceHolders.size() > 0) {
				fieldControlPlaceHolders.get(0).requestFocus();
			}
		}
	}

	@Override
	public boolean showCaption() {
		setBorder(BorderFactory.createTitledBorder(data.getCaption()));
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean refreshUI() {
		if (subForm == null) {
			subFormObject = data.getValue();
			subForm = getSubFormBuilder().createSubForm();
			add(subForm, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			Object newSubFormObject = data.getValue();
			if (newSubFormObject == subFormObject) {
				swingRenderer.refreshAllFieldControls(subForm, false);
			} else {
				remove(subForm);
				subForm = null;
				refreshUI();
			}
		}
		return true;
	}

	protected AbstractSubObjectUIBuilber getSubFormBuilder() {
		return new AbstractSubObjectUIBuilber() {

			@Override
			protected boolean isSubObjectNullable() {
				return data.isNullable();
			}

			@Override
			protected IModification getUpdatedSubObjectCommitModification(Object newObjectValue) {
				return new ControlDataValueModification(data, newObjectValue, input.getModificationsTarget());
			}

			@Override
			protected SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			protected ValueReturnMode getSubObjectValueReturnMode() {
				return data.getValueReturnMode();
			}

			@Override
			protected String getSubObjectModificationTitle() {
				return ControlDataValueModification.getTitle(input.getModificationsTarget());
			}

			@Override
			protected IInfo getSubObjectModificationTarget() {
				return input.getModificationsTarget();
			}

			@Override
			protected ITypeInfo getSubObjectDeclaredType() {
				return data.getType();
			}

			@Override
			protected Object getSubObject() {
				return data.getValue();
			}

			@Override
			protected ModificationStack getParentObjectModificationStack() {
				return input.getModificationStack();
			}

			@Override
			protected Component getOwnerComponent() {
				return EmbeddedFormControl.this;
			}

			@Override
			protected String getSubObjectTitle() {
				return data.getType().getCaption();
			}

			@Override
			protected IInfoFilter getSubObjectFormFilter() {
				IInfoFilter result = DesktopSpecificProperty
						.getFilter(DesktopSpecificProperty.accessControlDataProperties(data));
				if (result == null) {
					result = IInfoFilter.NO_FILTER;
				}
				return result;
			}
		};
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

	@Override
	public void validateSubForm() throws Exception {
		swingRenderer.validateForm(subForm);
	}

}
