/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Get/put string data.
 * @author mrk
 *
 */
public interface PVString extends PVScalar{
    /**
     * Get the <i>String</i> value stored in the field.
     * @return string value of field.
     */
    String get();
    /**
     * Put the <i>String</i> value into the field.
     * @param value New value.
     * @throws IllegalStateException if the field is not mutable.
     */
    void put(String value);
}