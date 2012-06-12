package com.redhat.topicindex.sort;

import java.util.List;

/**
 * Defines a class that can be used to sort a List<V> against a Map<T, U>
 */
public interface ExternalListSort <T, U, V>
{
	void sort (final List<U> map, List<V> list);
}
