.PHONY: all run build clean test

version = "0.1.0"

build:
	lein do cljsbuild once, ring uberjar

test: build
	 lein test

run:
	java -jar target/duckduckgo-clj-$(version)-SNAPSHOT-standalone.jar

clean:
	lein clean

all: clean build test
