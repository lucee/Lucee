component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}


	public void function testStaticFunctions(){
		// first of all a syntax check
		new static.StaticFunctions();

		// staticUDF1
		var udf=static.StaticFunctions::staticUDF1;
		var meta=getMetaData(udf);
		assertEquals("staticUDF1",meta.name);
		assertEquals("public",meta.access);
		assertEquals("any",meta.returnType);
		var res=static.StaticFunctions::staticUDF1(1);
		assertEquals(1,res.arg1);
		
		// staticUDF21
		var udf=static.StaticFunctions::staticUDF21;
		var meta=getMetaData(udf);
		assertEquals("staticUDF21",meta.name);
		assertEquals("public",meta.access);
		assertEquals("any",meta.returnType);
		var res=static.StaticFunctions::staticUDF1(21);
		assertEquals(21,res.arg1);

		// staticUDF21
		var udf=static.StaticFunctions::staticUDF22;
		var meta=getMetaData(udf);
		assertEquals("staticUDF22",meta.name);
		assertEquals("public",meta.access);
		assertEquals("any",meta.returnType);
		var res=static.StaticFunctions::staticUDF1(22);
		assertEquals(22,res.arg1);

		// staticUDF31
		var udf=static.StaticFunctions::staticUDF31;
		var meta=getMetaData(udf);
		assertEquals("staticUDF31",meta.name);
		assertEquals("public",meta.access);
		assertEquals("struct",meta.returnType);
		var res=static.StaticFunctions::staticUDF1(31);
		assertEquals(31,res.arg1);
		// staticUDF31
		var udf=static.StaticFunctions::staticUDF32;
		var meta=getMetaData(udf);
		assertEquals("staticUDF32",meta.name);
		assertEquals("public",meta.access);
		assertEquals("struct",meta.returnType);
		var res=static.StaticFunctions::staticUDF1(31);
		assertEquals(31,res.arg1);
		// staticUDF31
		var udf=static.StaticFunctions::staticUDF33;
		var meta=getMetaData(udf);
		assertEquals("staticUDF33",meta.name);
		assertEquals("public",meta.access);
		assertEquals("struct",meta.returnType);
		var res=static.StaticFunctions::staticUDF1(31);
		assertEquals(31,res.arg1);
		

		//dump(new static.StaticFunctions());

	}

	public void function testStaticConstructorData(){

	// get static data members from outsite
		assertEquals("static-constr-1",static.StaticConstructor::staticConstr1);
		assertEquals("static-constr-2",static.StaticConstructor::staticConstr2);
		var failed=true;
		try{
			var p=static.StaticConstructor::pstaticConstr1;
			failed=false;
		}
		catch(e){}
		// no longer supported if(!failed)fail("accessing a private static member from outsite should not be possible");
		

	// get static function members from outsite
		var c=static.StaticConstructor::staticConstr3;
		assertTrue(isClosure(c));
		var udf=static.StaticConstructor::staticConstr4;
		assertTrue(isValid("function",udf));
		assertFalse(isClosure(udf));

	// call static function members
		var res=static.StaticConstructor::staticConstr3(123,456);
		// check arguments
		assertEquals(123,res.arguments.abc);
		assertEquals(123,res.arguments[1]);
		
		// check local
		assertEquals(1,res.local.localvar);
		
		// check static
		assertEquals("static-constr-1",res.static.STATICCONSTR1);

		// now we check variables checkvariables set in the object constructor
		new static.StaticConstructor();
		assertEquals("constr-1",static.StaticConstructor::constr1);
		
	}


	public void function testStaticConstructorInInterface(){
		var failed=true;
		try{
			new static.StaticConstructorInInterfaceImpl();
			failed=false;
		}
		catch(e){}
		if(!failed) fail("must fail StaticConstructorInInterfaceImpl");
	}

	public void function testStaticFunctionInInterface(){
		var failed=true;
		try{
			new static.StaticFunctionInInterfaceImpl();
			failed=false;
		}
		catch(e){}
		if(!failed) fail("must fail StaticFunctionInInterfaceImpl");
	}


	public void function testStaticConstructorLifeCycle(){
		// reset the static scope
		file action="touch" file="static/StaticConstructorLifeCycle.cfc";

		assertEquals("0-0",static.StaticConstructorLifeCycle::getCount());
		new static.StaticConstructorLifeCycle();
		assertEquals("1-1",static.StaticConstructorLifeCycle::getCount());
		new static.StaticConstructorLifeCycle();
		assertEquals("2-2",static.StaticConstructorLifeCycle::getCount());
	}

	public void function testFakeStaticVariable() skip=true {
		// LDEV-3465
		// problem with this bug is that static is a scope name, which is writable, but inaccessbile
		var val = new static.FakeStaticVariableExtend().test();
		expect( val ).toBe("foo");
	}

	public void function testExtendedStaticVariable() skip=true {
		// LDEV-3465 (might need some tweaking?)
		var child = new static.StaticScopeExtend();
		// systemOutput (child.getStatic() );
		expect( child.getStatic() ).toBeTypeOf( "struct" );
		// systemOutput( child.getStaticVariable( "test" ) );
		expect( child.getStaticVariable( "test" ) ).toBe( "base" );
	}

	public void function testCreateInstanceInStaticFunction() skip=true {
		// creates an instance inside the component of the same component
		var instance = static.StaticCreateInstance::newInstance();
		debug(instance);
		expect( instance.test()).toBeTrue();
	}

	function run( testResults, testBox ){

		describe( "static scope shared per class across siblings", function(){
			// `_static` is class-level, not per-instance — siblings of the same class
			// share the same static counter via the LDEV-3335 infrastructure.
			it( title="static counter is shared across direct static calls", body=function( currentSpec ){
				static.StaticHolder::reset();
				static.StaticHolder::bump();
				static.StaticHolder::bump();
				expect( static.StaticHolder::read() ).toBe( 2 );
				static.StaticHolder::bump();
				expect( static.StaticHolder::read() ).toBe( 3 );
			});
			it( title="static counter survives instantiation — instance creation doesn't reset it", body=function( currentSpec ){
				static.StaticHolder::reset();
				static.StaticHolder::bump();
				var a = new static.StaticHolder();
				var b = new static.StaticHolder();
				static.StaticHolder::bump();
				expect( static.StaticHolder::read() ).toBe( 2 );
			});
			it( title="static counter survives duplicate — duplicates share class-level static state", body=function( currentSpec ){
				static.StaticHolder::reset();
				var orig = new static.StaticHolder();
				static.StaticHolder::bump();
				var dup = duplicate( orig );
				static.StaticHolder::bump();
				expect( static.StaticHolder::read() ).toBe( 2 );
			});
		});

		describe( "static scope inheritance", function(){
			// `_static` chain head is class-level via LDEV-3335. Static methods on the
			// base class must be reachable via the child's scope ref (the bcp-null bug
			// from the LDEV-3335 implementation work).
			it( title="access static method on base component", body=function( currentSpec ){
				var result = static.BaseComponent::baseStaticMethod();
				expect(result).toBe("base static method");
			});
			it( title="access static method on child component", body=function( currentSpec ){
				var result = static.ChildComponent::childStaticMethod();
				expect(result).toBe("child static method");
			});
			it( title="access base static method through child (bcp null issue)", body=function( currentSpec ){
				var result = static.ChildComponent::baseStaticMethod();
				expect(result).toBe("base static method");
			});
			it( title="access static method via variable reference (benchmark pattern)", body=function( currentSpec ){
				// must instantiate first so static.args is initialized
				var cfc = new static.StaticComponent();
				loop times=10 {
					static.StaticComponent::toSQL();
				}
				expect( serializeJson( static.StaticComponent::toSQL() ) ).toBe( '{"table":true,"name":true}' );
			});
		});

		describe( "static function instance overlay", function(){
			// Per lucee-docs/recipes/static-mocking.md — assigning a function to an
			// instance whose name matches a static function on the class overlays only
			// that instance. The class-level `Comp::fn()` call still returns the original.
			it( title="instance overlay returns mock; class-level call still returns original", body=function( currentSpec ){
				static.StaticHolder::reset();
				static.StaticHolder::bump();
				static.StaticHolder::bump();
				expect( static.StaticHolder::read() ).toBe( 2 );
				var inst = new static.StaticHolder();
				expect( inst.read() ).toBe( 2 );
				inst.read = function() { return -999; };
				expect( inst.read() ).toBe( -999 );
				expect( static.StaticHolder::read() ).toBe( 2 );
			});
			it( title="overlay on instance A is invisible to instance B", body=function( currentSpec ){
				static.StaticHolder::reset();
				static.StaticHolder::bump();
				var a = new static.StaticHolder();
				var b = new static.StaticHolder();
				a.read = function() { return -1; };
				expect( a.read() ).toBe( -1 );
				expect( b.read() ).toBe( 1 );
				expect( static.StaticHolder::read() ).toBe( 1 );
			});
			it( title="overlay invisible to getMetaData (matches runtime-injection contract)", body=function( currentSpec ){
				var inst = new static.StaticHolder();
				inst.read = function() { return -1; };
				var meta = getMetaData( inst );
				var fnNames = [];
				if ( structKeyExists( meta, "functions" ) ) {
					for ( var f in meta.functions ) arrayAppend( fnNames, f.name );
				}
				var fresh = new static.StaticHolder();
				expect( serializeJSON( meta ) ).toBe( serializeJSON( getMetaData( fresh ) ) );
			});
		});

	}
}



