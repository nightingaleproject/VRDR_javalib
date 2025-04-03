package edu.gatech.chai.VRDR.model.util;

import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;

import edu.gatech.chai.VRDR.model.valueset.MannerOfDeathVS;

public class MannerOfDeathUtil {
	public static final ObservationStatus status = ObservationStatus.FINAL;
	public static final CodeableConcept code = new CodeableConcept()
			.addCoding(new Coding(CommonUtil.loincSystemUrl, "69449-7", "Manner of death"));
	public static final CodeableConcept VALUE_NATURAL = new CodeableConcept()
			.addCoding(new Coding(CommonUtil.snomedSystemUrl, "38605008", "Natural death"));

	public static Set<CodeableConcept> valueCodesetList = MannerOfDeathVS.valueSet;
}