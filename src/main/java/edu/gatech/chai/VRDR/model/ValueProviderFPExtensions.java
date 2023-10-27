package edu.gatech.chai.VRDR.model;

import org.hl7.fhir.r4.elementmodel.Element;
//import org.hl7.fhir.r4.elementmodel.TypedElemental;
//import org.hl7.fhir.r4.utils.FhirPathCompilerCache;
//import org.hl7.fhir.r4.utils.FhirPathEvaluator.EvaluationContext;

import java.util.List;

public interface ValueProviderFPExtensions {
    public static int MAX_FP_EXPRESSION_CACHE_SIZE = new FhirPathCompilerCache().DEFAULT_FP_EXPRESSION_CACHE_SIZE;

    static final Lazy<FhirPathCompilerCache> CACHE = new Lazy<>(() -> new FhirPathCompilerCache(null, MAX_FP_EXPRESSION_CACHE_SIZE));

    public static List<TypedElemental> select(TypedElemental input, String expression, EvaluationContext ctx) {
        return CACHE.getValue().select(input, expression, ctx);
    }

    public static Object scalar(TypedElemental input, String expression, EvaluationContext ctx) {
        return CACHE.getValue().scalar(input, expression, ctx);
    }

    public static boolean predicate(TypedElemental input, String expression, EvaluationContext ctx) {
        return CACHE.getValue().predicate(input, expression, ctx);
    }

    public static boolean isTrue(TypedElemental input, String expression, EvaluationContext ctx) {
        return CACHE.getValue().isTrue(input, expression, ctx);
    }

    public static boolean isBoolean(TypedElemental input, String expression, boolean value, EvaluationContext ctx) {
        return CACHE.getValue().isBoolean(input, expression, value, ctx);
    }

    public static void reInitializeCache(FhirPathCompiler compiler, Integer cacheSize) {
        CACHE.set(new FhirPathCompilerCache(compiler, cacheSize != null ? cacheSize : MAX_FP_EXPRESSION_CACHE_SIZE));
    }
}
