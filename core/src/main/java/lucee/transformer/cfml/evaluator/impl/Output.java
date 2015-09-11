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
package lucee.transformer.cfml.evaluator.impl;

import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagOutput;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.tag.TagLibTag;


/**
 * Prueft den Kontext des Tag output.
 * Das Tag output darf nicht innerhalb eines output Tag verschachtelt sein, 
 * ausser das aeussere Tag besitzt ein group Attribute. Das innere Tag darf jedoch kein group Attribute besitzen.

 */
public final class Output extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag,TagLibTag libTag) throws EvaluatorException { 
		
		TagOutput output=(TagOutput) tag;
		
        // check if inside a query tag
		TagOutput parent = output;
        boolean hasParentWithGroup=false;
        boolean hasParentWithQuery=false;
		boolean hasQuery=tag.containsAttribute("query");
		
		while((parent=getParentTagOutput(parent))!=null) {
            if(!hasParentWithQuery)hasParentWithQuery=parent.hasQuery();
            if(!hasParentWithGroup)hasParentWithGroup=parent.hasGroup();
            if(hasParentWithQuery && hasParentWithGroup)break;
		}
        
        if(hasQuery && hasParentWithQuery) 
			throw new EvaluatorException("Nesting of tags cfoutput with attribute query is not allowed");

        if(hasQuery) 
        	output.setType(TagOutput.TYPE_QUERY);
        
        else if(tag.containsAttribute("group") && hasParentWithQuery)
        	output.setType(TagOutput.TYPE_GROUP);
        
        else if(hasParentWithQuery) {
        	if(hasParentWithGroup) output.setType(TagOutput.TYPE_INNER_GROUP);
        	else output.setType(TagOutput.TYPE_INNER_QUERY);
        }
        else
        	 output.setType(TagOutput.TYPE_NORMAL);
        
        
        
        // attribute maxrows and endrow not allowd at the same time
        if(tag.containsAttribute("maxrows") && tag.containsAttribute("endrow"))
        	throw new EvaluatorException("Wrong Context, you cannot use attribute maxrows and endrow at the same time.");
        
        
	}
	
	public static TagOutput getParentTagOutput(TagOutput stat) {
		Statement parent = stat;
		
		
		while(true)	{
			parent=parent.getParent();
			if(parent==null)return null;
			if(parent instanceof TagOutput)	return (TagOutput) parent;
		}
	}
}