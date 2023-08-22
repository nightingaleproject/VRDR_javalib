package edu.gatech.chai.VRDR.model;

import java.util.Date;

import org.hl7.fhir.r4.model.*;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.model.util.CommonUtil;
import edu.gatech.chai.VRDR.model.util.DeathDateUtil;

@ResourceDef(name = "Observation", profile = "http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-death-date")
public class DeathDate extends Observation {

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
	
	public DeathDate addPartialDateExtension(IntegerType year,String yearDataAbsentReason, IntegerType month,String monthDataAbsentReason,
			IntegerType day,String dayDataAbsentReason) {
		if(this.value == null) {
			this.value = new DateTimeType(); //Assuming a datetime value if uninitiated
		}
		Extension baseExtension = addPartialDateBaseExtension();
		addPartialDateYear(baseExtension,year,yearDataAbsentReason);
		addPartialDateMonth(baseExtension,month,monthDataAbsentReason);
		addPartialDateDay(baseExtension,day,dayDataAbsentReason);
		this.value.addExtension(baseExtension);
		return this;
	}

	public DeathDate addPartialDateTimeExtension(IntegerType year, String yearDataAbsentReason, IntegerType month, String monthDataAbsentReason,
												 IntegerType day, String dayDataAbsentReason, StringType time, String timeDataAbsentReason) {
		if(this.value == null) {
			this.value = new DateTimeType(); //Assuming a datetime value if uninitiated
		}
		Extension baseExtension = addPartialDateBaseExtension();
		addPartialDateYear(baseExtension,year,yearDataAbsentReason);
		addPartialDateMonth(baseExtension,month,monthDataAbsentReason);
		addPartialDateDay(baseExtension,day,dayDataAbsentReason);
		addPartialDateTime(baseExtension,time,timeDataAbsentReason);
		this.value.addExtension(baseExtension);
		return this;
	}
	
	private Extension addPartialDateBaseExtension() {
		Extension baseExtension = new Extension(CommonUtil.partialDatePartAbsentReasonURL);
		return baseExtension;
	}
	
	private DeathDate addPartialDateYear(Extension baseExtension, IntegerType year,String dataAbsentReason) {
		if(dataAbsentReason != null && !dataAbsentReason.isEmpty()) {
			baseExtension.addExtension(new Extension(CommonUtil.partialDateDateYearAbsentReasonURL,CommonUtil.findCodeFromCollectionUsingSimpleString(dataAbsentReason, CommonUtil.dataAbsentReasonCodeSet)));
		}
		else if(year != null && !year.isEmpty()){
			baseExtension.addExtension(new Extension(CommonUtil.partialDateDateYearURL,year));
		}
		return this;
	}
	
	private DeathDate addPartialDateMonth(Extension baseExtension, IntegerType month,String dataAbsentReason) {
		if(dataAbsentReason != null && !dataAbsentReason.isEmpty()) {
			baseExtension.addExtension(new Extension(CommonUtil.partialDateDateMonthAbsentReasonURL,CommonUtil.findCodeFromCollectionUsingSimpleString(dataAbsentReason, CommonUtil.dataAbsentReasonCodeSet)));
		}
		else if(month != null && !month.isEmpty()){
			baseExtension.addExtension(new Extension(CommonUtil.partialDateDateMonthURL,month));
		}
		return this;
	}
	
	private DeathDate addPartialDateDay(Extension baseExtension, IntegerType day,String dataAbsentReason) {
		if(dataAbsentReason != null && !dataAbsentReason.isEmpty()) {
			baseExtension.addExtension(new Extension(CommonUtil.partialDateDateDayAbsentReasonURL,CommonUtil.findCodeFromCollectionUsingSimpleString(dataAbsentReason, CommonUtil.dataAbsentReasonCodeSet)));
		}
		else if(day != null && !day.isEmpty()){
			baseExtension.addExtension(new Extension(CommonUtil.partialDateDateDayURL,day));
		}
		return this;
	}

	private DeathDate addPartialDateTime(Extension baseExtension, StringType time,String dataAbsentReason) {
		if(dataAbsentReason != null && !dataAbsentReason.isEmpty()) {
			baseExtension.addExtension(new Extension(CommonUtil.partialDateDateTimeAbsentReasonURL,CommonUtil.findCodeFromCollectionUsingSimpleString(dataAbsentReason, CommonUtil.dataAbsentReasonCodeSet)));
		}
		else if(time != null && !time.isEmpty()){
			baseExtension.addExtension(new Extension(CommonUtil.partialDateDateTimeURL,time));
		}
		return this;
	}
}
