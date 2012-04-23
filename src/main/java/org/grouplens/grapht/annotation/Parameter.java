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
package org.grouplens.grapht.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter is used to designate the primitive type of qualifier annotation's
 * intended for primitive parameters.
 * 
 * 
 * @author Michael Ludwig
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
    /**
     * Primitive type of the parameter.
     * 
     * @author Michael Ludwig
     * @review Should we use an enum to represent type, or have the annotation
     *         take a class?
     */
    public static enum PrimitiveType {
        BOOLEAN(boolean.class), BYTE(byte.class), SHORT(short.class), 
        INT(int.class), LONG(long.class), CHAR(char.class), 
        FLOAT(float.class), DOUBLE(double.class);
        
        private final Class<?> type;
        private PrimitiveType(Class<?> type) { this.type = type; }
        
        /**
         * @return The primitive type, e.g. <code>byte.class</code>
         * or <code>int.class</code>.
         */
        public Class<?> getType() {
            return type;
        }
    }
    
    PrimitiveType value();
}