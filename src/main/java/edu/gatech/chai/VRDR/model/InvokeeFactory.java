package edu.gatech.chai.VRDR.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
//import org.hl7.fhir.r4.elementmodel.Closure;
//import org.hl7.fhir.r4.elementmodel.TypedElemental;
//import org.hl7.fhir.r4.elementmodel.Type;
import org.hl7.fhir.r4.elementmodel.ObjectConverter;
import org.hl7.fhir.r4.elementmodel.Element;
//import org.hl7.fhir.r4.elementmodel.ElementType;
import org.hl7.fhir.r4.elementmodel.Property;
//import org.hl7.fhir.r4.elementmodel.Typecasts;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Function.*;

public class InvokeeFactory {
    public static final List<Invokee> EMPTY_ARGS = new ArrayList<Invokee>();

    public static List<TypedElemental> getThis(Closure context, List<Invokee> _) {
        return context.resolveValue("this");
    }

    public static List<TypedElemental> getTotal(Closure context, List<Invokee> _) {
        return context.resolveValue("total");
    }

    public static List<TypedElemental> getContext(Closure context, List<Invokee> _) {
        return context.resolveValue("context");
    }

    public static List<TypedElemental> getResource(Closure context, List<Invokee> _) {
        return context.resolveValue("resource");
    }

    public static List<TypedElemental> getRootResource(Closure context, List<Invokee> arguments) {
        return context.resolveValue("rootResource");
    }

    public static List<TypedElemental> getThat(Closure context, List<Invokee> _) {
        return context.resolveValue("that");
    }

    public static List<TypedElemental> getIndex(Closure context, List<Invokee> args) {
        return context.resolveValue("index");
    }

    public static Invokee wrap(Func<?> func) {
        return (context, args) -> {
            return Typecasts.castTo(func.invoke());
        };
    }

    public static <A, R> Invokee wrap(Func1<A, R> func1, boolean propNull) {
        return (context, args) -> {
            if (A.class != EvaluationContext.class) {
                Invokee invokee = args.iterator().next();
                Iterable<TypedElemental> focus = invokee.invoke(context, InvokeeFactory.EMPTY_ARGS);

                //Iterable<TypedElemental> focus = args.iterator().next().invoke((Closure)context, InvokeeFactory.EMPTY_ARGS);
                if (propNull && !focus.iterator().hasNext()) {
                    return new ArrayList<>();
                }
                return Typecasts.castTo(func1.invoke(Typecasts.castTo(focus.iterator().next(), A.));
            } else {
                A lastPar = (A) context.getEvaluationContext();
                return Typecasts.castTo(func1.invoke(lastPar));
            }
        };
    }

    public static <A, B, R> Invokee wrap(Func2<A, B, R> func, boolean propNull) {
        return (context, args) -> {
            Iterable<TypedElemental> focus = args.iterator().next().invoke(context, InvokeeFactory.EMPTY_ARGS);
            if (propNull && !focus.iterator().hasNext()) {
                return new ArrayList<>();
            }
            if (B.class != EvaluationContext.class) {
                Iterable<TypedElemental> argA = args.iterator().next().invoke(context, InvokeeFactory.EMPTY_ARGS);
                if (propNull && !argA.iterator().hasNext()) {
                    return new ArrayList<>();
                }
                return Typecasts.castTo(func.invoke(Typecasts.castTo(focus.iterator().next()), Typecasts.castTo(argA.get(0))));
            } else {
                B lastPar = (B) context.getEvaluationContext();
                return Typecasts.castTo(func.invoke(Typecasts.castTo(focus.get(0)), lastPar));
            }
        };
    }

    public static <A, B, C, R> Invokee wrapWithPropNullForFocus(Func3<A, B, C, R> func) {
        return (context, args) -> {
            Iterable<TypedElemental> focus = args.iterator().next().invoke(context, InvokeeFactory.EMPTY_ARGS);
            if (focus.) {
                return new ArrayList<TypedElemental>();
            }
            return wrap(func, false).invoke(context, args);
        };
    }
}

@FunctionalInterface
 interface Func1<T, TResult>
{
    TResult invoke(T t);
}

@FunctionalInterface
 interface Func3<T1, T2, T3, TResult>
{
    TResult invoke(T1 t1, T2 t2, T3 t3);
}

@FunctionalInterface
 interface Func2<T1, T2, TResult>
{
    TResult invoke(T1 t1, T2 t2);
}

@FunctionalInterface
 interface Func4<T1, T2, T3, T4, TResult>
{
    TResult invoke(T1 t1, T2 t2, T3 t3, T4 t4);
}
    public static Invokee Return(TypedElemental value) {
        return (ctx, args) -> Collections.singletonList(value);
    }

    public static Invokee Return(Iterable<TypedElemental> value) {
        return (ctx, args) -> value;
    }

    public static Invokee Invoke(String functionName, Iterable<Invokee> arguments, Invokee invokee) {
        return (ctx, args) -> {
            try {
                return invokee.invoke(ctx, arguments);
            } catch (Exception e) {
                throw new InvalidOperationException(
                        String.format("Invocation of %s failed: %s", formatFunctionName(functionName), e.getMessage()));
            }
        };
    }

    private static final Iterable<Invokee> EmptyArgs = Collections.emptyList();

    public static Invokee wrapLogic(Func2 func) {
        return (context, args) -> {
            Iterable<TypedElemental> left = args.iterator().next().invoke(context, Collections.emptyList()).iterator().next().b;
            Iterable<TypedElemental> right = args.iterator().next().invoke(context, Collections.emptyList()).iterator().next().booleanEval();
            return Typecasts.castTo(func.invoke(left, right));
        };
    }

    public static Invokee wrap(Func4 func, boolean propNull) {
        return (context, args) -> {
            var focus = args.iterator().next().invoke((Closure) context, Collections.emptyList());
            if (propNull && !focus.iterator().hasNext()) {
                return Collections.emptyList();
            }

            Iterable<TypedElemental> argA = args.iterator().next().invoke(context, Collections.emptyList());
            if (propNull && !argA.iterator().hasNext()) {
                return Collections.emptyList();
            }

            Iterable<TypedElemental> argB = args.iterator().next().invoke(context, Collections.emptyList());
            if (propNull && !argB.iterator().hasNext()) {
                return Collections.emptyList();
            }

            if (func instanceof Func3) {
                var argC = args.iterator().next().invoke(context, Collections.emptyList());
                if (propNull && !argC.iterator().hasNext()) {
                    return Collections.emptyList();
                }

                return Typecasts.castTo(func.invoke(
                        Typecasts.castTo(focus.iterator().next()),
                        Typecasts.castTo(argA.iterator().next()),
                        Typecasts.castTo(argB.iterator().next()),
                        Typecasts.castTo(argC.iterator().next())
                ));
            } else {
                var lastPar = (D) (Object) context.getEvaluationContext();
                return Typecasts.castTo(func.invoke(
                        Typecasts.castTo(focus.iterator().next()),
                        Typecasts.castTo(argA.iterator().next()),
                        Typecasts.castTo(argB.iterator().next()),
                        lastPar
                ));
            }
        };
    }

    public static Invokee wrapLogic(Func2 func) {
        return (context, args) -> {
            var left = args.iterator().next().invoke(context, Collections.emptyList()).iterator().next().booleanEval();
            var right = args.iterator().next().invoke(context, Collections.emptyList()).iterator().next().booleanEval();
            return Typecasts.castTo(func.invoke(left, right));
        };
    }

    public static Invokee returnVal(TypedElemental value) {
        return (context, args) -> Collections.singletonList(value);
    }

    public static Invokee returnVal(Iterable<TypedElemental> value) {
        return (context, args) -> value;
    }

    public static Invokee invoke(String functionName, Iterable<Invokee> arguments, Invokee invokee) {
        return (context, args) -> {
            try {
                return invokee.invoke(context, arguments);
            } catch (Exception e) {
                throw new IllegalStateException(
                        String.format("Invocation of %s failed: %s", formatFunctionName(functionName), e.getMessage())
                );
            }
        };
    }

    private static String formatFunctionName(String name) {
        if (name.startsWith(BinaryExpression.BIN_PREFIX))
            return String.format("operator '%s'", name.substring(BinaryExpression.BIN_PREFIX_LEN));
        else if (name.startsWith(UnaryExpression.URY_PREFIX))
            return String.format("operator '%s'", name.substring(UnaryExpression.URY_PREFIX_LEN));
        else
            return String.format("function '%s'", name);
    }

public class InvokeeFactory1 {
    public static final List<Invokee> EmptyArgs = Collections.emptyList();

    public static List<TypedElemental> GetThis(Closure context, List<Invokee> _) {
        return context.getThis();
    }

    public static List<TypedElemental> GetTotal(Closure context, List<Invokee> _) {
        return context.getTotal();
    }

    public static List<TypedElemental> GetContext(Closure context, List<Invokee> _) {
        return context.getOriginalContext();
    }

    public static List<TypedElemental> GetResource(Closure context, List<Invokee> _) {
        return context.getResource();
    }

    public static List<TypedElemental> GetRootResource(Closure context, List<Invokee> arguments) {
        return context.getRootResource();
    }

    public static List<TypedElemental> GetThat(Closure context, List<Invokee> _) {
        return context.getThat();
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
