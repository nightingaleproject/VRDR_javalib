package edu.gatech.chai.VRDR.model.util;

import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;

import edu.gatech.chai.VRDR.model.valueset.EducationLevelVS;

public class DecedentEducationLevelUtil {
	public static final ObservationStatus status = ObservationStatus.FINAL;
	public static final CodeableConcept code = new CodeableConcept()
			.addCoding(new Coding(CommonUtil.loincSystemUrl, "80913-7",
					"Highest level of education [US Standard Certificate of Death]s"));

	public static final HashSet<CodeableConcept> valueSet = EducationLevelVS.valueSet;
}
