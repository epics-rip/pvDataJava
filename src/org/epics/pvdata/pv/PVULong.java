/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Get/put long data.
 * Since Java does not support unsigned the actual arguments are signed.
 * Code that calls methods of the class is responsible for integer overflow problems.
 * @author mrk
 *
 */
public interface PVULong extends PVScalar{
    /**
     * Get the <i>long</i> value stored in the field.
     *
     * @return the long value of field
     */
    long get();

    /**
     * Put the <i>long</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     *
     * @param value the new value
     */
    void put(long value);
}
