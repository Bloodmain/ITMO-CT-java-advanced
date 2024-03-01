package info.kgeorgiy.ja.dunaev.arrayset;

import java.util.*;


public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E>, List<E> {
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
        storage = Collections.unmodifiableList(new ArrayList<>(distinctValues));
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
        return listIterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return storage.listIterator(index);
    }

    @Override
    public Spliterator<E> spliterator() {
        return storage.spliterator();
    }

    @Override
    public ArraySet<E> descendingSet() {
        return new ArraySet<>(this, storage.reversed(), comparator.reversed());
    }

    @Override
    public ArraySet<E> reversed() {
        return descendingSet();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return storage.reversed().iterator();
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
        return getFirst();
    }

    @Override
    public E last() {
        assertNotEmpty("Call \"last()\" on empty set");
        return getLast();
    }

    @Override
    public E getFirst() {
        return storage.getFirst();
    }

    @Override
    public E getLast() {
        return storage.getLast();
    }

    @Override
    public E get(int index) {
        return storage.get(index);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int indexOf(Object o) {
        int ind = binarySearch((E) o);
        return ind >= 0 ? ind : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return subView(fromIndex, toIndex);
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
    public boolean contains(final Object object) {
        return indexOf(object) != -1;
    }

    @SuppressWarnings("unchecked")
    private Comparator<? super E> wrapComparator(final Comparator<? super E> comparator) {
        if (comparator == null) {
            return (Comparator<? super E>) Comparator.naturalOrder();
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

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("addAll() method is not allowed in this unmodifiable collection");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("set() method is not allowed in this unmodifiable collection");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("add() method is not allowed in this unmodifiable collection");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("remove() method is not allowed in this unmodifiable collection");
    }

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException("addFirst() method is not allowed in this unmodifiable collection");
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException("addLast() method is not allowed in this unmodifiable collection");
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException("Unsupported operation \"removeFirst()\" on unmodifiable ArraySet");
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException("Unsupported operation \"removeLast()\" on unmodifiable ArraySet");
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Unsupported operation \"pollFirst()\" on unmodifiable ArraySet");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Unsupported operation \"pollLast()\" on unmodifiable ArraySet");
    }

}
