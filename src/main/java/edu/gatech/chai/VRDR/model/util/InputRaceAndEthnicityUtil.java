package edu.gatech.chai.VRDR.model.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

import edu.gatech.chai.VRDR.model.valueset.RaceMissingValueReasonVS;

public class InputRaceAndEthnicityUtil {

	public static final String codeSystemUrl = "http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-observations-cs";
	public static final String componentSystemUrl = "http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-component-cs";

	public static final CodeableConcept code = new CodeableConcept().addCoding(new Coding(codeSystemUrl,
			"inputraceandethnicity", "Race and Ethnicity Data submitted by Jurisdictions to NCHS"));

	public static final String hispanicCodingSystemUrl = "http://hl7.org/CodeSystem/v2-0136";

	public static Set<String> raceBooleanSystemStrings = new HashSet<String>(DecedentUtil.raceBooleanNVSSSet);
	public static Set<String> raceLiteralSystemStrings = new HashSet<String>(DecedentUtil.raceLiteralNVSSSet);
	public static Set<String> hispanicCodedSystemStrings = new HashSet<String>(DecedentUtil.hispanicCodedNVSSSet);
	public static Set<CodeableConcept> raceMissingValueReasonList = RaceMissingValueReasonVS.valueSet;

}