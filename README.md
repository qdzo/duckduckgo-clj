# Duckduckgo-clj

Search-like application, consumes ["duckduckgo instant answer api"](https://duckduckgo.com/api).

This is a toy-project, which purpose is to test `clojure/clojurescript` alliance as full-stack platform for client-server applications.

### Client's part made with:

- `reagent`
- `core.async`
- `garden`
- `cljs-http`

### Server's part made with:

- `ring`
- `compojure`
- `http-clj`

> Powered by `clojure.spec`

## Screenshots

* ![image1](images/image1.png)
* ![image2](images/image2.png)
* ![image3](images/image3.png)

## Requirenments

* [OracleJDK-9](http://www.oracle.com/technetwork/java/javase/downloads/jdk9-downloads-3848520.html) or [OpenJDK-9](http://openjdk.java.net/install/)
* [Leiningen](https://leiningen.org/)

## Usage

### Clone project

```bash
git clone https://github.com/qdzo/duckduckgo-clj && cd duckduckgo-clj
```

### Build project

```bash
lein do cljsbuild once, ring uberjar
```

### Run server

```bash
java -jar target/duckduckgo-clj-$(version)-standalone.jar
```

### Open browser tab

* http://localhost:3000/

## License

License
Distributed under the Eclipse Public License, the same as Clojure.
