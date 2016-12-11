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
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
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

	public EmbeddedFormControl(final SwingRenderer swingRenderer, final IControlData data) {
		this.swingRenderer = swingRenderer;
		this.data = data;
		setLayout(new BorderLayout());
		refreshUI();
	}

	public JPanel getSubForm() {
		return subForm;
	}

	@Override
	public Object getFocusDetails() {
		ITypeInfo subFormObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
		Object subFormFocusDetails = swingRenderer.getFormFocusDetails(subForm);
		if(subFormFocusDetails == null){
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
			final IFieldInfo field = SwingRendererUtils.getControlFormAwareField(EmbeddedFormControl.this);
			Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
				@Override
				public IModification get() {
					if (data.isGetOnly()) {
						return null;
					}
					return new ControlDataValueModification(data, subFormObject, field);
				}
			};
			Accessor<IInfo> childModifTargetGetter = new Accessor<IInfo>() {
				@Override
				public IInfo get() {
					return SwingRendererUtils.getControlFormAwareField(EmbeddedFormControl.this);
				}
			};
			Accessor<String> childModifTitleGetter = new Accessor<String>() {
				@Override
				public String get() {
					return ControlDataValueModification
							.getTitle(SwingRendererUtils.getControlFormAwareField(EmbeddedFormControl.this));
				}
			};
			Accessor<ModificationStack> parentModifStackGetter = new Accessor<ModificationStack>() {
				@Override
				public ModificationStack get() {
					return SwingRendererUtils.findParentFormModificationStack(EmbeddedFormControl.this, swingRenderer);
				}
			};
			SwingRendererUtils.forwardSubModifications(swingRenderer.getReflectionUI(), subForm,
					childModifAcceptedGetter, childValueReturnModeGetter, childValueNewGetter, commitModifGetter,
					childModifTargetGetter, childModifTitleGetter, parentModifStackGetter, swingRenderer);
		}
	}

	@Override
	public boolean showCaption(String caption) {
		setBorder(BorderFactory.createTitledBorder(caption));
		return true;
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		return false;
	}

	@Override
	public boolean refreshUI() {
		if (subForm == null) {
			subFormObject = data.getValue();
			subForm = swingRenderer.createForm(subFormObject);
			add(subForm, BorderLayout.CENTER);
			forwardSubFormModifications();
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			Object newSubFormObject = data.getValue();
			if (ReflectionUIUtils.equals(swingRenderer.getReflectionUI(), newSubFormObject, subFormObject)) {
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

}
