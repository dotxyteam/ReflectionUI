package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.DefaultFormatter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITextualTypeInfo;
import xy.reflect.ui.util.component.AlignedLabel;

public class TextControl extends JPanel implements IRefreshableControl,
		ICanShowCaptionControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected ITextualTypeInfo textType;
	protected JFormattedTextField textField;
	protected boolean textChangedByUser = true;

	public TextControl(final ReflectionUI reflectionUI, final Object object,
			final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.textType = (ITextualTypeInfo) field.getType();

		setLayout(new BorderLayout());

		DefaultFormatter formatter = new DefaultFormatter();
		formatter.setCommitsOnValidEdit(true);
		formatter.setOverwriteMode(false);
		textField = new JFormattedTextField(formatter){

			private static final long serialVersionUID = 1L;

			@Override
			protected void processFocusEvent(FocusEvent e) {
				textChangedByUser = false;
				super.processFocusEvent(e);
				textChangedByUser = true;
			}
			
		};
		
		add(textField, BorderLayout.CENTER);
		textField.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("value".equals(evt.getPropertyName())) {
					onTextChange();
				}
			}
		});
		refreshUI();
	}

	protected void onTextChange() {
		if (!textChangedByUser) {
			return;
		}
		field.setValue(object, textType.fromText(textField.getText()));
	}

	@Override
	public void requestFocus() {
		textField.requestFocus();
	}

	@Override
	public void refreshUI() {
		if (field.isReadOnly()) {
			textField.setEditable(false);
		}
		textChangedByUser = false;
		textField.setText(textType.toText(field.getValue(object)));
		textChangedByUser = true;
	}

	@Override
	public void showCaption() {
		add(new AlignedLabel(field.getCaption() + ": "), BorderLayout.WEST);
	}
}
