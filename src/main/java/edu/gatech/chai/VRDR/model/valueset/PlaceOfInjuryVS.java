
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class PlaceOfInjuryVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-place-of-injury-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://loinc.org","LA14084-0","Home")),
        new CodeableConcept().addCoding(new Coding("http://loinc.org","LA14085-7","Residential institution")),
        new CodeableConcept().addCoding(new Coding("http://loinc.org","LA14086-5","School")),
        new CodeableConcept().addCoding(new Coding("http://loinc.org","LA14088-1","Sports or recreational area")),
        new CodeableConcept().addCoding(new Coding("http://loinc.org","LA14089-9","Street or highway")),
        new CodeableConcept().addCoding(new Coding("http://loinc.org","LA14090-7","Trade or service area")),
        new CodeableConcept().addCoding(new Coding("http://loinc.org","LA14091-5","Industrial or construction area")),
        new CodeableConcept().addCoding(new Coding("http://loinc.org","LA14092-3","Farm")),
        new CodeableConcept().addCoding(new Coding("http://loinc.org","LA14093-1","Unspecified")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","OTH","Other"))
    ));
};