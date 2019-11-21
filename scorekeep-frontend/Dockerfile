FROM nginx
ADD public /public

ADD nginx.conf.template /etc/nginx/nginx.conf.template

COPY docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]

CMD ["nginx", "-g", "daemon off;"]
