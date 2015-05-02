package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.info.type.util.TypeInfoProxyConfiguration;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.undo.SetFieldValueModification;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class EmbeddedFormControl extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected Component textControl;
	protected Component iconControl;
	protected JButton button;

	public EmbeddedFormControl(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = forwardSubFieldUpdates(field);
		setLayout(new BorderLayout());
		JPanel subForm = createSubForm();
		add(subForm, BorderLayout.CENTER);
	}

	protected JPanel createSubForm() {
		Object fieldValue = field.getValue(object);
		JPanel subForm = reflectionUI.createObjectForm(fieldValue);
		return subForm;
	}

	protected IFieldInfo forwardSubFieldUpdates(final IFieldInfo field) {
		return new FieldInfoProxy(field) {

			@Override
			public Object getValue(final Object object) {
				Object fieldValue = super.getValue(object);
				ITypeInfo fieldValueType = reflectionUI
						.getTypeInfo(reflectionUI.getTypeInfoSource(fieldValue));
				fieldValueType = new TypeInfoProxyConfiguration() {
					@Override
					protected void setValue(Object subObject,
							Object subFieldValue, IFieldInfo subField,
							ITypeInfo containingType) {
						ModificationStack parentModifStack = ReflectionUIUtils
								.findModificationStack(
										EmbeddedFormControl.this, reflectionUI);
						if (parentModifStack != null) {
							parentModifStack.beginComposite();
						}
						try {
							if (parentModifStack != null) {
								parentModifStack
										.apply(new SetFieldValueModification(
												reflectionUI, subObject,
												subField, subFieldValue), false);
							} else {
								super.setValue(subObject, subFieldValue,
										subField, containingType);
							}
							field.setValue(object, subObject);
						} finally {
							if (parentModifStack != null) {
								String modifTitle = "Edit '"
										+ reflectionUI.composeTitle(
												field.getCaption(),
												subField.getCaption()) + "'";
								parentModifStack.endComposite(modifTitle,
										UndoOrder.FIFO);
							}
						}
					}
				}.get(fieldValueType);
				return new PrecomputedTypeInfoInstanceWrapper(fieldValue,
						fieldValueType);
			}

			@Override
			public void setValue(Object object, Object value) {
				value = ((PrecomputedTypeInfoInstanceWrapper)value).getInstance();
				super.setValue(object, value);
			}
		};
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
		return false;
	}

}
