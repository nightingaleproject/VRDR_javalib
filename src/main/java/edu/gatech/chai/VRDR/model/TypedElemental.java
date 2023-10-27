package edu.gatech.chai.VRDR.model;

import java.util.List;

public interface TypedElemental
{
        /// <summary>
        /// Enumerate the child nodes present in the source representation (if any)
        /// </summary>
        /// <param name="name">Return only the children with the given name.</param>
        /// <returns></returns>
        List<TypedElemental> Children(String name);

        /// <summary>
        /// Name of the node, e.g. "active", "value".
        /// </summary>
        String Name = null; //{ get; }
        public Object getName();

        /// <summary>
        /// Type of the node. If a FHIR type, this is just a simple string, otherwise a StructureDefinition url for a type defined as a logical model.
        /// </summary>
        String InstanceType = null; //{ get; }
        public Object getInstanceType();

        /// <summary>
        /// The value of the node (if it represents a primitive FHIR value)
        /// </summary>
        /// <remarks>
        /// FHIR primitives are mapped to underlying C# types as follows:
        ///
        /// instant         Hl7.Fhir.ElementModel.Types.DateTime
        /// time            Hl7.Fhir.ElementModel.Types.Time
        /// date            Hl7.Fhir.ElementModel.Types.Date
        /// dateTime        Hl7.Fhir.ElementModel.Types.DateTime
        /// decimal         decimal
        /// boolean         bool
        /// integer         int
        /// unsignedInt     int
        /// positiveInt     int
        /// long/integer64  long (name will be finalized in R5)
        /// String          string
        /// code            string
        /// id              string
        /// uri, oid, uuid, 
        /// canonical, url  string
        /// markdown        string
        /// base64Binary    String (uuencoded)
        /// xhtml           string
        /// </remarks>
        Object Value = null; //{ get; }
        public Object getValue();

        /// <summary>
        /// An indication of the location of this node within the data represented by the <c>TypedElemental</c>.
        /// </summary>
        /// <remarks>The format of the location is the dotted name of the property, including indices to make
        /// sure repeated occurences of an element can be distinguished. It needs to be sufficiently precise to aid 
        /// the user in locating issues in the data.</remarks>
        String Location = null; //{ get; }
        public Object getLocation();

        ElementDefinitionSummarily Definition = null; //{ get; }
        public ElementDefinitionSummarily getElementDefinitionSummarily();
}

