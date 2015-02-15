package xy.reflect.ui.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class NullControl extends TextControl {

	protected static final long serialVersionUID = 1L;

	public NullControl(final ReflectionUI reflectionUI,
			final Runnable onMousePress) {
		super(reflectionUI, null, new IFieldInfo() {

			@Override
			public String getName() {
				return "";
			}

			@Override
			public String getDocumentation() {
				return null;
			}

			@Override
			public String getCaption() {
				return "";
			}

			@Override
			public void setValue(Object object, Object value) {
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
				return "";
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
		if (onMousePress != null) {
			textComponent.addMouseListener(new MouseAdapter() {
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
		textComponent.setEditable(false);
		textComponent.setBackground(ReflectionUIUtils
				.fixSeveralColorRenderingIssues(reflectionUI
						.getNullColor()));
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

	@Override
	public boolean showCaption() {
		return false;
	}
	
	
	

}
