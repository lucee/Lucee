component extends="org.lucee.cfml.test.LuceeTestCase" labels="extensions" {

	function beforeAll() {
		variables.dir = getTempDirectory() & "LDEV6720-" & createUUID() & "/";
		directoryCreate( dir );
		variables.id = createUUID();
		variables.tagName = "ldev6720tag" & lCase( left( replace( id, "-", "", "all" ), 8 ) );
	}

	function afterAll() {
		if ( isInstalled( id ) ) {
			admin action="removeRHExtension" type="server" password=request.SERVERADMINPASSWORD id=id;
		}
		if ( directoryExists( dir ) ) directoryDelete( dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6720 installing an extension with tags, lite then full", function() {

			it( "installs the lite version and then the full version", function() {
				// back to back, as when both are deployed on startup, so the tags are not loaded in between.
				// lite and full share id and version, which of the two is found first depends on the file hash,
				// so it is done a few times with different files
				loop from=1 to=4 index="local.i" {
					admin action="updateRHExtension" type="server" password=request.SERVERADMINPASSWORD source=createExtension( "lite", i );
					admin action="updateRHExtension" type="server" password=request.SERVERADMINPASSWORD source=createExtension( "full", i );
				}
				expect( isInstalled( id ) ).toBeTrue();
				expect( structKeyExists( getTagList().cf, tagName ) ).toBeTrue();
			});

			it( "the tag is linked to the extension (LDEV-3259)", function() {
				var tagData = getTagData( "cf", tagName );
				expect( structKeyExists( tagData, "extension" ) ).toBeTrue();
				expect( tagData.extension ).toBe( id );
				expect( structKeyExists( getTagList( id ).cf, tagName ) ).toBeTrue();
			});
		});
	}

	private string function createExtension( required string flavour, required numeric round ) {
		var src = dir & "src-" & arguments.flavour & "-" & arguments.round & "/";
		directoryCreate( src & "META-INF", true );
		directoryCreate( src & "tlds", true );
		fileWrite( src & "META-INF/MANIFEST.MF", 'Manifest-Version: 1.0#chr(10)#id: "#id#"#chr(10)#version: "1.0.0.1"#chr(10)#groupId: "org.lucee.test"#chr(10)#artifactId: "ldev6720-#lCase( id )#"#chr(10)#name: "ldev6720"#chr(10)#description: "test extension with tags (#arguments.flavour# #arguments.round#)"#chr(10)#' );
		// written like this, so it is not read as a cf tag
		var cfmlVersion = "cfml-version";
		fileWrite( src & "tlds/ldev6720.tldx", '<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE taglib PUBLIC "-//Lucee//DTD CFML Tag Library 1.0//EN" "dtd/web-cfmtaglibrary_1_0.dtd">
<taglib>
	<tlib-version>1.0</tlib-version>
	<#cfmlVersion#>1.0</#cfmlVersion#>
	<short-name>ldev6720</short-name>
	<uri>http://lucee.org/tag/ldev6720</uri>
	<display-name>LDEV-6720</display-name>
	<description/>
	<name-space>cf</name-space>
	<name-space-separator></name-space-separator>
	<el>
		<el-class>lucee.transformer.cfml.expression.CFMLExprTransformer</el-class>
	</el>
	<tag>
		<name>#tagName#</name>
		<tag-class>lucee.runtime.tag.Abort</tag-class>
		<body-content>empty</body-content>
		<body-rtexprvalue>false</body-rtexprvalue>
		<script>
			<type>single</type>
			<rtexprvalue>true</rtexprvalue>
		</script>
		<description>test tag</description>
		<attribute-type>fixed</attribute-type>
	</tag>
</taglib>' );
		var lex = dir & "ldev6720-1.0.0.1." & arguments.flavour & "-" & arguments.round & ".lex";
		zip action="zip" source=src file=lex;
		return lex;
	}

	private boolean function isInstalled( required string id ) {
		var target = arguments.id;
		admin action="getRHExtensions" type="server" password=request.SERVERADMINPASSWORD returnVariable="local.exts";
		return queryFilter( local.exts, function( row ) { return row.id == target; } ).recordCount > 0;
	}
}
