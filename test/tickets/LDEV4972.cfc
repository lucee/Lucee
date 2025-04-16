component extends="org.lucee.cfml.test.LuceeTestCase"  {

	
	function run( testResults , testBox ) {
		describe( "test case for LDEV-4972", function() {
			it( title="use empty constructor on the JavaProxy class based on a component", body=function( currentSpec ) {
				
				// first we load the cfc with relative path
				var cfc=new LDEV4972.MyString("four");
				expect( cfc.length() ).toBe(4);

				// create a java proxy class of type CharSequence
				var obj=JavaCast("java.lang.CharSequence",cfc);
				expect( obj.length() ).toBe(4);
				
				// get the class and load a new instance with the default constructor
				var obj=createObject("java",obj.getClass());
				expect( obj.length() ).toBe(0);
			});

			it( title="use empty constructor on the JavaProxy class based on a sub component", body=function( currentSpec ) {
				
				// first we load the cfc with relative path
				var cfc=new LDEV4972.MyString$Sub("four");
				expect( cfc.length() ).toBe(4);

				// create a java proxy class of type CharSequence
				var obj=JavaCast("java.lang.CharSequence",cfc);
				expect( obj.length() ).toBe(4);
				
				// get the class and load a new instance with the default constructor
				var obj=createObject("java",obj.getClass());
				expect( obj.length() ).toBe(0);
			});
		});
	}
}
/*component name="Sub" {
    variables.text="";
    
    function init(text) {
        variables.text=text;
    }

    function length() {
        return len(variables.text);
    }
}*/