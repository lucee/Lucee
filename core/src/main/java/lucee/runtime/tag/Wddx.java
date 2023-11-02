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
package lucee.runtime.tag;

import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;

import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSConverter;
import lucee.runtime.converter.WDDXConverter;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;

/**
 * Serializes and de-serializes CFML data structures to the XML-based WDDX format. Generates
 * JavaScript statements to instantiate JavaScript objects equivalent to the contents of a WDDX
 * packet or some CFML data structures.
 *
 *
 *
 **/
public final class Wddx extends TagImpl {

	/** The value to be processed. */
	private Object input;

	/** Specifies the action taken by the cfwddx tag. */
	private String action;

	/**
	 * The name of the variable to hold the output of the operation. This attribute is required for
	 ** action = 'WDDX2CFML'. For all other actions, if this attribute is not provided, the result of the
	 ** WDDX processing is outputted in the HTML stream.
	 */
	private String output;

	private boolean validate;

	/**
	 * The name of the top-level JavaScript object created by the deserialization process. The object
	 ** created is an instance of the WddxRecordset object, explained in WddxRecordset Object.
	 */
	private String toplevelvariable;

	/**
	 * Indicates whether to output time-zone information when serializing CFML to WDDX. If time-zone
	 ** information is taken into account, the hour-minute offset, as represented in the ISO8601 format,
	 * is calculated in the date-time output. If time-zone information is not taken into account, the
	 * local time is output. The default is Yes.
	 */
	private boolean usetimezoneinfo;

	private boolean xmlConform;

	@Override
	public void release() {
		super.release();
		input = null;
		action = null;
		output = null;
		validate = false;
		toplevelvariable = null;
		usetimezoneinfo = false;
		xmlConform = false;
	}

	/**
	 * set the value input The value to be processed.
	 * 
	 * @param input value to set
	 **/
	public void setInput(Object input) {
		this.input = input;
	}

	/**
	 * set the value action Specifies the action taken by the cfwddx tag.
	 * 
	 * @param action value to set
	 **/
	public void setAction(String action) {
		this.action = action.toLowerCase();
	}

	/**
	 * set the value output The name of the variable to hold the output of the operation. This attribute
	 * is required for action = 'WDDX2CFML'. For all other actions, if this attribute is not provided,
	 * the result of the WDDX processing is outputted in the HTML stream.
	 * 
	 * @param output value to set
	 **/
	public void setOutput(String output) {
		this.output = output;
	}

	/**
	 * set the value validate
	 * 
	 * @param validate value to set
	 **/
	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	/**
	 * set the value toplevelvariable The name of the top-level JavaScript object created by the
	 * deserialization process. The object created is an instance of the WddxRecordset object, explained
	 * in WddxRecordset Object.
	 * 
	 * @param toplevelvariable value to set
	 **/
	public void setToplevelvariable(String toplevelvariable) {
		this.toplevelvariable = toplevelvariable;
	}

	/**
	 * set the value usetimezoneinfo Indicates whether to output time-zone information when serializing
	 * CFML to WDDX. If time-zone information is taken into account, the hour-minute offset, as
	 * represented in the ISO8601 format, is calculated in the date-time output. If time-zone
	 * information is not taken into account, the local time is output. The default is Yes.
	 * 
	 * @param usetimezoneinfo value to set
	 **/
	public void setUsetimezoneinfo(boolean usetimezoneinfo) {
		this.usetimezoneinfo = usetimezoneinfo;
	}

	/**
	 * sets if generated code is xml or wddx conform
	 * 
	 * @param xmlConform
	 */
	public void setXmlconform(boolean xmlConform) {
		this.xmlConform = xmlConform;
	}

	@Override
	public int doStartTag() throws PageException {
		try {
			doIt();

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return SKIP_BODY;
	}

	private void doIt() throws ExpressionException, PageException, ConverterException, IOException, FactoryConfigurationError {
		// cfml > wddx
		if (action.equals("cfml2wddx")) {
			if (output != null) pageContext.setVariable(output, cfml2wddx(input));
			else pageContext.forceWrite(cfml2wddx(input));
		}

		// wddx > cfml
		else if (action.equals("wddx2cfml")) {
			if (output == null) throw new ApplicationException("at tag cfwddx the attribute output is required if you set action==wddx2cfml");
			pageContext.setVariable(output, wddx2cfml(Caster.toString(input)));
		}

		// cfml > js
		else if (action.equals("cfml2js")) {
			if (output != null) pageContext.setVariable(output, cfml2js(input));
			else pageContext.forceWrite(cfml2js(input));
		}

		// wddx > js
		else if (action.equals("wddx2js")) {
			if (output != null) pageContext.setVariable(output, wddx2js(Caster.toString(input)));
			else pageContext.forceWrite(wddx2js(Caster.toString(input)));
		}

		else throw new ExpressionException("invalid attribute action for tag cfwddx, attributes are [cfml2wddx, wddx2cfml,cfml2js, wddx2js].");

	}

	private String cfml2wddx(Object input) throws ConverterException {
		WDDXConverter converter = new WDDXConverter(pageContext.getTimeZone(), xmlConform, true);
		if (!usetimezoneinfo) converter.setTimeZone(null);
		return converter.serialize(input);
	}

	private Object wddx2cfml(String input) throws ConverterException, IOException, FactoryConfigurationError {
		WDDXConverter converter = new WDDXConverter(pageContext.getTimeZone(), xmlConform, true);
		converter.setTimeZone(pageContext.getTimeZone());
		return converter.deserialize(input, validate);
	}

	private String cfml2js(Object input) throws ConverterException, ApplicationException {
		if (toplevelvariable == null) throw missingTopLevelVariable();
		JSConverter converter = new JSConverter();
		return converter.serialize(input, toplevelvariable);
	}

	private String wddx2js(String input) throws ConverterException, IOException, FactoryConfigurationError, ApplicationException {
		if (toplevelvariable == null) throw missingTopLevelVariable();
		JSConverter converter = new JSConverter();
		return converter.serialize(wddx2cfml(input), toplevelvariable);
	}

	private ApplicationException missingTopLevelVariable() {
		return new ApplicationException("at tag cfwddx the attribute topLevelVariable is required if you set action equal wddx2js or cfml2js");
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}