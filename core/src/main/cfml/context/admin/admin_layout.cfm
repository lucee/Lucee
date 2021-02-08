<cfif (thisTag.executionMode == "end" || !thisTag.hasEndTag)>
	<cfscript>

		param name="session.lucee_admin_lang" default="en";
		param name="attributes.navigation"    default="";
		param name="attributes.title"         default="";
		param name="attributes.content"       default="";
		param name="attributes.right"         default="";
		param name="attributes.width"         default="780";

		variables.stText = application.stText[session.lucee_admin_lang];
		ad=request.adminType;
		hasNavigation = len(attributes.navigation) GT 0;
		home = request.adminType & ".cfm";
		homeQS = URL.keyExists("action") ? "?action=" & url.action : "";
		request.mode = "full";
		resNameAppendix = hash(server.lucee.version & server.lucee["release-date"], "quick");
	</cfscript>
<cfcontent reset="yes"><!DOCTYPE html>
<cfoutput>
<html>
<head>
	<title>#attributes.title# - Lucee #ucFirst(request.adminType)# Administrator</title>
	<link rel="stylesheet" href="../res/css/admin6-#resNameAppendix#.css.cfm" type="text/css">
	<meta name="robots" content="noindex,nofollow">
	<cfhtmlhead action="flush">
</head>

<cfparam name="attributes.onload" default="">
<cfset mode=request.singleMode?"single":request.adminType>
<body id="body" class="admin-#mode# #mode#<cfif application.adminfunctions.getdata('fullscreen') eq 1> full</cfif>" onload="#attributes.onload#">
	<div id="<cfif !hasNavigation>login<cfelse>layout</cfif>">
		<table id="layouttbl">
			<tbody>
				<tr id="tr-header">	<!--- TODO: not sure where height of 275px is coming from? forcing here 113px/63px !--->
					<td colspan="2">
						<div id="header">
								<a id="logo" class="sprite" href="#home#"></a>
							<cfif not request.singleMode>
								<div id="admin-tabs" class="clearfix">
									<a href="server.cfm#homeQS#" class="sprite server"></a>
									<a href="web.cfm#homeQS#" class="sprite web"></a>
								</div>
							</cfif>
						</div>	<!--- #header !--->
					</td>
				</tr>
				<tr>
				<cfif hasNavigation>
					<td id="navtd" class="lotd">
						<div id="nav">
							<a href="##" id="resizewin" class="sprite" title="resize window"></a>

								<form method="get" action="#cgi.SCRIPT_NAME#">
									<input type="hidden" name="action" value="admin.search">
									<input type="text" name="q" size="15"  class="navSearch" id="lucee-admin-search-input" placeholder="#stText.buttons.search.ucase()#">
									<button type="submit" class="sprite  btn-search"><!--- <span>#stText.buttons.search# ---></span></button>
									<!--- btn-mini title="#stText.buttons.search#" --->
								</form>

								#attributes.navigation#
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
									<cfparam name="url.action" default="">
									<cfset pageIsFavorite = application.adminfunctions.isFavorite(url.action)>
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
												<li class="favtext"><i>No items yet.<br>Go to a page you use often, and click on "Favorites" to add it here.</i></li>
											</cfif>
										</ul>
									</div>
								</cfif>
									<div class="box"><cfif structKeyExists(request,'title')>#request.title#<cfelse>#attributes.title#</cfif>
									<cfif structKeyExists(request,'subTitle')> - #request.subTitle#</cfif></div>
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
							<a href="https://www.lucee.org" target="_blank">Lucee Association Switzerland</a>.
							All Rights Reserved
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</div>

	<script src="../res/js/base.min.js.cfm" type="text/javascript"></script>
	<script src="../res/js/jquery.modal.min.js.cfm" type="text/javascript"></script>
	<script src="../res/js/jquery.blockUI-#resNameAppendix#.js.cfm" type="text/javascript"></script>
	<script src="../res/js/admin-#resNameAppendix#.js.cfm" type="text/javascript"></script>
	<script src="../res/js/util-#resNameAppendix#.min.js.cfm"></script>
	<cfinclude template="navigation.cfm">
	<script>
		$(function(){

			$(".coding-tip-trigger").click(
				function(){
					var $this = $(this);
					$this.next(".coding-tip").slideDown();
					$this.hide();
				}
			);

			$(".coding-tip code").click(
				function(){
					__LUCEE.util.selectText(this);
				}
			).prop("title", "Click to select the text");
		});
	</script>

	<cfhtmlbody action="flush">
</body>
</html>
</cfoutput>
	<cfset thistag.generatedcontent="">
</cfif>
<cfparam name="session.debugEnabled" default="false">
<cfif structKeyExists (url, "debug")>
	<cfset session.debugEnabled = url.debug>
</cfif>
<cfsetting showdebugoutput="#session.debugEnabled#">
