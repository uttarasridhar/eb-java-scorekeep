#!/usr/bin/env bash
set -e

echo "################################## Run nginx with alb domain name=$ECS_ENV_LB_DNS. Also, ${ECS_ENV_LB_DNS}"
envsubst '${ECS_ENV_LB_DNS}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf
exec "$@"
