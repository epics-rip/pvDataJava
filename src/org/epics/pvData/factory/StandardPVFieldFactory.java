/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.pvData.factory;

import org.epics.pvData.pv.*;

/**
 * @author mrk
 *
 */
public final class  StandardPVFieldFactory {
	public static synchronized StandardPVField getStandardPVField()
	{
		if(impl==null) {
			impl = new StandardPVFieldImpl();
		}
		return impl;
	}
	private static StandardPVFieldImpl impl = null;
	private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static StandardField standardField = StandardFieldFactory.getStandardField();
	private static final class StandardPVFieldImpl implements StandardPVField
	{
		StandardPVFieldImpl(){}
		
		private PVStructure create(PVStructure parent,String fieldName,Field field,String properties) {
            boolean gotAlarm = false;
            boolean gotTimeStamp = false;
            boolean gotDisplay = false;
            boolean gotControl = false;
            int nextra = 0;
            if(properties.contains("alarm"))  {gotAlarm = true; nextra++;}
            if(properties.contains("timeStamp"))  {gotTimeStamp = true; nextra++;}
            if(properties.contains("display")) {gotDisplay = true; nextra++;}
            if(properties.contains("control")) {gotControl = true; nextra++;}
            if(nextra==0) {
                throw new IllegalStateException("PVStructure.getSubField: Logic error");
            }
            String[] fieldNames = new String[nextra + 1];
            Field[] fields = new Field[nextra + 1];
            fieldNames[0] = fieldName;
            fields[0] = field;
            int index = 1;
            if(gotAlarm) {
                fieldNames[index] = "alarm";
                fields[index++] = standardField.alarm();
            }
            if(gotTimeStamp) {
                fieldNames[index] = "timeStamp";
                fields[index++] = standardField.timeStamp();
            }
            if(gotDisplay) {
                fieldNames[index] = "display";
                fields[index++] = standardField.display();
            }
            if(gotControl) {
                fieldNames[index] = "control";
                fields[index++] = standardField.control();
            }
            Structure structure = fieldCreate.createStructure(fieldNames, fields);
            return pvDataCreate.createPVStructure(parent, structure);
        }
		@Override
		public PVStructure scalar(PVStructure parent,String fieldName,ScalarType scalarType,String properties)
		{
			Field field = fieldCreate.createScalar(scalarType);
			return create(parent,fieldName,field,properties);
		}
		@Override
		public PVStructure scalarArray(PVStructure parent,String fieldName,ScalarType elementType, String properties)
		{
		    ScalarArray field = standardField.scalarArray(elementType);
            return create(parent,fieldName,field,properties);
		}
		@Override
		public PVStructure structureArray(PVStructure parent,String fieldName,Structure structure,String properties)
		{
		    StructureArray field = standardField.structureArray(structure);
		    return create(parent,fieldName,field,properties);
		}
		@Override
		public PVStructure enumerated(PVStructure parent,String[] choices)
		{
			Structure field = standardField.enumerated();
			PVStructure pvStructure = pvDataCreate.createPVStructure(parent,field);
			PVStringArray pvChoices = (PVStringArray)pvStructure.getSubField(1);
			pvChoices.put(0,choices.length, choices, 0);
			pvChoices.setImmutable();
			return pvStructure;
		}
		@Override
		public PVStructure enumerated(PVStructure parent,String fieldName,String[] choices,String properties)
		{
			return null;
		}
	}
}


