#!/bin/sh

OUTPUT=../docroot/doc/.resources

dot -Tpng -o$OUTPUT/flow-1.png flow-1.dot
dot -Tpng -o$OUTPUT/flow-2.png flow-2.dot