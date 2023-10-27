package edu.gatech.chai.VRDR.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.elementmodel.*;

public class ElementNodeExtensions
{
    public static List<TypedElemental> children(List<TypedElemental> nodes, String name) {
        List<TypedElemental> children = new ArrayList<>();
        for (TypedElemental node : nodes) {
            children.addAll(node.Children(null));
        }
        return children;
    }

    public static List<TypedElemental> descendants(TypedElemental element) {
        List<TypedElemental> descendants = new ArrayList<>();
        for (TypedElemental child : element.Children(null)) {
            descendants.add(child);
            descendants.addAll(descendants(child));
        }
        return descendants;
    }

    public static List<TypedElemental> descendants(List<TypedElemental> elements) {
        List<TypedElemental> descendants = new ArrayList<>();
        for (TypedElemental element : elements) {
            descendants.addAll(descendants(element));
        }
        return descendants;
    }


    public static List<TypedElemental> descendantsAndSelf(TypedElemental element) {
        List<TypedElemental> descendants = new ArrayList<>();
        descendants.add(element);
        descendants.addAll(descendants(element));
        return descendants;
    }

    public static List<TypedElemental> descendantsAndSelf(List<TypedElemental> elements) {
        List<TypedElemental> descendants = new ArrayList<>();
        for (TypedElemental element : elements) {
            descendants.addAll(descendantsAndSelf(element));
        }
        return descendants;
    }

    public static void visit(TypedElemental root, Visitor visitor) {
        visit(root, visitor, 0);
    }

    private static void visit(TypedElemental root, Visitor visitor, int depth) {
        visitor.visit(depth, root);
        for (TypedElemental child : root.Children(null)) {
            visit(child, visitor, depth + 1);
        }
    }

    public interface Visitor {
        void visit(int depth, TypedElemental element);
    }

    public static void visit(TypedElemental root, Visitor visitor) {
        visit(root, visitor, 0);
    }

    private static void visit(TypedElemental root, Visitor visitor, int depth) {
        visitor.visit(depth, root);
        for (TypedElemental child : root.Children(null)) {
            visit(child, visitor, depth + 1);
        }
    }


    public static IDisposable catchException(TypedElemental source, ExceptionNotificationHandler handler) {
        if (source instanceof IExceptionSource) {
            return ((IExceptionSource) source).catchException(handler);
        } else {
            throw new NotImplementedException("Element does not implement IExceptionSource.");
        }
    }

    public static void visitAll(TypedElemental nav) {
        nav.visit((_, n) -> {
            Object dummy = n.getValue();
        });
    }

    public static List<Exception> visitAndCatch(TypedElemental node) {
        List<Exception> errors = new ArrayList<>();
        try (AutoCloseable catchHandler = catchException(node, (o, arg) -> errors.add(arg))) {
            visitAll(node);
        }
        return errors;
    }

    public static Iterable<Object> annotations(TypedElemental nav, Class<?> type) {
        if (nav instanceof Annotated) {
            Annotated ann = (Annotated) nav;
            return Collections.singleton(ann.annotationType());//.annotations(type);
        } else {
            return Enumerable.empty();
        }
    }

    public static <T> T annotation(TypedElemental nav, Class<T> type) {
        if (nav instanceof Annotated) {
            Annotated ann = (Annotated) nav;
            return (T) ann.annotationType();//.annotation(type);
        } else {
            return null;
        }
    }

    public static ISourceNode toSourceNode(TypedElemental node) {
        return new TypedElementToSourceNodeAdapter(node);
    }

    public static IReadOnlyCollection<ElementDefinitionSummarily> childDefinitions(TypedElemental me,
                                                                                  IStructureDefinitionSummaryProvider provider) {
        if (me.getDefinition() != null) {
            // If this is a backbone element, the child type is the nested complex type
            if (me.getDefinition().getType().get(0) instanceof IStructureDefinitionSummary be) {
                return be.getElements();
            } else {
                if (me.getInstanceType() != null) {
                    IStructureDefinitionSummary si = provider.provide(me.getInstanceType());
                    if (si != null) {
                        return si.getElements();
                    }
                }
            }
        }

        // Note: fall-through in all failure cases - return empty collection
        return new ArrayList<>();
    }

    public static ScopedNode toScopedNode(TypedElemental node) {
        return node instanceof ScopedNode ? (ScopedNode) node : new ScopedNode(node);
    }
    
    public static AutoCloseable Catch(TypedElemental source, ExceptionHandler handler) =>
    source is IExceptionSource s ? s.Catch(handler) : throw new NotImplementedException("Element does not implement IExceptionSource.");

    public static void VisitAll(TypedElemental nav) -> nav.Visit((_, n) -> { var dummy = n.Value; });

    public static List<Exception> VisitAndCatch(TypedElemental node)
    {
        var errors = new List<Exception>();

        using (node.Catch((o, arg) -> errors.Add(arg)))
        {
            node.VisitAll();
        }

        return errors;
    }



    public static List<Object> Annotations(TypedElemental nav, Type type) =>
    nav is Annotated ann ? ann.Annotations(type) : Enumerable.Empty<Object>();
    public static T? Annotation<T>(TypedElemental nav) =>
    nav is Annotated ann ? ann.Annotation<T>() : default;

    public static ISourceNode ToSourceNode(TypedElemental node) -> new TypedElementToSourceNodeAdapter(node);

    public static ReadOnlyCollection<ElementDefinitionSummarily> ChildDefinitions(TypedElemental me,
                                                                                  IStructureDefinitionSummaryProvider provider)
    {
        if (me.Definition != null)
        {
            // If this is a backbone element, the child type is the nested complex type
            if (me.Definition.Type[0] instanceof IStructureDefinitionSummary be)
            return be.GetElements();
                else
            {
                if (me.InstanceType != null)
                {
                    IStructureDefinitionSummary si = provider.Provide(me.InstanceType);
                    if (si != null) return si.GetElements();
                }
            }

        }

        // Note: fall-through in all failure cases - return empty collection
        return new ArrayList<ElementDefinitionSummarily>();
    }

    public static ScopedNode ToScopedNode(TypedElemental node) =>
    node as ScopedNode ?? new ScopedNode(node);
}
