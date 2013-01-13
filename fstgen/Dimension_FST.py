__author__ = 'fst generator'

import FSTRuntime
import array

class Dimension_FST(FSTRuntime.FSTSerBase) :

    def __init__(self, factory): 
        self.fac = factory

    height = 0 # int
    width = 0 # int


    def encode(self, stream) :
        self.writeCInt( stream, 18)
        bools = 0
        self.writeCInt( stream, height)
        self.writeCInt( stream, width)

    def decode(self, stream) :
        bools = 0
        self.height = self.readCInt( stream )
        self.width = self.readCInt( stream )

