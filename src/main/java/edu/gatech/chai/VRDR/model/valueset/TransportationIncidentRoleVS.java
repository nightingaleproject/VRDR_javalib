
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class TransportationIncidentRoleVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-transportation-incident-role-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","236320001","Vehicle driver")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","257500003","Passenger")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","257518000","Pedestrian")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","OTH","Other")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","UNK","unknown")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","NA","not applicable"))
    ));
};