package engine.datastructures;

/**
 * Custom generic singly linked list implementation with head and tail pointers.
 *
 * <p>Architectural constraint: Zero java.util collections or helper classes are used.
 * Nodes and pointer manipulations are maintained directly.
 *
 * @param <T> the type of elements held in this list
 */
public class LinkedList<T> {

    /**
     * Internal node storing data and reference to next node.
     */
    private static class Node<E> {
        E data;
        Node<E> next;

        Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /**
     * Constructs an empty LinkedList.
     */
    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Inserts the specified element at the beginning of this list.
     * O(1) time complexity.
     *
     * @param value the element to add
     */
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value, head);
        head = newNode;
        if (tail == null) {
            tail = head;
        }
        size++;
    }

    /**
     * Appends the specified element to the end of this list.
     * O(1) time complexity.
     *
     * @param value the element to add
     */
    public void addLast(T value) {
        Node<T> newNode = new Node<>(value, null);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Returns the element at the specified position in this list.
     * O(n) time complexity.
     *
     * @param index index of the element to return
     * @return the element at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size)
     */
    public T get(int index) {
        checkElementIndex(index);
        return getNode(index).data;
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     * O(n) time complexity.
     *
     * @param index index of the element to replace
     * @param value element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size)
     */
    public T set(int index, T value) {
        checkElementIndex(index);
        Node<T> target = getNode(index);
        T oldValue = target.data;
        target.data = value;
        return oldValue;
    }

    /**
     * Removes and returns the first element from this list.
     * O(1) time complexity.
     *
     * @return the first element from this list
     * @throws IllegalStateException if this list is empty
     */
    public T removeFirst() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot remove from empty LinkedList");
        }
        T removed = head.data;
        head = head.next;
        size--;
        if (size == 0) {
            tail = null;
        }
        return removed;
    }

    /**
     * Removes and returns the last element from this list.
     * O(n) time complexity for singly linked list traversal to find previous node.
     *
     * @return the last element from this list
     * @throws IllegalStateException if this list is empty
     */
    public T removeLast() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot remove from empty LinkedList");
        }
        if (size == 1) {
            T removed = head.data;
            head = null;
            tail = null;
            size = 0;
            return removed;
        }

        Node<T> current = head;
        while (current.next != tail) {
            current = current.next;
        }
        T removed = tail.data;
        current.next = null;
        tail = current;
        size--;
        return removed;
    }

    /**
     * Removes and returns the element at the specified position in this list.
     * Shifts any subsequent elements to the left.
     * O(n) time complexity.
     *
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size)
     */
    public T remove(int index) {
        checkElementIndex(index);
        if (index == 0) {
            return removeFirst();
        }
        if (index == size - 1) {
            return removeLast();
        }

        Node<T> prev = head;
        for (int i = 0; i < index - 1; i++) {
            prev = prev.next;
        }
        Node<T> target = prev.next;
        T removed = target.data;
        prev.next = target.next;
        size--;
        return removed;
    }

    /**
     * Returns true if this list contains the specified element.
     * O(n) time complexity.
     *
     * @param value element whose presence in this list is to be tested
     * @return true if this list contains the specified element
     */
    public boolean contains(T value) {
        Node<T> current = head;
        while (current != null) {
            if (elementEquals(value, current.data)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Returns the number of elements in this list.
     * O(1) time complexity.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this list contains no elements.
     * O(1) time complexity.
     *
     * @return true if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all elements from this list.
     * Unlinks all nodes to allow garbage collection.
     * O(n) time complexity.
     */
    public void clear() {
        Node<T> current = head;
        while (current != null) {
            Node<T> next = current.next;
            current.data = null;
            current.next = null;
            current = next;
        }
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Helper to retrieve the node at the specified index.
     */
    private Node<T> getNode(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
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
    private boolean elementEquals(T a, T b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
