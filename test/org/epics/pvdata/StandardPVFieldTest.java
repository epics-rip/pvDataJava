/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.*;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.*;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class StandardPVFieldTest extends TestCase {
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
    private static final StandardField standardField = StandardFieldFactory.getStandardField();
	
    static private void print(String name,String value) {
        System.out.println();
        System.out.println(name);
        System.out.println(value);
    }
    
    public static void testSimple() {     
        PVStructure pvStructure = standardPVField.scalar(ScalarType.pvDouble,"alarm,timeStamp.display,control,valueAlarm");
        PVDouble pvValue = pvStructure.getDoubleField("value");
        pvValue.put(10.0);
        PVInt pvSeverity = pvStructure.getIntField("alarm.severity");
        pvSeverity.put(2);
        PVString pvMessage = pvStructure.getStringField("alarm.message");
        pvMessage.put("test message");
        print("scalarTest",pvStructure.toString());
        pvStructure = standardPVField.scalar(ScalarType.pvBoolean,"alarm,timeStamp,valueAlarm");
        print("booleanTest",pvStructure.toString());
        String[] choices = {"one","two","three"};
        pvStructure = standardPVField.enumerated(choices, "alarm,timeStamp,valueAlarm");
        print("enumeratedTest",pvStructure.toString());
        pvStructure = standardPVField.scalarArray(ScalarType.pvBoolean,"alarm,timeStamp");
        print("scalarArrayTest",pvStructure.toString());
        Structure structure = standardField.scalar(ScalarType.pvDouble, "alarm,timeStamp");
        pvStructure = standardPVField.structureArray(structure,"alarm,timeStamp");
        int num = 2;
        PVStructure[] pvStructures = new PVStructure[num];
        for(int i=0; i<num; i++) {
            pvStructures[i] = pvDataCreate.createPVStructure(structure);
        }
        PVStructureArray pvStructureArray = pvStructure.getStructureArrayField("value");
        pvStructureArray.put(0, num, pvStructures, 0);
        print("structureArrayTest",pvStructureArray.toString());
    }
}

