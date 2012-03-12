package org.grouplens.inject;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.inject.annotation.Parameter;
import org.grouplens.inject.resolver.DefaultResolver;
import org.grouplens.inject.resolver.Resolver;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.InjectSPI;
import org.grouplens.inject.spi.reflect.ReflectionInjectSPI;

/**
 * <p>
 * InjectorBuilder is a Builder implementation that is capable of creating a
 * simple {@link Injector}. Additionally, it is root {@link Context} to make
 * configuring the built Injector as easy as possible. Injectors created by
 * InjectorBuilder instances memoize their created objects.
 * </p>
 * <p>
 * Internally, it uses an {@link InjectorConfigurationBuilder} to accumulate
 * bind rules, a {@link DefaultResolver} to resolve dependencies, and the
 * {@link ReflectionInjectSPI} to access dependency information.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class InjectorBuilder implements Context, Builder<Injector> {
    private final InjectorConfigurationBuilder builder;

    /**
     * Create a new InjectorBuilder that automatically applies the given Modules
     * via {@link #applyModule(Module)}. Additional Modules can be applied later
     * as well. Configuration via the {@link Context} interface is also possible
     * (and recommended if Modules aren't used) before calling {@link #build()}.
     * 
     * @param modules Any modules to apply immediately
     */
    public InjectorBuilder(Module... modules) {
        builder = new InjectorConfigurationBuilder();
        for (Module m: modules) {
            applyModule(m);
        }
    }
    
    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return builder.getRootContext().bind(type);
    }
    
    @Override
    public void bind(Class<? extends Annotation> param, Object value) {
        builder.getRootContext().bind(param, value);
    }

    @Override
    public Context in(Class<?> type) {
        return builder.getRootContext().in(type);
    }

    @Override
    public Context in(Class<? extends Annotation> role, Class<?> type) {
        return builder.getRootContext().in(role, type);
    }

    /**
     * Apply a module to the root context of this InjectorBuilder (i.e.
     * {@link Module#bind(Context)}).
     * 
     * @param module The module to apply
     * @return This InjectorBuilder
     */
    public InjectorBuilder applyModule(Module module) {
        builder.applyModule(module);
        return this;
    }

    @Override
    public Injector build() {
        Resolver resolver = new DefaultResolver(builder.build());
        return new SimpleInjector(builder.getSPI(), resolver);
    }
    
    private class SimpleInjector implements Injector {
        private final InjectSPI spi;
        private final Resolver resolver;
        
        public SimpleInjector(InjectSPI spi, Resolver resolver) {
            this.resolver = resolver;
            this.spi = spi;
        }
        
        public <T> T getInstance(Class<T> type) {
            return getInstance(null, type);
        }
        
        @SuppressWarnings("unchecked")
        public <T> T getInstance(@Nullable Class<? extends Annotation> role, Class<T> type) {
            Desire desire = spi.desire(role, type);
            Provider<?> provider = resolver.resolve(desire);
            return (T) provider.get();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getParameter(Class<? extends Annotation> param) {
            return (T) getInstance(param, param.getAnnotation(Parameter.class).value());
        }
    }
}