#!/bin/bash
nginx
echo "load nginx"
java -jar /ws/app.jar
echo "load java"
echo "finish startup script"