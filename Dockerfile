FROM clojure:tools-deps AS build

WORKDIR /srv

COPY . .
RUN --mount=target=/root/.m2,type=cache,sharing=locked \
    clojure -T:build uber



FROM bellsoft/liberica-openjre-alpine-musl:23 AS final

COPY --from=build /srv/target/sample.jar /sample.jar

ENTRYPOINT ["java", "-XX:-OmitStackTraceInFastThrow", "-jar", "/sample.jar"]
