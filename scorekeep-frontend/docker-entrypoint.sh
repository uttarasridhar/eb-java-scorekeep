#!/usr/bin/env bash
set -e

echo "################################## Run nginx"
envsubst '${ECS_PROJECT_LB_DNS}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf
exec "$@"
