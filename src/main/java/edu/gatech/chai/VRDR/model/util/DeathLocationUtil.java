package edu.gatech.chai.VRDR.model.util;

import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

import edu.gatech.chai.VRDR.model.valueset.PlaceOfDeathVS;

public class DeathLocationUtil {

	public static final HashSet<CodeableConcept> placeOfDeathTypeSet = PlaceOfDeathVS.valueSet;
	public static CodeableConcept typeCode = new CodeableConcept(
			new Coding().setSystem("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-location-type-cs").setCode("death")
					.setDisplay("death location"));
}
