package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.component.ImagePanel;

public class ImageViewPlugin extends FileBrowserPlugin {

	@Override
	public String getControlTitle() {
		return "Image View";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return Image.class.isAssignableFrom(javaType);
	}

	@Override
	protected boolean handlesNull() {
		return true;
	}

	@Override
	protected AbstractConfiguration getDefaultControlConfiguration() {
		return new ImageViewConfiguration();
	}

	@Override
	protected Component createControl(Object renderer, IFieldControlInput input,
			AbstractConfiguration controlCustomization) {
		return new ImageView((SwingRenderer) renderer, input, (ImageViewConfiguration) controlCustomization);
	}

	public static class ImageViewConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;
		public boolean preserveRatio = true;

	}

	protected class ImageView extends ImagePanel implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected ImageViewConfiguration controlCustomization;
		protected Class<?> numberClass;

		public ImageView(SwingRenderer swingRenderer, IFieldControlInput input,
				ImageViewConfiguration controlCustomization) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			this.controlCustomization = controlCustomization;
			try {
				this.numberClass = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
				if (this.numberClass.isPrimitive()) {
					this.numberClass = ClassUtils.primitiveToWrapperClass(numberClass);
				}
			} catch (ClassNotFoundException e1) {
				throw new ReflectionUIError(e1);
			}

			preserveRatio(controlCustomization.preserveRatio);
			if (!data.isGetOnly()) {
				addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						try {
							onBrowseImage();
						} catch (Throwable t) {
							ImageView.this.swingRenderer.handleExceptionsFromDisplayedUI(ImageView.this, t);
						}
					}
				});
			}
			setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));

			refreshUI();
		}

		protected void onBrowseImage() {
			FileBrowserConfiguration fileBrowserConfiguration = new FileBrowserConfiguration();
			final File[] imageFileHolder = new File[1];
			IFieldControlInput fileBrowserInput = new FieldControlInputProxy(IFieldControlInput.NULL_CONTROL_INPUT) {

				@Override
				public IFieldControlData getControlData() {
					return new FieldControlDataProxy(IFieldControlData.NULL_CONTROL_DATA) {

						@Override
						public Object getValue() {
							return imageFileHolder[0];
						}

						@Override
						public void setValue(Object value) {
							imageFileHolder[0] = (File) value;
						}

						@Override
						public ITypeInfo getType() {
							return swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(File.class));
						}

					};
				}

			};
			FileBrowser browser = new FileBrowser(swingRenderer, fileBrowserInput, fileBrowserConfiguration);
			browser.openDialog();
			if (imageFileHolder[0] == null) {
				return;
			}
			Image loadedImage;
			try {
				loadedImage = ImageIO.read(imageFileHolder[0]);
			} catch (IOException e) {
				throw new ReflectionUIError(
						"Failed to load the image file '" + imageFileHolder[0] + "': " + e.toString(), e);
			}
			data.setValue(loadedImage);
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		@Override
		public boolean refreshUI() {			
			setImage((Image) data.getValue());
			return true;
		}

		@Override
		public boolean handlesModificationStackUpdate() {
			return false;
		}

		@Override
		public boolean requestCustomFocus() {
			return false;
		}

		@Override
		public void validateSubForm() throws Exception {
		}

		@Override
		public void addMenuContribution(MenuModel menuModel) {
		}

		@Override
		public String toString() {
			return "ImageView [data=" + data + "]";
		}
	}

}
