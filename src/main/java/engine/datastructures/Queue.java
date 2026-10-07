package engine.datastructures;

/**
 * Custom generic FIFO (First-In-First-Out) Queue implementation backed by a circular array.
 *
 * <p>Architectural constraint: Zero java.util collections or helper classes are used.
 * Queue indices wrap around using modulo arithmetic, ensuring true O(1) dequeue
 * operations without shifting elements. Dynamic resizing realigns elements into
 * contiguous FIFO logical order.
 *
 * @param <T> the type of elements held in this queue
 */
public class Queue<T> {

    private static final int DEFAULT_CAPACITY = 8;

    private Object[] data;
    private int front;
    private int rear;
    private int size;
    private int capacity;

    /**
     * Constructs an empty Queue with the default initial capacity (8).
     */
    public Queue() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs an empty Queue with the specified initial capacity.
     *
     * @param initialCapacity initial capacity of the circular buffer
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public Queue(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative: " + initialCapacity);
        }
        int cap = (initialCapacity == 0) ? DEFAULT_CAPACITY : initialCapacity;
        this.data = new Object[cap];
        this.capacity = cap;
        this.front = 0;
        this.rear = 0;
        this.size = 0;
    }

    /**
     * Inserts the specified element into the queue.
     * Amortized O(1) time complexity.
     *
     * @param value the element to add
     */
    public void enqueue(T value) {
        if (size == capacity) {
            resize();
        }
        data[rear] = value;
        rear = (rear + 1) % capacity;
        size++;
    }

    /**
     * Retrieves and removes the head of this queue.
     * O(1) time complexity.
     *
     * @return the head of this queue
     * @throws IllegalStateException if this queue is empty
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot dequeue from an empty Queue");
        }
        T item = (T) data[front];
        data[front] = null; // null out reference to allow garbage collection
        front = (front + 1) % capacity;
        size--;
        return item;
    }

    /**
     * Retrieves, but does not remove, the head of this queue.
     * O(1) time complexity.
     *
     * @return the head of this queue
     * @throws IllegalStateException if this queue is empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot peek an empty Queue");
        }
        return (T) data[front];
    }

    /**
     * Returns true if this queue contains no elements.
     * O(1) time complexity.
     *
     * @return true if this queue contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of elements in this queue.
     * O(1) time complexity.
     *
     * @return the number of elements in this queue
     */
    public int size() {
        return size;
    }

    /**
     * Removes all elements from this queue.
     * O(n) time complexity.
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            data[(front + i) % capacity] = null;
        }
        front = 0;
        rear = 0;
        size = 0;
    }

    /**
     * Returns the current internal capacity of the circular buffer.
     * O(1) time complexity.
     *
     * @return internal capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Resizes internal array by allocating double capacity and re-ordering elements
     * in linear FIFO order from index 0.
     */
    private void resize() {
        int newCapacity = capacity == 0 ? DEFAULT_CAPACITY : capacity * 2;
        Object[] newData = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newData[i] = data[(front + i) % capacity];
        }
        this.data = newData;
        this.front = 0;
        this.rear = size;
        this.capacity = newCapacity;
    }
}
