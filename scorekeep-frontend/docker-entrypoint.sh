#!/usr/bin/env bash
set -e

echo "################################## Run nginx with alb domain name=$ECS_PROJECT_LB_DNS. Also, ${ECS_PROJECT_LB_DNS}"
envsubst '${ECS_PROJECT_LB_DNS}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf
exec "$@"
