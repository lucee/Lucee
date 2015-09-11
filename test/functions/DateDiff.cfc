<!--- 
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
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testDateDiff" localMode="modern">

	<cfset setTimeZone("CET")> <!--- this timezone is used for DST tests --->
<!--- begin old test code --->
<cfset d1=CreateDateTime(2001, 11, 1, 4, 10, 4)> 
<cfset d2=CreateDateTime(2004, 03, 4, 6, 3, 1)> 

<cfset valueEquals(left="#DateDiff("s",d1, d2)#", right="73792377")> 
<cfset valueEquals(left="#DateDiff("n",d1, d2)#", right="1229872")> 
<cfset valueEquals(left="#DateDiff("h",d1, d2)#", right="20497")> 
<cfset valueEquals(left="#DateDiff("yyyy",d1, d2)#", right="2")> 

<cfset valueEquals(left="0" , right="#DateDiff("yyyy", 1, 2)#")>


<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 1:00:00'}")#", right="2131")>


<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 2:00:00'}")#", right="2132")>

<!--- switch to summer time, should be the same --->
<cfset valueEquals(left="#DateDiff('h', "{ts '2008-03-30 2:00:00'}", "{ts '2008-03-30 3:00:00'}")#", right="0")>

<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 1:00:00'}")#", right="127914")>
<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 2:00:00'}")#", right="127974")>
<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 3:00:00'}")#", right="127974")>

<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 1:00:00'}")#", right="7674895")>
<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 2:00:00'}")#", right="7678495")>
<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 3:00:00'}")#", right="7678495")>

<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 2:00:00'}", "{ts '2008-03-30 1:00:00'}")#", right="88")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 2:00:00'}", "{ts '2008-03-30 2:00:00'}")#", right="89")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 2:00:00'}", "{ts '2008-03-30 3:00:00'}")#", right="89")>

<cfset valueEquals(left="#DateDiff('m', "{ts '2008-01-30 2:00:00'}", "{ts '2008-03-30 1:00:00'}")#", right="1")>
<cfset valueEquals(left="#DateDiff('m', "{ts '2008-01-30 2:00:00'}", "{ts '2008-03-30 2:00:00'}")#", right="2")>
<cfset valueEquals(left="#DateDiff('m', "{ts '2008-01-30 2:00:00'}", "{ts '2008-03-30 3:00:00'}")#", right="2")>




<!--- year --->
<cfset valueEquals(left="#DateDiff('yyyy', CreateDate(1974, 6, 28), CreateDate(1975, 5, 28))#", right="0")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDate(1974, 6, 28), CreateDate(1975, 6, 27))#", right="0")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,2,3,3))#", right="0")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,2,3))#", right="0")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,2))#", right="0")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,3))#", right="1")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,4))#", right="1")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 7, 28,3,3,4))#", right="1")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="100")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-100")>

<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1974,2,9,1,1,30))#", right="-1")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1974,4,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,29))#", right="-1")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,31))#", right="0")>

<cfset valueEquals(left="#DateDiff('yyyy', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:06'}")#", right="1")>
<cfset valueEquals(left="#DateDiff('yyyy', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#", right="1")>
<cfset valueEquals(left="#DateDiff('yyyy', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:04'}")#", right="0")>

<cfset valueEquals(left="#DateDiff('yyyy', CreateDate(1975, 1, 9), "{ts '1978-01-07 10:21:34'}")#", right="2")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDate(1974, 6, 28), "{ts '1978-01-07 10:21:34'}")#", right="3")>
<cfset valueEquals(left="#DateDiff('yyyy', "{ts '1978-01-07 10:21:34'}", CreateDate(1975, 1, 9))#", right="-2")>
<cfset valueEquals(left="#DateDiff('yyyy', "{ts '1978-01-07 10:21:34'}", CreateDate(1974, 6, 28))#", right="-3")>

<!--- month --->
<cfset valueEquals(left="#DateDiff('m', CreateDate(1974, 6, 28), CreateDate(1975, 6, 27))#", right="11")>
<cfset valueEquals(left="#DateDiff('m', CreateDate(1974, 6, 28), CreateDate(1975, 5, 28))#", right="11")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,2,3,3))#", right="11")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,2,3))#", right="11")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,2))#", right="11")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,3))#", right="12")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,4))#", right="12")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 7, 28,3,3,4))#", right="13")>

<cfset valueEquals(left="#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#", right="12")>
<cfset valueEquals(left="#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#", right="5")>
<cfset valueEquals(left="#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#", right="5")>
<cfset valueEquals(left="#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#", right="6")>
<cfset valueEquals(left="#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#", right="6")>
<cfset valueEquals(left="#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#", right="6")>

<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="1200")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-1200")>

<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#", right="-1")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#", right="1")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#", right="0")>
<cfset valueEquals(left="#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#", right="0")>


<!--- days --->
<cfset valueEquals(left="#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="36525")>
<cfset valueEquals(left="#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-36524")>

<cfset valueEquals(left="#DateDiff('d', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#", right="-28")>
<cfset valueEquals(left="#DateDiff('d', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#", right="31")>
<cfset valueEquals(left="#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#", right="0")>
<cfset valueEquals(left="#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#", right="0")>

<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#", right="366")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#", right="181")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#", right="181")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#", right="182")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#", right="182")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#", right="182")>

<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 6:05:04'}")#", right="182")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 6:05:05'}")#", right="182")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 6:05:06'}")#", right="182")>


<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#", right="8784")>
<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#", right="4366")>
<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#", right="4366")>
<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#", right="4367")>
<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#", right="4367")>
<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#", right="4367")>


<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#", right="31622400")>
<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#", right="15721198")>
<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#", right="15721199")>
<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#", right="15721200")>
<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#", right="15721201")>
<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#", right="15721202")>

<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#", right="527040")>
<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#", right="262019")>
<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#", right="262019")>
<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#", right="262020")>
<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#", right="262020")>
<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#", right="262020")>




<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2009-01-01 5:00:00'}")#", right="366")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#", right="182")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 4:00:00'}")#", right="181")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 6:00:00'}")#", right="182")>

<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2009-01-01 5:00:00'}")#", right="366")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 3:00:00'}")#", right="181")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 4:00:00'}")#", right="181")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#", right="182")>
<cfset valueEquals(left="#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 6:00:00'}")#", right="182")>


<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#", right="4367")>
<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#", right="262020")>
<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#", right="15721200")>


<cfset valueEquals(left="#DateDiff('h', "{ts '2008-01-01 5:00:00'}", "{ts '2008-02-01 5:00:00'}")#", right="744")>
<cfset valueEquals(left="#DateDiff('n', "{ts '2008-01-01 5:00:00'}", "{ts '2008-02-01 5:00:00'}")#", right="44640")>
<cfset valueEquals(left="#DateDiff('s', "{ts '2008-01-01 5:00:00'}", "{ts '2008-02-01 5:00:00'}")#", right="2678400")>


<!--- year --->
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="100")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-100")>

<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1974,2,9,1,1,30))#", right="-1")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1974,4,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,29))#", right="-1")>
<cfset valueEquals(left="#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,31))#", right="0")>

<!--- quarter --->

<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,4,4,4,4,4))#", right="0")>
<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,6,4,4,4,4))#", right="0")>
<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,7,4,4,4,3))#", right="0")>
<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,7,4,4,4,4))#", right="1")>
<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,7,4,4,4,5))#", right="1")>

<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="400")>
<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-400")>

<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#", right="0")>
<cfset valueEquals(left="#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#", right="0")>




<!--- days (y) --->
<cfset valueEquals(left="#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="36525")>
<cfset valueEquals(left="#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-36524")>

<cfset valueEquals(left="#DateDiff('y', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#", right="-28")>
<cfset valueEquals(left="#DateDiff('y', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#", right="31")>
<cfset valueEquals(left="#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#", right="0")>
<cfset valueEquals(left="#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#", right="0")>


<!--- w (weekdays) --->
<cfset valueEquals(left="#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="5217")>
<cfset valueEquals(left="#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-5217")>

<cfset valueEquals(left="#DateDiff('w', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#", right="-4")>
<cfset valueEquals(left="#DateDiff('w', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#", right="4")>
<cfset valueEquals(left="#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#", right="0")>
<cfset valueEquals(left="#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#", right="0")>

<!--- ww (weeks) --->
<cfset valueEquals(left="#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="5217")>
<cfset valueEquals(left="#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-5217")>

<cfset valueEquals(left="#DateDiff('ww', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#", right="-4")>
<cfset valueEquals(left="#DateDiff('ww', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#", right="4")>
<cfset valueEquals(left="#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#", right="0")>
<cfset valueEquals(left="#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#", right="0")>

<!--- hour --->
<cfset valueEquals(left="#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="876600")>
<cfset valueEquals(left="#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-876576")>

<cfset valueEquals(left="#DateDiff('h', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#", right="-672")>
<cfset valueEquals(left="#DateDiff('h', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#", right="744")>
<cfset valueEquals(left="#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#", right="0")>
<cfset valueEquals(left="#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#", right="0")>

<!--- minutes --->
<cfset valueEquals(left="#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#", right="52596000")>
<cfset valueEquals(left="#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#", right="-52594560")>

<cfset valueEquals(left="#DateDiff('n', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#", right="-40320")>
<cfset valueEquals(left="#DateDiff('n', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#", right="44640")>
<cfset valueEquals(left="#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#", right="0")>
<cfset valueEquals(left="#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#", right="0")>

<!--- seconds --->
<cfset valueEquals(left="#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1976,1,9,1,1,30))#", right="31536000")>
<cfset valueEquals(left="#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#", right="0")>
<cfset valueEquals(left="#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,30))#", right="-31536000")>

<cfset valueEquals(left="#DateDiff('s', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#", right="-2419200")>
<cfset valueEquals(left="#DateDiff('s', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#", right="2678400")>
<cfset valueEquals(left="#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#", right="-1")>
<cfset valueEquals(left="#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#", right="1")>




<cfset local.date1=CreateDate(2008,03,30) />
<cfset local.date2=createDate(2038,03,31) />


<cfset valueEquals(left="#dateDiff("s",local.date1,local.date2)#", right="946767600")>
<cfset valueEquals(left="#dateDiff("n",local.date1,local.date2)#", right="15779460")>
<cfset valueEquals(left="#dateDiff("h",local.date1,local.date2)#", right="262991")>
<cfset valueEquals(left="#dateDiff("d",local.date1,local.date2)#", right="10958")>
<cfset valueEquals(left="#dateDiff("w",local.date1,local.date2)#", right="1565")>
<cfset valueEquals(left="#dateDiff("ww",local.date1,local.date2)#", right="1565")>

<cfset valueEquals(left="#dateDiff("q",local.date1,local.date2)#", right="120")>
<cfset valueEquals(left="#dateDiff("m",local.date1,local.date2)#", right="360")>
<cfset valueEquals(left="#dateDiff("yyyy",local.date1,local.date2)#", right="30")>


<cfset local.date1=CreateDate(2008,03,30) />
<cfset local.date2=createDate(2008,03,31) />
<cfset local.date3=createDate(2008,04,01) />


<cfset valueEquals(left="#dateDiff("w",local.date1,local.date2)#", right="0")>

<cfset valueEquals(left="#dateDiff("m",local.date1,local.date2)#", right="0")>
<cfset valueEquals(left="#dateDiff("m",local.date1,local.date3)#", right="0")>

<cfset valueEquals(left="#dateDiff("d",local.date1,local.date2)#", right="1")>
<cfset valueEquals(left="#dateDiff("d",local.date1,local.date3)#", right="2")>
<cfset valueEquals(left="#dateDiff("d",local.date2,local.date3)#", right="1")>

<cfset valueEquals(left="#dateDiff("h",local.date1,local.date2)#", right="23")>
<cfset valueEquals(left="#dateDiff("h",local.date1,local.date3)#", right="47")>
<cfset valueEquals(left="#dateDiff("h",local.date2,local.date3)#", right="24")>



<cfset local.date1=createDate(2007,03,30) />
<cfset local.date2=createDate(2007,03,31) />
<cfset local.date3=createDate(2007,04,01) />

<cfset valueEquals(left="#dateDiff("d",local.date1,local.date2)#", right="1")>
<cfset valueEquals(left="#dateDiff("d",local.date1,local.date3)#", right="2")>
<cfset valueEquals(left="#dateDiff("d",local.date2,local.date3)#", right="1")>

<cfset valueEquals(left="#dateDiff("h",local.date1,local.date2)#", right="24")>
<cfset valueEquals(left="#dateDiff("h",local.date1,local.date3)#", right="48")>
<cfset valueEquals(left="#dateDiff("h",local.date2,local.date3)#", right="24")>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>