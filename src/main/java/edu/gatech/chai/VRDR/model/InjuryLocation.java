package edu.gatech.chai.VRDR.model;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.model.util.CommonUtil;

@ResourceDef(name = "Location", profile = "http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-injury-location")
public class InjuryLocation extends Location {

	public InjuryLocation() {
		super();
		CommonUtil.initResource(this);
	}
	public InjuryLocation(String name, String description, CodeableConcept type, Address address,
			CodeableConcept physicalType) {
		this();
		setName(name);
		setDescription(description);
		if (this.type == null || this.type.isEmpty()) {
			addType(type);
		}
		setAddress(address);
		setPhysicalType(physicalType);
	}
}