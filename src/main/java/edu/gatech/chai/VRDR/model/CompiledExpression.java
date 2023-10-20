package edu.gatech.chai.VRDR.model;

import java.util.List;
import java.util.stream.Collectors;

public interface CompiledExpression {
    //public List<TypedElemental> CompiledExpression(TypedElemental root, EvaluationContext ctx);

    public List<TypedElemental> evaluate(TypedElemental root, EvaluationContext ctx);


    public static class CompiledExpressionExtensions
    {
        /// <summary>
        /// Evaluates an expression against a given context and returns a single result
        /// </summary>
        /// <param name="evaluator">Expression which is to be evaluated</param>
        /// <param name="input">Input at which the expression is evaluated</param>
        /// <param name="ctx">Context of the evaluation</param>
        /// <returns>The single result of an expression</returns>
        //public static Object? Scalar(this CompiledExpression evaluator, TypedElemental input, EvaluationContext ctx)
//       {
//        var result = evaluator(input, ctx).Take(2).ToArray();
//        return result.Any() ? result.Single().Value : null;
//        }
        public static Object scalar(CompiledExpression evaluator, TypedElemental input, EvaluationContext ctx)
        {
            List result = evaluator.evaluate(input, ctx).subList(0, 2); //.take(2).toArray();
            return result.size() > 0 ? result.get(0) : null;
        }


        /// <summary>
        /// Evaluates an expression and returns true for expression being evaluated as true or empty, otherwise false.
        /// </summary>
        /// <param name="evaluator">Expression which is to be evaluated</param>
        /// <param name="input">Input at which the expression is evaluated</param>
        /// <param name="ctx">Context of the evaluation</param>
        /// <returns>True if expression returns true of empty, otheriwse false</returns>
        public static boolean predicate(CompiledExpression evaluator, TypedElemental input, EvaluationContext ctx)
        {
            Boolean result = (Boolean) evaluator.evaluate(input, ctx).get(0).getValue();
            return result == null || result.booleanValue();
        }

        /// <summary>
        /// Evaluates an expression and returns true for expression being evaluated as true, and false if the expression returns false or empty.
        /// </summary>
        /// <param name="evaluator">Expression which is to be evaluated</param>
        /// <param name="input">Input at which the expression is evaluated</param>
        /// <param name="ctx">Context of the evaluation</param>
        /// <returns>True if expression returns true , and false if expression returns empty of false.</returns>
        public static boolean isTrue(CompiledExpression evaluator, TypedElemental input, EvaluationContext ctx) {
            Boolean result = (Boolean) evaluator.evaluate(input, ctx).get(0).getValue();
            return result != null && result.booleanValue();
        }

        /// <summary>
        /// Evaluates if the result of an expression is equal to a given boolean.
        /// </summary>
        /// <param name = "evaluator"> Expression which is to be evaluated</param>
        /// <param name="value">boolean that is to be compared to the result of the expression</param>
        /// <param name="input">Input at which the expression is evaluated</param>
        /// <param name="ctx">Context of the evaluation</param>
        /// <returns>True if the result of an expression is equal to a given boolean, otherwise false</returns>
        public static boolean isBoolean(CompiledExpression evaluator, boolean value, TypedElemental input, EvaluationContext ctx)
        {
            Boolean result = (Boolean) evaluator.evaluate(input, ctx).get(0).getValue();
            return result != null && result.booleanValue() == value;
        }
    }
}
