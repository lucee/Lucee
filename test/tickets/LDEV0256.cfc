component extends="org.lucee.cfml.test.LuceeTestCase" {
//LDEV0256.cfc
	function beforeAll() {
		variables.salt = 'A41n9t0Q';
		variables.passphrase = 'passphrase';
		variables.iterations = 100000;
		variables.keysize = 2048;
	}

	function run( testResults , testBox ) {

		describe( 'LDEV-256' , function() {

			describe( 'GeneratePBKDFKey returns expected value' , function() {
				it( 'for PBKDF2WithHmacSHA1' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA1' , passphrase , salt, iterations , keysize )
					).toBe(
						'fRtApvaLO05oqIFHi86g1oyk9As2CfykHPW62wI3T4zl8aLbuTXHL2Z9cNQt/afv1uSsOcqRF/zjlP5Nfo4I4RcFOi+xK8Odixj5S5V7F6p3wN997NZNi9awWT/pBaPEnM2s4MaZclZMghKPGH2LMSF5ysf0VOIlDk30z+mJz3k62wlquISWgiPt7k73lZOsZCpLxQ0SJqFaIHiUm5xskpGt8nR1d8eHDOu7RhwjdjV40aPSowWxRWkIYtiv7gxCV2z1LZIz8B3JCVwfhElC0jS2z5jTpsGNWMGYifEOCCIRUNPPNxFkqehcBJc/ojQhCIKBlkDtFWp9OpslWdfnQw=='
					);
				});

				it( 'for PBKDF2WithHmacSHA1 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA1' , passphrase , salt)).toBe('T79SiDbjZA7PE40YtZsdhA==');
				});

				it( 'for PBKDF2WithHmacSHA224' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA224' , passphrase , salt, iterations , keysize )
					).toBe(
						'7+S9VMy0TyX4/2yTYOl7rlln1+GM9nxv3sTxpnE/g7VEOOMIkyuQu3GSN+UAocj1wON7iwPZq4GudRJg9VFswySCJ3SRp1DuTE4sITeoguFvw1DCnzFFAiWB7WRK/lwhbKOS8bdQ+/oZN6KIvZge54j5g4jlUFCJnRkUP8UHb9MdMKf/k4Q00IIS3tIe4SbBCNH+tNevFj9Y6ENMsUSoRS6vrzjh3FTkL2ldBRjixY59qkDxpeYWTIL2jWDsS5GvnRslP1zOFDbr+uCYbQJrWNR1hWFk8VjImsct/hrggw3wZ7qJXIgupIVGdra1tTIOtqsfani5t66r/oUPgkm0KQ=='
					);
				});


				it( 'for PBKDF2WithHmacSHA224 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA224' , passphrase , salt)).toBe('wlWb4EdrdkIffsCt2uaHIQ==');
				});


				it( 'for PBKDF2WithHmacSHA256' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA256' , passphrase , salt, iterations , keysize )
					).toBe(
						'pn/As0rErw6lrprnur+b1TaTszMXkoetNCceI01x+dRZDOBQHN0BYIUIlxmAUcrVGT8YvJE/AvlRuNVVeaXcm2D9ldOdXZ6dEIGdaY4ovQ88aiI3mZSHK6cPvxGRQ5KZA/YymWvBa/NZvHb0JWaZHZsygbXMCAIi3ZYECBonoRd4+s+R0isqjCjeTWZoFqIum78XkTz3zMSLcNMhwr9rVzUrj+WAxLYsx9PU5IC1X5gJ5cRU7e6B3zo6MMJ1qs777hqzCKAyrG6nw93YTinpRWfkPmWJ2aGmFWBj+y5N5d4kI8nv0a+U4G2m6CVtaI1VIaObliSD+CG6yuP4ylOKKQ=='
					);
				});


				it( 'for PBKDF2WithHmacSHA256 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA256' , passphrase , salt)).toBe('l+xVDQz+AbkHVR+1q8yyKA==');
				});



				it( 'for PBKDF2WithHmacSHA384' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA384' , passphrase , salt, iterations , keysize )
					).toBe(
						'cCCChfIzBIdh8h6FSv/ULHXCgClCVwY1ScyZvRl4vFKaYQCNp2JIN9nMQbsKy/iO6QYbuVSgiouz8GVW3AcP+Tmp7OFOZerIf2PP9zGCgd2b3/GkdoUegcgxGREkkMHpuDSRwGwBrusb8bd1Uvt9+cMx8WMLGML8Q7LzDqZyj2Bt3dZHydncAEelWxMXfs+SrOyTdpiVXqHNOWQO2lYROs3RMUjYJmP6IrD4VLsLdOhHadpOqTWExa2uGOiZjlP5Rem3a5mM9tTH7lnxbh7XYRyeDyCQmpk0NxETVrOznclvARYlXTF3WBt/Qg2tw1HfjpoqOL8b5W1yilK8maQdhg=='
					);
				});


				it( 'for PBKDF2WithHmacSHA384 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA384' , passphrase , salt)).toBe('7vZLcvs2laScVQgtpDkuGA==');
				});



				it( 'for PBKDF2WithHmacSHA512' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithHmacSHA512' , passphrase , salt, iterations , keysize )
					).toBe(
						'ufg8j2Yzw7rKp/iyXYzFoNe7/BNdwa8OYvlX3oQw0HfTlj9Wm7FIxUnabHXqRpjxRIwmS42Aecn1wrWgdwZXzRLIQB0NtiUNPaTKsBu4jo2pMMxC+MVJiJAW3CEjJyQSi6ZLSSzLh0KPlFtcyrdfu/ZLcUmLVVmBLrIDuVKJwCiXOAuhOaz7NCasBRZGiQTdQreNFDUD00wPt/t2NkDwpUDfVRn7qZ10MS8gOYeo3D4kaE1DxAJpYejxwDMrvxoZdqImTSiE2nzQ+zDDgYMywkhyNYhHjyLYbI1D6t7Rcgr3vc6f7orwgho+M1Ko52caVoTN3c64ioNyI3qSqc5TjQ=='
					);
				});


				it( 'for PBKDF2WithHmacSHA512 without optional optional arguments' , function() {
					expect(GeneratePBKDFKey( 'PBKDF2WithHmacSHA512' , passphrase , salt)).toBe('FOHqEZmtiJU70L07REmppA==');
				});

				/*it( 'for PBKDF2WithSHA1' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA1' , passphrase , salt, iterations , keysize )
					).toBe(
						'fRtApvaLO05oqIFHi86g1oyk9As2CfykHPW62wI3T4zl8aLbuTXHL2Z9cNQt/afv1uSsOcqRF/zjlP5Nfo4I4RcFOi+xK8Odixj5S5V7F6p3wN997NZNi9awWT/pBaPEnM2s4MaZclZMghKPGH2LMSF5ysf0VOIlDk30z+mJz3k62wlquISWgiPt7k73lZOsZCpLxQ0SJqFaIHiUm5xskpGt8nR1d8eHDOu7RhwjdjV40aPSowWxRWkIYtiv7gxCV2z1LZIz8B3JCVwfhElC0jS2z5jTpsGNWMGYifEOCCIRUNPPNxFkqehcBJc/ojQhCIKBlkDtFWp9OpslWdfnQw=='
					);
				}); 

				it( 'for PBKDF2WithSHA224' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA224' , passphrase , salt, iterations , keysize )
					).toBe(
						'7+S9VMy0TyX4/2yTYOl7rlln1+GM9nxv3sTxpnE/g7VEOOMIkyuQu3GSN+UAocj1wON7iwPZq4GudRJg9VFswySCJ3SRp1DuTE4sITeoguFvw1DCnzFFAiWB7WRK/lwhbKOS8bdQ+/oZN6KIvZge54j5g4jlUFCJnRkUP8UHb9MdMKf/k4Q00IIS3tIe4SbBCNH+tNevFj9Y6ENMsUSoRS6vrzjh3FTkL2ldBRjixY59qkDxpeYWTIL2jWDsS5GvnRslP1zOFDbr+uCYbQJrWNR1hWFk8VjImsct/hrggw3wZ7qJXIgupIVGdra1tTIOtqsfani5t66r/oUPgkm0KQ=='
					);
				}); 

				it( 'for PBKDF2WithSHA256' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA256' , passphrase , salt, iterations , keysize )
					).toBe(
						'pn/As0rErw6lrprnur+b1TaTszMXkoetNCceI01x+dRZDOBQHN0BYIUIlxmAUcrVGT8YvJE/AvlRuNVVeaXcm2D9ldOdXZ6dEIGdaY4ovQ88aiI3mZSHK6cPvxGRQ5KZA/YymWvBa/NZvHb0JWaZHZsygbXMCAIi3ZYECBonoRd4+s+R0isqjCjeTWZoFqIum78XkTz3zMSLcNMhwr9rVzUrj+WAxLYsx9PU5IC1X5gJ5cRU7e6B3zo6MMJ1qs777hqzCKAyrG6nw93YTinpRWfkPmWJ2aGmFWBj+y5N5d4kI8nv0a+U4G2m6CVtaI1VIaObliSD+CG6yuP4ylOKKQ=='
					);
				}); 

				it( 'for PBKDF2WithSHA384' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA384' , passphrase , salt, iterations , keysize )
					).toBe(
						'cCCChfIzBIdh8h6FSv/ULHXCgClCVwY1ScyZvRl4vFKaYQCNp2JIN9nMQbsKy/iO6QYbuVSgiouz8GVW3AcP+Tmp7OFOZerIf2PP9zGCgd2b3/GkdoUegcgxGREkkMHpuDSRwGwBrusb8bd1Uvt9+cMx8WMLGML8Q7LzDqZyj2Bt3dZHydncAEelWxMXfs+SrOyTdpiVXqHNOWQO2lYROs3RMUjYJmP6IrD4VLsLdOhHadpOqTWExa2uGOiZjlP5Rem3a5mM9tTH7lnxbh7XYRyeDyCQmpk0NxETVrOznclvARYlXTF3WBt/Qg2tw1HfjpoqOL8b5W1yilK8maQdhg=='
					);
				}); 

				it( 'for PBKDF2WithSHA512' , function() {
					expect(
						GeneratePBKDFKey( 'PBKDF2WithSHA512' , passphrase , salt, iterations , keysize )
					).toBe(
						'ufg8j2Yzw7rKp/iyXYzFoNe7/BNdwa8OYvlX3oQw0HfTlj9Wm7FIxUnabHXqRpjxRIwmS42Aecn1wrWgdwZXzRLIQB0NtiUNPaTKsBu4jo2pMMxC+MVJiJAW3CEjJyQSi6ZLSSzLh0KPlFtcyrdfu/ZLcUmLVVmBLrIDuVKJwCiXOAuhOaz7NCasBRZGiQTdQreNFDUD00wPt/t2NkDwpUDfVRn7qZ10MS8gOYeo3D4kaE1DxAJpYejxwDMrvxoZdqImTSiE2nzQ+zDDgYMywkhyNYhHjyLYbI1D6t7Rcgr3vc6f7orwgho+M1Ko52caVoTN3c64ioNyI3qSqc5TjQ=='
					);
				}); */

			});

		});

	}
}
