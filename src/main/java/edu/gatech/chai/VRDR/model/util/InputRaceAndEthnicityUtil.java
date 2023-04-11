package edu.gatech.chai.VRDR.model.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class InputRaceAndEthnicityUtil {

    public static final String codeSystemUrl = "http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-observations-cs";
    public static final String componentSystemUrl = "http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-component-cs";

    public static final CodeableConcept code = new CodeableConcept().addCoding(new Coding(codeSystemUrl, "inputraceandethnicity", "Race and Ethnicity Data submitted by Jurisdictions to NCHS"));

    public static Set<String> raceSystemStrings = new HashSet<String>(Arrays.asList(
            "White",
            "BlackOrAfricanAmerican",
            "AmericanIndianOrAlaskanNative",
            "AsianIndian",
            "Chinese",
            "Filipino",
            "Japanese",
            "Korean",
            "Vietnamese",
            "OtherAsian",
            "NativeHawaiian",
            "GuamanianOrChamorro",
            "Samoan",
            "OtherPacificIslander",
            "OtherRace"));

    public static Set<String> ethnicitySystemStrings = new HashSet<String>(Arrays.asList(
            "HispanicMexican",
            "HispanicPuertoRican",
            "HispanicCuban",
            "HispanicOther"));

    public static Set<String> raceEthnicityLiteralSystemStrings = new HashSet<String>(Arrays.asList(
            "FirstAmericanIndianOrAlaskanNativeLiteral",
            "SecondAmericanIndianOrAlaskanNativeLiteral",
            "FirstOtherAsianLiteral",
            "SecondOtherAsianLiteral",
            "FirstOtherPacificIslanderLiteral",
            "SecondOtherPacificIslanderLiteral",
            "FirstOtherRaceLiteral",
            "SecondOtherRaceLiteral",
            "HispanicLiteral"));

    public static Set<CodeableConcept> raceMissingValueReasonList = new HashSet<CodeableConcept>(Arrays.asList(
            new CodeableConcept().addCoding(new Coding(CommonUtil.missingValueReasonUrl, "R", "Refused")),
            new CodeableConcept().addCoding(new Coding(CommonUtil.missingValueReasonUrl, "S", "Sought, but unknown")),
            new CodeableConcept().addCoding(new Coding(CommonUtil.missingValueReasonUrl, "C", "Not obtainable"))
    ));
}