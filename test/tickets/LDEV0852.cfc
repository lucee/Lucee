<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf">
	<cfscript>
		
		public function beforeAll(){
			variables.file="test852.pdf";
			if(not fileExists(file)){
				cfdocument(format="PDF" filename=file){
					defaultDocumentSection();
				}
			}
		}

		public function afterAll(){
			if(fileExists(file)){
				fileDelete(file);
			}
		}

		public function run( testResults , testBox ) {

			describe( "pdfparam attribute with dynamic variable", function() {
				it(title="pdfparam source attribute with static variable name", body=function(){
					try {
						var myVar=file;
						var count = 0;
						cfdocument(format="PDF" name="myVar"){
							DocumentSectionWithDynamicVariable("");
						}
						cfpdf(action="merge" destination="test852.pdf" overwrite="yes"){
							cfpdfparam(source="myVar");
						}
					}
					catch( any e ) {
						assertEquals("", e.message);
					}
				});

				it(title="pdfparam source attribute with dynamic variable", body=function(){
					try{
						var count = 0;
						var i = 1;
						var myVar1=file;
						cfDocument(format="PDF" name="myVar#i#"){
							DocumentSectionWithDynamicVariable(i);
						}
						cfpdf(action="merge" destination="test852.pdf" overwrite="yes"){
							cfpdfparam(source="myVar#i#");
						}
					}
					catch( any e ) {
						assertEquals("", e.message);
					}
				});
			});

			describe( "pdfparam source attribute value  dynamically variable in list and array", function() {

				it(title="pdfparam source attribute with list as dynamic variable", body=function(){
					try{
						var count = 0;
						var x = '1,2,3,4,5';
						for (i = 1; i <= ListLen(x); i++) {
							var variables['myVar#i#']=file;
							cfDocument(format="PDF" name="myVar#i#"){
								DocumentSectionWithDynamicVariable(i);
							}
							cfpdf(action="merge" destination="test852.pdf" overwrite="yes"){
								cfpdfparam(source="myVar#i#");
							}
						}
					}
					catch( any e ) {
						assertEquals("", e.message);
					}
				});

				it(title="pdfparam source attribute with array as dynamic variable", body=function(){
					try{
						var count = 0;
						var x = ["a","b","c","d","e"];
						for (i in x){
							var variables['myVar#i#']=file;
							cfDocument(format="PDF" name="myVar#i#"){
								DocumentSectionWithDynamicVariable(i);
							}
							cfpdf(action="merge" destination="test852.pdf" overwrite="yes"){
								cfpdfparam(source="myVar#i#");
							}
						}
					}
					catch( any e ) {
						assertEquals("", e.message);
					}
				});
			});
		}
	</cfscript>

	<!-- Private Functions --->
	<cffunction name="defaultDocumentSection" access="private" output="false">
		<cfdocumentsection>
			Lucee test document
		</cfdocumentsection>
	</cffunction>

	<cffunction name="DocumentSectionWithDynamicVariable" access="private" output="false">
		<cfdocumentsection>
			Lucee test document
		</cfdocumentsection>
	</cffunction>
</cfcomponent>