/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.reflect;

import org.grouplens.inject.graph.BindRule;
import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Node;
import org.grouplens.inject.graph.NodeRepository;

import java.lang.reflect.Type;

public class ClasspathNodeRepository implements NodeRepository {

    @Override
    public Node resolve(Desire desire) {
        // TODO Support resolving desires
        throw new UnsupportedOperationException();
    }

    @Override
    public BindRule defaultBindRule() {
        // TODO Implement default bind rule
        throw new UnsupportedOperationException();
    }

    /**
     * Create a new node wrapping an instance. The object must be of a
     * non-parameterized type.
     * @param obj The object to wrap.
     * @return A node which, when instantiated, returns <var>obj</var>.
     * @throws IllegalArgumentException if <var>obj</var> is an instance of a
     * parameterized type.
     */
    public Node newInstanceNode(Object obj) {
        Class<?> type = obj.getClass();
        if (type.getTypeParameters().length > 0) {
            throw new IllegalArgumentException("object is of parameterized type");
        }
        return newInstanceNode(obj, type);
    }

    /**
     * Create a new node wrapping an instance.
     * @param obj The object instance to wrap.
     * @param type The type of the object.
     * @return A node which, when instantiated, returns <var>obj</var>.
     * @throws IllegalArgumentException if <var>obj</var> is not, as far as the
     * code can tell, of type <var>type</var>. Due to type reification, not all
     * such errors can be caught.
     */
    public Node newInstanceNode(Object obj, Type type) {
        return new InstanceNode(obj, type);
    }

    /**
     * Create a new node wrapping an instance.
     * @param obj The object.
     * @param type The type of the object.
     * @param <T> The type of the object.
     * @return A node wrapping the object.
     * @see #newInstanceNode(Object, java.lang.reflect.Type) 
     */
    public <T> Node newInstanceNode(T obj, TypeLiteral<T> type) {
        return newInstanceNode(obj, type.getType());
    }
}