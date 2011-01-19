#!/bin/sh

OUTPUT=../docroot/doc/.resources

dot -Tpng -o$OUTPUT/flow-1.png  flow-1.dot
dot -Tpng -o$OUTPUT/flow-2.png  flow-2.dot
dot -Tpng -o$OUTPUT/flow-3a.png flow-3a.dot
dot -Tpng -o$OUTPUT/flow-3b.png flow-3b.dot
dot -Tpng -o$OUTPUT/flow-4a.png flow-4a.dot
dot -Tpng -o$OUTPUT/flow-4b.png flow-4b.dot
dot -Tpng -o$OUTPUT/flow-5a.png flow-5a.dot
dot -Tpng -o$OUTPUT/flow-5b.png flow-5b.dot

dot -Tpng -o$OUTPUT/implementation-1.png  implementation-1.dot
