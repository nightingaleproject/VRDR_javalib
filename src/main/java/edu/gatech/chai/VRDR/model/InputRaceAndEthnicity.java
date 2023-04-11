package edu.gatech.chai.VRDR.model;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.model.util.CommonUtil;
import edu.gatech.chai.VRDR.model.util.InputRaceAndEthnicityUtil;

@ResourceDef(name = "Observation", profile = "http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-input-race-and-ethnicity")
public class InputRaceAndEthnicity extends Observation {
	public InputRaceAndEthnicity() {
		super();
		CommonUtil.initResource(this);
		this.setCode(InputRaceAndEthnicityUtil.code);
	}
	
	public InputRaceAndEthnicity(String... racesAndEthnicities) {
		for(String raceOrEth:racesAndEthnicities) {
			if(InputRaceAndEthnicityUtil.raceBooleanSystemStrings.contains(raceOrEth)) {
				addRaceBooleanComponent(raceOrEth);
			}
			else if(InputRaceAndEthnicityUtil.raceLiteralSystemStrings.contains(raceOrEth)) {
				addRaceLiteralComponent(raceOrEth, (String)null);
			}
			else if(InputRaceAndEthnicityUtil.hispanicCodedSystemStrings.contains(raceOrEth)) {
				addHispanicBooleanComponent(raceOrEth);
			}
		}
	}

	public void addRaceBooleanComponent(String codeName) {
		addComponent(codeName, new BooleanType(true));
	}
	public void addRaceBooleanComponent(String codeName, boolean value) {
		addComponent(codeName, new BooleanType(value));
	}
	
	public void addRaceBooleanComponent(String codeName, BooleanType value) {
		addComponent(codeName, value);
	}

	public void addRaceLiteralComponent(String codeName, String value) {
		addComponent(codeName, new StringType(value));
	}

	public void addRaceLiteralComponent(String codeName, StringType value) {
		addComponent(codeName, value);
	}

	public void addHispanicBooleanComponent(String codeName) {
		addComponent(codeName, CommonUtil.findConceptFromCollectionUsingSimpleString("unknown", CommonUtil.yesNoUnknownSet));
	}

	public void addHispanicBooleanComponent(String codeName, boolean value) {
		addComponent(codeName, CommonUtil.findConceptFromCollectionUsingSimpleString(value ? "Yes" : "No", CommonUtil.yesNoUnknownSet));
	}

	public void addHispanicBooleanComponentUnknown(String codeName) {
		addComponent(codeName, CommonUtil.findConceptFromCollectionUsingSimpleString("unknown", CommonUtil.yesNoUnknownSet));
	}
	
	public void addHispanicBooleanComponent(String codeName, CodeableConcept value) {
		addComponent(codeName, value);
	}
	
	public void addMissingRaceValueReason(String value) {
		CodeableConcept mrvrCodeableConcept = CommonUtil.findConceptFromCollectionUsingSimpleString(value, InputRaceAndEthnicityUtil.raceMissingValueReasonList);
		addComponent("RACEMVR",mrvrCodeableConcept);
	}
	
	public void addComponent(String codeName, BooleanType value) {
		ObservationComponentComponent occ = addComponentCommon(codeName);
		occ.setValue(value);
		this.addComponent(occ);
	}
	
	public void addComponent(String codeName, CodeableConcept value) {
		ObservationComponentComponent occ = addComponentCommon(codeName);
		occ.setValue(value);
		this.addComponent(occ);
	}
	
	public void addComponent(String codeName, StringType value) {
		ObservationComponentComponent occ = addComponentCommon(codeName);
		occ.setValue(value);
		this.addComponent(occ);
	}
	
	private ObservationComponentComponent addComponentCommon(String codeName) {
		ObservationComponentComponent occ = new ObservationComponentComponent();
		occ.setCode(new CodeableConcept().addCoding(new Coding(InputRaceAndEthnicityUtil.codeAndComponentSystemUrl,codeName,"")));
		addComponent(occ);
		return occ;
	}

	public String getValueCodeableConceptCodeForCoding(CodeableConcept codeableConcept) {
		return CommonUtil.findObservationComponentComponentValueCodeableConceptCodeForCoding(getComponent(),
				codeableConcept.getCodingFirstRep());
	}

}
