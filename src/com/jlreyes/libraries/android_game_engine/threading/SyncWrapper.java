package com.jlreyes.libraries.android_game_engine.threading;

import com.jlreyes.libraries.android_game_engine.utils.math.function.Function0;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Collection of objects where each object added is intended to be handled by a
 * different thread. Internally, objects are stored in an ArrayList.
 * @author jlreyes
 */
public class SyncWrapper<E> {
    private Scheduler mScheduler;
    /**
     * The elements we are syncing
     */
    private ArrayList<E> mElems;
    private HashMap<E, Thread> mElemToThread;
    private HashMap<Thread, E> mThreadToElem;
    private MyLock mLocksLock;
    private HashMap<E, Function0<Void>> mCallbacks;

    public SyncWrapper(Scheduler scheduler) {
        this.mScheduler = scheduler;
        this.mElems = new ArrayList<E>();
        this.mElemToThread = new HashMap<E, Thread>();
        this.mThreadToElem = new HashMap<Thread, E>();
        this.mLocksLock = new MyLock();
        this.mCallbacks = new HashMap<E, Function0<Void>>();
    }

    /**
     * Add a new element.
     *
     * @param elem The element to control
     */
    public synchronized void add(final E elem) {
        if (elem == null)
            throw new RuntimeException("Null elements are not allowed.");
        synchronized (mLocksLock) {
            mElems.add(elem);
            mElemToThread.put(elem, null);
            Function0<Void> callback = new Function0<Void>() {
                public Void run() {
                    SyncWrapper.this.unattachElem(elem);
                    return null;
                }
            };
            mCallbacks.put(elem, callback);
        }
    }

    /**
     * No longer monitor the given element.
     */
    public synchronized void remove(E elem) {
        synchronized (mLocksLock) {
            if (elemIsLocked(elem) == true)
                unattachElem(elem);
            mElems.remove(elem);
            mElemToThread.remove(elem);
            mCallbacks.remove(elem);
        }
    }

    /**
     * No longer monitor any of the elements currently held by this syncwrapper
     */
    public synchronized void clear() {
        synchronized (mLocksLock) {
            for (E elem : mElems) {
                if (elemIsLocked(elem) == true) unattachElem(elem);
            }
            mElems.clear();
            mElemToThread.clear();
            mThreadToElem.clear();
            mCallbacks.clear();
        }
    }

    /**
     * Attaches the calling thread to some elem E. Unattaches whenever the
     * given function returns True.
     * returns True.
     *
     * @param f The function we will use to see if the thread is still using the
     *          elem.
     * @return An elem of type E guaranteed to not be being accessed by
     *         some thread. Returns null if there is no such elem.
     */
    public synchronized E get(Function0<Boolean> f) {
        synchronized (mLocksLock) {
            int length = mElems.size();
            for (int i = 0; i < length; i++) {
                E elem = mElems.get(i);
                if (elemIsLocked(elem) == false) {
                    if (f != null)
                        mScheduler.registerListener(f, mCallbacks.get(elem));
                    mElemToThread.put(elem, Thread.currentThread());
                    mThreadToElem.put(Thread.currentThread(), elem);
                    return elem;
                }
            }
        }
        throw new RuntimeException("No available elements to get in SyncWrapper!");
    }

    /**
     * Same as get(null)
     */
    public synchronized E get() {
        return get(null);
    }

    public synchronized int getNumElements() {
        return mElems.size();
    }

    /**
     * Passes control of the elem from the thread calling this to the given
     * thread.
     *
     * @param thread The thread we will pass control to.
     */
    public synchronized void pass(E elem, Thread thread) {
        synchronized (mLocksLock) {
            unattachElem(elem);
            unattachThread(thread);
            mElemToThread.put(elem, thread);
            mThreadToElem.put(thread, elem);
        }
    }

    /**
     * If the given thread currently holds an element, calling this
     * releases it.
     */
    public synchronized void release(Thread thread) {
        unattachThread(thread);
    }

    /**
     * Unattaches whatever thread is attached to the given elem.
     *
     * @param elem The element that the thread is attached to. NOT THREAD SAFE.
     */
    private synchronized void unattachElem(E elem) {
        Thread thread = mElemToThread.get(elem);
        mThreadToElem.put(thread, null);
        mElemToThread.put(elem, null);
    }

    /**
     * Unattaches elem is attached to the given thread.
     *
     * @param thread the element is attached to. NOT THREAD SAFE.
     */
    private synchronized void unattachThread(Thread thread) {
        E previousElem = mThreadToElem.get(thread);
        mThreadToElem.put(thread, null);
        mElemToThread.put(previousElem, null);
    }

    public synchronized boolean elemIsLocked(E elem) {
        return mElemToThread.get(elem) != null;
    }
}
