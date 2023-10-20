package edu.gatech.chai.VRDR.model;

public enum XmlRepresentation
{
    None,
    XmlElement, // This property is represented as an Xml element
    XmlAttr,  // In Xml, this property is represented as an attribute not an element.
    XmlText,  // This element is represented using the Xml text attribute (primitives only)
    TypeAttr, // The type of this element is indicated using xsi:type
    CdaText,  // Use CDA narrative instead of XHTML
    XHtml,    // The property is represented using XHTML
}
