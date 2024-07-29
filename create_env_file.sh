#!/bin/sh

create_env_file() {

  if [ -z "$KEYS" ]; then
       echo "Ошибка: переменная KEYS не установлена." >&2
       exit 1
  fi

  keys=$(echo "$KEYS" | tr ' ' '\n')

  env_file=".env"
  touch "$env_file"

  printenv | while IFS='=' read -r key value; do
    for search_key in $keys; do
      if [ "$key" = "$search_key" ]; then
        echo "$key=$value" >> "$env_file"
        break
      fi
    done
  done
}

create_env_file