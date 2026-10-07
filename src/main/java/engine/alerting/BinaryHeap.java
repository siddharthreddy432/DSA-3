package engine.alerting;

import engine.datastructures.DynamicArray;

/**
 * First-principles array-backed Binary Max-Heap priority queue.
 * <p>
 * Implemented using the project's custom {@link DynamicArray} with zero dependencies
 * on {@code java.util.PriorityQueue} or other standard collections.
 *
 * @param <T> the type of elements stored in the heap
 */
public class BinaryHeap<T> {

    private final DynamicArray<T> elements;
    private final PriorityComparator<T> comparator;

    /**
     * Constructs a BinaryHeap with a given priority comparator.
     *
     * @param comparator comparison rule defining element priority
     * @throws IllegalArgumentException if comparator is null
     */
    public BinaryHeap(PriorityComparator<T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator must not be null");
        }
        this.elements = new DynamicArray<>();
        this.comparator = comparator;
    }

    /**
     * Inserts an element into the binary heap and restores the heap property in $O(\log n)$ time.
     *
     * @param item the element to insert
     * @throws IllegalArgumentException if item is null
     */
    public void insert(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot insert null element into heap");
        }
        elements.add(item);
        siftUp(elements.size() - 1);
    }

    /**
     * Retrieves, but does not remove, the highest priority element in $O(1)$ time.
     *
     * @return the root element with highest priority
     * @throws IllegalStateException if heap is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Heap is empty");
        }
        return elements.get(0);
    }

    /**
     * Retrieves and removes the highest priority element in $O(\log n)$ time.
     *
     * @return the root element with highest priority
     * @throws IllegalStateException if heap is empty
     */
    public T extractMax() {
        if (isEmpty()) {
            throw new IllegalStateException("Heap is empty");
        }

        int lastIdx = elements.size() - 1;
        T maxElement = elements.get(0);
        T lastElement = elements.get(lastIdx);

        elements.remove(lastIdx);

        if (!elements.isEmpty()) {
            elements.set(0, lastElement);
            siftDown(0);
        }

        return maxElement;
    }

    /**
     * Alias for {@link #extractMax()}.
     *
     * @return the root element with highest priority
     */
    public T extract() {
        return extractMax();
    }

    /**
     * Returns the current number of elements in the heap.
     *
     * @return element count
     */
    public int size() {
        return elements.size();
    }

    /**
     * Checks if the heap contains no elements.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Removes all elements from the heap.
     */
    public void clear() {
        elements.clear();
    }

    /**
     * Sifts up the element at the given index until the heap property is satisfied.
     */
    private void siftUp(int index) {
        int current = index;
        while (current > 0) {
            int parent = (current - 1) / 2;
            T currItem = elements.get(current);
            T parentItem = elements.get(parent);

            if (comparator.compare(currItem, parentItem) > 0) {
                // Current item has higher priority than parent -> swap
                elements.set(current, parentItem);
                elements.set(parent, currItem);
                current = parent;
            } else {
                break;
            }
        }
    }

    /**
     * Sifts down the element at the given index until the heap property is satisfied.
     */
    private void siftDown(int index) {
        int current = index;
        int size = elements.size();

        while (true) {
            int leftChild = 2 * current + 1;
            int rightChild = 2 * current + 2;
            int highestPriority = current;

            if (leftChild < size) {
                if (comparator.compare(elements.get(leftChild), elements.get(highestPriority)) > 0) {
                    highestPriority = leftChild;
                }
            }

            if (rightChild < size) {
                if (comparator.compare(elements.get(rightChild), elements.get(highestPriority)) > 0) {
                    highestPriority = rightChild;
                }
            }

            if (highestPriority != current) {
                T temp = elements.get(current);
                elements.set(current, elements.get(highestPriority));
                elements.set(highestPriority, temp);
                current = highestPriority;
            } else {
                break;
            }
        }
    }
}
