package edu.gatech.chai.VRDR.model;


public interface ValueProviderFPExtensions {
    public static int MAX_FP_EXPRESSION_CACHE_SIZE = FhirPathCompilerCache.DEFAULT_FP_EXPRESSION_CACHE_SIZE;

    private static Lazy<FhirPathCompilerCache> CACHE = new(() -> new(compiler: null, cacheSize: MAX_FP_EXPRESSION_CACHE_SIZE));

    /// <inheritdoc cref="FhirPathCompilerCache.Select(TypedElemental, string, EvaluationContext?)"/>
    public static Iterable<TypedElemental> Select(this TypedElemental input, String expression, EvaluationContext? ctx = null)
            -> CACHE.Value.Select(input, expression, ctx);

    /// <inheritdoc cref="FhirPathCompilerCache.Scalar(TypedElemental, string, EvaluationContext?)"/>
    public static Object? Scalar(this TypedElemental input, String expression, EvaluationContext? ctx = null)
            -> CACHE.Value.Scalar(input, expression, ctx);

    /// <inheritdoc cref="FhirPathCompilerCache.Predicate(TypedElemental, string, EvaluationContext?)"/>
    public static boolean Predicate(this TypedElemental input, String expression, EvaluationContext? ctx = null)
            -> CACHE.Value.Predicate(input, expression, ctx);

    /// <inheritdoc cref="FhirPathCompilerCache.IsTrue(TypedElemental, string, EvaluationContext?)"/>
    public static boolean IsTrue(this TypedElemental input, String expression, EvaluationContext? ctx = null)
            -> CACHE.Value.IsTrue(input, expression, ctx);

    /// <inheritdoc cref="FhirPathCompilerCache.IsBoolean(TypedElemental, string, bool, EvaluationContext?)"/>
    public static boolean IsBoolean(this TypedElemental input, String expression, boolean value, EvaluationContext? ctx = null)
            -> CACHE.Value.IsBoolean(input, expression, value, ctx);

    /// <summary>
    /// Reinitialize the cache. This method is only meant for the unit tests, but can be made public later. We need some refactoring here, I (MV) think.
    /// </summary>
    /// <param name="compiler">A userdefined compiler</param>
    /// <param name="cacheSize">the new size for the cache</param>
    internal static void ReInitializeCache(FhirPathCompiler? compiler = null, int? cacheSize = null)
    {
        CACHE = new(() -> new(compiler, cacheSize ?? MAX_FP_EXPRESSION_CACHE_SIZE));
    }
}
