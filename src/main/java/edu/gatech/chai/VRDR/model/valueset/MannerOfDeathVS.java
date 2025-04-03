
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class MannerOfDeathVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-manner-of-death-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","38605008","Natural death")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","7878000","Accidental death")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","44301001","Suicide")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","27935005","Homicide")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","185973002","Patient awaiting investigation")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","65037004","Death, manner undetermined"))
    ));
};