<cfparam name="url.encode" default="false">
<cfcookie name="encode" value="#url.encode#" expires="7" encodevalue="#url.encode#">
<cfcookie name="cookie_test" value="376B3346-463E-4F79-83FFE7C41451304A" expires="7" encodevalue="#url.encode#">
<cfcookie name="cookie_test2" value="space& & space & &nbsp;" expires="7" encodevalue="#url.encode#">