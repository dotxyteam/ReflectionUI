package xy.reflect.ui.control.swing.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowser;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileNameFilterConfiguration;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.SelectionModeConfiguration;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ImagePanel;

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
	protected boolean displaysDistinctNullValue() {
		return false;
	}

	@Override
	protected AbstractConfiguration getDefaultControlConfiguration() {
		return new ImageViewConfiguration();
	}

	@Override
	public ImageView createControl(Object renderer, IFieldControlInput input,
			AbstractConfiguration controlCustomization) {
		return new ImageView((SwingRenderer) renderer, input, (ImageViewConfiguration) controlCustomization);
	}

	public static class ImageViewConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public SizeConstraint sizeConstraint;
	}

	public static abstract class SizeConstraint extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public int canvasWidth = 100;
		public int canvasHeight = 100;

		public abstract void configure(JPanel imagePanelContainer, ImagePanel imagePanel);

	}

	public static class StretchingSizeConstraint extends SizeConstraint {
		private static final long serialVersionUID = 1L;

		@Override
		public void configure(JPanel imagePanelContainer, ImagePanel imagePanel) {
			Dimension size = new Dimension(canvasWidth, canvasHeight);
			imagePanel.setPreferredSize(size);
			imagePanel.setMinimumSize(size);
			imagePanel.setMaximumSize(size);
			imagePanel.preserveRatio(false);
			imagePanelContainer.setLayout(new BorderLayout());
			imagePanelContainer.add(imagePanel, BorderLayout.CENTER);
		}
	}

	public static class ScalingSizeConstraint extends SizeConstraint {
		private static final long serialVersionUID = 1L;

		@Override
		public void configure(JPanel imagePanelContainer, ImagePanel imagePanel) {
			Dimension size = new Dimension(canvasWidth, canvasHeight);
			imagePanel.setPreferredSize(size);
			imagePanel.setMinimumSize(size);
			imagePanel.setMaximumSize(size);
			imagePanel.preserveRatio(true);
			imagePanelContainer.setLayout(new BorderLayout());
			imagePanelContainer.add(imagePanel, BorderLayout.CENTER);
		}
	}

	public static class ScrollableSizeConstraint extends SizeConstraint {
		private static final long serialVersionUID = 1L;

		@Override
		public void configure(JPanel imagePanelContainer, ImagePanel imagePanel) {
			imagePanel.preserveRatio(true);
			JScrollPane scrollPane = new JScrollPane(
					SwingRendererUtils.flowInLayout(imagePanel, GridBagConstraints.CENTER));
			Dimension size = new Dimension(canvasWidth, canvasHeight);
			scrollPane.setPreferredSize(size);
			scrollPane.setMinimumSize(size);
			scrollPane.setMaximumSize(size);
			imagePanelContainer.setLayout(new BorderLayout());
			imagePanelContainer.add(scrollPane, BorderLayout.CENTER);
		}
	}

	public static class ZoomableSizeConstraint extends SizeConstraint {
		private static final long serialVersionUID = 1L;

		float ZOOM_CHANGE_FACTOR = 1.2f;

		@Override
		public void configure(JPanel imagePanelContainer, final ImagePanel imagePanel) {
			imagePanel.preserveRatio(true);
			if ((imagePanel.getPreferredSize().width > canvasWidth)
					|| (imagePanel.getPreferredSize().height > canvasHeight)) {
				imagePanel.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
			}
			JPanel zoomPanel = new JPanel();
			{
				zoomPanel.setLayout(new BorderLayout());
				JButton biggerButton = new JButton("+");
				{
					zoomPanel.add(biggerButton, BorderLayout.EAST);
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
				JButton smallerButton = new JButton("-");
				{
					zoomPanel.add(smallerButton, BorderLayout.WEST);
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
			}
			JScrollPane scrollPane = new JScrollPane(
					SwingRendererUtils.flowInLayout(imagePanel, GridBagConstraints.CENTER));
			Dimension size = new Dimension(canvasWidth, canvasHeight);
			scrollPane.setPreferredSize(size);
			scrollPane.setMinimumSize(size);
			scrollPane.setMaximumSize(size);
			imagePanelContainer.setLayout(new BorderLayout());
			imagePanelContainer.add(scrollPane, BorderLayout.CENTER);
			imagePanelContainer.add(SwingRendererUtils.flowInLayout(zoomPanel, GridBagConstraints.CENTER),
					BorderLayout.NORTH);
		}
	}

	public class ImageView extends JPanel implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected ImageViewConfiguration controlCustomization;
		protected Class<?> numberClass;
		protected ImagePanel imagePanel;
		protected JPanel imagePanelContainer;

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
			setLayout(new BorderLayout());
			imagePanelContainer = new JPanel();
			add(SwingRendererUtils.flowInLayout(imagePanelContainer, GridBagConstraints.CENTER), BorderLayout.CENTER);
			if (!data.isGetOnly()) {
				browseOnMousePressed(this);
			}
			if (data.getCaption().length() > 0) {
				setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
			}

			refreshUI();
		}

		protected void browseOnMousePressed(Component c) {
			c.addMouseListener(new MouseAdapter() {
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

		protected ImagePanel createImagePanel() {
			ImagePanel result = new ImagePanel();
			Image image = (Image) data.getValue();
			if (image != null) {
				result.setImage(image);
				Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));
				result.setPreferredSize(size);
				result.setMinimumSize(size);
				result.setMaximumSize(size);
			}
			if (!data.isGetOnly()) {
				browseOnMousePressed(result);
			}
			return result;
		}

		protected void onBrowseImage() {
			FileBrowserConfiguration fileBrowserConfiguration = new FileBrowserConfiguration();
			fileBrowserConfiguration.selectionMode = SelectionModeConfiguration.FILES_ONLY;
			fileBrowserConfiguration.actionTitle = "Load Image";
			FileNameFilterConfiguration filter = new FileNameFilterConfiguration();
			{
				filter.description = "Images";
				filter.extensions.addAll(Arrays.asList(ImageIO.getReaderFileSuffixes()));
				fileBrowserConfiguration.fileNameFilters.add(filter);
			}
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
			FileBrowser browser = new FileBrowserPlugin().createControl(swingRenderer, fileBrowserInput,
					fileBrowserConfiguration);
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
			return false;
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		@Override
		public boolean refreshUI() {
			imagePanelContainer.removeAll();
			imagePanel = createImagePanel();
			if (controlCustomization.sizeConstraint == null) {
				imagePanel.preserveRatio(true);
				imagePanelContainer.setLayout(new BorderLayout());
				imagePanelContainer.add(imagePanel, BorderLayout.CENTER);
			} else {
				controlCustomization.sizeConstraint.configure(imagePanelContainer, imagePanel);
			}
			SwingRendererUtils.handleComponentSizeChange(this);
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
