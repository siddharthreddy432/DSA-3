package engine.datastructures;

/**
 * Custom generic LIFO (Last-In-First-Out) Stack implementation backed by custom {@link DynamicArray}.
 *
 * <p>Architectural constraint: Zero java.util collections or helper classes are used.
 *
 * @param <T> the type of elements held in this stack
 */
public class Stack<T> {

    private final DynamicArray<T> elements;

    /**
     * Constructs an empty Stack with default initial capacity.
     */
    public Stack() {
        this.elements = new DynamicArray<>();
    }

    /**
     * Constructs an empty Stack with the specified initial capacity.
     *
     * @param initialCapacity initial capacity for backing dynamic array
     */
    public Stack(int initialCapacity) {
        this.elements = new DynamicArray<>(initialCapacity);
    }

    /**
     * Pushes an item onto the top of this stack.
     * Amortized O(1) time complexity.
     *
     * @param value the item to be pushed
     */
    public void push(T value) {
        elements.add(value);
    }

    /**
     * Removes the object at the top of this stack and returns that object as the value of this function.
     * O(1) time complexity.
     *
     * @return the object at the top of this stack
     * @throws IllegalStateException if this stack is empty
     */
    public T pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot pop from an empty Stack");
        }
        return elements.remove(elements.size() - 1);
    }

    /**
     * Looks at the object at the top of this stack without removing it from the stack.
     * O(1) time complexity.
     *
     * @return the object at the top of this stack
     * @throws IllegalStateException if this stack is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot peek an empty Stack");
        }
        return elements.get(elements.size() - 1);
    }

    /**
     * Tests if this stack is empty.
     * O(1) time complexity.
     *
     * @return true if and only if this stack contains no items; false otherwise
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Returns the number of items in this stack.
     * O(1) time complexity.
     *
     * @return the number of items in this stack
     */
    public int size() {
        return elements.size();
    }

    /**
     * Removes all elements from this stack.
     * O(n) time complexity.
     */
    public void clear() {
        elements.clear();
    }
}
