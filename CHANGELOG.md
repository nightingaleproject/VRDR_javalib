## Changelog

### 4.0.2 - 2023-06-05
* Update and sync examples files (json and xml) with IG as follows:
    Remove Transport Role and its references
    
    Remove Death Certificate Reference and its references
    
    Remove Death Pronouncement Performer and its references
    
    Remove Decedent Employment History and its references
    
    Remove Cause of Death Pathway and its references
    
    Change Mortician, Funeral Home Director, Funeral Service Licensee to us-core-practitioner
    
    Change Interested Party to us-core-organization
    
    Change Decendent Pregnancy to decedent-pregnancy-status
    
    Change Cause of Death Condition to cause-of-death-part1
    
    Change Cause of Death Condition Contributing Death to cause-of-death-part2

### 4.0.1 - 2023-05-16

* Sync of race and ethnicity literal fields with VRDR IG latest build
* Added support for valueTime in Date of Death Pronounced as per VRDR IG
* Removed Mortician class as not useful or used in the library
* Corrected numerous FHIR profile ID and mapping errors

### v1.4.3-STU2.1 - 2023-04-26

* Race Literal Helper Methods

### v1.4.2-STU2.1 - 2023-04-25

* Race Literal AlaskanNative typo fix

### v1.4.1-STU2.1 - 2023-04-20

* Numerous bug fixes found as part of testing certification with UT
* Race Literal handling update as per VRDR IG

### v1.4.0-STU2 - 2023-02-10

* Added support for VRDR Messaging IG
* New package "messaging" for VRDR Messaging IG models
* New package "messaging.util" for messaging helper classes separate from the IG models
* VRDRFhirContext now contains all data structure and messaging definitions and is the primary context use case
* New class VRDRFhirContextDataStructuresOnly is a context class with only the data structure definitions
* New package edu.gatech.chai.VRDR.messaging contains all messaging structure definitions in the IG
* Besides IG classes, messaging also contains BaseMessage and UnknownMessage classes for assisting in message handling
* New class MessagingTest contains thorough messaging tests for VRDR Messaging IG flows
* Test resources now contains FHIR json and xml files for use by the MessagingTest class
* Generic parser method on BaseMessage for parsing bundles and messages
* Added xml and json test files and tests
* Added CauseOfDeath and Demographics coded bundle handling
* Tests complete for all major flows
* Added CLI for generating canary tests
* Added CHANGELOG for tracking changes with each release
* Updated README

