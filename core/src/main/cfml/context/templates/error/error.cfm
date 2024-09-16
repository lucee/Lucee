<cfparam name="addClosingHTMLTags" default="#true#" type="boolean"><cfif addClosingHTMLTags></TD></TD></TD></TH></TH></TH></TR></TR></TR></TABLE></TABLE></TABLE></A></ABBREV></ACRONYM></ADDRESS></APPLET></AU></B></BANNER></BIG></BLINK></BLOCKQUOTE></BQ></CAPTION></CENTER></CITE></CODE></COMMENT></DEL></DFN></DIR></DIV></DL></EM></FIG></FN></FONT></FORM></FRAME></FRAMESET></H1></H2></H3></H4></H5></H6></HEAD></I></INS></KBD></LISTING></MAP></MARQUEE></MENU></MULTICOL></NOBR></NOFRAMES></NOSCRIPT></NOTE></OL></P></PARAM></PERSON></PLAINTEXT></PRE></Q></S></SAMP></SCRIPT></SELECT></SMALL></STRIKE></STRONG></SUB></SUP></TABLE></TD></TEXTAREA></TH></TITLE></TR></TT></U></UL></VAR></WBR></XMP>
</cfif><style>
	#-lucee-err			{ font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; font-size: 11px; background-color:#930; border-collapse: collapse; }
	#-lucee-err td 		{ font-size: 1.1em;border: 0px solid #350606; color: #930; background-color: #FC0; line-height: 1.35;border: 1px solid #930;  }
	#-lucee-err td.label	{ background-color: #F90; font-weight: bold; white-space: nowrap; vertical-align: top; }

	#-lucee-err .collapsed	{ display: none; }
	#-lucee-err .expanded 	{ display: block; }

	.-lucee-icon-plus 	{ background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIRhI+hG7bwoJINIktzjizeUwAAOw==)
    					no-repeat left center; padding: 4px 0 4px 16px; }

	.-lucee-icon-minus 	{ background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIQhI+hG8brXgPzTHllfKiDAgA7)
						no-repeat left center; padding: 4px 0 4px 16px; }

	.-no-icon 	{padding: 0px 0px 0px 16px; }
	.-lucee-comment 	{
		opacity: 0.5; 
	}
    
	#aivalue code {
		font-size: 1.2em;
		background-color: rgba(0, 0, 0, 0.6); /* Lighter semi-transparent background */
		color: #E6F0F2;              /* Light text color */
		padding: 2px 4px;            /* Smaller padding for inline text */
		border-radius: 5px;          /* Slightly rounded corners */
		font-family: Consolas, "Courier New", monospace; /* Monospaced font for code */
		display: inline;             /* Keep it as an inline element */
		white-space: nowrap;         /* Prevent breaking lines */
	}

	#aivalue pre code  {
		display: block;              /* Make it a block element */
		padding: 10px;               /* Padding around the text for better readability */
		border-radius: 5px;          /* Rounded corners for a nicer look */
		overflow-x: auto;            /* Allows horizontal scrolling for long lines of code */
		font-family: Consolas, "Courier New", monospace; /* Use a monospaced font for code */
		margin: 10px 0;              /* Margin around the code block for spacing */
		white-space: pre-wrap;       /* Preserve whitespace and wrap as necessary */
	}

	

</style>
<script>

	var __LUCEE = {

		oc: 	function ( btn ) {

			var id = btn.id.split( '$' )[ 1 ];

			var curBtnClass = btn.attributes[ 'class' ];	// bracket-notation required for IE<9
			var cur = curBtnClass.value;

			var curCstClass = document.getElementById( '__cst$' + id ).attributes[ 'class' ];

			if ( cur == '-lucee-icon-plus' ) {

				curBtnClass.value = '-lucee-icon-minus';
				curCstClass.value = 'expanded';
			} else {

				curBtnClass.value = '-lucee-icon-plus';
				curCstClass.value = 'collapsed';
			}
		}
	}
</script>
<cfoutput>
<table id="-lucee-err" cellpadding="4" cellspacing="1">
	<tr>
		<td colspan="2" class="label">Lucee #server.lucee.version# Error (#catch.type#)</td>
	</tr>
	<cfparam name="catch.message" default="">
	<tr>
		<td class="label">Message</td>
		<td>#replace( HTMLEditFormat( trim( catch.message ) ), chr(10), '<br>', 'all' )#</td>
	</tr>
	<cfparam name="catch.detail" default="">
	<cfif len( catch.detail )>
		<tr>
			<td class="label">Detail</td>
		    <td>#replace( HTMLEditFormat( trim( catch.detail ) ), chr(10), '<br>', 'all' )#</td>
		</tr>
	</cfif>
	<!--- AI --->
	<cfif LuceeAIHas('default:exception')>
		<cftry>
			<cfset meta=LuceeAIGetMetaData('default:exception')>
			<tr>
				<td class="label">
					AI (#meta.label?:""#)
				</td>
				<td id="ai-response-cell">...</td>
			</tr>
			<cfcatch></cfcatch>
		</cftry>
	</cfif>
	<cfif structkeyexists( catch, 'errorcode' ) && len( catch.errorcode ) && catch.errorcode NEQ 0>
		<tr>
			<td class="label">Error Code</td>
			<td>#catch.errorcode#</td>
		</tr>
	</cfif>
	<cfif structKeyExists( catch, 'extendedinfo' ) && len( catch.extendedinfo )>
		<tr>
			<td class="label">Extended Info</td>
			<td>#HTMLEditFormat( catch.extendedinfo )#</td>
		</tr>
	</cfif>
	<cfif structKeyExists( catch, 'additional' )>
		<cfloop collection="#catch.additional#" index="key" item="val">
			<tr>
				<td class="label">#key#</td>

				<td><cftry>#markdowntohtml( catch.additional[key])#<cfcatch>#replace( HTMLEditFormat( catch.additional[key] ), chr(10),'<br>', 'all' )#</cfcatch></cftry></td>
			</tr>
		</cfloop>
	</cfif>
	<cfif structKeyExists( catch, 'tagcontext' )>
		<cfset len=arrayLen( catch.tagcontext )>
		<cfif len>
			<tr>
				<td class="label">Stacktrace</td>
				<td>The Error Occurred in<br>
					<cfloop index="idx" from="1" to="#len#">
						<cfset tc = catch.tagcontext[ idx ]>
						<cfparam name="tc.codeprinthtml" default="">
						<cfif len( tc.codeprinthtml )>

							<cfset isFirst = ( idx == 1 )>

							<a class="-lucee-icon-#isFirst ? 'minus' : 'plus'#" id="__btn$#idx#" onclick="__LUCEE.oc( this );" style="cursor: pointer;">
								#isFirst ? "<b>#tc.template#: line #tc.line#</b>" : "<b>called from</b> #tc.template#: line #tc.line#"#
							</a>
							<br>

							<blockquote class="#isFirst ? 'expanded' : 'collapsed'#" id="__cst$#idx#">
								#tc.codeprinthtml#<br>
							</blockquote>
						<cfelse>
							<span class="-no-icon">#idx == 1 ? "<b>#HTMLEditFormat(tc.template)#: line #tc.line#</b>" : "<b>called from</b> #HTMLEditFormat(tc.template)#: line #tc.line#"#</span>
							<br>
						</cfif>
					</cfloop>
				</td>
			</tr>
		</cfif>
	</cfif>
	<tr>
		<td class="label">Java Stacktrace</td>
		<td>#replace( HTMLEditFormat(catch.stacktrace), chr(10), "<br><span style='margin-right: 1em;'>&nbsp;</span>", "all" )#</td>
	</tr>
	<tr>
		<td class="label">Timestamp</td>
		<td>
			<cfset timestamp = now()>
			#LsDateFormat( timestamp, 'short' )# #LsTimeFormat( timestamp, 'long' )#
		</td>
	</tr>
</table>


<cfif LuceeAIHas('default:exception')>
	
	<cftry>
		<script>
			var val= "";
			spinner=true;
			function luceeSpinner(index) {
				var spinnerElement = document.getElementById('ai-response-cell');
				
				var dotCycle = ['⣷','⣯','⣟','⡿','⢿','⣻','⣽','⣾'];
				if(!index) index = 0;
				if(!spinner) return;
				spinnerElement.innerText = dotCycle[index];
				index = (index + 1) % dotCycle.length;
				setTimeout(luceeSpinner, 200, index)
			}
			luceeSpinner();
		
		</script>
<cfflush throwonerror=false>


		<cfscript>
			path=catch.TagContext[1].template?:"";
			line=catch.TagContext[1].line?:"";
			
			
			ais=LuceeCreateAISession('default:exception', 
			"You are a Lucee expert. There was an exception while executing Lucee (CFML) code in template #path# on line #line#.
Analyze the provided JSON containing exception details of this issue. 
The 'content' key contains the source code of the template that failed.

Provide a concise analysis with potential fixes, and include example code if it maske sense. 
Return the result in plain markdown format (no starting ""```markdown"") without referencing how the data was provided or mentioning any specific JSON keys. 
Avoid repeating exception details, as those will be presented elsewhere. Keep your response brief and structured for inclusion in existing HTML output.");
			catchi=duplicate(catch);
			path=catch.TagContext[1].template?:"";
			if(!fileExists(path)) path=expandPath(path);
			if(fileExists(path)) {
				catchi["content"]=fileRead(path);

			}
			structDelete(catchi, "TagContext",false);
			structDelete(catchi, "ErrorCode",false);
			answer=LuceeInquiryAISession(ais,serializeJSON(catchi),function(msg) {
				echo('<script>');
				echo('spinner=false;');
				echo('val+=#serializeJson(msg)#;');
				echo("document.getElementById('ai-response-cell').innerText = val;");	
				echo('</script>');
				cfflush(throwonerror=false);
				
			});
	
		</cfscript>
		<script>
			document.getElementById('ai-response-cell').innerHTML ='<span id="aivalue">'+ #serializeJSON(markdowntohtml(replaceNoCase(answer,"Coldfusion","CFML","all") ))# 
				+'</span><p class="-lucee-comment"> created by #meta.label# (#(meta.model)#)</p>';
		</script>
		<cfcatch></cfcatch>
	</cftry>
</cfif>

<br>
</cfoutput>