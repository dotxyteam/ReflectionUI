package xy.reflect.ui.control.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class NullControl2 extends JButton implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;

	public NullControl2(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		
		setContentAreaFilled(false);
		setBorder(null);
		setFocusable(false);
		String text = data.getNullValueLabel();
		if ((text == null) || (text.length() == 0)) {
			setText(" ");
		} else {
			setText(text);
		}
	}

	public void setAction(final Runnable action) {
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		});
	}

	@Override
	public boolean showCaption() {
		setBorder(BorderFactory.createTitledBorder(data.getCaption()));
		return true;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return false;
	}

	@Override
	public Object getFocusDetails() {
		return null;
	}

	@Override
	public boolean requestDetailedFocus(Object focusDetails) {
		return false;
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public String toString() {
		return "NullControl2 [data=" + data + "]";
	}

}
