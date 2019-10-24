#ARG DOCKER_REGISTRY_URL=""
# Environment integration latest java 8 docker image
FROM openjdk:8u181-jre-alpine3.8

RUN apk add --update nginx && rm -rf /var/cache/apk/*
RUN apk add --no-cache bash gawk sed grep bc coreutils
RUN mkdir -p /run/nginx

COPY package /usr/share/nginx/html/ui/
COPY demo-gradle-*.jar /ws/app.jar
COPY startup.sh /startup.sh

RUN ["chmod", "+x", "/startup.sh"]

#RUN sed -i "s+http://localhost:9000/+/ws/+g" /usr/share/nginx/html/ui/app/app.js
COPY ws.conf /etc/nginx/conf.d/ws.conf

CMD ["/startup.sh"]

EXPOSE 81
