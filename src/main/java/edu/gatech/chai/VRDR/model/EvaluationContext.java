package edu.gatech.chai.VRDR.model;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EvaluationContext
{
    public static EvaluationContext createDefault() {
        return new EvaluationContext();
    }

    public EvaluationContext()
    {
        // no defaults yet
    }

    /// <summary>
    /// Create an EvaluationContext with the given value for <c>%resource</c>.
    /// </summary>
    /// <param name="resource">The data that will be represented by %resource</param>
    //public EvaluationContext(TypedElemental resource) : this(resource, null) { }
    public EvaluationContext(TypedElemental resource) {
        this(resource, null);
    }

    /// <summary>
    /// Create an EvaluationContext with the given value for <c>%resource</c> and <c>%rootResource</c>.
    /// </summary>
    /// <param name="resource">The data that will be represented by <c>%resource</c>.</param>
    /// <param name="rootResource">The data that will be represented by <c>%rootResource</c>.</param>
    public EvaluationContext(TypedElemental resource, TypedElemental rootResource)
    {
        Resource = resource;
        RootResource = (rootResource != null) ? rootResource : resource;
    }
    
    /// <summary>
    /// The data represented by <c>%rootResource</c>.
    /// </summary>
    public TypedElemental RootResource;
    public TypedElemental getRootResource() {
        return RootResource;
    }

    public void setRootResource(TypedElemental rootResource) {
        RootResource = rootResource;
    }

    
    /// <summary>
    /// The data represented by <c>%resource</c>.
    /// </summary>
    public TypedElemental Resource;
    public TypedElemental getResource() {
        return Resource;
    }

    public void setResource(TypedElemental resource) {
        Resource = resource;
    }

    /// <summary>
    /// A delegate that handles the output for the <c>trace()</c> function.
    /// </summary>
    public BiConsumer<String, Iterable<TypedElemental>> Tracer;

    public BiConsumer<String, Iterable<TypedElemental>> getTracer() {
        return Tracer;
    }

    public void setTracer(BiConsumer<String, Iterable<TypedElemental>> tracer) {; //<String, Iterable<TypedElemental>> tracer) {
        Tracer = tracer;
    }

       // #region Obsolete members
      //  [Obsolete("Please use CreateDefault() instead of this member, which may cause raise conditions.")]
    public static final EvaluationContext Default = new EvaluationContext();
       // #endregion
}
