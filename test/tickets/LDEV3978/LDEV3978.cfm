<cfparam name="url.uuid" default="">
<cfset systemOutput(url.uuid,1,1)>
<cfset list="first,second">
<cfloop list=#list# item="idx">
	<cflog type="information" file="LDEV3978_1" text="testone_#idx#_#url.uuid#" />
	<cflog type="information" file="LDEV3978_2" text="testtwo_#idx#_#url.uuid#" />
	<cflog type="information" file="LDEV3978_3" text="testthree_#idx#_#url.uuid#" />

	<cflog text="test_application_without_file_#idx#_#url.uuid#"> <!--- cflog without file attribute --->
</cfloop> 