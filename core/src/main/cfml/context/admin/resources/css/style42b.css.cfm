<cfsetting showdebugoutput="no">
<cfsilent>
		
	<cfapplication name='__LUCEE_STATIC_CONTENT' sessionmanagement='#false#' clientmanagement='#false#' applicationtimeout='#createtimespan( 1, 0, 0, 0 )#'>
	
	<cfset mimetype = "text/css" />
	<cfset etag = hash( getCurrentTemplatePath() & '-' & Server.lucee.Version ) />

	<cfheader name='Expires' value='#getHttpTimeString( now() + 100 )#'>
	<cfheader name='Cache-Control' value='max-age=#86400 * 100#'>		
	<cfheader name='ETag' value='#etag#'>
	
	<cfif false and len( CGI.HTTP_IF_NONE_MATCH ) && ( CGI.HTTP_IF_NONE_MATCH == '#etag#' )>

		<!--- etag matches, return 304 !--->
		<cfheader statuscode='304' statustext='Not Modified'>
		<cfcontent reset='#true#' type='#mimetype#'><cfabort>
	</cfif>
	
	<!--- file was not cached; send the data --->
	<cfcontent reset="yes" type="#mimetype#" />
	
	<!--- PK: this style tag is here, so my editor color-codes the content underneath. (it won't get outputted) --->
	<style type="text/css">
	
</cfsilent><!---

--->html, body {
	min-height: 450px;
	height: 100%;
}
body.web, body.server {
	min-width:600px;
	background:#f7f7f7 url(../img/web-back.png.cfm) repeat-x top;
	margin:0;
	padding:0;
}
body.server {
	background-image:url(../img/server-back.png.cfm);
}
body, td, th {
	font-family:'Helvetica Neue', Arial, Helvetica, sans-serif;
	font-size : 12px;
	color:#3c3e40;
}
table {
	border-collapse:collapse;
}
h1, h2, h3, h4, h5 {
	font-weight:normal;
	font-size : 18px;
	color:#007bb7;
	margin:0;
	padding:0 0 4px 0;
}
h1 {padding-bottom:10px}
h2 {font-size:16px;}
h3 {font-size:14px;}
h4 {font-size:12px;}
h5 {font-size:10px;}
* + h1, * + h2 {
	padding-top: 20px;
}
div.pageintro + h2 {
	padding-top:0;
}

table + h3, div + h3 {
	padding-top: 10px;
}
a {
	color:#007bb7;
	text-decoration:underline
}
img, a img { border:0; }
form, div { margin:0; padding:0; }
pre {
	padding: 0px;
	white-space: pre-wrap; /* css-3 */
	white-space: -moz-pre-wrap !important; /* Mozilla, since 1999 */
	white-space: -pre-wrap; /* Opera 4-6 */
	white-space: -o-pre-wrap; /* Opera 7 */
	word-wrap: break-word; /* Internet Explorer 5.5+ */
	word-wrap:break-word;
}

.clear { clear:both }
.right { text-align:right; }
.left { text-align:left; }
.center { text-align:center }







/* site main layout */
#layout {
	min-height: 100%;
	max-width: 100%;
	width: 1000px;
	margin:0px auto;
}
body.full #layout {
	width:100%;
}

#layouttbl {
	width: 100%;
	height: 100%;
	padding: 0;
	margin: 0;
	border-collapse: collapse;
	border: 0;
}
td.lotd {
	vertical-align: top;
	padding: 0;
	margin: 0;
}

#logo {
	margin:34px 0 0 0;
	padding:5px 0px;
}
td#logotd {
	height: 113px;
}
body.full #logo {
	margin: 0 0 0 5px;
}
body.full td#logotd {
	height: 34px;
}

#logo h2 {
	display:none;
}
body.server #logo a {
	background-image:url(../img/server-lucee.png.cfm);
}
body.server.full #logo a {
	background-image:url(../img/server-lucee-small.png.cfm);
}

#layouttbl td#tabstd {
	vertical-align: bottom;
	text-align: right;
}
#tabstd a, #tabstd img {
	margin: 0;
	padding: 0;
	vertical-align: bottom;
}

#leftshadow, #rightshadow {
	width:11px;
	background:transparent url(../img/shadow-left.gif.cfm) no-repeat 0px 77px;
}
#rightshadow {
	background-image:url(../img/shadow-right.gif.cfm);
}

td#navtd {
	width:170px;
	height: auto;
	background-color:#e6e6e6;
	border-top-left-radius: 10px;
	border-right:1px solid #d2d2d2;
}
#nav {
	padding:10px 10px 40px 10px;
}
#nav form {
	white-space: nowrap;
}
#navsearch {
	margin: 10px 0 0 10px;
	width: 105px;
	height: 20px;
}

#contenttd {
	background-color: #fff;
}
#content {
	padding:30px 20px 10px 20px;
}
#innercontent {
	margin:30px 18px 10px 10px;
}

#copyright {
	padding: 10px 0px 0px 30px;
	text-align:left;
	font-size : 8pt;
	color:#666;
}
#copyright a {
	color:#666;
}
#copyrighttd {
	height: 40px;
}

/* page title */	
#maintitle {
	height:29px;
	border:1px solid #cdcdcd;
	border-radius:5px;
	background:#f0f0f0 url(../img/box-bg.png.cfm) repeat-x 0px -1px;
	padding-left:10px;
}
#maintitle span.box {
	line-height:30px;
}
#maintitle a.navsub {
	float:right;
	width:75px;
	height:29px;
	border-left:1px solid #cdcdcd;
	line-height:27px;
	text-align:center;
}

/* favorites in main title bar */
#favorites {
	float:right;
	width: 100px;
	height: 29px;
	margin: 0px;
	border-left:1px solid #cdcdcd;
	text-align:left;
}
#favorites:hover, #favorites:hover ul {
	display: block;
	background-color: #ddd;
}
#favorites ul {
	display: none;
	border:1px solid #cdcdcd;
	width:200px;
	position: absolute;
	z-index: 2;
	background-color: #e6e6e6;
	margin: 0px 75px 0 -101px;
	padding: 0px;
	text-align: left;
	border-radius: 0px 0px 5px 5px;
	overflow: hidden;
}
#favorites li {
	list-style: none;
	border-top: 1px solid #ccc;
	padding: 0;
	margin: 0;
}
#favorites li:first-child {
	border-top: 0px;
}
#favorites li.favtext {
	padding: 5px;
}
#favorites li.favorite a {
	background:url(../img/star_icon_small.png.cfm) no-repeat 6px;
	padding: 10px 5px 10px 22px;
	display: block;
}
#favorites li.favorite a:hover {
	background-color: #ccc;
}
#favorites > a {
	height:29px;
	z-index:3;
	background:url(../img/star_icon.png.cfm) no-repeat 13px;
	padding-left: 35px;
	display: block;
	line-height: 29px;
}
#favorites > a.favorite_inactive {
	background-image: url(../img/star_icon_grey.png.cfm);
}


	/* text under title */
div.pageintro {
	margin: 0 0 20px 0;
}
/* intro text for a section (i.e. under an h2; above a table) */	
.itemintro {
	font-style:italic;
	margin: -3px 0 10px 0;
}


/* tables */
table {empty-cells:show;}
td, th {
	padding:3px;
	vertical-align:top;
}
th {/* like .tblHead */
	background-color:#f2f2f2;
	color:#3c3e40;
	font-weight:normal;
	text-align:left;
}
table.nospacing {
	border-collapse:collapse;
}
/*.tblHead{padding-left:5px;padding-right:5px;border:1px solid #e0e0e0;background-color:#f2f2f2;color:#3c3e40}
.tblContent			{padding-left:5px;padding-right:5px;border:1px solid #e0e0e0;}
*/
tr.OK td {background-color:#e0f3e6;}
tr.notOK td {background-color:#f9e0e0;}
.tblContentRed		{padding-left:5px;padding-right:5px;border:1px solid #cc0000;color:red}
.tblContentGreen	{padding-left:5px;padding-right:5px;border:1px solid #009933;}
.tblContentYellow	{padding-left:5px;padding-right:5px;border:1px solid #ccad00;background-color:#fff9da;}
/* tables */
.maintbl {
	width:100%;
}
.maintbl + .maintbl {
	margin-top: 15px;
}
.autowidth {
	width: auto;
}
.maintbl td, .maintbl th {
	padding: 3px 5px;
	font-weight:normal;
	empty-cells:show;
	border:1px solid #e0e0e0;
}
.longwords {
	word-break:break-all;
}
.maintbl th {
	text-align:left;
}
.maintbl > tbody > tr > th {/* like .tblHead */
	width: 30%;
}
.maintbl tfoot td {
	border:none;
}
td.fieldPadded {
	padding-top:10px;
	padding-bottom:10px;
}


/* display boxes etc. */
.commentError{font-size : 10px;color:#cc0000;text-decoration:none;}
.comment{
	font-size:11px;
	color:#787a7d;
	text-decoration:none;
	font-style: italic;
	padding: 1px 0px;
	/* PK: when using mouseover helptexts, this padding might be better:
	 * padding:2px 0 5px 0;
	*/
}
.important {
	color:red !important;
}
.commentHead{font-size : 11px;color:#DFE9F6;}
div.comment + * {
	margin-top: 5px;
}
.checkbox + .comment, .radio + .comment, .radiolist label + .comment.inline {
	display:inline;
	padding-left:10px;
}
h3 + .comment {
	padding-top:0px;
}

.normal {
	border:1px solid #e0e0e0;
	padding:5px;
	margin:10px 0px;
	color:#3c3e40;
}

div.error, div.warning, div.message {
	border:1px solid red;
	padding:5px;
	margin:10px 0px;
	color:red;
}
div.warning {
	border-color: #FC6;
	color:#C93;
}
div.message {
	border-color: #0C0;
	color:#000;
}
div.ok {
	border-color: #e0e0e0;
	color:#e0e0e0;
}



.coding-tip-trigger {

	cursor: pointer; 
	color: #007bb7;
}

.coding-tip { 

	display: none; 
	box-sizing: border-box;
	color: #FFF;
	margin: 0.25em;
	padding: 0.5em;
	border: 1px solid #667;
	border-radius: 0.5em;
	max-width: 680px;
	background-color: #007bb7;
}

.coding-tip.expanded { 

	display: block;
}

.coding-tip code {

	white-space: pre-wrap;
	tab-size: 4;
	margin: 0.5em;
	padding: 0.5em;
	background-color: #CCC;
	color: #222;
	display: block;
	cursor: pointer;
}

.admin-server .coding-tip-trigger {

	color: #9c0000;
}

.admin-server .coding-tip {

	background-color: #9c0000;
}


/* unorganized */
.box {
	font-weight:normal;
	font-family:'Helvetica Neue', Arial, Helvetica, sans-serif;
	font-size : 14pt;
	color:#007bb7;
}
div.hr{border-color:red;border-style:solid;border-color:#e0e0e0;border-width:0px 0px 1px 0px;margin:0px 16px 4px 0px;}

td.inactivTab{border-style:solid;border-color:#e0e0e0;padding: 0px 5px 0px 5px;background-color:white;}
a.inactivTab{color:#3c3e40;text-decoration:none;}

td.activTab{border-style:solid;border-color:#e0e0e0;border-width:1px 1px 0px 1px ;padding: 2px 10px 2px 10px;background-color:#e0e0e0;}
a.activTab{font-weight:bold;color:#3c3e40;text-decoration:none;}

td.tab {border-color:#e0e0e0;border-width:1px;border-style:solid;border-top:0px;padding:10px;background-color:white;}
td.tabtop {border-style:solid;border-color:#e0e0e0;border-width:0px 0px 1px 0px ;padding: 0px 1px 0px 0px;}


.CheckOk{font-weight:bold;color:#009933;font-size : 12px;}
.CheckError{font-weight:bold;color:#cc0000;font-size : 12px;}



/* forms */
input,select,textarea {
  box-sizing: border-box;
  -moz-box-sizing: border-box;
  -webkit-box-sizing: border-box;
}
input {
	background: url('../img/input-shadow.png.cfm') repeat-x 0 0;
	background-color:white;
	padding:3px 2px 3px 3px;
	margin:3px 1px;
	color:#3c3e40;
	border:1px solid;
	border-color: #aaa #ddd #ddd #aaa;
}

select {
	font-size:11px;
	color:#3c3e40;
	margin:3px 0px;
	padding:1px 2px 0px 3px;
}
.button, .submit {
	display: inline-block;
	outline: none;
	text-align: center;
	text-decoration: none;
	padding:3px 10px;
	width: auto;
	overflow: visible;
	-webkit-border-radius: 5px;
	-moz-border-radius: 5px;
	border-radius: 5px;
	border-color: #777;
	color:#3c3e40;
	font-weight:bold;
	font-size: 11px;

	background: #f2f2f2;
	background: -webkit-gradient(linear, left top, left bottom, from(#fff), to(#ddd));
	background: -moz-linear-gradient(top,  #fff,  #ddd);
	filter:  progid:DXImageTransform.Microsoft.gradient(startColorstr='#ffffff', endColorstr='#dddddd');
}
input.reset {
	display:none;
}
.button:hover {
	text-decoration: none;
}
.button:active {
	position: relative;
	top: 1px;
}
/*
.button,.submit,.reset {
	background:#f2f2f2 url('../img/input-button.png.cfm') repeat-x 0 0;
	color:#3c3e40;
	font-weight:bold;
	padding:3px 10px;
	margin:0px;
	border-color: #777;
	border-radius:5px;
}
*/
.btn-mini {
	display:inline-block;
	outline:none;
	height:18px;
	border:1px solid #aaa;
	-webkit-border-radius: 3px;
	-moz-border-radius: 3px;
	border-radius: 3px;
	background: #f2f2f2;
}
a.edit {
	background-image: url(../img/edit.png.cfm); /* fallback */
	background-image: url(../img/edit.png.cfm), -webkit-linear-gradient(top, #fff,#ddd); /* Chrome 10+, Saf5.1+ */
	background-image: url(../img/edit.png.cfm),	-moz-linear-gradient(top, #fff,#ddd); /* FF3.6+ */
	background-image: url(../img/edit.png.cfm),	 -ms-linear-gradient(top, #fff,#ddd); /* IE10 */
	background-image: url(../img/edit.png.cfm),	  -o-linear-gradient(top, #fff,#ddd); /* Opera 11.10+ */
	background-image: url(../img/edit.png.cfm),         linear-gradient(top, #fff,#ddd); /* W3C */
	background-repeat: no-repeat;
	background-position: center;
	line-height:18px;
	width:20px;
}
.btn-search {
	background-image: url(../img/search_icon.png.cfm); /* fallback */
	background-image: url(../img/search_icon.png.cfm), -webkit-linear-gradient(top, #fff,#ddd); /* Chrome 10+, Saf5.1+ */
	background-image: url(../img/search_icon.png.cfm),	-moz-linear-gradient(top, #fff,#ddd); /* FF3.6+ */
	background-image: url(../img/search_icon.png.cfm),	 -ms-linear-gradient(top, #fff,#ddd); /* IE10 */
	background-image: url(../img/search_icon.png.cfm),	  -o-linear-gradient(top, #fff,#ddd); /* Opera 11.10+ */
	background-image: url(../img/search_icon.png.cfm),    linear-gradient(top, #fff,#ddd); /* W3C */
	vertical-align:bottom;
	background-repeat: no-repeat;
	background-position: center;
	height:20px;
	width:20px;
}
.btn-mini span {
	display:none;
}
label:hover {
	/* background-color:#f6f6f6; */
	cursor:pointer;
	border-bottom:1px dotted #666;
}
.checkbox, .radio {
	border:0px;
	background-image: none;
	background-color: transparent;
}
.radiolist {
	list-style:none;
	padding:0;
	margin:0;
}
.radiolist > li {
	clear:both;
}
.radiolist.float li {
	float:left;
	padding-right:0px;
	clear:none;
}
.radiolist .comment {
	padding-left:20px;
}
.radiolist label b {
	font-weight:normal;
}
.radiolist table {
	margin-left:20px;
}
.InputError{
	background:#fae2e2 url('../img/input-shadow-error.png.cfm') repeat-x 0 0;
}
.xlarge {width:96%} .autowidth .xlarge {width:400px;}
.large  {width:60%} .autowidth .large {width:250px;}
.medium {width:40%} .autowidth .medium {width:150px;}
.small  {width:20%} .autowidth .small {width:80px;}
.xsmall {width:10%} .autowidth .xsmall {width:40px;}
.number { width:40px; text-align:right }

/* menu */
#menu, #menu ul {
	list-style-type:none;
	margin: 0;
	padding: 0;
}
#menu {
	margin:10px 0px 0px 0px;
}
#menu a {
  display: block;
  text-decoration: none;	
}

#menu li {margin-top: 1px;}

#menu li a {
	margin:8px 0px 3px 0px;
	color:#333;
	font-weight:bold;
	font-size : 9pt;
	padding-left: 10px;
	background:url('../img/arrow-right.gif.cfm') no-repeat left;
}
#menu li a:hover, #menu li.collapsed a:hover {
	color:#000;
	background-image:url('../img/arrow-active.gif.cfm');
}
#menu > li a {
	background-image:url('../img/arrow-down.gif.cfm');
}
#menu > li a:hover {
	background-image:url('../img/arrow-down-active.gif.cfm');
}
#menu li.collapsed a {
	background-image:url('../img/arrow.gif.cfm');
}


#menu li li a {
	margin:0px 0px 0px 10px;
	font-weight:normal;
	text-decoration:none;
	color:#007bb7;
	font-size : 11px;
	background-image:url('../img/arrow.gif.cfm');
}
#menu li li a:hover, #menu li li a.menu_active {
	text-decoration:none;
	color:#007bb7;
	background-image:url('../img/arrow-active.gif.cfm');
}
/*
#menu li li.favorite a, #menu li li.favorite a:hover, #menu li li.favorite a.menu_active {
	background-image:url(../img/star_icon_small.png.cfm) !important;
	padding-left:14px;
	font-weight:bold;
}
*/
#menu li li a.menu_active {
	font-weight:bold;
}




/* server admin */
body.server {background-image:url('../img/server-back.png.cfm')}
body.server .box, body.server h1, body.server h2, body.server h3, body.server h4, body.server a, body.server #menu li ul li a
, body.server #menu li ul li a:hover, body.server #menu li ul li a.menu_active, body.server .extensionthumb a:hover
{color:#9c0000}

/* percentage bars: <div class="percentagebar"><div style="width:60%"></div></div> */
div.percentagebar {
	height:13px;
	font-size: 10px;
	border:1px solid #999;
	background-color: #d6eed4;
	position: relative;
}
div.percentagebar div {
	height:100%;
	overflow:hidden;
	font-size: 10px;
	background-color:#eee2d4;
	border-right:1px solid #999;
	padding-left:2px;
	display: flex;
}
div.percentagebar span {
	position: absolute;
	top:0px;
	left:3px;
	height:100%;
	font-size: 10px;
}
}

div.percentagebar span {
	position: absolute;
 	top:0px;
 	left:3px;
 	height:100%;
 	font-size: 10px;
}




.optionslist {border:0; border-collapse:collapse; width:auto;}
.optionslist td, .optionslist th { padding:3px; vertical-align:top; border:0 !important;}
.contentlayout { border-collapse:collapse; width:100%; }
/*
.contentlayout td, .contentlayout th { border:0; }
*/


/* filter form */
.filterform {
	padding:5px;
	margin:10px 0px;
	border:1px solid #e0e0e0;
	background-color:#f2f2f2;
	color:#3c3e40
}
.filterform ul {
	list-style:none;
	margin:0;
	padding:0;
}
.filterform li {
	width: auto;
	float:left;
	padding-right: 10px;
}
.filterform label {
	width: 200px;
	height:18px;
	display:block;
}
.filterform input.txt, .filterform select {
	width: 200px;
}
.filterform input.submit {
	margin-top: 20px;
}




/* module Extensions > Providers */
tbody#extproviderlist td {
	height:40px;
	vertical-align:middle;
}
/* module Extensions > Applications */
/* extensions overview */
.extensionlist {
	margin-bottom: 20px;
}
.extensionthumb {
	width:148px;
	height:108px;
	overflow: hidden;
	margin:5px 5px 0px 0px;
	float:left;
	text-align:center;
}
.extensionthumb a {
	display:block;
	padding:2px;
	height: 102px;
	text-decoration:none !important;
	border: 1px solid #E0E0E0;
}
.extensionthumb a:hover {
	background-color:#f8f8f8;
	border-color: #007bb7;
}
.extimg {
	height:50px;
}
/* install extension*/
textarea.licensetext {
	height:200px;
	width:100%;
	font-family:"Courier New",Courier,monospace;
	font-size : 8pt;
	color:##595F73;
	border: 1px solid #666;
}


/* page Overview / home */
div.classpaths {
	font-family:"Courier New",Courier,monospace;
	font-size: 10px;
	overflow:auto;
	max-height:100px;
	border:1px solid #333;
}
div.classpaths div {
	padding:1px 5px;
}
div.classpaths div.odd {
	background-color:#d2e0ee;
}


/* module remote > Security key */
#remotekey {
	text-align:center;
	font-size:16px;
	color:#007bb7;
	width:450px;
	padding:10px;
	background-color:white;
	border: 1px solid #595F73;
}
body.server #remotekey {
	color:#9c0000;
}

/* tooltips */
.helptextimage {
	width:16px;
	height:16px;
	display:inline-block;
	background: url(../img/info.png.cfm) no-repeat;
	vertical-align:text-bottom;
}

.helptextimage .inner {
	display:none;
}
th .helptextimage {
	float:right;
}
.radiolist li .helptextimage {
	float:left;
	padding-right:10px;
}
div.tooltip {
	position: absolute;
	background-color: #4D4D4D;
	padding: 2px 6px 2px 6px;
	color: #FFFFFF;
	z-index: 100;
	max-width:400px;
}
.tooltip .arrow {
	content: '';
	background: transparent url(../img/arrow_tooltip.png.cfm) no-repeat left top;
	width: 7px;
	height: 4px;
	position: absolute;
	left: 16px;
	bottom: -4px;
}
.removeClickOverlay {
	position: fixed;
	width: 100%;
	height: 100%;
	z-index: 99;	
}
.tooltip form {
	margin: 10px 10px 10px 10px;	
}
.tooltip table {
	color: #FFFFFF;
	width: 100%;	
}
.tooltip table tr td:first-child {
	font-weight: bold;	
}
.tooltip table td {
	padding: 5px 3px;
}

.fLeft{
	position: relative;
	float: left;
	width:33%;
}
.smBtn {
    background: #333 none repeat scroll 0 0;
    border-radius: 5px;
    border-width: 0;
    color: #efede5;
    display: inline-block;
    font-size: 11px;
    font-weight: bold;
    outline: medium none;
    overflow: visible;
    padding: 3px 5px;
    text-align: center;
    text-decoration: none;
    width: auto;
}
.pdTop{
    padding-top: 3%;
}
.fs {
    font-size: 13px !important;
}
.alertMsg{
	color: red;
    font-size: 11px;
    font-weight: bold;
    margin: 5px 0 ;
    padding: 5px;
}

/* css for tag, object, and function documentation */
.syntaxTag, .syntaxFunc{color:#993300;}
.syntaxText {color:#CC0000;}
.syntaxAttr {color:#000099;}
.syntaxType {color:##000099;}

:-moz-placeholder {
	color: #ccc !important;
}
::-webkit-input-placeholder {
	color: #ccc !important;
}



			body 	{ margin: 0; padding: 0; }

			#page	{ margin: 0 auto; width: 960px; height: 100%; background-color: #999; position: relative; }


			/** reusable util classes */
			.clearfix:before, .clearfix:after { content: " "; display: table; }
			.clearfix:after { clear: both; }
			.clearfix 	{ *zoom: 1; /** IE 6/7 fix */ }

			.pos-l		{ position: absolute; left: 0; }
			.pos-r		{ position: absolute; right: 0; }			
			.pos-b		{ position: absolute; bottom: 0; }


			/** admin common properties */
			#tr-header				{ height: 113px; }
			body.full #tr-header	{ height: 63px; }

			#header			{ position: relative; height: 100px; -padding: 0 22px; -margin: 0 22px 0 22px; }
			body.full #header { height: 50px; }

			#logo			{ display: block; position: absolute; top: 0; left: 0; width: 100px; height: 68px; padding: 0; }
			body.full #logo { width: 70px; height:48px; background-size: 150px; background-position: -18px -50px; }

			#admin-tabs		{ width: 370px; position: absolute; bottom: -10px; right: -7px; }

			#admin-tabs	a 	{ display: block; width: 184px; height: 30px; float: left; }

			.sprite			{ background-image: url( '../img/admin-sprite.png.cfm' ); background-repeat: no-repeat; }
			
			.colshadow		{ width: 6px; height: 329px; }

			#resizewin 		{ display:block; width:22px; height:22px; text-indent: -9999px; background-position: -160px 0; }

			body.full #resizewin { background-position: -184px 0; }

			

			/** server-specific values */
			.-admin-server #header				{ background-color: #F33; }

			.admin-server #logo 				{ background-position: -22px -69px; }			
			.admin-server #admin-tabs .server 	{ background-position: -22px -193px; }
			.admin-server #admin-tabs .web 		{ background-position: -22px -299px; }


			/** web-specific values */
			.-admin-web #header					{ background-color: #33F; }

			.admin-web #logo					{ background-position: -22px 0; }
			.admin-web #admin-tabs .server 		{ background-position: -22px -227px; }
			.admin-web #admin-tabs .web 		{ background-position: -22px -264px; }


			#content	{ margin: 1em 2.5em; }

			.icon		{ background-image: url( '../img/admin-sprite.png.cfm' ); margin: 3px; width: 9px; height: 9px; cursor: pointer; }

			.plus		{ background-position: -55px -149px; }
			.minus		{ background-position: -55px -140px; }



			/** base64 icons begin */

			.icon-b64-expand { background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIRhI+hG7bwoJINIktzjizeUwAAOw==)
    							no-repeat left center; padding: 4px 0 4px 16px; }

			.icon-b64-contract { background: url(data:image/gif;base64,R0lGODlhCQAJAIABAAAAAP///yH5BAEAAAEALAAAAAAJAAkAAAIQhI+hG8brXgPzTHllfKiDAgA7)
    							no-repeat left center; padding: 4px 0 4px 16px; }

    		/** base64 icons end */



