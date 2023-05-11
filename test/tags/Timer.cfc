/*
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase" {

	public function testTimerUnits() localmode="true"{
		loop list="milli:ms,nano:ns,micro:us,seconds:s" item="unit"{
			savecontent variable="out"{
				timer type="outline" unit="#ListFirst( unit, ":" )#" {
					 sleep( 7 );
				}
			}
			// check for <fieldset class="cftimer"><legend align="top">: 135ms</legend></fieldset>
			expect( out ).ToInclude( ListLast( unit, ":" ) & "<" );
		}
	}

	public function testTimerVariable() localmode="true"{
		timer unit="milli" variable="timer" {
			sleep( 7 );
		}
		expect( timer ).ToBeGTE( 7 );
		expect( timer ).ToBeLT( 20 ); // headroom for overloaded CI server
	}

	public function testTimerType() localmode="true" {
		expect( 
			function() {
				loop list="comment, debug, inline, outline" item="timerType"{
					timer type="#timerType#" {
						sleep( 1 );
					}
				}
		} ).NotToThrow();
	}	
}