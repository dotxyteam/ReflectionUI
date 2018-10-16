package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ControlPanel;

public class NullControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected Runnable activationAction;
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
		if (data.getForegroundColor() != null) {
			((TitledBorder) getBorder()).setTitleColor(SwingRendererUtils.getColor(data.getForegroundColor()));
		}
		setupActivationAction();
	}

	protected void setupActivationAction() {
		labelComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				runActivationAction();
			}
		});
		if (!input.getControlData().isGetOnly()) {
			setActivationAction(new Runnable() {
				@Override
				public void run() {
					Object newValue = null;
					try {
						newValue = data.createValue(data.getType(), true);
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
						return new DefaultTypeInfo(swingRenderer.getReflectionUI(), new JavaTypeInfoSource(String.class, null));
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
					result.setBackground(SwingRendererUtils.getDisabledTextBackgroundColor());
				}
				result.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						runActivationAction();
					}
				});
				return result;
			}

			@Override
			protected JScrollPane createScrollPane() {
				JScrollPane result = super.createScrollPane();
				return result;
			}
		};
		return result;
	}

	protected void runActivationAction() {
		if (activationAction != null) {
			try {
				activationAction.run();
			} catch (Throwable t) {
				swingRenderer.handleExceptionsFromDisplayedUI(NullControl.this, t);
			}
		}
	}

	public Runnable getActivationAction() {
		return activationAction;
	}

	public void setActivationAction(Runnable action) {
		this.activationAction = action;
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		return false;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean handlesModificationStackAndStress() {
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
