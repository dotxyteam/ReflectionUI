


package xy.reflect.ui.control.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import xy.reflect.ui.control.BufferedFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.builder.AbstractEditorBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that displays a button allowing to open an editor for the field
 * value.
 * 
 * @author olitank
 *
 */
public class DialogAccessControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected BufferedFieldControlData data;

	protected Component statusControl;
	protected Component iconControl;
	protected Component actionControl;
	protected IFieldControlInput input;

	public DialogAccessControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		input = new FieldControlInputProxy(input) {
			ErrorHandlingFieldControlData errorHandlingFieldControlData = new ErrorHandlingFieldControlData(
					super.getControlData(), swingRenderer, DialogAccessControl.this);

			BufferedFieldControlData bufferedFieldControlData = new BufferedFieldControlData(
					errorHandlingFieldControlData);

			@Override
			public IFieldControlData getControlData() {
				return bufferedFieldControlData;
			}
		};
		this.input = input;
		this.data = (BufferedFieldControlData) input.getControlData();

		setLayout(new GridBagLayout());
		statusControl = createStatusControl(input);
		actionControl = createActionControl();
		iconControl = createIconControl();

		if (actionControl != null) {
			GridBagConstraints c = new GridBagConstraints();
			add(SwingRendererUtils.flowInLayout(actionControl, GridBagConstraints.CENTER), c);
		}
		if (statusControl != null) {
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(statusControl, c);
		}
		if (iconControl != null) {
			GridBagConstraints c = new GridBagConstraints();
			add(SwingRendererUtils.flowInLayout(iconControl, GridBagConstraints.CENTER), c);
		}

		refreshUI(true);
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		Object value = data.getValue();
		if ((value == null) && !isNullSupported()) {
			return false;
		}
		if (statusControl != null) {
			data.addInBuffer(value);
			updateStatusControl(refreshStructure);
		}
		if (iconControl != null) {
			data.addInBuffer(value);
			updateIconControl(refreshStructure);
		}
		if (actionControl != null) {
			updateActionControl();
		}
		return true;
	}

	protected boolean isNullSupported() {
		return false;
	}

	protected Component createIconControl() {
		return new JLabel();
	}

	protected Component createActionControl() {
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public Image retrieveBackgroundImage() {
				if (data.getButtonBackgroundImagePath() == null) {
					return null;
				} else {
					return SwingRendererUtils.loadImageThroughCache(data.getButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
				}
			}

			@Override
			public Color retrieveBackgroundColor() {
				if (data.getButtonBackgroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonBackgroundColor());
				}
			}

			@Override
			public Color retrieveForegroundColor() {
				if (data.getButtonForegroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonForegroundColor());
				}
			}

			@Override
			public Color retrieveBorderColor() {
				if (data.getButtonBorderColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonBorderColor());
				}
			}

			@Override
			public String retrieveText() {
				return swingRenderer.prepareMessageToDisplay("...");
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return null;
				}
				result.width = result.height;
				return result;
			}

		};
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					openDialog(DialogAccessControl.this);
				} catch (Throwable t) {
					swingRenderer.handleObjectException(result, t);
				}
			}
		});
		return result;
	}

	protected Component createStatusControl(IFieldControlInput input) {
		return new TextControl(swingRenderer, new FieldControlInputProxy(input) {

			@Override
			public IFieldControlData getControlData() {
				return new DefaultFieldControlData(swingRenderer.getReflectionUI()) {

					@Override
					public Object getValue() {
						Object fieldValue = DialogAccessControl.this.data.getValue();
						return ReflectionUIUtils.toString(swingRenderer.getReflectionUI(), fieldValue);
					}

					@Override
					public ITypeInfo getType() {
						return new DefaultTypeInfo(
								new JavaTypeInfoSource(swingRenderer.getReflectionUI(), String.class, null));
					}

				};
			}
		});
	}

	protected void openDialog(Component owner) {
		AbstractEditorBuilder subDialogBuilder = createSubDialogBuilder(owner);
		subDialogBuilder.createAndShowDialog();
	}

	protected AbstractEditorBuilder createSubDialogBuilder(final Component owner) {
		return new SubDialogBuilder(swingRenderer, owner, input);
	}

	protected void updateActionControl() {
		((AbstractControlButton) actionControl).updateStyle();
	}

	protected void updateIconControl(boolean refreshStructure) {
		ImageIcon icon = SwingRendererUtils.getObjectIcon(swingRenderer, data.getValue());
		if (icon != null) {
			icon = SwingRendererUtils.getSmallIcon(icon);
		}
		((JLabel) iconControl).setIcon(icon);
		iconControl.setVisible(((JLabel) iconControl).getIcon() != null);
	}

	protected void updateStatusControl(boolean refreshStructure) {
		((TextControl) statusControl).refreshUI(refreshStructure);
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
	public boolean isAutoManaged() {
		return true;
	}

	@Override
	public boolean requestCustomFocus() {
		if (data.isGetOnly()) {
			return false;
		}
		if (SwingRendererUtils.requestAnyComponentFocus(statusControl, swingRenderer)) {
			return true;
		}
		if (SwingRendererUtils.requestAnyComponentFocus(actionControl, swingRenderer)) {
			return true;
		}
		return false;
	}

	@Override
	public void validateSubForms() throws Exception {
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
	}

	@Override
	public String toString() {
		return "DialogAccessControl [data=" + data + "]";
	}

	protected static class SubDialogBuilder extends AbstractEditorBuilder {

		protected SwingRenderer swingRenderer;
		protected Component ownerComponent;
		protected IFieldControlInput input;
		protected IFieldControlData data;

		public SubDialogBuilder(SwingRenderer swingRenderer, Component ownerComponent, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.ownerComponent = ownerComponent;
			this.input = input;
			this.data = input.getControlData();
		}

		@Override
		protected IContext getContext() {
			return input.getContext();
		}

		@Override
		protected IContext getSubContext() {
			return null;
		}

		@Override
		protected boolean isEncapsulatedFormEmbedded() {
			return true;
		}

		@Override
		protected boolean isNullValueDistinct() {
			return false;
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		protected ValueReturnMode getReturnModeFromParent() {
			return data.getValueReturnMode();
		}

		@Override
		protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
			return data.getType().getSource();
		}

		@Override
		protected Object loadValue() {
			return data.getValue();
		}

		@Override
		protected String getParentModificationTitle() {
			return FieldControlDataModification.getTitle(data.getCaption());
		}

		@Override
		protected boolean isParentModificationFake() {
			return data.isTransient();
		}

		@Override
		protected ModificationStack getParentModificationStack() {
			return input.getModificationStack();
		}

		@Override
		protected Component getOwnerComponent() {
			return ownerComponent;
		}

		@Override
		protected boolean canCommitToParent() {
			return !data.isGetOnly();
		}

		@Override
		protected IModification createCommittingModification(Object newObjectValue) {
			return new FieldControlDataModification(data, newObjectValue);
		}

		@Override
		protected IInfoFilter getEncapsulatedFormFilter() {
			return data.getFormControlFilter();
		}

		@Override
		protected void handleRealtimeLinkCommitException(Throwable t) {
			swingRenderer.handleObjectException(ownerComponent, t);
		}
	}

}
