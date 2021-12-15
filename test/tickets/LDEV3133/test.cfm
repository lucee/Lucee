<cftry>
    <cfscript>
            param name="FORM.scene" default="";
            if(FORM.scene == 4) {
                    animals=${
                        Aardwolf:"Proteles cristata",
                        aardvark:"Orycteropus afer",
                        AlliGator:"Mississippiensis",
                        albatross:"Diomedeidae"
                    }
                }
            }
    </cfscript>
    <cfcatch>
        <cfoutput>#cfcatch.message#</cfoutput>
    </cfcatch>
</cftry>