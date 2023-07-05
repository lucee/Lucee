component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults , testBox ) {

		describe( title='QofQ' , body=function(){

			var arrG4AppLicenses = [
				{
					"g4app_id": 4,
					"g4app_code": "g4emp",
					"is_hr": 0,
					"is_emp": 1,
					"license_min_summed": 0,
					"license_max_summed": 400
				}
			];
			
			variables.qryG4AppLicenses = QueryNew(
				"g4app_id,g4app_code,is_hr,is_emp,license_min_summed,license_max_summed",
				"integer,varchar,bit,bit,double,double",
				arrG4AppLicenses
			);
		
			// Setup query #2
			var arrUsageCount = [
				{
					"g4app_id": 3,
					"g4app_code": "g4hr",
					"g4app_name": "HR Management System",
					"usage_count": 9
				}
			];
		
			variables.qryUsageCount = QueryNew(
				"g4app_id,g4app_code,g4app_name,usage_count",
				"integer,varchar,varchar,double", 
				arrUsageCount
			);

			it( title='LDEV-4613 incompatible data type in operation simple join, all upper case sql' , body=function() {
				var q = QueryExecute(
					sql="
						SELECT QRYG4APPLICENSES.*
						FROM QRYUSAGECOUNT, QRYG4APPLICENSES
						WHERE QRYUSAGECOUNT.G4APP_ID = QRYG4APPLICENSES.G4APP_ID
					",
					options={
						dbtype='query'
					}
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 0 );
			});

			it( title='LDEV-4613 incompatible data type in operation simple join, mixed case sql' , body=function() {
				
				var q = QueryExecute(
					sql="
						select qryG4AppLicenses.*
						from qryUsageCount, qryG4AppLicenses
						where qryUsageCount.g4app_id = qryG4AppLicenses.g4app_id
					",
					options={
						dbtype='query'
					}
				);

				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 0 );
			});

			it( title='LDEV-4613 incompatible data type in operation simple join, mixed case sql' , body=function() {
				
				var q = QueryExecute(
					sql="
						select qryG4AppLicenses.g4app_id
						from qryUsageCount, qryG4AppLicenses
						where qryUsageCount.g4app_id = qryG4AppLicenses.g4app_id
					",
					options={
						dbtype='query'
					}
				);

				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 0 );
			});

			it( title='LDEV-4613 incompatible data type in operation simple join, mixed case sql' , body=function() {
				
				var q = QueryExecute(
					sql="
						select qryG4AppLicenses.G4APP_ID
						from qryUsageCount, qryG4AppLicenses
						where qryUsageCount.g4app_id = qryG4AppLicenses.g4app_id
					",
					options={
						dbtype='query'
					}
				);

				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 0 );
			});

			it( title='LDEV-4613 incompatible data type in operation simple join, orig example' , body=function() {
				var arrG4AppLicenses = [
					{
						"g4app_id": 4,
						"g4app_code": "g4emp",
						"is_hr": 0,
						"is_emp": 1,
						"license_min_summed": 0,
						"license_max_summed": 400
					},
					{
						"g4app_id": 6,
						"g4app_code": "g4research",
						"is_hr": 1,
						"is_emp": 0,
						"license_min_summed": 0,
						"license_max_summed": 400
					}
				];
				
				var qryG4AppLicenses = QueryNew(
						"g4app_id,g4app_code,is_hr,is_emp,license_min_summed,license_max_summed",
						"integer,varchar,bit,bit,double,double",
						arrG4AppLicenses
					);
			
				// Setup query #2
				var arrUsageCount = [
					{
						"g4app_id": 3,
						"g4app_code": "g4hr",
						"g4app_name": "HR Management System",
						"usage_count": 9
					},
					{
						"g4app_id": 4,
						"g4app_code": "g4emp",
						"g4app_name": "Gen4 Employees Center",
						"usage_count": 8
					},
					{
						"g4app_id": 14,
						"g4app_code": "g4benadminportal",
						"g4app_name": "Ben Admin Portal",
						"usage_count": 11
					},
					{
						"g4app_id": 13,
						"g4app_code": "g4communicationportal",
						"g4app_name": "Communication Portal",
						"usage_count": 4
					},
					{
						"g4app_id": 5,
						"g4app_code": "g4benefits",
						"g4app_name": "Gen4Benefits Central",
						"usage_count": 6
					},
					{
						"g4app_id": 3,
						"g4app_code": "g4benadminportal",
						"g4app_name": "Ben Admin Portal",
						"usage_count": 11
					},
					{
						"g4app_id": 7,
						"g4app_code": "g4communicationportal",
						"g4app_name": "Communication Portal",
						"usage_count": 3
					},
					{
						"g4app_id": 7,
						"g4app_code": "g4hrlite",
						"g4app_name": "HR Communication Center",
						"usage_count": 4
					}
				];

				var qryUsageCount = QueryNew(
					"g4app_id,g4app_code,g4app_name,usage_count",
					"integer,varchar,varchar,double", 
					arrUsageCount
				);
				var q = QueryExecute(
					sql="
						select qryG4AppLicenses.*
						from qryUsageCount, qryG4AppLicenses
						where qryUsageCount.g4app_id = qryG4AppLicenses.g4app_id
					",
					options={
						dbtype='query'
					}
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 1 );
			});

			
		});

	}

} 