<cfsetting requesttimeout="0.01">
<cfset sleep( 100 )>
<!--- cfinclude triggers checkRequestTimeout(), sleep() alone won't as it's a single blocking Java call --->
<cfinclude template="sleep.cfm">
done
