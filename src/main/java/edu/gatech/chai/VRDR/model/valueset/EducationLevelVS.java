
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class EducationLevelVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-education-level-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-EducationLevel","ELEM","Elementary School")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-EducationLevel","SEC","Some secondary or high school education")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-EducationLevel","HS","High School or secondary school degree complete")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-EducationLevel","SCOL","Some College education")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-EducationLevel","POSTG","Doctoral or post graduate education")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0360","AA","Associate's or technical degree complete")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0360","BA","Bachelor's degree")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0360","MA","Master's degree")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","UNK","unknown"))
    ));
};