from collections import Sequence
import io
import struct

__author__ = 'ruedi'

class FSTPyFactory:

    objectMap = {}

    def decodeFromStream(self,inputStream):
        base = FSTSerBase(self)
        res = base.decodeObject(inputStream)
        self.reset()
        return res

    def reset(self):
        self.objectMap.clear()

    def instantiate(self, clz, stream, serbase, streampos ):
        return None

class jarray:
    length = 0

    def __init__(self, len):
        self.length = len
        self.arr = []
        for i in range(len):
            self.arr.append(None)

    def __getitem__(self,index):
        return self.arr[index]

    def __setitem__(self,index,object):
        self.arr[index] = object

    def read(self,serbase,stream):
        return serbase.decodeObject(stream)

class jbytearr(jarray) :
    def read(self,serbase,stream):
        return serbase.readS(stream)

class jbooleanarr(jarray) :
    def read(self,serbase,stream):
        if ( serbase.readS(stream) ) :
            return True
        else :
            return False

class jchararr(jarray) :
    def read(self,serbase,stream):
        return serbase.readCChar(stream)

class jshortarr(jarray) :
    def read(self,serbase,stream):
        return serbase.readShort(stream)

class jintarr(jarray) :
    def read(self,serbase,stream):
        return serbase.readCInt(stream)

class jlongarr(jarray) :
    def read(self,serbase,stream):
        return serbase.readLong(stream)

class jfloatarr(jarray) :
    def read(self,serbase,stream):
        return serbase.readCFloat(stream)

class jdoublearr(jarray) :
    def read(self,serbase,stream):
        return serbase.readCDouble(stream)



class FSTSerBase:

    BIG_BOOLEAN_FALSE = -17
    BIG_BOOLEAN_TRUE = -16
    BIG_INT = -9
    COPYHANDLE = -8
    HANDLE = -7
    ENUM = -6
    ARRAY = -5
    NULL = -1
    OBJECT = 0

    def __init__(self, factory):
        self.fac = factory

    def readU(self, stream):
        return struct.unpack('!B',stream.read(1))[0]

    def readS(self, stream):
        return struct.unpack('!b',stream.read(1))[0]

    def readShort(self, stream):
        return struct.unpack('!h',stream.read(2))[0]

    def readCShort(self, stream ):
        head = self.readU(stream)
        if (head >= 0 and head < 255):
            return head
        return self.readShort(stream)

    def readInt(self, stream):
        return struct.unpack('!i',stream.read(4))[0]

    def readCInt(self, stream ):
        head = self.readS(stream)
        # -128 = short byte, -127 == 4 byte
        if (head > -127 and head <= 127):
            return head
        if (head == -128):
            return self.readShort(stream)
        return self.readInt(stream)

    def readCDouble(self, stream):
        return struct.unpack('!d',stream.read(8))[0]

    def readCFloat(self, stream):
        return struct.unpack('!f',stream.read(4))[0]

    def readCLong(self, stream):
        head = self.readS(stream)
        if (head > -126 and head <= 127):
            return head
        if ( head == -128 ) :
            return self.readShort(stream)
        if ( head == -127 ) :
            return self.readInt(stream)
        return self.readLong(stream)

    def readLong(self, stream):
        return struct.unpack('!q',stream.read(8))[0]

    def readCChar(self, stream):
        head = self.readU(stream)
        if ( head >= 0 and head < 255 ) :
            return head
        return self.readChar(stream)

    def readChar(self, stream):
        return struct.unpack('!H',stream.read(2))[0]

    def readStringUTF(self, stream):
        len = self.readCInt(stream)
        res = bytearray()
        for i in range(len) :
            res.append(self.readCChar(stream))
        return res

    def readArray(self, array, stream):
        for i in range(array.length) :
            array[i] = array.read(self,stream)

    def decodeObject(self, stream):
        streampos = stream.tell()
        header = self.readS(stream)
        if header == self.OBJECT:
            clz = self.readCShort(stream)
            obj = self.fac.instantiate(clz, stream, self, streampos)
            if obj != None:
                self.fac.objectMap[streampos] = obj;
            if isinstance(obj,FSTSerBase):
                obj.decode(stream)
            elif isinstance(obj, jarray ):
                self.readArray(obj,stream)
            else:
                pass
                #throw new RuntimeException("unknown class id "+clz);
            return obj
        elif header == self.NULL:
            return None
        elif header == self.HANDLE:
            ha = self.readCInt(stream)
            return self.fac.objectMap.get(ha)
        elif header == self.BIG_INT:
            return self.readCInt(stream)
        elif header == self.ENUM:
            clzId = self.readCShort(stream) # skip enum clz
            s = self.readStringUTF(stream)
            fac.objectMap[streampos] = s
            return s
        elif header == self.ARRAY:
            clsId = self.readCShort(stream)
            obj = self.fac.instantiate(clsId, stream, self, streampos)
            self.readArray(obj, stream)
            return obj
        elif header == self.COPYHANDLE:
            pass
            #throw new RuntimeException("class has been written using a non-cross-language configuration");
        else:
            return None

fac = FSTPyFactory()

stream = io.FileIO('\\tmp\\crosstest.oos')
print( 'pok %s' % fac.decodeFromStream(stream) )