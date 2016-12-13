(defproject pieter-van-prooijen/embedded-kafka "0.1.0-SNAPSHOT"
  :description "Embedded Kafka Clojure component bindings (from the curator project)"

  ;; Kafka streams is not yet in the main repos
  :repositories [["confluent" "http://packages.confluent.io/maven"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  
  :dependencies [[org.apache.curator/curator-test "2.9.0" :exclusions [log4j org.apache.zookeeper/zookeeper]]
                 [org.apache.kafka/kafka_2.11 "0.10.1.0-cp1"
                  :exclusions [org.slf4j/slf4j-api org.slf4j/slf4j-log4j12]]
                 [org.apache.kafka/kafka_2.11 "0.10.1.0-cp1"
                  :classifier "test"
                  :exclusions [org.slf4j/slf4j-api org.slf4j/slf4j-log4j12]]

                 ;; Kafka has hardcoded reference to log4j ?
                 [log4j "1.2.16" :exclusions [com.sun.jmx/jmxri javax.jms/jms com.sun.jdmk/jmxtools]]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [com.stuartsierra/component "0.3.1"]
                 [org.clojure/tools.logging "0.3.1"]])
