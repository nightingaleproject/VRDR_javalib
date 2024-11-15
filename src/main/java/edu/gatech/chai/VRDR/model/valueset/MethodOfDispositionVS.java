
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class MethodOfDispositionVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-method-of-disposition-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","449931000124108","Entombment")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","449941000124103","Removal from state")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","449951000124101","Donation")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","449961000124104","Cremation")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","449971000124106","Burial")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","OTH","Other")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","UNK","Unknown"))
    ));
};