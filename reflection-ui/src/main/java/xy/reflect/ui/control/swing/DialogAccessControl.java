package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import xy.reflect.ui.control.data.ControlDataProxy;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class DialogAccessControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IControlData data;

	protected Component statusControl;
	protected Component iconControl;
	protected Component button;

	public DialogAccessControl(final SwingRenderer swingRenderer, final IControlData data) {
		this.swingRenderer = swingRenderer;
		this.data = data;
		setLayout(new BorderLayout());

		statusControl = createStatusControl();
		button = createButton();
		iconControl = createIconControl();

		if (button != null) {
			add(SwingRendererUtils.flowInLayout(button, FlowLayout.CENTER), BorderLayout.WEST);
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

	@Override
	public void requestFocus() {
		button.requestFocus();
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

	protected Component createButton() {
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

	protected Component createStatusControl() {
		return new TextControl(swingRenderer, new ControlDataProxy(IControlData.NULL_CONTROL_DATA) {

			@Override
			public Object getValue() {
				Object fieldValue = data.getValue();
				return ReflectionUIUtils.toString(swingRenderer.getReflectionUI(), fieldValue);
			}

			@Override
			public ITypeInfo getType() {
				return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
			}

		});
	}

	protected void openDialog() {
		Object oldValue = data.getValue();
		ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(swingRenderer, this, oldValue);
		dialogBuilder.setGetOnly(data.isGetOnly());
		boolean cancellable = true;
		{
			if (!dialogBuilder.getDisplayValueType().isModificationStackAccessible()) {
				cancellable = false;
			}
			if (data.isGetOnly() && (data.getValueReturnMode() == ValueReturnMode.COPY)) {
				cancellable = false;
			}
		}
		dialogBuilder.setCancellable(cancellable);
		swingRenderer.showDialog(dialogBuilder.build(), true);

		IFieldInfo field = SwingRendererUtils.getControlFormAwareField(DialogAccessControl.this);
		
		ModificationStack parentModifStack = SwingRendererUtils
				.findParentFormModificationStack(DialogAccessControl.this, swingRenderer);
		ModificationStack childModifStack = dialogBuilder.getModificationStack();
		String childModifTitle = ControlDataValueModification.getTitle(field);
		IInfo childModifTarget = field;
		IModification commitModif;
		if (field.isGetOnly()) {
			commitModif = null;
		} else {
			commitModif = new ControlDataValueModification(data, dialogBuilder.getValue(), childModifTarget);
		}
		boolean childModifAccepted = (!dialogBuilder.isCancellable()) || dialogBuilder.isOkPressed();
		ValueReturnMode childValueReturnMode = field.getValueReturnMode();
		boolean childValueNew = dialogBuilder.isValueNew();
		if (ReflectionUIUtils.integrateSubModifications(swingRenderer.getReflectionUI(), parentModifStack,
				childModifStack, childModifAccepted, childValueReturnMode, childValueNew, commitModif, childModifTarget,
				childModifTitle)) {
			updateControls();
		}
	}

	protected void updateControls() {
		updateStatusControl();
		updateIconControl();
	}

	protected void updateIconControl() {
		Object fieldValue = data.getValue();
		Image iconImage = swingRenderer.getObjectIconImage(fieldValue);
		if (iconImage != null) {
			iconImage = SwingRendererUtils.scalePreservingRatio(iconImage, 16, 16, Image.SCALE_SMOOTH);
			((JLabel) iconControl).setIcon(new ImageIcon(iconImage));
			iconControl.setVisible(true);
		} else {
			((JLabel) iconControl).setIcon(null);
			iconControl.setVisible(false);
		}
	}

	protected void updateStatusControl() {
		((TextControl) statusControl).refreshUI();
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		return false;
	}

	@Override
	public boolean showCaption(String caption) {
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
	public Object getFocusDetails() {
		return null;
	}

	@Override
	public void requestDetailedFocus(Object focusDetails) {
	}

	@Override
	public void validateSubForm() throws Exception {
	}
}
