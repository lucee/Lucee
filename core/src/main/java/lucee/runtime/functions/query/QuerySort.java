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
/**
 * Implements the CFML Function querysetcell
 */
package lucee.runtime.functions.query;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.util.ListUtil;

public final class QuerySort extends BIF {

	private static final long serialVersionUID = -6566120440638749819L;

	public static boolean call(PageContext pc , Query query, String columnName) throws PageException {
		return call(pc,query,columnName,null);
	}
	public static boolean call(PageContext pc , Query query, String columnNames, String directions) throws PageException {
		// column names
		String[] arrColumnNames = ListUtil.trimItems(ListUtil.listToStringArray(columnNames, ','));
		int[] dirs = new int[arrColumnNames.length];
		
		// directions
		if(!StringUtil.isEmpty(directions)) {
			String[] arrDirections = ListUtil.trimItems(ListUtil.listToStringArray(directions, ','));
			if(arrColumnNames.length!=arrDirections.length)throw new DatabaseException("column names and directions has not the same count",null,null,null);
			
			String direction;
			for(int i=0;i<dirs.length;i++){
				direction=arrDirections[i].toLowerCase();
				dirs[i]=0;
				if(direction.equals("asc"))dirs[i]=Query.ORDER_ASC;
				else if(direction.equals("desc"))dirs[i]=Query.ORDER_DESC;
				else {		
					throw new DatabaseException("argument direction of function querySort must be \"asc\" or \"desc\", now \""+direction+"\"",null,null,null);
				}
			}
		}
		else {
			for(int i=0;i<dirs.length;i++){
				dirs[i]=Query.ORDER_ASC;
			}
		}
		
		
		for(int i=arrColumnNames.length-1;i>=0;i--)
			query.sort(KeyImpl.init(arrColumnNames[i]),dirs[i]);
		
		
		
		return true;		
	}
	
	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==2)return call(pc,Caster.toQuery(args[0]),Caster.toString(args[1]));
		return call(pc,Caster.toQuery(args[0]),Caster.toString(args[1]),Caster.toString(args[2]));
	}
}