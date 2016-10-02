package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.type.util.VirtualFieldWrapperTypeInfo;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;

public class ObjectDialogBuilder {

	protected boolean getOnly;
	protected IInfoCollectionSettings infoSettings = IInfoCollectionSettings.DEFAULT;
	protected List<Component> additionalToolbarComponents;
	protected boolean cancellable = false;

	protected DialogBuilder delegate;
	protected SwingRenderer swingRenderer;
	protected Object value;
	private JPanel objectForm;

	public ObjectDialogBuilder(SwingRenderer swingRenderer, Object value) {
		this.swingRenderer = swingRenderer;
		this.value = value;
		delegate = new DialogBuilder(swingRenderer);

		setTitle(swingRenderer.getObjectTitle(value));
		setIconImage(swingRenderer.getObjectIconImage(value));
	}

	public Object getValue() {
		return value;
	}

	public boolean isGetOnly() {
		return getOnly;
	}

	public void setGetOnly(boolean getOnly) {
		this.getOnly = getOnly;
	}

	public IInfoCollectionSettings getInfoSettings() {
		return infoSettings;
	}

	public void setInfoSettings(IInfoCollectionSettings infoSettings) {
		this.infoSettings = infoSettings;
	}

	public List<Component> getAdditionalToolbarComponents() {
		return additionalToolbarComponents;
	}

	public void setAdditionalToolbarComponents(List<Component> additionalToolbarComponents) {
		this.additionalToolbarComponents = additionalToolbarComponents;
	}

	public boolean isCancellable() {
		return cancellable;
	}

	public void setCancellable(boolean cancellable) {
		this.cancellable = cancellable;
	}

	public boolean isOkPressed() {
		return delegate.isOkPressed();
	}

	public Component getOwnerComponent() {
		return delegate.getOwnerComponent();
	}

	public void setOwnerComponent(Component ownerComponent) {
		delegate.setOwnerComponent(ownerComponent);
	}

	public String getTitle() {
		return delegate.getTitle();
	}

	public void setTitle(String title) {
		delegate.setTitle(title);
	}

	public Image getIconImage() {
		return delegate.getIconImage();
	}

	public void setIconImage(Image iconImage) {
		delegate.setIconImage(iconImage);
	}

	public JPanel getObjectForm() {
		return objectForm;
	}

	public ModificationStack getModificationStack() {
		return swingRenderer.getModificationStackByForm().get(objectForm);
	}
	
	public boolean isModificationDetected() {
		ModificationStack modifStack = getModificationStack();
		if(modifStack == null){
			return false;
		}
		if(modifStack.isNull()){
			return false;
		}
		return true;
	}

	public JDialog build() {
		Object toDisplay = value;
		if (swingRenderer.hasCustomFieldControl(value)) {
			String fieldName = swingRenderer.getDefaultFieldCaption(toDisplay);
			Accessor<Object> valueAccessor = new Accessor<Object>() {
				@Override
				public Object get() {
					return ObjectDialogBuilder.this.value;
				}

				@Override
				public void set(Object t) {
					ObjectDialogBuilder.this.value = t;
				}
			};
			toDisplay = VirtualFieldWrapperTypeInfo.wrap(swingRenderer.getReflectionUI(), valueAccessor, fieldName,
					getTitle(), getOnly);
		}

		objectForm = swingRenderer.createObjectForm(toDisplay, infoSettings);
		delegate.setContentComponent(objectForm);

		List<Component> toolbarControls = new ArrayList<Component>();
		List<Component> commonToolbarControls = swingRenderer.createCommonToolbarControls(objectForm);
		if (commonToolbarControls != null) {
			toolbarControls.addAll(commonToolbarControls);
		}
		if (additionalToolbarComponents != null) {
			toolbarControls.addAll(additionalToolbarComponents);
		}
		if (cancellable) {
			List<JButton> okCancelButtons = delegate.createStandardOKCancelDialogButtons();
			toolbarControls.addAll(okCancelButtons);
		} else {
			toolbarControls.add(delegate.createDialogClosingButton("Close", null));
		}
		delegate.setToolbarComponents(toolbarControls);

		delegate.setWhenClosing(new Runnable() {			
			@Override
			public void run() {
				if(cancellable && !isOkPressed()){
					ModificationStack modificationStack = getModificationStack();
					if (modificationStack != null) {
						if (!modificationStack.isInvalidated()) {
							if (modificationStack.getNumberOfUndoUnits() > 0) {
								modificationStack.undoAll();
							}
						}
					}
				}
			}
		});
		
		return delegate.build();
	}

}
