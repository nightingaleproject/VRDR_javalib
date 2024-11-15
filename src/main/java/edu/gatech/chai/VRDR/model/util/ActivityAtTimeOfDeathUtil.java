package edu.gatech.chai.VRDR.model.util;

import java.util.HashSet;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

import edu.gatech.chai.VRDR.model.valueset.*;;

public class ActivityAtTimeOfDeathUtil {
	public static final CodeableConcept code = new CodeableConcept()
			.addCoding(new Coding(CommonUtil.loincSystemUrl, "80626-5", "Activity at time of death [CDC]"));
	public static final String valueSetSystemUrl = ActivityAtTimeOfDeathVS.url;
	public static final HashSet<CodeableConcept> valueSet = ActivityAtTimeOfDeathVS.valueSet;

}
