package ru.georgeee.itmo.sem5.cg.common;

import java.util.Comparator;
import java.util.function.BiPredicate;

@FunctionalInterface
public interface EqualComparator<T extends Comparable<T>> extends Comparator<T>, BiPredicate<T, T> {
    boolean test(T p, T q);

    @Override
    default int compare(T p, T q) {
        return test(p, q) ? 0 : p.compareTo(q);
    }
}
