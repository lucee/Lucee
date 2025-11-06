component extends="org.lucee.cfml.test.LuceeTestCase" {

    function beforeAll() {
        variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
    }

    function run( testResults, testBox ) {
        describe( "Testcase for LDEV-5027", function() {
            it( title="Allow empty default in updateCacheConnection()", body=function(){
                    admin
                        action="updateCacheConnection"
                        type="web"
                        password="#request.webadminpassword#"
                        name="#cacheName#"
                        class="lucee.runtime.cache.ram.RamCache"
                        storage="false"
                        default=""
                        custom="#{timeToLiveSeconds:86400
                            ,timeToIdleSeconds:86400}#";
            });
        });
    }

    function afterAll() {
        admin
            action="removeCacheConnection"
            type="web"
            password="#request.webadminpassword#"
            name="#cacheName#";
    }
}