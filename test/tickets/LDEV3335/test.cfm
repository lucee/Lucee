<cfparam name="FORM.scene" default="">
<cfparam name="compnt" default="">
<cfscript>
    if( form.scene == 1 ) {
        compnt = new testNoAccessors();
    }
    if( form.scene == 2 ) {
        compnt = new testMannual();
    }
    if( form.scene == 3 ) {
        compnt = new testWithAccessors();
    }
    writeoutput(sizeOf(compnt));
</cfscript>