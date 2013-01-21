package com.jlreyes.libraries.android_game_engine.datastructures;

import com.jlreyes.libraries.android_game_engine.utils.exceptions.OverflowException;

import java.lang.reflect.Array;
import java.util.*;

/**
 * HashSet optimized for the game-loop that uses arrays as a backing. Good for
 * when you need a persistent set in the game loop. Make sure the initial
 * capacity is set large enough, or a larger number of allocations will occur
 * than intended. As long as the capacity is never exceeded, adding to this
 * set results in no allocations at all.
 *
 * Uses chaining to deal with collisions.
 *
 * @author jlreyes
 */
public class MinAllocHashSet<E> implements Set<E> {
    public class MinAllocHashSetIterator implements Iterator<E> {
        private int mCurrentChain = 0;
        private int mCurrentChainLink = 0;
        private E mLastObjectReturned = null;

        public boolean hasNext() {
            int currentChain = mCurrentChain;
            int currentChainLink = mCurrentChainLink;
            try {
                while (true) {
                    ArrayList<E> chain = mTable[currentChain];
                    try {
                        chain.get(currentChainLink + 1);
                        return true;
                    } catch (IndexOutOfBoundsException e) {
                        currentChain += 1;
                        currentChainLink = 0;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }

        public E next() {
            try {
                while (true) {
                    ArrayList<E> chain = mTable[mCurrentChain];
                    try {
                        E result = chain.get(mCurrentChainLink + 1);
                        mCurrentChainLink += 1;
                        mLastObjectReturned = result;
                        return result;
                    } catch (IndexOutOfBoundsException e) {
                        mCurrentChain += 1;
                        mCurrentChainLink = 0;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }
        public void reset() {
            mCurrentChain = 0;
            mCurrentChainLink = 0;
            mLastObjectReturned = null;
        }


        public void remove() {
            if (mLastObjectReturned == null) throw new IllegalStateException();
            MinAllocHashSet.this.remove(mLastObjectReturned);
            mLastObjectReturned = null;
        }
    }

    /**
     * The default initial capacity for a MinAllocHashSet that was not supplied
     * with an initial capacity.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The default load factor for a MinAllocHashSet that was not supplied with
     * an load factor.
     */
    private static final int DEFAULT_LOAD_FACTOR = 2;

    /**
     * The hashtable backing this hash set. We use a simulated unbounded array
     * of arraylists for the table.
     */
    private ArrayList<E>[] mTable;

    /**
     * The number of elements stored in this set.
     */
    private int mNumElems;

    /**
     * The load factor for this hash set. That is, when
     * this.mTable.length > this.mLoadFactor, we will resize
     * the hash table.
     */
    private float mLoadFactor;

    public MinAllocHashSet() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public MinAllocHashSet(int initialCapacity, float loadFactor) {
        if (initialCapacity <= 0)
            throw new IllegalArgumentException("Initial capacity must be an " +
                                               " integer greater than 0");
        if (loadFactor <= 0)
            throw new IllegalArgumentException("Load factor must be greater " +
                                               "than 0.");
        this.mTable = new ArrayList[initialCapacity];
        this.mNumElems = 0;
        this.mLoadFactor = loadFactor;
        /* Filling the table with empty chains */
        for (int i = 0; i < initialCapacity; i++)
            mTable[i] = new ArrayList<E>(Math.round(mLoadFactor) << 1);
    }

    public boolean add(E elem) {
        int hash = elem.hashCode() % mTable.length;
        /* Navigate chain to see if element already exists */
        ArrayList<E> chain = mTable[hash];
        int chainLength = chain.size();
        for (int i = 0; i < chainLength; i++)
            if (chain.get(i).equals(elem) == true) return false;
        /* The element does not already exist, so we add it */
        chain.add(elem);
        mNumElems += 1;
        /* Calculate the load factor and adjust the table if necessary */
        float currentLoadFactor = (float) mNumElems / (float) mTable.length;
        if (currentLoadFactor > mLoadFactor) doubleTableSize();
        return true;
    }

    private void doubleTableSize() {
        /* Old table */
        int oldCapacity = mTable.length;
        ArrayList<E>[] oldTable = mTable;
        /* New table */
        int newCapacity = oldTable.length << 1;
        if (newCapacity < 0) throw new OverflowException();
        @SuppressWarnings("unchecked")
        ArrayList<E>[] newTable = new ArrayList[newCapacity];
        /* Filling the table with empty chains */
        for (int i = 0; i < newCapacity; i++)
            newTable[i] = new ArrayList<E>(Math.round(mLoadFactor) << 1);
        /* Rehashing all the old table elements into this new table */
        for (int i = 0; i < oldCapacity; i++) {
            ArrayList<E> chain = mTable[i];
            int chainLength = chain.size();
            for (int j = 0; j < chainLength; j++) {
                E elem = chain.get(j);
                int hash = elem.hashCode() % newCapacity;
                newTable[hash].add(elem);
            }
            /* Preventing memory leaks */
            chain.clear();
            mTable[i] = null;
        }
        /* Finishing up */
        mTable = newTable;
    }

    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends E> collection) {
        if (collection.isEmpty() == true) return false;
        Object[] array = collection.toArray();
        for (Object o : array) add((E) o);
        return true;
    }

    public void clear() {
        int length = mTable.length;
        for (int i = 0; i < length; i++) mTable[i] = new ArrayList<E>();
        mNumElems = 0;
    }

    public boolean contains(Object object) {
        int hash = object.hashCode() % mTable.length;
        ArrayList<E> chain = mTable[hash];
        int chainLength = chain.size();
        for (int i = 0; i < chainLength; i++)
            if (object.equals(chain.get(i)) == true) return true;
        return false;
    }

    public boolean containsAll(Collection<?> collection) {
        Object[] array = collection.toArray();
        for (Object o : array)
            if (contains(o) == false) return false;
        return true;
    }

    public boolean isEmpty() {
        return mNumElems == 0;
    }

    public Iterator<E> iterator() {
        return new MinAllocHashSetIterator();
    }

    public boolean remove(Object object) {
        int hash = object.hashCode() % mTable.length;
        return mTable[hash].remove(object);
    }

    public boolean removeAll(Collection<?> collection) {
        boolean modified = false;
        Object[] array = collection.toArray();
        for (Object o : array) modified |= remove(o);
        return modified;
    }

    public boolean retainAll(Collection<?> collection) {
        Object[] array = collection.toArray();
        int length = array.length;
        for (int i = 0; i < length; i++)
            if (contains(array[i]) == false) remove(array[i]);
        return false;
    }

    public int size() {
        return mNumElems;
    }

    public int capacity() {
        return mTable.length;
    }

    public float loadFactor() {
        return (float) mNumElems / (float) mTable.length;
    }

    public Object[] toArray() {
        int numChains = mTable.length;
        int resultIndex = 0;
        Object[] result = new Object[mNumElems];
        for (int i = 0; i < numChains; i++) {
            ArrayList<E> chain = mTable[i];
            int chainLength = chain.size();
            for (int j = 0; j < chainLength; j++) {
                result[resultIndex] = chain.get(j);
                resultIndex += 1;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        T[] result = array;
        if (result.length < mNumElems)
            result = (T[]) Array.newInstance(array.getClass().getComponentType(),
                                             mNumElems);
        int numChains = mTable.length;
        int resultIndex = 0;
        for (int i = 0; i < numChains; i++) {
            ArrayList<E> chain = mTable[i];
            int chainLength = chain.size();
            for (int j = 0; j < chainLength; j++) {
                result[resultIndex] = (T) chain.get(j);
                resultIndex += 1;
            }
        }
        return result;
    }
}
