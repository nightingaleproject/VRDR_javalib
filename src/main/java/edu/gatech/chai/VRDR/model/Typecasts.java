package edu.gatech.chai.VRDR.model;
import com.google.common.collect.Iterables;
import edu.gatech.chai.VRDR.model.util.DeathCertificateDocumentUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.*;

import ca.uhn.fhir.model.api.IElement;
import ca.uhn.fhir.model.api.IElement.*;
import org.hl7.fhir.r4.elementmodel.Element;

import org.hl7.fhir.r4.elementmodel.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.hl7.fhir.r4.model.Type.*;
import org.hl7.fhir.r4.model.TypeDetails;
import org.hl7.fhir.r4.model.Enumerations.FHIRAllTypes;
import org.hl7.fhir.r4.elementmodel.ObjectConverter;

import java.util.*;
import java.lang.*;
import java.math.*;



import org.hl7.fhir.r4.elementmodel.Element;
//import org.hl7.fhir.r4.model.api.*;
//import org.hl7.fhir.r4.elementmodel.Manager.FhirFormat. .ITypedElement;
import org.hl7.fhir.r4.elementmodel.Element.ICodingImpl.*; //.Types;
//import org.hl7.fhir.r4.elementmodel.Types;
//import org.hl7.fhir.r4.hapi.fluentpath.FluentPathR4.
//import ca.uhn.fhir.fluentpath.
//import org.hl7.fhir.r4.fhirpath;


import org.hl7.fhir.r4.model.*;
public class Typecasts
{
    static List<String> fhirPathNames = new ArrayList<>() {{
        add("model.BooleanType");
        add("model.IntegerType");
        add("model.DecimalType");
        add("model.StringType");
        add("model.DateTimeType");
        add("model.DateType");
        add("model.TimeType");
        add("model.Quantity");
        add("model.UriType");
        add("model.Identifier");
        add("model.CodeType");
        add("model.Coding");
        add("model.CodeableConcept");
        add("model.Reference");
        add("model.InstantType");
        add("model.Base64BinaryType");
        add("model.IdType");
        add("model.UuidType");
        add("model.OidType");
        add("model.MarkdownType");
        add("model.UnsignedIntType");
        add("model.PositiveIntType");
        add("model.Ratio");
        add("model.Annotation");
        add("model.SampledData");
        add("model.Signature");
        add("model.Meta");
    }};

    //public delegate Object Cast(Object source);


    public interface Cast { Object cast(Object source); }

    //private static Object id(Object source) -> source;
    private static Object id(Object source) {
        return source;
    }

   // private static Cast makeNativeCast(Class to) -> source -> Convert.ChangeType(source, to);
//    private static <T> Cast makeNativeCast(Class<T> to) {
//        return source -> (T) Convert.ChangeType(source, to);
//    }

    private static Cast makeNativeCast(Class<?> to) {
        return source -> to.cast(source);
    }


//    public static <T> T makeNativeCast(Object source, Class<T> to) {
//        return to.cast(source);
//    }

   // private static TypedElemental any2primitiveTypedElement(Object source) -> ElementNode.ForPrimitive(source);
    private static TypedElemental any2primitiveTypedElement(Object source) {
        return ElementNode.ForPrimitive(source);
    }

//    private static IPrimitiveType<?> any2primitiveTypedElement(Object source) {
//        return (IPrimitiveType<?>) ElementFactory.createPrimitive(source.toString());
//    }

   // private static List<TypedElemental> any2List(Object source) -> ElementNode.CreateList(source);
    private static List<TypedElemental> any2List(Object source) {
        return ElementNode.CreateList((List) source);
    }

//    private static List<TypedElemental> any2List(Object source) {
//        return ElementFactory.createList(source);
//    }

    //private static P.Quantity tryQuantity(Object source)
    private static Quantity tryQuantity(Object source)
        {
            if (source instanceof TypedElemental element) {
                if (element.InstanceType.equals("Quantity")) {
                    // Need to downcast from a FHIR Quantity to a System.Quantity
                    return ParseQuantity(element);
                } else
                    throw new ClassCastException(new StringBuffer("Cannot convert from '").append(element.InstanceType).append("' to Quantity").toString());
            }

            throw new ClassCastException(new StringBuffer("Cannot convert from '").append(source.getClass()).append("' to Quantity").toString());
        }

    public static Quantity ParseQuantity1(TypedElemental qe) {
        BigDecimal value = qe.Children("value").stream().findFirst().map(e -> (BigDecimal) e.getValue()).orElse(null);
        if (value == null) {
            return null;
        }
        String unit = qe.Children("code").stream().findFirst().map(e -> (String) e.getValue()).orElse(null);
        return new Quantity().setValue(value).setUnit(unit);
    }

        public static Quantity ParseQuantity(TypedElemental qe)
        {
//            var value = qe.Children("value").SingleOrDefault() ?.Value as decimal ?;
//            if (value == null) return null;

            BigDecimal value = null;
            Element valueElement = (Element) qe.Children("value").get(0);
            if (valueElement != null)
            {
                String valueString = valueElement.toString();
                if (valueString != null)
                {
                    try
                    {
                        value = new BigDecimal(valueString);
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
            }
            if (value == null)
            {
                return null;
            }

//            var unit = qe.Children("code").SingleOrDefault() ?.Value as string;
//            return new P.Quantity(value.Value, unit);

            Element unitElement = (Element) qe.Children("code").get(0); // .getChild("code");
            String unit = (unitElement != null) ? unitElement.getValue() : null;
            return new Quantity().setValue(value).setUnit(unit);
        }

    private static Quantity parseQuantity2(TypedElemental qe) {
        BigDecimal value = qe.Children("value").stream()
                .findFirst()
                .map(TypedElemental::getValue)
                .map(Object::toString)
                .map(BigDecimal::new)
                .orElse(null);
        if (value == null) {
            return null;
        }
        String unit = qe.Children("code").stream()
                .findFirst()
                .map(TypedElemental::getValue)
                .map(Object::toString)
                .orElse(null);
        return new Quantity().setValue(value).setUnit(unit);
    }

        private static Cast getImplicitCast (Object f, Class<?> to)
        {
            Class<?> from = f.getClass();

            if (to == Object.class) return Typecasts::id;
            if (from.isAssignableFrom(to)) {
                return Typecasts::id;
            }
            //List<TypedElemental> list = new ArrayList<>();
            boolean fromElemList = List.class.isAssignableFrom(from);
            //if (to instanceof Quantity.class && from instanceof TypedElemental.class return tryQuantity;
            if (to == Quantity.class && TypedElemental.class.isAssignableFrom(from))
            {
                return o -> Typecasts.tryQuantity(o); //Typecasts::tryQuantity;
            }

            if (to == TypedElemental.class && (!fromElemList)) return o -> Typecasts.any2primitiveTypedElement(o);
            if (to == List.class) return o -> Typecasts.any2List(o);


//            if (from == long.class && to == BigDecimal.class || to == BigDecimal.class)
//                return makeNativeCast(BigDecimal.class);
//            if (from == Long.class && to == BigDecimal.class) return makeNativeCast(BigDecimal.class);
            if(from == long.class || from == Long.class)
            {
                if(to == BigDecimal.class)
                    return makeNativeCast(BigDecimal.class);
            }


//            if (from == int.class && (to == BigDecimal.class || to == BigDecimal.class))
//                return makeNativeCast(BigDecimal.class);
//            if (from == Integer.class && to == BigDecimal.class) return makeNativeCast(BigDecimal.class);
            if(from == int.class || from == Integer.class)
            {
                if(to == BigDecimal.class)
                    return makeNativeCast(BigDecimal.class);
            }

            if (from == Integer.class && to == Long.class) {
                return Typecasts.makeNativeCast(Long.class);
            }
            if (from == Integer.class && to == Long.class) {
                return Typecasts.makeNativeCast(Long.class);
            }

            // cast ints to longs
            if (from == int.class || from == Integer.class && (to == long.class || to == Long.class)) return makeNativeCast(Long.class);


            //if (typeof(P.Any).IsAssignableFrom(to) && !fromElemList) {

            if (FHIRAllTypes.ANY.getClass().isAssignableFrom(to) && !fromElemList) {
                if (f instanceof TypedElemental te && te.InstanceType.equals("Quantity"))
                    return o -> ParseQuantity((TypedElemental) o);
                return makeNativeCast(FHIRAllTypes.ANY.getClass());//  o -> FHIRAllTypes.ANY. .convert(o);
            }

            return null;
        }

    private static Object unboxTo(Object instance, Class<?> to) {
        if (instance == null) {
            return null;
        }
        if (instance instanceof Iterable<?>) {
            //Iterable<?> list = (Iterable<?>) instance;
            Iterable<TypedElemental> list = (Iterable<TypedElemental>) instance;
            if (to.isAssignableFrom(Iterable.class)) {
                return instance;
            }
            if (!list.iterator().hasNext()) {
                return null;
            }
            if (Iterables.size(list) == 1) {
                instance = list.iterator().next();
            }
        }
        if (instance instanceof TypedElemental) {
            TypedElemental element = (TypedElemental) instance;
            if (to.isAssignableFrom(TypedElemental.class)) {
                return instance;
            }
            if (to == Object.class) {
                return instance;
            }

            boolean isPrimitive = element.getValue() != null || (element.InstanceType != null && Character.isLowerCase(element.InstanceType.charAt(0)) || element.InstanceType.startsWith("System."));
            //if (element.getValue() != null || (element != null && Character.isLowerCase(element.toString().charAt(0)) || element.toString().startsWith("System."))) {

            if (isPrimitive) {
                instance = element.getValue();
            }
        }

        return instance;
    }

        public static boolean CanCastTo (Object source, Class<?> to)
        {
            if (source == null)
                return DeathCertificateDocumentUtil.isNullable(to); //to.isNullable();
            Object from = unboxTo(source, to);
            return from == null ? DeathCertificateDocumentUtil.isNullable(to) : getImplicitCast(from, to) != null;
        }

    public static boolean isOfExactType(Object source, Class<?> to) {
        if (source == null) {
            return to.isPrimitive() || to.isAssignableFrom(Object.class);
        }
        Object from = unboxTo(source, to);
        if (from == null) {
            return to.isPrimitive() || to.isAssignableFrom(Object.class);
        }
        if (to == Object.class) {
            return true;
        }
        Class<?> fromType = from.getClass();
        return fromType == to;
    }


    //public static boolean CanCastTo(Type from, Type to) -> getImplicitCast(from, to) != null;

    //public static T CastTo<T > (Object source) -> (T) CastTo(source, typeof(T));
    //public static <T> T CastTo(Object source) { return (T) CastTo(source, T.class); }

//    public static <T> T castTo(Object source, Class<T> to) {
//        return to.cast(castTo(source, (Class<?>) to));
//    }


    public static <T> T castTo(Object source, Class<T> to) {
        if (source != null) {
            if (to.isAssignableFrom(source.getClass())) {
                return to.cast(source); // source
            }
            source = unboxTo(source, to);
            if (source != null) {
                Cast cast = getImplicitCast(source, to);
                if (cast == null) {
                    String message = String.format("Cannot cast from '%s' to '%s'",
                            Typecasts.readableFhirPathName(source), Typecasts.readableTypeName(to));
                    throw new ClassCastException(message);
                }
                return to.cast(cast.cast(source)); // cast.apply(source);
            }
        }

        //if source == null, or unboxed source == null....
        if (Iterable.class.isAssignableFrom(to)) {
            return (T) ElementNode.EmptyList;//to.cast(ElementFactory.emptyList());
        }
        if (to.isPrimitive() || to.isAssignableFrom(Object.class) || to.isAssignableFrom(Void.class)) {
            return null;
        } else {
            throw new ClassCastException("Cannot cast a null value to non-nullable type '" + to.getSimpleName() + "'");
        }
    }

    public static boolean isNullable(Class<?> type) {
        return Optional.class.isAssignableFrom(type);
    }

    public static String readableFhirPathName(Object value) {
        if (value instanceof Iterable<?>) {
            Iterable<?> iterable = (Iterable<?>) value;
            List<?> values = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
            Set<String> types = values.stream().map(Typecasts::readableFhirPathName).collect(Collectors.toSet());

            return values.size() > 1 ? "collection of " + String.join("/", types) : types.iterator().next();
        } else if (value instanceof ICompositeType) {
            return ((ICompositeType) value).fhirType();
        } else if (value instanceof IPrimitiveType) {
            return ((IPrimitiveType<?>) value).fhirType();//.getElementType().name();
        } else {
            return value.getClass().getSimpleName();
        }
    }

    public static String readableTypeName(Class<?> type) {
        //List<String> matchingStrings = fhirPathNames.stream().filter(s -> s.contains(type.getName())).toList();
        for (String fhirPathName : fhirPathNames) {
            if (type.getName().contains(fhirPathName)){
                return "FhirPath type " + type.getSimpleName();
            }
        }
        if (Iterable.class.isAssignableFrom(type) && TypedElemental.class.isAssignableFrom(type)) {
            return "collection";
        } else if (TypedElemental.class.isAssignableFrom(type)) {
            return "any type";
        } else {
            return ".NET type " + type.getSimpleName();
        }
    }
    }
