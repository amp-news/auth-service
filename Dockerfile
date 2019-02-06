FROM java:8

WORKDIR /

ADD authorization-0.0.1.jar authorization.jar

EXPOSE 8080

CMD java -jar -Dspring.profiles.active=aws authorization.jar