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
package xy.reflect.ui.util;

public class MoreSystemProperties extends SystemProperties {

	@Usage("If the value of this property is \"true\" then the UI customization tools will be hidden.")
	public static final String HIDE_INFO_CUSTOMIZATIONS_TOOLS = PREFIX + ".infoCustomizationsToolsHidden";
	@Usage("If the value of this property is set then the customizations that were specified for the UI customization tools will be editable and saved to the specified output file.")
	public static final String INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH = PREFIX
			+ ".customizationToolsCustomizationsFilePath";

	public static boolean areCustomizationToolsDisabled() {
		return System.getProperty(HIDE_INFO_CUSTOMIZATIONS_TOOLS, "false").equals("true");
	}

	public static String getInfoCustomizationToolsCustomizationsFilePath() {
		return System.getProperty(INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH);
	}

	public static boolean isInfoCustomizationToolsCustomizationAllowed() {
		return getInfoCustomizationToolsCustomizationsFilePath() != null;
	}

	public static String describe() {
		return describe(MoreSystemProperties.class);
	}

}
