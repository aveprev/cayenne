/* ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0 
 *
 * Copyright (c) 2002 The ObjectStyle Group 
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne" 
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
package org.objectstyle.cayenne.util;

import org.apache.log4j.Logger;
import org.objectstyle.cayenne.CayenneTestCase;

/**
 * @author Andrei Adamchik
 */
public class RequestQueueTst extends CayenneTestCase {
    static Logger logObj = Logger.getLogger(RequestQueueTst.class.getName());

    /**
     * Constructor for RequestQueueTst.
     * @param arg0
     */
    public RequestQueueTst(String arg0) {
        super(arg0);
    }

    public void testDequeueSuccess() throws Exception {
        // create non-expiring queue
        QueueTestThread testCase1 =
            new QueueTestThread("thread-1", new RequestQueue(5, Integer.MAX_VALUE));
        QueueTestThread testCase2 = new QueueTestThread("thread-2", testCase1.getQueue());

        try {
            synchronized (testCase1.getLock()) {
                testCase1.start();
                testCase1.getLock().wait(1000);

                synchronized (testCase2.getLock()) {
                    testCase2.start();
                    testCase2.getLock().wait(1000);
                }

                assertEquals(2, testCase1.getQueue().getSize());

                // test dequeue 
                Object dequeueObj = new Object();
                assertTrue(testCase1.getQueue().dequeueFirst(dequeueObj));
                
                // this is needed due to some thread scheduling weirdness on Linux
                testCase1.getLock().wait(1000);
                
                RequestDequeue result = testCase1.getResult();
                assertNotNull(result);
                assertTrue(result.isDequeueSuccess());
                assertSame(dequeueObj, result.getDequeueEventObject());

                // second thread should still be waiting
                assertNull(testCase2.getResult());
                assertEquals(1, testCase1.getQueue().getSize());
            }
        } finally {
            testCase1.die();
            testCase2.die();
        }
    }

    public void testQueueFull() throws Exception {
        // create non-expiring queue
        QueueTestThread testCase1 =
            new QueueTestThread("thread-3", new RequestQueue(1, Integer.MAX_VALUE));
        QueueTestThread testCase2 = new QueueTestThread("thread-4", testCase1.getQueue());

        try {
            testCase1.start();

            // thread must exit immediately
            synchronized (testCase2.getLock()) {
                testCase2.start();

                // will release the lock for a short period of time
                // to allow thread to run
                testCase2.getLock().wait(1000);
            }

            // second thread must have finished with "QUEUE_FULL" status
            RequestDequeue result = testCase2.getResult();
            assertNotNull(result);
            assertTrue(result.isQueueFull());

            // first thread should still be waiting
            assertNull(testCase1.getResult());
        } finally {
            testCase1.die();
            testCase2.die();
        }
    }

    public void testWaitTimedOut() throws Exception {
        // create quickly expiring queue
        QueueTestThread testCase1 =
            new QueueTestThread("thread-5", new RequestQueue(1, 100));

        try {
            synchronized (testCase1.getLock()) {
                testCase1.start();

                // will release the lock for a short period of time
                // to allow thread to run
                testCase1.getLock().wait(1000);

                // thread must have finished with "WAIT_TIMED_OUT" status
                RequestDequeue result = testCase1.getResult();
                assertNotNull(result);
                assertTrue(result.isTimedOut());
                assertEquals(0, testCase1.getQueue().getSize());
            }
        } finally {
            testCase1.die();
        }
    }

    public void testThreadDied() throws Exception {
        // create non-expiring queue
        QueueTestThread testCase1 =
            new QueueTestThread("thread-6", new RequestQueue(1, Integer.MAX_VALUE));

        try {

            // thread must exit immediately
            synchronized (testCase1.getLock()) {
                testCase1.start();

                // will release the lock for a short period of time
                // to allow thread to run
                testCase1.getLock().wait(1000);

                // still waiting
                assertNull(testCase1.getResult());

                // assert that dead threads are removed from the queue
                assertEquals(1, testCase1.getQueue().getSize());
                testCase1.die();
                
                testCase1.getLock().wait(1000);
                assertEquals(0, testCase1.getQueue().getSize());
                
                
                RequestDequeue result = testCase1.getResult();
                assertNotNull(result);
                assertTrue(result.isInterrupted());
            }
        } finally {
            testCase1.die();
        }
    }

    /** A class that tests the queue in a separate thread. */
    class QueueTestThread extends Thread {
        protected RequestQueue queue;
        protected RequestDequeue result;
        protected Object lock = new Object();

        QueueTestThread(String name, RequestQueue queue) {
            super(name);
            this.queue = queue;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            // This is a stumbling block for threads to avoid
            // premature startup before caller is ready.

            // Caller would start threads while maintaining a lock
            // on this object, and after all threads are started, caller
            // would wait on the lock, until any of the finished threads
            // would call "notify"
            synchronized (lock) {
               logObj.debug("Thread started: " + this.getName());
            }

            result = queue.queueThread();

            synchronized (lock) {
                lock.notifyAll();
            }

            logObj.debug("Thread finished: " + this.getName());
        }

        public void die() {
            // this make sure that thread is not waiting forever 
            // in the queue
            if (this.isAlive()) {
                this.interrupt();
            }
        }

        /**
         * Returns the queue.
         * @return RequestQueue
         */
        public RequestQueue getQueue() {
            return queue;
        }

        /**
         * Returns the result.
         * @return RequestDequeue
         */
        public RequestDequeue getResult() {
            return result;
        }
        /**
         * Returns the lock.
         * @return Object
         */
        public Object getLock() {
            return lock;
        }
    }
}
