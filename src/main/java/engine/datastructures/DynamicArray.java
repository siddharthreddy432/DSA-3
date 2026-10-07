package engine.datastructures;

/**
 * Custom generic dynamic array implementation backed by a primitive Object array.
 *
 * <p>Architectural constraint: Zero java.util collections or helper classes are used.
 * All memory management, resizing, boundary validation, and element shifting are
 * implemented directly.
 *
 * @param <T> the type of elements held in this dynamic array
 */
public class DynamicArray<T> {

    private static final int DEFAULT_CAPACITY = 10;

    private Object[] data;
    private int size;
    private int capacity;

    /**
     * Constructs an empty DynamicArray with the default initial capacity (10).
     */
    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs an empty DynamicArray with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the internal array
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative: " + initialCapacity);
        }
        int cap = (initialCapacity == 0) ? DEFAULT_CAPACITY : initialCapacity;
        this.data = new Object[cap];
        this.capacity = cap;
        this.size = 0;
    }

    /**
     * Appends the specified element to the end of this array.
     * Amortized O(1) time complexity.
     *
     * @param value element to be appended
     */
    public void add(T value) {
        if (size == capacity) {
            grow();
        }
        data[size++] = value;
    }

    /**
     * Inserts the specified element at the specified position in this array.
     * Shifts the element currently at that position and any subsequent elements to the right.
     * O(n) time complexity.
     *
     * @param index index at which the specified element is to be inserted
     * @param value element to be inserted
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt; size)
     */
    public void add(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (size == capacity) {
            grow();
        }
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }
        data[index] = value;
        size++;
    }

    /**
     * Returns the element at the specified position in this array.
     * O(1) time complexity.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this array
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size)
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkElementIndex(index);
        return (T) data[index];
    }

    /**
     * Replaces the element at the specified position in this array with the specified element.
     * O(1) time complexity.
     *
     * @param index index of the element to replace
     * @param value element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size)
     */
    @SuppressWarnings("unchecked")
    public T set(int index, T value) {
        checkElementIndex(index);
        T oldValue = (T) data[index];
        data[index] = value;
        return oldValue;
    }

    /**
     * Removes the element at the specified position in this array.
     * Shifts any subsequent elements to the left.
     * O(n) time complexity.
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the array
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size)
     */
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkElementIndex(index);
        T removedValue = (T) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[size - 1] = null; // null out reference to allow garbage collection
        size--;
        return removedValue;
    }

    /**
     * Returns true if this array contains the specified element.
     * O(n) time complexity.
     *
     * @param value element whose presence in this array is to be tested
     * @return true if this array contains the specified element
     */
    public boolean contains(T value) {
        for (int i = 0; i < size; i++) {
            if (elementEquals(value, data[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of elements in this array.
     * O(1) time complexity.
     *
     * @return the number of elements in this array
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this array contains no elements.
     * O(1) time complexity.
     *
     * @return true if this array contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all elements from this array. The array will be empty after this call returns.
     * Internal array slots are set to null for garbage collection.
     * O(n) time complexity.
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
        size = 0;
    }

    /**
     * Returns the current internal capacity of this dynamic array.
     * O(1) time complexity.
     *
     * @return internal capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Doubles the capacity of the internal array and copies all existing elements.
     */
    private void grow() {
        int newCapacity = capacity == 0 ? DEFAULT_CAPACITY : capacity * 2;
        Object[] newData = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }
        this.data = newData;
        this.capacity = newCapacity;
    }

    /**
     * Validates that the index is within [0, size - 1].
     */
    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    /**
     * Null-safe element equality check without java.util.Objects.
     */
    private boolean elementEquals(T a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
