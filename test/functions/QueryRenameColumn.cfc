/*
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	public void function testRenameColumn() {
		local.qry = QueryNew("col");
		QueryAddRow(qry);
		QuerySetCell(qry, "col", 1);

		QueryRenameColumn(qry,"col","col2");
		
		QueryAddRow(qry);
		QuerySetCell(qry, "col2", 2);

		assertEquals(2, qry.recordcount);
		assertEquals("col2", qry.columnList);
		
		qry.renameColumn("col2","col3");
		assertEquals("col3", qry.columnList);
	}
} 