import io
import MyFSTFactory

__author__ = 'ruedi'

fac = MyFSTFactory.MyFSTFactory()

stream = io.FileIO('\\tmp\\crosstest.oos')
print( 'pok %s' % fac.decodeFromStream(stream) )
