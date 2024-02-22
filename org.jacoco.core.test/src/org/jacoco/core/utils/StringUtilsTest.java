package org.jacoco.core.utils;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {
    @Test
    public void should_join_all_items_without_spaces_given_separator_is_null() {
        final String ret = StringUtils.join(Arrays.asList("a", "b", "c"), null);
        assertEquals("abc", ret);
    }
    @Test
    public void should_join_all_items_with_spaces_given_separator_is_space() {
        final String ret = StringUtils.join(Arrays.asList("a", "b", "c"), " ");
        assertEquals("a b c", ret);
    }
}
