package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class EmbeddedFormControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IControlData data;

	protected Component textControl;
	protected Component iconControl;
	protected JButton button;
	protected Object subFormObject;
	protected JPanel subForm;
	protected FieldControlPlaceHolder fieldControlPlaceHolder;

	public EmbeddedFormControl(final SwingRenderer swingRenderer, final IControlData data) {
		this.swingRenderer = swingRenderer;
		this.data = data;
		setLayout(new BorderLayout());
		refreshUI();
	}

	@Override
	public void setPalceHolder(FieldControlPlaceHolder fieldControlPlaceHolder) {
		this.fieldControlPlaceHolder = fieldControlPlaceHolder;
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

	protected void forwardSubFormModifications() {
		if (data.isGetOnly() && (data.getValueReturnMode() == ValueReturnMode.COPY)) {
			ModificationStack childModifStack = swingRenderer.getModificationStackByForm().get(subForm);
			childModifStack.addListener(new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					refreshUI();
				}
			});
		} else {
			Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
			Accessor<ValueReturnMode> childValueReturnModeGetter = Accessor.returning(data.getValueReturnMode());
			Accessor<Boolean> childValueNewGetter = Accessor.returning(Boolean.FALSE);
			Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
				@Override
				public IModification get() {
					if (data.isGetOnly()) {
						return null;
					}
					return new ControlDataValueModification(data, subFormObject, getModifiedField());
				}
			};
			Accessor<IInfo> childModifTargetGetter = new Accessor<IInfo>() {
				@Override
				public IInfo get() {
					return getModifiedField();
				}
			};
			Accessor<String> childModifTitleGetter = new Accessor<String>() {
				@Override
				public String get() {
					return ControlDataValueModification.getTitle(getModifiedField());
				}
			};
			Accessor<ModificationStack> parentModifStackGetter = new Accessor<ModificationStack>() {
				@Override
				public ModificationStack get() {
					return ReflectionUIUtils.findParentFormModificationStack(fieldControlPlaceHolder, swingRenderer);
				}
			};
			SwingRendererUtils.forwardSubModifications(swingRenderer.getReflectionUI(), subForm,
					childModifAcceptedGetter, childValueReturnModeGetter, childValueNewGetter, commitModifGetter,
					childModifTargetGetter, childModifTitleGetter, parentModifStackGetter, swingRenderer);
		}
	}

	protected IFieldInfo getModifiedField() {
		return fieldControlPlaceHolder.getField();
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
			IInfoFilter filter = DesktopSpecificProperty.getFilter(data.getSpecificProperties());
			{
				if (filter == null) {
					filter = IInfoFilter.DEFAULT;
				}
			}
			subForm = swingRenderer.createForm(subFormObject, filter);
			add(subForm, BorderLayout.CENTER);
			forwardSubFormModifications();
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

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

	@Override
	public void validateSubForm() throws Exception {
		swingRenderer.validateForm(subForm);
	}

	@Override
	public ITypeInfo getDynamicObjectType() {
		return null;
	}
}
