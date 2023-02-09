package edu.gatech.chai.VRDR.model;

import edu.gatech.chai.VRDR.model.util.EntityAxisCauseOfDeathUtil;
import org.hl7.fhir.r4.model.*;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.model.util.CodedRaceAndEthnicityUtil;
import edu.gatech.chai.VRDR.model.util.CommonUtil;

@ResourceDef(name = "Observation", profile = "http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-coded-race-and-ethnicity")
public class CodedRaceAndEthnicity extends Observation {

	private static final long serialVersionUID = -3974516224741393275L;

	public CodedRaceAndEthnicity() {
		super();
		CommonUtil.initResource(this);
		setCode(CodedRaceAndEthnicityUtil.code);
	}
	
	public CodedRaceAndEthnicity(CodeableConcept value) {
		this();
		setValue(value);
	}
	
	public void addComponent(CodeableConcept code, CodeableConcept value) {
		ObservationComponentComponent occ = new ObservationComponentComponent();
		occ.setCode(code);
		occ.setValue(value);
		addComponent(occ);
	}
	
	public void addComponent(String code, CodeableConcept value) {
		CodeableConcept ccCode = CommonUtil.findConceptFromCollectionUsingSimpleString(code, CodedRaceAndEthnicityUtil.valueCodesetList);
		addComponent(ccCode,value);
	}

	public String getValueCodeableConceptCodeForCoding(CodeableConcept codeableConcept) {
		return CommonUtil.findObservationComponentComponentValueCodeableConceptCodeForCoding(getComponent(),
				codeableConcept.getCodingFirstRep());
	}

}