package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class NullControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected Runnable action;
	protected IFieldControlInput input;
	protected IFieldControlData data;
	protected SwingRenderer swingRenderer;
	protected Component labelComponent;

	public NullControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		setLayout(new BorderLayout());
		add(labelComponent = createLabelComponent(), BorderLayout.CENTER);
		setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));

		setupAction();
	}

	protected void setupAction() {
		labelComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				runAction();
			}
		});
		if (!input.getControlData().isGetOnly()) {
			setAction(new Runnable() {
				@Override
				public void run() {
					Object newValue = null;
					try {
						newValue = swingRenderer.onTypeInstanciationRequest(NullControl.this, data.getType());
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(NullControl.this, t);
					}
					if (newValue == null) {
						return;
					}
					data.setValue(newValue);
				}
			});
		}
	}

	protected Component createLabelComponent() {
		TextControl result = new TextControl(swingRenderer, new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				return new FieldControlDataProxy(super.getControlData()) {

					@Override
					public boolean isGetOnly() {
						return true;
					}

					@Override
					public Object getValue() {
						String result = base.getNullValueLabel();
						if (result == null) {
							result = "";
						}
						return result;
					}

					@Override
					public ITypeInfo getType() {
						return new DefaultTypeInfo(swingRenderer.getReflectionUI(), String.class);
					}

				};
			}

		}) {
			private static final long serialVersionUID = 1L;

			@Override
			protected JTextArea createTextComponent() {
				final JTextArea result = new JTextArea();
				result.setEditable(false);
				((JComponent) result).setBorder(null);
				if ("".equals(data.getValue())) {
					result.setBackground(swingRenderer.getNullColor());
				} else {
					result.setBackground(ReflectionUIUtils.getDisabledTextBackgroundColor());
				}
				result.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						runAction();
					}
				});
				return result;
			}

			@Override
			protected JScrollPane createScrollPane() {
				JScrollPane result = super.createScrollPane();
				result.setViewportBorder(null);
				return result;
			}
		};
		return result;
	}

	protected void runAction() {
		if (action != null) {
			try {
				action.run();
			} catch (Throwable t) {
				swingRenderer.handleExceptionsFromDisplayedUI(NullControl.this, t);
			}
		}
	}

	public Runnable getAction() {
		return action;
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	@Override
	public boolean showsCaption() {
		return true;
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
	public boolean handlesModificationStackUpdate() {
		return false;
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
	}

	@Override
	public boolean requestCustomFocus() {
		return false;
	}

	@Override
	public String toString() {
		return "NullControl [data=" + data + "]";
	}

}
