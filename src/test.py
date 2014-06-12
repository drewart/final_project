#!/usr/bin/python
from pykeyboard import PyKeyboard

k = PyKeyboard()

k.type_string('javac FileSystem.java Inode.java SuperBlock.java FileTable.java Directory.java && java Boot')
k.tap_key(k.enter_key)

k.type_string('l Test5')
k.tap_key(k.enter_key)
