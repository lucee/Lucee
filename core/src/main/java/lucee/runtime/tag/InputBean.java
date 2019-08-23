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

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;

/**
 * 
 */
public final class InputBean {
	private short type = Input.TYPE_TEXT;
	private short validate = Input.VALIDATE_NONE;
	private String name;
	private boolean required;
	private String onValidate;
	private String onError;
	private String pattern;
	// private String passThrough;
	private double range_min = Double.NaN;
	private double range_max = Double.NaN;
	private String message;
	private int maxLength = -1;

	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message The message to set.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the onError.
	 */
	public String getOnError() {
		return onError;
	}

	/**
	 * @param onError The onError to set.
	 */
	public void setOnError(String onError) {
		this.onError = onError;
	}

	/**
	 * @return Returns the onValidate.
	 */
	public String getOnValidate() {
		return onValidate;
	}

	/**
	 * @param onValidate The onValidate to set.
	 */
	public void setOnValidate(String onValidate) {
		this.onValidate = onValidate;
	}

	/**
	 * @return Returns the pattern.
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern The pattern to set.
	 * @throws ExpressionException
	 */
	public void setPattern(String pattern) throws ExpressionException {
		// '
		if (StringUtil.startsWith(pattern, '\'')) {
			if (!StringUtil.endsWith(pattern, '\'')) throw new ExpressionException("invalid pattern definition [" + pattern + ", missing closing [']");
			pattern = pattern.substring(1, pattern.length() - 1);
		}
		// "
		if (StringUtil.startsWith(pattern, '"')) {
			if (!StringUtil.endsWith(pattern, '"')) throw new ExpressionException("invalid pattern definition [" + pattern + ", missing closing [\"]");
			pattern = pattern.substring(1, pattern.length() - 1);
		}

		if (!StringUtil.startsWith(pattern, '/')) pattern = "/".concat(pattern);
		if (!StringUtil.endsWith(pattern, '/')) pattern = pattern.concat("/");
		this.pattern = pattern;
	}

	/**
	 * @return Returns the range_max.
	 */
	public double getRangeMax() {
		return range_max;
	}

	/**
	 * @param range_max The range_max to set.
	 */
	public void setRangeMax(double range_max) {
		this.range_max = range_max;
	}

	/**
	 * @return Returns the range_min.
	 */
	public double getRangeMin() {
		return range_min;
	}

	/**
	 * @param range_min The range_min to set.
	 */
	public void setRangeMin(double range_min) {
		this.range_min = range_min;
	}

	/**
	 * @return Returns the required.
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required The required to set.
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * @return Returns the type.
	 */
	public short getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(short type) {
		this.type = type;
	}

	/**
	 * @return Returns the validate.
	 */
	public short getValidate() {
		return validate;
	}

	/**
	 * @param validate The validate to set.
	 */
	public void setValidate(short validate) {
		this.validate = validate;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMaxLength() {
		return maxLength;
	}
}