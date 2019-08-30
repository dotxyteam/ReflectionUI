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
package xy.reflect.ui.info.app;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;

public class DefaultApplicationInfo implements IApplicationInfo {

	@Override
	public String getName() {
		return "application.default";
	}

	@Override
	public String getCaption() {
		return "Application";
	}

	@Override
	public boolean isSystemIntegrationCrossPlatform() {
		return false;
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public ColorSpecification getMainBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainForegroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainEditorForegroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainEditorBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainBorderColor() {
		return null;
	}

	@Override
	public ResourcePath getMainBackgroundImagePath() {
		return null;
	}

	@Override
	public ColorSpecification getMainButtonBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainButtonForegroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainButtonBorderColor() {
		return null;
	}

	@Override
	public ResourcePath getMainButtonBackgroundImagePath() {
		return null;
	}

	@Override
	public ColorSpecification getTitleBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getTitleForegroundColor() {
		return null;
	}

}
