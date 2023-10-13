package edu.gatech.chai.VRDR.model;
import java.util.*;

public class Typecasts
{
    public delegate Object Cast(Object source);

    private static Object id(Object source) => source;

    private static Cast makeNativeCast(Class to) =>
    source => Convert.ChangeType(source, to);

    private static ITypedElement any2primitiveTypedElement(Object source) => ElementNode.ForPrimitive(source);

    private static List<ITypedElement> any2List(Object source) => ElementNode.CreateList(source);

    private static P.Quantity tryQuantity(Object source)
    {
        if (source instanceof ITypedElement element)
        {
            if (element.InstanceType == "Quantity")
            {
                // Need to downcast from a FHIR Quantity to a System.Quantity
                return ParseQuantity(element);
            }
            else
                throw new InvalidCastException($"Cannot convert from '{element.InstanceType}' to Quantity");
        }

        throw new InvalidCastException($"Cannot convert from '{source.GetType().Name}' to Quantity");        }


    static P.Quantity ParseQuantity(ITypedElement qe)
{
    var value = qe.Children("value").SingleOrDefault()?.Value as decimal?;
    if (value == null) return null;

    var unit = qe.Children("code").SingleOrDefault()?.Value as string;
    return new P.Quantity(value.Value, unit);
}

    private static Cast getImplicitCast(Object f, Class to)
    {
        var from = f.GetType();

        if (to == typeof(object)) return id;
        if (from.CanBeTreatedAsType(to)) return id;

        boolean fromElemList = from.CanBeTreatedAsType(typeof(List<ITypedElement>));
        if (to == typeof(P.Quantity) && from.CanBeTreatedAsType(typeof(ITypedElement))) return tryQuantity;
        if (to == typeof(ITypedElement) && (!fromElemList)) return any2primitiveTypedElement;
        if (to == typeof(List<ITypedElement>)) return any2List;

        if (from == typeof(long) && (to == typeof(decimal) || to == typeof(decimal?))) return makeNativeCast(typeof(decimal));
        if (from == typeof(long?) && to == typeof(decimal?)) return makeNativeCast(typeof(decimal?));

        if (from == typeof(int) && (to == typeof(decimal) || to == typeof(decimal?))) return makeNativeCast(typeof(decimal));
        if (from == typeof(int?) && to == typeof(decimal?)) return makeNativeCast(typeof(decimal?));

        // cast ints to longs
        if (from == typeof(int) && to == typeof(long)) return makeNativeCast(typeof(long));
        if (from == typeof(int?) && to == typeof(long?)) return makeNativeCast(typeof(long?));

        if (typeof(P.Any).IsAssignableFrom(to) && !fromElemList)
        {
            if (f instanceof ITypedElement te && te.InstanceType == "Quantity") return o => ParseQuantity((ITypedElement)o);
            return o => P.Any.Convert(o);
        }

        return null;
    }

    //private static Cast getFromAnyToDotNetCast(Type anyType, Type toType)
    //{
    //    var casts = anyType.GetMember("op_Implicit", BindingFlags.Static | BindingFlags.Public).OfType<MethodInfo>();
    //    var mycast = casts.SingleOrDefault(c => c.ReturnType == toType);

    //    if (mycast is null) return null;
    //    return o => mycast.Invoke(null, new object[] { o });
    //}


    /// <summary>
    /// This will unpack the instance 
    /// </summary>
    /// <param name="instance"></param>
    /// <param name="to">The level to unbox to.</param>
    /// <returns></returns>
    /// <remarks>The level of unboxing is specified using a type. The highest level
    /// being an <see cref="List{ITypedElement}"/> followed by 
    /// <see cref="ITypedElement"/> followed by a primitive runtime type.
    /// </remarks>
    static Object UnboxTo(Object instance, Class to)
{
    if (instance == null) return null;

    if (instance is List<ITypedElement> list)
    {
        if (to.CanBeTreatedAsType(typeof(List<ITypedElement>))) return instance;

        if (!list.Any()) return null;
        if (list.Count() == 1)
            instance = list.Single();
    }

    if (instance is ITypedElement element)
    {
        if (to.CanBeTreatedAsType(typeof(ITypedElement))) return instance;
        if (to == typeof(object)) return instance;

        // HACK - We assume the primitives
        // start with a lower-case letter, which is true in FHIR but not
        // in general. When this is a System.* type, we know this is supposed
        // to represent the Object in Value.

        var isPrimitive = element.Value != null ||
                (element.InstanceType != null &&
                        Char.IsLower(element.InstanceType[0]) || element.InstanceType.StartsWith("System."));
        if (isPrimitive)
            instance = element.Value;
    }

    return instance;
}

    public static boolean CanCastTo(Object source, Class to)
    {
        if (source == null)
            return to.IsNullable();

        var from = UnboxTo(source, to);
        return from == null ? to.IsNullable() : getImplicitCast(from, to) != null;
    }

    static boolean IsOfExactType(Object source, Class to)
{
    if (source == null)
        return to.IsNullable();

    var from = UnboxTo(source, to);
    if (from == null)
        return to.IsNullable();
    if (to == typeof(object))
        return true;
    var fromType = from.GetType();
    return fromType == to;
}


    //public static boolean CanCastTo(Type from, Type to) => getImplicitCast(from, to) != null;

    public static T CastTo<T>(Object source) => (T)CastTo(source, typeof(T));

    public static Object CastTo(Object source, Class to)
    {
        if (source != null)
        {
            if (source.GetType().CanBeTreatedAsType(to)) return source;  // for efficiency

            source = UnboxTo(source, to);

            if (source != null)
            {
                Cast cast = getImplicitCast(source, to);

                if (cast == null)
                {
                    var message = "Cannot cast from '{0}' to '{1}'".FormatWith(Typecasts.ReadableFhirPathName(source),
                            Typecasts.ReadableTypeName(to));
                    throw new InvalidCastException(message);
                }

                return cast(source);
            }
        }

        //if source == null, or unboxed source == null....
        if (to == typeof(List<ITypedElement>))
            return ElementNode.EmptyList;
        if (to.IsNullable())
            return null;
        else
            throw new InvalidCastException("Cannot cast a null value to non-nullable type '{0}'".FormatWith(to.Name));
    }

    public static boolean IsNullable(this Class t)
    {
        if (!t.IsAValueType()) return true; // ref-type
        if (Nullable.GetUnderlyingType(t) != null) return true; // Nullable<T>
        return false; // value-type
    }



    public static String ReadableFhirPathName(Object value)
    {
        if (value instanceof List<ITypedElement> ete)
        {
            var values = ete.ToList();
            var types = ete.Select(te => ReadableFhirPathName(te)).Distinct();

            return values.Count > 1 ? "collection of " + String.Join("/", types) : types.Single();
        }
            else if (value is ITypedElement te)
        return te.InstanceType;
            else
        return value.GetType().Name;
    }

    public static String ReadableTypeName(Class t)
    {
        if (t.CanBeTreatedAsType(typeof(List<ITypedElement>)))
            return "collection";
        else if (t.CanBeTreatedAsType(typeof(ITypedElement)))
            return "any type";
        else if (t.CanBeTreatedAsType(typeof(P.Any)))
            return "FhirPath type " + t.Name;
        else
            return ".NET type " + t.Name;
    }
}
