/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.misc.Queue;
import org.epics.pvData.misc.QueueCreate;
import org.epics.pvData.misc.QueueElement;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pvCopy.PVCopy;

/**
 * Factory that creates a MonitorQueue.
 * @author mrk
 *
 */
public class MonitorQueueFactory {
    
    /**
     * Create a MonitorQueue.
     * @param pvCopy The PVCopy to create the data in each queue element.
     * @param queueSize The queue size which must be at least 2.
     * @throws IllegalStateException if the queue size is not at least 2.
     * @return The MonitorQueue interface.
     */
    public static MonitorQueue create(PVCopy pvCopy, int queueSize) {
        if(queueSize<2) {
            Thread.dumpStack();
            throw new IllegalStateException("queueSize must be at least 2 ");
        }
        QueueElement<MonitorElement>[] queueElements = new QueueElement[queueSize];
        for(int i=0; i<queueElements.length; i++) {
            PVStructure pvStructure = pvCopy.createPVStructure();
            MonitorElementImlp monitorElement = new MonitorElementImlp(pvStructure);
            QueueElement<MonitorElement> queueElement = queueCreate.createQueueElement(monitorElement);
            monitorElement.setQueueElement(queueElement);
            queueElements[i] = queueElement;
        }
        Queue<MonitorElement> queue = queueCreate.create(queueElements);
        return new MonitorQueueImpl(queue);
    }
    
    private static QueueCreate<MonitorElement> queueCreate = new QueueCreate<MonitorElement>();
    
    private static class MonitorElementImlp implements MonitorElement {
        MonitorElementImlp(PVStructure pvStructure) {
            this.pvStructure = pvStructure;
            changedBitSet = new BitSet(pvStructure.getNumberFields());
            overrunBitSet = new BitSet(pvStructure.getNumberFields());
        }
        
        private final PVStructure pvStructure;
        private final BitSet changedBitSet;
        private final BitSet overrunBitSet;
        private QueueElement<MonitorElement> queueElement = null;
        
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement#getChangedBitSet()
         */
        @Override
        public BitSet getChangedBitSet() {
            return changedBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement#getOverrunBitSet()
         */
        @Override
        public BitSet getOverrunBitSet() {
            return overrunBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement#getPVStructure()
         */
        @Override
        public PVStructure getPVStructure() {
            return pvStructure;
        }
        
        private void setQueueElement(QueueElement<MonitorElement> queueElement) {
            this.queueElement = queueElement;
        }
        
        private QueueElement<MonitorElement> getQueueElement() {
            return queueElement;
        }
    }
    
    private static class MonitorQueueImpl implements MonitorQueue {
        private final Queue<MonitorElement> queue;

        MonitorQueueImpl(Queue<MonitorElement> queue) {
           this.queue = queue;
        }
       
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#clear()
         */
        @Override
        public void clear() {
           queue.clear();
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#capacity()
         */
        @Override
        public int capacity() {
            return queue.capacity();
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#getNumberFree()
         */
        @Override
        public int getNumberFree() {
            return queue.getNumberFree();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorQueue#getFree()
         */
        @Override
        public MonitorElement getFree() {
            QueueElement<MonitorElement> queueElement = queue.getFree();
            if(queueElement==null) return null;
            return queueElement.getObject();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorQueue#setUsed(org.epics.pvData.monitor.MonitorElement)
         */
        @Override
        public void setUsed(MonitorElement monitorElement) {
            MonitorElementImlp temp = (MonitorElementImlp)monitorElement;
            queue.setUsed(temp.getQueueElement());
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorQueue#getUsed()
         */
        @Override
        public MonitorElement getUsed() {
            QueueElement<MonitorElement> queueElement = queue.getUsed();
            if(queueElement==null) return null;
            return queueElement.getObject();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorQueue#releaseUsed(org.epics.pvData.monitor.MonitorElement)
         */
        @Override
        public void releaseUsed(MonitorElement monitorElement) {
            MonitorElementImlp temp = (MonitorElementImlp)monitorElement;
            queue.releaseUsed(temp.getQueueElement());
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return queue.toString();
        }
    }
}