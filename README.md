# clojure-embedded-kafka-bindings

A library to embed Kafka as a component into your program and use it for integration tests.
Bindings for component and integrant are available.

## Usage

The library defines a default component for the Kafka broker which can be embedded in your complete system (with
the consumers / producers / stream processors etc.)

To start a broker for each test and make the resulting (integrant) system available under the \*system\* var
(Integrant version):

```clojure
(ns some-test-ns
  ...
  (:require [integrant.core :as ig]
            [clojure.test :as t] 
            [embedded-kafka.integrant :as embedded-kafka]
  ...)

(def kafka-system 
  (merge
   ...your complete system config goes here...
   embedded-kafka/default-system)) ; default kafka system


(t/use-fixtures :each (fn [f]
                        (with-bindings {#'*system* (ig/init kafka-system)}
                          (try
                            (f)
                            (finally
                              (ig/halt! *system*))))))
```

The connection strings to Kafka and Zookeeper are available in the system under two keys, retrieve them as:
```clojure
;; During system init:
(ig/ref ::embedded-kafka/kafka-connect)
(ig/ref ::embedded-kafka/zoo-keeper-connect)

;; In the test code itself:
(::embedded-kafka/kafka-connect *system*)
(::embedded-kafka/zoo-keeper-connect *system*)
```

## License

Copyright © 2017 Pieter van Prooijen

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
