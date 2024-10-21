/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package org.apache.taglibs.datetime;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import lucee.commons.i18n.FormatUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;

public final class FormatTag extends BodyTagSupport {

	// format tag attributes

	// Optional attribute, use users locale if known when formatting date
	private boolean locale_flag = false;
	// Optional attribute, time pattern string to use when formatting date
	private String pattern = null;
	// Optional attribute, name of script variable to use as pattern
	private String patternid = null;
	// Optional attribute, timeZone script variable id to use when formatting date
	private String timeZone_string;
	// Optional attribute, date object from rtexprvalue
	private Date date = null;
	// Optional attribute, the default text if the tag body or date given is invalid/null
	private String default_text = "Invalid Date";
	// Optional attribute, the name of an attribute which contains the Locale
	private String localeRef = null;
	// Optional attribute, name of script variable to use as date symbols source
	private String symbolsRef = null;

	// format tag invocation variables

	// The date to be formatted an output by tag
	private Date output_date = null;

	/**
	 * Method called at start of tag, always returns EVAL_BODY_TAG
	 *
	 * @return EVAL_BODY_TAG
	 */
	@Override
	public final int doStartTag() throws PageException {
		output_date = date;
		return EVAL_BODY_TAG;
	}

	/**
	 * Method called at end of format tag body.
	 *
	 * @return SKIP_BODY
	 */
	@Override
	public final int doAfterBody() throws PageException {
		// Use the body of the tag as input for the date
		BodyContent body = getBodyContent();
		String s = body.getString().trim();
		// Clear the body since we will output only the formatted date
		body.clearBody();
		if (output_date == null) {
			long time;
			try {
				time = Long.valueOf(s).longValue();
				output_date = new Date(time);
			}
			catch (NumberFormatException nfe) {
			}
		}

		return SKIP_BODY;
	}

	/**
	 * Method called at end of Tag
	 *
	 * @return EVAL_PAGE
	 */
	@Override
	public final int doEndTag() throws PageException {
		String date_formatted = default_text;

		if (output_date != null) {
			// Get the pattern to use
			DateTimeFormatter formatter;
			String pat = pattern;

			if (pat == null && patternid != null) {
				Object attr = pageContext.findAttribute(patternid);
				if (attr != null) pat = attr.toString();
			}

			if (pat == null) {
				pat = new SimpleDateFormat().toPattern(); // TODO find a better way for this
				formatter = FormatUtil.getDateTimeFormatter(null, pat);
			}

			// Get a DateFormatSymbols
			if (symbolsRef != null) {
				throw new ApplicationException("symbols are no longer suported"); // TODO can we do that?
			}

			// Get a SimpleDateFormat using locale if necessary
			if (localeRef != null) {
				Locale locale = (Locale) pageContext.findAttribute(localeRef);
				if (locale == null) {
					throw new ApplicationException("datetime format tag could not find locale for localeRef \"" + localeRef + "\".");
				}
				formatter = FormatUtil.getDateTimeFormatter(locale, pat);
			}
			else if (locale_flag) {
				formatter = FormatUtil.getDateTimeFormatter(pageContext.getRequest().getLocale(), pat);
			}
			else {
				formatter = FormatUtil.getDateTimeFormatter(null, pat);
			}

			// See if there is a timeZone
			TimeZone timeZone = null;
			if (timeZone_string != null) {
				timeZone = (TimeZone) pageContext.getAttribute(timeZone_string, PageContext.SESSION_SCOPE);
				if (timeZone == null) {
					throw new ApplicationException("Datetime format tag timeZone " + "script variable \"" + timeZone_string + " \" does not exist");
				}

			}
			else {

			}
			// Format the date for display
			date_formatted = FormatUtil.format(formatter, output_date, timeZone);
		}

		try

		{
			pageContext.getOut().write(date_formatted);
		}
		catch (Exception e) {
			throw new ApplicationException("IO Error: " + e.getMessage());
		}

		return EVAL_PAGE;
	}

	@Override
	public void release() {
		// lucee.print.ln("release FormatTag");
		super.release();
		locale_flag = false;
		pattern = null;
		patternid = null;
		date = null;
		localeRef = null;
		symbolsRef = null;
	}

	/**
	 * Locale flag, if set to true, format date for client's preferred locale if known.
	 *
	 * @param boolean use users locale, true or false
	 */
	public final void setLocale(short flag) {
		// locale_flag = flag;
	}

	/**
	 * Set the time zone to use when formatting date.
	 *
	 * Value must be the name of a <b>timeZone</b> tag script variable ID.
	 *
	 * @param String name of timeZone to use
	 */
	public final void setTimeZone(String tz) {
		timeZone_string = tz;
	}

	/**
	 * Set the pattern to use when formatting Date.
	 *
	 * @param String SimpleDateFormat style time pattern format string
	 */
	public final void setPattern(String str) {
		pattern = str;
	}

	/**
	 * Set the pattern to use when parsing Date using a script variable attribute.
	 * 
	 * @param String name of script variable attribute id
	 */
	public final void setPatternId(String str) {
		patternid = str;
	}

	/**
	 * Set the date to use (overrides tag body) for formatting
	 *
	 * @param Date to use for formatting (could be null)
	 */
	public final void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Set the default text if an invalid date or no tag body is given
	 *
	 * @param String to use as default text
	 */
	public final void setDefault(String default_text) {
		this.default_text = default_text;
	}

	/**
	 * Provides a key to search the page context for in order to get the java.util.Locale to use.
	 *
	 * @param String name of locale attribute to use
	 */
	public void setLocaleRef(String value) {
		localeRef = value;
	}

	/**
	 * Provides a key to search the page context for in order to get the java.text.DateFormatSymbols to
	 * use
	 *
	 * @param symbolsRef
	 */
	public void setSymbolsRef(String symbolsRef) {
		this.symbolsRef = symbolsRef;
	}

}