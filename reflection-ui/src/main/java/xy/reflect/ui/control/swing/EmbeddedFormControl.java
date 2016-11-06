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

import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SetFieldValueModification;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class EmbeddedFormControl extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IFieldInfo field;

	protected Component textControl;
	protected Component iconControl;
	protected JButton button;
	protected Object subFormObject;
	protected JPanel subForm;

	public EmbeddedFormControl(final SwingRenderer swingRenderer, final Object object, final IFieldInfo field) {
		this.swingRenderer = swingRenderer;
		this.object = object;
		this.field = field;
		setLayout(new BorderLayout());
		refreshUI();
	}

	public JPanel getSubForm() {
		return subForm;
	}

	@Override
	public Object getFocusDetails() {
		int focusedFieldControlIndex = swingRenderer.getFocusedFieldControlPaceHolderIndex(subForm);
		if (focusedFieldControlIndex == -1) {
			return null;
		}
		Object focusedFieldControlDetails = null;
		Class<?> focusedFieldControlClass = null;
		{
			Component focusedFieldControl = swingRenderer.getAllFieldControlPlaceHolders(subForm)
					.get(focusedFieldControlIndex).getFieldControl();
			if (focusedFieldControl instanceof IFieldControl) {
				focusedFieldControlDetails = ((IFieldControl) focusedFieldControl).getFocusDetails();
			}
		}
		ITypeInfo subFormObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("subFormObjectType", subFormObjectType);
		result.put("focusedFieldControlIndex", focusedFieldControlIndex);
		result.put("focusedFieldControlDetails", focusedFieldControlDetails);
		result.put("focusedFieldControlClass", focusedFieldControlClass);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		ITypeInfo subFormObjectType = (ITypeInfo) focusDetails.get("subFormObjectType");
		int focusedFieldControlIndex = (Integer) focusDetails.get("focusedFieldControlIndex");
		Object focusedFieldControlDetails = focusDetails.get("focusedFieldControlDetails");
		Class<?> focusedFieldControlClass = (Class<?>) focusDetails.get("focusedFieldControlClass");
		ITypeInfo currentSubFormObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
		if (subFormObjectType.equals(currentSubFormObjectType)) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = swingRenderer
					.getAllFieldControlPlaceHolders(subForm);
			FieldControlPlaceHolder fieldControlPlaceHolder = fieldControlPlaceHolders.get(focusedFieldControlIndex);
			fieldControlPlaceHolder.requestFocus();
			if (focusedFieldControlDetails != null) {
				Component fieldControl = fieldControlPlaceHolder.getFieldControl();
				if (fieldControl.getClass().equals(focusedFieldControlClass)) {
					((IFieldControl) fieldControl).requestDetailedFocus(focusedFieldControlDetails);
				}
			}
		}
	}

	@Override
	public void requestFocus() {
		if (subForm != null) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = swingRenderer
					.getAllFieldControlPlaceHolders(subForm);
			if (fieldControlPlaceHolders.size() > 0) {
				fieldControlPlaceHolders.get(0).requestFocus();
			}
		}
	}

	protected void forwardSubFormModifications() {
		if (field.isGetOnly() && (field.getValueReturnMode() == ValueReturnMode.COPY)) {
			ModificationStack childModifStack = swingRenderer.getModificationStackByForm().get(subForm);
			childModifStack.addListener(new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					refreshUI();
				}
			});
		} else {
			Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
			Accessor<ValueReturnMode> childValueReturnModeGetter = Accessor.returning(field.getValueReturnMode());
			Accessor<Boolean> childValueNewGetter =  Accessor.returning(Boolean.FALSE);
			Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
				@Override
				public IModification get() {
					if (field.isGetOnly()) {
						return null;
					}
					return SetFieldValueModification.create(swingRenderer.getReflectionUI(), object, field, 
							subFormObject);
				}
			};
			ReflectionUIUtils.forwardSubModifications(subForm, childModifAcceptedGetter, childValueReturnModeGetter,
					childValueNewGetter, commitModifGetter, field, SetFieldValueModification.getTitle(field),
					swingRenderer);
		}
	}

	@Override
	public boolean showCaption() {
		setBorder(BorderFactory.createTitledBorder(field.getCaption()));
		return true;
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		return false;
	}

	@Override
	public boolean refreshUI() {
		if (subForm == null) {
			subFormObject = field.getValue(object);
			subForm = swingRenderer.createObjectForm(subFormObject, IInfoCollectionSettings.DEFAULT);
			add(subForm, BorderLayout.CENTER);
			forwardSubFormModifications();
			swingRenderer.handleComponentSizeChange(this);
		} else {
			Object newSubFormObject = field.getValue(object);
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

}
