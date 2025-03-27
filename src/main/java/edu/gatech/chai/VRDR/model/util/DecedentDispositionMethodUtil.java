package edu.gatech.chai.VRDR.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;

import edu.gatech.chai.VRDR.model.valueset.MethodOfDispositionVS;

public class DecedentDispositionMethodUtil {
	public static final Observation.ObservationStatus status = ObservationStatus.FINAL;
	public static final CodeableConcept code = new CodeableConcept().addCoding(
			new Coding().setSystem(CommonUtil.loincSystemUrl).setCode("80905-3").setDisplay("Body disposition method"));

	public static List<CodeableConcept> valueCodesetList = new ArrayList<CodeableConcept>(
			MethodOfDispositionVS.valueSet);
}