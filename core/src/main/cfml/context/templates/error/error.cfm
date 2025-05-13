<cfsetting enablecfoutputonly="reset">
<cfparam name="addClosingHTMLTags" default="#true#" type="boolean"><cfif addClosingHTMLTags></TD></TD></TD></TH></TH></TH></TR></TR></TR></TABLE></TABLE></TABLE></A></ABBREV></ACRONYM></ADDRESS></APPLET></AU></B></BANNER></BIG></BLINK></BLOCKQUOTE></BQ></CAPTION></CENTER></CITE></CODE></COMMENT></DEL></DFN></DIR></DIV></DL></EM></FIG></FN></FONT></FORM></FRAME></FRAMESET></H1></H2></H3></H4></H5></H6></HEAD></I></INS></KBD></LISTING></MAP></MARQUEE></MENU></MULTICOL></NOBR></NOFRAMES></NOSCRIPT></NOTE></OL></P></PARAM></PERSON></PLAINTEXT></PRE></Q></S></SAMP></SCRIPT></SELECT></SMALL></STRIKE></STRONG></SUB></SUP></TABLE></TD></TEXTAREA></TH></TITLE></TR></TT></U></UL></VAR></WBR></XMP>
</cfif><style>
	#-lucee-err			{ font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; font-size: 11px; background-color:#930; border-collapse: collapse; }
	#-lucee-err td.mono	, #ai-response-cell td.mono
		{ font-family: "DejaVu Sans Mono","Menlo", "Consolas", "Monaco", monospace; }
	
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
    
	#ai-response-cell h1, #ai-response-cell h2, #ai-response-cell h3, #ai-response-cell h4, #ai-response-cell h5, #ai-response-cell h6 {
		font-size: 1em !important;
	}

	#ai-response-cell code {
		font-size: 1.2em;
		background-color: rgba(0, 0, 0, 0.6); /* Lighter semi-transparent background */
		color: #E6F0F2;              /* Light text color */
		padding: 2px 4px;            /* Smaller padding for inline text */
		border-radius: 5px;          /* Slightly rounded corners */
		font-family: Consolas, "Courier New", monospace; /* Monospaced font for code */
		display: inline;             /* Keep it as an inline element */
		white-space: nowrap;         /* Prevent breaking lines */
	}

	#ai-response-cell code.lucee-ml  {
		display: block;              /* Make it a block element */
		padding: 5px 10px;               /* Padding around the text for better readability */
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

function luceeLoadError(jsonData) {
    const cell = document.getElementById('ai-response-cell');
    // Clear existing content
    cell.innerHTML = '';
    luceeSpinner();
    fetch('/lucee/debug/modern/error.cfm', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(jsonData)
    })
    .then(response => {
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        
        function readStream(text) {
            return reader.read().then(({done, value}) => {
                if (done) {
                    return;
                }
                spinner=false;
                // Decode and append the new chunk
                text += decoder.decode(value, {stream: true});
                cell.innerHTML = text;
                
                // Continue reading
                return readStream(text);
            });
        }
        
        return readStream("");
    })
    .catch(error => {
		spinner=false;
        cell.innerHTML = 'Error loading content';
        console.error('Error:', error);
    });
}

spinner=true;
function luceeSpinner(index) {
	var spinnerElement = document.getElementById('ai-response-cell');
	
	var dotCycle = ['⠁', '⠈', '⠐', '⠠', '⢀', '⡀', '⠄', '⠂'];
	if(!index) index = 0;
	if(!spinner) return;
	spinnerElement.innerText = dotCycle[index];
	index = (index + 1) % dotCycle.length;
	setTimeout(luceeSpinner, 200, index)
}

<cfif LuceeAIHas('default:exception')>
	<cfoutput>luceeCatchData=#luceeCatchToString(catch)#;</cfoutput>
</cfif>
</script>

<cfscript>
function luceeMonoBlock(input,tablengt=1) {
	try {
		var lines=listToArray(HTMLEditFormat( trim( input ) ),chr(10));
		var rtn="";
		// we make any whitespace not breaking at the begin of a line
		loop array=lines item="local.line" {
			if(len(rtn)) rtn&="<br>";
			
			// Replace leading whitespace with '&nbsp;'
			var finds=reFind("[ \t]+", line,1,true);
			if(finds.pos[1]==1) {
				var len=finds.len[1];
				var match=finds.match[1];
				var replacement=replace( replace( match, ' ', '&nbsp;', 'all'), '	', repeatString('&nbsp;', tablengt), 'all');
				line=replacement&mid(line,len+1);
			}
			rtn &= line;
		}
	}
	catch(ex) {
		return input;
	}
	return rtn;
}

function luceeCatchToString(caughtError) {
	try{
		var catchi=duplicate(arguments.caughtError,true);
		if (structKeyExists(catchi, "TagContext")) {
			var path=catch.TagContext[1].template?:"";
			var line=catch.TagContext[1].line?:"";
			
			if(!fileExists(path)) path=expandPath(path);
			if(fileExists(path)) {
				catchi["content"]=fileRead(path);
			}
			catchi.path=path;
			catchi.line=line;
			//structDelete(catchi, "TagContext",false);
			loop array=catchi.TagContext item="el" {
				structDelete(el, "id",false);
				structDelete(el, "column",false);
				structDelete(el, "Raw_Trace",false);
				structDelete(el, "codePrintHTML",false);
				structDelete(el, "type",false);
			}
		}
		structDelete(catchi, "ErrorCode",false);

		return serializeJSON(catchi);
	} catch(e) {
		SerializeJSON(arguments.caughtError);
	}
}


</cfscript>
<cfoutput>
<table id="-lucee-err" cellpadding="4" cellspacing="1">
	<tr>
		<td colspan="2" class="label">Lucee #server.lucee.version# Error (#catch.type#)</td>
	</tr>
	<cfparam name="catch.message" default="">
	<tr>
		<td class="label">Message</td>
		<td class="mono">#luceeMonoBlock( catch.message )#</td>
	</tr>
	<cfparam name="catch.detail" default="">
	<cfif len( catch.detail )>
		<tr>
			<td class="label">Detail</td>
		    <td class="mono">#luceeMonoBlock( catch.detail )#</td>
		</tr>
	</cfif>
	<!--- AI --->
	<tr>
		<td class="label">
			AI (Experimental)
		</td>
		<td id="ai-response-cell" class="mono">
		    <cftry>
				<cfif LuceeAIHas('default:exception')>
					<a href="##" onclick="luceeLoadError(luceeCatchData); return false;">Analyse</a>
				<cfelse>
					For AI-driven exception analysis setup, see <a target="blank" href="https://github.com/lucee/lucee-docs/blob/master/docs/recipes/ai.md">AI Setup Guide</a>.
				</cfif>
				<cfcatch>
					<cflog log="application" exception="#cfcatch#">
				</cfcatch>
			</cftry>
		</td>
	</tr>
	<cfif structkeyexists( catch, 'errorcode' ) && len( catch.errorcode ) && catch.errorcode NEQ 0>
		<tr>
			<td class="label">Error Code</td>
			<td class="mono">#catch.errorcode#</td>
		</tr>
	</cfif>
	<cfif structKeyExists( catch, 'extendedinfo' ) && len( catch.extendedinfo )>
		<tr>
			<td class="label">Extended Info</td>
			<td class="mono">#luceeMonoBlock( catch.extendedinfo )#</td>
		</tr>
	</cfif>
	<cfif structKeyExists( catch, 'additional' )>
		<cfloop collection="#catch.additional#" index="key" item="val">
			<tr>
				<td class="label">#key#</td>

				<td class="mono"><cftry>#markdowntohtml( catch.additional[key])#<cfcatch>#rluceeMonoBlock( catch.additional[key] )#</cfcatch></cftry></td>
			</tr>
		</cfloop>
	</cfif>
	<cfif structKeyExists( catch, 'tagcontext' )>
		<cfset len=arrayLen( catch.tagcontext )>
		<cfif len>
			<tr>
				<td class="label">Stacktrace</td>
				<td class="mono">The Error Occurred in<br>
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
							<span class="-no-icon">#idx == 1 ? "<b>#luceeMonoBlock(tc.template)#: line #tc.line#</b>" : "<b>called from</b> #luceeMonoBlock(tc.template)#: line #tc.line#"#</span>
							<br>
						</cfif>
					</cfloop>
				</td>
			</tr>
		</cfif>
	</cfif>
	<tr>
		<td class="label">Java Stacktrace</td>
		<td class="mono">#luceeMonoBlock(catch.stacktrace)#</td>
	</tr>
	<tr>
		<td class="label">Timestamp</td>
		<td class="mono">
			<cfset timestamp = now()>
			#LsDateFormat( timestamp, 'short' )# #LsTimeFormat( timestamp, 'long' )#
		</td>
	</tr>
</table>
</cfoutput>
