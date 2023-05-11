<cfscript>
	vals = [
		simple="lucee",
		guid="376B3346-463E-4F79-83FFE7C41451304A",
		html="space& & space & &nbsp;",
		delims=";,="
	]
</cfscript>

<cfloop collection=#vals# key="k" value="v">
	<cfcookie name="DEFAULT_#k#" value="#v#" expires="7"> <!-- i.e. default encodevalue which is true -->
	<cfcookie name="ENCODED_#k#" value="#v#" expires="7" encodevalue="true">
	<cfcookie name="#k#" value="#v#" expires="7" encodevalue="false">
	<cfcookie name="preservecase_#k#" value="#v#" expires="7" encodevalue="false" preservecase="true">
</cfloop>
