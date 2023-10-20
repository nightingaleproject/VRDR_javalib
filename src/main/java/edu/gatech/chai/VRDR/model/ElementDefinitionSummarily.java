package edu.gatech.chai.VRDR.model;

public interface ElementDefinitionSummarily
{
        String ElementName = null; //{ get; }
        String getElementName();
        boolean IsCollection = false; //{ get; }
        String getIsCollection();
        boolean IsRequired = false; //{ get; }
        String getIsRequired();
        boolean InSummary = false; //{ get; }
        String getInSummary();
        boolean IsChoiceElement = false; //{ get; }
        String getIsChoiceElement();
        boolean IsResource = false; //{ get; }
        String getIsResource();

        /// <summary>
        /// If this modifies the meaning of other elements
        /// </summary>
        boolean IsModifier = false; //{ get; }
        String getIsModifier();

        TypeSerializationInformative[] Type = null; //{ get; }
        TypeSerializationInformative[] getTypeSerializationInformative();

        /// <summary>
        /// Logical Models where a choice type is represented by ElementDefinition.representation = typeAttr might define a default type (elementdefinition-defaulttype extension). null in most cases.
        /// </summary>
        String DefaultTypeName = null; //{ get; }
        String getDefaultTypeName();

        /// <summary>
        /// This is the namespace used for the xml node representing this element.
        /// Only need to be set if different from "http://hl7.org/fhir".
        /// </summary>
        String NonDefaultNamespace = null; //{ get; }
        String getNonDefaultNamespace();

        /// <summary>
        /// The kind of node used to represent this element in XML.
        /// Default is <see cref="XmlRepresentation.XmlElement"/>.
        /// </summary>
        XmlRepresentation Representation = null; //{ get; }
        String getXmlRepresentation();

        Integer Order = null; //{ get; }
        Integer getOrder();
}

interface TypeSerializationInformative
{
}

/// <summary>
/// A class representing a complex type, with child elements. 
/// </summary>
/// <remarks>
///  In FHIR, this interface represents definitions of Resources, datatypes and BackboneElements. 
///  BackboneElements will have the TypeName set to "BackboneElement" (in resources) or "Element" (in datatypes)
///  and IsAbstract set to true.
///  </remarks>
 interface IStructureDefinitionSummary extends TypeSerializationInformative
        {
        String TypeName  = null;// { get; }
        String getTypeName();
        boolean IsAbstract  = false;// { get; }
        boolean getIsAbstract();
        boolean IsResource  = false;// { get; }
        boolean getIsResource();

        //ReadOnlyCollectionable<ElementDefinitionSummarily> GetElements();
        }

 interface IStructureDefinitionReference extends TypeSerializationInformative
    {
    String ReferredType = null;// { get; }
    String getReferredType();
    }

 interface IStructureDefinitionSummaryProvider
{
    IStructureDefinitionSummary Provide(String canonical);
}

 class TypeSerializationInfoExtensions
{
    public static String GetTypeName(TypeSerializationInformative info)
    {
        switch(info)
        {
            case IStructureDefinitionReference tr:
                return tr.ReferredType;
            case IStructureDefinitionSummary ct:
                return ct.TypeName;
            default:
                try {
                    throw new Exception(new StringBuffer("Don't know how to derive type information from type ").append(info.getClass()).toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }
    }

}