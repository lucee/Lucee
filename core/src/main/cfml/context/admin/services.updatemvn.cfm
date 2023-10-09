<!---
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 --->
<cfscript>
	CACHE_IN_SECONDS=60;

	if(isNull(url.action2))url.action2="none";
	error.message="";
	error.detail="";
</cfscript>

<cfif request.admintype EQ "web"><cflocation url="#request.self#" addtoken="no"></cfif>

<!--- only available for server --->
<cfadmin
    action="getloaderinfo"
    type="#request.adminType#"
    password="#session["password"&request.adminType]#"
    returnVariable="loaderInfo">
<cfset error.message="">
<cfset error.detail="">

 <cfscript>
	include template="ext.functions.cfm";
	//include template="services.update.functions.cfm";


	
	hasOptions=false;

	admin
			action="getUpdate"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnvariable="upd";
	

	stText.services.update.downUpDesc=replace(stText.services.update.downUpDesc,'{version}',server.lucee.version);

		version = "lucee";
		
		versionsStr = {};
		versionsStr.snapShot = {};
		versionsStr.pre_Release= {};
		versionsStr.release = {};
		if(version eq 'custom'){
			versionsStr.custom = {};
		}
		for(type in versionsStr){
			versionsStr[type].upgrade = [];
			versionsStr[type].downgrade = [];
		}
		if(version eq 'custom' &&  Len(otherVersions)){
			for(versions in otherVersions){
				if(toVersionSortable(versions) LTE toVersionSortable(server.lucee.version)){
					arrayPrepend(versionsStr.custom.downgrade, versions);
				}
				else{
					arrayPrepend(versionsStr.custom.upgrade, versions);
				}
				hasOptions=true;
			}
		}
		
		admin
			action="getMinVersion"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnvariable="minVersion";
		minVs = toVersionSortable(minVersion);
		otherVersions=MavenListVersions();

		if(!isNull(otherVersions) && len(otherVersions)){

			for(versions in otherVersions ){
				if(versions EQ server.lucee.version) cfcontinue;
				vs=toVersionSortable(versions);
				if(vs LT minVS) cfcontinue;
				;
				if(FindNoCase("SNAPSHOT", versions)){
					if(vs LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.SNAPSHOT.downgrade, versions);
					} 
					else{
						arrayPrepend(versionsStr.SNAPSHOT.upgrade, versions);
					}
					hasOptions=true;
				} 
				else if(FindNoCase("ALPHA", versions) || FindNoCase("BETA", versions) || FindNoCase("RC", versions)){
					if(vs LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.pre_Release.downgrade, versions);
					} else{
						arrayPrepend(versionsStr.pre_Release.upgrade, versions);
					}
					hasOptions=true;
				}
				else{
					if(vs LTE toVersionSortable(server.lucee.version)){
						arrayPrepend(versionsStr.release.downgrade, versions);
					} else{
						arrayPrepend(versionsStr.release.upgrade, versions);
					}
					hasOptions=true;
				}
			}
		}
	printError(error);

	currMajor=listFirst(server.lucee.version,".");

</cfscript>
<cfoutput>

<style>
	.btn {
		color:white;
		background-color:##CC0000;
	}
</style>

<cfif !hasOptions>
	<p><b>No upgrades or downgrades available!</b></p>
<cfelse>
	<!--- <h1>#stText.services.update.luceeProvider#</h1>--->
	<p>
		Current Version <b>( #server.lucee.version# )</b><br><br>
		#stText.services.update.titleDesc#
		<!--- #replace(stText.services.update.titleDesc,'{version}',"<b>"&server.lucee.version&"</b>") # --->
	</p>

	<cfset hiddenFormContents = "" >
	<cfset count = 1>
	<cfset listVrs = "Release,Pre_Release,SnapShot">
	<cfloop list="#listVrs#" index="key">
		<cfset len = 0>
		<cfset len = len(versionsStr[key].upgrade) + len(versionsStr[key].downgrade)>
		<span>
			<input
				<cfif count EQ 1>class="bl button alignLeft" <cfelseif count EQ StructCount(versionsStr)> class="br button" <cfelse> class="bm button" </cfif>  
				style="width:180px"
				name="changeConnection" 
				id="btn_#UcFirst(Lcase(key))#" 
				value="#stText.services.update.short[key]# (#len#)" 
				onclick="enableVersion('#UcFirst(Lcase(key))#');"  
				type="button"></span>
		<cfset count++>
	</cfloop>
	<cfsavecontent variable="tmpContent">
		<div  class="topBottomSpace">
			<div class="whitePanel">
				<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
					<select name="UPDATE" id="upt_version"  class="large">
						<!--- <option value="">--- select the version ---</option> --->
						<cfloop list="#listVrs#" index="key">
							<cfif len(versionsStr[key].upgrade) gt 0|| len(versionsStr[key].downgrade) gt 0>
								<optgroup class="td_#UcFirst(Lcase(key))#" label="#stText.services.update.short[key]#">
									<cfloop array="#versionsStr[key].upgrade#" index="i">
										<option class="td_#UcFirst(Lcase(key))#" value="#i#">#stText.services.update.upgradeTo# #i#</option>
									</cfloop>

									<cfloop array="#versionsStr[key].downgrade#" index="i">
										<option class="td_#UcFirst(Lcase(key))#" value="#i#">#stText.services.update.downgradeTo# #i#</option>
									</cfloop>
								</optgroup>
							<cfelseif len(versionsStr[key].upgrade) eq 0>
								<cfset session.empUpgrade = true>
							</cfif>
						</cfloop>
						<cfif isdefined("session.empUpgrade") && session.empUpgrade eq true>
							<option value="">--- select the version ---</option>
						</cfif>
					</select>
					<input type="button" class="button submit"
						onclick="changeVersion(this, UPDATE)" 
						name="mainAction" 
						id="disableButton"
						value="#stText.services.update.downUpBtn#">
						<span class="msg"></span>
					<div class="comment">
						<cfloop list="#listVrs#" index="key">
							<div class="itemintro"><b>#stText.services.update.short[key]# :</b> #stText.services.update[key&"Desc"]#</div>
						</cfloop>
					</div>
				</cfformClassic>
			</div>
		</div>
	</cfsavecontent>

	<div id="updateInfoDesc" style="text-align: center;"></div>
	<div id="group_Connection">
		#tmpContent#
	</div>
</cfif>
	
	<cfhtmlbody>
		<script type="text/javascript">
			$(document).ready(function(){
				if($('##sp_radio_custom').prop("checked")){
					$( '##customtextinput' ).attr( 'disabled', false);
					$('##customURL').attr( 'disabled', false);
				}
				if('#server.lucee.state#' == 'SNAPSHOT')
					var version = 'Snapshot';
				else if('#server.lucee.state#' == 'RC')
					var version = 'Pre_release';
				else
					var version = 'Release';
				enableVersion(version, "intial");
				$("##btn_"+version).addClass("btn");
			});

			function enableVersion(v, i){
				$(".msg").text("");
				$("input[type='button'][name='changeConnection']").click(function(){
				    var a_myname = $("##btn_"+v).attr("class");
	 				if(a_myname.includes("btn") == false) {
						$('##disableButton').attr('disabled','disabled').css('opacity',0.5);
	 				}
					else if(a_myname.includes("btn") == true){
						$('##disableButton').attr("disabled", false).css('opacity',1);	
					}
				});
				if(i== 'intial'){
					$("##group_Connection").find('optgroup' ).each(function(index) {
						var xx = $(this).attr('class');
						window[xx] = $("."+xx).detach();
						if("td_"+v == xx){
							$("##upt_version").append(window[xx]);
						}
				  		$(".btn").removeClass('btn');
				  		$("##btn_"+v).addClass("btn");
					});
				} else {
					if($( "##btn_"+v).hasClass( "btn" )){
						window[v] = $(".td_"+v).detach();
						$("##btn_"+v).removeClass('btn');
					} else {
						if(v == "Snapshot"){
							$(".td_Pre_release").remove();
							$(".td_Release").remove();
						}
						if(v == "Pre_release"){
							$(".td_Snapshot").remove();
							$(".td_Release").remove();
						}
						if(v == "Release"){
							$(".td_Snapshot").remove();
							$(".td_Pre_release").remove();
						}
						$("##upt_version").append(window["td_"+v]);
						$(".btn").removeClass('btn');
						$("##btn_"+v).addClass('btn');
					}
				}
			}

			function changeVersion(field, frm) {
				if(frm.value == "") {
					$(".msg").text("");
					$( ".msg" ).append( "<div class='error'>Please Choose any version</p>" );
					disableBlockUI=true;
					return false;
				}
				else{
					submitted = true;
					$('##group_Connection').hide();
					url='?action=mvnchangeto&adminType=#request.admintype#&version='+frm.value;
					$(document).ready(function(){
						$('##updateInfoDesc').html('<img src="../res/img/spinner16.gif.cfm">');
						disableBlockUI=true;

				 		$.get(url, function(response) {
				      		field.disabled = false;

				 			if((response+"").trim()=="")
								window.location=('#request.self#?action=#url.action#'); //$('##updateInfoDesc').html("<p>#stText.services.update.restartOKDesc#</p>");
							else
								$('##updateInfoDesc').html('<div class="error">'+response+'</div>');
								//window.location=('#request.self#?action=#url.action#'); //$('##updateInfoDesc').html(response);
				 		});
					});
				}
			}

			$('##sp_radio_custom').change(function(){
				// enable / disable custom URL field
				if($(this).prop("checked")){
					$( '##customtextinput' ).attr( 'disabled', false);
				} else{
					$( '##customtextinput' ).attr( 'disabled', true);
				}
			});
			$('##updateProvider').click(function(){
				$(".alertprovider").text("");
				var re = /(http(s)?:\\)?([\w-]+\.)+[\w-]+[.com|.in|.org]+(\[\?%&=]*)?/;
				var checkbox = document.getElementById("sp_radio_custom").checked;
				var text = $('##customtextinput').val();
				if(checkbox == true && (text.length == 0 || !re.test(text))) {
					$('##customtextinput').addClass("InputError");
					$( '##customtextinput' ).after( '</br><span class="alertprovider">Please provide the valid url</span>' );
					disableBlockUI=true;
					return false
				}
			});
		</script>
	</cfhtmlbody>

	<cfscript>
		loaderText = replaceNoCase(stText.services.update.loaderMinVersion,"{min-version}", "<b>#minVersion#</b>");
		loaderPath = replaceNoCase(stText.services.update.loaderPath,"{loaderPath}", '<b>'& loaderInfo.LoaderPath & '</b>' );
		//replace(stText.services.update.titleDesc2,'{context}',"<b class='error'>"&#expandPath("{lucee-server}\patches")#&"</b>");
	</cfscript>
	<p class="comment">#loaderText#</p>
	<p class="comment">#loaderPath#</p>	
</cfoutput>
