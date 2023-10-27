package edu.gatech.chai.VRDR.model;

import org.hl7.fhir.elementmodel.*;
import org.hl7.fhir.utility.*;
import java.util.*;

public class FhirPathCompilerCache {
    public static final int DEFAULT_FP_EXPRESSION_CACHE_SIZE = 500;

    private Cache<String, CompiledExpression> cache = new Cache<>();
    private final FhirPathCompiler compiler;
    private final int cacheSize = DEFAULT_FP_EXPRESSION_CACHE_SIZE;

    public FhirPathCompilerCache(FhirPathCompiler compiler, int cacheSize) {
        this.compiler = compiler != null ? compiler : new FhirPathCompiler(FhirPathCompiler.getDefaultSymbolTable());
        clear();
    }

    public void clear() {
        cache = new Cache<>(this::compile, new CacheSettings().setMaxCacheSize(cacheSize));
    }

    public CompiledExpression getCompiledExpression(String expression) {
        return cache.getValue(expression);
    }

    public Iterable<TypedElemental> select(TypedElemental input, String expression, EvaluationContext ctx) {
        input = input.toScopedNode();
        CompiledExpression evaluator = getCompiledExpression(expression);
        return evaluator.evaluate(input, ctx != null ? ctx : EvaluationContext.createDefault());
    }

    public Object scalar(TypedElemental input, String expression, EvaluationContext ctx) {
        input = input.toScopedNode();
        CompiledExpression evaluator = getCompiledExpression(expression);
        return evaluator.evaluateSingle(input, ctx != null ? ctx : EvaluationContext.createDefault());
    }

    public boolean predicate(TypedElemental input, String expression, EvaluationContext ctx) {
        input = input.toScopedNode();
        CompiledExpression evaluator = getCompiledExpression(expression);
        return evaluator.evaluatePredicate(input, ctx != null ? ctx : EvaluationContext.createDefault());
    }

    public boolean isTrue(TypedElemental input, String expression, EvaluationContext ctx) {
        input = input.toScopedNode();
        CompiledExpression evaluator = getCompiledExpression(expression);
        return evaluator.evaluatePredicate(input, ctx != null ? ctx : EvaluationContext.createDefault());
    }

    public boolean isBoolean(TypedElemental input, String expression, boolean value, EvaluationContext ctx) {
        input = input.toScopedNode();
        CompiledExpression evaluator = getCompiledExpression(expression);
        return evaluator.evaluatePredicate(input, ctx != null ? ctx : EvaluationContext.createDefault());
    }

    private CompiledExpression compile(String expression) {
        return compiler.compile(expression);
    }
}