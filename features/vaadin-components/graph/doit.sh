#!/bin/sh
export OPENNMS_HOME=~/git/opennms/target/opennms
rm -rf ./target
../../../compile.pl
cp target/graph-17.0.0-SNAPSHOT.jar $OPENNMS_HOME/system/org/opennms/features/vaadin-components/graph/17.0.0-SNAPSHOT/graph-17.0.0-SNAPSHOT.jar
sudo $OPENNMS_HOME/bin/opennms stop
sudo rm -rf $OPENNMS_HOME/data/
sudo $OPENNMS_HOME/bin/opennms start
