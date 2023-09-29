component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-272", function() {
			beforeEach(function(){
				empDetails = queryNew("name,age,dept","varchar,integer,varchar", [['saravana',35,'MD'],['Bob',20, 'Employee'],['pothy',25, 'Employee']]);
			});

			it("Checking QOQ with same alias for a column with some condition using it in where clause", function( currentSpec ){
				errorMsg = "";
				try {
					query name="getMaxEmpAge" result="result" dbtype="query"{
						echo('select MAX(age) AS age from empDetails WHERE age > 22');
					}
				} catch( any e ) {
					errorMsg = e.Detail;
				}
				expect(errorMsg).toBe("");
				expect(getMaxEmpAge).toBeTypeOf("Query");
				expect(getMaxEmpAge.Age).toBe(35);
			});

			it("Checking QOQ with different alias for a column with some condition using it in where clause", function( currentSpec ){
				errorMsg = "";
				try {
					query name="getMaxEmpAge" result="result" dbtype="query"{
						echo('select MAX(age) AS MaxAge from empDetails WHERE age > 22');
					}
				} catch( any e ) {
					errorMsg = e.Detail;
				}
				expect(errorMsg).toBe("");
				expect(getMaxEmpAge).toBeTypeOf("Query");
				expect(getMaxEmpAge.MaxAge).toBe(35);
			});

			it("Checking QOQ with different alias for a column with some condition using it in where clause & group by clause", function( currentSpec ){
				errorMsg = "";
				try {
					query name="getDeptEmpCount" result="result" dbtype="query"{
						echo("select COUNT(dept) AS deptCount from empDetails WHERE dept = 'Employee' GROUP BY dept");
					}
				} catch( any e ) {
					errorMsg = e.Detail;
				}
				expect(errorMsg).toBe("");
				expect(getDeptEmpCount).toBeTypeOf("Query");
				expect(getDeptEmpCount.deptCount).toBe(2);
			});
		});
	}
}