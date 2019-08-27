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
package xy.reflect.ui.info.type.iterable.item;

import java.io.Serializable;

/**
 * Allows to describe preferences about items display of list types.
 * 
 * @author olitank
 *
 */
public interface IListItemDetailsAccessMode extends Serializable {

	boolean hasDetailsDisplayOption();

	boolean hasDetailsDisplayArea();

	ItemDetailsAreaPosition getDetailsAreaPosition();

	double getDefaultDetailsAreaOccupationRatio();

}
