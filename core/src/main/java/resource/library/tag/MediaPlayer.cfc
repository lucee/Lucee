<cfcomponent>

<!--- Meta data --->
	<cfset this.metadata.attributetype="fixed">
    <cfset this.metadata.attributes=[
		"align": {required:false,type:"string",default:"left",hint:'Specifies the horizontal alignment of the media player. You can select from left, right, and center.'},
		"autoplay": {required:false,type:"boolean",default:false,hint:'A Boolean value that specifies if the media player must automatically play the FLV file on loading the CFM page'},
		"bgColor": {required:false,type:"string",default:"##6b7c85",hint:'The background color of the media player specified as a Hexadecimal value or or a recognized color name, for example red.'},
		"controlBar": {required:false,type:"boolean",default:true,hint:'A Boolean value that specifies if you want to display the control panel for the media player'},
		"hideBorder": {required:false,type:"boolean",default:true,hint:'A Boolean value that specifies if you want a border for the media player panel'},
		"hideTitle": {required:false,type:"boolean",default:false,hint:'If true, displays the title.'},
		"title": {required:false,type:"string",hint:'Title for the video, if not set, the video file name is used.'},
		"fullScreenControl": {required:false,type:"boolean",default:true,hint:'Whether full screen is enabled'},
		"height": {required:false,type:"numeric",default:"360",hint:'Height of the media player, in pixels.'},
		"width": {required:false,type:"numeric",default:"480",hint:'Width of the media player, in pixels.'},
		"name": {required:false,type:"string",hint:'Name of the media player. the name attribute is required when you invoke JavaScript functions.'},
		"onComplete": {required:false,type:"string",default:"",hint:'Custom JavaScript function to run when the FLV file has finished playing.'},
		"load": {required:false,type:"boolean",default:true,hint:'if set to true, start loading before play was pressed.'},
		"onLoad": {required:false,type:"string",default:"",hint:'Custom JavaScript function to run on loading of the player component.'},
		"onStart": {required:false,type:"string",default:"",hint:'Custom JavaScript function to run when the FLV file starts playing.'},
		"quality": {required:false,type:"string",default:"high",hint:'The quality of the media playback (only used for flash fallback)'},
		"source": {required:true,type:"any",hint:'The URL (absolute or relative to the current page) to the movie files, supported formats are (mp4,ogg,webm). not every Browser supports the same format, define as many formats possible. You can define the urls as string list or as array'},
		"delimiter": {required:false,type:"string",default:",",hint:'delimiter used to separate urls in source attribute, default is comma'},
		"style": {required:false,type:"string",default:"",hint:''},
		"wmode": {required:false,type:"string",default:"window",hint:'Specifies the absolute positioning and layering capabilities in your browser'},
		"image": {required:false,type:"string",default:"",hint:'image displayed when movie is stopped'},
		"printJSControls": {required:false,type:"boolean",default:false,hint:'it set to true, all possible JS Controls ar printed after the video.'}
		
	]>
    
    <cfset variables.swfPlayer="/mapping-tag/build/player.swf">
    <cfset variables.cssFile="/mapping-tag/build/mediaelementplayer.min.css.cfm">
    <cfset variables.jsFile="/mapping-tag/build/mediaelement-and-player.min.js.cfm">
    <cfset variables.jqFile="/mapping-tag/build/jquery.js.cfm">

    <cffunction name="init" output="yes" returntype="void"
      hint="invoked after tag is constructed">
    	<cfargument name="hasEndTag" type="boolean" required="yes">
      	<cfargument name="parent" type="component" required="no" hint="the parent cfc custom tag, if there is one">
        <cfset lst=GetBaseTagList()>
        
        
        <!--- add css/js --->
        <cfset var embeddingStuff="">
        <cfsavecontent variable="embeddingStuff" trim="true">
<cfoutput>
<script src="#variables.jqFile#"></script>	
<script src="#variables.jsFile#"></script>
<link rel="stylesheet" href="#variables.cssFile#" />
</cfoutput> 
        </cfsavecontent>
        
		<cfset var htmlhead="">
		<cfhtmlhead action="read" variable="htmlhead">
		<cfif not find(embeddingStuff,htmlhead)>
        	<cfhtmlhead action="append" text="#embeddingStuff#">
        </cfif>
        
  	</cffunction> 
    
    
    <cffunction name="onStartTag" output="yes" returntype="boolean">
   		<cfargument name="attributes" type="struct">
   		<cfargument name="caller" type="struct">
<cfsilent>	
	<!--- check --->
    	<!--- align --->
        <cfset attributes.align=trim(LCase(attributes.align))>
    	<cfif attributes.align NEQ "left" and attributes.align NEQ "center" and attributes.align NEQ "right">
        	<cfthrow message="invalid value for attribute align [#attributes.align#]"
                detail="value must be one of the following [left,center,right]">
        </cfif>
        
        <!--- source --->
        <cfset var src=[]>
        <cfif isArray(attributes.source)>
        	<cfset src=attributes.align>
        <cfelseif IsSimpleValue(attributes.source)>
        	<cfset src=ListToArray(attributes.source,attributes.delimiter)>
        <cfelse>
        	<cfthrow message="invalid type for attribute source"
                detail="value must be a string list or an array">
		</cfif>
        
        <!--- hideborder --->
        <cfif not attributes.hideborder>
        	<cfset attributes.width=attributes.width-1>
        	<cfset attributes.height=attributes.height-1>
        </cfif>
        
        <!--- title --->
        <cfif not structKeyExists(attributes,"title")>
        	<cfset attributes.title=listLast(src[1],'\/')>
            <cfset var index=len(listLast(attributes.title,'.'))>
            <cfset attributes.title=mid(attributes.title,1,(len(attributes.title)-index)-1)>
            
        </cfif>
        
        <!--- name --->
        <cfset var _id=getTickCount()>
        <cfif structKeyExists(attributes,"name")>
        	<cfif isValid("variableName",attributes.name)>
            	<cfset var name=attributes.name>
            <cfelse>
        		<cfthrow message="invalid value for attribute name [#attributes.name#]"
                	detail="value must define a valid variable name">
            </cfif>
        <cfelse>
        	<cfset var name="video_"&_id>
        </cfif>
        
        <!--- quality --->
        <cfset attributes.quality=trim(LCase(attributes.quality))>
    	<cfif attributes.quality NEQ "high" and attributes.quality NEQ "medium" and  attributes.quality NEQ "low">
        	<cfthrow message="invalid value for attribute quality [#attributes.quality#]"
                detail="value must be one of the following [low,medium,high]">
        </cfif>
        
        <!--- wmode --->
        <cfset attributes.wmode=trim(LCase(attributes.wmode))>
    	<cfif attributes.wmode NEQ "window" and attributes.wmode NEQ "opaque" and attributes.wmode NEQ "transparent">
        	<cfthrow message="invalid value for attribute wmode [#attributes.wmode#]"
                detail="value must be one of the following [window,opaque,transparent]">
        </cfif>
        
        
        
        
        <!---
		hideborder
		fullScreenControl
		--->
         <cfset var ext="">
<cfsavecontent variable="local.flashPart">
<object id="flash_#_id#" type="application/x-shockwave-flash" data="#variables.swfPlayer#" #getTagAttributes(attributes)#> 		
    <param name="movie" value="#variables.swfPlayer#" /> 
    <param name="allowfullscreen" value="true" />
    <cfif attributes.quality NEQ "hight"><param name="quality" value="#attributes.quality#" /></cfif>	
    <cfloop array="#src#" index="s">
        <cfset ext=right(trim(s),4)>
      	<cfif  ext EQ ".mp4" or ext EQ ".flv">
        	<param name="flashvars" value='controls=#attributes.controlbar#&amp;file=#fixPath(trim(s))#' />
		</cfif>
    </cfloop>
    	
    <!--- <cfif len(attributes.image)><img src="#fixPath(attributes.image)#"/></cfif>--->
</object>
</cfsavecontent>

<cfsavecontent variable="local.html5PartBegin">
    <video id="video_#_id#" preload="none" style="#getVideoStyle(attributes)#" #getTagAttributes(attributes)# <cfif len(attributes.image)>poster="#fixPath(attributes.image)#"</cfif>>
      <cfset var s="">
      <cfloop array="#src#" index="s">
      	#getSourceTag(trim(s))#  	
      </cfloop>
</cfsavecontent>

<cfsavecontent variable="local.html5PartEnd">
    </video>
</cfsavecontent>
   
</cfsilent>
<div style="#getDivStyle(attributes)##attributes.style#">
<cfif not attributes.hidetitle><div style="position:absolute;z-index:1000;text-align:center;width:#attributes.width#;font-family:Arial, Helvetica, sans-serif;">#attributes.title#</div>
</cfif><div  class="video-js-box" style="#getDivStyle(attributes)#">
#html5PartBegin##flashPart##html5PartEnd#
</div></div>        
<script>
var #name# = new MediaElementPlayer('##video_#_id#',{
	features: ['playpause','progress','current','duration','tracks','volume'<cfif attributes.fullScreenControl>,'fullscreen'</cfif>],
 	alwaysShowControls:false,
	flashName: 'player.swf.cfm',
	silverlightName: 'player.xap.cfm',
    
	success: function (mediaElement, domObject) {
            <cfif len(attributes.onLoad)>mediaElement.addEventListener('loadedmetadata', function(){#trim(attributes.onLoad)#(this);}, false);</cfif>
            <cfif len(attributes.onStart)>mediaElement.addEventListener('play', function(){#trim(attributes.onStart)#(this);}, false);</cfif>
            <cfif len(attributes.onComplete)>mediaElement.addEventListener('ended', function(){#trim(attributes.onComplete)#(this);}, false);</cfif>
        }

});
<cfif attributes.load>#name#.load();</cfif>
<cfif not attributes.controlbar>#name#.disableControls();<cfelse>#name#.hideControls();</cfif>
<cfif attributes.printJSControls>for(var key in #name#){
	document.write(key);
	document.write("<br>");
}
</cfif>
</script>
        <cfreturn false>
    </cffunction>
    
    <cffunction name="getTagAttributes" output="no" returntype="string" access="private">
    	<cfargument name="attributes" type="struct">
    	<cfargument name="forFlash" type="boolean" default="false">
   		<cfset var tag=getWH(attributes)>
    	
        <!--- autoplay --->
        <cfif not forFlash and attributes.autoplay>
        	<cfset tag&=' autoplay="autoplay"'>
        </cfif>
        <!--- controlbar
        <cfif not forFlash>
        	<cfset tag&=' controls="#attributes.controlbar?'controls':''#"'>
        </cfif> --->
        <!--- onstart
        <cfif not forFlash and len(attributes.onstart)>
        	<cfset var name=RandRange(1,100000)>
        	<cfset tag&=' onplay="#trim(attributes.onStart)#(this)"'>
        </cfif> --->
        <!--- oncomplete
        <cfif not forFlash and len(attributes.oncomplete)>
        	<cfset tag&=' onended="#trim(attributes.oncomplete)#(this)"'>
        </cfif> --->
        <!--- onload
        <cfif not forFlash and len(attributes.onload)>
        	<cfset tag&=' onloadedmetadata="#trim(attributes.onload)#(this)"'>
        </cfif> --->
        
        
        <cfreturn tag>
        
    </cffunction>
    
     <cffunction name="getWH" output="no" returntype="string" access="private">
    	<cfargument name="attributes" type="struct">
    	
   		<cfset var tag="">
    	<!--- height --->
        <cfif attributes.height GT 0>
        	<cfset tag&=' height="#attributes.height#"'>
        </cfif>
        <!--- width --->
        <cfif attributes.width GT 0>
        	<cfset tag&=' width="#attributes.width#"'>
        </cfif>
        <cfreturn tag>
        
    </cffunction>
    
    <!---<cffunction name="getSourceTag" output="no" returntype="string" access="private">
    	<cfargument name="uri" type="string">
   		
        <cfif right(arguments.uri,4) EQ ".mp4">
        	<cfreturn '<source src="#fixPath(uri)#" type=''video/mp4; codecs="avc1.42E01E, mp4a.40.2"'' />'>
         
		<cfelseif right(arguments.uri,5) EQ ".webm">
        	<cfreturn '<source src="#fixPath(uri)#" type=''video/webm; codecs="vp8, vorbis"'' />'>
         
		<cfelseif right(arguments.uri,4) EQ ".ogv" or right(arguments.uri,4) EQ ".ogg">
        	<cfreturn '<source src="#fixPath(uri)#" type=''video/ogg; codecs="theora, vorbis"'' />'>
         <cfelse>
         	<cfreturn '<source src="#fixPath(uri)#"/>'>
         </cfif>
    </cffunction>--->
    
    
    <cffunction name="getSourceTag" output="no" returntype="string" access="private">
    	<cfargument name="uri" type="string">
   		
        <cfif right(arguments.uri,4) EQ ".mp4">
        	<cfreturn '<source src="#fixPath(uri)#" type="video/mp4" />'>
         
		<cfelseif right(arguments.uri,5) EQ ".webm">
        	<cfreturn '<source src="#fixPath(uri)#" type="video/webm" />'>
         
		<cfelseif right(arguments.uri,4) EQ ".ogv" or right(arguments.uri,4) EQ ".ogg">
        	<cfreturn '<source src="#fixPath(uri)#" type="video/ogg" />'>
         <cfelse>
         	<cfreturn '<source src="#fixPath(uri)#"/>'>
         </cfif>
    </cffunction>
    
    
    <cffunction name="fixPath" output="no" returntype="string" access="private">
    	<cfargument name="path" type="string">
   		<cfreturn arguments.path>
    </cffunction>
    <cffunction name="getVideoStyle" output="no" returntype="string" access="private">
    	<cfargument name="attributes" type="struct">
   		<cfset var css="">
    	<!--- bgcolor --->
        <cfif len(attributes.bgcolor) and attributes.wmode NEQ "transparent">
        	<cfset css&="background-color:#attributes.bgcolor#;">
        </cfif>
        <cfreturn css>
    </cffunction>
    
    <cffunction name="getDivStyle" output="no" returntype="string" access="private">
    	<cfargument name="attributes" type="struct">
   		<cfset var css="">
    	<!--- align --->
        <cfif len(attributes.align)>
            <cfif attributes.align EQ "center">
        		<cfset css&="margin-right:auto;margin-left:auto;">
            <cfelseif attributes.align EQ "right">
        		<cfset css&="margin-left:auto;">
            </cfif>
        </cfif>
        <!--- height --->
        <cfif attributes.height GT 0>
        	<cfset css&='height:#attributes.height#px;'>
        </cfif>
        <!--- width --->
        <cfif attributes.width GT 0>
        	<cfset css&='width:#attributes.width#px;'>
        </cfif>
        <!--- hideborder --->
        <cfif not attributes.hideborder>
        	<cfset css&='border-style:solid;border-width:1px;border-color:#attributes.bgcolor#;'>
        </cfif>
        
    	<!--- bgcolor --->
        <cfif len(attributes.bgcolor) and attributes.wmode NEQ "transparent">
        	<cfset css&="background-color:#attributes.bgcolor#;">
        </cfif>
        
        
        <cfreturn css>
    </cffunction>



   
</cfcomponent>