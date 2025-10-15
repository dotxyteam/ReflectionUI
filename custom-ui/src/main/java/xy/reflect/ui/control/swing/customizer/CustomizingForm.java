
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
	protected JPanel newMembersPanel;
	protected JPanel typeCustomizationsControl;

	public CustomizingForm(SwingCustomizer swingCustomizer, Object object, IInfoFilter infoFilter) {
		super(swingCustomizer, object, infoFilter);
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
		ITypeInfo objectType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
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

	@Override
	protected void layoutMembersControlPlaceHolders(
			Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory,
			JPanel membersPanel) {
		membersPanel.setLayout(new BorderLayout());
		newMembersPanel = new ControlPanel();
		{
			membersPanel.add(newMembersPanel, BorderLayout.CENTER);
			super.layoutMembersControlPlaceHolders(fieldControlPlaceHoldersByCategory,
					methodControlPlaceHoldersByCategory, newMembersPanel);
		}
		typeCustomizationsControl = new ControlPanel();
		{
			typeCustomizationsControl.setLayout(new BorderLayout());
			membersPanel.add(SwingRendererUtils.flowInLayout(typeCustomizationsControl, GridBagConstraints.CENTER),
					BorderLayout.NORTH);
		}
	}

	protected void refreshTools() {
		if (areCustomizationsEditable(object)) {
			Border newMembersPanelBorder;
			{
				int borderThickness = 2;
				newMembersPanelBorder = BorderFactory.createLineBorder(
						getSwingRenderer().getCustomizationTools().getToolsRenderer().getToolsForegroundColor(),
						borderThickness);
				newMembersPanelBorder = BorderFactory.createCompoundBorder(newMembersPanelBorder,
						BorderFactory.createLineBorder(
								getSwingRenderer().getCustomizationTools().getToolsRenderer().getToolsBackgroundColor(),
								borderThickness));
				newMembersPanelBorder = BorderFactory.createCompoundBorder(newMembersPanelBorder,
						BorderFactory.createLineBorder(
								getSwingRenderer().getCustomizationTools().getToolsRenderer().getToolsForegroundColor(),
								borderThickness));
			}
			newMembersPanel.setBorder(newMembersPanelBorder);
			typeCustomizationsControl.add(getSwingRenderer().getCustomizationTools().makeButtonForType(object),
					BorderLayout.CENTER);
			typeCustomizationsControl.setVisible(true);
			toolsAdded = true;
		} else {
			newMembersPanel.setBorder(null);
			typeCustomizationsControl.removeAll();
			typeCustomizationsControl.setVisible(false);
			toolsAdded = false;
		}
	}

	@Override
	public void refresh(boolean refreshStructure) {
		if (areCustomizationsEditable(object) != toolsAdded) {
			super.refresh(true);
			refreshTools();
		} else {
			super.refresh(refreshStructure);
		}
	}

	@Override
	protected CustomizingFieldControlPlaceHolder createFieldControlPlaceHolder(IFieldInfo field) {
		return new CustomizingFieldControlPlaceHolder(this, field);
	}

	@Override
	protected CustomizingMethodControlPlaceHolder createMethodControlPlaceHolder(IMethodInfo method) {
		return new CustomizingMethodControlPlaceHolder(this, method);
	}

}