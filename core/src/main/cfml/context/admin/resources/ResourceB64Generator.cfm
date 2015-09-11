<cfparam name="src" 		default="C:\Apps\lucee4-jetty\webapps\lucee\crop-image\admin-sprite.png">

<cfparam name="mimeType"	default="">


<cfif !len( mimetype )>

	<cfset ext = listLast( src, '.' )>

	<cfswitch expression="#ext#">
		
		<cfcase value="jpg">
			
			<cfset mimetype = "image/jpeg">
		</cfcase>
		<cfcase value="gif,png" delimiters=",">

			<cfset mimetype = "image/#ext#">
		</cfcase>
		<cfdefaultcase>
			
			<cfthrow type="UnsupportedType" message="files of type [#ext#] are not supported">
		</cfdefaultcase>
	</cfswitch>
</cfif>


<cfset template = "
	
	<cfset data ='{base64image}'>

	<cfsetting showdebugoutput='##false##'>
	<cfif getBaseTemplatePath() == getCurrentTemplatePath()>

		<cfapplication name='__LUCEE_STATIC_CONTENT' sessionmanagement='##false##' clientmanagement='##false##' applicationtimeout='##createtimespan( 1, 0, 0, 0 )##'>
				
		<cfset etag 	= '{etag}'>
		<cfset mimetype = '{mimeType}'>		

		<cfset expireDays = 100>
		<cfheader name='Expires' value='##getHTTPTimeString(now() + expireDays)##'>
		<cfheader name='Cache-Control' value='max-age=##86400 * expireDays##'>		
		<cfheader name='ETag' value='''##etag##'''>

		<cfif len( CGI.HTTP_IF_NONE_MATCH ) && ( CGI.HTTP_IF_NONE_MATCH CT etag )>
			<!--- etag matches, return 304 !--->
			<cfheader statuscode='304' statustext='Not Modified'>
			<cfcontent reset='##true##' type='##mimetype##'><cfabort>
		</cfif>

		<!--- file was not cached; send the data !--->
		<cfcontent reset='##true##' type='##mimetype##' variable='##toBinary( data )##'><cfabort>
	<cfelse>

		<cfcontent reset='##true##'><cfoutput>data:image/{mimeType};base64,##data##</cfoutput><cfabort>
	</cfif>
">

<cfset image = fileReadBinary( src )>

<cfset base64image = toBase64( image )>

<!---cfset etag = replace( createUUID(), '-', '', 'all' )!--->
<cfset etag = hash( base64image )>

<cfset content = trim( template )>
<cfset content = replace( content, "{base64image}", base64image )>
<cfset content = replace( content, "{etag}", etag, 'all' )>
<cfset content = replace( content, "{mimeType}", mimeType, 'all' )>

<cfset filepath = expandPath( "img/#getFileFromPath( src )#.cfm" )>

<cfset fileWrite( filepath, content )>


<cfoutput>

	<p>Generated file #filepath#
</cfoutput>