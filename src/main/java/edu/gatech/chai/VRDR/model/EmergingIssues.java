  

     package edu.gatech.chai.VRDR.model;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.SampledData;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Timing;
import org.hl7.fhir.r4.model.Type;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.model.util.CommonUtil;
import edu.gatech.chai.VRDR.model.util.EmergingIssuesUtil;

@ResourceDef(name = "Observation", profile = "http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-emerging-issues")
public class EmergingIssues extends Observation {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2672514795721353562L;

	/* Even though these fields are not allowed as per VRDR IG http://hl7.org/fhir/us/vrdr/StructureDefinition-vrdr-emerging-issues.html
	 * they are not redefined due to issues this causes in some clients.
	@Child(name = "value", type = {Quantity.class, CodeableConcept.class, StringType.class, BooleanType.class, IntegerType.class, Range.class, Ratio.class, SampledData.class, TimeType.class, DateTimeType.class, Period.class}, order=2, min=0, max=0, modifier=false, summary=true)
    @Description(shortDefinition="Actual component result", formalDefinition="The information determined as a result of making the observation, if the information has a simple value." )
    protected Type value;
	
    @Child(name = "effective", type = {DateTimeType.class, Period.class, Timing.class, InstantType.class}, order=9, min=0, max=1, modifier=false, summary=true)
    @Description(shortDefinition="Clinically relevant time/time-period for observation", formalDefinition="The time or time-period the observed value is asserted as being true. For biological subjects - e.g. human patients - this is usually called the \"physiologically relevant time\". This is usually either the time of the procedure or of specimen collection, but very often the source of the date/time is not known, only the date/time itself." )
    protected Type effective;
    */
	
	public EmergingIssues() {
		super();
		CommonUtil.initResource(this);
		setCode(EmergingIssuesUtil.code);
		setStatus(EmergingIssuesUtil.status);
	}

	public EmergingIssues(Decedent decedent) {
		this();
		setSubject(new Reference(decedent));
	}
	

	public EmergingIssues addComponent(String code, String value) {
		ObservationComponentComponent component = new ObservationComponentComponent();
		CodeableConcept componentCode = new CodeableConcept();
		componentCode.addCoding(new Coding(CommonUtil.vrdrComponentCsUrl,code,""));
		component.setCode(componentCode);
		component.setValue(new StringType(value));
		this.addComponent(component);
		return this;
	}
	
	public void addEmergingIssues1_1(String value) {
		addComponent("EmergingIssue1_1", value);
	}
	
	public void addEmergingIssues1_2(String value) {
		addComponent("EmergingIssue1_2", value);
	}
	
	public void addEmergingIssues1_3(String value) {
		addComponent("EmergingIssue1_3", value);
	}
	
	public void addEmergingIssues1_4(String value) {
		addComponent("EmergingIssue1_4", value);
	}
	
	public void addEmergingIssues1_5(String value) {
		addComponent("EmergingIssue1_5", value);
	}
	
	public void addEmergingIssues1_6(String value) {
		addComponent("EmergingIssue1_6", value);
	}
	
	public void addEmergingIssues1_7(String value) {
		addComponent("EmergingIssue1_7", value);
	}
	
	public void addEmergingIssues1_8(String value) {
		addComponent("EmergingIssue1_8", value);
	}
}
