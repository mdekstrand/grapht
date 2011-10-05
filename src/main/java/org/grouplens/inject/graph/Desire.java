package org.grouplens.inject.graph;

import java.lang.annotation.Annotation;
import java.util.Comparator;

/**
 * A possibly-not-concrete type. This represents the type of a dependency; it
 * may or may not be concrete. It can effectively be any type. Desires are
 * iteratively resolved and narrowed until they finally correspond to
 * {@link Node}s.
 * 
 * <p>
 * There are two types of desires: parameter desires and component desires. They
 * are distinguished by the {@link #isParameter()} method. Parameter desires are
 * for primitive or string parameters; component desires are for objects and
 * have more complex resolution rules.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface Desire {
    /**
     * Query whether a particular node will satisfy this desire. If true, then
     * an instance of the type represented by <var>node</var> is type-compatible
     * with this desire. This is equivalent to
     * {@link Class#isAssignableFrom(Class)}.
     * 
     * @param node The node in question.
     * @return <tt>true</tt> if the node is compatible with the desire (can
     *         satisfy it); <tt>false</tt> otherwise.
     * @throws IllegalArgumentException if <var>node</var> is not from the same
     *         graph implementation as this desire.
     * @see Class#isAssignableFrom(Class)
     */
    boolean isSatisfiedBy(Node node);

    /**
     * Query whether a particular desire is a subset of this desire. If true,
     * then any node satisfying <var>desire</var> will also satisfy this desire.
     * This method is used to check establish transitive chains through desires
     * while resolving them to nodes.
     * 
     * <p>
     * For parameter desires, this checks whether the other desire is for the
     * same type and parameter annotation, or a parameter inherited by this
     * desire's parameter annotation.
     * 
     * <p>
     * For component desires.
     * 
     * @param desire The desire to check.
     * @return <tt>true</tt> if any node satisfying <var>desire</var> will
     *         satisfy this desire.
     * @throws IllegalArgumentException if <var>desire</var> is not from the
     *         same graph implementation as this desire.
     * @see #isSatisfiedBy(Node)
     */
    boolean isSatisfiedBy(Desire desire);

    /**
     * Query whether this desire is for a parameter (primitive or string with a
     * parameter annotation).
     * 
     * @return <tt>true</tt> if this desire is for a parameter, <tt>false</tt>
     *         if it is for a component.
     */
    boolean isParameter();

    /**
     * Get the role annotation applied to this desire.
     * 
     * @return The role annotation applied to this desire, if it is for a
     *         component (if it is for a parameter, <tt>null</tt> is returned).
     */
    Class<? extends Annotation> getRoleAnnotation();

    /**
     * Get the parameter annotation applied to this desire.
     * 
     * @return The parameter annotation applied to this desire, if it is a
     *         parameter.
     */
    Class<? extends Annotation> getParameterAnnotation();

    /**
     * Query whether this desire is instantiable — that is, resovled to a
     * concrete type. If it is instantiable, then it can be converted to a node
     * with {@link #getNode()}.
     * 
     * @return <tt>true</tt> if the desire is for a concrete class. The only
     *         further desires or nodes that can satisfy it are for subclasses
     *         of the desire type.
     */
    boolean isInstantiable();

    /**
     * Get the node (concrete type) if this desire is fully resolved.
     * 
     * @return The node for this desire, or <tt>null</tt> if the desire is not a
     *         concrete type.
     */
    Node getNode();

    /**
     * Get a comparator for ordering bind rules.  The resulting comparator will
     * throw {@link IllegalArgumentException} when comparing bind rules from a
     * different implementation or which do not apply to this desire.
     * 
     * @return A comparator that compares bind rules which apply to this desire
     *         in increasing order of closeness.
     */
    Comparator<BindRule> ruleComparator();
}