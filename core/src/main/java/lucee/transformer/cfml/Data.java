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
package lucee.transformer.cfml;

import lucee.runtime.config.Config;
import lucee.transformer.Factory;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.Root;
import lucee.transformer.cfml.evaluator.EvaluatorPool;
import lucee.transformer.cfml.expression.SimpleExprTransformer;
import lucee.transformer.cfml.script.DocComment;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagScript;
import lucee.transformer.util.SourceCode;

public class Data {

	public final SourceCode srcCode;
	public final TransfomerSettings settings;
	public final TagLib[][] tlibs;
	public final FunctionLib[] flibs;
	public final Root root;
	public final TagLibTag[] scriptTags;
	public final EvaluatorPool ep;
	public final Factory factory;
	public final Config config;
	public boolean allowLowerThan;
	public boolean parseExpression;

	private SimpleExprTransformer set;

	public short mode = 0;
	public boolean insideFunction;
	public String tagName;
	public boolean isCFC;
	public boolean isInterface;
	public short context = TagLibTagScript.CTX_NONE;
	public DocComment docComment;
	private Body parent;
	public ExprTransformer transformer;

	public Data(Factory factory, Root root, SourceCode srcCode, EvaluatorPool ep, TransfomerSettings settings, TagLib[][] tlibs, FunctionLib[] flibs, TagLibTag[] scriptTags,
			boolean allowLowerThan) {
		this.root = root;
		this.srcCode = srcCode;
		this.settings = settings;
		this.tlibs = tlibs;
		this.flibs = flibs;
		this.scriptTags = scriptTags;
		this.ep = ep;
		this.factory = factory;
		this.config = factory.getConfig();
		this.allowLowerThan = allowLowerThan;
	}

	public SimpleExprTransformer getSimpleExprTransformer() {
		return set;
	}

	public void setSimpleExprTransformer(SimpleExprTransformer set) {
		this.set = set;
	}

	public Body setParent(Body parent) {
		Body tmp = this.parent;
		this.parent = parent;
		return tmp;
	}

	public Body getParent() {
		return parent;
	}
}