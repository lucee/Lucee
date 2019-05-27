<cfcomponent extends="Modern" output="no">
	<cfscript>
		string function getLabel(){
			return "Modern IFrame";
		}

		string function getDescription(){
			return "This debug template wraps the modern template in an iframe to prevent it from interacting with the rest of the webpage or namespace (such as wrecking jquery)";
		}

		string function getid(){
			return "lucee-modern-iframe";
		}
	</cfscript>

	<cffunction name="output" returntype="void" localmode=true>
		<cfargument name="custom" required="true" type="struct" />
		<cfargument name="debugging" required="true" type="struct" />
		<cfargument name="context" type="string" default="web" />
		
		<cfoutput>
		<cfsavecontent variable="html">#super.output( argumentCollection= arguments )#</cfsavecontent>

		<template id="luceeModernDebug">#html#</template>
		<iframe id="luceeModernDebugIFrame" frameborder="0" src="about:blank" style="border: 0; width: 100%;" onload="resizeIframe(this)"></iframe>
		<script type="text/javascript">
			function resizeIframe(iframe) {
				iframe.height = iframe.contentWindow.document.body.scrollHeight + "px";
				window.requestAnimationFrame(() => resizeIframe(iframe));
			}
			var frame = document.getElementById('luceeModernDebugIFrame');
			var doc = frame.contentWindow.document;
			var body = document.getElementById('luceeModernDebug').innerHTML;
			doc.open();
			doc.write('<html><head><title></title></head><body>' + body + '</body></html>');
			doc.close();
		</script>

		</cfoutput>
		
	</cffunction><!--- output() !--->

</cfcomponent>
