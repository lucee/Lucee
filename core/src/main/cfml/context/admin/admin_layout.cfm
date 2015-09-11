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
		hasNavigation=len(attributes.navigation) GT 0;
		home=request.adminType&".cfm"
		if(structKeyExists(url,'action'))homeQS="?action="&url.action;
		else homeQS="";
	</cfscript>
	<cfset request.mode="full">
	
<cfcontent reset="yes" /><!DOCTYPE HTML>
<!--[if lt IE 9]> <style> body.full #header #logo.sprite { background-image: url(resources/img/server-lucee-small.png.cfm); background-position: 0 0; margin-top: 16px; } </style> <![endif]-->	<!--- remove once IE9 is the min version to be supported !--->
<cfoutput>
<html>
<head>
	<title>Lucee #ucFirst(request.adminType)# Administrator</title>
	<cfset nameAppendix=hash(server.lucee.version&server.lucee['release-date'],'quick')>
	<link rel="stylesheet" href="../res/css/admin-#nameAppendix#.css.cfm?#getTickCount()#" type="text/css">

	<script src="resources/js/jquery-1.7.2.min.js.cfm" type="text/javascript"></script>
	<script src="resources/js/jquery.blockUI.js.cfm" type="text/javascript"></script>
	<script src="resources/js/admin.js.cfm" type="text/javascript"></script>
</head>

<cfparam name="attributes.onload" default="">

<body id="body" class="admin-#request.adminType# #request.adminType#<cfif application.adminfunctions.getdata('fullscreen') eq 1> full</cfif>" onload="#attributes.onload#">
	<div id="<cfif !hasNavigation>login<cfelse>layout</cfif>">
		<table id="layouttbl">
			<tbody>
				<tr id="tr-header">	<!--- TODO: not sure where height of 275px is coming from? forcing here 113px/63px !--->
					<td colspan="2">
						<div id="header">
							<!--- http://localhost:9090/context5/res/img/web-lucee.png.cfm --->
							<a id="logo" class="sprite" href="#home#"></a>
							<div id="admin-tabs" class="clearfix">
								<a href="server.cfm#homeQS#" class="sprite server"></a>
								<a href="web.cfm#homeQS#" class="sprite web"></a>
							</div>
						</div>	<!--- #header !--->
					</td>
				</tr>

				<tr>
				<cfif hasNavigation>
					<td id="navtd" class="lotd">
						<div id="nav">
							<a href="##" id="resizewin" class="sprite" title="resize window"></a>
							<cfif hasNavigation>
								<form method="get" action="#cgi.SCRIPT_NAME#">
									<input type="hidden" name="action" value="admin.search" />
									<input type="text" name="q" size="15" id="navsearch" placeholder="#stText.buttons.search.ucase()#" />
									<button type="submit" class="sprite  btn-search"><!--- <span>#stText.buttons.search# ---></span></button>
									<!--- btn-mini title="#stText.buttons.search#" --->
								</form>
								#attributes.navigation#
							</cfif>
						</div>
					</td>
				</cfif>
					<td id="<cfif !hasNavigation>logintd<cfelse>contenttd</cfif>" class="lotd">
						<div id="content">
							 <div id="maintitle">
								<cfif hasNavigation>
									<div id="logouts">
									<a class="sprite tooltipMe logout" href="#request.self#?action=logout" title="Logout"></a>
									</div>
									<!--- Favorites --->
									<cfparam name="url.action" default="" />
									<cfset pageIsFavorite = application.adminfunctions.isFavorite(url.action) />
									<div id="favorites">


										<cfif url.action eq "">
											<a href="##" class="sprite favorite tooltipMe" title="Go to your favorite pages"></a>
										<cfelseif pageIsFavorite>
											<a href="#request.self#?action=internal.savedata&action2=removefavorite&favorite=#url.action#" class="sprite favorite tooltipMe" title="Remove this page from your favorites"></a>
										<cfelse>
											<a href="#request.self#?action=internal.savedata&action2=addfavorite&favorite=#url.action#" class="sprite tooltipMe favorite_inactive" title="Add this page to your favorites"></a>
										</cfif>
										<ul>
											<cfif attributes.favorites neq "">
												#attributes.favorites#
											<cfelse>
												<li class="favtext"><i>No items yet.<br />Go to a page you use often, and click on "Favorites" to add it here.</i></li>
											</cfif>
										</ul>
									</div>
								</cfif>
									<div class="box">#attributes.title#<cfif structKeyExists(request,'subTitle')> - #request.subTitle#</cfif></div>
								</div>
							<div id="innercontent" <cfif !hasNavigation>align="center"</cfif>>
								#thistag.generatedContent#
							</div>
						</div>
					</td>
				</tr>
				<tr>
					<td class="lotd" id="copyrighttd" colspan="#hasNavigation?2:1#">
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