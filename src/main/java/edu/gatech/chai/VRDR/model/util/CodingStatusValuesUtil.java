package edu.gatech.chai.VRDR.model.util;

import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;

import edu.gatech.chai.VRDR.model.valueset.IntentionalRejectVS;
import edu.gatech.chai.VRDR.model.valueset.SystemRejectVS;
import edu.gatech.chai.VRDR.model.valueset.TransaxConversionVS;

public class CodingStatusValuesUtil {

	public static Set<CodeableConcept> intentionalRejectValueset = IntentionalRejectVS.valueSet;
	public static Set<CodeableConcept> systemRejectValueset = SystemRejectVS.valueSet;
	public static Set<CodeableConcept> transaxConversionValueset = TransaxConversionVS.valueSet;
}