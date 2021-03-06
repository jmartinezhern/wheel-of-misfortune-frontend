FROM clojure:lein-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN lein uberjar
CMD ["java", "-jar", "target/wheel-of-misfortune-0.1.0-SNAPSHOT-standalone.jar"]