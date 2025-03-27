package edu.gatech.chai.VRDR.model.util;

import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;

import edu.gatech.chai.VRDR.model.valueset.TransportationIncidentRoleVS;

public class DecedentTransportationRoleUtil {
	public static final Observation.ObservationStatus status = ObservationStatus.FINAL;
	public static final CodeableConcept code = new CodeableConcept()
			.addCoding(new Coding().setSystem(CommonUtil.loincSystemUrl).setCode("69451-3")
					.setDisplay("Transportation role of decedent"));

	public static Set<CodeableConcept> valueCodesetList = TransportationIncidentRoleVS.valueSet;
}