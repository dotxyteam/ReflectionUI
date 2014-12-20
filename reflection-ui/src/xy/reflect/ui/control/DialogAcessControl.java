package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.component.TabulatingLabel;

public class DialogAcessControl extends JPanel implements ICanShowCaptionControl{

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected TextControl textControl;
	protected Component iconControl;
	protected JButton button;

	public DialogAcessControl(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		setLayout(new BorderLayout());

		add(iconControl = createIconControl(), BorderLayout.EAST);

		JPanel centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(textControl = createTextControl(), BorderLayout.CENTER);
		centerPanel.add(createButton(), BorderLayout.WEST);

		updateControls();
	}

	protected Component createIconControl() {
		return new JLabel();
	}

	protected JButton createButton() {
		button = new JButton("...");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					openDialog();
				} catch (Throwable t) {
					reflectionUI.handleDisplayedUIExceptions(button, t);
				}
			}
		});
		return button;
	}

	protected TextControl createTextControl() {
		return new TextControl(reflectionUI, object, new IFieldInfo() {

			@Override
			public String getName() {
				return "";
			}

			@Override
			public String getCaption() {
				return null;
			}

			@Override
			public void setValue(Object object, Object value) {
				throw new AssertionError();
			}

			@Override
			public boolean isReadOnly() {
				return true;
			}

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public Object getValue(Object object) {
				Object fieldValue = field.getValue(object);
				return reflectionUI.toString(fieldValue);
			}

			@Override
			public ITypeInfo getType() {
				return new DefaultTextualTypeInfo(reflectionUI, String.class);
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}
		});
	}

	protected void openDialog() {
		final Accessor<Object> valueAccessor = new Accessor<Object>() {

			@Override
			public Object get() {
				return field.getValue(object);
			}

			@Override
			public void set(Object t) {
				field.setValue(object, t);
			}

		};
		Runnable refreshTrigger = new Runnable() {
			@Override
			public void run() {
				updateControls();
			}
		};
		ModificationStack parentStack = ReflectionUIUtils.findModificationStack(DialogAcessControl.this, reflectionUI);
		String title = reflectionUI.getFieldTitle(object, field);
		IInfoCollectionSettings settings = new IInfoCollectionSettings() {
			@Override
			public boolean allReadOnly() {
				return field.isReadOnly();
			}

			@Override
			public boolean excludeField(IFieldInfo field) {
				return false;
			}

			@Override
			public boolean excludeMethod(IMethodInfo method) {
				return false;
			}
		};
		reflectionUI.openValueDialog(this, object, valueAccessor, settings,
				refreshTrigger, parentStack, title);

	}

	protected void updateControls() {
		updateTextControl();
		updateIconControl();
	}

	protected void updateIconControl() {
		Object fieldValue = field.getValue(object);
		Image iconImage = reflectionUI.getObjectIconImage(fieldValue);
		if (iconImage != null) {
			((JLabel) iconControl).setIcon(new ImageIcon(iconImage));
		} else {
			((JLabel) iconControl).setIcon(null);
		}
	}

	protected void updateTextControl() {
		textControl.refreshUI();
	}
	


	@Override
	public void showCaption() {
		add(new TabulatingLabel(field.getCaption() + ": "), BorderLayout.WEST);
	}

}
