/**
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase" {


	public void function testRandom() {
		
		Randomize( getTickCount() );

		var series1 = [];
		for ( i=0; i<5; i++ )
			series1.append( RandRange( 1000, 2000 ) );

		var series2 = [];
		for ( i=0; i<5; i++ )
			series2.append( RandRange( 1000, 2000 ) );

		assertNotEquals( series1.toList(), series2.toList() );		
	}


	public void function testSeed() {
		
		Randomize( 2577 );

		var series1 = [];
		for ( i=0; i<5; i++ )
			series1.append( RandRange( 1000, 2000 ) );

		Randomize( 2577 );

		var series2 = [];
		for ( i=0; i<5; i++ )
			series2.append( RandRange( 1000, 2000 ) );

		Randomize( getTickCount() );	// reset seed for future requests

		assertEquals( series1.toList(), series2.toList() );		
	}

}