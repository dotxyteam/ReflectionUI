package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
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
	protected boolean forwardUpdatesToParentFormCOnfigured = false;

	public EmbeddedFormControl(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		setLayout(new BorderLayout());
		final JPanel subForm = createSubForm();
		add(subForm, BorderLayout.CENTER);
		addAncestorListener(new AncestorListener() {
			
			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}
			
			@Override
			public void ancestorMoved(AncestorEvent event) {
			}
			
			@Override
			public void ancestorAdded(AncestorEvent event) {
				if(forwardUpdatesToParentFormCOnfigured){
					return;
				}
				forwardUpdatesToParentForm(subForm);
				forwardUpdatesToParentFormCOnfigured  = true;	
			}
		});
	}

	protected void forwardUpdatesToParentForm(JPanel subForm) {
		final ModificationStack parentModifStack = ReflectionUIUtils
				.findModificationStack(EmbeddedFormControl.this, reflectionUI);
		reflectionUI.getModificationStackByForm().put(subForm,
				new ModificationStack(null) {

					@Override
					public void apply(IModification modif, boolean refreshView) {
						parentModifStack.apply(modif, refreshView);
					}

					@Override
					public void pushUndo(IModification undoModif) {
						parentModifStack.pushUndo(undoModif);
					}

					@Override
					public void beginComposite() {
						parentModifStack.beginComposite();
					}

					@Override
					public void endComposite(String title, UndoOrder order) {
						parentModifStack.endComposite(title, order);
					}

					@Override
					public void invalidate() {
						parentModifStack.invalidate();
					}

				});
	}

	protected JPanel createSubForm() {
		Object fieldValue = field.getValue(object);
		JPanel subForm = reflectionUI.createObjectForm(fieldValue);
		return subForm;
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
