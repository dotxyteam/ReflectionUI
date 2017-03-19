package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class ObjectDialogBuilder {

	protected SwingRenderer swingRenderer;
	protected DialogBuilder delegate;
	protected Object initialValue;
	protected Object value;
	protected JPanel objectForm;
	protected boolean cancellable;
	protected String customCancelCaption;
	protected String customOKCaption;
	protected List<Component> additionalToolbarComponents;

	public ObjectDialogBuilder(SwingRenderer swingRenderer, Component ownerComponent, Object value) {
		this.swingRenderer = swingRenderer;
		this.delegate = createDelegateDialogBuilder(ownerComponent);
		this.initialValue = this.value = value;
		this.cancellable = getEncapsulatedValueType().isModificationStackAccessible();

		setTitle(swingRenderer.getObjectTitle(value));
		setIconImage(swingRenderer.getObjectIconImage(value));
	}

	protected DialogBuilder createDelegateDialogBuilder(Component ownerComponent) {
		return new DialogBuilder(swingRenderer, ownerComponent);
	}

	public Object getValue() {
		return value;
	}

	public boolean isValueNew() {
		return initialValue != value;
	}

	protected Object getEncapsulatedValue() {
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
		return getEncapsulation().getInstance(valueAccessor);
	}

	public EncapsulatedObjectFactory getEncapsulation() {
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
				getValueType());
		result.setTypeCaption(getTitle());
		result.setFieldCaption(BooleanTypeInfo.isCompatibleWith(value.getClass()) ? "Is True" : "");
		result.setFieldNullable(false);
		result.setFieldValueReturnMode(ValueReturnMode.SELF_OR_PROXY);;
		Map<String, Object> properties = new HashMap<String, Object>();
		{
			DesktopSpecificProperty.setSubFormExpanded(properties, true);
			result.setFieldSpecificProperties(properties);
		}
		return result;
	}

	protected ITypeInfo getValueType() {
		return swingRenderer.getReflectionUI().getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(value));
	}

	protected ITypeInfo getEncapsulatedValueType() {
		Object displayValue = getEncapsulatedValue();
		return swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(displayValue));
	}

	public String getCustomCancelCaption() {
		return customCancelCaption;
	}

	public void setCustomCancelCaption(String customCancelCaption) {
		this.customCancelCaption = customCancelCaption;
	}

	public String getCustomOKCaption() {
		return customOKCaption;
	}

	public void setCustomOKCaption(String customOKCaption) {
		this.customOKCaption = customOKCaption;
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

	public boolean wasOkPressed() {
		return delegate.wasOkPressed();
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
		Object displayValue = getEncapsulatedValue();
		objectForm = swingRenderer.createForm(displayValue);
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
			List<JButton> okCancelButtons = delegate.createStandardOKCancelDialogButtons(customOKCaption,
					customCancelCaption);
			toolbarControls.addAll(okCancelButtons);
		} else {
			toolbarControls.add(delegate.createDialogClosingButton("Close", null));
		}
		delegate.setToolbarComponents(toolbarControls);

		return delegate.build();
	}

}
