
package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextArea;
import javax.swing.border.Border;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.Accessor;

/**
 * Empty field control that allows to represent the null value.
 * 
 * @author olitank
 *
 */
public class NullControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected Runnable activationAction;
	protected IFieldControlInput input;
	protected IFieldControlData data;
	protected SwingRenderer swingRenderer;
	protected TextControl labelComponent;

	public NullControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		setLayout(new BorderLayout());
		add(labelComponent = createLabelComponent(), BorderLayout.CENTER);
		labelComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				runActivationAction();
			}
		});
		refreshUI(true);
	}

	protected TextControl createLabelComponent() {
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
						return swingRenderer.prepareMessageToDisplay(result);
					}

					@Override
					public ITypeInfo getType() {
						return new DefaultTypeInfo(swingRenderer.getReflectionUI(),
								new JavaTypeInfoSource(String.class, null));
					}

				};
			}

		}) {
			private static final long serialVersionUID = 1L;

			@Override
			protected JTextArea createTextComponent() {
				final JTextArea result = new JTextArea();
				result.setEditable(false);
				result.setBorder(null);
				result.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						runActivationAction();
					}
				});
				return result;
			}

			@Override
			protected Dimension getScrollPanePreferredSize(Dimension defaultPreferredSize) {
				return defaultPreferredSize;
			}
		};
		return result;
	}

	protected void runActivationAction() {
		if (activationAction != null) {
			try {
				activationAction.run();
			} catch (Throwable t) {
				swingRenderer.handleException(NullControl.this, t);
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
		if (data.getValue() != null) {
			return false;
		}
		if (refreshStructure) {
			SwingRendererUtils.showFieldCaptionOnBorder(data, this, new Accessor<Border>() {
				@Override
				public Border get() {
					return new ControlPanel().getBorder();
				}
			}, swingRenderer);
			if (input.getControlData().isGetOnly()) {
				setActivationAction(null);
			} else {
				setActivationAction(new Runnable() {
					@Override
					public void run() {
						Object newValue = swingRenderer.onTypeInstantiationRequest(NullControl.this, data.getType());
						if (newValue == null) {
							return;
						}
						data.setValue(newValue);
					}
				});
			}
			labelComponent.refreshUI(true);
		}
		return true;
	}

	@Override
	public boolean displayError(Throwable error) {
		return false;
	}

	@Override
	public boolean isModificationStackManaged() {
		return false;
	}

	@Override
	public boolean areValueAccessErrorsManaged() {
		return false;
	}

	@Override
	public void validateControlData(ValidationSession session) throws Exception {
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
	}

	@Override
	public boolean requestCustomFocus() {
		return false;
	}

}
