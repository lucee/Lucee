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
            writeoutput(structKeyList(animals));    
        }

        if(FORM.scene == 5) {
            animals=$[
                Aardwolf:"Proteles cristata",
                aardvark:"Orycteropus afer",
                alliGator:"Mississippiensis",
                Albatross:"Diomedeidae"
            ]
            writeoutput(structKeyList(animals));    
        }
    </cfscript>
    <cfcatch>
        <cfoutput>#cfcatch.message#</cfoutput>
    </cfcatch>
</cftry>