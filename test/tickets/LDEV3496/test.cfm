<cfscript>
        public void function test() {
            loop key = "local.key" value = "local.value" struct = structNew() {
                if ( local?.value ) {}
            }
        }
        writeoutput("itsCompiled");
</cfscript>