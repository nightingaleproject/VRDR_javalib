
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class EditBypass0124VS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-edit-bypass-0124-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-bypass-edit-flag-cs","0","Edit Passed")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-bypass-edit-flag-cs","1","Edit Failed, Data Queried, and Verified")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-bypass-edit-flag-cs","2","Edit Failed, Data Queried, but not Verified")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-bypass-edit-flag-cs","4","Edit Failed, Query Needed"))
    ));
};