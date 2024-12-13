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
// Copyright (c) 2003-2009, Jodd Team (jodd.org). All Rights Reserved.

/**
 * Checks whether a string matches a given wildcard pattern. Possible patterns
 * allow to match single characters ('?') or any count of characters ('*').
 * Wildcard characters can be escaped (by an '\').
 * <p>
 * This method uses recursive matching, as in linux or windows. regexp works the
 * same. This method is very fast, comparing to similar implementations.
 */
public class Wildcard {

	/**
	 * Checks whether a string matches a given wildcard pattern.
	 *
	 * @param string
	 *            input string
	 * @param pattern
	 *            pattern to match
	 * @return <code>true</code> if string matches the pattern, otherwise
	 *         <code>false</code>
	 */
	public static boolean match(String string, String pattern) {
		return match(string, pattern, 0, 0);
	}

	/**
	 * Checks whether a string matches a given wildcard pattern.
	 *
	 * @param string
	 *            input string
	 * @param pattern
	 *            pattern to match
	 * @param ignoreCase
	 *            whether the case must be ignored
	 * @param partially
	 *            whether matching a segment of the string is enough
	 * @return <code>true</code> if string matches the pattern, otherwise
	 *         <code>false</code>
	 */
	public static boolean match(String string, String pattern, boolean ignoreCase, boolean partially) {
		if(ignoreCase) {
			string = string.toLowerCase();
			pattern = pattern.toLowerCase();
		}
		if(partially) {
			pattern = "*" + pattern + "*";
		}
		return match(string, pattern, 0, 0);
	}

	/**
	 * Checks if two strings are equals or if they {@link #match(String, String)}.
	 * Useful for cases when matching a lot of equal strings and speed is important.
	 */
	public static boolean equalsOrMatch(String string, String pattern) {
		if (string.equals(pattern) == true) {
			return true;
		}
		return match(string, pattern, 0, 0);
	}

	/**
	 * Internal matching recursive function.
	 */
	private static boolean match(String string, String pattern, int stringStartNdx, int patternStartNdx) {
		int pNdx = patternStartNdx;
		int sNdx = stringStartNdx;
		int pLen = pattern.length();
		if (pLen == 1) {
			if (pattern.charAt(0) == '*') { // speed-up
				return true;
			}
		}
		int sLen = string.length();
		boolean nextIsNotWildcard = false;

		while (true) {

			// check if end of string and/or pattern occurred
			if ((sNdx >= sLen) == true) { // end of string still may have pending '*' in pattern
				while ((pNdx < pLen) && (pattern.charAt(pNdx) == '*')) {
					pNdx++;
				}
				return pNdx >= pLen;
			}
			if (pNdx >= pLen) { // end of pattern, but not end of the string
				return false;
			}
			char p = pattern.charAt(pNdx); // pattern char

			// perform logic
			if (nextIsNotWildcard == false) {

				if (p == '\\') {
					pNdx++;
					nextIsNotWildcard = true;
					continue;
				}
				if (p == '?') {
					sNdx++;
					pNdx++;
					continue;
				}
				if (p == '*') {
					char pnext = 0; // next pattern char
					if (pNdx + 1 < pLen) {
						pnext = pattern.charAt(pNdx + 1);
					}
					if (pnext == '*') { // double '*' have the same effect as one '*'
						pNdx++;
						continue;
					}
					int i;
					pNdx++;

					// find recursively if there is any substring from the end of the
					// line that matches the rest of the pattern !!!
					for (i = string.length(); i >= sNdx; i--) {
						if (match(string, pattern, i, pNdx) == true) {
							return true;
						}
					}
					return false;
				}
			} else {
				nextIsNotWildcard = false;
			}

			// check if pattern char and string char are equals
			if (p != string.charAt(sNdx)) {
				return false;
			}

			// everything matches for now, continue
			sNdx++;
			pNdx++;
		}
	}

	// ---------------------------------------------------------------- utilities

	/**
	 * Matches string to at least one pattern. Returns index of matched pattern, or
	 * <code>-1</code> otherwise.
	 * 
	 * @see #match(String, String)
	 */
	public static int matchOne(String src, String[] patterns) {
		for (int i = 0; i < patterns.length; i++) {
			if (match(src, patterns[i]) == true) {
				return i;
			}
		}
		return -1;
	}

}
