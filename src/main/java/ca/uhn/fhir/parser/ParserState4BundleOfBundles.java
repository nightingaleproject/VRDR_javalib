package ca.uhn.fhir.parser;

/*
 * #%L
 * This is a modified version of JsonParser.java from ca.uhn.fhir.parser of ca.uhn.hapi.fhir
 * (version 4.1.0) to function as 1 of the 2 helper classes for parsing FHIR Bundle of Bundles
 * #L%
 */

import ca.uhn.fhir.context.*;
import ca.uhn.fhir.context.BaseRuntimeChildDefinition.IMutator;
import ca.uhn.fhir.model.api.*;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.base.composite.BaseResourceReferenceDt;
import ca.uhn.fhir.model.base.resource.ResourceMetadataMap;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.model.primitive.XhtmlDt;
import ca.uhn.fhir.parser.json.JsonLikeValue.ScalarType;
import ca.uhn.fhir.parser.json.JsonLikeValue.ValueType;
import ca.uhn.fhir.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;
import static org.apache.commons.lang3.StringUtils.*;

class ParserState4BundleOfBundles<T> {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ParserState4BundleOfBundles.class);
    private final FhirContext myContext;
    private final IParserErrorHandler myErrorHandler;
    private final boolean myJsonMode;
    private final IParser myParser;
    private List<String> myComments = new ArrayList<String>(2);
    private T myObject;
    private IBase myPreviousElement;
    private ParserState4BundleOfBundles.BaseState myState;

    public ParserState4BundleOfBundles(IParser theParser, FhirContext theContext, boolean theJsonMode, IParserErrorHandler theErrorHandler) {
        myParser = theParser;
        myContext = theContext;
        myJsonMode = theJsonMode;
        myErrorHandler = theErrorHandler;
    }

    public void attributeValue(String theName, String theValue) throws DataFormatException {
        myState.attributeValue(theName, theValue);
    }

    public void commentPost(String theCommentText) {
        if (myPreviousElement != null) {
            myPreviousElement.getFormatCommentsPost().add(theCommentText);
        }
    }

    public void commentPre(String theCommentText) {
        if (myState.getCurrentElement() != null) {
            IBase element = myState.getCurrentElement();
            element.getFormatCommentsPre().add(theCommentText);
        }
    }

    boolean elementIsRepeating(String theChildName) {
        return myState.elementIsRepeating(theChildName);
    }

    void endingElement() throws DataFormatException {
        myState.endingElement();
    }

    void enteringNewElement(String theNamespaceUri, String theName) throws DataFormatException {
        myState.enteringNewElement(theNamespaceUri, theName);
    }

    void enteringNewElementExtension(StartElement theElem, String theUrlAttr, boolean theIsModifier, final String baseServerUrl) {
        myState.enteringNewElementExtension(theElem, theUrlAttr, theIsModifier, baseServerUrl);
    }

    public T getObject() {
        return myObject;
    }

    boolean isPreResource() {
        return myState.isPreResource();
    }

    private Object newContainedDt(IResource theTarget) {
        return ReflectionUtil.newInstance(theTarget.getStructureFhirVersionEnum().getVersionImplementation().getContainedType());
    }

    @SuppressWarnings("unchecked")
    private void pop() {
        myPreviousElement = myState.getCurrentElement();
        if (myState.myStack != null) {
            myState = myState.myStack;
            myState.wereBack();
        } else {
            myObject = (T) myState.getCurrentElement();
            myState = null;
        }
    }

    private void push(ParserState4BundleOfBundles.BaseState theState) {
        theState.setStack(myState);
        myState = theState;
        if (myComments.isEmpty() == false) {
            if (myState.getCurrentElement() != null) {
                myState.getCurrentElement().getFormatCommentsPre().addAll(myComments);
                myComments.clear();
            }
        }
    }


    public void string(String theData) {
        myState.string(theData);
    }

    /**
     * Invoked after any new XML event is individually processed, containing a copy of the XML event. This is basically
     * intended for embedded XHTML content
     */
    void xmlEvent(XMLEvent theNextEvent) {
        if (myState != null) {
            myState.xmlEvent(theNextEvent);
        }
    }

    private abstract class BaseState {

        private ParserState4BundleOfBundles.PreResourceState myPreResourceState;
        private ParserState4BundleOfBundles.BaseState myStack;

        BaseState(ParserState4BundleOfBundles.PreResourceState thePreResourceState) {
            super();
            myPreResourceState = thePreResourceState;
        }

        /**
         * @param theValue The attribute value
         */
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            myErrorHandler.unknownAttribute(null, theName);
        }

        public boolean elementIsRepeating(String theChildName) {
            return false;
        }

        public void endingElement() throws DataFormatException {
            // ignore by default
        }

        /**
         * @param theNamespaceUri The XML namespace (if XML) or null
         */
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            myErrorHandler.unknownElement(null, theLocalPart);
        }

        /**
         * Default implementation just handles undeclared extensions
         */
        @SuppressWarnings("unused")
        public void enteringNewElementExtension(StartElement theElement, String theUrlAttr, boolean theIsModifier, final String baseServerUrl) {
            if (myPreResourceState != null && getCurrentElement() instanceof ISupportsUndeclaredExtensions) {
                ExtensionDt newExtension = new ExtensionDt(theIsModifier);
                newExtension.setUrl(theUrlAttr);
                ISupportsUndeclaredExtensions elem = (ISupportsUndeclaredExtensions) getCurrentElement();
                elem.addUndeclaredExtension(newExtension);
                ParserState4BundleOfBundles.ExtensionState newState = new ParserState4BundleOfBundles.ExtensionState(myPreResourceState, newExtension);
                push(newState);
            } else {
                if (theIsModifier == false) {
                    if (getCurrentElement() instanceof IBaseHasExtensions) {
                        IBaseExtension<?, ?> ext = ((IBaseHasExtensions) getCurrentElement()).addExtension();
                        ext.setUrl(theUrlAttr);
                        ParserState4BundleOfBundles<T>.ExtensionState newState = new ParserState4BundleOfBundles.ExtensionState(myPreResourceState, ext);
                        push(newState);
                    } else {
                        logAndSwallowUnexpectedElement("extension");
                    }
                } else {
                    if (getCurrentElement() instanceof IBaseHasModifierExtensions) {
                        IBaseExtension<?, ?> ext = ((IBaseHasModifierExtensions) getCurrentElement()).addModifierExtension();
                        ext.setUrl(theUrlAttr);
                        ParserState4BundleOfBundles<T>.ExtensionState newState = new ParserState4BundleOfBundles.ExtensionState(myPreResourceState, ext);
                        push(newState);
                    } else {
                        logAndSwallowUnexpectedElement("modifierExtension");
                    }
                }
            }
        }

        protected IBase getCurrentElement() {
            return null;
        }

        ParserState4BundleOfBundles.PreResourceState getPreResourceState() {
            return myPreResourceState;
        }

        public boolean isPreResource() {
            return false;
        }

        void logAndSwallowUnexpectedElement(String theLocalPart) {
            myErrorHandler.unknownElement(null, theLocalPart);
            push(new ParserState4BundleOfBundles.SwallowChildrenWholeState(getPreResourceState()));
        }

        public void setStack(ParserState4BundleOfBundles.BaseState theState) {
            myStack = theState;
        }

        /**
         * @param theData The string value
         */
        public void string(String theData) {
            // ignore by default
        }

        public void wereBack() {
            // allow an implementor to override
        }

        /**
         * @param theNextEvent The XML event
         */
        public void xmlEvent(XMLEvent theNextEvent) {
            // ignore
        }
    }

    private class ContainedResourcesStateHapi extends ParserState4BundleOfBundles.PreResourceState {

        public ContainedResourcesStateHapi(ParserState4BundleOfBundles.PreResourceState thePreResourcesState) {
            super(thePreResourcesState, ((IResource) thePreResourcesState.myInstance).getStructureFhirVersionEnum());
        }

        @Override
        public void endingElement() throws DataFormatException {
            pop();
        }

        @Override
        protected void populateTarget() {
            // nothing
        }

        @Override
        public void wereBack() {
            super.wereBack();

            IResource res = (IResource) getCurrentElement();
            assert res != null;
            if (res.getId() == null || res.getId().isEmpty()) {
                // If there is no ID, we don't keep the resource because it's useless (contained resources
                // need an ID to be referred to)
                myErrorHandler.containedResourceWithNoId(null);
            } else {
                if (!res.getId().isLocal()) {
                    res.setId(new IdDt('#' + res.getId().getIdPart()));
                }
                getPreResourceState().getContainedResources().put(res.getId().getValueAsString(), res);
            }
            IResource preResCurrentElement = (IResource) getPreResourceState().getCurrentElement();

            @SuppressWarnings("unchecked")
            List<IResource> containedResources = (List<IResource>) preResCurrentElement.getContained().getContainedResources();
            containedResources.add(res);
        }
    }

    private class ContainedResourcesStateHl7Org extends ParserState4BundleOfBundles.PreResourceState {

        public ContainedResourcesStateHl7Org(ParserState4BundleOfBundles.PreResourceState thePreResourcesState) {
            super(thePreResourcesState, thePreResourcesState.myParentVersion);
        }

        @Override
        public void endingElement() throws DataFormatException {
            pop();
        }

        @Override
        protected void populateTarget() {
            // nothing
        }

        @Override
        public void wereBack() {
            super.wereBack();

            IBaseResource res = getCurrentElement();
            assert res != null;
            if (res.getIdElement() == null || res.getIdElement().isEmpty()) {
                // If there is no ID, we don't keep the resource because it's useless (contained resources
                // need an ID to be referred to)
                myErrorHandler.containedResourceWithNoId(null);
            } else {
                res.getIdElement().setValue('#' + res.getIdElement().getIdPart());
                getPreResourceState().getContainedResources().put(res.getIdElement().getValue(), res);
            }

            IBaseResource preResCurrentElement = getPreResourceState().getCurrentElement();
            RuntimeResourceDefinition def = myContext.getResourceDefinition(preResCurrentElement);
            def.getChildByName("contained").getMutator().addValue(preResCurrentElement, res);
        }
    }

    @SuppressWarnings("EnumSwitchStatementWhichMissesCases")
    private class DeclaredExtensionState extends ParserState4BundleOfBundles.BaseState {

        private IBase myChildInstance;
        private RuntimeChildDeclaredExtensionDefinition myDefinition;
        private IBase myParentInstance;
        private ParserState4BundleOfBundles.PreResourceState myPreResourceState;

        public DeclaredExtensionState(ParserState4BundleOfBundles.PreResourceState thePreResourceState, RuntimeChildDeclaredExtensionDefinition theDefinition, IBase theParentInstance) {
            super(thePreResourceState);
            myPreResourceState = thePreResourceState;
            myDefinition = theDefinition;
            myParentInstance = theParentInstance;
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            if (theName.equals("url")) {
                // This can be ignored
                return;
            }
            super.attributeValue(theName, theValue);
        }

        @Override
        public void endingElement() throws DataFormatException {
            pop();
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            BaseRuntimeElementDefinition<?> target = myDefinition.getChildByName(theLocalPart);
            if (target == null) {
                myErrorHandler.unknownElement(null, theLocalPart);
                push(new ParserState4BundleOfBundles.SwallowChildrenWholeState(getPreResourceState()));
                return;
            }

            switch (target.getChildType()) {
                case COMPOSITE_DATATYPE: {
                    BaseRuntimeElementCompositeDefinition<?> compositeTarget = (BaseRuntimeElementCompositeDefinition<?>) target;
                    ICompositeType newChildInstance = (ICompositeType) compositeTarget.newInstance(myDefinition.getInstanceConstructorArguments());
                    myDefinition.getMutator().addValue(myParentInstance, newChildInstance);
                    ParserState4BundleOfBundles.ElementCompositeState newState = new ParserState4BundleOfBundles.ElementCompositeState(myPreResourceState, theLocalPart, compositeTarget, newChildInstance);
                    push(newState);
                    return;
                }
                case ID_DATATYPE:
                case PRIMITIVE_DATATYPE: {
                    RuntimePrimitiveDatatypeDefinition primitiveTarget = (RuntimePrimitiveDatatypeDefinition) target;
                    IPrimitiveType<?> newChildInstance = primitiveTarget.newInstance(myDefinition.getInstanceConstructorArguments());
                    myDefinition.getMutator().addValue(myParentInstance, newChildInstance);
                    ParserState4BundleOfBundles.PrimitiveState newState = new ParserState4BundleOfBundles.PrimitiveState(getPreResourceState(), newChildInstance);
                    push(newState);
                    return;
                }
                case PRIMITIVE_XHTML:
                case RESOURCE:
                case RESOURCE_BLOCK:
                case UNDECL_EXT:
                case EXTENSION_DECLARED:
                default:
                    break;
            }
        }

        @Override
        public void enteringNewElementExtension(StartElement theElement, String theUrlAttr, boolean theIsModifier, final String baseServerUrl) {
            RuntimeChildDeclaredExtensionDefinition declaredExtension = myDefinition.getChildExtensionForUrl(theUrlAttr);
            if (declaredExtension != null) {
                if (myChildInstance == null) {
                    myChildInstance = myDefinition.newInstance();
                    myDefinition.getMutator().addValue(myParentInstance, myChildInstance);
                }
                ParserState4BundleOfBundles.BaseState newState = new ParserState4BundleOfBundles.DeclaredExtensionState(getPreResourceState(), declaredExtension, myChildInstance);
                push(newState);
            } else {
                super.enteringNewElementExtension(theElement, theUrlAttr, theIsModifier, baseServerUrl);
            }
        }

        @Override
        protected IBase getCurrentElement() {
            return myParentInstance;
        }
    }

    private class ElementCompositeState extends ParserState4BundleOfBundles.BaseState {

        private BaseRuntimeElementCompositeDefinition<?> myDefinition;
        private IBase myInstance;
        private Set<String> myParsedNonRepeatableNames = new HashSet<>();
        private String myElementName;

        ElementCompositeState(ParserState4BundleOfBundles.PreResourceState thePreResourceState, String theElementName, BaseRuntimeElementCompositeDefinition<?> theDef, IBase theInstance) {
            super(thePreResourceState);
            myDefinition = theDef;
            myInstance = theInstance;
            myElementName = theElementName;
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            if ("id".equals(theName)) {
                if (myInstance instanceof IIdentifiableElement) {
                    ((IIdentifiableElement) myInstance).setElementSpecificId((theValue));
                } else if (myInstance instanceof IBaseElement) {
                    ((IBaseElement) myInstance).setId(theValue);
                }
            } else {
                if (myJsonMode) {
                    myErrorHandler.incorrectJsonType(null, myElementName, ValueType.OBJECT, null, ValueType.SCALAR, ScalarType.STRING);
                } else {
                    myErrorHandler.unknownAttribute(null, theName);
                }
            }
        }

        @Override
        public boolean elementIsRepeating(String theChildName) {
            BaseRuntimeChildDefinition child = myDefinition.getChildByName(theChildName);
            if (child == null) {
                return false;
            }
            return child.getMax() > 1 || child.getMax() == Child.MAX_UNLIMITED;
        }

        @Override
        public void endingElement() {
            pop();
        }

        @Override
        public void enteringNewElement(String theNamespace, String theChildName) throws DataFormatException {
            BaseRuntimeChildDefinition child = myDefinition.getChildByName(theChildName);
            if (child == null) {
                if (theChildName.equals("id")) {
                    if (getCurrentElement() instanceof IIdentifiableElement) {
                        push(new ParserState4BundleOfBundles.IdentifiableElementIdState(getPreResourceState(), (IIdentifiableElement) getCurrentElement()));
                        return;
                    }
                }

                /*
                 * This means we've found an element that doesn't exist on the structure. If the error handler doesn't throw
                 * an exception, swallow the element silently along with any child elements
                 */
                myErrorHandler.unknownElement(null, theChildName);
                push(new ParserState4BundleOfBundles.SwallowChildrenWholeState(getPreResourceState()));
                return;
            }

            if ((child.getMax() == 0 || child.getMax() == 1) && !myParsedNonRepeatableNames.add(theChildName)) {
                myErrorHandler.unexpectedRepeatingElement(null, theChildName);
                push(new ParserState4BundleOfBundles.SwallowChildrenWholeState(getPreResourceState()));
                return;
            }

            BaseRuntimeElementDefinition<?> target = child.getChildByName(theChildName);
            if (target == null) {
                // This is a bug with the structures and shouldn't happen..
                throw new DataFormatException("Found unexpected element '" + theChildName + "' in parent element '" + myDefinition.getName() + "'. Valid names are: " + child.getValidChildNames());
            }

            switch (target.getChildType()) {
                case COMPOSITE_DATATYPE: {
                    BaseRuntimeElementCompositeDefinition<?> compositeTarget = (BaseRuntimeElementCompositeDefinition<?>) target;
                    ICompositeType newChildInstance = (ICompositeType) compositeTarget.newInstance(child.getInstanceConstructorArguments());
                    child.getMutator().addValue(myInstance, newChildInstance);
                    ParserState4BundleOfBundles<T>.ElementCompositeState newState = new ParserState4BundleOfBundles.ElementCompositeState(getPreResourceState(), theChildName, compositeTarget, newChildInstance);
                    push(newState);
                    return;
                }
                case ID_DATATYPE:
                case PRIMITIVE_DATATYPE: {
                    RuntimePrimitiveDatatypeDefinition primitiveTarget = (RuntimePrimitiveDatatypeDefinition) target;
                    IPrimitiveType<?> newChildInstance;
                    newChildInstance = primitiveTarget.newInstance(child.getInstanceConstructorArguments());
                    child.getMutator().addValue(myInstance, newChildInstance);
                    ParserState4BundleOfBundles.PrimitiveState newState = new ParserState4BundleOfBundles.PrimitiveState(getPreResourceState(), newChildInstance);
                    push(newState);
                    return;
                }
                case RESOURCE_BLOCK: {
                    RuntimeResourceBlockDefinition blockTarget = (RuntimeResourceBlockDefinition) target;
                    IBase newBlockInstance = blockTarget.newInstance();
                    child.getMutator().addValue(myInstance, newBlockInstance);
                    ParserState4BundleOfBundles.ElementCompositeState newState = new ParserState4BundleOfBundles.ElementCompositeState(getPreResourceState(), theChildName, blockTarget, newBlockInstance);
                    push(newState);
                    return;
                }
                case PRIMITIVE_XHTML: {
                    RuntimePrimitiveDatatypeNarrativeDefinition xhtmlTarget = (RuntimePrimitiveDatatypeNarrativeDefinition) target;
                    XhtmlDt newDt = xhtmlTarget.newInstance();
                    child.getMutator().addValue(myInstance, newDt);
                    ParserState4BundleOfBundles.XhtmlState state = new ParserState4BundleOfBundles.XhtmlState(getPreResourceState(), newDt, true);
                    push(state);
                    return;
                }
                case PRIMITIVE_XHTML_HL7ORG: {
                    RuntimePrimitiveDatatypeXhtmlHl7OrgDefinition xhtmlTarget = (RuntimePrimitiveDatatypeXhtmlHl7OrgDefinition) target;
                    IBaseXhtml newDt = xhtmlTarget.newInstance();
                    child.getMutator().addValue(myInstance, newDt);
                    ParserState4BundleOfBundles.XhtmlStateHl7Org state = new ParserState4BundleOfBundles.XhtmlStateHl7Org(getPreResourceState(), newDt);
                    push(state);
                    return;
                }
                case CONTAINED_RESOURCES: {
                    List<? extends IBase> values = child.getAccessor().getValues(myInstance);
                    if (values == null || values.isEmpty() || values.get(0) == null) {
                        Object newDt = newContainedDt((IResource) getPreResourceState().myInstance);
                        child.getMutator().addValue(myInstance, (IBase) newDt);
                    }
                    ParserState4BundleOfBundles.ContainedResourcesStateHapi state = new ParserState4BundleOfBundles.ContainedResourcesStateHapi(getPreResourceState());
                    push(state);
                    return;
                }
                case CONTAINED_RESOURCE_LIST: {
                    ParserState4BundleOfBundles.ContainedResourcesStateHl7Org state = new ParserState4BundleOfBundles.ContainedResourcesStateHl7Org(getPreResourceState());
                    push(state);
                    return;
                }
                case RESOURCE: {
                    if (myInstance instanceof IAnyResource || myInstance instanceof IBaseBackboneElement || myInstance instanceof IBaseElement) {
                        ParserState4BundleOfBundles<T>.PreResourceStateHl7Org state = new ParserState4BundleOfBundles.PreResourceStateHl7Org(myInstance, child.getMutator(), null);
                        push(state);
                    } else {
                        ParserState4BundleOfBundles<T>.PreResourceStateHapi state = new ParserState4BundleOfBundles.PreResourceStateHapi(myInstance, child.getMutator(), null);
                        push(state);
                    }
                    return;
                }
                case UNDECL_EXT:
                case EXTENSION_DECLARED: {
                    // Throw an exception because this shouldn't happen here
                    break;
                }
            }

            throw new DataFormatException("Illegal resource position: " + target.getChildType());
        }

        @Override
        public void enteringNewElementExtension(StartElement theElement, String theUrlAttr, boolean theIsModifier, final String baseServerUrl) {
            RuntimeChildDeclaredExtensionDefinition declaredExtension = myDefinition.getDeclaredExtension(theUrlAttr, baseServerUrl);
            if (declaredExtension != null) {
                ParserState4BundleOfBundles.BaseState newState = new ParserState4BundleOfBundles.DeclaredExtensionState(getPreResourceState(), declaredExtension, myInstance);
                push(newState);
            } else {
                super.enteringNewElementExtension(theElement, theUrlAttr, theIsModifier, baseServerUrl);
            }
        }

        @Override
        protected IBase getCurrentElement() {
            return myInstance;
        }
    }

    public class ElementIdState extends ParserState4BundleOfBundles.BaseState {

        private IBaseElement myElement;

        ElementIdState(ParserState4BundleOfBundles<T>.PreResourceState thePreResourceState, IBaseElement theElement) {
            super(thePreResourceState);
            myElement = theElement;
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            myElement.setId(theValue);
        }

        @Override
        public void endingElement() {
            pop();
        }
    }

    private class ExtensionState extends ParserState4BundleOfBundles.BaseState {

        private IBaseExtension<?, ?> myExtension;

        ExtensionState(ParserState4BundleOfBundles.PreResourceState thePreResourceState, IBaseExtension<?, ?> theExtension) {
            super(thePreResourceState);
            myExtension = theExtension;
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            if ("url".equals(theName)) {
                // The URL attribute is handles in the XML loop as a special case since it is "url" instead
                // of "value" like every single other place
                return;
            }
            if ("id".equals(theName)) {
                if (getCurrentElement() instanceof IBaseElement) {
                    ((IBaseElement) getCurrentElement()).setId(theValue);
                    return;
                } else if (getCurrentElement() instanceof IIdentifiableElement) {
                    ((IIdentifiableElement) getCurrentElement()).setElementSpecificId(theValue);
                    return;
                }
            }
            super.attributeValue(theName, theValue);
        }

        @Override
        public void endingElement() throws DataFormatException {
            if (myExtension.getValue() != null && myExtension.getExtension().size() > 0) {
                throw new DataFormatException("Extension (URL='" + myExtension.getUrl() + "') must not have both a value and other contained extensions");
            }
            pop();
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            if (theLocalPart.equals("id")) {
                if (getCurrentElement() instanceof IBaseElement) {
                    push(new ParserState4BundleOfBundles.ElementIdState(getPreResourceState(), (IBaseElement) getCurrentElement()));
                    return;
                } else if (getCurrentElement() instanceof IIdentifiableElement) {
                    push(new ParserState4BundleOfBundles.IdentifiableElementIdState(getPreResourceState(), (IIdentifiableElement) getCurrentElement()));
                    return;
                }
            }

            BaseRuntimeElementDefinition<?> target = myContext.getRuntimeChildUndeclaredExtensionDefinition().getChildByName(theLocalPart);

            if (target != null) {
                switch (target.getChildType()) {
                    case COMPOSITE_DATATYPE: {
                        BaseRuntimeElementCompositeDefinition<?> compositeTarget = (BaseRuntimeElementCompositeDefinition<?>) target;
                        ICompositeType newChildInstance = (ICompositeType) compositeTarget.newInstance();
                        myExtension.setValue(newChildInstance);
                        ParserState4BundleOfBundles.ElementCompositeState newState = new ParserState4BundleOfBundles.ElementCompositeState(getPreResourceState(), theLocalPart, compositeTarget, newChildInstance);
                        push(newState);
                        return;
                    }
                    case ID_DATATYPE:
                    case PRIMITIVE_DATATYPE: {
                        RuntimePrimitiveDatatypeDefinition primitiveTarget = (RuntimePrimitiveDatatypeDefinition) target;
                        IPrimitiveType<?> newChildInstance = primitiveTarget.newInstance();
                        myExtension.setValue(newChildInstance);
                        ParserState4BundleOfBundles.PrimitiveState newState = new ParserState4BundleOfBundles.PrimitiveState(getPreResourceState(), newChildInstance);
                        push(newState);
                        return;
                    }
                    case CONTAINED_RESOURCES:
                    case CONTAINED_RESOURCE_LIST:
                    case EXTENSION_DECLARED:
                    case PRIMITIVE_XHTML:
                    case PRIMITIVE_XHTML_HL7ORG:
                    case RESOURCE:
                    case RESOURCE_BLOCK:
                    case UNDECL_EXT:
                        break;
                }
            }

            // We hit an invalid type for the extension
            myErrorHandler.unknownElement(null, theLocalPart);
            push(new ParserState4BundleOfBundles.SwallowChildrenWholeState(getPreResourceState()));
        }

        @Override
        protected IBaseExtension<?, ?> getCurrentElement() {
            return myExtension;
        }
    }

    public class IdentifiableElementIdState extends ParserState4BundleOfBundles.BaseState {

        private IIdentifiableElement myElement;

        public IdentifiableElementIdState(ParserState4BundleOfBundles<T>.PreResourceState thePreResourceState, IIdentifiableElement theElement) {
            super(thePreResourceState);
            myElement = theElement;
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            myElement.setElementSpecificId(theValue);
        }

        @Override
        public void endingElement() {
            pop();
        }
    }

    private class MetaElementState extends ParserState4BundleOfBundles.BaseState {
        private ResourceMetadataMap myMap;

        public MetaElementState(ParserState4BundleOfBundles<T>.PreResourceState thePreResourceState, ResourceMetadataMap theMap) {
            super(thePreResourceState);
            myMap = theMap;
        }

        @Override
        public void endingElement() throws DataFormatException {
            pop();
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            switch (theLocalPart) {
                case "versionId":
                    push(new ParserState4BundleOfBundles.MetaVersionElementState(getPreResourceState(), myMap));
                    // } else if (theLocalPart.equals("profile")) {
                    //
                    break;
                case "lastUpdated":
                    InstantDt updated = new InstantDt();
                    push(new ParserState4BundleOfBundles.PrimitiveState(getPreResourceState(), updated));
                    myMap.put(ResourceMetadataKeyEnum.UPDATED, updated);
                    break;
                case "security":
                    @SuppressWarnings("unchecked")
                    List<IBase> securityLabels = (List<IBase>) myMap.get(ResourceMetadataKeyEnum.SECURITY_LABELS);
                    if (securityLabels == null) {
                        securityLabels = new ArrayList<>();
                        myMap.put(ResourceMetadataKeyEnum.SECURITY_LABELS, securityLabels);
                    }
                    IBase securityLabel = myContext.getVersion().newCodingDt();
                    BaseRuntimeElementCompositeDefinition<?> codinfDef = (BaseRuntimeElementCompositeDefinition<?>) myContext.getElementDefinition(securityLabel.getClass());
                    push(new ParserState4BundleOfBundles.SecurityLabelElementStateHapi(getPreResourceState(), codinfDef, securityLabel));
                    securityLabels.add(securityLabel);
                    break;
                case "profile":
                    @SuppressWarnings("unchecked")
                    List<IdDt> profiles = (List<IdDt>) myMap.get(ResourceMetadataKeyEnum.PROFILES);
                    List<IdDt> newProfiles;
                    if (profiles != null) {
                        newProfiles = new ArrayList<>(profiles.size() + 1);
                        newProfiles.addAll(profiles);
                    } else {
                        newProfiles = new ArrayList<>(1);
                    }
                    IdDt profile = new IdDt();
                    push(new ParserState4BundleOfBundles.PrimitiveState(getPreResourceState(), profile));
                    newProfiles.add(profile);
                    myMap.put(ResourceMetadataKeyEnum.PROFILES, Collections.unmodifiableList(newProfiles));
                    break;
                case "tag":
                    TagList tagList = (TagList) myMap.get(ResourceMetadataKeyEnum.TAG_LIST);
                    if (tagList == null) {
                        tagList = new TagList();
                        myMap.put(ResourceMetadataKeyEnum.TAG_LIST, tagList);
                    }
                    push(new ParserState4BundleOfBundles.TagState(tagList));
                    break;
                default:
                    myErrorHandler.unknownElement(null, theLocalPart);
                    push(new ParserState4BundleOfBundles.SwallowChildrenWholeState(getPreResourceState()));
            }
        }

        @Override
        public void enteringNewElementExtension(StartElement theElem, String theUrlAttr, boolean theIsModifier, final String baseServerUrl) {
            ResourceMetadataKeyEnum.ExtensionResourceMetadataKey resourceMetadataKeyEnum = new ResourceMetadataKeyEnum.ExtensionResourceMetadataKey(theUrlAttr);
            Object metadataValue = myMap.get(resourceMetadataKeyEnum);
            ExtensionDt newExtension;
            if (metadataValue == null) {
                newExtension = new ExtensionDt(theIsModifier);
            } else if (metadataValue instanceof ExtensionDt) {
                newExtension = (ExtensionDt) metadataValue;
            } else {
                throw new IllegalStateException("Expected ExtensionDt as custom resource metadata type, got: " + metadataValue.getClass().getSimpleName());
            }
            newExtension.setUrl(theUrlAttr);
            myMap.put(resourceMetadataKeyEnum, newExtension);

            ParserState4BundleOfBundles.ExtensionState newState = new ParserState4BundleOfBundles.ExtensionState(getPreResourceState(), newExtension);
            push(newState);
        }
    }

    private class MetaVersionElementState extends ParserState4BundleOfBundles.BaseState {

        private ResourceMetadataMap myMap;

        MetaVersionElementState(ParserState4BundleOfBundles<T>.PreResourceState thePreResourceState, ResourceMetadataMap theMap) {
            super(thePreResourceState);
            myMap = theMap;
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            myMap.put(ResourceMetadataKeyEnum.VERSION, theValue);
        }

        @Override
        public void endingElement() throws DataFormatException {
            pop();
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            myErrorHandler.unknownElement(null, theLocalPart);
            push(new ParserState4BundleOfBundles.SwallowChildrenWholeState(getPreResourceState()));
        }
    }

    private abstract class PreResourceState extends ParserState4BundleOfBundles.BaseState {

        private Map<String, IBaseResource> myContainedResources;
        private IBaseResource myInstance;
        private FhirVersionEnum myParentVersion;
        private Class<? extends IBaseResource> myResourceType;

        PreResourceState(Class<? extends IBaseResource> theResourceType) {
            super(null);
            myResourceType = theResourceType;
            myContainedResources = new HashMap<>();
            if (theResourceType != null) {
                myParentVersion = myContext.getResourceDefinition(theResourceType).getStructureVersion();
            } else {
                myParentVersion = myContext.getVersion().getVersion();
            }
        }

        PreResourceState(ParserState4BundleOfBundles.PreResourceState thePreResourcesState, FhirVersionEnum theParentVersion) {
            super(thePreResourcesState);
            Validate.notNull(theParentVersion);
            myParentVersion = theParentVersion;
            myContainedResources = thePreResourcesState.getContainedResources();
        }

        @Override
        public void endingElement() throws DataFormatException {
            stitchBundleCrossReferences();
            pop();
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            RuntimeResourceDefinition definition;
            if (myResourceType == null) {
                definition = null;
                if (myParser.getPreferTypes() != null) {
                    for (Class<? extends IBaseResource> next : myParser.getPreferTypes()) {
                        RuntimeResourceDefinition nextDef = myContext.getResourceDefinition(next);
                        if (nextDef.getName().equals(theLocalPart)) {
                            definition = nextDef;
                        }
                    }
                }
                if (definition == null) {
                    definition = myContext.getResourceDefinition(myParentVersion, theLocalPart);
                }
                if ((definition == null)) {
                    throw new DataFormatException("Element '" + theLocalPart + "' is not a known resource type, expected a resource at this position");
                }
            } else {
                definition = myContext.getResourceDefinition(myResourceType);
                if (!definition.getName().equals("Bundle") && !myResourceType.getName().contains("Bundle") && !StringUtils.equals(theLocalPart, definition.getName())) {
                    throw new DataFormatException(myContext.getLocalizer().getMessage(ParserState4BundleOfBundles.class, "wrongResourceTypeFound", definition.getName(), theLocalPart));
                }
            }

            RuntimeResourceDefinition def = definition;
            if (!definition.getName().equals(theLocalPart) && definition.getName().equalsIgnoreCase(theLocalPart)) {
                throw new DataFormatException("Unknown resource type '" + theLocalPart + "': Resource names are case sensitive, found similar name: '" + definition.getName() + "'");
            }
            myInstance = def.newInstance();

            if (myInstance instanceof IResource) {
                push(new ParserState4BundleOfBundles.ResourceStateHapi(getRootPreResourceState(), def, (IResource) myInstance));
            } else {
                push(new ParserState4BundleOfBundles.ResourceStateHl7Org(getRootPreResourceState(), def, myInstance));
            }
        }

        public Map<String, IBaseResource> getContainedResources() {
            return myContainedResources;
        }

        @Override
        protected IBaseResource getCurrentElement() {
            return myInstance;
        }

        private ParserState4BundleOfBundles.PreResourceState getRootPreResourceState() {
            if (getPreResourceState() != null) {
                return getPreResourceState();
            }
            return this;
        }

        @Override
        public boolean isPreResource() {
            return true;
        }

        protected abstract void populateTarget();

        private void postProcess() {
            if (myContext.hasDefaultTypeForProfile()) {
                IBaseMetaType meta = myInstance.getMeta();
                Class<? extends IBaseResource> wantedProfileType = null;
                String usedProfile = null;
                for (IPrimitiveType<String> next : meta.getProfile()) {
                    if (isNotBlank(next.getValue())) {
                        wantedProfileType = myContext.getDefaultTypeForProfile(next.getValue());
                        if (wantedProfileType != null) {
                            usedProfile = next.getValue();
                            break;
                        }
                    }
                }
                if (wantedProfileType != null && !wantedProfileType.equals(myInstance.getClass())) {
                    if (myResourceType == null || myResourceType.isAssignableFrom(wantedProfileType)) {
                        logger.debug("Converting resource of type {} to type defined for profile \"{}\": {}", myInstance.getClass().getName(), usedProfile, wantedProfileType);

                        /*
                         * This isn't the most efficient thing really.. If we want a specific
                         * type we just re-parse into that type. The problem is that we don't know
                         * until we've parsed the resource which type we want to use because the
                         * profile declarations are in the text of the resource itself.
                         *
                         * At some point it would be good to write code which can present a view
                         * of one type backed by another type and use that.
                         */
                       // IParser parser = myContext.newJsonParser();
                        IParser parser = new JsonParser4BundleOfBundles(myContext, new LenientErrorHandler());
                        String asString = parser.encodeResourceToString(myInstance);
                        myInstance = parser.parseResource(wantedProfileType, asString);
                    }
                }
            }
            populateTarget();
        }

        private void stitchBundleCrossReferences() {
            final boolean bundle = "Bundle".equals(myContext.getResourceDefinition(myInstance).getName());
            if (bundle) {

                FhirTerser t = myContext.newTerser();

                Map<String, IBaseResource> idToResource = new HashMap<>();
                List<IBase> entries = t.getValues(myInstance, "Bundle.entry", IBase.class);
                for (IBase nextEntry : entries) {
                    IPrimitiveType<?> fullUrl = t.getSingleValueOrNull(nextEntry, "fullUrl", IPrimitiveType.class);
                    if (fullUrl != null && isNotBlank(fullUrl.getValueAsString())) {
                        IBaseResource resource = t.getSingleValueOrNull(nextEntry, "resource", IBaseResource.class);
                        if (resource != null) {
                            idToResource.put(fullUrl.getValueAsString(), resource);
                        }
                    }
                }

                /*
                 * Stitch together resource references
                 */
                List<IBaseResource> resources = t.getAllPopulatedChildElementsOfType(myInstance, IBaseResource.class);
                for (IBaseResource next : resources) {
                    IIdType id = next.getIdElement();
                    if (id != null && id.isEmpty() == false) {
                        String resName = myContext.getResourceDefinition(next).getName();
                        IIdType idType = id.withResourceType(resName).toUnqualifiedVersionless();
                        idToResource.put(idType.getValueAsString(), next);
                    }
                }

                for (IBaseResource next : resources) {
                    List<IBaseReference> refs = myContext.newTerser().getAllPopulatedChildElementsOfType(next, IBaseReference.class);
                    for (IBaseReference nextRef : refs) {
                        if (nextRef.isEmpty() == false && nextRef.getReferenceElement() != null) {
                            IIdType unqualifiedVersionless = nextRef.getReferenceElement().toUnqualifiedVersionless();
                            IBaseResource target = idToResource.get(unqualifiedVersionless.getValueAsString());
                            if (target != null) {
                                nextRef.setResource(target);
                            }
                        }
                    }
                }

                /*
                 * Set resource IDs based on Bundle.entry.request.url
                 */
                List<Pair<String, IBaseResource>> urlsAndResources = BundleUtil.getBundleEntryUrlsAndResources(myContext, (IBaseBundle) myInstance);
                for (Pair<String, IBaseResource> pair : urlsAndResources) {
                    if (pair.getRight() != null && isNotBlank(pair.getLeft()) && pair.getRight().getIdElement().isEmpty()) {
                        if (pair.getLeft().startsWith("urn:")) {
                            pair.getRight().setId(pair.getLeft());
                        }
                    }
                }

            }
        }

        void weaveContainedResources() {
            FhirTerser terser = myContext.newTerser();
            terser.visit(myInstance, new IModelVisitor() {

                @Override
                public void acceptElement(IBaseResource theResource, IBase theElement, List<String> thePathToElement, BaseRuntimeChildDefinition theChildDefinition,
                                          BaseRuntimeElementDefinition<?> theDefinition) {
                    if (theElement instanceof BaseResourceReferenceDt) {
                        BaseResourceReferenceDt nextRef = (BaseResourceReferenceDt) theElement;
                        String ref = nextRef.getReference().getValue();
                        if (isNotBlank(ref)) {
                            if (ref.startsWith("#")) {
                                IResource target = (IResource) myContainedResources.get(ref);
                                if (target != null) {
                                    logger.debug("Resource contains local ref {} in field {}", ref, thePathToElement);
                                    nextRef.setResource(target);
                                } else {
                                    myErrorHandler.unknownReference(null, ref);
                                }
                            }
                        }
                    } else if (theElement instanceof IBaseReference) {
                        IBaseReference nextRef = (IBaseReference) theElement;
                        String ref = nextRef.getReferenceElement().getValue();
                        if (isNotBlank(ref)) {
                            if (ref.startsWith("#")) {
                                IBaseResource target = myContainedResources.get(ref);
                                if (target != null) {
                                    logger.debug("Resource contains local ref {} in field {}", ref, thePathToElement);
                                    nextRef.setResource(target);
                                } else {
                                    myErrorHandler.unknownReference(null, ref);
                                }
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void wereBack() {
            postProcess();
        }

    }

    private class PreResourceStateHapi extends ParserState4BundleOfBundles.PreResourceState {
        private IMutator myMutator;
        private IBase myTarget;


        PreResourceStateHapi(Class<? extends IBaseResource> theResourceType) {
            super(theResourceType);
            assert theResourceType == null || IResource.class.isAssignableFrom(theResourceType);
        }

        PreResourceStateHapi(IBase theTarget, IMutator theMutator, Class<? extends IBaseResource> theResourceType) {
            super(theResourceType);
            myTarget = theTarget;
            myMutator = theMutator;
            assert theResourceType == null || IResource.class.isAssignableFrom(theResourceType);
        }

        // @Override
        // public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
        // super.enteringNewElement(theNamespaceUri, theLocalPart);
        // populateTarget();
        // }

        @Override
        protected void populateTarget() {
            weaveContainedResources();
            if (myMutator != null) {
                myMutator.setValue(myTarget, getCurrentElement());
            }
        }

        @Override
        public void wereBack() {
            super.wereBack();

            IResource nextResource = (IResource) getCurrentElement();
            String version = ResourceMetadataKeyEnum.VERSION.get(nextResource);
            String resourceName = myContext.getResourceDefinition(nextResource).getName();
            String bundleIdPart = nextResource.getId().getIdPart();
            if (isNotBlank(bundleIdPart)) {
                // if (isNotBlank(entryBaseUrl)) {
                // nextResource.setId(new IdDt(entryBaseUrl, resourceName, bundleIdPart, version));
                // } else {
                IdDt previousId = nextResource.getId();
                nextResource.setId(new IdDt(null, resourceName, bundleIdPart, version));
                // Copy extensions
                if (!previousId.getAllUndeclaredExtensions().isEmpty()) {
                    for (final ExtensionDt ext : previousId.getAllUndeclaredExtensions()) {
                        nextResource.getId().addUndeclaredExtension(ext);
                    }
                }
            }
        }

    }

    private class PreResourceStateHl7Org extends ParserState4BundleOfBundles.PreResourceState {

        private IMutator myMutator;
        private IBase myTarget;

        PreResourceStateHl7Org(Class<? extends IBaseResource> theResourceType) {
            super(theResourceType);
        }

        PreResourceStateHl7Org(IBase theTarget, IMutator theMutator, Class<? extends IBaseResource> theResourceType) {
            super(theResourceType);
            myMutator = theMutator;
            myTarget = theTarget;
        }

        @Override
        protected void populateTarget() {
            weaveContainedResources();
            if (myMutator != null) {
                myMutator.setValue(myTarget, getCurrentElement());
            }
        }

        @Override
        public void wereBack() {
            super.wereBack();
            if (getCurrentElement() instanceof IDomainResource) {
                IDomainResource elem = (IDomainResource) getCurrentElement();
                String resourceName = myContext.getResourceDefinition(elem).getName();
                String versionId = elem.getMeta().getVersionId();
                if (StringUtils.isBlank(elem.getIdElement().getIdPart())) {
                    // Resource has no ID
                } else if (StringUtils.isNotBlank(versionId)) {
                    elem.getIdElement().setValue(resourceName + "/" + elem.getIdElement().getIdPart() + "/_history/" + versionId);
                } else {
                    elem.getIdElement().setValue(resourceName + "/" + elem.getIdElement().getIdPart());
                }
            }
        }

    }

    private class PreTagListState extends ParserState4BundleOfBundles.BaseState {

        private TagList myTagList;

        PreTagListState() {
            super(null);
            myTagList = new TagList();
        }

        @Override
        public void endingElement() throws DataFormatException {
            pop();
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            if (!TagList.ELEMENT_NAME_LC.equals(theLocalPart.toLowerCase())) {
                throw new DataFormatException("resourceType does not appear to be 'TagList', found: " + theLocalPart);
            }

            push(new ParserState4BundleOfBundles.TagListState(myTagList));
        }

        @Override
        protected IBase getCurrentElement() {
            return myTagList;
        }

        @Override
        public boolean isPreResource() {
            return true;
        }

    }

    private class PrimitiveState extends ParserState4BundleOfBundles.BaseState {
        private IPrimitiveType<?> myInstance;

        PrimitiveState(ParserState4BundleOfBundles.PreResourceState thePreResourceState, IPrimitiveType<?> theInstance) {
            super(thePreResourceState);
            myInstance = theInstance;
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            if ("value".equals(theName)) {
                if ("".equals(theValue)) {
                    myErrorHandler.invalidValue(null, theValue, "Attribute values must not be empty (\"\")");
                } else {
                    try {
                        myInstance.setValueAsString(theValue);
                    } catch (DataFormatException | IllegalArgumentException e) {
                        myErrorHandler.invalidValue(null, theValue, e.getMessage());
                    }
                }
            } else if ("id".equals(theName)) {
                if (myInstance instanceof IIdentifiableElement) {
                    ((IIdentifiableElement) myInstance).setElementSpecificId(theValue);
                } else if (myInstance instanceof IBaseElement) {
                    ((IBaseElement) myInstance).setId(theValue);
                } else if (myInstance instanceof IBaseResource) {
                    new IdDt(theValue).applyTo((org.hl7.fhir.instance.model.api.IBaseResource) myInstance);
                } else {
                    myErrorHandler.unknownAttribute(null, theName);
                }
            } else {
                super.attributeValue(theName, theValue);
            }
        }

        @Override
        public void endingElement() {
            pop();
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            super.enteringNewElement(theNamespaceUri, theLocalPart);
            push(new ParserState4BundleOfBundles.SwallowChildrenWholeState(getPreResourceState()));
        }

        @Override
        protected IBase getCurrentElement() {
            return myInstance;
        }

    }

    private class ResourceStateHapi extends ParserState4BundleOfBundles.ElementCompositeState {

        private IResource myInstance;

        public ResourceStateHapi(ParserState4BundleOfBundles.PreResourceState thePreResourceState, BaseRuntimeElementCompositeDefinition<?> theDef, IResource theInstance) {
            super(thePreResourceState, theDef.getName(), theDef, theInstance);
            myInstance = theInstance;
        }

        @Override
        public void enteringNewElement(String theNamespace, String theChildName) throws DataFormatException {
            if ("id".equals(theChildName)) {
                push(new ParserState4BundleOfBundles.PrimitiveState(getPreResourceState(), myInstance.getId()));
            } else if ("meta".equals(theChildName)) {
                push(new ParserState4BundleOfBundles.MetaElementState(getPreResourceState(), myInstance.getResourceMetadata()));
            } else {
                super.enteringNewElement(theNamespace, theChildName);
            }
        }
    }

    private class ResourceStateHl7Org extends ParserState4BundleOfBundles.ElementCompositeState {

        ResourceStateHl7Org(ParserState4BundleOfBundles.PreResourceState thePreResourceState, BaseRuntimeElementCompositeDefinition<?> theDef, IBaseResource theInstance) {
            super(thePreResourceState, theDef.getName(), theDef, theInstance);
        }

    }

    private class SecurityLabelElementStateHapi extends ParserState4BundleOfBundles.ElementCompositeState {

        SecurityLabelElementStateHapi(ParserState4BundleOfBundles<T>.PreResourceState thePreResourceState, BaseRuntimeElementCompositeDefinition<?> theDef, IBase codingDt) {
            super(thePreResourceState, theDef.getName(), theDef, codingDt);
        }

        @Override
        public void endingElement() throws DataFormatException {
            pop();
        }

    }

    private class SwallowChildrenWholeState extends ParserState4BundleOfBundles.BaseState {

        private int myDepth;

        SwallowChildrenWholeState(ParserState4BundleOfBundles.PreResourceState thePreResourceState) {
            super(thePreResourceState);
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            // ignore
        }

        @Override
        public void endingElement() throws DataFormatException {
            myDepth--;
            if (myDepth < 0) {
                pop();
            }
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            myDepth++;
        }

        @Override
        public void enteringNewElementExtension(StartElement theElement, String theUrlAttr, boolean theIsModifier, final String baseServerUrl) {
            myDepth++;
        }

    }

    private class TagListState extends ParserState4BundleOfBundles.BaseState {

        private TagList myTagList;

        public TagListState(TagList theTagList) {
            super(null);
            myTagList = theTagList;
        }

        @Override
        public void endingElement() throws DataFormatException {
            pop();
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            if (TagList.ATTR_CATEGORY.equals(theLocalPart)) {
                push(new ParserState4BundleOfBundles.TagState(myTagList));
            } else {
                throw new DataFormatException("Unexpected element: " + theLocalPart);
            }
        }

        @Override
        protected IBase getCurrentElement() {
            return myTagList;
        }

    }

    private class TagState extends ParserState4BundleOfBundles.BaseState {

        private static final int LABEL = 2;
        private static final int NONE = 0;

        private static final int SCHEME = 3;
        private static final int TERM = 1;
        private String myLabel;
        private String myScheme;
        private int mySubState = 0;
        private TagList myTagList;
        private String myTerm;

        public TagState(TagList theTagList) {
            super(null);
            myTagList = theTagList;
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            String value = defaultIfBlank(theValue, null);

            switch (mySubState) {
                case TERM:
                    myTerm = (value);
                    break;
                case LABEL:
                    myLabel = (value);
                    break;
                case SCHEME:
                    myScheme = (value);
                    break;
                case NONE:
                    // This handles JSON encoding, which is a bit weird
                    enteringNewElement(null, theName);
                    attributeValue(null, value);
                    endingElement();
                    break;
            }
        }

        @Override
        public void endingElement() throws DataFormatException {
            if (mySubState != NONE) {
                mySubState = NONE;
            } else {
                if (isNotEmpty(myScheme) || isNotBlank(myTerm) || isNotBlank(myLabel)) {
                    myTagList.addTag(myScheme, myTerm, myLabel);
                }
                pop();
            }
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            /*
             * We allow for both the DSTU1 and DSTU2 names here
             */
            if (Tag.ATTR_TERM.equals(theLocalPart) || "code".equals(theLocalPart)) {
                mySubState = TERM;
            } else if (Tag.ATTR_SCHEME.equals(theLocalPart) || "system".equals(theLocalPart)) {
                mySubState = SCHEME;
            } else if (Tag.ATTR_LABEL.equals(theLocalPart) || "display".equals(theLocalPart)) {
                mySubState = LABEL;
            } else {
                throw new DataFormatException("Unexpected element: " + theLocalPart);
            }
        }

    }

    private class XhtmlState extends ParserState4BundleOfBundles.BaseState {
        private int myDepth;
        private XhtmlDt myDt;
        private List<XMLEvent> myEvents = new ArrayList<XMLEvent>();
        private boolean myIncludeOuterEvent;

        private XhtmlState(ParserState4BundleOfBundles.PreResourceState thePreResourceState, XhtmlDt theXhtmlDt, boolean theIncludeOuterEvent) throws DataFormatException {
            super(thePreResourceState);
            myDepth = 0;
            myDt = theXhtmlDt;
            myIncludeOuterEvent = theIncludeOuterEvent;
        }

        @Override
        public void attributeValue(String theName, String theValue) throws DataFormatException {
            if (myJsonMode) {
                myDt.setValueAsString(theValue);
            } else {
                // IGNORE - don't handle this as an error, we process these as XML events
            }
        }

        protected void doPop() {
            pop();
        }

        @Override
        public void endingElement() throws DataFormatException {
            if (myJsonMode) {
                doPop();
                return;
            }
            super.endingElement();
        }

        @Override
        public void enteringNewElement(String theNamespaceUri, String theLocalPart) throws DataFormatException {
            // IGNORE - don't handle this as an error, we process these as XML events
        }

        @Override
        protected IElement getCurrentElement() {
            return myDt;
        }

        public XhtmlDt getDt() {
            return myDt;
        }

        @Override
        public void xmlEvent(XMLEvent theEvent) {
            if (theEvent.isEndElement()) {
                myDepth--;
            }

            if (myIncludeOuterEvent || myDepth > 0) {
                myEvents.add(theEvent);
            }

            if (theEvent.isStartElement()) {
                myDepth++;
            }

            if (theEvent.isEndElement()) {
                if (myDepth == 0) {
                    String eventsAsString = XmlUtil.encode(myEvents);
                    myDt.setValue(eventsAsString);
                    doPop();
                }
            }
        }
    }

    private class XhtmlStateHl7Org extends ParserState4BundleOfBundles.XhtmlState {
        private IBaseXhtml myHl7OrgDatatype;

        private XhtmlStateHl7Org(ParserState4BundleOfBundles.PreResourceState thePreResourceState, IBaseXhtml theHl7OrgDatatype) {
            super(thePreResourceState, new XhtmlDt(), true);
            myHl7OrgDatatype = theHl7OrgDatatype;
        }

        @Override
        public void doPop() {
            // TODO: this is not very efficient
            String value = getDt().getValueAsString();
            myHl7OrgDatatype.setValueAsString(value);

            super.doPop();
        }
    }

    /**
     * @param theResourceType May be null
     */
    static <T extends IBaseResource> ParserState4BundleOfBundles<T> getPreResourceInstance(IParser theParser, Class<T> theResourceType, FhirContext theContext, boolean theJsonMode, IParserErrorHandler theErrorHandler)
            throws DataFormatException {
        ParserState4BundleOfBundles<T> retVal = new ParserState4BundleOfBundles<T>(theParser, theContext, theJsonMode, theErrorHandler);
        if (theResourceType == null) {
            if (theContext.getVersion().getVersion().isRi()) {
                retVal.push(retVal.new PreResourceStateHl7Org(theResourceType));
            } else {
                retVal.push(retVal.new PreResourceStateHapi(theResourceType));
            }
        } else {
            if (IResource.class.isAssignableFrom(theResourceType)) {
                retVal.push(retVal.new PreResourceStateHapi(theResourceType));
            } else {
                retVal.push(retVal.new PreResourceStateHl7Org(theResourceType));
            }
        }
        return retVal;
    }

    static ParserState4BundleOfBundles<TagList> getPreTagListInstance(IParser theParser, FhirContext theContext, boolean theJsonMode, IParserErrorHandler theErrorHandler) {
        ParserState4BundleOfBundles<TagList> retVal = new ParserState4BundleOfBundles<TagList>(theParser, theContext, theJsonMode, theErrorHandler);
        retVal.push(retVal.new PreTagListState());
        return retVal;
    }
}
