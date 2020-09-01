<cfif thistag.executionmode == "start">

	<cfexit method="exittemplate">
</cfif>

<cfparam name="cookie.lucee_admin_lang" default="en">
<cfset session.lucee_admin_lang = cookie.lucee_admin_lang>
<cfinclude template="/lucee/admin/resources/text.cfm">

<cfcontent reset="#true#">

<cfset luceeVersion = listFirst( server.lucee.version, '.' ) & '.' & listGetAt( server.lucee.version, 2, '.' )>

<cfparam name="Attributes.title" default="Lucee Doc Reference #luceeVersion#">
<cfparam name="Attributes.description" default="Lucee Tag, Function, and Member Methods Reference for Lucee #luceeVersion#">


<!DOCTYPE HTML>
<html>
	<cfoutput>

	<head>
		<title>#Attributes.title#</title>
		<meta name="description" value="#Attributes.description#">
		<meta name="ROBOTS" content="NOINDEX,NOFOLLOW">
		<link rel="stylesheet" href="../res/css/normalize2.min.css.cfm">
		<link rel="stylesheet" href="../res/css/doc.css.cfm">

		<cfif Request.keyExists( "htmlHead" )>
			#Request.htmlHead#
		</cfif>
	</head>
	<body id="body-#listFirst( listLast( CGI.SCRIPT_NAME, '/' ), '.' )#">
		<div id="wrapper">
			<header>
				<img id="logo" src="../res/img/logo.png.cfm">

				<nav>
					<a href="tags.cfm" class="tags">Tags</a>
					&middot;
					<a href="functions.cfm" class="functions">Functions</a>
					&middot;
					<a href="objects.cfm" class="objects">Objects</a>
				</nav>
				<div id="header-title">Lucee #luceeVersion# Reference</div>
			</header>
			<div id="content">
				#thistag.generatedcontent#
			</div>
			<footer>
				<br><br><br>
				<nav>
					<div class="centered">
						<a href="tags.cfm">Tags</a>
						&middot;
						<a href="functions.cfm">Functions</a>
						&middot;
						<a href="objects.cfm">Objects</a>
					</div>
				</nav>
				<br>
				<div class="x-small" style="text-align: center;">
					Lucee Doc reference version #server.lucee.version#
					<br>
					&copy; #year( now() )# <a href="https://www.lucee.org/">Lucee Association Switzerland</a>. All Rights Reserved.
				</div>
			</footer>
		</div>	<!--- #wrapper !--->

	</cfoutput>

		<script src="../res/js/jquery-1.9.min.js.cfm"></script>

		<script type="text/javascript">

			<cfoutput>

				var baseUrl = "#CGI.SCRIPT_NAME#";
			</cfoutput>

			$( function() { 

				$( "#form-item-selector input[type=submit]" ).hide();

				$( "#select-item" ).change( function() { 

					var item = $( this ).val();

					if ( item )
						window.location = baseUrl + "?item=" + item;
				} );
			} );
		</script>

		<cfif Request.keyExists( "htmlBody" )>
			<cfoutput>#Request.htmlBody#</cfoutput>
		</cfif>
	</body>
</html>


<cfset thistag.generatedcontent="">

<cfparam name="showdebugoutput" default="#false#">
<cfsetting showdebugoutput="#showdebugoutput#">
