component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title = "Test suite for getMetaData", body = function() {

			describe( title = "Array metadata", body = function() {

				it( title = 'Checking regular array', body = function( currentSpec ) {
					var data=[1,2,3];
					var meta=data.getmetadata();

					assertEquals("class,datatype,dimensions,name,type", listSort( meta.keyList(), "text") );
					assertEquals("any",meta.datatype);
					assertEquals(1,meta.dimensions);
					assertEquals("unsynchronized",meta.type);
				});

				it( title = 'Checking 2 dim array', body = function( currentSpec ) {
					var data=arrayNew(2);
					var meta=data.getmetadata();

					assertEquals("class,datatype,dimensions,name,type", listSort( meta.keyList(), "text") );
					assertEquals("any",meta.datatype);
					assertEquals(2,meta.dimensions);
					assertEquals("unsynchronized",meta.type);
				});

				it( title = 'Checking typed array', body = function( currentSpec ) {
					var data=arrayNew(1,"string");
					var meta=data.getmetadata();

					assertEquals("class,datatype,dimensions,name,type", listSort( meta.keyList(), "text" ) );
					assertEquals("string",meta.datatype);
					assertEquals(1,meta.dimensions);
					assertEquals("unsynchronized",meta.type);
				});

			});

			describe( title = "Struct metadata", body = function() {

				it( title = 'Checking regular struct', body = function( currentSpec ) {
					var data={a:1};
					var meta=data.getmetadata();

					assertEquals("class,name,ordered,type", listSort( meta.keyList(), "text" ) );
					assertEquals("unordered",meta.ordered);
					//assertEquals("regular",meta.type);
				});

				it( title = 'Checking ordered struct', body = function( currentSpec ) {
					var data=[a:1];
					var meta=data.getmetadata();

					assertEquals("class,name,ordered,type", listSort( meta.keyList(), "text" ) );
					assertEquals("ordered",meta.ordered);
					assertEquals("ordered",meta.type);
				});

				it( title = 'Checking soft struct', body = function( currentSpec ) {
					var data=structNew("soft");
					var meta=data.getmetadata();

					assertEquals("class,name,ordered,type", listSort( meta.keyList(), "text" ) );
					assertEquals("unordered",meta.ordered);
					assertEquals("soft",meta.type);
				});

			});

			describe( title = "UDF metadata", body = function() {

				it( title = "Checking UDFs", body = () => {
					var metaA = getMetadata( exampleFunctionWithDescriptionInAnnotation );
					expect( metaA ).toBeStruct();
					expect( metaA ).toHaveKey( "description" );
					expect( metaA.description ).toBe( "Description shows up." );

					var metaB = getMetadata( exampleFunctionWithDescriptionInDocblock );
					expect( metaB ).toBeStruct();
					expect( metaB ).toHaveKey( "description" );
					expect( metaB.description ).toBe( "Description does not show up." );
				}, labels = [ "metadata" ], skip = true );

			});

			describe( title = "Regular UDF metadata", body = function() {

				it( title = "Checking regular UDF owner field", body = () => {
					var obj = new getMetaData.GetMetaDataComponent();

					// Test regular getter
					var getterMeta = getMetadata( obj.getMessage );
					expect( getterMeta ).toBeStruct();
					expect( getterMeta ).toHaveKey( "owner" );
					expect( getterMeta.owner ).toInclude( "GetMetaDataComponent.cfc" );

					// Test regular setter
					var setterMeta = getMetadata( obj.setMessage );
					expect( setterMeta ).toBeStruct();
					expect( setterMeta ).toHaveKey( "owner" );
					expect( setterMeta.owner ).toInclude( "GetMetaDataComponent.cfc" );
				}, labels = [ "metadata" ] );

				it( title = "Checking regular UDF owner field with mixin", body = () => {
					// Use two DIFFERENT components to prove owner tracking works correctly
					var comp1 = new getMetaData.GetMetaDataComponent();
					var comp2 = new getMetaData.GetMetaDataComponent2();

					// Inject comp1's getter into comp2 (mixin pattern)
					comp2.injectedFromComp1 = comp1.getMessage;

					// Owner should point to Component (comp1), NOT Component2 (comp2)
					var meta = getMetadata( comp2.injectedFromComp1 );
					expect( meta ).toBeStruct();
					expect( meta ).toHaveKey( "owner" );
					expect( meta.owner ).toInclude( "GetMetaDataComponent.cfc" );
					expect( meta.owner ).notToInclude( "GetMetaDataComponent2.cfc" );
				}, labels = [ "metadata", "mixin" ] );

				it( title = "Checking regular UDF owner field with inheritance", body = () => {
					var obj = new getMetaData.GetMetaDataComponentChild();

					// Test regular getter from parent
					var getterMeta = getMetadata( obj.getMessage );
					expect( getterMeta ).toBeStruct();
					expect( getterMeta ).toHaveKey( "owner" );
					expect( getterMeta.owner ).toInclude( "GetMetaDataComponent.cfc" );
				}, labels = [ "metadata", "inheritance" ] );

			});

			describe( title = "Accessor UDF metadata", body = function() {

				it( title = "Checking accessor UDF owner field", body = () => {
					var obj = new getMetaData.GetMetaDataAccessorComponent();

					// Test auto-generated getter
					var getterMeta = getMetadata( obj.getMessage );
					expect( getterMeta ).toBeStruct();
					expect( getterMeta ).toHaveKey( "owner" );
					expect( getterMeta.owner ).toInclude( "GetMetaDataAccessorComponent.cfc" );

					// Test auto-generated setter
					var setterMeta = getMetadata( obj.setMessage );
					expect( setterMeta ).toBeStruct();
					expect( setterMeta ).toHaveKey( "owner" );
					expect( setterMeta.owner ).toInclude( "GetMetaDataAccessorComponent.cfc" );
				}, labels = [ "metadata", "accessor" ] );

				it( title = "Checking accessor UDF owner field with mixin", body = () => {
					// Use two DIFFERENT components to prove owner tracking works correctly
					var comp1 = new getMetaData.GetMetaDataAccessorComponent();
					var comp2 = new getMetaData.GetMetaDataAccessorComponent2();

					// Inject comp1's getter into comp2 (mixin pattern)
					comp2.injectedFromComp1 = comp1.getMessage;

					// Owner should point to Component (comp1), NOT Component2 (comp2)
					var meta = getMetadata( comp2.injectedFromComp1 );
					expect( meta ).toBeStruct();
					expect( meta ).toHaveKey( "owner" );
					expect( meta.owner ).toInclude( "GetMetaDataAccessorComponent.cfc" );
					expect( meta.owner ).notToInclude( "GetMetaDataAccessorComponent2.cfc" );
				}, labels = [ "metadata", "accessor", "mixin" ] );

				it( title = "Checking accessor UDF owner field with inheritance", body = () => {
					var obj = new getMetaData.GetMetaDataAccessorComponentChild();

					// Test auto-generated getter from parent
					var getterMeta = getMetadata( obj.getMessage );
					expect( getterMeta ).toBeStruct();
					expect( getterMeta ).toHaveKey( "owner" );
					expect( getterMeta.owner ).toInclude( "GetMetaDataAccessorComponent.cfc" );
				}, labels = [ "metadata", "accessor", "inheritance" ] );

			});

			describe( title = "Component metadata", body = function() {

				it( title = "name is the fully-qualified dotted path from the web root", body = () => {
					var c = new getMetaData.GetMetaDataChild();
					expect( getMetaData( c ).name ).toInclude( "functions.getMetaData.GetMetaDataChild" );
				}, labels = [ "metadata" ] );

				it( title = "implements is reported on the CFC that declared it, not on descendants", body = () => {
					// GetMetaDataChild extends GetMetaDataBase implements GetMetaDataIface.
					// The interface metadata lives on Base (which declared `implements`),
					// not on Child. Child's own `implements` struct is empty.
					var c = new getMetaData.GetMetaDataChild();
					var meta = getMetaData( c );
					expect( meta ).toHaveKey( "implements" );
					expect( meta.implements ).toBeEmpty();
					expect( meta.extends.implements ).toHaveKey( "GetMetaDataIface" );
					expect( meta.extends.implements.GetMetaDataIface.name ).toInclude( "GetMetaDataIface" );
				}, labels = [ "metadata", "inheritance", "implements" ] );

				it( title = "runtime-injected closures don't appear in getMetaData.functions", body = () => {
					// Assigning a closure at runtime makes it callable but invisible to
					// metadata — getMetaData reflects declared structure, not the
					// component's current member table.
					var c = new getMetaData.GetMetaDataChild();
					var declaredCount = arrayLen( getMetaData( c ).functions );

					c.injectedFn = function() { return "injected"; };
					expect( c.injectedFn() ).toBe( "injected" );

					expect( arrayLen( getMetaData( c ).functions ) ).toBe( declaredCount );
				}, labels = [ "metadata", "mixin" ] );

				it( title = "duplicate has identical metadata shape to the original", body = () => {
					var c = new getMetaData.GetMetaDataChild();
					var d = duplicate( c );
					expect( getMetaData( d ).name ).toBe( getMetaData( c ).name );
					expect( arrayLen( getMetaData( d ).functions ) ).toBe( arrayLen( getMetaData( c ).functions ) );
				}, labels = [ "metadata" ] );

				it( title = "duplicate metadata is unaffected by runtime mixins on either side", body = () => {
					var c = new getMetaData.GetMetaDataChild();
					c.injectedFn = function() { return "injected"; };
					var d = duplicate( c );
					// metadata reports declared structure for both, despite the mixin
					expect( arrayLen( getMetaData( d ).functions ) ).toBe( arrayLen( getMetaData( c ).functions ) );
				}, labels = [ "metadata", "mixin" ] );

				it( title = "property order is preserved (declaration order, not alphabetical)", body = function() {
					// Critical for ORM, serialization, and backward compatibility — getMetaData
					// must surface properties in the order they were declared, not sorted.
					var cfc = new component accessors="true" {
						property name="zulu" type="string" default="last";
						property name="alpha" type="string" default="first";
						property name="mike" type="string" default="middle";
					};
					var props = getMetaData( cfc ).properties;
					expect( arrayLen( props ) ).toBe( 3 );
					expect( props[ 1 ].name ).toBe( "zulu" );
					expect( props[ 2 ].name ).toBe( "alpha" );
					expect( props[ 3 ].name ).toBe( "mike" );
				}, labels = [ "metadata", "properties" ] );

			});

		});

	}

	function exampleFunctionWithDescriptionInAnnotation() description="Description shows up." {}

	/**
	 * @description Description does not show up.
	 */
	function exampleFunctionWithDescriptionInDocblock() {}

}