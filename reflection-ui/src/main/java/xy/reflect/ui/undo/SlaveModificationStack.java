/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.undo;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.ReflectionUIUtils;

public class SlaveModificationStack extends ModificationStack {

	protected Accessor<Boolean> valueModifAcceptedGetter;
	protected Accessor<ValueReturnMode> valueReturnModeGetter;
	protected Accessor<Boolean> valueReplacedGetter;
	protected Accessor<String> masterModificationTitleGetter;
	protected Accessor<ModificationStack> masterModificationStackGetter;
	protected Accessor<IModification> committingModificationGetter;
	protected boolean exclusiveLinkWithParent;
	protected Listener<String> debugLogListener;

	public SlaveModificationStack(String name, Accessor<Boolean> valueModifAcceptedGetter,
			Accessor<ValueReturnMode> valueReturnModeGetter, Accessor<Boolean> valueReplacedGetter,
			Accessor<IModification> committingModificationGetter, Accessor<String> masterModificationTitleGetter,
			Accessor<ModificationStack> masterModificationStackGetter, boolean exclusiveLinkWithParent,
			Listener<String> debugLogListener) {
		super(name);
		this.valueModifAcceptedGetter = valueModifAcceptedGetter;
		this.valueReturnModeGetter = valueReturnModeGetter;
		this.valueReplacedGetter = valueReplacedGetter;
		this.committingModificationGetter = committingModificationGetter;
		this.masterModificationTitleGetter = masterModificationTitleGetter;
		this.masterModificationStackGetter = masterModificationStackGetter;
		this.exclusiveLinkWithParent = exclusiveLinkWithParent;
		this.debugLogListener = debugLogListener;
	}

	@Override
	public boolean pushUndo(final IModification undoModif) {
		if (undoModif.isNull()) {
			return false;
		}
		boolean result = super.pushUndo(undoModif);
		if (isInComposite()) {
			return result;
		}
		ModificationStack valueModifStack;
		if (undoModif.isFake()) {
			valueModifStack = new ModificationStack(null) {

				@Override
				public boolean isNull() {
					return false;
				}

				@Override
				public IModification toCompositeUndoModification(String title) {
					return new AbstractModificationProxy(undoModif) {

						@Override
						public String getTitle() {
							return title;
						}

						@Override
						public IModification applyAndGetOpposite() {
							return this;
						}

					};
				}

			};
		} else {
			valueModifStack = new ModificationStack(null);
			valueModifStack.pushUndo(new ModificationStackShitf(this, -1, undoModif.getTitle()) {

				@Override
				protected void shiftBackward() {
					SlaveModificationStack.this.super_undo();
				}

				@Override
				protected void shiftForeward() {
					SlaveModificationStack.this.super_redo();
				}

			});
		}
		Boolean valueModifAccepted = valueModifAcceptedGetter.get();
		ValueReturnMode valueReturnMode = valueReturnModeGetter.get();
		boolean valueReplaced = valueReplacedGetter.get();
		IModification committingModif = committingModificationGetter.get();
		String modifTitle = AbstractModification.getUndoTitle(undoModif.getTitle());
		String modifTitlePrefix = masterModificationTitleGetter.get();
		if (modifTitlePrefix != null) {
			modifTitle = ReflectionUIUtils.composeMessage(modifTitlePrefix, modifTitle);
		}
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		return ReflectionUIUtils.finalizeSubModifications(parentObjectModifStack, valueModifStack, valueModifAccepted,
				valueReturnMode, valueReplaced, committingModif, modifTitle, debugLogListener);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		forwardInvalidation();
	}

	protected void forwardInvalidation() {
		ModificationStack valueModifStack = new ModificationStack(null);
		valueModifStack.invalidate();
		Boolean valueModifAccepted = valueModifAcceptedGetter.get();
		ValueReturnMode valueReturnMode = valueReturnModeGetter.get();
		boolean valueReplaced = valueReplacedGetter.get();
		IModification committingModif = committingModificationGetter.get();
		String parentObjectModifTitle = null;
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		ReflectionUIUtils.finalizeSubModifications(parentObjectModifStack, valueModifStack, valueModifAccepted,
				valueReturnMode, valueReplaced, committingModif, parentObjectModifTitle, debugLogListener);
	}

	@Override
	public void forget() {
		super.forget();
		if (exclusiveLinkWithParent) {
			ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
			parentObjectModifStack.forget();
		} else {
			forwardInvalidation();
		}
	}

	@Override
	public void undo() {
		super_undo();
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.invalidate();
	}

	@Override
	public void redo() {
		super_redo();
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.invalidate();
	}

	protected void super_undo() {
		super.undo();
	}

	protected void super_redo() {
		super.redo();
	}

	@Override
	public boolean isEventFiringEnabled() {
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		return parentObjectModifStack.isEventFiringEnabled();
	}

	@Override
	public void setEventFiringEnabled(boolean eventFiringEnabled) {
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.setEventFiringEnabled(eventFiringEnabled);
	}

	@Override
	public String toString() {
		return "Slave" + super.toString();
	}

}
