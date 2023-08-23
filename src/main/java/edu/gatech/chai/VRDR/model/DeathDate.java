package edu.gatech.chai.VRDR.model;

import java.util.Date;

import org.hl7.fhir.r4.model.*;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.model.util.CommonUtil;
import edu.gatech.chai.VRDR.model.util.DeathDateUtil;

@ResourceDef(name = "Observation", profile = "http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-death-date")
public class DeathDate extends Observation implements PartialDateTimable {

	/**
	 *
	 */
	private static final long serialVersionUID = 493914021771207783L;

	public DeathDate() {
		super();
		CommonUtil.initResource(this);
		setCode(DeathDateUtil.code);
		setStatus(DeathDateUtil.status);
	}

	public DeathDate(Date effectiveDateTime,Date datePronouncedDead, String placeOfDeath) {
		this();
		setEffective(new DateTimeType(effectiveDateTime));
		addDatePronouncedDead(new DateTimeType(datePronouncedDead));
		addPlaceOfDeathComponent(placeOfDeath);
	}

	public void addMethod(String method) {
		CodeableConcept ccMethod = CommonUtil.findConceptFromCollectionUsingSimpleString(method, DeathDateUtil.deathDeterminationMethodSet);
		setMethod(ccMethod);
	}

	public void addDatePronouncedDead(DateTimeType dtType) {
		ObservationComponentComponent component = new ObservationComponentComponent();
		component.setCode(DeathDateUtil.componentDatePronouncedDeadCode);
		component.setValue(dtType);
		addComponent(component);
	}

	public void addPlaceOfDeathComponent(String placeOfDeath) {
		ObservationComponentComponent component = new ObservationComponentComponent();
		component.setCode(DeathDateUtil.componentPlaceOfDeathCode);
		CodeableConcept ccPoD = CommonUtil.findConceptFromCollectionUsingSimpleString(placeOfDeath, DeathDateUtil.placeOfDeathTypeSet);
		component.setCode(ccPoD);
		addComponent(component);
	}
}
