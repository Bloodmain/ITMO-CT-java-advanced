package info.kgeorgiy.ja.dunaev.arrayset;

import java.util.*;


public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final Comparator<? super E> comparator;
    private final List<E> storage;
    private final boolean isNaturalOrdered;

    public ArraySet() {
        this(Collections.emptyList());
    }

    public ArraySet(final Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(final Collection<? extends E> collection, final Comparator<? super E> comparator) {
        isNaturalOrdered = comparator == null;
        this.comparator = wrapComparator(comparator);

        final SortedSet<E> distinctValues = new TreeSet<>(this.comparator);
        distinctValues.addAll(collection);
        storage = List.copyOf(distinctValues);
    }

    /* view constructor */
    private ArraySet(final ArraySet<E> viewFrom, final List<E> newStorage, final Comparator<? super E> newComparator) {
        isNaturalOrdered = viewFrom.isNaturalOrdered;
        comparator = newComparator;
        storage = newStorage;
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        return isNaturalOrdered ? null : comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return storage.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(this, storage.reversed(), comparator.reversed());
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public E lower(final E element) {
        return getOrNull(lowerBound(element, false) - 1);
    }

    @Override
    public E floor(final E element) {
        return getOrNull(lowerBound(element, true) - 1);
    }

    @Override
    public E ceiling(final E element) {
        return getOrNull(lowerBound(element, false));
    }

    @Override
    public E higher(final E element) {
        return getOrNull(lowerBound(element, true));
    }

    @Override
    public E first() {
        assertNotEmpty("Call \"first()\" on empty set");
        return storage.getFirst();
    }

    @Override
    public E last() {
        assertNotEmpty("Call \"last()\" on empty set");
        return storage.getLast();
    }

    @Override
    public NavigableSet<E> subSet(final E fromElement, final boolean fromInclusive,
                                  final E toElement, final boolean toInclusive) {
        if (comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("Call \"subset(from, to)\" with from > to");
        }
        return subView(lowerBound(fromElement, !fromInclusive), lowerBound(toElement, toInclusive));
    }

    @Override
    public NavigableSet<E> headSet(final E toElement, final boolean inclusive) {
        return subView(0, lowerBound(toElement, inclusive));
    }

    @Override
    public NavigableSet<E> tailSet(final E fromElement, final boolean inclusive) {
        return subView(lowerBound(fromElement, !inclusive), size());
    }

    @Override
    public SortedSet<E> subSet(final E fromElement, final E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(final E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(final E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Unsupported operation \"pollFirst()\" on unmodifiable ArraySet");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Unsupported operation \"pollLast()\" on unmodifiable ArraySet");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(final Object object) {
        return binarySearch((E) object) >= 0;
    }

    @SuppressWarnings("unchecked")
    private Comparator<? super E> wrapComparator(final Comparator<? super E> comparator) {
        if (comparator == null) {
            return (first, second) -> {
                final Comparable<? super E> compFirst = (Comparable<? super E>) first;
                return compFirst.compareTo(second);
            };
        }
        return comparator;
    }

    private int lowerBound(final E element, boolean exclusive) {
        int lowerBound = binarySearch(element); // (-(insert position) - 1) if absent
        if (lowerBound < 0) {
            lowerBound = -lowerBound - 1;
        } else if (exclusive) {
            lowerBound += 1;
        }

        return lowerBound;
    }

    private E getOrNull(final int index) {
        if (0 <= index && index < size()) {
            return storage.get(index);
        }
        return null;
    }

    private void assertNotEmpty(final String message) {
        if (isEmpty()) {
            throw new NoSuchElementException(message);
        }
    }

    private int binarySearch(final E element) {
        return Collections.binarySearch(storage, element, comparator);
    }

    private ArraySet<E> subView(final int from, final int to) {
        return new ArraySet<>(
                this, from <= to ? storage.subList(from, to) : Collections.emptyList(), comparator
        );
    }
}
