
package xy.reflect.ui.control.swing.plugin;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin.StyledTextConfiguration.ControlDimensionSpecification;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin.StyledTextConfiguration.ControlSizeUnit;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control plugin that allows to display/update text that conforms to a
 * specific syntax.
 * 
 * @author olitank
 *
 */
public class EditorPlugin extends StyledTextPlugin {

	@Override
	public String getControlTitle() {
		return "Editor Control";
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new EditorConfiguration();
	}

	@Override
	public EditorControl createControl(Object renderer, IFieldControlInput input) {
		return new EditorControl((SwingRenderer) renderer, input);
	}

	public static class EditorConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public ControlDimensionSpecification width = new ControlDimensionSpecification();
		public ControlDimensionSpecification height = new ControlDimensionSpecification();
		public String syntaxImplementationClassName;

		public int getWidthInPixels() {
			if (width == null) {
				return -1;
			}
			if (width.unit == ControlSizeUnit.PIXELS) {
				return width.value;
			} else if (width.unit == ControlSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
				return Math.round((width.value / 100f) * screenSize.width);
			} else {
				throw new ReflectionUIError();
			}
		}

		public int getHeightInPixels() {
			if (height == null) {
				return -1;
			}
			if (height.unit == ControlSizeUnit.PIXELS) {
				return height.value;
			} else if (height.unit == ControlSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
				return Math.round((height.value / 100f) * screenSize.height);
			} else {
				throw new ReflectionUIError();
			}
		}

		public void validate() throws ClassNotFoundException {
			if ((syntaxImplementationClassName == null) || (syntaxImplementationClassName.length() == 0)) {
				throw new ReflectionUIError("Syntax implementation class name not specified !");
			}
			ClassUtils.getCachedClassForName(syntaxImplementationClassName);
		}

	}

	public class EditorControl extends StyledTextControl {

		private static final long serialVersionUID = 1L;

		public EditorControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected JTextComponent createTextComponent() {
			JEditorPane result = new JEditorPane() {

				private static final long serialVersionUID = 1L;

				@Override
				public void replaceSelection(String content) {
					boolean listenerWasDisabled = listenerDisabled;
					listenerDisabled = true;
					try {
						super.replaceSelection(content);
					} finally {
						listenerDisabled = listenerWasDisabled;
					}
					try {
						EditorControl.this.textComponentEditHappened();
					} catch (Throwable t) {
						swingRenderer.handleException(EditorControl.this, t);
					}
				}

				@Override
				public void setText(String t) {
					super.setText(t);
					updateScrollPolicy();
				}

			};
			return result;
		}

		@Override
		protected void setupTextComponentEvents() {
			textComponent.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						EditorControl.this.textComponentFocustLost();
					} catch (Throwable t) {
						swingRenderer.handleException(EditorControl.this, t);
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
		}

		@Override
		protected void updateTextComponentStyle(boolean refreshStructure) {
			if (refreshStructure) {
				String textToRestore = textComponent.getText();
				try {
					String syntaxImplementationClassName = ((EditorConfiguration) getOrLoadControlCustomization()).syntaxImplementationClassName;
					if ((syntaxImplementationClassName != null) && (syntaxImplementationClassName.length() > 0)) {
						try {
							Class<?> editorKitClass = ClassUtils.getCachedClassForName(syntaxImplementationClassName);
							((JEditorPane) textComponent).setEditorKit((EditorKit) editorKitClass.newInstance());
						} catch (Exception e) {
							throw new ReflectionUIError(e);
						}
					} else {
						((JEditorPane) textComponent).setEditorKit(new JEditorPane().getEditorKit());
					}
				} finally {
					textComponent.setText(textToRestore);
				}
				textComponent.getDocument().addUndoableEditListener(new UndoableEditListener() {
					@Override
					public void undoableEditHappened(UndoableEditEvent e) {
						try {
							EditorControl.this.textComponentEditHappened();
						} catch (Throwable t) {
							swingRenderer.handleException(EditorControl.this, t);
						}
					}
				});
				textComponent.getDocument().addDocumentListener(new DocumentListener() {

					void anyUpdate(DocumentEvent e) {
						try {
							EditorControl.this.textComponentEditHappened();
						} catch (Throwable t) {
							swingRenderer.handleException(EditorControl.this, t);
						}
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						anyUpdate(e);
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						anyUpdate(e);
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						anyUpdate(e);
					}
				});
			}
		}

		@Override
		protected int getConfiguredScrollPaneHeight() {
			return ((EditorConfiguration) getOrLoadControlCustomization()).getHeightInPixels();
		}

		@Override
		protected int getConfiguredScrollPaneWidth() {
			return ((EditorConfiguration) getOrLoadControlCustomization()).getWidthInPixels();
		}

		@Override
		public String toString() {
			return "EditorControl [data=" + data + "]";
		}
	}

}
