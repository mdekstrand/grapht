package org.grouplens.inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.InjectSPI;
import org.grouplens.inject.spi.reflect.ReflectionInjectSPI;

/**
 * InjectorConfigurationBuilder is a Builder that creates
 * InjectorConfigurations. This uses its own implementations of {@link Context}
 * and {@link Binding} to accumulate {@link BindRule BindRules}. For simple
 * applications, {@link InjectorBuilder} is the recommended entry point.
 * InjectorConfigurationBuilder is useful for different implementations of
 * Injector that only need to change the inject behavior, but do not need to
 * modify configuration.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class InjectorConfigurationBuilder implements Builder<InjectorConfiguration> {
    private final InjectSPI spi;
    private final Context root;
    
    private final Set<Class<?>> defaultExcludes;
    private final Map<ContextChain, Collection<BindRule>> bindRules;
    
    /**
     * Create a new InjectorConfigurationBuilder that uses a
     * {@link ReflectionInjectSPI}.
     */
    public InjectorConfigurationBuilder() {
        this(new ReflectionInjectSPI());
    }

    /**
     * Create a new InjectorConfigurationBuilder that uses the given
     * {@link InjectSPI} instance.
     * 
     * @param spi The injection service provider to use
     * @throws NullPointerException if spi is null
     */
    public InjectorConfigurationBuilder(InjectSPI spi) {
        if (spi == null) {
            throw new NullPointerException("SPI cannot be null");
        }
        
        this.spi = spi;
        defaultExcludes = new HashSet<Class<?>>();
        bindRules = new HashMap<ContextChain, Collection<BindRule>>();
        
        root = new ContextImpl(this, new ContextChain(new ArrayList<ContextMatcher>()));
    }
    
    /**
     * @return The SPI used by this builder
     */
    public InjectSPI getSPI() {
        return spi;
    }
    
    /**
     * @return The root context managed by this builder
     */
    public Context getRootContext() {
        return root;
    }

    /**
     * Run the module's {@link Module#bind(Context) bind()} method on the root
     * context of this builder.
     * 
     * @param module The module to apply
     */
    public void applyModule(Module module) {
        module.bind(getRootContext());
    }

    /**
     * Add a type to be excluded from when generating bind rules. This does not
     * invalidate bindings that bind directly to this type.
     * 
     * @param type The type to exclude
     * @throws NullPointerException if type is null
     */
    public void addDefaultExclusion(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Exclusion type cannot be null");
        }
        defaultExcludes.add(type);
    }

    /**
     * Remove a type that is currently being excluded.
     * 
     * @see #addDefaultExclusion(Class)
     * @param type The type that should no longer be excluded
     * @throws NullPointerException if type is null
     */
    public void removeDefaultExclusion(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Exclusion type cannot be null");
        }
        defaultExcludes.remove(type);
    }
    
    void addBindRule(ContextChain context, BindRule rule) {
        Collection<BindRule> inContext = bindRules.get(context);
        if (inContext == null) {
            inContext = new ArrayList<BindRule>();
            bindRules.put(context, inContext);
        }
        
        inContext.add(rule);
    }

    Set<Class<?>> getDefaultExclusions() {
        return Collections.unmodifiableSet(defaultExcludes);
    }
    
    @Override
    public InjectorConfiguration build() {
        // make a deep copy of the bind rules, since the map's key set can change
        // and the collection of bind rules can change
        final Map<ContextChain, Collection<? extends BindRule>> rules = new HashMap<ContextChain, Collection<? extends BindRule>>();
        for (Entry<ContextChain, Collection<BindRule>> e: bindRules.entrySet()) {
            rules.put(e.getKey(), new ArrayList<BindRule>(e.getValue()));
        }
        
        return new InjectorConfiguration() {
            @Override
            public Map<ContextChain, Collection<? extends BindRule>> getBindRules() {
                return Collections.unmodifiableMap(rules);
            }
        };
    }
}
