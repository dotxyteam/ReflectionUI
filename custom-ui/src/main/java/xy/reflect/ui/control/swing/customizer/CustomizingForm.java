
package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizationsFactory;

/**
 * This is a sub-class of {@link Form} that allows to install customization
 * tools.
 * 
 * @author olitank
 *
 */
public class CustomizingForm extends Form {
	private static final long serialVersionUID = 1L;

	protected boolean toolsAdded;

	public CustomizingForm(SwingCustomizer swingRenderer, Object object, IInfoFilter infoFilter) {
		super(swingRenderer, object, infoFilter);
		if (getSwingRenderer().isCustomizationsEditorEnabled()) {
			addAncestorListener(new AncestorListener() {

				@Override
				public void ancestorRemoved(AncestorEvent event) {
					getSwingRenderer().getCustomizationController().formRemoved(CustomizingForm.this);
				}

				@Override
				public void ancestorMoved(AncestorEvent event) {
				}

				@Override
				public void ancestorAdded(AncestorEvent event) {
					getSwingRenderer().getCustomizationController().formAdded(CustomizingForm.this);
				}
			});
		}
	}

	@Override
	public SwingCustomizer getSwingRenderer() {
		return (SwingCustomizer) super.getSwingRenderer();
	}

	/**
	 * @param object The object to inspect.
	 * @return whether customizations are enabled for the specified object or not.
	 */
	public boolean areCustomizationsEditable(Object object) {
		if (!getSwingRenderer().isCustomizationsEditorEnabled()) {
			return false;
		}
		if (!getSwingRenderer().getCustomizationOptions().isInEditMode()) {
			return false;
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo objectType = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (!InfoCustomizationsFactory.areCustomizationsActive(getSwingRenderer().getInfoCustomizations(),
				objectType.getSpecificProperties())) {
			return false;
		}
		if (Boolean.TRUE.equals(
				objectType.getSpecificProperties().get(SwingCustomizer.CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY))) {
			return false;
		}
		return true;
	}

	/**
	 * @return whether customization tools are currently installed on the form or
	 *         not.
	 */
	public boolean isToolsAdded() {
		return toolsAdded;
	}

	@Override
	public void layoutMembersControls(
			Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory,
			JPanel membersPanel) {
		if (areCustomizationsEditable(object)) {
			membersPanel.setLayout(new BorderLayout());
			JPanel newMembersPanel = new ControlPanel();
			{
				membersPanel.add(newMembersPanel, BorderLayout.CENTER);
				Border newMembersPanelBorder;
				{
					int borderThickness = 2;
					newMembersPanelBorder = BorderFactory.createLineBorder(
							getSwingRenderer().getCustomizationTools().getToolsRenderer().getToolsForegroundColor(),
							borderThickness);
					newMembersPanelBorder = BorderFactory.createCompoundBorder(newMembersPanelBorder,
							BorderFactory.createLineBorder(getSwingRenderer().getCustomizationTools().getToolsRenderer()
									.getToolsBackgroundColor(), borderThickness));
					newMembersPanelBorder = BorderFactory.createCompoundBorder(newMembersPanelBorder,
							BorderFactory.createLineBorder(getSwingRenderer().getCustomizationTools().getToolsRenderer()
									.getToolsForegroundColor(), borderThickness));
					newMembersPanel.setBorder(newMembersPanelBorder);
				}
				super.layoutMembersControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
						newMembersPanel);
			}
			JPanel typeCustomizationsControl = new ControlPanel();
			{
				typeCustomizationsControl.setLayout(new BorderLayout());
				typeCustomizationsControl.add(getSwingRenderer().getCustomizationTools().makeButtonForType(object),
						BorderLayout.CENTER);
				membersPanel.add(SwingRendererUtils.flowInLayout(typeCustomizationsControl, GridBagConstraints.CENTER),
						BorderLayout.NORTH);
			}
			toolsAdded = true;
		} else {
			super.layoutMembersControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
					membersPanel);
			toolsAdded = false;
		}
	}

	@Override
	public void refresh(boolean refreshStructure) {
		if (areCustomizationsEditable(object) != toolsAdded) {
			objectType = null;
			super.refresh(true);
		} else {
			super.refresh(refreshStructure);
		}
	}

	@Override
	public CustomizingFieldControlPlaceHolder createFieldControlPlaceHolder(IFieldInfo field) {
		return new CustomizingFieldControlPlaceHolder(this, field);
	}

	@Override
	public CustomizingMethodControlPlaceHolder createMethodControlPlaceHolder(IMethodInfo method) {
		return new CustomizingMethodControlPlaceHolder(this, method);
	}

}