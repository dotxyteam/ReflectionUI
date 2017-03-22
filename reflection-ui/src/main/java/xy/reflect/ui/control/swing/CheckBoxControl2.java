package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.input.FieldControlDataProxy;
import xy.reflect.ui.control.input.FieldControlInputProxy;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class CheckBoxControl2 extends NullableControl {

	private static final long serialVersionUID = 1L;

	protected static Object NOT_NULL = "not null";

	public CheckBoxControl2(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, new FieldControlInputProxy(input) {

			@Override
			public IFieldControlData getControlData() {
				return new FieldControlDataProxy(super.getControlData()) {

					@Override
					public Object getValue() {
						boolean b = (Boolean) super.getValue();
						if (b) {
							return NOT_NULL;
						} else {
							return null;
						}
					}

					@Override
					public void setValue(Object value) {
						if (value != null) {
							super.setValue(true);
						} else {
							super.setValue(false);
						}
					}

					@Override
					public ITypeInfo getType() {
						return new DefaultTypeInfo(swingRenderer.getReflectionUI(), Object.class);
					}

				};
			}

		});
	}

	@Override
	protected Component createSubControl() {
		JButton result = new JButton();
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IFieldControlData data = input.getControlData();
				if (data.getValue() == null) {
					data.setValue(NOT_NULL);
				} else {
					data.setValue(null);
				}
				SwingUtilities.invokeLater(new Runnable() {					
					@Override
					public void run() {
						refreshUI();
					}
				});				
			}
		});
		result.setBorderPainted(false);
		result.setContentAreaFilled(false);
		return result;
	}

	@Override
	protected Component createNullControl() {
		return createSubControl();
	}

	@Override
	protected Object generateNonNullValue() {
		return NOT_NULL;
	}

}
