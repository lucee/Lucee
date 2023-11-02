/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.commons.date;

import java.util.TimeZone;

public final class TimeZoneConstants {

	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	public static final TimeZone GMT0 = TimeZone.getTimeZone("GMT0");
	public static final TimeZone EUROPE_LONDON = TimeZone.getTimeZone("Europe/London");
	public static final TimeZone CET = TimeZone.getTimeZone("CET");

	/*
	 * static final TimeZone ETC_GMT_PLUS_12 = TimeZone.getTimeZone("Etc/GMT+12"); static final TimeZone
	 * ETC_GMT_PLUS_11 = TimeZone.getTimeZone("Etc/GMT+11"); static final TimeZone MIT =
	 * TimeZone.getTimeZone("MIT"); static final TimeZone PACIFIC_APIA =
	 * TimeZone.getTimeZone("Pacific/Apia"); static final TimeZone PACIFIC_MIDWAY =
	 * TimeZone.getTimeZone("Pacific/Midway"); static final TimeZone PACIFIC_NIUE =
	 * TimeZone.getTimeZone("Pacific/Niue"); static final TimeZone PACIFIC_PAGO_PAGO =
	 * TimeZone.getTimeZone("Pacific/Pago_Pago"); static final TimeZone PACIFIC_SAMOA =
	 * TimeZone.getTimeZone("Pacific/Samoa"); static final TimeZone US_SAMOA =
	 * TimeZone.getTimeZone("US/Samoa"); static final TimeZone AMERICA_ADAK =
	 * TimeZone.getTimeZone("America/Adak"); static final TimeZone AMERICA_ATKA =
	 * TimeZone.getTimeZone("America/Atka"); static final TimeZone ETC_GMT_PLUS_10 =
	 * TimeZone.getTimeZone("Etc/GMT+10"); static final TimeZone HST = TimeZone.getTimeZone("HST");
	 * static final TimeZone PACIFIC_FAKAOFO = TimeZone.getTimeZone("Pacific/Fakaofo"); static final
	 * TimeZone PACIFIC_HONOLULU = TimeZone.getTimeZone("Pacific/Honolulu"); static final TimeZone
	 * PACIFIC_JOHNSTON = TimeZone.getTimeZone("Pacific/Johnston"); static final TimeZone
	 * PACIFIC_RAROTONGA = TimeZone.getTimeZone("Pacific/Rarotonga"); static final TimeZone
	 * PACIFIC_TAHITI = TimeZone.getTimeZone("Pacific/Tahiti"); static final TimeZone SYSTEMV_HST10 =
	 * TimeZone.getTimeZone("SystemV/HST10"); static final TimeZone US_ALEUTIAN =
	 * TimeZone.getTimeZone("US/Aleutian"); static final TimeZone US_HAWAII =
	 * TimeZone.getTimeZone("US/Hawaii"); static final TimeZone US_EASTERN =
	 * TimeZone.getTimeZone("US/Eastern"); static final TimeZone ET = US_EASTERN; static final TimeZone
	 * US_MOUNTAIN = TimeZone.getTimeZone("US/Mountain"); static final TimeZone MT = US_MOUNTAIN; static
	 * final TimeZone US_CENTRAL = TimeZone.getTimeZone("US/Central"); static final TimeZone CT =
	 * US_CENTRAL; static final TimeZone US_PACIFIC = TimeZone.getTimeZone("US/Pacific"); static final
	 * TimeZone PT = US_PACIFIC;
	 * 
	 * static final TimeZone PACIFIC_MARQUESAS = TimeZone.getTimeZone("Pacific/Marquesas"); static final
	 * TimeZone AST = TimeZone.getTimeZone("AST"); static final TimeZone AMERICA_ANCHORAGE =
	 * TimeZone.getTimeZone("America/Anchorage"); static final TimeZone AMERICA_JUNEAU =
	 * TimeZone.getTimeZone("America/Juneau"); static final TimeZone AMERICA_NOME =
	 * TimeZone.getTimeZone("America/Nome"); static final TimeZone AMERICA_YAKUTAT =
	 * TimeZone.getTimeZone("America/Yakutat"); static final TimeZone ETC_GMT_PLUS_9 =
	 * TimeZone.getTimeZone("Etc/GMT+9"); static final TimeZone PACIFIC_GAMBIER =
	 * TimeZone.getTimeZone("Pacific/Gambier"); static final TimeZone SYSTEMV_YST9 =
	 * TimeZone.getTimeZone("SystemV/YST9"); static final TimeZone SYSTEMV_YST9YDT =
	 * TimeZone.getTimeZone("SystemV/YST9YDT"); static final TimeZone US_ALASKA =
	 * TimeZone.getTimeZone("US/Alaska"); static final TimeZone AMERICA_DAWSON =
	 * TimeZone.getTimeZone("America/Dawson"); static final TimeZone AMERICA_ENSENADA =
	 * TimeZone.getTimeZone("America/Ensenada"); static final TimeZone AMERICA_LOS_ANGELES =
	 * TimeZone.getTimeZone("America/Los_Angeles"); static final TimeZone AMERICA_TIJUANA =
	 * TimeZone.getTimeZone("America/Tijuana"); static final TimeZone AMERICA_VANCOUVER =
	 * TimeZone.getTimeZone("America/Vancouver"); static final TimeZone AMERICA_WHITEHORSE =
	 * TimeZone.getTimeZone("America/Whitehorse"); static final TimeZone CANADA_PACIFIC =
	 * TimeZone.getTimeZone("Canada/Pacific"); static final TimeZone CANADA_YUKON =
	 * TimeZone.getTimeZone("Canada/Yukon"); static final TimeZone ETC_GMT_PLUS_8 =
	 * TimeZone.getTimeZone("Etc/GMT+8"); static final TimeZone MEXICO_BAJANORTE =
	 * TimeZone.getTimeZone("Mexico/BajaNorte"); static final TimeZone PST =
	 * TimeZone.getTimeZone("PST"); static final TimeZone PST8PDT = TimeZone.getTimeZone("PST8PDT");
	 * static final TimeZone PACIFIC_PITCAIRN = TimeZone.getTimeZone("Pacific/Pitcairn"); static final
	 * TimeZone SYSTEMV_PST8 = TimeZone.getTimeZone("SystemV/PST8"); static final TimeZone
	 * SYSTEMV_PST8PDT = TimeZone.getTimeZone("SystemV/PST8PDT"); static final TimeZone US_PACIFIC_NEW =
	 * TimeZone.getTimeZone("US/Pacific-New"); static final TimeZone AMERICA_BOISE =
	 * TimeZone.getTimeZone("America/Boise"); static final TimeZone AMERICA_CAMBRIDGE_BAY =
	 * TimeZone.getTimeZone("America/Cambridge_Bay"); static final TimeZone AMERICA_CHIHUAHUA =
	 * TimeZone.getTimeZone("America/Chihuahua"); static final TimeZone AMERICA_DAWSON_CREEK =
	 * TimeZone.getTimeZone("America/Dawson_Creek"); static final TimeZone AMERICA_DENVER =
	 * TimeZone.getTimeZone("America/Denver"); static final TimeZone AMERICA_EDMONTON =
	 * TimeZone.getTimeZone("America/Edmonton"); static final TimeZone AMERICA_HERMOSILLO =
	 * TimeZone.getTimeZone("America/Hermosillo"); static final TimeZone AMERICA_INUVIK =
	 * TimeZone.getTimeZone("America/Inuvik"); static final TimeZone AMERICA_MAZATLAN =
	 * TimeZone.getTimeZone("America/Mazatlan"); static final TimeZone AMERICA_PHOENIX =
	 * TimeZone.getTimeZone("America/Phoenix"); static final TimeZone AMERICA_SHIPROCK =
	 * TimeZone.getTimeZone("America/Shiprock"); static final TimeZone AMERICA_YELLOWKNIFE =
	 * TimeZone.getTimeZone("America/Yellowknife"); static final TimeZone CANADA_MOUNTAIN =
	 * TimeZone.getTimeZone("Canada/Mountain"); static final TimeZone ETC_GMT_PLUS_7 =
	 * TimeZone.getTimeZone("Etc/GMT+7"); static final TimeZone MST = TimeZone.getTimeZone("MST");
	 * static final TimeZone MST7MDT = TimeZone.getTimeZone("MST7MDT"); static final TimeZone
	 * MEXICO_BAJASUR = TimeZone.getTimeZone("Mexico/BajaSur"); static final TimeZone NAVAJO =
	 * TimeZone.getTimeZone("Navajo"); static final TimeZone PNT = TimeZone.getTimeZone("PNT"); static
	 * final TimeZone SYSTEMV_MST7 = TimeZone.getTimeZone("SystemV/MST7"); static final TimeZone
	 * SYSTEMV_MST7MDT = TimeZone.getTimeZone("SystemV/MST7MDT"); static final TimeZone US_ARIZONA =
	 * TimeZone.getTimeZone("US/Arizona"); static final TimeZone AMERICA_BELIZE =
	 * TimeZone.getTimeZone("America/Belize"); static final TimeZone AMERICA_CANCUN =
	 * TimeZone.getTimeZone("America/Cancun"); static final TimeZone AMERICA_CHICAGO =
	 * TimeZone.getTimeZone("America/Chicago"); static final TimeZone AMERICA_COSTA_RICA =
	 * TimeZone.getTimeZone("America/Costa_Rica"); static final TimeZone AMERICA_EL_SALVADOR =
	 * TimeZone.getTimeZone("America/El_Salvador"); static final TimeZone AMERICA_GUATEMALA =
	 * TimeZone.getTimeZone("America/Guatemala"); static final TimeZone AMERICA_INDIANA_KNOX =
	 * TimeZone.getTimeZone("America/Indiana/Knox"); static final TimeZone AMERICA_INDIANA_TELL_CITY =
	 * TimeZone.getTimeZone("America/Indiana/Tell_City"); static final TimeZone AMERICA_KNOX_IN =
	 * TimeZone.getTimeZone("America/Knox_IN"); static final TimeZone AMERICA_MANAGUA =
	 * TimeZone.getTimeZone("America/Managua"); static final TimeZone AMERICA_MENOMINEE =
	 * TimeZone.getTimeZone("America/Menominee"); static final TimeZone AMERICA_MERIDA =
	 * TimeZone.getTimeZone("America/Merida"); static final TimeZone AMERICA_MEXICO_CITY =
	 * TimeZone.getTimeZone("America/Mexico_City"); static final TimeZone AMERICA_MONTERREY =
	 * TimeZone.getTimeZone("America/Monterrey"); static final TimeZone AMERICA_NORTH_DAKOTA_CENTER =
	 * TimeZone.getTimeZone("America/North_Dakota/Center"); static final TimeZone
	 * AMERICA_NORTH_DAKOTA_NEW_SALEM = TimeZone.getTimeZone("America/North_Dakota/New_Salem"); static
	 * final TimeZone AMERICA_RAINY_RIVER = TimeZone.getTimeZone("America/Rainy_River"); static final
	 * TimeZone AMERICA_RANKIN_INLET = TimeZone.getTimeZone("America/Rankin_Inlet"); static final
	 * TimeZone AMERICA_REGINA = TimeZone.getTimeZone("America/Regina"); static final TimeZone
	 * AMERICA_SWIFT_CURRENT = TimeZone.getTimeZone("America/Swift_Current"); static final TimeZone
	 * AMERICA_TEGUCIGALPA = TimeZone.getTimeZone("America/Tegucigalpa"); static final TimeZone
	 * AMERICA_WINNIPEG = TimeZone.getTimeZone("America/Winnipeg"); static final TimeZone CST =
	 * TimeZone.getTimeZone("CST"); static final TimeZone CST6CDT = TimeZone.getTimeZone("CST6CDT");
	 * static final TimeZone CANADA_CENTRAL = TimeZone.getTimeZone("Canada/Central"); static final
	 * TimeZone CANADA_EAST_SASKATCHEWAN = TimeZone.getTimeZone("Canada/East-Saskatchewan"); static
	 * final TimeZone CANADA_SASKATCHEWAN = TimeZone.getTimeZone("Canada/Saskatchewan"); static final
	 * TimeZone CHILE_EASTERISLAND = TimeZone.getTimeZone("Chile/EasterIsland"); static final TimeZone
	 * ETC_GMT_PLUS_6 = TimeZone.getTimeZone("Etc/GMT+6"); static final TimeZone MEXICO_GENERAL =
	 * TimeZone.getTimeZone("Mexico/General"); static final TimeZone PACIFIC_EASTER =
	 * TimeZone.getTimeZone("Pacific/Easter"); static final TimeZone PACIFIC_GALAPAGOS =
	 * TimeZone.getTimeZone("Pacific/Galapagos"); static final TimeZone SYSTEMV_CST6 =
	 * TimeZone.getTimeZone("SystemV/CST6"); static final TimeZone SYSTEMV_CST6CDT =
	 * TimeZone.getTimeZone("SystemV/CST6CDT"); static final TimeZone US_INDIANA_STARKE =
	 * TimeZone.getTimeZone("US/Indiana-Starke"); static final TimeZone AMERICA_ATIKOKAN =
	 * TimeZone.getTimeZone("America/Atikokan"); static final TimeZone AMERICA_BOGOTA =
	 * TimeZone.getTimeZone("America/Bogota"); static final TimeZone AMERICA_CAYMAN =
	 * TimeZone.getTimeZone("America/Cayman"); static final TimeZone AMERICA_CORAL_HARBOUR =
	 * TimeZone.getTimeZone("America/Coral_Harbour"); static final TimeZone AMERICA_DETROIT =
	 * TimeZone.getTimeZone("America/Detroit"); static final TimeZone AMERICA_EIRUNEPE =
	 * TimeZone.getTimeZone("America/Eirunepe"); static final TimeZone AMERICA_FORT_WAYNE =
	 * TimeZone.getTimeZone("America/Fort_Wayne"); static final TimeZone AMERICA_GRAND_TURK =
	 * TimeZone.getTimeZone("America/Grand_Turk"); static final TimeZone AMERICA_GUAYAQUIL =
	 * TimeZone.getTimeZone("America/Guayaquil"); static final TimeZone AMERICA_HAVANA =
	 * TimeZone.getTimeZone("America/Havana"); static final TimeZone AMERICA_INDIANA_INDIANAPOLIS =
	 * TimeZone.getTimeZone("America/Indiana/Indianapolis"); static final TimeZone
	 * AMERICA_INDIANA_MARENGO = TimeZone.getTimeZone("America/Indiana/Marengo"); static final TimeZone
	 * AMERICA_INDIANA_VEVAY = TimeZone.getTimeZone("America/Indiana/Vevay"); static final TimeZone
	 * AMERICA_INDIANA_WINAMAC = TimeZone.getTimeZone("America/Indiana/Winamac"); static final TimeZone
	 * AMERICA_INDIANAPOLIS = TimeZone.getTimeZone("America/Indianapolis"); static final TimeZone
	 * AMERICA_IQALUIT = TimeZone.getTimeZone("America/Iqaluit"); static final TimeZone AMERICA_JAMAICA
	 * = TimeZone.getTimeZone("America/Jamaica"); static final TimeZone AMERICA_KENTUCKY_LOUISVILLE =
	 * TimeZone.getTimeZone("America/Kentucky/Louisville"); static final TimeZone
	 * AMERICA_KENTUCKY_MONTICELLO = TimeZone.getTimeZone("America/Kentucky/Monticello"); static final
	 * TimeZone AMERICA_LIMA = TimeZone.getTimeZone("America/Lima"); static final TimeZone
	 * AMERICA_LOUISVILLE = TimeZone.getTimeZone("America/Louisville"); static final TimeZone
	 * AMERICA_MONTREAL = TimeZone.getTimeZone("America/Montreal"); static final TimeZone AMERICA_NASSAU
	 * = TimeZone.getTimeZone("America/Nassau"); static final TimeZone AMERICA_NEW_YORK =
	 * TimeZone.getTimeZone("America/New_York"); static final TimeZone AMERICA_NIPIGON =
	 * TimeZone.getTimeZone("America/Nipigon"); static final TimeZone AMERICA_PANAMA =
	 * TimeZone.getTimeZone("America/Panama"); static final TimeZone AMERICA_PANGNIRTUNG =
	 * TimeZone.getTimeZone("America/Pangnirtung"); static final TimeZone AMERICA_PORT_AU_PRINCE =
	 * TimeZone.getTimeZone("America/Port-au-Prince"); static final TimeZone AMERICA_PORTO_ACRE =
	 * TimeZone.getTimeZone("America/Porto_Acre"); static final TimeZone AMERICA_RESOLUTE =
	 * TimeZone.getTimeZone("America/Resolute"); static final TimeZone AMERICA_RIO_BRANCO =
	 * TimeZone.getTimeZone("America/Rio_Branco"); static final TimeZone AMERICA_THUNDER_BAY =
	 * TimeZone.getTimeZone("America/Thunder_Bay"); static final TimeZone AMERICA_TORONTO =
	 * TimeZone.getTimeZone("America/Toronto"); static final TimeZone BRAZIL_ACRE =
	 * TimeZone.getTimeZone("Brazil/Acre"); static final TimeZone CANADA_EASTERN =
	 * TimeZone.getTimeZone("Canada/Eastern"); static final TimeZone CUBA =
	 * TimeZone.getTimeZone("Cuba"); static final TimeZone EST = TimeZone.getTimeZone("EST"); static
	 * final TimeZone EST5EDT = TimeZone.getTimeZone("EST5EDT"); static final TimeZone ETC_GMT_PLUS_5 =
	 * TimeZone.getTimeZone("Etc/GMT+5"); static final TimeZone IET = TimeZone.getTimeZone("IET");
	 * static final TimeZone JAMAICA = TimeZone.getTimeZone("Jamaica"); static final TimeZone
	 * SYSTEMV_EST5 = TimeZone.getTimeZone("SystemV/EST5"); static final TimeZone SYSTEMV_EST5EDT =
	 * TimeZone.getTimeZone("SystemV/EST5EDT"); static final TimeZone US_EAST_INDIANA =
	 * TimeZone.getTimeZone("US/East-Indiana"); static final TimeZone US_MICHIGAN =
	 * TimeZone.getTimeZone("US/Michigan"); static final TimeZone AMERICA_ANGUILLA =
	 * TimeZone.getTimeZone("America/Anguilla"); static final TimeZone AMERICA_ANTIGUA =
	 * TimeZone.getTimeZone("America/Antigua"); static final TimeZone AMERICA_ARUBA =
	 * TimeZone.getTimeZone("America/Aruba"); static final TimeZone AMERICA_ASUNCION =
	 * TimeZone.getTimeZone("America/Asuncion"); static final TimeZone AMERICA_BARBADOS =
	 * TimeZone.getTimeZone("America/Barbados"); static final TimeZone AMERICA_BLANC_SABLON =
	 * TimeZone.getTimeZone("America/Blanc-Sablon"); static final TimeZone AMERICA_BOA_VISTA =
	 * TimeZone.getTimeZone("America/Boa_Vista"); static final TimeZone AMERICA_CAMPO_GRANDE =
	 * TimeZone.getTimeZone("America/Campo_Grande"); static final TimeZone AMERICA_CARACAS =
	 * TimeZone.getTimeZone("America/Caracas"); static final TimeZone AMERICA_CUIABA =
	 * TimeZone.getTimeZone("America/Cuiaba"); static final TimeZone AMERICA_CURACAO =
	 * TimeZone.getTimeZone("America/Curacao"); static final TimeZone AMERICA_DOMINICA =
	 * TimeZone.getTimeZone("America/Dominica"); static final TimeZone AMERICA_GLACE_BAY =
	 * TimeZone.getTimeZone("America/Glace_Bay"); static final TimeZone AMERICA_GOOSE_BAY =
	 * TimeZone.getTimeZone("America/Goose_Bay"); static final TimeZone AMERICA_GRENADA =
	 * TimeZone.getTimeZone("America/Grenada"); static final TimeZone AMERICA_GUADELOUPE =
	 * TimeZone.getTimeZone("America/Guadeloupe"); static final TimeZone AMERICA_GUYANA =
	 * TimeZone.getTimeZone("America/Guyana"); static final TimeZone AMERICA_HALIFAX =
	 * TimeZone.getTimeZone("America/Halifax"); static final TimeZone AMERICA_LA_PAZ =
	 * TimeZone.getTimeZone("America/La_Paz"); static final TimeZone AMERICA_MANAUS =
	 * TimeZone.getTimeZone("America/Manaus"); static final TimeZone AMERICA_MARTINIQUE =
	 * TimeZone.getTimeZone("America/Martinique"); static final TimeZone AMERICA_MONCTON =
	 * TimeZone.getTimeZone("America/Moncton"); static final TimeZone AMERICA_MONTSERRAT =
	 * TimeZone.getTimeZone("America/Montserrat"); static final TimeZone AMERICA_PORT_OF_SPAIN =
	 * TimeZone.getTimeZone("America/Port_of_Spain"); static final TimeZone AMERICA_PORTO_VELHO =
	 * TimeZone.getTimeZone("America/Porto_Velho"); static final TimeZone AMERICA_PUERTO_RICO =
	 * TimeZone.getTimeZone("America/Puerto_Rico"); static final TimeZone AMERICA_SANTIAGO =
	 * TimeZone.getTimeZone("America/Santiago"); static final TimeZone AMERICA_SANTO_DOMINGO =
	 * TimeZone.getTimeZone("America/Santo_Domingo"); static final TimeZone AMERICA_ST_KITTS =
	 * TimeZone.getTimeZone("America/St_Kitts"); static final TimeZone AMERICA_ST_LUCIA =
	 * TimeZone.getTimeZone("America/St_Lucia"); static final TimeZone AMERICA_ST_THOMAS =
	 * TimeZone.getTimeZone("America/St_Thomas"); static final TimeZone AMERICA_ST_VINCENT =
	 * TimeZone.getTimeZone("America/St_Vincent"); static final TimeZone AMERICA_THULE =
	 * TimeZone.getTimeZone("America/Thule"); static final TimeZone AMERICA_TORTOLA =
	 * TimeZone.getTimeZone("America/Tortola"); static final TimeZone AMERICA_VIRGIN =
	 * TimeZone.getTimeZone("America/Virgin"); static final TimeZone ANTARCTICA_PALMER =
	 * TimeZone.getTimeZone("Antarctica/Palmer"); static final TimeZone ATLANTIC_BERMUDA =
	 * TimeZone.getTimeZone("Atlantic/Bermuda"); static final TimeZone ATLANTIC_STANLEY =
	 * TimeZone.getTimeZone("Atlantic/Stanley"); static final TimeZone BRAZIL_WEST =
	 * TimeZone.getTimeZone("Brazil/West"); static final TimeZone CANADA_ATLANTIC =
	 * TimeZone.getTimeZone("Canada/Atlantic"); static final TimeZone CHILE_CONTINENTAL =
	 * TimeZone.getTimeZone("Chile/Continental"); static final TimeZone ETC_GMT_PLUS_4 =
	 * TimeZone.getTimeZone("Etc/GMT+4"); static final TimeZone PRT = TimeZone.getTimeZone("PRT");
	 * static final TimeZone SYSTEMV_AST4 = TimeZone.getTimeZone("SystemV/AST4"); static final TimeZone
	 * SYSTEMV_AST4ADT = TimeZone.getTimeZone("SystemV/AST4ADT"); static final TimeZone AMERICA_ST_JOHNS
	 * = TimeZone.getTimeZone("America/St_Johns"); static final TimeZone CNT =
	 * TimeZone.getTimeZone("CNT"); static final TimeZone CANADA_NEWFOUNDLAND =
	 * TimeZone.getTimeZone("Canada/Newfoundland"); static final TimeZone AGT =
	 * TimeZone.getTimeZone("AGT"); static final TimeZone AMERICA_ARAGUAINA =
	 * TimeZone.getTimeZone("America/Araguaina"); static final TimeZone AMERICA_ARGENTINA_BUENOS_AIRES =
	 * TimeZone.getTimeZone("America/Argentina/Buenos_Aires"); static final TimeZone
	 * AMERICA_ARGENTINA_CATAMARCA = TimeZone.getTimeZone("America/Argentina/Catamarca"); static final
	 * TimeZone AMERICA_ARGENTINA_COMODRIVADAVIA =
	 * TimeZone.getTimeZone("America/Argentina/ComodRivadavia"); static final TimeZone
	 * AMERICA_ARGENTINA_CORDOBA = TimeZone.getTimeZone("America/Argentina/Cordoba"); static final
	 * TimeZone AMERICA_ARGENTINA_JUJUY = TimeZone.getTimeZone("America/Argentina/Jujuy"); static final
	 * TimeZone AMERICA_ARGENTINA_LA_RIOJA = TimeZone.getTimeZone("America/Argentina/La_Rioja"); static
	 * final TimeZone AMERICA_ARGENTINA_MENDOZA = TimeZone.getTimeZone("America/Argentina/Mendoza");
	 * static final TimeZone AMERICA_ARGENTINA_RIO_GALLEGOS =
	 * TimeZone.getTimeZone("America/Argentina/Rio_Gallegos"); static final TimeZone
	 * AMERICA_ARGENTINA_SAN_JUAN = TimeZone.getTimeZone("America/Argentina/San_Juan"); static final
	 * TimeZone AMERICA_ARGENTINA_TUCUMAN = TimeZone.getTimeZone("America/Argentina/Tucuman"); static
	 * final TimeZone AMERICA_ARGENTINA_USHUAIA = TimeZone.getTimeZone("America/Argentina/Ushuaia");
	 * static final TimeZone AMERICA_BAHIA = TimeZone.getTimeZone("America/Bahia"); static final
	 * TimeZone AMERICA_BELEM = TimeZone.getTimeZone("America/Belem"); static final TimeZone
	 * AMERICA_BUENOS_AIRES = TimeZone.getTimeZone("America/Buenos_Aires"); static final TimeZone
	 * AMERICA_CATAMARCA = TimeZone.getTimeZone("America/Catamarca"); static final TimeZone
	 * AMERICA_CAYENNE = TimeZone.getTimeZone("America/Cayenne"); static final TimeZone AMERICA_CORDOBA
	 * = TimeZone.getTimeZone("America/Cordoba"); static final TimeZone AMERICA_FORTALEZA =
	 * TimeZone.getTimeZone("America/Fortaleza"); static final TimeZone AMERICA_GODTHAB =
	 * TimeZone.getTimeZone("America/Godthab"); static final TimeZone AMERICA_JUJUY =
	 * TimeZone.getTimeZone("America/Jujuy"); static final TimeZone AMERICA_MACEIO =
	 * TimeZone.getTimeZone("America/Maceio"); static final TimeZone AMERICA_MENDOZA =
	 * TimeZone.getTimeZone("America/Mendoza"); static final TimeZone AMERICA_MIQUELON =
	 * TimeZone.getTimeZone("America/Miquelon"); static final TimeZone AMERICA_MONTEVIDEO =
	 * TimeZone.getTimeZone("America/Montevideo"); static final TimeZone AMERICA_PARAMARIBO =
	 * TimeZone.getTimeZone("America/Paramaribo"); static final TimeZone AMERICA_RECIFE =
	 * TimeZone.getTimeZone("America/Recife"); static final TimeZone AMERICA_ROSARIO =
	 * TimeZone.getTimeZone("America/Rosario"); static final TimeZone AMERICA_SAO_PAULO =
	 * TimeZone.getTimeZone("America/Sao_Paulo"); static final TimeZone ANTARCTICA_ROTHERA =
	 * TimeZone.getTimeZone("Antarctica/Rothera"); static final TimeZone BET =
	 * TimeZone.getTimeZone("BET"); static final TimeZone BRAZIL_EAST =
	 * TimeZone.getTimeZone("Brazil/East"); static final TimeZone ETC_GMT_PLUS_3 =
	 * TimeZone.getTimeZone("Etc/GMT+3"); static final TimeZone AMERICA_NORONHA =
	 * TimeZone.getTimeZone("America/Noronha"); static final TimeZone ATLANTIC_SOUTH_GEORGIA =
	 * TimeZone.getTimeZone("Atlantic/South_Georgia"); static final TimeZone BRAZIL_DENORONHA =
	 * TimeZone.getTimeZone("Brazil/DeNoronha"); static final TimeZone ETC_GMT_PLUS_2 =
	 * TimeZone.getTimeZone("Etc/GMT+2"); static final TimeZone AMERICA_SCORESBYSUND =
	 * TimeZone.getTimeZone("America/Scoresbysund"); static final TimeZone ATLANTIC_AZORES =
	 * TimeZone.getTimeZone("Atlantic/Azores"); static final TimeZone ATLANTIC_CAPE_VERDE =
	 * TimeZone.getTimeZone("Atlantic/Cape_Verde"); static final TimeZone ETC_GMT_PLUS_1 =
	 * TimeZone.getTimeZone("Etc/GMT+1"); static final TimeZone AFRICA_ABIDJAN =
	 * TimeZone.getTimeZone("Africa/Abidjan"); static final TimeZone AFRICA_ACCRA =
	 * TimeZone.getTimeZone("Africa/Accra"); static final TimeZone AFRICA_BAMAKO =
	 * TimeZone.getTimeZone("Africa/Bamako"); static final TimeZone AFRICA_BANJUL =
	 * TimeZone.getTimeZone("Africa/Banjul"); static final TimeZone AFRICA_BISSAU =
	 * TimeZone.getTimeZone("Africa/Bissau"); static final TimeZone AFRICA_CASABLANCA =
	 * TimeZone.getTimeZone("Africa/Casablanca"); static final TimeZone AFRICA_CONAKRY =
	 * TimeZone.getTimeZone("Africa/Conakry"); static final TimeZone AFRICA_DAKAR =
	 * TimeZone.getTimeZone("Africa/Dakar"); static final TimeZone AFRICA_EL_AAIUN =
	 * TimeZone.getTimeZone("Africa/El_Aaiun"); static final TimeZone AFRICA_FREETOWN =
	 * TimeZone.getTimeZone("Africa/Freetown"); static final TimeZone AFRICA_LOME =
	 * TimeZone.getTimeZone("Africa/Lome"); static final TimeZone AFRICA_MONROVIA =
	 * TimeZone.getTimeZone("Africa/Monrovia"); static final TimeZone AFRICA_NOUAKCHOTT =
	 * TimeZone.getTimeZone("Africa/Nouakchott"); static final TimeZone AFRICA_OUAGADOUGOU =
	 * TimeZone.getTimeZone("Africa/Ouagadougou"); static final TimeZone AFRICA_SAO_TOME =
	 * TimeZone.getTimeZone("Africa/Sao_Tome"); static final TimeZone AFRICA_TIMBUKTU =
	 * TimeZone.getTimeZone("Africa/Timbuktu"); static final TimeZone AMERICA_DANMARKSHAVN =
	 * TimeZone.getTimeZone("America/Danmarkshavn"); static final TimeZone ATLANTIC_CANARY =
	 * TimeZone.getTimeZone("Atlantic/Canary"); static final TimeZone ATLANTIC_FAEROE =
	 * TimeZone.getTimeZone("Atlantic/Faeroe"); static final TimeZone ATLANTIC_FAROE =
	 * TimeZone.getTimeZone("Atlantic/Faroe"); static final TimeZone ATLANTIC_MADEIRA =
	 * TimeZone.getTimeZone("Atlantic/Madeira"); static final TimeZone ATLANTIC_REYKJAVIK =
	 * TimeZone.getTimeZone("Atlantic/Reykjavik"); static final TimeZone ATLANTIC_ST_HELENA =
	 * TimeZone.getTimeZone("Atlantic/St_Helena"); static final TimeZone EIRE =
	 * TimeZone.getTimeZone("Eire"); static final TimeZone ETC_GMT = TimeZone.getTimeZone("Etc/GMT");
	 * static final TimeZone ETC_GMT_PLUS_0 = TimeZone.getTimeZone("Etc/GMT+0"); static final TimeZone
	 * ETC_GMT_MINUS_0 = TimeZone.getTimeZone("Etc/GMT-0"); static final TimeZone ETC_GMT0 =
	 * TimeZone.getTimeZone("Etc/GMT0"); static final TimeZone ETC_GREENWICH =
	 * TimeZone.getTimeZone("Etc/Greenwich"); static final TimeZone ETC_UCT =
	 * TimeZone.getTimeZone("Etc/UCT"); static final TimeZone ETC_UTC = TimeZone.getTimeZone("Etc/UTC");
	 * static final TimeZone ETC_UNIVERSAL = TimeZone.getTimeZone("Etc/Universal"); static final
	 * TimeZone ETC_ZULU = TimeZone.getTimeZone("Etc/Zulu"); static final TimeZone EUROPE_BELFAST =
	 * TimeZone.getTimeZone("Europe/Belfast"); static final TimeZone EUROPE_DUBLIN =
	 * TimeZone.getTimeZone("Europe/Dublin"); static final TimeZone EUROPE_GUERNSEY =
	 * TimeZone.getTimeZone("Europe/Guernsey"); static final TimeZone EUROPE_ISLE_OF_MAN =
	 * TimeZone.getTimeZone("Europe/Isle_of_Man"); static final TimeZone EUROPE_JERSEY =
	 * TimeZone.getTimeZone("Europe/Jersey"); static final TimeZone EUROPE_LISBON =
	 * TimeZone.getTimeZone("Europe/Lisbon"); static final TimeZone GB = TimeZone.getTimeZone("GB");
	 * static final TimeZone GB_EIRE = TimeZone.getTimeZone("GB-Eire"); static final TimeZone GREENWICH
	 * = TimeZone.getTimeZone("Greenwich"); static final TimeZone ICELAND =
	 * TimeZone.getTimeZone("Iceland"); static final TimeZone PORTUGAL =
	 * TimeZone.getTimeZone("Portugal"); static final TimeZone UCT = TimeZone.getTimeZone("UCT"); static
	 * final TimeZone UNIVERSAL = TimeZone.getTimeZone("Universal"); static final TimeZone WET =
	 * TimeZone.getTimeZone("WET"); static final TimeZone ZULU = TimeZone.getTimeZone("Zulu"); static
	 * final TimeZone AFRICA_ALGIERS = TimeZone.getTimeZone("Africa/Algiers"); static final TimeZone
	 * AFRICA_BANGUI = TimeZone.getTimeZone("Africa/Bangui"); static final TimeZone AFRICA_BRAZZAVILLE =
	 * TimeZone.getTimeZone("Africa/Brazzaville"); static final TimeZone AFRICA_CEUTA =
	 * TimeZone.getTimeZone("Africa/Ceuta"); static final TimeZone AFRICA_DOUALA =
	 * TimeZone.getTimeZone("Africa/Douala"); static final TimeZone AFRICA_KINSHASA =
	 * TimeZone.getTimeZone("Africa/Kinshasa"); static final TimeZone AFRICA_LAGOS =
	 * TimeZone.getTimeZone("Africa/Lagos"); static final TimeZone AFRICA_LIBREVILLE =
	 * TimeZone.getTimeZone("Africa/Libreville"); static final TimeZone AFRICA_LUANDA =
	 * TimeZone.getTimeZone("Africa/Luanda"); static final TimeZone AFRICA_MALABO =
	 * TimeZone.getTimeZone("Africa/Malabo"); static final TimeZone AFRICA_NDJAMENA =
	 * TimeZone.getTimeZone("Africa/Ndjamena"); static final TimeZone AFRICA_NIAMEY =
	 * TimeZone.getTimeZone("Africa/Niamey"); static final TimeZone AFRICA_PORTO_NOVO =
	 * TimeZone.getTimeZone("Africa/Porto-Novo"); static final TimeZone AFRICA_TUNIS =
	 * TimeZone.getTimeZone("Africa/Tunis"); static final TimeZone AFRICA_WINDHOEK =
	 * TimeZone.getTimeZone("Africa/Windhoek"); static final TimeZone ARCTIC_LONGYEARBYEN =
	 * TimeZone.getTimeZone("Arctic/Longyearbyen"); static final TimeZone ATLANTIC_JAN_MAYEN =
	 * TimeZone.getTimeZone("Atlantic/Jan_Mayen"); static final TimeZone ECT =
	 * TimeZone.getTimeZone("ECT"); static final TimeZone ETC_GMT_MINUS_1 =
	 * TimeZone.getTimeZone("Etc/GMT-1"); static final TimeZone EUROPE_AMSTERDAM =
	 * TimeZone.getTimeZone("Europe/Amsterdam"); static final TimeZone EUROPE_ANDORRA =
	 * TimeZone.getTimeZone("Europe/Andorra"); static final TimeZone EUROPE_BELGRADE =
	 * TimeZone.getTimeZone("Europe/Belgrade"); static final TimeZone EUROPE_BERLIN =
	 * TimeZone.getTimeZone("Europe/Berlin"); static final TimeZone EUROPE_BRATISLAVA =
	 * TimeZone.getTimeZone("Europe/Bratislava"); static final TimeZone EUROPE_BRUSSELS =
	 * TimeZone.getTimeZone("Europe/Brussels"); static final TimeZone EUROPE_BUDAPEST =
	 * TimeZone.getTimeZone("Europe/Budapest"); static final TimeZone EUROPE_COPENHAGEN =
	 * TimeZone.getTimeZone("Europe/Copenhagen"); static final TimeZone EUROPE_GIBRALTAR =
	 * TimeZone.getTimeZone("Europe/Gibraltar"); static final TimeZone EUROPE_LJUBLJANA =
	 * TimeZone.getTimeZone("Europe/Ljubljana"); static final TimeZone EUROPE_LUXEMBOURG =
	 * TimeZone.getTimeZone("Europe/Luxembourg"); static final TimeZone EUROPE_MADRID =
	 * TimeZone.getTimeZone("Europe/Madrid"); static final TimeZone EUROPE_MALTA =
	 * TimeZone.getTimeZone("Europe/Malta"); static final TimeZone EUROPE_MONACO =
	 * TimeZone.getTimeZone("Europe/Monaco"); static final TimeZone EUROPE_OSLO =
	 * TimeZone.getTimeZone("Europe/Oslo"); static final TimeZone EUROPE_PARIS =
	 * TimeZone.getTimeZone("Europe/Paris"); static final TimeZone EUROPE_PODGORICA =
	 * TimeZone.getTimeZone("Europe/Podgorica"); static final TimeZone EUROPE_PRAGUE =
	 * TimeZone.getTimeZone("Europe/Prague"); static final TimeZone EUROPE_ROME =
	 * TimeZone.getTimeZone("Europe/Rome"); static final TimeZone EUROPE_SAN_MARINO =
	 * TimeZone.getTimeZone("Europe/San_Marino"); static final TimeZone EUROPE_SARAJEVO =
	 * TimeZone.getTimeZone("Europe/Sarajevo"); static final TimeZone EUROPE_SKOPJE =
	 * TimeZone.getTimeZone("Europe/Skopje"); static final TimeZone EUROPE_STOCKHOLM =
	 * TimeZone.getTimeZone("Europe/Stockholm"); static final TimeZone EUROPE_TIRANE =
	 * TimeZone.getTimeZone("Europe/Tirane"); static final TimeZone EUROPE_VADUZ =
	 * TimeZone.getTimeZone("Europe/Vaduz"); static final TimeZone EUROPE_VATICAN =
	 * TimeZone.getTimeZone("Europe/Vatican"); static final TimeZone EUROPE_VIENNA =
	 * TimeZone.getTimeZone("Europe/Vienna"); static final TimeZone EUROPE_WARSAW =
	 * TimeZone.getTimeZone("Europe/Warsaw"); static final TimeZone EUROPE_ZAGREB =
	 * TimeZone.getTimeZone("Europe/Zagreb"); static final TimeZone EUROPE_ZURICH =
	 * TimeZone.getTimeZone("Europe/Zurich"); static final TimeZone MET = TimeZone.getTimeZone("MET");
	 * static final TimeZone POLAND = TimeZone.getTimeZone("Poland"); static final TimeZone ART =
	 * TimeZone.getTimeZone("ART"); static final TimeZone AFRICA_BLANTYRE =
	 * TimeZone.getTimeZone("Africa/Blantyre"); static final TimeZone AFRICA_BUJUMBURA =
	 * TimeZone.getTimeZone("Africa/Bujumbura"); static final TimeZone AFRICA_CAIRO =
	 * TimeZone.getTimeZone("Africa/Cairo"); static final TimeZone AFRICA_GABORONE =
	 * TimeZone.getTimeZone("Africa/Gaborone"); static final TimeZone AFRICA_HARARE =
	 * TimeZone.getTimeZone("Africa/Harare"); static final TimeZone AFRICA_JOHANNESBURG =
	 * TimeZone.getTimeZone("Africa/Johannesburg"); static final TimeZone AFRICA_KIGALI =
	 * TimeZone.getTimeZone("Africa/Kigali"); static final TimeZone AFRICA_LUBUMBASHI =
	 * TimeZone.getTimeZone("Africa/Lubumbashi"); static final TimeZone AFRICA_LUSAKA =
	 * TimeZone.getTimeZone("Africa/Lusaka"); static final TimeZone AFRICA_MAPUTO =
	 * TimeZone.getTimeZone("Africa/Maputo"); static final TimeZone AFRICA_MASERU =
	 * TimeZone.getTimeZone("Africa/Maseru"); static final TimeZone AFRICA_MBABANE =
	 * TimeZone.getTimeZone("Africa/Mbabane"); static final TimeZone AFRICA_TRIPOLI =
	 * TimeZone.getTimeZone("Africa/Tripoli"); static final TimeZone ASIA_AMMAN =
	 * TimeZone.getTimeZone("Asia/Amman"); static final TimeZone ASIA_BEIRUT =
	 * TimeZone.getTimeZone("Asia/Beirut"); static final TimeZone ASIA_DAMASCUS =
	 * TimeZone.getTimeZone("Asia/Damascus"); static final TimeZone ASIA_GAZA =
	 * TimeZone.getTimeZone("Asia/Gaza"); static final TimeZone ASIA_ISTANBUL =
	 * TimeZone.getTimeZone("Asia/Istanbul"); static final TimeZone ASIA_JERUSALEM =
	 * TimeZone.getTimeZone("Asia/Jerusalem"); static final TimeZone ASIA_NICOSIA =
	 * TimeZone.getTimeZone("Asia/Nicosia"); static final TimeZone ASIA_TEL_AVIV =
	 * TimeZone.getTimeZone("Asia/Tel_Aviv"); static final TimeZone CAT = TimeZone.getTimeZone("CAT");
	 * static final TimeZone EET = TimeZone.getTimeZone("EET"); static final TimeZone EGYPT =
	 * TimeZone.getTimeZone("Egypt"); static final TimeZone ETC_GMT_MINUS_2 =
	 * TimeZone.getTimeZone("Etc/GMT-2"); static final TimeZone EUROPE_ATHENS =
	 * TimeZone.getTimeZone("Europe/Athens"); static final TimeZone EUROPE_BUCHAREST =
	 * TimeZone.getTimeZone("Europe/Bucharest"); static final TimeZone EUROPE_CHISINAU =
	 * TimeZone.getTimeZone("Europe/Chisinau"); static final TimeZone EUROPE_HELSINKI =
	 * TimeZone.getTimeZone("Europe/Helsinki"); static final TimeZone EUROPE_ISTANBUL =
	 * TimeZone.getTimeZone("Europe/Istanbul"); static final TimeZone EUROPE_KALININGRAD =
	 * TimeZone.getTimeZone("Europe/Kaliningrad"); static final TimeZone EUROPE_KIEV =
	 * TimeZone.getTimeZone("Europe/Kiev"); static final TimeZone EUROPE_MARIEHAMN =
	 * TimeZone.getTimeZone("Europe/Mariehamn"); static final TimeZone EUROPE_MINSK =
	 * TimeZone.getTimeZone("Europe/Minsk"); static final TimeZone EUROPE_NICOSIA =
	 * TimeZone.getTimeZone("Europe/Nicosia"); static final TimeZone EUROPE_RIGA =
	 * TimeZone.getTimeZone("Europe/Riga"); static final TimeZone EUROPE_SIMFEROPOL =
	 * TimeZone.getTimeZone("Europe/Simferopol"); static final TimeZone EUROPE_SOFIA =
	 * TimeZone.getTimeZone("Europe/Sofia"); static final TimeZone EUROPE_TALLINN =
	 * TimeZone.getTimeZone("Europe/Tallinn"); static final TimeZone EUROPE_TIRASPOL =
	 * TimeZone.getTimeZone("Europe/Tiraspol"); static final TimeZone EUROPE_UZHGOROD =
	 * TimeZone.getTimeZone("Europe/Uzhgorod"); static final TimeZone EUROPE_VILNIUS =
	 * TimeZone.getTimeZone("Europe/Vilnius"); static final TimeZone EUROPE_ZAPOROZHYE =
	 * TimeZone.getTimeZone("Europe/Zaporozhye"); static final TimeZone ISRAEL =
	 * TimeZone.getTimeZone("Israel"); static final TimeZone LIBYA = TimeZone.getTimeZone("Libya");
	 * static final TimeZone TURKEY = TimeZone.getTimeZone("Turkey"); static final TimeZone
	 * AFRICA_ADDIS_ABABA = TimeZone.getTimeZone("Africa/Addis_Ababa"); static final TimeZone
	 * AFRICA_ASMARA = TimeZone.getTimeZone("Africa/Asmara"); static final TimeZone AFRICA_ASMERA =
	 * TimeZone.getTimeZone("Africa/Asmera"); static final TimeZone AFRICA_DAR_ES_SALAAM =
	 * TimeZone.getTimeZone("Africa/Dar_es_Salaam"); static final TimeZone AFRICA_DJIBOUTI =
	 * TimeZone.getTimeZone("Africa/Djibouti"); static final TimeZone AFRICA_KAMPALA =
	 * TimeZone.getTimeZone("Africa/Kampala"); static final TimeZone AFRICA_KHARTOUM =
	 * TimeZone.getTimeZone("Africa/Khartoum"); static final TimeZone AFRICA_MOGADISHU =
	 * TimeZone.getTimeZone("Africa/Mogadishu"); static final TimeZone AFRICA_NAIROBI =
	 * TimeZone.getTimeZone("Africa/Nairobi"); static final TimeZone ANTARCTICA_SYOWA =
	 * TimeZone.getTimeZone("Antarctica/Syowa"); static final TimeZone ASIA_ADEN =
	 * TimeZone.getTimeZone("Asia/Aden"); static final TimeZone ASIA_BAGHDAD =
	 * TimeZone.getTimeZone("Asia/Baghdad"); static final TimeZone ASIA_BAHRAIN =
	 * TimeZone.getTimeZone("Asia/Bahrain"); static final TimeZone ASIA_KUWAIT =
	 * TimeZone.getTimeZone("Asia/Kuwait"); static final TimeZone ASIA_QATAR =
	 * TimeZone.getTimeZone("Asia/Qatar"); static final TimeZone ASIA_RIYADH =
	 * TimeZone.getTimeZone("Asia/Riyadh"); static final TimeZone EAT = TimeZone.getTimeZone("EAT");
	 * static final TimeZone ETC_GMT_MINUS_3 = TimeZone.getTimeZone("Etc/GMT-3"); static final TimeZone
	 * EUROPE_MOSCOW = TimeZone.getTimeZone("Europe/Moscow"); static final TimeZone EUROPE_VOLGOGRAD =
	 * TimeZone.getTimeZone("Europe/Volgograd"); static final TimeZone INDIAN_ANTANANARIVO =
	 * TimeZone.getTimeZone("Indian/Antananarivo"); static final TimeZone INDIAN_COMORO =
	 * TimeZone.getTimeZone("Indian/Comoro"); static final TimeZone INDIAN_MAYOTTE =
	 * TimeZone.getTimeZone("Indian/Mayotte"); static final TimeZone W_SU =
	 * TimeZone.getTimeZone("W-SU"); static final TimeZone ASIA_RIYADH87 =
	 * TimeZone.getTimeZone("Asia/Riyadh87"); static final TimeZone ASIA_RIYADH88 =
	 * TimeZone.getTimeZone("Asia/Riyadh88"); static final TimeZone ASIA_RIYADH89 =
	 * TimeZone.getTimeZone("Asia/Riyadh89"); static final TimeZone MIDEAST_RIYADH87 =
	 * TimeZone.getTimeZone("Mideast/Riyadh87"); static final TimeZone MIDEAST_RIYADH88 =
	 * TimeZone.getTimeZone("Mideast/Riyadh88"); static final TimeZone MIDEAST_RIYADH89 =
	 * TimeZone.getTimeZone("Mideast/Riyadh89"); static final TimeZone ASIA_TEHRAN =
	 * TimeZone.getTimeZone("Asia/Tehran"); static final TimeZone IRAN = TimeZone.getTimeZone("Iran");
	 * static final TimeZone ASIA_BAKU = TimeZone.getTimeZone("Asia/Baku"); static final TimeZone
	 * ASIA_DUBAI = TimeZone.getTimeZone("Asia/Dubai"); static final TimeZone ASIA_MUSCAT =
	 * TimeZone.getTimeZone("Asia/Muscat"); static final TimeZone ASIA_TBILISI =
	 * TimeZone.getTimeZone("Asia/Tbilisi"); static final TimeZone ASIA_YEREVAN =
	 * TimeZone.getTimeZone("Asia/Yerevan"); static final TimeZone ETC_GMT_MINUS_4 =
	 * TimeZone.getTimeZone("Etc/GMT-4"); static final TimeZone EUROPE_SAMARA =
	 * TimeZone.getTimeZone("Europe/Samara"); static final TimeZone INDIAN_MAHE =
	 * TimeZone.getTimeZone("Indian/Mahe"); static final TimeZone INDIAN_MAURITIUS =
	 * TimeZone.getTimeZone("Indian/Mauritius"); static final TimeZone INDIAN_REUNION =
	 * TimeZone.getTimeZone("Indian/Reunion"); static final TimeZone NET = TimeZone.getTimeZone("NET");
	 * static final TimeZone ASIA_KABUL = TimeZone.getTimeZone("Asia/Kabul"); static final TimeZone
	 * ASIA_AQTAU = TimeZone.getTimeZone("Asia/Aqtau"); static final TimeZone ASIA_AQTOBE =
	 * TimeZone.getTimeZone("Asia/Aqtobe"); static final TimeZone ASIA_ASHGABAT =
	 * TimeZone.getTimeZone("Asia/Ashgabat"); static final TimeZone ASIA_ASHKHABAD =
	 * TimeZone.getTimeZone("Asia/Ashkhabad"); static final TimeZone ASIA_DUSHANBE =
	 * TimeZone.getTimeZone("Asia/Dushanbe"); static final TimeZone ASIA_KARACHI =
	 * TimeZone.getTimeZone("Asia/Karachi"); static final TimeZone ASIA_ORAL =
	 * TimeZone.getTimeZone("Asia/Oral"); static final TimeZone ASIA_SAMARKAND =
	 * TimeZone.getTimeZone("Asia/Samarkand"); static final TimeZone ASIA_TASHKENT =
	 * TimeZone.getTimeZone("Asia/Tashkent"); static final TimeZone ASIA_YEKATERINBURG =
	 * TimeZone.getTimeZone("Asia/Yekaterinburg"); static final TimeZone ETC_GMT_MINUS_5 =
	 * TimeZone.getTimeZone("Etc/GMT-5"); static final TimeZone INDIAN_KERGUELEN =
	 * TimeZone.getTimeZone("Indian/Kerguelen"); static final TimeZone INDIAN_MALDIVES =
	 * TimeZone.getTimeZone("Indian/Maldives"); static final TimeZone PLT = TimeZone.getTimeZone("PLT");
	 * static final TimeZone ASIA_CALCUTTA = TimeZone.getTimeZone("Asia/Calcutta"); static final
	 * TimeZone ASIA_COLOMBO = TimeZone.getTimeZone("Asia/Colombo"); static final TimeZone IST =
	 * TimeZone.getTimeZone("IST"); static final TimeZone ASIA_KATMANDU =
	 * TimeZone.getTimeZone("Asia/Katmandu"); static final TimeZone ANTARCTICA_MAWSON =
	 * TimeZone.getTimeZone("Antarctica/Mawson"); static final TimeZone ANTARCTICA_VOSTOK =
	 * TimeZone.getTimeZone("Antarctica/Vostok"); static final TimeZone ASIA_ALMATY =
	 * TimeZone.getTimeZone("Asia/Almaty"); static final TimeZone ASIA_BISHKEK =
	 * TimeZone.getTimeZone("Asia/Bishkek"); static final TimeZone ASIA_DACCA =
	 * TimeZone.getTimeZone("Asia/Dacca"); static final TimeZone ASIA_DHAKA =
	 * TimeZone.getTimeZone("Asia/Dhaka"); static final TimeZone ASIA_NOVOSIBIRSK =
	 * TimeZone.getTimeZone("Asia/Novosibirsk"); static final TimeZone ASIA_OMSK =
	 * TimeZone.getTimeZone("Asia/Omsk"); static final TimeZone ASIA_QYZYLORDA =
	 * TimeZone.getTimeZone("Asia/Qyzylorda"); static final TimeZone ASIA_THIMBU =
	 * TimeZone.getTimeZone("Asia/Thimbu"); static final TimeZone ASIA_THIMPHU =
	 * TimeZone.getTimeZone("Asia/Thimphu"); static final TimeZone BST = TimeZone.getTimeZone("BST");
	 * static final TimeZone ETC_GMT_MINUS_6 = TimeZone.getTimeZone("Etc/GMT-6"); static final TimeZone
	 * INDIAN_CHAGOS = TimeZone.getTimeZone("Indian/Chagos"); static final TimeZone ASIA_RANGOON =
	 * TimeZone.getTimeZone("Asia/Rangoon"); static final TimeZone INDIAN_COCOS =
	 * TimeZone.getTimeZone("Indian/Cocos"); static final TimeZone ANTARCTICA_DAVIS =
	 * TimeZone.getTimeZone("Antarctica/Davis"); static final TimeZone ASIA_BANGKOK =
	 * TimeZone.getTimeZone("Asia/Bangkok"); static final TimeZone ASIA_HOVD =
	 * TimeZone.getTimeZone("Asia/Hovd"); static final TimeZone ASIA_JAKARTA =
	 * TimeZone.getTimeZone("Asia/Jakarta"); static final TimeZone ASIA_KRASNOYARSK =
	 * TimeZone.getTimeZone("Asia/Krasnoyarsk"); static final TimeZone ASIA_PHNOM_PENH =
	 * TimeZone.getTimeZone("Asia/Phnom_Penh"); static final TimeZone ASIA_PONTIANAK =
	 * TimeZone.getTimeZone("Asia/Pontianak"); static final TimeZone ASIA_SAIGON =
	 * TimeZone.getTimeZone("Asia/Saigon"); static final TimeZone ASIA_VIENTIANE =
	 * TimeZone.getTimeZone("Asia/Vientiane"); static final TimeZone ETC_GMT_MINUS_7 =
	 * TimeZone.getTimeZone("Etc/GMT-7"); static final TimeZone INDIAN_CHRISTMAS =
	 * TimeZone.getTimeZone("Indian/Christmas"); static final TimeZone VST =
	 * TimeZone.getTimeZone("VST"); static final TimeZone ANTARCTICA_CASEY =
	 * TimeZone.getTimeZone("Antarctica/Casey"); static final TimeZone ASIA_BRUNEI =
	 * TimeZone.getTimeZone("Asia/Brunei"); static final TimeZone ASIA_CHONGQING =
	 * TimeZone.getTimeZone("Asia/Chongqing"); static final TimeZone ASIA_CHUNGKING =
	 * TimeZone.getTimeZone("Asia/Chungking"); static final TimeZone ASIA_HARBIN =
	 * TimeZone.getTimeZone("Asia/Harbin"); static final TimeZone ASIA_HONG_KONG =
	 * TimeZone.getTimeZone("Asia/Hong_Kong"); static final TimeZone ASIA_IRKUTSK =
	 * TimeZone.getTimeZone("Asia/Irkutsk"); static final TimeZone ASIA_KASHGAR =
	 * TimeZone.getTimeZone("Asia/Kashgar"); static final TimeZone ASIA_KUALA_LUMPUR =
	 * TimeZone.getTimeZone("Asia/Kuala_Lumpur"); static final TimeZone ASIA_KUCHING =
	 * TimeZone.getTimeZone("Asia/Kuching"); static final TimeZone ASIA_MACAO =
	 * TimeZone.getTimeZone("Asia/Macao"); static final TimeZone ASIA_MACAU =
	 * TimeZone.getTimeZone("Asia/Macau"); static final TimeZone ASIA_MAKASSAR =
	 * TimeZone.getTimeZone("Asia/Makassar"); static final TimeZone ASIA_MANILA =
	 * TimeZone.getTimeZone("Asia/Manila"); static final TimeZone ASIA_SHANGHAI =
	 * TimeZone.getTimeZone("Asia/Shanghai"); static final TimeZone ASIA_SINGAPORE =
	 * TimeZone.getTimeZone("Asia/Singapore"); static final TimeZone ASIA_TAIPEI =
	 * TimeZone.getTimeZone("Asia/Taipei"); static final TimeZone ASIA_UJUNG_PANDANG =
	 * TimeZone.getTimeZone("Asia/Ujung_Pandang"); static final TimeZone ASIA_ULAANBAATAR =
	 * TimeZone.getTimeZone("Asia/Ulaanbaatar"); static final TimeZone ASIA_ULAN_BATOR =
	 * TimeZone.getTimeZone("Asia/Ulan_Bator"); static final TimeZone ASIA_URUMQI =
	 * TimeZone.getTimeZone("Asia/Urumqi"); static final TimeZone AUSTRALIA_PERTH =
	 * TimeZone.getTimeZone("Australia/Perth"); static final TimeZone AUSTRALIA_WEST =
	 * TimeZone.getTimeZone("Australia/West"); static final TimeZone CTT = TimeZone.getTimeZone("CTT");
	 * static final TimeZone ETC_GMT_MINUS_8 = TimeZone.getTimeZone("Etc/GMT-8"); static final TimeZone
	 * HONGKONG = TimeZone.getTimeZone("Hongkong"); static final TimeZone PRC =
	 * TimeZone.getTimeZone("PRC"); static final TimeZone SINGAPORE = TimeZone.getTimeZone("Singapore");
	 * static final TimeZone AUSTRALIA_EUCLA = TimeZone.getTimeZone("Australia/Eucla"); static final
	 * TimeZone ASIA_CHOIBALSAN = TimeZone.getTimeZone("Asia/Choibalsan"); static final TimeZone
	 * ASIA_DILI = TimeZone.getTimeZone("Asia/Dili"); static final TimeZone ASIA_JAYAPURA =
	 * TimeZone.getTimeZone("Asia/Jayapura"); static final TimeZone ASIA_PYONGYANG =
	 * TimeZone.getTimeZone("Asia/Pyongyang"); static final TimeZone ASIA_SEOUL =
	 * TimeZone.getTimeZone("Asia/Seoul"); static final TimeZone ASIA_TOKYO =
	 * TimeZone.getTimeZone("Asia/Tokyo"); static final TimeZone ASIA_YAKUTSK =
	 * TimeZone.getTimeZone("Asia/Yakutsk"); static final TimeZone ETC_GMT_MINUS_9 =
	 * TimeZone.getTimeZone("Etc/GMT-9"); static final TimeZone JST = TimeZone.getTimeZone("JST");
	 * static final TimeZone JAPAN = TimeZone.getTimeZone("Japan"); static final TimeZone PACIFIC_PALAU
	 * = TimeZone.getTimeZone("Pacific/Palau"); static final TimeZone ROK = TimeZone.getTimeZone("ROK");
	 * static final TimeZone ACT = TimeZone.getTimeZone("ACT"); static final TimeZone AUSTRALIA_ADELAIDE
	 * = TimeZone.getTimeZone("Australia/Adelaide"); static final TimeZone AUSTRALIA_BROKEN_HILL =
	 * TimeZone.getTimeZone("Australia/Broken_Hill"); static final TimeZone AUSTRALIA_DARWIN =
	 * TimeZone.getTimeZone("Australia/Darwin"); static final TimeZone AUSTRALIA_NORTH =
	 * TimeZone.getTimeZone("Australia/North"); static final TimeZone AUSTRALIA_SOUTH =
	 * TimeZone.getTimeZone("Australia/South"); static final TimeZone AUSTRALIA_YANCOWINNA =
	 * TimeZone.getTimeZone("Australia/Yancowinna"); static final TimeZone AET =
	 * TimeZone.getTimeZone("AET"); static final TimeZone ANTARCTICA_DUMONTDURVILLE =
	 * TimeZone.getTimeZone("Antarctica/DumontDUrville"); static final TimeZone ASIA_SAKHALIN =
	 * TimeZone.getTimeZone("Asia/Sakhalin"); static final TimeZone ASIA_VLADIVOSTOK =
	 * TimeZone.getTimeZone("Asia/Vladivostok"); static final TimeZone AUSTRALIA_ACT =
	 * TimeZone.getTimeZone("Australia/ACT"); static final TimeZone AUSTRALIA_BRISBANE =
	 * TimeZone.getTimeZone("Australia/Brisbane"); static final TimeZone AUSTRALIA_CANBERRA =
	 * TimeZone.getTimeZone("Australia/Canberra"); static final TimeZone AUSTRALIA_CURRIE =
	 * TimeZone.getTimeZone("Australia/Currie"); static final TimeZone AUSTRALIA_HOBART =
	 * TimeZone.getTimeZone("Australia/Hobart"); static final TimeZone AUSTRALIA_LINDEMAN =
	 * TimeZone.getTimeZone("Australia/Lindeman"); static final TimeZone AUSTRALIA_MELBOURNE =
	 * TimeZone.getTimeZone("Australia/Melbourne"); static final TimeZone AUSTRALIA_NSW =
	 * TimeZone.getTimeZone("Australia/NSW"); static final TimeZone AUSTRALIA_QUEENSLAND =
	 * TimeZone.getTimeZone("Australia/Queensland"); static final TimeZone AUSTRALIA_SYDNEY =
	 * TimeZone.getTimeZone("Australia/Sydney"); static final TimeZone AUSTRALIA_TASMANIA =
	 * TimeZone.getTimeZone("Australia/Tasmania"); static final TimeZone AUSTRALIA_VICTORIA =
	 * TimeZone.getTimeZone("Australia/Victoria"); static final TimeZone ETC_GMT_MINUS_10 =
	 * TimeZone.getTimeZone("Etc/GMT-10"); static final TimeZone PACIFIC_GUAM =
	 * TimeZone.getTimeZone("Pacific/Guam"); static final TimeZone PACIFIC_PORT_MORESBY =
	 * TimeZone.getTimeZone("Pacific/Port_Moresby"); static final TimeZone PACIFIC_SAIPAN =
	 * TimeZone.getTimeZone("Pacific/Saipan"); static final TimeZone PACIFIC_TRUK =
	 * TimeZone.getTimeZone("Pacific/Truk"); static final TimeZone PACIFIC_YAP =
	 * TimeZone.getTimeZone("Pacific/Yap"); static final TimeZone AUSTRALIA_LHI =
	 * TimeZone.getTimeZone("Australia/LHI"); static final TimeZone AUSTRALIA_LORD_HOWE =
	 * TimeZone.getTimeZone("Australia/Lord_Howe"); static final TimeZone ASIA_MAGADAN =
	 * TimeZone.getTimeZone("Asia/Magadan"); static final TimeZone ETC_GMT_MINUS_11 =
	 * TimeZone.getTimeZone("Etc/GMT-11"); static final TimeZone PACIFIC_EFATE =
	 * TimeZone.getTimeZone("Pacific/Efate"); static final TimeZone PACIFIC_GUADALCANAL =
	 * TimeZone.getTimeZone("Pacific/Guadalcanal"); static final TimeZone PACIFIC_KOSRAE =
	 * TimeZone.getTimeZone("Pacific/Kosrae"); static final TimeZone PACIFIC_NOUMEA =
	 * TimeZone.getTimeZone("Pacific/Noumea"); static final TimeZone PACIFIC_PONAPE =
	 * TimeZone.getTimeZone("Pacific/Ponape"); static final TimeZone SST = TimeZone.getTimeZone("SST");
	 * static final TimeZone PACIFIC_NORFOLK = TimeZone.getTimeZone("Pacific/Norfolk"); static final
	 * TimeZone ANTARCTICA_MCMURDO = TimeZone.getTimeZone("Antarctica/McMurdo"); static final TimeZone
	 * ANTARCTICA_SOUTH_POLE = TimeZone.getTimeZone("Antarctica/South_Pole"); static final TimeZone
	 * ASIA_ANADYR = TimeZone.getTimeZone("Asia/Anadyr"); static final TimeZone ASIA_KAMCHATKA =
	 * TimeZone.getTimeZone("Asia/Kamchatka"); static final TimeZone ETC_GMT_MINUS_12 =
	 * TimeZone.getTimeZone("Etc/GMT-12"); static final TimeZone KWAJALEIN =
	 * TimeZone.getTimeZone("Kwajalein"); static final TimeZone NST = TimeZone.getTimeZone("NST");
	 * static final TimeZone NZ = TimeZone.getTimeZone("NZ"); static final TimeZone PACIFIC_AUCKLAND =
	 * TimeZone.getTimeZone("Pacific/Auckland"); static final TimeZone PACIFIC_FIJI =
	 * TimeZone.getTimeZone("Pacific/Fiji"); static final TimeZone PACIFIC_FUNAFUTI =
	 * TimeZone.getTimeZone("Pacific/Funafuti"); static final TimeZone PACIFIC_KWAJALEIN =
	 * TimeZone.getTimeZone("Pacific/Kwajalein"); static final TimeZone PACIFIC_MAJURO =
	 * TimeZone.getTimeZone("Pacific/Majuro"); static final TimeZone PACIFIC_NAURU =
	 * TimeZone.getTimeZone("Pacific/Nauru"); static final TimeZone PACIFIC_TARAWA =
	 * TimeZone.getTimeZone("Pacific/Tarawa"); static final TimeZone PACIFIC_WAKE =
	 * TimeZone.getTimeZone("Pacific/Wake"); static final TimeZone PACIFIC_WALLIS =
	 * TimeZone.getTimeZone("Pacific/Wallis"); static final TimeZone NZ_CHAT =
	 * TimeZone.getTimeZone("NZ-CHAT"); static final TimeZone PACIFIC_CHATHAM =
	 * TimeZone.getTimeZone("Pacific/Chatham"); static final TimeZone ETC_GMT_MINUS_13 =
	 * TimeZone.getTimeZone("Etc/GMT-13"); static final TimeZone PACIFIC_ENDERBURY =
	 * TimeZone.getTimeZone("Pacific/Enderbury"); static final TimeZone PACIFIC_TONGATAPU =
	 * TimeZone.getTimeZone("Pacific/Tongatapu"); static final TimeZone ETC_GMT_MINUS_14 =
	 * TimeZone.getTimeZone("Etc/GMT-14"); static final TimeZone PACIFIC_KIRITIMATI =
	 * TimeZone.getTimeZone("Pacific/Kiritimati"); static final TimeZone AMERICA_INDIANA_VINCENNES =
	 * TimeZone.getTimeZone("America/Indiana/Vincennes"); static final TimeZone
	 * AMERICA_INDIANA_PETERSBURG = TimeZone.getTimeZone("America/Indiana/Petersburg");
	 */
}