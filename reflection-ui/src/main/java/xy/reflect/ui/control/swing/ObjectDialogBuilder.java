package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class ObjectDialogBuilder {

	protected boolean getOnly = false;
	protected IInfoFilter infoFilter = IInfoFilter.DEFAULT;
	protected List<Component> additionalToolbarComponents;

	protected DialogBuilder delegate;
	protected SwingRenderer swingRenderer;
	protected Object initialValue;
	protected Object value;
	protected JPanel objectForm;
	protected boolean cancellable = false;
	protected String customCancelCaption;
	protected String customOKCaption;

	public ObjectDialogBuilder(SwingRenderer swingRenderer, Component ownerComponent, Object value) {
		this.swingRenderer = swingRenderer;
		this.initialValue = this.value = value;
		delegate = createDelegateDialogBuilder(ownerComponent);

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

	protected Object getDisplayValue() {
		if (isValueEncapsulatedForDisplay()) {
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
					getValueType());
			encapsulation.setTypeCaption(getTitle());
			encapsulation.setFieldCaption(BooleanTypeInfo.isCompatibleWith(value.getClass()) ? "Is True" : "");
			encapsulation.setFieldGetOnly(getOnly);
			encapsulation.setFieldNullable(false);
			return encapsulation.getInstance(valueAccessor);
		} else {
			return value;
		}
	}

	protected boolean isValueEncapsulatedForDisplay() {
		return SwingRendererUtils.hasCustomControl(value, getValueType(), swingRenderer);
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

	public IInfoFilter getInfoFilter() {
		return infoFilter;
	}

	public void setInfoFilter(IInfoFilter infoFilter) {
		this.infoFilter = infoFilter;
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
		if (isValueEncapsulatedForDisplay()) {
			objectForm = swingRenderer.createForm(displayValue);
		} else {
			objectForm = swingRenderer.createForm(displayValue, infoFilter);
		}
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
