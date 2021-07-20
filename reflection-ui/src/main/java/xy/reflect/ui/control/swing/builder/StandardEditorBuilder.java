


package xy.reflect.ui.control.swing.builder;

import java.awt.Component;

import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * This is a standard editor window factory class.
 * 
 * @author olitank
 *
 */
public class StandardEditorBuilder extends AbstractEditorBuilder {

	protected SwingRenderer swingRenderer;
	protected Component ownerComponent;
	protected Object rootObject;

	/**
	 * Constructs a standard editor window builder.
	 * 
	 * @param swingRenderer  The renderer object used to generate the controls.
	 * @param ownerComponent the component that will own the editor dialog.
	 * @param rootObject     The local object that will be viewed/modified by the
	 *                       editor window.
	 */
	public StandardEditorBuilder(SwingRenderer swingRenderer, Component ownerComponent, Object rootObject) {
		this.swingRenderer = swingRenderer;
		this.ownerComponent = ownerComponent;
		this.rootObject = rootObject;
		if (rootObject == null) {
			throw new ReflectionUIError();
		}
	}

	public Object getRootObject() {
		return rootObject;
	}

	@Override
	protected IContext getContext() {
		return null;
	}

	@Override
	protected IContext getSubContext() {
		return null;
	}

	@Override
	protected boolean isDialogCancellable() {
		return false;
	}

	@Override
	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	@Override
	protected Component getOwnerComponent() {
		return ownerComponent;
	}

	@Override
	protected ModificationStack getParentModificationStack() {
		return null;
	}

	@Override
	protected String getParentModificationTitle() {
		return null;
	}

	@Override
	protected boolean isParentModificationFake() {
		return false;
	}

	@Override
	protected boolean canCommitToParent() {
		return false;
	}

	@Override
	protected IModification createCommittingModification(Object newObjectValue) {
		return null;
	}

	@Override
	protected void handleRealtimeLinkCommitException(Throwable t) {
		throw new ReflectionUIError();
	}

	@Override
	protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
		return null;
	}

	@Override
	protected ValueReturnMode getReturnModeFromParent() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	@Override
	protected boolean isNullValueDistinct() {
		return false;
	}

	@Override
	protected Object loadValue() {
		return rootObject;
	}

	@Override
	protected IInfoFilter getEncapsulatedFormFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	protected boolean isEncapsulatedFormEmbedded() {
		return true;
	}

}
