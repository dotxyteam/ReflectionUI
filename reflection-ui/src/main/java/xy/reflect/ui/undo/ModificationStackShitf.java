/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.undo;

public class ModificationStackShitf extends AbstractModification {

	protected ModificationStack modificationStack;
	protected int offset;
	protected String title;

	public ModificationStackShitf(ModificationStack modificationStack, int offset, String title) {
		this.modificationStack = modificationStack;
		this.offset = offset;
		this.title = title;
	}

	@Override
	protected Runnable createDoJob() {
		return new Runnable() {
			@Override
			public void run() {
				if (offset > 0) {
					for (int i = 0; i < offset; i++) {
						shiftForeward();
					}
				} else {
					for (int i = 0; i < (-offset); i++) {
						shiftBackward();
					}
				}
			}
		};
	}

	@Override
	protected Runnable createUndoJob() {
		return new Runnable() {
			@Override
			public void run() {
				if (offset > 0) {
					for (int i = 0; i < offset; i++) {
						shiftBackward();
					}
				} else {
					for (int i = 0; i < (-offset); i++) {
						shiftForeward();
					}
				}
			}
		};
	}

	protected void shiftBackward() {
		modificationStack.undo();
	}

	protected void shiftForeward() {
		modificationStack.redo();
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public String getTitle() {
		return title;
	}

}
