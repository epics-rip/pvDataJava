/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.util.Formatter;
import java.util.Map;
import java.util.Set;

import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVAuxInfo;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Type;

/**
 * Factory to create default implementations for PVField objects.
 * The PVField instances are created via interface PVDataCreate,
 * which is obtained via a call to <i>PVDataCreateFactory.getPVDataCreate</i>.
 * @author mrk
 *
 */
public class PVDataFactory {
    private PVDataFactory() {} // don't create
    /**
     * Get the interface for PVDataCreate.
     * @return The interface.
     */
    public static synchronized PVDataCreate getPVDataCreate() {
        return PVDataCreateImpl.getPVDataCreate();
    }
    
    private static final class PVDataCreateImpl implements PVDataCreate{
        private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        private static Convert convert = ConvertFactory.getConvert();
        private static PVDataCreateImpl singleImplementation = null;
        private static synchronized PVDataCreateImpl getPVDataCreate() {
                if (singleImplementation==null) {
                    singleImplementation = new PVDataCreateImpl();
                }
                return singleImplementation;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVField(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.Field)
         */
        @Override
		public PVField createPVField(PVStructure parent, Field field) {
			switch(field.getType()) {
			case scalar: 	  return createPVScalar(parent, (Scalar)field); 
			case scalarArray: return createPVScalarArray(parent, (ScalarArray)field); 
			case structure:   return new BasePVStructure(parent,(Structure)field);
			case structureArray: return new BasePVStructureArray(parent,(StructureArray)field);
			}
            throw new IllegalArgumentException("Illegal Type");
		}
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVField(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVField)
         */
        @Override
        public PVField createPVField(PVStructure parent,PVField fieldToClone) {
            switch(fieldToClone.getField().getType()) {
            case scalar:
                return createPVScalar(parent,(PVScalar)fieldToClone);
            case scalarArray:
                return createPVScalarArray(parent,(PVScalarArray)fieldToClone);
            case structure:
                return createPVStructure(parent,(PVStructure)fieldToClone);
            case structureArray:
            	throw new IllegalArgumentException("structureArray not valid fieldToClone");
            }
            throw new IllegalStateException("Logic error in PVDataFactory");
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalar(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.Scalar)
         */
        @Override
        public PVScalar createPVScalar(PVStructure parent, Scalar scalar)
		{
           switch(scalar.getScalarType()) {
            case pvBoolean: return new BasePVBoolean(parent,scalar);
            case pvByte:    return new BasePVByte(parent,scalar);
            case pvShort:   return new BasePVShort(parent,scalar);
            case pvInt:     return new BasePVInt(parent,scalar);
            case pvLong:    return new BasePVLong(parent,scalar);
            case pvUByte:    return new BasePVUByte(parent,scalar);
            case pvUShort:   return new BasePVUShort(parent,scalar);
            case pvUInt:     return new BasePVUInt(parent,scalar);
            case pvULong:    return new BasePVULong(parent,scalar);
            case pvFloat:   return new BasePVFloat(parent,scalar);
            case pvDouble:  return new BasePVDouble(parent,scalar);
            case pvString:  return new BasePVString(parent,scalar);
            }
            throw new IllegalArgumentException(
                "Illegal Type. Must be pvBoolean,...");
	 	}
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalar(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.ScalarType)
         */
        @Override
        public PVScalar createPVScalar(PVStructure parent,ScalarType scalarType)
        {
        	Scalar scalar = fieldCreate.createScalar( scalarType);
            return createPVScalar(parent,scalar);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalar(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVScalar)
         */
        @Override
        public PVScalar createPVScalar(PVStructure parent,PVScalar scalarToClone) {
            PVScalar pvScalar = createPVScalar(parent,scalarToClone.getScalar().getScalarType());
            convert.copyScalar(scalarToClone, pvScalar);
            Map<String,PVScalar> attributes = scalarToClone.getPVAuxInfo().getInfos();
            PVAuxInfo pvAttribute = pvScalar.getPVAuxInfo();
            Set<Map.Entry<String, PVScalar>> set = attributes.entrySet();
            for(Map.Entry<String,PVScalar> entry : set) {
                String key = entry.getKey();
                PVScalar fromAttribute = attributes.get(key);
                PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                convert.copyScalar(fromAttribute, toAttribute);
            }
            return pvScalar;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalarArray(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.ScalarArray)
         */
        @Override
        public PVScalarArray createPVScalarArray(PVStructure parent,ScalarArray array)
        {
        	switch(array.getElementType()) {
            case pvBoolean: return new BasePVBooleanArray(parent,array);
            case pvByte:    return new BasePVByteArray(parent,array);
            case pvShort:   return new BasePVShortArray(parent,array);
            case pvInt:     return new BasePVIntArray(parent,array);
            case pvLong:    return new BasePVLongArray(parent,array);
            case pvUByte:    return new BasePVUByteArray(parent,array);
            case pvUShort:   return new BasePVUShortArray(parent,array);
            case pvUInt:     return new BasePVUIntArray(parent,array);
            case pvULong:    return new BasePVULongArray(parent,array);
            case pvFloat:   return new BasePVFloatArray(parent,array);
            case pvDouble:  return new BasePVDoubleArray(parent,array);
            case pvString:  return new BasePVStringArray(parent,array);
            }
            throw new IllegalArgumentException("Illegal Type. Logic error");
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalarArray(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.ScalarType)
         */
        @Override
        public PVScalarArray createPVScalarArray(PVStructure parent,ScalarType scalarType)
        {
        	return createPVScalarArray(parent, fieldCreate.createScalarArray(scalarType));
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalarArray(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVScalarArray)
         */
        @Override
        public PVScalarArray createPVScalarArray(PVStructure parent,PVScalarArray arrayToClone) {
            PVScalarArray pvArray = createPVScalarArray(parent,arrayToClone.getScalarArray().getElementType());
            convert.copyScalarArray(arrayToClone,0, pvArray,0,arrayToClone.getLength());
            Map<String,PVScalar> attributes = arrayToClone.getPVAuxInfo().getInfos();
            PVAuxInfo pvAttribute = pvArray.getPVAuxInfo();
            Set<Map.Entry<String, PVScalar>> set = attributes.entrySet();
            for(Map.Entry<String,PVScalar> entry : set) {
                String key = entry.getKey();
                PVScalar fromAttribute = attributes.get(key);
                PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                convert.copyScalar(fromAttribute, toAttribute);
            }
            return pvArray;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVStructureArray(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.StructureArray)
         */
        @Override
		public PVStructureArray createPVStructureArray(PVStructure parent,StructureArray structureArray) {
			return new BasePVStructureArray(parent,structureArray);
		}
		/* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVStructure(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.Structure)
         */
        @Override
        public PVStructure createPVStructure(PVStructure parent,Structure structure)
        {
            return new BasePVStructure(parent,structure);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVStructure(org.epics.pvdata.pv.PVStructure, java.lang.String[], org.epics.pvdata.pv.PVField[])
         */
        @Override
        public PVStructure createPVStructure(PVStructure parent,String[] fieldNames, PVField[] pvFields)
        {
            int length = pvFields.length;
            Field[] fields = new Field[length];
            for(int i=0; i<length; i++) fields[i] = pvFields[i].getField();
            Structure structure = fieldCreate.createStructure(fieldNames, fields);
            return new BasePVStructure(parent,structure,pvFields);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVStructure(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVStructure)
         */
        @Override
        public PVStructure createPVStructure(PVStructure parent,PVStructure structToClone)
        {
            if(structToClone==null) {
                throw new IllegalArgumentException("structToClone is null");
            }
            Structure structure = structToClone.getStructure();
        	PVStructure pvStructure = new BasePVStructure(parent,structure);
        	copyStructure(structToClone,pvStructure);
        	return pvStructure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#flattenPVStructure(org.epics.pvdata.pv.PVStructure)
         */
        @Override
        public PVField[] flattenPVStructure(PVStructure pvStructure) {
            Flatten temp = new Flatten(pvStructure);
            temp.initStructure(pvStructure);
            return temp.pvFields;
        }
        
        private static class Flatten {
            
            private Flatten(PVStructure pvStructure) {
                pvFields = new PVField[pvStructure.getNumberFields()];
            }
            
            private void initStructure(PVStructure pvStructure) {
                this.pvFields[currentIndex++] = pvStructure;
                PVField[] pvStructureFields = pvStructure.getPVFields();
                for(PVField pvField : pvStructureFields) {
                    if(pvField.getField().getType()==Type.structure) {
                        initStructure((PVStructure)pvField);
                    } else {
                        this.pvFields[currentIndex++] = pvField;
                    }
                }
            }
            private PVField[] pvFields = null;
            private int currentIndex = 0;
        }
        
        private void copyStructure(PVStructure from,PVStructure to)  {
            Map<String,PVScalar> attributes = from.getPVAuxInfo().getInfos();
            PVAuxInfo pvAttribute = to.getPVAuxInfo();
            Set<Map.Entry<String, PVScalar>> set = attributes.entrySet();
            for(Map.Entry<String,PVScalar> entry : set) {
                String key = entry.getKey();
                PVScalar fromAttribute = attributes.get(key);
                PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                convert.copyScalar(fromAttribute, toAttribute);
            }
            PVField[] fromFields = from.getPVFields();
            PVField[] toFields = to.getPVFields();
            if(fromFields.length!=toFields.length) {
            	Formatter formatter = new Formatter();
            	formatter.format("PVDataFactory.copyStructure number of fields differ %nfrom %s%nto %s",
            			from.toString(),to.toString());
            	String message = formatter.toString();
            	System.out.println(message);
            	throw new IllegalStateException("PVDataFactory.copyStructure number of fields differ");
            }
            for(int i=0; i<fromFields.length; i++) {
            	PVField fromPV = fromFields[i];
            	PVField toPV = toFields[i];
            	Type type = fromPV.getField().getType();
            	if(type==Type.structure) {
            		copyStructure((PVStructure)fromPV,(PVStructure)toPV);
            		continue;
            	}
            	attributes = fromPV.getPVAuxInfo().getInfos();
            	pvAttribute = toPV.getPVAuxInfo();
            	set = attributes.entrySet();
                for(Map.Entry<String,PVScalar> entry : set) {
                    String key = entry.getKey();
                    PVScalar fromAttribute = attributes.get(key);
                    PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                    convert.copyScalar(fromAttribute, toAttribute);
                }
                if(type==Type.scalar) {
                	convert.copyScalar((PVScalar)fromPV, (PVScalar)toPV);
                } else if(type==Type.scalarArray) {
                	PVScalarArray fromPVArray = (PVScalarArray)fromPV;
                	PVScalarArray toPVArray = (PVScalarArray)toPV;
                	convert.copyScalarArray(fromPVArray,0,toPVArray, 0, fromPVArray.getLength());
                } else {
                	PVStructureArray fromPVArray = (PVStructureArray)fromPV;
                	PVStructureArray toPVArray = (PVStructureArray)toPV;
                	convert.copyStructureArray(fromPVArray,toPVArray);
                }
            }
        }
        
    }
}