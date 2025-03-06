<cfscript>
    param name="url.default" value="false";
    param name="url.preserve" value="true";
    if (url.default){
        st = {
            camelCase: "default"
        };
        echo( st.toJson() );
    } else if (url.preserve) {
        processingDirective preserveCase=true {
            st = {
                camelCase: true
            };
            echo(st.toJson());
        };
    } else {
        processingDirective preserveCase=false {
            st = {
                camelCase: false
            };
            echo(st.toJson());
        };
    }
</cfscript>