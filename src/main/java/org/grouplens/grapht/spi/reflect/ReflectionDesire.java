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
package org.grouplens.grapht.spi.reflect;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.grouplens.grapht.InvalidBindingException;
import org.grouplens.grapht.annotation.DefaultBoolean;
import org.grouplens.grapht.annotation.DefaultDouble;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.grapht.annotation.DefaultInteger;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.grapht.annotation.DefaultString;
import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectionPoint;

/**
 * ReflectionDesire is an implementation of desire that contains all necessary
 * implementation to represent a desire, except that the point of injection is
 * abstracted by an {@link InjectionPoint}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionDesire implements Desire, Externalizable {
    /**
     * Return a list of desires that must satisfied in order to instantiate the
     * given type.
     *
     * @param type The class type whose dependencies will be queried
     * @return The dependency desires for the given type
     * @throws NullPointerException if the type is null
     */
    public static List<ReflectionDesire> getDesires(Class<?> type) {
        List<ReflectionDesire> desires = new ArrayList<ReflectionDesire>();

        boolean ctorFound = false;
        for (Constructor<?> ctor: type.getConstructors()) {
            if (ctor.getAnnotation(Inject.class) != null) {
                if (!ctorFound) {
                    ctorFound = true;
                    for (int i = 0; i < ctor.getParameterTypes().length; i++) {
                        desires.add(new ReflectionDesire(new ConstructorParameterInjectionPoint(ctor, i)));
                    }
                } else {
                    // at the moment there can only be one injectable constructor
                    throw new InvalidBindingException(type, "More than one constructor with @Inject is not allowed");
                }
            }
        }

        for (Method m: type.getMethods()) {
            if (m.getAnnotation(Inject.class) != null) {
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    desires.add(new ReflectionDesire(new SetterInjectionPoint(m, i)));
                }
            }
        }

        return Collections.unmodifiableList(desires);
    }
    
    // all are "final"
    private Class<?> desiredType;
    private InjectionPoint injectPoint;
    private ReflectionSatisfaction satisfaction;

    /**
     * Create a ReflectionDesire that immediately wraps the given
     * InjectionPoint. The desired type equals the type declared by the
     * injection point. The created desire will have a satisfaction if the
     * injection point's type is satisfiable.
     * 
     * @param injectPoint The injection point to wrap
     * @throws NullPointerException if injectPoint is null
     */
    public ReflectionDesire(InjectionPoint injectPoint) {
        this(injectPoint.getErasedType(), injectPoint, null);
    }

    /**
     * Create a ReflectionDesire that represents the dependency for
     * <tt>desiredType</tt> that will be injected into the given InjectionPoint.
     * The optional satisfaction will satisfy this desire. If null is provided,
     * and the desired type instantiable, a ClassSatisfaction is created.
     * 
     * @param desiredType The desired type of the dependency
     * @param injectPoint The injection point of the desire
     * @param satisfaction The satisfaction satisfying this desire, if there is
     *            one
     * @throws NullPointerException if desiredType, injectPoint, or dfltSource is null
     * @throws IllegalArgumentException if desiredType is not assignable to the
     *             type of the injection point, or if the satisfaction's type is
     *             not assignable to the desired type
     */
    public ReflectionDesire(Class<?> desiredType, InjectionPoint injectPoint,
                            ReflectionSatisfaction satisfaction) {
        Checks.notNull("desired type", desiredType);
        Checks.notNull("injection point", injectPoint);

        desiredType = Types.box(desiredType);
        Checks.isAssignable(injectPoint.getErasedType(), desiredType);
        if (satisfaction != null) {
            Checks.isAssignable(desiredType, satisfaction.getErasedType());
        }

        // try and find a satisfaction
        if (satisfaction == null) {
            if (Types.isInstantiable(desiredType)) {
                satisfaction = new ClassSatisfaction(desiredType);
            } else if (injectPoint.isNullable()) {
                satisfaction = new NullSatisfaction(desiredType);
            }
        }

        this.desiredType = desiredType;
        this.injectPoint = injectPoint;
        this.satisfaction = satisfaction;
    }
    
    /**
     * Constructor required by {@link Externalizable}.
     */
    public ReflectionDesire() { }
    
    @Override
    public Class<?> getDesiredType() {
        return desiredType;
    }

    @Override
    public InjectionPoint getInjectionPoint() {
        return injectPoint;
    }

    @Override
    public boolean isInstantiable() {
        return satisfaction != null;
    }

    @Override
    public ReflectionSatisfaction getSatisfaction() {
        return satisfaction;
    }

    @Override
    public Desire getDefaultDesire() {
        Annotation qualifier = injectPoint.getAttributes().getQualifier();

        // Check the qualifier first, but only if the qualifier source hasn't been disabled
        if (!isInstantiable()) {
            if (qualifier != null) {
                Class<? extends Annotation> annotType = qualifier.annotationType();
                DefaultDouble dfltDouble = annotType.getAnnotation(DefaultDouble.class);
                if (dfltDouble != null) {
                    return new ReflectionDesire(Double.class, injectPoint, new InstanceSatisfaction(dfltDouble.value()));
                }
                DefaultInteger dfltInt = annotType.getAnnotation(DefaultInteger.class);
                if (dfltInt != null) {
                    return new ReflectionDesire(Integer.class, injectPoint, new InstanceSatisfaction(dfltInt.value()));
                }
                DefaultBoolean dfltBool = annotType.getAnnotation(DefaultBoolean.class);
                if (dfltBool != null) {
                    return new ReflectionDesire(Boolean.class, injectPoint, new InstanceSatisfaction(dfltBool.value()));
                }
                DefaultString dfltStr = annotType.getAnnotation(DefaultString.class);
                if (dfltStr != null) {
                    return new ReflectionDesire(String.class, injectPoint, new InstanceSatisfaction(dfltStr.value()));
                }
                DefaultImplementation impl = annotType.getAnnotation(DefaultImplementation.class);
                if (impl != null) {
                    // let the constructor create any satisfaction
                    return new ReflectionDesire(impl.value(), injectPoint, null);
                }
            }
        }
        
        // Now check the desired type for @DefaultImplementation or @DefaultProvider if the type
        // source has not been disabled.
        DefaultProvider provided = injectPoint.getErasedType().getAnnotation(DefaultProvider.class);
        if (provided != null) {
            return new ReflectionDesire(Types.getProvidedType(provided.value()), injectPoint, 
                                        new ProviderClassSatisfaction(provided.value()));
        }
        DefaultImplementation impl = injectPoint.getErasedType().getAnnotation(DefaultImplementation.class);
        if (impl != null) {
            // let the constructor create any satisfaction
            return new ReflectionDesire(impl.value(), injectPoint, null);
        }
        
        // There are no annotations on the {@link Qualifier} or the type that indicate a
        // default binding or value, or the defaults have been disabled,
        // so we return null
        return null;
    }

    @Override
    public Comparator<BindRule> ruleComparator() {
        // 1st comparison is manual vs generated bind rules
        // - manual bind rules are preferred over any generated rules
        // 2nd comparison is the priority of the qualifier matcher, e.g:
        // - match a specific instance/none > match class type > match any
        return new Comparator<BindRule>() {
            @Override
            public int compare(BindRule o1, BindRule o2) {
                ReflectionBindRule b1 = (ReflectionBindRule) o1;
                ReflectionBindRule b2 = (ReflectionBindRule) o2;

                // #1 - select manual over generated
                if (b1.getWeight() != b2.getWeight()) {
                    return b1.getWeight() - b2.getWeight();
                }

                // #2 - select the higher priority qualifier matcher
                return b1.getQualifier().compareTo(b2.getQualifier());
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectionDesire)) {
            return false;
        }
        ReflectionDesire r = (ReflectionDesire) o;
        return (r.desiredType.equals(desiredType) && 
                r.injectPoint.equals(injectPoint) && 
                (r.satisfaction == null ? satisfaction == null : r.satisfaction.equals(satisfaction)));
    }

    @Override
    public int hashCode() {
        return desiredType.hashCode() ^ injectPoint.hashCode() ^ (satisfaction == null ? 0 : satisfaction.hashCode());
    }

    @Override
    public String toString() {
        return "Desire(" + desiredType.getSimpleName() + ", " + injectPoint + ")";
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        desiredType = Types.readClass(in);
        
        injectPoint = (InjectionPoint) in.readObject();
        satisfaction = (ReflectionSatisfaction) in.readObject();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Types.writeClass(out, desiredType);
        
        out.writeObject(injectPoint);
        out.writeObject(satisfaction);
    }
}
