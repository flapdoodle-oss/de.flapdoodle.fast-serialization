__author__ = 'fst generator'

import FSTRuntime
import array

class CrossB_FST(FSTRuntime.FSTSerBase) :

    def __init__(self, factory): 
        self.fac = factory

    a = False # boolean
    aa = False # boolean
    ab = False # boolean
    ac = False # boolean
    ad = False # boolean
    ae = False # boolean
    b = False # boolean
    c = False # boolean
    d = False # boolean
    e = False # boolean
    xx = False # boolean
    aByte = 0 # byte
    aDouble = 0.0 # double
    aFloat = 0.0 # float
    aLong = 0 # long
    aShort = 0 # short
    achar = 0 # char
    anInt = 0 # int
    bigLong = None # java.lang.Long
    crossB = None # CrossB_FST
    crossBs = None # CrossB_FST[] 
    dimension = None # Dimension_FST
    dims = None # Dimension_FST[] 
    enu = None # String
    integer = None # java.lang.Integer
    list = None # java.lang.Object[]
    map = None # java.lang.Object[]
    map1 = None # java.lang.Object[]
    now = None # java.lang.Long
    object = None # java.lang.Object
    oho = None # String
    other = None # java.lang.Object[] 
    string = None # String
    strsub = None # String
    testByte = None # byte[] 
    testChar = None # char[] 
    testDouble = None # double[] 
    testFloat = None # float[] 
    testInt = None # int[] 
    testShort = None # short[] 
    testlong = None # long[] 


    def encode(self, stream) :
        self.writeCInt( stream, 109)
        bools = 0
        if self.a :
            bools |= 128
        if self.aa :
            bools |= 64
        if self.ab :
            bools |= 32
        if self.ac :
            bools |= 16
        if self.ad :
            bools |= 8
        if self.ae :
            bools |= 4
        if self.b :
            bools |= 2
        if self.c :
            bools |= 1
        self.writeU( stream, bools )
        bools = 0
        if self.d :
            bools |= 128
        if self.e :
            bools |= 64
        if self.xx :
            bools |= 32
        self.writeU( stream, bools )
        bools = 0
        writeByte( stream, aByte)
        writeCDouble( stream, aDouble)
        writeCFloat( stream, aFloat)
        writeCLong( stream, aLong)
        writeCShort( stream, aShort)
        writeCChar( stream, achar)
        self.writeCInt( stream, anInt)
        self.encodeObject( stream, bigLong )
        self.encodeObject( stream, crossB )
        self.encodeObject( stream, crossBs )
        self.encodeObject( stream, dimension )
        self.encodeObject( stream, dims )
        self.encodeObject( stream, enu )
        self.encodeObject( stream, integer )
        self.encodeObject( stream, list )
        self.encodeObject( stream, map )
        self.encodeObject( stream, map1 )
        self.encodeObject( stream, now )
        self.encodeObject( stream, object )
        self.encodeObject( stream, oho )
        self.encodeObject( stream, other )
        self.encodeObject( stream, string )
        self.encodeObject( stream, strsub )
        self.encodeObject( stream, testByte )
        self.encodeObject( stream, testChar )
        self.encodeObject( stream, testDouble )
        self.encodeObject( stream, testFloat )
        self.encodeObject( stream, testInt )
        self.encodeObject( stream, testShort )
        self.encodeObject( stream, testlong )

    def decode(self, stream) :
        bools = 0
        bools = self.readU(stream)
        self.a = (bools & 128) != 0
        self.aa = (bools & 64) != 0
        self.ab = (bools & 32) != 0
        self.ac = (bools & 16) != 0
        self.ad = (bools & 8) != 0
        self.ae = (bools & 4) != 0
        self.b = (bools & 2) != 0
        self.c = (bools & 1) != 0
        bools = self.readU(stream)
        self.d = (bools & 128) != 0
        self.e = (bools & 64) != 0
        self.xx = (bools & 32) != 0
        self.aByte = self.readS( stream )
        self.aDouble = self.readCDouble( stream )
        self.aFloat = self.readCFloat( stream )
        self.aLong = self.readCLong( stream )
        self.aShort = self.readCShort( stream )
        self.achar = self.readCChar( stream )
        self.anInt = self.readCInt( stream )
        self.bigLong = self.decodeObject( stream )
        self.crossB = self.decodeObject( stream )
        self.crossBs = self.decodeObject( stream )
        self.dimension = self.decodeObject( stream )
        self.dims = self.decodeObject( stream )
        self.enu = self.decodeObject( stream )
        self.integer = self.decodeObject( stream )
        self.list = self.decodeObject( stream )
        self.map = self.decodeObject( stream )
        self.map1 = self.decodeObject( stream )
        self.now = self.decodeObject( stream )
        self.object = self.decodeObject( stream )
        self.oho = self.decodeObject( stream )
        self.other = self.decodeObject( stream )
        self.string = self.decodeObject( stream )
        self.strsub = self.decodeObject( stream )
        self.testByte = self.decodeObject( stream )
        self.testChar = self.decodeObject( stream )
        self.testDouble = self.decodeObject( stream )
        self.testFloat = self.decodeObject( stream )
        self.testInt = self.decodeObject( stream )
        self.testShort = self.decodeObject( stream )
        self.testlong = self.decodeObject( stream )

