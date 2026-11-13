component extends="org.lucee.cfml.test.LuceeTestCase" labels="exception" {

	function run( testResults, testBox ) {
		describe( title="LDEV-5579 stacktrace should not contain the dynamic invocation wrapper frames (lucee.invoc.wrap)", body=function() {

			it( "cfcatch.stacktrace of a java exception with a wrapper frame", function() {
				var ex = createObject( "java", "java.lang.IllegalStateException" ).init( "LDEV-5579" );
				var ste = createObject( "java", "java.lang.StackTraceElement" );
				var frames = [
					ste.init( "java.lang.Integer", "parseInt", "Integer.java", 662 ),
					ste.init( "lucee.invoc.wrap.v5.java.lang.Integer.parseInt_1ohgdbv88suv8", "apply", nullValue(), -1 ),
					ste.init( "lucee.runtime.reflection.pairs.MethodInstance", "invoke", "MethodInstance.java", 49 )
				];
				ex.setStackTrace( javaCast( "java.lang.StackTraceElement[]", frames ) );
				var st = "";
				try {
					throw( object=ex );
				}
				catch ( any e ) {
					st = e.stacktrace;
				}
				expect( st ).toInclude( "java.lang.Integer.parseInt(Integer.java:662)" );
				expect( st ).toInclude( "lucee.runtime.reflection.pairs.MethodInstance.invoke(MethodInstance.java:49)" );
				expect( st ).notToInclude( "lucee.invoc.wrap." );
			});

			it( "cfcatch.stacktrace of a java method called with dynamic invocation enabled", function() {
				var field = getDynamicInvocationField();
				var old = field.getBoolean( nullValue() );
				var st = "";
				field.setBoolean( nullValue(), true );
				try {
					var i = createObject( "java", "java.lang.Integer" );
					i.parseInt( "LDEV-5579" );
				}
				catch ( any e ) {
					st = e.stacktrace;
				}
				finally {
					field.setBoolean( nullValue(), old );
				}
				expect( st ).toInclude( "java.lang.Integer.parseInt" );
				expect( st ).notToInclude( "lucee.invoc.wrap." );
			});

		});
	}

	// lucee.dynamic.invocation is off by default since 7.0, switch it on for this spec only
	private function getDynamicInvocationField() {
		var cl = getPageContext().getClass().getClassLoader();
		var clazz = createObject( "java", "java.lang.Class" ).forName( "lucee.transformer.dynamic.meta.dynamic.ConstructorDynamic", true, cl );
		var field = clazz.getField( "USE_DYN_CLASS_CREATION" );
		field.setAccessible( true );
		return field;
	}

}
