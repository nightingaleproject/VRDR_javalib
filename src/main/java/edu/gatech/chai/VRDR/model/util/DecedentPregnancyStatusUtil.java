package edu.gatech.chai.VRDR.model.util;

import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;

import edu.gatech.chai.VRDR.model.valueset.PregnancyStatusVS;

public class DecedentPregnancyStatusUtil {
	public static final Observation.ObservationStatus status = ObservationStatus.FINAL;
	public static final CodeableConcept code = new CodeableConcept()
			.addCoding(new Coding().setSystem(CommonUtil.loincSystemUrl).setCode("69442-2")
					.setDisplay("Timing of recent pregnancy in relation to death"));
	public static final String codeValueSystem = "http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-pregnancy-status-cs";

	public static final HashSet<CodeableConcept> valueSet = PregnancyStatusVS.valueSet;

}