package xy.reflect.ui.control.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public class NullControl extends TextControl {

	protected static final long serialVersionUID = 1L;

	public NullControl(final ReflectionUI reflectionUI, final Runnable onMousePress) {
		super(reflectionUI, null, new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

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
				return new TextualTypeInfo(reflectionUI, String.class);
			}

		});
		if (onMousePress != null) {
			textComponent.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					try {
						onMousePress.run();
					} catch (Throwable t) {
						reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(NullControl.this, t);
					}
				}
			});
		}
		textComponent.setEditable(false);
		textComponent.setBackground(
				SwingRendererUtils.fixSeveralColorRenderingIssues(reflectionUI.getSwingRenderer().getNullColor()));
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
