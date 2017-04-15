package xy.reflect.ui.control.swing.editor;

import java.awt.Component;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;

public class StandardEditorBuilder extends AbstractEditorBuilder {

	protected SwingRenderer swingRenderer;
	protected Component ownerComponent;
	protected Object rootObject;
	protected ITypeInfo rootObjectType;

	public StandardEditorBuilder(SwingRenderer swingRenderer, Component ownerComponent, Object rootObject) {
		this.swingRenderer = swingRenderer;
		this.ownerComponent = ownerComponent;
		this.rootObject = rootObject;
		if (rootObject == null) {
			throw new ReflectionUIError();
		}
		this.rootObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(rootObject));
	}

	@Override
	public String getContextIdentifier() {
		return null;
	}

	@Override
	public String getSubContextIdentifier() {
		return null;
	}

	@Override
	public boolean isCancellable() {
		return false;
	}

	@Override
	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	@Override
	public Component getOwnerComponent() {
		return ownerComponent;
	}

	@Override
	public ModificationStack getParentObjectModificationStack() {
		return null;
	}

	@Override
	public IInfo getCumulatedModificationsTarget() {
		return null;
	}

	@Override
	public String getCumulatedModificationsTitle() {
		return null;
	}

	@Override
	public boolean canCommit() {
		return false;
	}

	@Override
	public IModification createCommitModification(Object newObjectValue) {
		return null;
	}

	@Override
	public ITypeInfo getObjectDeclaredType() {
		return null;
	}

	@Override
	public ValueReturnMode getObjectValueReturnMode() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	@Override
	public boolean isObjectValueNullable() {
		return false;
	}

	@Override
	public Object getInitialObjectValue() {
		return rootObject;
	}

	@Override
	public IInfoFilter getObjectFormFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public boolean isObjectFormExpanded() {
		return true;
	}

}
