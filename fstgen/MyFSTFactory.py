__author__ = 'fst generator'

import Dimension_FST
import Cross_FST
import CrossB_FST
import FSTRuntime
import array

class MyFSTFactory(FSTRuntime.FSTPyFactory) : 

    CID_BYTE_ARR = 41
    CID_CHAR_ARR = 46
    CID_DOUBLE_ARR = 79
    CID_FLOAT_ARR = 70
    CID_INT_ARR = 60
    CID_LONG_ARR = 87
    CID_CROSS_FST_ARR = 108
    CID_CROSSB_FST_ARR = 110
    CID_DIMENSION_FST_ARR = 113
    CID_JAVA_LANG_BOOLEAN_ARR = 89
    CID_JAVA_LANG_BYTE_ARR = 88
    CID_JAVA_LANG_CHARACTER_ARR = 90
    CID_JAVA_LANG_DOUBLE_ARR = 95
    CID_JAVA_LANG_FLOAT_ARR = 94
    CID_JAVA_LANG_INTEGER_ARR = 92
    CID_JAVA_LANG_LONG_ARR = 93
    CID_JAVA_LANG_OBJECT_ARR = 37
    CID_JAVA_LANG_SHORT_ARR = 91
    CID_JAVA_LANG_STRING_ARR = 98
    CID_ARRAYLIST_FST_ARR = 99
    CID_JAVA_UTIL_DATE_ARR = 96
    CID_HASHMAP_FST_ARR = 104
    CID_HASHSET_FST_ARR = 102
    CID_HASHTABLE_FST_ARR = 105
    CID_LINKEDLIST_FST_ARR = 103
    CID_VECTOR_FST_ARR = 100
    CID_CONCURRENTHASHMAP_FST_ARR = 106
    CID_SHORT_ARR = 52
    CID_BOOLEAN_ARR = 86
    CID_CROSS_FST = 107
    CID_DE_RUEDIGERMOELLER_SERIALIZATION_TESTCLASSES_CROSSLANGUAGE_CROSS_OHO = 112
    CID_DE_RUEDIGERMOELLER_SERIALIZATION_TESTCLASSES_CROSSLANGUAGE_CROSS_TEST = 111
    CID_CROSSB_FST = 109
    CID_DIMENSION_FST = 18
    CID_JAVA_LANG_BOOLEAN = 12
    CID_JAVA_LANG_BYTE = 3
    CID_JAVA_LANG_CHARACTER = 11
    CID_JAVA_LANG_DOUBLE = 8
    CID_JAVA_LANG_FLOAT = 7
    CID_JAVA_LANG_INTEGER = 5
    CID_JAVA_LANG_LONG = 6
    CID_JAVA_LANG_OBJECT = 36
    CID_JAVA_LANG_SHORT = 4
    CID_STRING = 97
    CID_ARRAYLIST_FST = 15
    CID_JAVA_UTIL_DATE = 23
    CID_HASHMAP_FST = 14
    CID_HASHSET_FST = 101
    CID_HASHTABLE_FST = 31
    CID_LINKEDLIST_FST = 27
    CID_VECTOR_FST = 30
    CID_CONCURRENTHASHMAP_FST = 16

    def instantiate(self, clzId, stream, serbase, streampos) : 
        len = 0
        if (clzId == self.CID_BYTE_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jbytearr(len)
        if (clzId == self.CID_CHAR_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jchararr(len)
        if (clzId == self.CID_DOUBLE_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jdoublearr(len)
        if (clzId == self.CID_FLOAT_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jfloatarr(len)
        if (clzId == self.CID_INT_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jintarr(len)
        if (clzId == self.CID_LONG_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jlongarr(len)
        if (clzId == self.CID_CROSS_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len) # Cross_FST[]
        if (clzId == self.CID_CROSSB_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len) # CrossB_FST[]
        if (clzId == self.CID_DIMENSION_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len) # Dimension_FST[]
        if (clzId == self.CID_JAVA_LANG_BOOLEAN_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jbooleanarr(len)
        if (clzId == self.CID_JAVA_LANG_BYTE_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jbytearr(len)
        if (clzId == self.CID_JAVA_LANG_CHARACTER_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jchararr(len)
        if (clzId == self.CID_JAVA_LANG_DOUBLE_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jdoublearr(len)
        if (clzId == self.CID_JAVA_LANG_FLOAT_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jfloatarr(len)
        if (clzId == self.CID_JAVA_LANG_INTEGER_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jintarr(len)
        if (clzId == self.CID_JAVA_LANG_LONG_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jlongarr(len)
        if (clzId == self.CID_JAVA_LANG_OBJECT_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_JAVA_LANG_SHORT_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jshortarr(len)
        if (clzId == self.CID_JAVA_LANG_STRING_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jchararr(len)
        if (clzId == self.CID_ARRAYLIST_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_JAVA_UTIL_DATE_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len) # java.lang.Long
        if (clzId == self.CID_HASHMAP_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_HASHSET_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_HASHTABLE_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_LINKEDLIST_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_VECTOR_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_CONCURRENTHASHMAP_FST_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_SHORT_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jshortarr(len)
        if (clzId == self.CID_BOOLEAN_ARR) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jbooleanarr(len)
        if (clzId == self.CID_CROSS_FST) :
            return Cross_FST.Cross_FST(self)
        if (clzId == self.CID_CROSSB_FST) :
            return CrossB_FST.CrossB_FST(self)
        if (clzId == self.CID_DIMENSION_FST) :
            return Dimension_FST.Dimension_FST(self)
        if (clzId == self.CID_ARRAYLIST_FST) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_HASHMAP_FST) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_HASHSET_FST) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_HASHTABLE_FST) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_LINKEDLIST_FST) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_VECTOR_FST) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_CONCURRENTHASHMAP_FST) :
            len = serbase.readCInt(stream)
            return FSTRuntime.jarray(len)
        if (clzId == self.CID_JAVA_LANG_LONG) :
            return serbase.readCLong(stream)
        if (clzId == self.CID_JAVA_LANG_BYTE) :
            return serbase.readS(stream)
        if (clzId == self.CID_JAVA_LANG_CHARACTER) :
            return serbase.readCChar(stream)
        if (clzId == self.CID_JAVA_LANG_INTEGER) :
            return serbase.readCInt(stream)
        if (clzId == self.CID_JAVA_LANG_SHORT) :
            return serbase.readCShort(stream)
        if (clzId == self.CID_JAVA_LANG_FLOAT) :
            return serbase.readCFloat(stream)
        if (clzId == self.CID_JAVA_LANG_DOUBLE) :
            return serbase.readCDouble(stream)
        if (clzId == self.CID_JAVA_UTIL_DATE) :
            return serbase.readCLong(stream)
        if (clzId == self.CID_STRING) :
            return serbase.readStringUTF(stream)
        print 'unknown class id:', clzId
        return None

            #default: throw new RuntimeException("unknown class id:"+clzId);
    
    def getId(self, clazz) :
        return 0
