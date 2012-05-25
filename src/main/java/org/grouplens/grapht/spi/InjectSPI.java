/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.grapht.spi;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;

/**
 * InjectSPI is a service provider interface for accessing and creating the
 * types needed to use graph-based injections. InjectSPIs are responsible for
 * creating concrete instances of {@link Desire Desires}, {@link Satisfaction
 * Satisfactions}, and other SPI related interfaces.
 * <p>
 * The {@link ReflectionInjectSPI} provides a complete implementation of the SPI
 * using reflection to analyze types.
 * 
 * @see ReflectionInjectSPI
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface InjectSPI {
    /**
     * Return a Satisfaction wrapping the given class type. The type must be
     * instantiable and injectable, e.g.
     * <ol>
     * <li>Not abstract</li>
     * <li>Not an interface</li>
     * <li>Has a single constructor annotated with {@link Inject @Inject}</li>
     * <li>- or - has a no-argument constructor</li>
     * </ol>
     * <p>
     * The returned satisfaction will create new instances of the type with its
     * providers using the available constructor, as well as satisfy its
     * dependencies based on the {@link ProviderSource} passed to
     * {@link Satisfaction#makeProvider(ProviderSource)}.
     * 
     * @param type The type to wrap
     * @return A satisfaction wrapping the given class type, capable of
     *         instantiating new instances
     */
    Satisfaction satisfy(Class<?> type);
    
    /**
     * Return a Satisfaction that satisfies the given type by explicitly
     * returning null from the providers created by
     * {@link Satisfaction#makeProvider(ProviderSource)}.
     * 
     * @param type The type to wrap
     * @return A satisfaction wrapping the given class type, that satisfies with
     *         null values
     */
    Satisfaction satisfyWithNull(Class<?> type);
    
    /**
     * Return a Satisfaction that wraps the given instance, and can satisfy
     * dependencies of the object's type. Its providers will always provide the
     * specified instance.
     * 
     * @param o The instance to wrap
     * @return A satisfaction wrapping the given instance
     */
    Satisfaction satisfy(Object o);
    
    /**
     * Return a Satisfaction that wraps a Provider class, and can satisfy
     * dependencies for the Provider's provided type. The satisfaction is
     * responsible for creating instances of the Provider type.
     * 
     * @param providerType The provider type ultimately responsible for creating
     *            instances of the satisfaction type
     * @return A satisfaction wrapping the given provider class, that creates
     *         instances of that provider type
     */
    Satisfaction satisfyWithProvider(Class<? extends Provider<?>> providerType);
    
    /**
     * Return a Satisfaction that wraps the given Provider instance. The
     * satisfaction will use the given Provider to construct the instances of
     * its provided type.
     * 
     * @param provider The provider to wrap
     * @return A satisfaction wrapping the given provider
     */
    Satisfaction satisfyWithProvider(Provider<?> provider);

    /**
     * <p>
     * Create a Desire that wraps the given InjectionPoint, and is optionally
     * satisfied by the provided satisfaction. Exceptions will be raised if the
     * injection point and satisfaction's types are not compatible.
     * <p>
     * For root desires, use {@link #desire(Annotation, Class, boolean)}
     * 
     * @param inject The injection point
     * @param satisfaction An optional satisfaction for the desire, if available
     * @return A Desire wrapping the injection point and satisfaction
     */
    Desire desire(InjectionPoint inject, @Nullable Satisfaction satisfaction);
    
    /**
     * Create a Desire that wraps a synthetic InjectionPoint for the qualified
     * type, that may or may not be satisfied by a null value.
     * 
     * @param qualifier The qualifier on the synthetic injection point
     * @param type The desired type, and type of the injection point
     * @param nullable True if the injection point accepts null values
     * @return A Desire for the given qualified type
     */
    Desire desire(@Nullable Annotation qualifier, Class<?> type, boolean nullable);
    
    /**
     * Create a ContextMatcher that matches the given context formed by a
     * Qualifier and a type. The created ContextMatcher must be
     * compatible with the BindRules, Desires, and Satisfactions created by this
     * InjectSPI.
     * 
     * @param qualifier The qualifier matcher
     * @param type The type of the context
     * @return A ContextMatcher representing the qualifier and type
     */
    ContextMatcher context(QualifierMatcher qualifier, Class<?> type);
    
    /**
     * Create a QualifierMatcher that matches the given annotation type. This annotation must
     * be annotated with {@link javax.inject.Qualifier}.
     * 
     * @param qualifier The qualifier annotation type
     * @return A QualifierMatcher matching the qualifier type
     */
    QualifierMatcher match(Class<? extends Annotation> qualifier);
    
    /**
     * Create a QualifierMatcher that matches annotation instances equal to 
     * the given instance.
     * @param annot The annotation instance to equal to
     * @return A QualifierMatcher matching the given annotation instance
     */
    QualifierMatcher match(Annotation annot);
    
    /**
     * @return A QualifierMatcher that matches any Qualifier, including the null
     *         qualifier
     */
    QualifierMatcher matchAny();
    
    /**
     * @return A QualifierMatcher that matches only the null qualifier
     */
    QualifierMatcher matchNone();
}
