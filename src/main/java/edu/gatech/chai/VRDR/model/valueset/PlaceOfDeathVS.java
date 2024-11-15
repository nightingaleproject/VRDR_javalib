
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class PlaceOfDeathVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-place-of-death-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","63238001","Dead on arrival at hospital")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","440081000124100","Death in home")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","440071000124103","Death in hospice")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","16983000","Death in hospital")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","450391000124102","Death in hospital-based emergency department or outpatient department")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","450381000124100","Death in nursing home or long term care facility")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","OTH","Other")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","UNK","UNK"))
    ));
};