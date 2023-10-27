package edu.gatech.chai.VRDR.model;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.hl7.fhir.r4.elementmodel.*;
import org.hl7.fhir.r4.model.*;

@FunctionalInterface
public interface Invokee
{
    java.lang.Iterable<TypedElemental> invoke(Closure context, java.lang.Iterable<Invokee> arguments);
}

public static class InvokeeFactory {
        public static final List<Invokee> EmptyArgs = Collections.emptyList();

        public static List<TypedElemental> GetThis(Closure context, List<Invokee> _) {
            return context.GetThis();
        }

        public static List<TypedElemental> GetTotal(Closure context, List<Invokee> _) {
            return context.GetTotal();
        }

        public static List<TypedElemental> GetContext(Closure context, List<Invokee> _) {
            return context.GetOriginalContext();
        }

        public static List<TypedElemental> GetResource(Closure context, List<Invokee> _) {
            return context.getResource();
        }

        public static List<TypedElemental> GetRootResource(Closure context, List<Invokee> arguments) {
            return context.getRootResource();
        }

        public static List<TypedElemental> GetThat(Closure context, List<Invokee> _) {
            return context.GetThat();
        }

        public static List<TypedElemental> GetIndex(Closure context, List<Invokee> args) {
            return context.getIndex();
        }

        public static Invokee Wrap(Function<Object[], Object> func) {
            return (ctx, args) -> Typecasts.castTo(func.apply(new Object[0]), List.class, TypedElemental.class);
        }

        public static <A, R> Invokee Wrap(Function<A, R> func, boolean propNull) {
            return (ctx, args) -> {
                if (A.class != EvaluationContext.class) {
                    List<TypedElemental> focus = args.iterator().next().apply(ctx, EmptyArgs);
                    if (propNull && !focus.iterator().hasNext())
                        return Collections.emptyList();

                    return Typecasts.castTo(func.apply(Typecasts.castTo(focus.iterator().next(), A.class)), List.class, TypedElemental.class);
                } else {
                    A lastPar = (A) ctx.getEvaluationContext();
                    return Typecasts.castTo(func.apply(lastPar), List.class, TypedElemental.class);
                }
            };
        }

        public static <A, B, C, R> Invokee WrapWithPropNullForFocus(Function3<A, B, C, R> func) {
            return (ctx, args) -> {
                // propagate only null for focus
                List<TypedElemental> focus = args.iterator().next().apply(ctx, EmptyArgs);
                if (!focus.iterator().hasNext())
                    return Collections.emptyList();

                return Wrap(func, false).apply(ctx, args);
            };
        }

        public static <A, B, R> Invokee Wrap(Function2<A, B, R> func, boolean propNull) {
            return (ctx, args) -> {
                List<TypedElemental> focus = args.iterator().next().apply(ctx, EmptyArgs);
                if (propNull && !focus.iterator().hasNext())
                    return Collections.emptyList();

                if (B.class != EvaluationContext.class) {
                    List<TypedElemental> argA = args.iterator().next().apply(ctx, EmptyArgs);
                    if (propNull && !argA.iterator().hasNext())
                        return Collections.emptyList();

                    return Typecasts.castTo(func.apply(Typecasts.castTo(focus.iterator().next(), A.class), Typecasts.castTo(argA.iterator().next(), B.class)), List.class, TypedElemental.class);
                } else {
                    B lastPar = (B) ctx.getEvaluationContext();
                    return Typecasts.castTo(func.apply(Typecasts.castTo(focus.iterator().next(), A.class), lastPar), List.class, TypedElemental.class);
                }
            };
        }
    }

