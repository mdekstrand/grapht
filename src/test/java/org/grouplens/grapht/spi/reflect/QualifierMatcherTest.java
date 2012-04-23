package org.grouplens.grapht.spi.reflect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.reflect.types.RoleA;
import org.grouplens.grapht.util.AnnotationBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class QualifierMatcherTest {
    private InjectSPI spi;
    
    @Before
    public void setup() {
        spi = new ReflectionInjectSPI();
    }
    
    @Test
    public void testAnnotationInstanceMatch() {
        QualifierMatcher matcher = spi.match(new AnnotationBuilder<Named>(Named.class).set("value", "test").build());
        Assert.assertTrue(matcher.matches(spi.qualifier(new AnnotationBuilder<Named>(Named.class).set("value", "test").build())));
        Assert.assertFalse(matcher.matches(spi.qualifier(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build())));
        Assert.assertFalse(matcher.matches(spi.qualifier(new AnnotationBuilder<RoleA>(RoleA.class).build())));
        Assert.assertFalse(matcher.matches(null));
    }
    
    @Test
    public void testAnnotationClassMatch() {
        QualifierMatcher matcher = spi.match(Named.class);
        Assert.assertTrue(matcher.matches(spi.qualifier(new AnnotationBuilder<Named>(Named.class).set("value", "test").build())));
        Assert.assertTrue(matcher.matches(spi.qualifier(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build())));
        Assert.assertFalse(matcher.matches(spi.qualifier(new AnnotationBuilder<RoleA>(RoleA.class).build())));
        Assert.assertFalse(matcher.matches(null));
    }
    
    @Test
    public void testAnyMatch() {
        QualifierMatcher matcher = spi.matchAny();
        Assert.assertTrue(matcher.matches(spi.qualifier(new AnnotationBuilder<Named>(Named.class).set("value", "test").build())));
        Assert.assertTrue(matcher.matches(spi.qualifier(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build())));
        Assert.assertTrue(matcher.matches(spi.qualifier(new AnnotationBuilder<RoleA>(RoleA.class).build())));
        Assert.assertTrue(matcher.matches(null));
    }
    
    @Test
    public void testNoContextMatch() {
        QualifierMatcher matcher = spi.matchNone();
        Assert.assertFalse(matcher.matches(spi.qualifier(new AnnotationBuilder<Named>(Named.class).set("value", "test").build())));
        Assert.assertFalse(matcher.matches(spi.qualifier(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build())));
        Assert.assertFalse(matcher.matches(spi.qualifier(new AnnotationBuilder<RoleA>(RoleA.class).build())));
        Assert.assertTrue(matcher.matches(null));
    }
    
    @Test
    public void testComparator() {
        QualifierMatcher m1 = spi.match(new AnnotationBuilder<Named>(Named.class).set("value", "test").build());
        QualifierMatcher m2 = spi.match(Named.class);
        QualifierMatcher m3 = spi.matchAny();
        QualifierMatcher m4 = spi.matchNone();
        
        List<QualifierMatcher> ordered = Arrays.asList(m3, m2, m4, m1); // purposely unordered
        List<QualifierMatcher> expected = Arrays.asList(m4, m1, m2, m3); // m4, and m1 are equal, but its a consistent ordering
        
        Collections.sort(ordered);
        Assert.assertEquals(expected, ordered);
    }
}