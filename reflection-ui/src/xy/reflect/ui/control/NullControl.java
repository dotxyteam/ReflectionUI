package xy.reflect.ui.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.UIManager;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class NullControl extends TextControl {

	protected static final long serialVersionUID = 1L;

	public NullControl(final ReflectionUI reflectionUI, final Runnable onMousePress) {
		super(reflectionUI, null, new IFieldInfo() {
			
			@Override
			public Object getValue(Object object) {
				return "";
			}

			@Override
			public ITypeInfo getType() {
				return new DefaultTextualTypeInfo(reflectionUI, String.class);
			}

			@Override
			public void setValue(Object object, Object value) {
			}

			@Override
			public boolean isReadOnly() {
				return true;
			}

			@Override
			public String getName() {
				return "";
			}

			@Override
			public String getCaption() {
				return "";
			}

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}
		});
		if (onMousePress != null) {
			textField.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					try {
						onMousePress.run();
					} catch (Throwable t) {
						reflectionUI.handleExceptionsFromDisplayedUI(
								NullControl.this, t);
					}
				}
			});
		}
	}

	@Override
	public void refreshUI() {
		super.refreshUI();
		textField.setBackground(UIManager.getColor("TextField.shadow"));
	}

}
