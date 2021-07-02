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

/**
 * Modification that calls undo() or redo() on the given modification stack
 * according to the specified offset. If the offset is positive then +offset
 * redo() calls will be performed. Otherwise -offset undo() calls will be
 * performed.
 * 
 * @author olitank
 *
 */
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

	@Override
	public String toString() {
		return "ModificationStackShitf [modificationStack=" + modificationStack + ", offset=" + offset + ", title="
				+ title + "]";
	}
	
	
	

}
