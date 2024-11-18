package edu.gatech.chai.VRDR.model.util;

import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;

import edu.gatech.chai.VRDR.model.valueset.IntentionalRejectVS;
import edu.gatech.chai.VRDR.model.valueset.SystemRejectVS;

public class CodingStatusValuesUtil {

	public static Set<CodeableConcept> intentionalRejectValueset = SystemRejectVS.valueSet;
	public static Set<CodeableConcept> systemRejectValueset = IntentionalRejectVS.valueSet;
}