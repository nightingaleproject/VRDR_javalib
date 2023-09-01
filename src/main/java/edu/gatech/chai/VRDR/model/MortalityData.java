package edu.gatech.chai.VRDR.model;


import java.util.HashMap;
import java.util.Map;

/// <summary>Data helper class for dealing with IJE mortality data. Follows Singleton-esque pattern!</summary>
public class MortalityData {
    private MortalityData() {
    }

    /// <summary>Instance get method for singleton.</summary>
    public Nested getInstance() {
        return new Nested(); //MortalityData.
    }

    private class Nested {
//        static Nested() { }
//        internal static readonly MortalityData instance = new MortalityData();
    }

    // /// <summary>Given a State code, return a random PlaceCode.</summary>
    // public PlaceCode StateCodeToRandomPlace(String state)
    // {
    //     Random random = new Random();
    //     List<PlaceCode> places =
    //         PlaceCodes.Where(t => LinqHelper.EqualsInsensitive(t.State, state)).ToList();
    //     return places[random.Next(places.Count)];
    // }

    /// <summary>Given a State, Territory, or Province name - return the representative State code.</summary>
    public String StateNameToStateCode(String state) {
        if (StateTerritoryProvinceCodes.values().contains(state)) {
            // Passed a code so just return it
            return state;
        } else {
            // Passed a name so look up code
            return MapValueFinderHelper(StateTerritoryProvinceCodes, state);
        }
    }

    /// <summary>Given a Jurisdiction code - return the Jurisdiction name.</summary>
    public String JurisdictionNameToJurisdictionCode(String jurisdiction) {

        return MapValueFinderHelper(JurisdictionCodes, jurisdiction);
    }

    /// <summary>Given a Jurisdiction name - return the representative Jurisdiction code.</summary>
    public String JurisdictionCodeToJurisdictionName(String code) {
        return MapKeyFinderHelper(JurisdictionCodes, code);
    }

    /// <summary>Given a State, Territory, or Province code - return the representative State, Territory, or Province name.</summary>
    public String StateCodeToStateName(String code) {
        return MapKeyFinderHelper(StateTerritoryProvinceCodes, code);
    }

    /// <summary>Given a Country name - return the representative Country code.</summary>
    public String CountryNameToCountryCode(String country) {
        if (MapKeyFinderHelper(CountryCodes, country) != null) {
            // Passed a code so just return it
            return country;
        } else {
            // Passed a name so look up code
            return MapValueFinderHelper(CountryCodes, country);
        }
    }

    /// <summary>Given a Country code - return the representative Country name.</summary>
    public String CountryCodeToCountryName(String code) {
        return MapKeyFinderHelper(CountryCodes, code);
    }

    // Note: did not delete this, because it is referenced by a line in VRDR.HTTP/Nightingale.cs that I commented out.
    // /// <summary>Given a Race name - return the representative Race code.</summary>
    // public String RaceNameToRaceCode(String name)
    // {
    //     return WRaceNameToRaceCode(name) ?? BAARaceNameToRaceCode(name) ?? ARaceNameToRaceCode(name) ?? AIANRaceNameToRaceCode(name) ?? NHOPIRaceNameToRaceCode(name);
    // }

    // /// <summary>Given a Race code - return the representative Race name.</summary>
    // public String RaceCodeToRaceName(String code)
    // {
    //     return WRaceCodeToRaceName(code) ?? BAARaceCodeToRaceName(code) ?? ARaceCodeToRaceName(code) ?? AIANRaceCodeToRaceName(code) ?? NHOPIRaceCodeToRaceName(code);
    // }

    /// <summary>Given a value in a &lt;String, String&gt; object, return the first matching key.</summary>
    // public String ABC<T>(T obj) where T : IDestination
    private static String MapKeyFinderHelper<T>(
    T map, String
    value)
    where T :IEnumerable<Map<String, String>>

    {
        if (IsNullOrWhiteSpace(value)) {
            return null;
        }
        return map.get(0) (
            t = > LinqHelper.EqualsInsensitive(t.Value, value)
            ).get(Key);
    }

    /// <summary>Given a key in a (String, String) object, return the first matching value.</summary>
    private static String MapValueFinderHelper<T>(T map, String key) where T :IEnumerable<Map<String, String>>
    {
        if (isNullOrWhiteSpace(key)) {
            return null;
        }
        return map.FirstOrDefault(
                t = > LinqHelper.EqualsInsensitive(t.Key, key)
            ).Value;
    }

    /// <summary>Jurisdiction Codes</summary>
    // JurisdictionCodes uses IJE-defined two-character String as key, and provides the jurisdiction's name.
    // all codes are USPS Postal codes except for YC
    public Map<String, String> JurisdictionCodes = new HashMap<String, String>() {{
        put("AL", "Alabama");
        put("AK", "Alaska");
        put("AS", "American Samoa");
        put("AZ", "Arizona");
        put("AR", "Arkansas");
        put("CA", "California");
        put("CO", "Colorado");
        put("CT", "Connecticut");
        put("DE", "Delaware");
        put("DC", "District of Columbia");
        put("FL", "Florida");
        put("GA", "Georgia");
        put("GU", "Guam");
        put("HI", "Hawaii");
        put("ID", "Idaho");
        put("IL", "Illinois");
        put("IN", "Indiana");
        put("IA", "Iowa");
        put("KS", "Kansas");
        put("KY", "Kentucky");
        put("LA", "Louisiana");
        put("ME", "Maine");
        put("MD", "Maryland");
        put("MA", "Massachusetts");
        put("MI", "Michigan");
        put("MN", "Minnesota");
        put("MS", "Mississippi");
        put("MO", "Missouri");
        put("MT", "Montana");
        put("NE", "Nebraska");
        put("NV", "Nevada");
        put("NH", "New Hampshire");
        put("NJ", "New Jersey");
        put("NM", "New Mexico");
        put("NY", "New York");
        put("NC", "North Carolina");
        put("ND", "North Dakota");
        put("MP", "Northern Mariana Islands");
        put("OH", "Ohio");
        put("OK", "Oklahoma");
        put("OR", "Oregon");
        put("PA", "Pennsylvania");
        put("PR", "Puerto Rico");
        put("RI", "Rhode Island");
        put("SC", "South Carolina");
        put("SD", "South Dakota");
        put("TN", "Tennessee");
        put("TX", "Texas");
        put("UT", "Utah");
        put("VT", "Vermont");
        put("VI", "Virgin Islands");
        put("VA", "Virginia");
        put("WA", "Washington");
        put("WV", "West Virginia");
        put("WI", "Wisconsin");
        put("WY", "Wyoming");
        put("YC", "New York City");
        put("TT", "Test Jurisdiction"); // This should only be used for testing with NCHS.
        put("TS", "STEVE Test Jurisdiction"); // This should only be used for STEVE testing with NCHS.
    }};

    /// <summary>State and Territory Province Codes</summary>
    public Map<String, String> StateTerritoryProvinceCodes = new HashMap<String, String>()
        {{
            put("Alabama", "AL");
            put("Alaska", "AK");
            put("Arizona", "AZ");
            put("Arkansas", "AR");
            put("California", "CA");
            put("Colorado", "CO");
            put("Connecticut", "CT");
            put("Delaware", "DE");
            put("Florida", "FL");
            put("Georgia", "GA");
            put("Hawaii", "HI");
            put("Idaho", "ID");
            put("Illinois", "IL");
            put("Indiana", "IN");
            put("Iowa", "IA");
            put("Kansas", "KS");
            put("Kentucky", "KY");
            put("Louisiana", "LA");
            put("Maine", "ME");
            put("Maryland", "MD");
            put("Massachusetts", "MA");
            put("Michigan", "MI");
            put("Minnesota", "MN");
            put("Mississippi", "MS");
            put("Missouri", "MO");
            put("Montana", "MT");
            put("Nebraska", "NE");
            put("Nevada", "NV");
            put("New Hampshire", "NH");
            put("New Jersey", "NJ");
            put("New Mexico", "NM");
            put("New York", "NY");
            put("North Carolina", "NC");
            put("North Dakota", "ND");
            put("Ohio", "OH");
            put("Oklahoma", "OK");
            put("Oregon", "OR");
            put("Pennsylvania", "PA");
            put("Rhode Island", "RI");
            put("South Carolina", "SC");
            put("South Dakota", "SD");
            put("Tennessee", "TN");
            put("Texas", "TX");
            put("Utah", "UT");
            put("Vermont", "VT");
            put("Virginia", "VA");
            put("Washington", "WA");
            put("West Virginia", "WV");
            put("Wisconsin", "WI");
            put("Wyoming", "WY");
            put("Northern Marianas", "MP");
            put("American Samoa", "AS");
            put("Guam", "GU");
            put("Virgin Islands", "VI");
            put("Puerto Rico", "PR");
            put("Alberta", "AB");
            put("British Columbia", "BC");
            put("Manitoba", "MB");
            put("New Brunswick", "NB");
            put("Newfoundland", "NF");
            put("Nova Scotia", "NS");
            put("Northwest Territories", "NT");
            put("Nunavut", "NU");
            put("Ontario", "ON");
            put("Prince Edward Island", "PE");
            put("Quebec", "QC");
            put("Saskatchewan", "SK");
            put("Yukon", "YK");
        }};

        /// <summary>Country Codes based on PH_Country_GEC = 2.16.840.1.113883.13.250    </summary>
        public Map<String, String> CountryCodes = new HashMap<String, String>()
        {{
            put("Aruba", "AA");
            put("Antigua And Barbuda", "AC");
            put("United Arab Emirates", "AE");
            put("﻿afghanistan", "AF");
            put("Algeria", "AG");
            put("Azerbaijan", "AJ");
            put("Albania", "AL");
            put("Armenia", "AM");
            put("Andorra", "AN");
            put("Angola", "AO");
            put("Argentina", "AR");
            put("Australia", "AS");
            put("Ashmore And Cartier Islands", "AT");
            put("Austria", "AU");
            put("Anguilla", "AV");
            put("Akrotiri", "AX");
            put("Antarctica", "AY");
            put("Bahrain", "BA");
            put("Barbados", "BB");
            put("Botswana", "BC");
            put("Bermuda", "BD");
            put("Belgium", "BE");
            put("Bahamas, The", "BF");
            put("Bangladesh", "BG");
            put("Belize", "BH");
            put("Bosnia And Herzegovina", "BK");
            put("Bolivia", "BL");
            put("Burma, Myanmar", "BM");
            put("Benin", "BN");
            put("Belarus", "BO");
            put("Solomon Islands", "BP");
            put("Brazil", "BR");
            put("Bassas Da India", "BS");
            put("Bhutan", "BT");
            put("Bulgaria", "BU");
            put("Bouvet Island", "BV");
            put("Brunei", "BX");
            put("Burundi", "BY");
            put("Canada", "CA");
            put("Cambodia", "CB");
            put("Chad", "CD");
            put("Sri Lanka", "CE");
            put("Congo (brazzaville); Republic Of The Congo", "CF");
            put("Congo (kinshasa); Democratic Republic Of The Congo, Zaire", "CG");
            put("China", "CH");
            put("Chile", "CI");
            put("Cayman Islands", "CJ");
            put("Cocos (keeling) Islands", "CK");
            put("Central And Southern Line Islands", "CL");
            put("Cameroon", "CM");
            put("Comoros", "CN");
            put("Colombia", "CO");
            put("Coral Sea Islands", "CR");
            put("Costa Rica", "CS");
            put("Central African Republic", "CT");
            put("Cuba", "CU");
            put("Cape Verde", "CV");
            put("Cook Islands", "CW");
            put("Cyprus", "CY");
            put("Czechoslovakia", "CZ");
            put("Denmark", "DA");
            put("Djibouti", "DJ");
            put("Dahomey", "DM");
            put("Dominica", "DO");
            put("Jarvis Island", "DQ");
            put("Dominican Republic", "DR");
            put("Dhekelia", "DX");
            put("East Berlin", "EB");
            put("Ecuador", "EC");
            put("Egypt", "EG");
            put("Ireland", "EI");
            put("Equatorial Guinea", "EK");
            put("Estonia", "EN");
            put("Canton And Enderberry Islands", "EQ");
            put("Eritrea", "ER");
            put("El Salvador", "ES");
            put("Ethiopia", "ET");
            put("Europa Island", "EU");
            put("Czech Republic", "EZ");
            put("French Guiana", "FG");
            put("Åland, Finland", "FI");
            put("Fiji", "FJ");
            put("Falkland Islands (islas Malvinas); Islas Malvinas", "FK");
            put("Federated States Of Micronesia, Micronesia,federated States Of", "FM");
            put("Faroe Islands", "FO");
            put("French Polynesia, Tahiti", "FP");
            put("France", "FR");
            put("French Southern And Antarctic Lands", "FS");
            put("French Territory Of The Affars And Issas", "FT");
            put("Gambia,the", "GA");
            put("Gabon", "GB");
            put("East Germany, German Democratic Republic", "GC");
            put("Federal Republic Of Germany, West Germany", "GE");
            put("Georgia", "GG");
            put("Ghana", "GH");
            put("Gibraltar", "GI");
            put("Grenada", "GJ");
            put("Guernsey", "GK");
            put("Greenland", "GL");
            put("Germany", "GM");
            put("Gilbert And Ellice Islands", "GN");
            put("Glorioso Islands", "GO");
            put("Guadeloupe", "GP");
            put("Greece", "GR");
            put("Gilbert Islands", "GS");
            put("Guatemala", "GT");
            put("Guinea", "GV");
            put("Guyana", "GY");
            put("Gaza Strip", "GZ");
            put("Haiti", "HA");
            put("Hong Kong", "HK");
            put("Heard Island And Mcdonald Islands", "HM");
            put("Honduras", "HO");
            put("Howland Island", "HQ");
            put("Croatia", "HR");
            put("Hungary", "HU");
            put("Iceland", "IC");
            put("Indonesia", "ID");
            put("Isle Of Man", "IM");
            put("India", "IN");
            put("British Indian Ocean Territory", "IO");
            put("Clipperton Island", "IP");
            put("Us Miscellaneous Pacific Islands", "IQ");
            put("Iran", "IR");
            put("Israel", "IS");
            put("Italy", "IT");
            put("Israel-syria Demilitarized Zone", "IU");
            put("Côte D’ivoire, Ivory Coast", "IV");
            put("Israel-jordan Demilitarized Zone", "IW");
            put("Iraq-saudi Arabia Neutral Zone", "IY");
            put("Iraq", "IZ");
            put("Japan", "JA");
            put("Jersey", "JE");
            put("Jamaica", "JM");
            put("Jan Mayen", "JN");
            put("Jordan", "JO");
            put("Johnston Atoll", "JQ");
            put("Svalbard And Jan Mayen", "JS");
            put("Juan De Nova Island", "JU");
            put("Kenya", "KE");
            put("Kyrgyzstan", "KG");
            put("Korea,north, North Korea", "KN");
            put("Kiribati", "KR");
            put("Korea,south, South Korea", "KS");
            put("Christmas Island", "KT");
            put("Kuwait", "KU");
            put("Kosovo", "KV");
            put("Kazakhstan", "KZ");
            put("Laos", "LA");
            put("Lebanon", "LE");
            put("Latvia", "LG");
            put("Lithuania", "LH");
            put("Liberia", "LI");
            put("Slovakia", "LO");
            put("Palmyra Atoll", "LQ");
            put("Liechtenstein", "LS");
            put("Lesotho", "LT");
            put("Luxembourg", "LU");
            put("Libya", "LY");
            put("Madagascar", "MA");
            put("Martinique", "MB");
            put("Macau", "MC");
            put("Moldova", "MD");
            put("Mayotte", "MF");
            put("Mongolia", "MG");
            put("Montserrat", "MH");
            put("Malawi", "MI");
            put("Montenegro", "MJ");
            put("Macedonia", "MK");
            put("Mali", "ML");
            put("Monaco", "MN");
            put("Morocco", "MO");
            put("Mauritius", "MP");
            put("Midway Islands", "MQ");
            put("Mauritania", "MR");
            put("Malta", "MT");
            put("Oman", "MU");
            put("Maldives", "MV");
            put("Mexico", "MX");
            put("Malaysia", "MY");
            put("Mozambique", "MZ");
            put("New Caledonia", "NC");
            put("Niue", "NE");
            put("Norfolk Island", "NF");
            put("Niger", "NG");
            put("New Hebrides, Vanuatu", "NH");
            put("Nigeria", "NI");
            put("Bonaire, Netherlands, Saba, Saint Eustatius", "NL");
            put("Sint Maarten", "NN");
            put("Norway", "NO");
            put("Nepal", "NP");
            put("Nauru", "NR");
            put("Suriname", "NS");
            put("Netherlands Antilles", "NT");
            put("Nicaragua", "NU");
            put("New Zealand", "NZ");
            put("South Sudan", "OD");
            put("Paraguay", "PA");
            put("Pitcairn Island", "PC");
            put("Peru", "PE");
            put("Paracel Islands", "PF");
            put("Spratly Islands", "PG");
            put("Etorofu, Habomai,kunashiri,and Shikotan Islands", "PJ");
            put("Pakistan", "PK");
            put("Poland", "PL");
            put("Panama", "PM");
            put("Panama", "PN");
            put("Azores, Portugal", "PO");
            put("Papua New Guinea", "PP");
            put("Panama Canal Zone", "PQ");
            put("Palau", "PS");
            put("Guinea-bissau", "PU");
            put("Qatar", "QA");
            put("Reunion", "RE");
            put("Rhodesia, Southern Rhodesia", "RH");
            put("Serbia", "RI");
            put("Marshall Islands", "RM");
            put("Saint Martin", "RN");
            put("Romania", "RO");
            put("Philippines", "RP");
            put("Russia", "RS");
            put("Rwanda", "RW");
            put("Saudi Arabia", "SA");
            put("Saint Pierre And Miquelon", "SB");
            put("Nevis, Saint Kitts And Nevis", "SC");
            put("Seychelles", "SE");
            put("South Africa", "SF");
            put("Senegal", "SG");
            put("Saint Helena, Ascension And Tristan Da Cunha", "SH");
            put("Slovenia", "SI");
            put("Sikkim", "SK");
            put("Sierra Leone", "SL");
            put("San Marino", "SM");
            put("Singapore", "SN");
            put("Somalia", "SO");
            put("Spain", "SP");
            put("Swan Islands", "SQ");
            put("Spanish Sahara", "SS");
            put("Saint Lucia", "ST");
            put("Sudan", "SU");
            put("Svalbard", "SV");
            put("Sweden", "SW");
            put("South Georgia And South Sandwich Islands", "SX");
            put("Syria", "SY");
            put("Switzerland", "SZ");
            put("Saint Barthélemy", "TB");
            put("United Arab Emirates", "TC");
            put("Trinidad And Tobago", "TD");
            put("Tromelin Island", "TE");
            put("Thailand", "TH");
            put("Tajikistan", "TI");
            put("Turks And Caicos Islands", "TK");
            put("Tokelau", "TL");
            put("Tonga", "TN");
            put("Togo", "TO");
            put("Sao Tome And Principe", "TP");
            put("Trust Territory Of The Pacific Islands", "TQ");
            put("Tunisia", "TS");
            put("East Timor, Timor-leste", "TT");
            put("Turkey", "TU");
            put("Tuvalu", "TV");
            put("Taiwan", "TW");
            put("Turkmenistan", "TX");
            put("Tanzania", "TZ");
            put("Curaçao", "UC");
            put("Uganda", "UG");
            put("England, Great Britain, United Kingdom", "UK");
            put("Ukraine", "UP");
            put("Soviet Union, Union Of Soviet Socialist Republics", "UR");
            put("United States", "US");
            put("Burkina Faso, Upper Volta", "UV");
            put("Uruguay", "UY");
            put("Uzbekistan", "UZ");
            put("Saint Vincent And The Grenadines", "VC");
            put("Venezuela", "VE");
            put("British Virgin Islands, Virgin Islands,british", "VI");
            put("Vietnam", "VM");
            put("North Vietnam", "VN");
            put("South Vietnam", "VS");
            put("Holy See, Vatican City", "VT");
            put("Namibia", "WA");
            put("West Berlin", "WB");
            put("West Bank", "WE");
            put("Wallis And Futuna", "WF");
            put("Western Sahara", "WI");
            put("Wake Island", "WQ");
            put("Samoa", "WS");
            put("Swaziland", "WZ");
            put("Yemen (sana'a)", "YE");
            put("Serbia And Montenegro, Yugoslavia", "YI");
            put("Yemen", "YM");
            put("Ryukyu Islands,southern", "YQ");
            put("Yemen (aden)", "YS");
            put("Zambia", "ZA");
            put("Zimbabwe", "ZI");
            put("Not Classifiable", "ZZ");
        }};

/// <summary>Internal Helper class which provides Trimming and Case-Insensitive comparison of LINQ Queries.</summary>

        static class LinqHelper {
            /// <summary>Adds a extension to handle case insensitive comparisons, always Trims second parameter.</summary>
            public static boolean EqualsInsensitive(this String str, String value) =>
                    String.Equals(str,value.trim(),StringComparison.OrdinalIgnoreCase);

            str.equalsIgnoringCase(value);

        }

}


