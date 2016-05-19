<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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


	<cffunction name="testArraySortMember" localMode="modern">
		<cfset arr=arrayNew(1)>
		<cfset arr[1]=111>
		<cfset arr[2]=22>
		<cfset arr[3]=3>

		<cfset arr.sort("numeric")>
		<cfset valueEquals(left="#arr[1]#", right="3")>
		<cfset valueEquals(left="#arr[2]#", right="22")>
		<cfset valueEquals(left="#arr[3]#", right="111")>

	</cffunction>

	<cffunction name="testArraySort" localMode="modern">

<!--- begin old test code --->
<cfset arr=arrayNew(1)>
<cfset arr[1]=111>
<cfset arr[2]=22>
<cfset arr[3]=3>

<cfset ArraySort(arr, "numeric")>
<cfset valueEquals(left="#arr[1]#", right="3")>
<cfset valueEquals(left="#arr[2]#", right="22")>
<cfset valueEquals(left="#arr[3]#", right="111")>

<cfset ArraySort(arr, "text")>
<cfset valueEquals(left="#arr[1]#", right="111")>
<cfset valueEquals(left="#arr[2]#", right="22")>
<cfset valueEquals(left="#arr[3]#", right="3")>

<cfset arr=arrayNew(1)>
<cfset arr[1]="BB">
<cfset arr[2]="aa">
<cfset arr[3]="bbb">


<cftry>
	<cfset ArraySort(arr, "numeric")>
	<cfset fail("must throw:Non-numeric value found.")>
	<cfcatch></cfcatch>
</cftry>

<cfset ArraySort(arr, "text")>
<cfset valueEquals(left="#arr[1]#", right="BB")>
<cfset valueEquals(left="#arr[2]#", right="aa")>
<cfset valueEquals(left="#arr[3]#", right="bbb")>

<cfset ArraySort(arr, "textnocase")>
<cfset valueEquals(left="#arr[1]#", right="aa")>
<cfset valueEquals(left="#arr[2]#", right="BB")>
<cfset valueEquals(left="#arr[3]#", right="bbb")>

<cfset ArraySort(arr, "textnocase","asc")>
<cfset valueEquals(left="#arr[1]#", right="aa")>
<cfset valueEquals(left="#arr[2]#", right="BB")>
<cfset valueEquals(left="#arr[3]#", right="bbb")>

<cfset ArraySort(arr, "textnocase","desc")>
<cfset valueEquals(left="#arr[1]#", right="bbb")>
<cfset valueEquals(left="#arr[2]#", right="BB")>
<cfset valueEquals(left="#arr[3]#", right="aa")>

<cfset arr[4]=arrayNew(1)>
<cftry>
	<cfset ArraySort(arr, "textnocase","desc")>
	<cfset fail("must throw:In function ArraySort the array element at position 4 is not a simple value ")>
	<cfcatch></cfcatch>
</cftry>
<cfset arr[4]="">

<cftry>
	<cfset ArraySort(arr, "susi")>
	<cfset fail("must throw:Invalid sort type susi. ")>
	<cfcatch></cfcatch>
</cftry>

<cftry>
	<cfset ArraySort(arr, "text","susi")>
	<cfset fail("must throw:Invalid sort order susi. ")>
	<cfcatch></cfcatch>
</cftry>

<cfset arr=arrayNew(2)>
<cftry>
	<cfset ArraySort(arr, "numeric")>
	<cfset fail("must throw:The array passed cannot contain more than one dimension. ")>
	<cfcatch></cfcatch>
</cftry>

<cfset arr=listToArray("d,a,a,b,A")>
<cfset ArraySort(arr, "textnocase","desc")>
<cfset valueEquals(left="#arrayToList(arr)#", right="d,b,A,a,a")>

<cfset arr=listToArray("d,a,a,b,A")>
<cfset ArraySort(arr, "textnocase","asc")>
<cfset valueEquals(left="#arrayToList(arr)#", right="a,a,A,b,d")>


<cfscript>
arr=["hello","world","susi","world"];

// UDF
arraySort(arr,doSort);
valueEquals(arrayToList(arr),"hello,susi,world,world");

// Closure
doSort=function (left,right){
	return Compare(left,right);
};
arraySort(arr,doSort);
valueEquals(arrayToList(arr),"hello,susi,world,world");

</cfscript>	


<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	<cfscript>
	private function doSort(left,right){
		return Compare(left,right);
	}
	</cfscript>
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>