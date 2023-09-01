package edu.gatech.chai.VRDR.model;


import java.util.ArrayList;
import java.util.List;

/// <summary> String representations of IJE Race fields </summary>
public class NvssRace
{
    /// <summary> White </summary>
    public static final String White = "White";
    /// <summary> BlackOrAfricanAmerican </summary>
    public static final String BlackOrAfricanAmerican = "BlackOrAfricanAmerican";
    /// <summary> AmericanIndianOrAlaskanNative </summary>
    public static final String AmericanIndianOrAlaskanNative = "AmericanIndianOrAlaskanNative";
    /// <summary> AsianIndian </summary>
    public static final String AsianIndian = "AsianIndian";
    /// <summary> Chinese </summary>
    public static final String Chinese = "Chinese";
    /// <summary> Filipino </summary>
    public static final String Filipino = "Filipino";
    /// <summary> Japanese </summary>
    public static final String Japanese = "Japanese";
    /// <summary> Korean </summary>
    public static final String Korean = "Korean";
    /// <summary> Vietnamese </summary>
    public static final String Vietnamese = "Vietnamese";
    /// <summary> OtherAsian </summary>
    public static final String OtherAsian = "OtherAsian";
    /// <summary> NativeHawaiian </summary>
    public static final String NativeHawaiian = "NativeHawaiian";
    /// <summary> GuamanianOrChamorro </summary>
    public static final String GuamanianOrChamorro = "GuamanianOrChamorro";
    /// <summary> Samoan </summary>
    public static final String Samoan = "Samoan";
    /// <summary> OtherPacificIslander </summary>
    public static final String OtherPacificIslander = "OtherPacificIslander";
    /// <summary> OtherRace </summary>
    public static final String OtherRace = "OtherRace";
    /// <summary> FirstAmericanIndianOrAlaskanNativeLiteral </summary>
    public static final String FirstAmericanIndianOrAlaskanNativeLiteral = "FirstAmericanIndianOrAlaskanNativeLiteral";
    /// <summary> SecondAmericanIndianOrAlaskanNativeLiteral </summary>
    public static final String SecondAmericanIndianOrAlaskanNativeLiteral = "SecondAmericanIndianOrAlaskanNativeLiteral";
    /// <summary> FirstOtherAsianLiteralFirst </summary>
    public static final String FirstOtherAsianLiteral = "FirstOtherAsianLiteral";
    /// <summary> SecondOtherPacificIslanderLiteral </summary>
    public static final String SecondOtherAsianLiteral = "SecondOtherAsianLiteral";
    /// <summary> FirstOtherPacificIslanderLiteral </summary>
    public static final String FirstOtherPacificIslanderLiteral = "FirstOtherPacificIslanderLiteral";
    /// <summary> SecondOtherPacificIslanderLiteral </summary>
    public static final String SecondOtherPacificIslanderLiteral = "SecondOtherPacificIslanderLiteral";
    /// <summary> FirstOtherRaceLiteral </summary>
    public static final String FirstOtherRaceLiteral = "FirstOtherRaceLiteral";
    /// <summary> SecondOtherRaceLiteral </summary>
    public static final String SecondOtherRaceLiteral = "SecondOtherRaceLiteral";
    /// <summary> MissingValueReason </summary>
    public static final String MissingValueReason = "MissingValueReason";
    /// <summary> GetBooleanRaceCodes Returns a list of the Boolean Race Codes, Y or N values </summary>
    public static List<String> GetBooleanRaceCodes()
    {
        List<String> booleanRaceCodes = new ArrayList<String>();
        booleanRaceCodes.add(NvssRace.White);
        booleanRaceCodes.add(NvssRace.BlackOrAfricanAmerican);
        booleanRaceCodes.add(NvssRace.AmericanIndianOrAlaskanNative);
        booleanRaceCodes.add(NvssRace.AsianIndian);
        booleanRaceCodes.add(NvssRace.Chinese);
        booleanRaceCodes.add(NvssRace.Filipino);
        booleanRaceCodes.add(NvssRace.Japanese);
        booleanRaceCodes.add(NvssRace.Korean);
        booleanRaceCodes.add(NvssRace.Vietnamese);
        booleanRaceCodes.add(NvssRace.OtherAsian);
        booleanRaceCodes.add(NvssRace.NativeHawaiian);
        booleanRaceCodes.add(NvssRace.GuamanianOrChamorro);
        booleanRaceCodes.add(NvssRace.Samoan);
        booleanRaceCodes.add(NvssRace.OtherPacificIslander);
        booleanRaceCodes.add(NvssRace.OtherRace);
        return booleanRaceCodes;
    }
    /// <summary> GetDisplayValueForCode returns the display value for a race code, or the code itself if none exists</summary>
    public static String GetDisplayValueForCode(String code)
    {
        switch (code)
        {
            case BlackOrAfricanAmerican:
                return "Black Or African American";
            case AmericanIndianOrAlaskanNative:
                return "American Indian Or Alaskan Native";
            case AsianIndian:
                return "Asian Indian";
            case OtherAsian:
                return "Other Asian";
            case NativeHawaiian:
                return "Native Hawaiian";
            case GuamanianOrChamorro:
                return "Guamanian Or Chamorro";
            case OtherPacificIslander:
                return "Other Pacific Islander";
            case OtherRace:
                return "Other Race";
            default:
                return code;
        }
    }
    /// <summary> GetLiteralRaceCodes Returns a list of the literal Race Codes</summary>
    public static List<String> GetLiteralRaceCodes()
    {
        List<String> literalRaceCodes = new ArrayList<String>();
        literalRaceCodes.add(NvssRace.FirstAmericanIndianOrAlaskanNativeLiteral);
        literalRaceCodes.add(NvssRace.SecondAmericanIndianOrAlaskanNativeLiteral);
        literalRaceCodes.add(NvssRace.FirstOtherAsianLiteral);
        literalRaceCodes.add(NvssRace.SecondOtherAsianLiteral);
        literalRaceCodes.add(NvssRace.FirstOtherPacificIslanderLiteral);
        literalRaceCodes.add(NvssRace.SecondOtherPacificIslanderLiteral);
        literalRaceCodes.add(NvssRace.FirstOtherRaceLiteral);
        literalRaceCodes.add(NvssRace.SecondOtherRaceLiteral);
        return literalRaceCodes;
    }
};
/// <summary> String representations of IJE Ethnicity fields </summary>
class NvssEthnicity
{
    /// <summary> Mexican </summary>
    public static final String Mexican = "HispanicMexican";
    /// <summary> Hispanic Mexican </summary>
    public static final String MexicanDisplay = "Hispanic Mexican";
    /// <summary> Puerto Rican </summary>
    public static final String PuertoRican = "HispanicPuertoRican";
    /// <summary> Hispanic Puerto Rican </summary>
    public static final String PuertoRicanDisplay = "Hispanic Puerto Rican";
    /// <summary> Cuban </summary>
    public static final String Cuban = "HispanicCuban";
    /// <summary> Hispanic Cuban </summary>
    public static final String CubanDisplay = "Hispanic Cuban";
    /// <summary> Other </summary>
    public static final String Other = "HispanicOther";
    /// <summary> Hispanic Other </summary>
    public static final String OtherDisplay = "Hispanic Other";
    /// <summary> Literal </summary>
    public static final String Literal = "HispanicLiteral";
    /// <summary> Hispanic Literal </summary>
    public static final String LiteralDisplay = "Hispanic Literal";
}



