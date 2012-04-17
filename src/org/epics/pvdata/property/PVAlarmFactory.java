/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.property;

import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Type;

public final class PVAlarmFactory implements PVAlarm{
    private PVInt pvSeverity = null;
    private PVInt pvStatus = null;
    private PVString pvMessage = null;
    private static final String noAlarmFound = "No alarm structure was located";
    private static final String notAttached = "Not attached to an alarm structure";

    /**
     * Create a PVAlarm.
     * @return The newly created PVAlarm.
     */
    public static PVAlarm create() { return new PVAlarmFactory();} 
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#attach(org.epics.pvdata.pv.PVField)
     */
    @Override
    public boolean attach(PVField pvField) {
        if(pvField.getField().getType()!=Type.structure) {
            pvField.message(noAlarmFound,MessageType.error);
            return false;
        }
        PVStructure pvStructure = (PVStructure)(pvField);
        PVInt pvInt = pvStructure.getIntField("severity");
        if(pvInt==null) {
            pvField.message(noAlarmFound,MessageType.error);
            return false;
        }
        pvSeverity = pvInt;
        pvInt = pvStructure.getIntField("status");
        if(pvInt==null) {
            pvField.message(noAlarmFound,MessageType.error);
            return false;
        }
        pvStatus = pvInt;
        PVString pvString = pvStructure.getStringField("message");
        if(pvString==null) {
            pvField.message(noAlarmFound,MessageType.error);
            return false;
        }
        pvMessage = pvString;
        return true;

    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#detach()
     */
    @Override
    public void detach() {
        pvSeverity = null;
        pvMessage = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#isAttached()
     */
    @Override
    public boolean isAttached() {
        if(pvSeverity==null || pvMessage==null) return false;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#get(org.epics.pvdata.property.Alarm)
     */
    @Override
    public void get(Alarm alarm) {
        if(pvSeverity==null || pvMessage==null) {
            throw new IllegalStateException(notAttached);
        }
        alarm.setSeverity(AlarmSeverity.getSeverity(pvSeverity.get()));
        alarm.setStatus(AlarmStatus.getStatus(pvStatus.get()));
        alarm.setMessage(pvMessage.get());
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#set(org.epics.pvdata.property.Alarm)
     */
    @Override
    public boolean set(Alarm alarm) {
        if(pvSeverity==null || pvMessage==null) {
            throw new IllegalStateException(notAttached);
        }
        if(pvSeverity.isImmutable() || pvMessage.isImmutable()) return false;
        pvSeverity.put(alarm.getSeverity().ordinal());
        pvStatus.put(alarm.getStatus().ordinal());
        pvMessage.put(alarm.getMessage());
        return true;
    }

}