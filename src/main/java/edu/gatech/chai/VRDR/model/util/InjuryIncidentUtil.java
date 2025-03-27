package edu.gatech.chai.VRDR.model.util;

import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;

import edu.gatech.chai.VRDR.model.valueset.PlaceOfInjuryVS;
import edu.gatech.chai.VRDR.model.valueset.TransportationIncidentRoleVS;

public class InjuryIncidentUtil {
	public static final ObservationStatus status = ObservationStatus.FINAL;
	public static final CodeableConcept code = new CodeableConcept()
			.addCoding(new Coding(CommonUtil.loincSystemUrl, "11374-6", "Injury incident description"));
	public static final CodeableConcept componentPlaceofInjuryCode = new CodeableConcept()
			.addCoding(new Coding(CommonUtil.loincSystemUrl, "69450-5", "Place of injury Facility"));
	public static final CodeableConcept componentInjuryAtWorkCode = new CodeableConcept()
			.addCoding(new Coding(CommonUtil.loincSystemUrl, "69444-8", "Did death result from injury at work"));
	public static final CodeableConcept componentTransportationEventIndicator = new CodeableConcept()
			.addCoding(new Coding(CommonUtil.loincSystemUrl, "69448-9",
					"Injury leading to death associated with transportation event"));
	public static final String componentPlaceOfInjuryValueSystem = "urn:oid:2.16.840.1.114222.4.5.320";

	public static Set<CodeableConcept> placeOfInjuryValueSet = PlaceOfInjuryVS.valueSet;

	public static Set<CodeableConcept> transportationRoleValueSet = TransportationIncidentRoleVS.valueSet;
}