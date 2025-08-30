#!/usr/bin/env bash
set -e

# Wait for database to become available (optional). If SPRING_DATASOURCE_URL is not set, skip waiting.
if [ -n "${SPRING_DATASOURCE_URL}" ]; then
  echo "Waiting for database to be available at ${SPRING_DATASOURCE_URL} ..."
  # Extract host and port from JDBC URL (simple heuristics for mysql and postgres)
  host_port="$(echo ${SPRING_DATASOURCE_URL} | sed -E 's#.*://([^/:]+)(:([0-9]+))?/.*#\1:\3#')"
  host=$(echo $host_port | cut -d: -f1)
  port=$(echo $host_port | cut -d: -f2)

  # Fallbacks
  if [ -z "$host" ] || [ "$host" = "null" ]; then
    echo "Could not parse host from SPRING_DATASOURCE_URL, skipping wait."
  else
    if [ -z "$port" ] || [ "$port" = "null" ]; then
      # default ports
      if echo "${SPRING_DATASOURCE_URL}" | grep -qi mysql; then
        port=3306
      elif echo "${SPRING_DATASOURCE_URL}" | grep -qi postgres; then
        port=5432
      fi
    fi
    echo "Polling $host:$port"
    retries=30
    until nc -z "$host" "$port"; do
      retries=$((retries-1))
      if [ "$retries" -le 0 ]; then
        echo "Timed out waiting for database at $host:$port"
        break
      fi
      sleep 2
    done
  fi
fi

echo "Starting application"
exec java -jar /app.jar