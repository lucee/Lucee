/*
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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

	public void function testQueryColumnExists() localmode="true" {
		qry=query(name:["Susi","Peter"]);
		assertTrue(QueryColumnExists(qry,'name'));
		assertFalse(QueryColumnExists(qry,'lastname'));
	} 

	public void function testColumnExistsMember() localmode="true" {
		qry=query(name:["Susi","Peter"]);
		assertTrue(qry.ColumnExists('name'));
		assertFalse(qry.ColumnExists('lastname'));
	} 
	public void function testQueryKeyExists() localmode="true" {
		qry=query(name:["Susi","Peter"]);
		assertTrue(QueryKeyExists(qry,'name'));
		assertFalse(QueryKeyExists(qry,'lastname'));
	} 

	public void function testKeyExistsMember() localmode="true" {
		qry=query(name:["Susi","Peter"]);
		assertTrue(qry.keyExists('name'));
		assertFalse(qry.keyExists('lastname'));
	} 

} 