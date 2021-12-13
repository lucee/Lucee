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

import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.op.Caster;

/**
 * Saves the generated content inside the tag body in a variable.
 *
 *
 *
 **/
public final class SaveContent extends BodyTagTryCatchFinallyImpl {

	/** The name of the variable in which to save the generated content inside the tag. */
	private String variable;
	private boolean trim;
    private boolean setTrimManually;
	private boolean append;

	@Override
	public void release() {
		super.release();
		variable = null;
		trim = false;
        setTrimManually = false;
		append = false;
	}

	/**
	 * set the value variable The name of the variable in which to save the generated content inside the
	 * tag.
	 * 
	 * @param variable value to set
	 **/
	public void setVariable(String variable) {
		this.variable = variable;
	}

	public void setTrim(boolean trim) {
		this.trim = trim;
        this.setTrimManually = true;
	}

	/**
	 * if true, and a variable with the passed name already exists, the content will be appended to the
	 * variable instead of overwriting it
	 */
	public void setAppend(boolean append) {
		this.append = append;
	}

	@Override
	public int doStartTag() {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doAfterBody() throws PageException {
        //If trim-attribute is not set by the user, use the whitespace-setting
        if(!setTrimManually) {
            ConfigWebPro config = (ConfigWebPro) pageContext.getConfig();
            trim = config.getCFMLWriterType() != ConfigPro.CFML_WRITER_REFULAR;
        }

		String value = trim ? bodyContent.getString().trim() : bodyContent.getString();

		if (append) {
			value = Caster.toString(VariableInterpreter.getVariableEL(pageContext, variable, ""), "") + value; // prepend the current variable or empty-string if not found
		}
		pageContext.setVariable(variable, value);
		bodyContent.clearBody();

		return SKIP_BODY;
	}

}