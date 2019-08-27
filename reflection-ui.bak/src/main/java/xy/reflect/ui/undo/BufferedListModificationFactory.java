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

import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.Mapper;

public class BufferedListModificationFactory extends ListModificationFactory {

	public BufferedListModificationFactory(BufferedItemPosition anyListItemPosition,
			Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
		super(anyListItemPosition, rootListValueCommitModificationAccessor);
	}

	@Override
	public IModification createListModification(ItemPosition itemPosition, Object[] newListRawValue) {
		return new BufferedListModification(itemPosition, newListRawValue, rootListValueCommitModificationAccessor);
	}

	public static class BufferedListModification extends ListModification {

		public BufferedListModification(ItemPosition itemPosition, Object[] newListRawValue,
				Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
			super(itemPosition, newListRawValue, rootListValueCommitModificationAccessor);
		}

		@Override
		public IModification applyAndGetOpposite() {
			ListModification opposite = (ListModification) super.applyAndGetOpposite();
			((BufferedItemPosition) itemPosition).refreshBranch();
			return new BufferedListModification(opposite.itemPosition, opposite.newListRawValue,
					opposite.rootListValueCommitModificationAccessor);
		}

	}

}
