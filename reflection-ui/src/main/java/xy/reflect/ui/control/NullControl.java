package xy.reflect.ui.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
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
			public Object[] getValueOptions(Object object) {
				return null;
			}

			
			@Override
			public ITypeInfo getType() {
				return new TextualTypeInfo(reflectionUI, String.class);
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}

			@Override
			public Map<String, Object> getSpecificProperties() {
				return Collections.emptyMap();
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
