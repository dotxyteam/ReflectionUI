package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class ObjectDialogBuilder {

	protected boolean getOnly;
	protected IInfoCollectionSettings infoSettings = IInfoCollectionSettings.DEFAULT;
	protected List<Component> additionalToolbarComponents;

	protected DialogBuilder delegate;
	protected SwingRenderer swingRenderer;
	protected Object initialValue;
	protected Object value;
	protected JPanel objectForm;
	protected boolean cancellable = false;

	public ObjectDialogBuilder(SwingRenderer swingRenderer, Component ownerComponent, Object value) {
		this.swingRenderer = swingRenderer;
		this.initialValue = this.value = value;
		delegate = new DialogBuilder(swingRenderer, ownerComponent);

		setTitle(swingRenderer.getObjectTitle(value));
		setIconImage(swingRenderer.getObjectIconImage(value));
	}

	public Object getValue() {
		return value;
	}
	


	public boolean isValueNew() {
		return initialValue !=  value;
	}

	protected Object getDisplayValue() {
		ITypeInfo valueType = getValueType();
		if (SwingRendererUtils.hasCustomControl(value, valueType, swingRenderer)) {
			String fieldCaption = swingRenderer.getDefaultFieldCaption(value);
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
			EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
					valueType);
			encapsulation.setCaption(getTitle());
			encapsulation.setFieldCaption(fieldCaption);
			encapsulation.setFieldGetOnly(getOnly);
			encapsulation.setFieldNullable(false);
			return encapsulation.getInstance(valueAccessor);
		} else {
			return value;
		}
	}

	protected ITypeInfo getValueType() {
		return swingRenderer.getReflectionUI().getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(value));
	}

	protected ITypeInfo getDisplayValueType() {
		Object displayValue = getDisplayValue();
		return swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(displayValue));
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

	public JDialog getBuiltDialog() {
		return delegate.getBuiltDialog();
	}

	public boolean isOkPressed() {
		return delegate.isOkPressed();
	}

	public Component getOwnerComponent() {
		return delegate.getOwnerComponent();
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

	public JDialog build() {
		Object displayValue = getDisplayValue();
		objectForm = swingRenderer.createObjectForm(displayValue, infoSettings);
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

		return delegate.build();
	}

}
