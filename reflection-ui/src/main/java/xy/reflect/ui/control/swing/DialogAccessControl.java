package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.editor.AbstractEditorBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class DialogAccessControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected Component statusControl;
	protected Component iconControl;
	protected Component actionControl;
	protected IFieldControlInput input;

	public DialogAccessControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		setLayout(new BorderLayout());

		statusControl = createStatusControl(input);
		actionControl = createChangeControl();
		iconControl = createIconControl();

		if (actionControl != null) {
			add(SwingRendererUtils.flowInLayout(actionControl, GridBagConstraints.CENTER), BorderLayout.WEST);
		}
		if (statusControl != null) {
			JPanel centerPanel = new JPanel();
			{
				add(centerPanel, BorderLayout.CENTER);
				centerPanel.setLayout(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1.0;
				centerPanel.add(statusControl, c);
			}
		}
		if (iconControl != null) {
			add(iconControl, BorderLayout.EAST);
		}

		updateControls();
	}

	protected Component createIconControl() {
		return new JLabel() {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result != null) {
					if (statusControl != null) {
						Dimension textControlPreferredSize = statusControl.getPreferredSize();
						if (textControlPreferredSize != null) {
							result.height = textControlPreferredSize.height;
						}
					}
				}
				return result;
			}

		};
	}

	protected Component createChangeControl() {
		final JButton result = new JButton("...") {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result != null) {
					result.width = result.height;
				}
				return result;
			}

		};
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					openDialog();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(result, t);
				}
			}
		});
		return result;
	}

	protected Component createStatusControl(IFieldControlInput input) {
		return new TextControl(swingRenderer, new FieldControlInputProxy(input) {

			@Override
			public IFieldControlData getControlData() {
				return new FieldControlDataProxy(IFieldControlData.NULL_CONTROL_DATA) {

					@Override
					public Object getValue() {
						Object fieldValue = DialogAccessControl.this.data.getValue();
						return ReflectionUIUtils.toString(swingRenderer.getReflectionUI(), fieldValue);
					}

					@Override
					public ITypeInfo getType() {
						return new DefaultTypeInfo(swingRenderer.getReflectionUI(), String.class);
					}

				};
			}
		});
	}

	protected void openDialog() {
		AbstractEditorBuilder subDialogBuilder = getSubDialogBuilder();
		subDialogBuilder.showDialog();
		if (subDialogBuilder.isParentModificationStackImpacted()) {
			updateControls();
		}
	}

	protected AbstractEditorBuilder getSubDialogBuilder() {
		return new AbstractEditorBuilder() {

			@Override
			public String getContextIdentifier() {
				return input.getContextIdentifier();
			}

			@Override
			public String getSubContextIdentifier() {
				return null;
			}

			@Override
			public boolean isObjectFormExpanded() {
				return true;
			}

			@Override
			public boolean isObjectValueNullable() {
				return false;
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return data.getValueReturnMode();
			}

			@Override
			public ITypeInfo getObjectDeclaredType() {
				return data.getType();
			}

			@Override
			public Object getInitialObjectValue() {
				return data.getValue();
			}

			@Override
			public String getCumulatedModificationsTitle() {
				return ControlDataValueModification.getTitle(input.getModificationsTarget());
			}

			@Override
			public IInfo getCumulatedModificationsTarget() {
				return input.getModificationsTarget();
			}

			@Override
			public ModificationStack getParentObjectModificationStack() {
				return input.getModificationStack();
			}

			@Override
			public Component getOwnerComponent() {
				return DialogAccessControl.this;
			}

			@Override
			public boolean canCommit() {
				return !data.isGetOnly();
			}

			@Override
			public IModification createCommitModification(Object newObjectValue) {
				return new ControlDataValueModification(data, newObjectValue, getCumulatedModificationsTarget());
			}

			@Override
			public IInfoFilter getObjectFormFilter() {
				return data.getFormControlFilter();
			}
		};
	}

	protected void updateControls() {
		updateStatusControl();
		updateIconControl();
	}

	protected void updateIconControl() {
		((JLabel) iconControl).setIcon(SwingRendererUtils.geObjectIcon(swingRenderer, data.getValue()));
		iconControl.setVisible(((JLabel) iconControl).getIcon() != null);
	}

	protected void updateStatusControl() {
		((TextControl) statusControl).refreshUI();
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean showsCaption() {
		return false;
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(statusControl, swingRenderer);
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
	}

	@Override
	public String toString() {
		return "DialogAccessControl [data=" + data + "]";
	}

}
