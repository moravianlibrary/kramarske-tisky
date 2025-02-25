#!/bin/bash

CONFIG=~/dkt/config/config.properties

echo "Tento skript kontroluje dostupnost runtime závislostí pro DKT workflow"
echo "Konfigurační soubor: $CONFIG"
echo

java -jar ~/dkt/dkt-workflow.jar --test_dependencies -c $CONFIG -i /dev/null -o /dev/null