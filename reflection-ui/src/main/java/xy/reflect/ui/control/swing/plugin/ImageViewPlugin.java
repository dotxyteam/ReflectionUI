
package xy.reflect.ui.control.swing.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlInput;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowser;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileNameFilterConfiguration;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.SelectionModeConfiguration;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.ImagePanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control plugin that allows to display/update images.
 * 
 * @author olitank
 *
 */
public class ImageViewPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Image View";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return Image.class.isAssignableFrom(javaType);
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new ImageViewConfiguration();
	}

	@Override
	public ImageView createControl(Object renderer, IFieldControlInput input) {
		return new ImageView((SwingRenderer) renderer, input);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public IFieldControlData filterDistinctNullValueControlData(final Object renderer, IFieldControlData controlData) {
		return new FieldControlDataProxy(controlData) {
			@Override
			public ITypeInfo getType() {
				return new ImageTypeInfoProxyFactory(((SwingRenderer) renderer).getReflectionUI())
						.wrapTypeInfo(super.getType());
			}

		};
	}

	protected static class ImageTypeInfoProxyFactory extends InfoProxyFactory {

		protected ReflectionUI reflectionUI;

		public ImageTypeInfoProxyFactory(ReflectionUI reflectionUI) {
			this.reflectionUI = reflectionUI;
		}

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo type) {
			if (ImageConstructor.isCompatibleWith(type)) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>();
				result.add(new ImageConstructor(reflectionUI, type));
				return result;
			}
			return super.getConstructors(type);
		}

		@Override
		protected boolean isConcrete(ITypeInfo type) {
			if (ImageConstructor.isCompatibleWith(type)) {
				return true;
			}
			return super.isConcrete(type);
		}

	}

	protected static class ImageConstructor extends AbstractConstructorInfo {

		protected ReflectionUI reflectionUI;
		protected ITypeInfo type;
		protected ITypeInfo returnType;

		public ImageConstructor(ReflectionUI reflectionUI, ITypeInfo type) {
			this.reflectionUI = reflectionUI;
			this.type = type;
		}

		@Override
		public ITypeInfo getReturnValueType() {
			if (returnType == null) {
				returnType = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(type.getSource()) {
					@Override
					public SpecificitiesIdentifier getSpecificitiesIdentifier() {
						return null;
					}

					@Override
					protected String getTypeInfoProxyFactoryIdentifier() {
						return "ConstructorReturnValueTypeInfoProxyFactory [of=" + getClass().getName() + "]";
					}
				});
			}
			return returnType;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Object invoke(Object ignore, InvocationData invocationData) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}

		public static boolean isCompatibleWith(ITypeInfo type) {
			Class<?> imageClass;
			try {
				imageClass = ClassUtils.getCachedClassforName(type.getName());
			} catch (ClassNotFoundException e) {
				return false;
			}
			return imageClass.isAssignableFrom(BufferedImage.class);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ImageConstructor other = (ImageConstructor) obj;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ImageConstructor [type=" + type + "]";
		}

	}

	public static class ImageViewConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public SizeConstraint sizeConstraint;
	}

	public enum ImageSizeUnit {
		PIXELS, SCREEN_PERCENT
	}

	public static abstract class SizeConstraint extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public int canvasWidth = 100;
		public int canvasHeight = 100;
		public ImageSizeUnit unit = ImageSizeUnit.PIXELS;
		public boolean scalingQualitHigh = false;;

		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			if (unit == null) {
				unit = ImageSizeUnit.PIXELS;
			}
		}

		public abstract void configure(ImageView imageView, JPanel imagePanelContainer, ImagePanel imagePanel);

		public abstract void updateImagePanel(ImageView imageView, ImagePanel imagePanel);

		protected Dimension getSizeInPixels() {
			if (unit == ImageSizeUnit.PIXELS) {
				return new Dimension(canvasWidth, canvasHeight);
			} else if (unit == ImageSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
				int width = Math.round((canvasWidth / 100f) * screenSize.width);
				int height = Math.round((canvasHeight / 100f) * screenSize.height);
				return new Dimension(width, height);
			} else {
				throw new ReflectionUIError();
			}
		}

	}

	public static class StretchingSizeConstraint extends SizeConstraint {
		private static final long serialVersionUID = 1L;

		@Override
		public void configure(ImageView imageView, JPanel imagePanelContainer, ImagePanel imagePanel) {
			Dimension size = getSizeInPixels();
			imagePanel.setPreferredSize(size);
			imagePanel.setMinimumSize(size);
			imagePanel.setPreservingRatio(false);
			imagePanel.setScalingQualitHigh(scalingQualitHigh);
			imagePanelContainer.setLayout(new BorderLayout());
			imagePanelContainer.add(imagePanel, BorderLayout.CENTER);
		}

		@Override
		public void updateImagePanel(ImageView imageView, ImagePanel imagePanel) {
			Image image = (Image) imageView.data.getValue();
			imagePanel.setImage(image);
		}
	}

	public static class ScalingSizeConstraint extends SizeConstraint {
		private static final long serialVersionUID = 1L;

		public boolean areaFilled = false;

		@Override
		public void configure(ImageView imageView, JPanel imagePanelContainer, ImagePanel imagePanel) {
			Dimension size = getSizeInPixels();
			imagePanel.setPreferredSize(size);
			imagePanel.setMinimumSize(size);
			imagePanel.setPreservingRatio(true);
			imagePanel.setFillingAreaWhenPreservingRatio(areaFilled);
			imagePanel.setScalingQualitHigh(scalingQualitHigh);
			imagePanelContainer.setLayout(new BorderLayout());
			imagePanelContainer.add(imagePanel, BorderLayout.CENTER);
		}

		@Override
		public void updateImagePanel(ImageView imageView, ImagePanel imagePanel) {
			Image image = (Image) imageView.data.getValue();
			imagePanel.setImage(image);
		}
	}

	public static class ScrollableSizeConstraint extends SizeConstraint {
		private static final long serialVersionUID = 1L;

		@Override
		public void configure(ImageView imageView, JPanel imagePanelContainer, ImagePanel imagePanel) {
			imagePanel.setPreservingRatio(true);
			imagePanel.setFillingAreaWhenPreservingRatio(false);
			imagePanel.setScalingQualitHigh(scalingQualitHigh);
			JScrollPane scrollPane = new ControlScrollPane(
					SwingRendererUtils.flowInLayout(imagePanel, GridBagConstraints.CENTER));
			scrollPane.setBorder(null);
			Dimension size = getSizeInPixels();
			scrollPane.setPreferredSize(size);
			scrollPane.setMinimumSize(size);
			imagePanelContainer.setLayout(new BorderLayout());
			imagePanelContainer.add(scrollPane, BorderLayout.CENTER);
		}

		@Override
		public void updateImagePanel(ImageView imageView, ImagePanel imagePanel) {
			imageView.updateImagePanelWithoutSizeConstraint(imageView, imagePanel);
		}
	}

	public static class ZoomableSizeConstraint extends SizeConstraint {
		private static final long serialVersionUID = 1L;

		@Override
		public void configure(final ImageView imageView, JPanel imagePanelContainer, final ImagePanel imagePanel) {
			imagePanelContainer.setLayout(new BorderLayout());
			imagePanel.setPreservingRatio(true);
			imagePanel.setFillingAreaWhenPreservingRatio(false);
			imagePanel.setScalingQualitHigh(scalingQualitHigh);
			JPanel zoomPanel = new ControlPanel();
			{
				imagePanelContainer.add(SwingRendererUtils.flowInLayout(zoomPanel, GridBagConstraints.CENTER),
						BorderLayout.NORTH);
				zoomPanel.setBorder(
						BorderFactory.createTitledBorder(null, imageView.swingRenderer.prepareMessageToDisplay("Zoom"),
								TitledBorder.CENTER, TitledBorder.TOP));
				if (imageView.data.getLabelForegroundColor() != null) {
					((TitledBorder) zoomPanel.getBorder())
							.setTitleColor(SwingRendererUtils.getColor(imageView.data.getLabelForegroundColor()));
				}
				if (imageView.data.getBorderColor() != null) {
					((TitledBorder) zoomPanel.getBorder()).setBorder(BorderFactory
							.createLineBorder(SwingRendererUtils.getColor(imageView.data.getBorderColor())));
				}
				final float ZOOM_CHANGE_FACTOR = 1.3f;
				zoomPanel.setLayout(new FlowLayout());
				JButton smallerButton = createZoomButton(imageView, "-");
				{
					zoomPanel.add(smallerButton);
					smallerButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Dimension size = imagePanel.getSize();
							size.width /= ZOOM_CHANGE_FACTOR;
							size.height /= ZOOM_CHANGE_FACTOR;
							imagePanel.setPreferredSize(size);
							SwingRendererUtils.handleComponentSizeChange(imagePanel);
						}
					});
				}
				JButton biggerButton = createZoomButton(imageView, "+");
				{
					zoomPanel.add(biggerButton);
					biggerButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Dimension size = imagePanel.getSize();
							size.width *= ZOOM_CHANGE_FACTOR;
							size.height *= ZOOM_CHANGE_FACTOR;
							imagePanel.setPreferredSize(size);
							SwingRendererUtils.handleComponentSizeChange(imagePanel);
						}
					});
				}
			}
			imagePanel.setPreferredSize(getSizeInPixels());
			JScrollPane scrollPane = new ControlScrollPane(
					SwingRendererUtils.flowInLayout(imagePanel, GridBagConstraints.CENTER));
			{
				imagePanelContainer.add(scrollPane, BorderLayout.CENTER);
				scrollPane.setBorder(null);
				Dimension size = getSizeInPixels();
				size.width += scrollPane.getHorizontalScrollBar().getPreferredSize().height;
				size.height += scrollPane.getVerticalScrollBar().getPreferredSize().width;
				scrollPane.setPreferredSize(size);
				scrollPane.setMinimumSize(size);
			}
		}

		protected JButton createZoomButton(final ImageView imageView, final String caption) {
			return new AbstractControlButton() {

				private static final long serialVersionUID = 1L;

				@Override
				public String retrieveText() {
					return imageView.swingRenderer.prepareMessageToDisplay(caption);
				}

				@Override
				public Image retrieveBackgroundImage() {
					if (imageView.data.getButtonBackgroundImagePath() == null) {
						return null;
					} else {
						return SwingRendererUtils.loadImageThroughCache(imageView.data.getButtonBackgroundImagePath(),
								ReflectionUIUtils.getErrorLogListener(imageView.swingRenderer.getReflectionUI()));
					}
				}

				@Override
				public Color retrieveBackgroundColor() {
					if (imageView.data.getButtonBackgroundColor() == null) {
						return null;
					} else {
						return SwingRendererUtils.getColor(imageView.data.getButtonBackgroundColor());
					}
				}

				@Override
				public Color retrieveForegroundColor() {
					if (imageView.data.getButtonForegroundColor() == null) {
						return null;
					} else {
						return SwingRendererUtils.getColor(imageView.data.getButtonForegroundColor());
					}
				}

				@Override
				public Color retrieveBorderColor() {
					if (imageView.data.getButtonBorderColor() == null) {
						return null;
					} else {
						return SwingRendererUtils.getColor(imageView.data.getButtonBorderColor());
					}
				}

			};
		}

		@Override
		public void updateImagePanel(ImageView imageView, ImagePanel imagePanel) {
			Image image = (Image) imageView.data.getValue();
			imagePanel.setImage(image);
		}
	}

	public class ImageView extends ControlPanel implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected Class<?> numberClass;
		protected ImagePanel imagePanel;
		protected JPanel imagePanelContainer;

		public ImageView(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			try {
				this.numberClass = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
				if (this.numberClass.isPrimitive()) {
					this.numberClass = ClassUtils.primitiveToWrapperClass(numberClass);
				}
			} catch (ClassNotFoundException e1) {
				throw new ReflectionUIError(e1);
			}
			setLayout(new BorderLayout());
			imagePanelContainer = new ControlPanel();
			add(SwingRendererUtils.flowInLayout(imagePanelContainer, GridBagConstraints.CENTER), BorderLayout.CENTER);
			browseOnMousePressed(this);
			refreshUI(true);
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			ImageViewConfiguration controlCustomization = (ImageViewConfiguration) loadControlCustomization(input);
			if (refreshStructure) {
				if (data.getCaption().length() > 0) {
					setBorder(
							BorderFactory.createTitledBorder(swingRenderer.prepareMessageToDisplay(data.getCaption())));
					if (data.getLabelForegroundColor() != null) {
						((TitledBorder) getBorder())
								.setTitleColor(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
					}
					if (data.getBorderColor() != null) {
						((TitledBorder) getBorder()).setBorder(
								BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
					}
				} else {
					setBorder(BorderFactory.createEmptyBorder());
				}
				setOpaque(false);
				imagePanelContainer.removeAll();
				imagePanel = null;
			}
			if (imagePanel == null) {
				imagePanel = createImagePanel();
				if (controlCustomization.sizeConstraint == null) {
					configureWithoutSizeConstraint(this);
					updateImagePanelWithoutSizeConstraint(this, imagePanel);
				} else {
					controlCustomization.sizeConstraint.configure(this, imagePanelContainer, imagePanel);
					controlCustomization.sizeConstraint.updateImagePanel(this, imagePanel);
				}
			} else {
				if (controlCustomization.sizeConstraint == null) {
					updateImagePanelWithoutSizeConstraint(this, imagePanel);
				} else {
					controlCustomization.sizeConstraint.updateImagePanel(this, imagePanel);
				}
			}
			imagePanel.getParent().invalidate();
			SwingRendererUtils.handleComponentSizeChange(this);
			return true;
		}

		protected void browseOnMousePressed(Component c) {
			c.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (data.isGetOnly()) {
						return;
					}
					try {
						onBrowseImage();
					} catch (Throwable t) {
						ImageView.this.swingRenderer.handleObjectException(ImageView.this, t);
					}
				}
			});
		}

		protected ImagePanel createImagePanel() {
			ImagePanel result = new ImagePanel();
			if (!data.isGetOnly()) {
				browseOnMousePressed(result);
			}
			return result;
		}

		protected void onBrowseImage() {
			final FileBrowserConfiguration fileBrowserConfiguration = new FileBrowserConfiguration();
			fileBrowserConfiguration.selectionMode = SelectionModeConfiguration.FILES_ONLY;
			fileBrowserConfiguration.actionTitle = "Load Image";
			FileNameFilterConfiguration filter = new FileNameFilterConfiguration();
			{
				filter.description = "Images";
				filter.extensions.addAll(Arrays.asList(ImageIO.getReaderFileSuffixes()));
				fileBrowserConfiguration.fileNameFilters.add(filter);
			}
			final File[] imageFileHolder = new File[1];
			final FileBrowserPlugin browserPlugin = new FileBrowserPlugin();
			IFieldControlInput fileBrowserInput = new DefaultFieldControlInput(swingRenderer.getReflectionUI()) {

				@Override
				public IFieldControlData getControlData() {
					return new DefaultFieldControlData(swingRenderer.getReflectionUI()) {

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
							return new InfoProxyFactory() {

								@Override
								protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
									Map<String, Object> result = super.getSpecificProperties(type);
									result = new HashMap<String, Object>();
									result = browserPlugin.storeControlCustomization(fileBrowserConfiguration, result);
									return result;
								}

							}.wrapTypeInfo(swingRenderer.getReflectionUI().buildTypeInfo(
									new JavaTypeInfoSource(swingRenderer.getReflectionUI(), File.class, null)));
						}

					};
				}

			};
			FileBrowser browser = browserPlugin.createControl(swingRenderer, fileBrowserInput);
			browser.openDialog(browser);
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
			SwingRendererUtils.displayErrorOnBorderAndTooltip(imagePanel, imagePanel, msg, swingRenderer);
			return true;
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		protected void updateImagePanelWithoutSizeConstraint(ImageView imageView, ImagePanel imagePanel) {
			Image image = (Image) data.getValue();
			if (image != null) {
				imagePanel.setImage(image);
				Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));
				imagePanel.setPreferredSize(size);
				imagePanel.setMinimumSize(size);
			} else {
				imagePanel.setImage(null);
				imagePanel.setPreferredSize(null);
				imagePanel.setMinimumSize(null);
			}
		}

		protected void configureWithoutSizeConstraint(ImageView imageView) {
			imagePanel.setPreservingRatio(true);
			imagePanel.setScalingQualitHigh(false);
			imagePanelContainer.setLayout(new BorderLayout());
			imagePanelContainer.add(imagePanel, BorderLayout.CENTER);
		}

		@Override
		public boolean isAutoManaged() {
			return false;
		}

		@Override
		public boolean requestCustomFocus() {
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
			return "ImageView [data=" + data + "]";
		}
	}

}
