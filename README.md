# ms-satelite-position-consumer

Este proyecto es un servicio que calcula la posicion de una nave en el espacio,
a partir de la informacion que llega de tres satelites permitiendo la trilateracion
de la posicion de la nave.

## Tecnologias

> ***GraalVM***: Es un entorno de ejecución de aplicaciones que proporciona alta eficiencia y bajo consumo de recursos.
> ***Quarkus***: Es un framework de Java diseñado para aplicaciones nativas de la nube, optimizado para OpenJDK HotSpot y GraalVM, ofreciendo un tiempo de inicio ultrarrápido y un uso de memoria reducido.
> ***Infinispan***: Es una plataforma de datos distribuida y altamente escalable para aplicaciones en la nube y en la periferia.
> ***OpenJDK 21***: Es una implementación de código abierto de la Plataforma Java, estándar para el desarrollo y ejecución de aplicaciones Java.
> ***Docker/Podman***: Es una plataforma de contenedores de software que permite la creación, implementación y ejecución de aplicaciones en contenedores.

## Despliegue local
para el despliegue local se debe tener instalado en la maquina:
- Docker
- OpenJDK 21 / graalvm-jdk-21.0.6+8.1
en el caso de docker se debe tener en cuenta que se debe tener el servicio de docker corriendo en la maquina.
para poder desplegar el infinispan que genera el devservices de quarkus para el manejo de cache se debe ejecutar el siguiente comando:

```shell script
./mvnw quarkus:dev
```

## Configuracion de cache

Se debe ingresar en el navegador al siguiente link: [http://localhost:11222](http://localhost:11222) y se debe ingresar el usuario y 
contraseña que se encuentra en el archivo application.properties en modo dev.

Dentro de la consola se debe crear una cache con el nombre de "SATELLITE_POSITIONS" y 
se debe subir los siguientes json con su respectiva key:
***key: kenobi***
```json
{
  "name": "kenobi",
  "distance": 100.0,
  "message": ["", "este", "", "", "mensaje"]
}
```
***key: skywalker***
```json
{
  "name": "skywalker",
  "distance": 115.5,
  "message": ["este", "", "un", "", ""]
}
```
***key: sato*** 
```json
{
  "name": "sato",
  "distance": 142.7,
  "message": ["", "", "un", "mensaje", ""]
}
```
Ya con esto podemos realizar 3 peticiones a las siguientes api:
```curl
curl -X 'POST' \
  'http://localhost:8080/topsecret' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "satellites": [
    {"name": "kenobi", "distance": 447.213, "message": ["", "este", "es", "un", "mensaje"]},
    {"name": "skywalker", "distance": 223.606, "message": ["este", "", "un", "mensaje", "","secreto"]},
    {"name": "sato", "distance": 632.455, "message": ["", "", "es", "", "mensaje"]}
  ]
}'
```
```curl
curl -X 'POST' \
  'http://localhost:8080/topsecret_split/skywalker' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "distance": 223.606,
  "message": ["este", "", "un", "mensaje", "","secreto"]
}'
```
```curl
curl -X 'POST' \
  'http://localhost:8080/topsecret_split/kenobi' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "distance": 447.213,
  "message": ["", "este", "es", "un", "mensaje"]
}'
```
```curl
curl -X 'POST' \
  'http://localhost:8080/topsecret_split/sato' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "distance": 632.455,
  "message": ["", "", "es", "", "mensaje"]
}'
```
## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/ms-satelite-position-consumer-1.0.0-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- Micrometer Registry Prometheus ([guide](https://quarkus.io/guides/micrometer)): Enable Prometheus support for Micrometer
- MongoDB with Panache ([guide](https://quarkus.io/guides/mongodb-panache)): Simplify your persistence code for MongoDB via the active record or the repository pattern
- SmallRye OpenAPI ([guide](https://quarkus.io/guides/openapi-swaggerui)): Document your REST APIs with OpenAPI - comes with Swagger UI
- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- Apache Kafka Client ([guide](https://quarkus.io/guides/kafka)): Connect to Apache Kafka with its native API
- Jacoco - Code Coverage ([guide](https://quarkus.io/guides/tests-with-coverage)): Jacoco test coverage support
- Apache Kafka Streams ([guide](https://quarkus.io/guides/kafka-streams)): Implement stream processing applications based on Apache Kafka
- SmallRye Health ([guide](https://quarkus.io/guides/smallrye-health)): Monitor service health
- Micrometer metrics ([guide](https://quarkus.io/guides/micrometer)): Instrument the runtime and your application with dimensional metrics using Micrometer.

## Provided Code

### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### SmallRye Health

Monitor your application's health using SmallRye Health

[Related guide section...](https://quarkus.io/guides/smallrye-health)
