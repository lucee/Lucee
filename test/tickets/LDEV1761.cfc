component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function beforeAll(){
		variables.ts=getTimeZone();
	}
	
	public function afterAll(){
		setTimezone(variables.ts);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1761", body=function() {
			it(title = "Checking evaluate() with datetime", body = function( currentSpec ) {
				var timezoneList = "AET, Africa/Bangui, Africa/Brazzaville, Africa/Casablanca, Africa/Douala, Africa/El_Aaiun, Africa/Johannesburg, Africa/Kinshasa, Africa/Lagos, Africa/Libreville, Africa/Luanda, Africa/Malabo, Africa/Maseru, Africa/Mbabane, Africa/Ndjamena, Africa/Niamey, Africa/Porto-Novo, Africa/Windhoek, America/Adak, America/Anchorage, America/Araguaina, America/Asuncion, America/Atka, America/Bahia, America/Belem, America/Boa_Vista, America/Bogota, America/Campo_Grande, America/Caracas, America/Cayenne, America/Cuiaba, America/Fortaleza, America/Glace_Bay, America/Godthab, America/Goose_Bay, America/Guyana, America/Halifax, America/Juneau, America/La_Paz, America/Lima, America/Maceio, America/Manaus, America/Metlakatla, America/Miquelon, America/Moncton, America/Montevideo, America/Nome, America/Noronha, America/Paramaribo, America/Porto_Velho, America/Recife, America/Santarem, America/Santiago, America/Sao_Paulo, America/Scoresbysund, America/Sitka, America/St_Johns, America/Thule, America/Yakutat, Antarctica/Casey, Antarctica/Davis, Antarctica/DumontDUrville, Antarctica/Macquarie, Antarctica/Mawson, Antarctica/McMurdo, Antarctica/Palmer, Antarctica/Rothera, Antarctica/South_Pole, Antarctica/Syowa, Antarctica/Vostok, Asia/Almaty, Asia/Amman, Asia/Anadyr, Asia/Aqtau, Asia/Aqtobe, Asia/Ashgabat, Asia/Ashkhabad, Asia/Baku, Asia/Bangkok, Asia/Beirut, Asia/Bishkek, Asia/Brunei, Asia/Chita, Asia/Choibalsan, Asia/Dacca, Asia/Damascus, Asia/Dhaka, Asia/Dili, Asia/Dubai, Asia/Dushanbe, Asia/Gaza, Asia/Hebron, Asia/Ho_Chi_Minh, Asia/Hong_Kong, Asia/Hovd, Asia/Irkutsk, Asia/Jakarta, Asia/Jayapura, Asia/Jerusalem, Asia/Kabul, Asia/Kamchatka, Asia/Karachi, Asia/Kashgar, Asia/Kathmandu, Asia/Katmandu, Asia/Khandyga, Asia/Krasnoyarsk, Asia/Kuala_Lumpur, Asia/Kuching, Asia/Magadan, Asia/Makassar, Asia/Manila, Asia/Muscat, Asia/Nicosia, Asia/Novokuznetsk, Asia/Novosibirsk, Asia/Omsk, Asia/Oral, Asia/Phnom_Penh, Asia/Pontianak, Asia/Pyongyang, Asia/Qyzylorda, Asia/Rangoon, Asia/Saigon, Asia/Sakhalin, Asia/Samarkand, Asia/Seoul, Asia/Singapore, Asia/Srednekolymsk, Asia/Tashkent, Asia/Tbilisi, Asia/Tehran, Asia/Tel_Aviv, Asia/Thimbu, Asia/Thimphu, Asia/Ujung_Pandang, Asia/Ulaanbaatar, Asia/Ulan_Bator, Asia/Urumqi, Asia/Ust-Nera, Asia/Vientiane, Asia/Vladivostok, Asia/Yakutsk, Asia/Yangon, Asia/Yekaterinburg, Asia/Yerevan, AST, Atlantic/Azores, Atlantic/Bermuda, Atlantic/Canary, Atlantic/Cape_Verde, Atlantic/Faeroe, Atlantic/Faroe, Atlantic/Madeira, Atlantic/South_Georgia, Atlantic/Stanley, Australia/ACT, Australia/Canberra, Australia/Currie, Australia/Eucla, Australia/Hobart, Australia/LHI, Australia/Lord_Howe, Australia/Melbourne, Australia/NSW, Australia/Perth, Australia/Sydney, Australia/Tasmania, Australia/Victoria, Australia/West, BET, Brazil/DeNoronha, Brazil/East, Brazil/West, BST, Canada/Atlantic, Canada/Newfoundland, Chile/Continental, Chile/EasterIsland, CNT, EET, Etc/GMT-13, Etc/GMT-14, Europe/Athens, Europe/Bucharest, Europe/Chisinau, Europe/Helsinki, Europe/Kiev, Europe/Lisbon, Europe/Mariehamn, Europe/Minsk, Europe/Moscow, Europe/Nicosia, Europe/Riga, Europe/Samara, Europe/Simferopol, Europe/Sofia, Europe/Tallinn, Europe/Tiraspol, Europe/Uzhgorod, Europe/Vilnius, Europe/Volgograd, Europe/Zaporozhye, Hongkong, Indian/Chagos, Indian/Christmas, Indian/Cocos, Indian/Kerguelen, Indian/Mahe, Indian/Maldives, Indian/Mauritius, Indian/Reunion, Iran, Israel, Kwajalein, MET, MIT, NET, NST, NZ, NZ-CHAT, Pacific/Apia, Pacific/Auckland, Pacific/Chatham, Pacific/Chuuk, Pacific/Easter, Pacific/Efate, Pacific/Enderbury, Pacific/Fakaofo, Pacific/Fiji, Pacific/Funafuti, Pacific/Galapagos, Pacific/Gambier, Pacific/Guadalcanal, Pacific/Guam, Pacific/Kiritimati, Pacific/Kosrae, Pacific/Kwajalein, Pacific/Majuro, Pacific/Marquesas, Pacific/Nauru, Pacific/Niue, Pacific/Norfolk, Pacific/Noumea, Pacific/Palau, Pacific/Pohnpei, Pacific/Ponape, Pacific/Port_Moresby, Pacific/Rarotonga, Pacific/Saipan, Pacific/Tahiti, Pacific/Tarawa, Pacific/Tongatapu, Pacific/Truk, Pacific/Wake, Pacific/Wallis, Pacific/Yap, PLT, Portugal, ROK, Singapore, SST, SystemV/AST4ADT, SystemV/YST9, SystemV/YST9YDT, US/Alaska, US/Aleutian, VST, WET, W-SU"
				loop list="#timezoneList#" item="listValue"{
					var hasError = false;
					try{
						SetTimeZone(listValue);
						var testDate = ["02/09/2018","10:30:02"];
						var tempdate = parsedatetime(testDate[1] & " " & testDate[2]);
						var st = {
							timestamp: tempdate
						};
						var c = serialize(st);
						evaluate(c);
					}
					catch(any e){
						var hasError = true;
					}
				expect(hasError).toBefalse();
				}
			});
		});
	}
} 