package edu.gatech.chai.VRDR.model.util;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;

import edu.gatech.chai.VRDR.model.valueset.ContributoryTobaccoUseVS;

public class TobaccoUseContributedToDeathUtil {
	public static final ObservationStatus status = ObservationStatus.FINAL;
	public static final CodeableConcept code = new CodeableConcept()
			.addCoding(new Coding().setSystem(CommonUtil.loincSystemUrl).setCode("69443-0")
					.setDisplay("Did tobacco use contribute to death"));
	public static final String codeValueSystem = "urn:oid:2.16.840.1.114222.4.5.274";

	public static List<CodeableConcept> valueCodesetList = new ArrayList<>(ContributoryTobaccoUseVS.valueSet);
}