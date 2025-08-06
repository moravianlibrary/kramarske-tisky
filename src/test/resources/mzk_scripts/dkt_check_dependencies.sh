#!/bin/bash

CONFIG="$HOME/dkt/config/config-test.properties"

echo "Kontroluji dostupnost runtime závislostí pro DKT workflow"
echo "Konfigurační soubor: $CONFIG"
echo

java -jar ~/dkt/dkt-workflow.jar --test_dependencies -c $CONFIG -i /dev/null -o /dev/null