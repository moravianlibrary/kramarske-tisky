#!/bin/bash

if [ -z "$1" ]; then
  echo "Chyba: Prázdný povinný parametr - adresář s vstupními daty"
  echo "Použití: $0 <adresář>"
  exit 1
fi

CONFIG=~/dkt/config/config-prod.properties
INPUT_DIR="$1"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
OUTPUT_DIR="$HOME/data/prod/output/$TIMESTAMP"
LOG_FILE="$HOME/data/prod/logs/dkt_run_$TIMESTAMP.log"

mkdir -p "$OUTPUT_DIR"


echo "Spouštím DKT workflow nad produkčními daty"
echo "Konfigurační soubor: $CONFIG"
echo "Adresář se vstupními daty: $INPUT_DIR"
echo "Adresář s výstupními daty: $OUTPUT_DIR"
echo "Logovací soubor: $LOG_FILE"
echo

java -jar ~/dkt/dkt-workflow.jar -c $CONFIG -i $INPUT_DIR -o $OUTPUT_DIR >$LOG_FILE 2>&1
