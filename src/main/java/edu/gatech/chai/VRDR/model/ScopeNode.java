package edu.gatech.chai.VRDR.model;

import java.util.List;

public class ScopedNode implements TypedElemental, IAnnotated, IExceptionSource
    {
        private class Cache
        {
            public final Object _lock = new();

            public String? Id;
            public List<ScopedNode>? ContainedResources;
            public List<BundledResource>? BundledResources;

            public String? InstanceUri;
        }

        private final Cache _cache = new();

        public final TypedElemental Current;

        public ScopedNode(TypedElemental wrapped, String? instanceUri = null)
        {
            Current = wrapped;
            InstanceUri = instanceUri;

            if (Current is IExceptionSource ies && ies.ExceptionHandler == null)
            ies.ExceptionHandler = (o, a) -> ExceptionHandler.NotifyOrThrow(o, a);
        }

        private ScopedNode(ScopedNode parentNode, ScopedNode? parentResource, TypedElemental wrapped, String? fullUrl)
        {
            Current = wrapped;
            ExceptionHandler = parentNode.ExceptionHandler;
            ParentResource = parentNode.AtResource ? parentNode : parentResource;

            _fullUrl = fullUrl;

            if (Current.Name.equals("entry"))
                _fullUrl = Current.Children("fullUrl").FirstOrDefault()?.Value as String ?? _fullUrl;

        }

        public ExceptionNotificationHandler? ExceptionHandler { get; set; }

        /// <summary>
        /// Represents the most direct resource parent in which the current node is located.
        /// </summary>
        /// <remarks>
        /// When the node is the inital root, there is no parent and this property returns null.
        /// When the node is a (contained/bundled) resource itself, this property still returns
        /// that resource's most direct parent.
        /// </remarks>
        public final ScopedNode? ParentResource;

        /// <summary>
        /// Returns the location of the current element within its most direct parent resource or datatype.
        /// </summary>
        /// <remarks>
        /// For deeper paths, this would return the direct path within the encompassing type, e.g. 
        /// for an element at Patient.identifier.use this property would return Identifier.use.
        /// </remarks>
        public String LocalLocation -> ParentResource == null ? Location :
            $"{ParentResource.InstanceType}.{Location.Substring(ParentResource.Location.Length + 1)}";

        /// <inheritdoc/>
        public String Name -> Current.Name;

        /// <inheritdoc/>
        public String InstanceType -> Current.InstanceType;

        /// <inheritdoc/>
        public Object Value -> Current.Value;

        /// <inheritdoc/>
        public String Location -> Current.Location;

        /// <summary>
        /// Whether this node is a root element of a Resource.
        /// </summary>
        public boolean AtResource -> Current.Definition?.IsResource ?? Current is IResourceTypeSupplier rt && rt.ResourceType is not null;

        /// <summary>
        /// The instance type of the resource this element is part of.
        /// </summary>
        public String NearestResourceType -> ParentResource == null ? Location : ParentResource.InstanceType;

        /// <summary>
        /// The %resource context, as defined by FHIRPath
        /// </summary>
        /// <remarks>
        /// This is the original resource the current context is part of. When evaluating a datatype, 
        /// this would be the resource the element is part of. Do not go past a root resource into a bundle, 
        /// if it is contained in a bundle.
        /// </remarks>
        public TypedElemental ResourceContext
        {
            get
            {
                var scan = this;

                while (scan.ParentResource != null && scan.ParentResource.InstanceType != "Bundle")
                {
                    scan = scan.ParentResource;
                }

                return scan;
            }
        }

        /// <inheritdoc />
        public ElementDefinitionSummarily Definition -> Current.Definition;

        /// <summary>
        /// Get the list of container parents in a list, nearest parent first.
        /// </summary>
        /// <returns></returns>
        public List<ScopedNode> ParentResources()
        {
            var scan = ParentResource;

            while (scan != null)
            {
                yield return scan;

                scan = scan.ParentResource;
            }
        }

        /// <summary>
        /// Returns the Id of the resource, if the current node is a resource.
        /// </summary>
        /// <returns></returns>
        public String? Id()
        {
            if (_cache.Id == null)
            {
                _cache.Id = AtResource ? "#" + Current.Children("id").FirstOrDefault()?.Value : null;
            }

            return _cache.Id;
        }

        /// <summary>
        /// When this node is the node of a resource, it will return its contained resources, if any.
        /// </summary>
        public List<ScopedNode> ContainedResources()
        {
            if (_cache.ContainedResources == null)
            {
                _cache.ContainedResources = AtResource ?
                        this.Children("contained").Cast<ScopedNode>() :
                        Enumerable.Empty<ScopedNode>();
            }
            return _cache.ContainedResources;
        }

        /// <summary>
        /// A tuple of a bundled resource plus its Bundle.entry.fullUrl property.
        /// </summary>
        public class BundledResource
        {
            public String? FullUrl;
            public ScopedNode? Resource;
        }

        /// <summary>
        /// When this node is the root of a Bundle, retrieves the bundled resources in its Bundle.entry.
        /// </summary>
        public List<BundledResource> BundledResources()
        {
            if (_cache.BundledResources == null)
            {
                if (InstanceType == "Bundle")
                    _cache.BundledResources = from e in this.Children("entry")
                let fullUrl = e.Children("fullUrl").FirstOrDefault()?.Value as String
                let resource = e.Children("resource").FirstOrDefault() as ScopedNode
                select new BundledResource { FullUrl = fullUrl, Resource = resource };
                else
                _cache.BundledResources = Enumerable.Empty<BundledResource>();
            }

            return _cache.BundledResources;
        }


        private final String? _fullUrl = null;

        /// <summary>
        /// The full url of the resource this element is part of (if in a Bundle)
        /// </summary>
        /// <returns></returns>
        public String? FullUrl() -> _fullUrl;

        /// <summary>
        /// The full uri from where the instance this node is part of was retrieved.
        /// </summary>
        /// <remarks>The initial (parent) ScopedNode must have been created supplying the instanceUri parameter
        /// of the constructor.</remarks>
        public String? InstanceUri
        {
            get
            {
                // Scan up until the first parent that knowns the instance uri (at the last the
                // root, if it has been supplied).
                if (_cache.InstanceUri is null)
                _cache.InstanceUri = ParentResources().SkipWhile(p -> p.InstanceUri is null).FirstOrDefault()?.InstanceUri;

                return _cache.InstanceUri;
            }

            private set
            {
                _cache.InstanceUri = value;
            }
        }

        /// <inheritdoc />
        public List<Object> Annotations(Type type) -> type == typeof(ScopedNode) ? (new[] { this }) : Current.Annotations(type);

        /// <inheritdoc />
        public List<TypedElemental> Children(String? name = null) =>
        Current.Children(name).Select(c -> new ScopedNode(this, ParentResource, c, _fullUrl));
    
}
