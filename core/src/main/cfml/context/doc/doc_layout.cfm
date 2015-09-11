<cfset request.adminType="web">
<cfif thistag.executionmode EQ "end" or not thistag.hasendtag>

	<cfparam name="session.lucee_admin_lang" default="en">
	<cfset variables.stText = application.stText[session.lucee_admin_lang] />
	<cfparam name="attributes.navigation" default="">
	<cfparam name="attributes.title" default="">
	<cfparam name="attributes.content" default="">
	<cfparam name="attributes.right" default="">
	<cfparam name="attributes.width" default="780">

	<cfscript>
		ad=request.adminType;
	</cfscript>
	<cfset request.mode="full">
	
<cfcontent reset="yes" /><!DOCTYPE HTML>
<!--[if lt IE 9]> <style> body.full #header #logo.sprite { background-image: url(resources/img/server-lucee-small.png.cfm); background-position: 0 0; margin-top: 16px; } </style> <![endif]-->	<!--- remove once IE9 is the min version to be supported !--->
<cfoutput>
<html>
<head>
	<title>Lucee Reference</title>
	<cfset nameAppendix=hash(server.lucee.version&server.lucee['release-date'],'quick')>
	<link rel="stylesheet" href="../res/css/admin-#nameAppendix#.css.cfm?#getTickCount()#" type="text/css">
	<!-- <link rel="stylesheet" href="../res/css/bootstrap2.min-#nameAppendix#.css.cfm" type="text/css">
	<link rel="stylesheet" href="../res/css/bootstrap2-responsive.min-#nameAppendix#.css.cfm" type="text/css">-->



</head>

<cfparam name="attributes.onload" default="">

<body id="body" class="admin-#request.adminType# #request.adminType#" onload="#attributes.onload#">
	<div id="layout">
		<table id="layouttbl">
			<tbody>
				<!--- Logo --->
				<tr id="tr-header">
					<td colspan="2">
						<div id="header">
							<a id="logo" class="sprite" href="index.cfm"></a>

						</div>
					</td>
				</tr>
				<tr>
					<td id="contenttd" class="lotd">
						<div id="content">

					<a href="tags.cfm" class="tags">Tags</a> &middot;
					<a href="functions.cfm" class="functions">Functions</a> &middot;
					<a href="objects.cfm" class="objects">Objects</a>
		
							 <div class="box">#attributes.title#</div>
							<div id="innercontent">
								#thistag.generatedContent#
							</div>
						</div>
					</td>
				</tr>
				<tr>
					<td class="lotd" id="copyrighttd" colspan="2">
						<div id="copyright" class="copy">
							&copy; #year(Now())#
							<a href="http://www.lucee.org" target="_blank">Lucee Association Switzerland</a>.
							All Rights Reserved
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</div>

	<!--- TODO: move to reusable script in /res/js/admin.js !--->
	<script>
		var getDomObject = function( obj ) {	// returns the element if it is an object, or finds the object by id */

			if ( typeof obj == 'string' || obj instanceof String )
				return document.getElementById( obj );

			return obj;
		}

		var selectText = function( obj ) {

	        if ( document.selection ) {

	            var range = document.body.createTextRange();
	            range.moveToElementText( getDomObject( obj ) );
	            range.select();
	        } else if ( window.getSelection ) {

	            var range = document.createRange();
	            range.selectNode( getDomObject( obj ) );
	            window.getSelection().addRange( range );
	        }
	    }


		$( function(){

			$( '.coding-tip-trigger-#request.adminType#' ).click( 
				function(){ 
					var $this = $(this);
					$this.next( '.coding-tip-#request.adminType#' ).slideDown();
					$this.hide();
				}
			);

			$( '.coding-tip-#request.adminType# code' ).click( 
				function(){ 					
					selectText(this);					
				}
			).prop("title", "Click to select the text");
		});
	</script>

	<cfif isDefined( "Request.htmlBody" )>#Request.htmlBody#</cfif>
</body>
</html>
</cfoutput>
	<cfset thistag.generatedcontent="">
</cfif>

<cfparam name="url.showdebugoutput" default="no">
<cfsetting showdebugoutput="#url.showdebugoutput#">