
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class CanadaProvincesVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-canada-provinces-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","AB","Alberta")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","BC","British Columbia")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","MB","Manitoba")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","NB","New Brunswick")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","NL","Newfoundland and Labrador")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","NS","Nova Scotia")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","NT","Northwest Territories")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","NU","Nunavut")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","ON","Ontario")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","PE","Prince Edward Island")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","QC","Quebec")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","SK","Saskatchewan")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-canadian-provinces-cs","YT","Yukon"))
    ));
};