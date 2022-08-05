<cfparam name="addClosingHTMLTags" default="#true#" type="boolean"><cfif addClosingHTMLTags></TD></TD></TD></TH></TH></TH></TR></TR></TR></TABLE></TABLE></TABLE></A></ABBREV></ACRONYM></ADDRESS></APPLET></AU></B></BANNER></BIG></BLINK></BLOCKQUOTE></BQ></CAPTION></CENTER></CITE></CODE></COMMENT></DEL></DFN></DIR></DIV></DL></EM></FIG></FN></FONT></FORM></FRAME></FRAMESET></H1></H2></H3></H4></H5></H6></HEAD></I></INS></KBD></LISTING></MAP></MARQUEE></MENU></MULTICOL></NOBR></NOFRAMES></NOSCRIPT></NOTE></OL></P></PARAM></PERSON></PLAINTEXT></PRE></Q></S></SAMP></SCRIPT></SELECT></SMALL></STRIKE></STRONG></SUB></SUP></TABLE></TD></TEXTAREA></TH></TITLE></TR></TT></U></UL></VAR></WBR></XMP>
</cfif><style>
	#-lucee-err			{ font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; font-size: 11px;
	 background-color:#930; border: 0px; }
	#-lucee-err td 		{ border: 0px solid #350606; color: #930; background-color: #FC0; line-height: 1.35; }
	#-lucee-err td.label	{ background-color: #F90; font-weight: bold; white-space: nowrap; vertical-align: top; }

	#-lucee-err .collapsed	{ display: none; white-space:pre;line-height:75%; }
	#-lucee-err .expanded 	{ display: block; white-space:pre;line-height:75%; }

	.-lucee-icon-plus 	{ background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIRhI+hG7bwoJINIktzjizeUwAAOw==)
    					no-repeat left center; padding: 4px 0 4px 16px; }

	.-lucee-icon-minus 	{ background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIQhI+hG8brXgPzTHllfKiDAgA7)
						no-repeat left center; padding: 4px 0 4px 16px; }

	.-no-icon 	{padding: 0px 0px 0px 16px; }
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
		<cfloop collection="#catch.additional#" item="key">
			<tr>
				<td class="label">#key#</td>
				<td>#replace( HTMLEditFormat( catch.additional[key] ), chr(10),'<br>', 'all' )#</td>
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

							<blockquote class="#isFirst ? 'expanded' : 'collapsed'#" id="__cst$#idx#">#tc.codeprinthtml#<br></blockquote>
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
		<td>#replace( catch.stacktrace, chr(10), "<br><span style='margin-right: 1em;'>&nbsp;</span>", "all" )#</td>
	</tr>
	<tr>
		<td class="label">Timestamp</td>
		<td>
			<cfset timestamp = now()>
			#LsDateFormat( timestamp, 'short' )# #LsTimeFormat( timestamp, 'long' )#
		</td>
	</tr>
</table>
<br>
</cfoutput>
