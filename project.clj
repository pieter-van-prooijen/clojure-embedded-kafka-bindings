(defproject pieter-van-prooijen/embedded-kafka "0.1.0-SNAPSHOT"
  :description "Embedded Kafka Clojure component/integrant bindings (from the curator project)"

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  
  :dependencies [[org.apache.curator/curator-test "2.11.1" :exclusions [log4j org.apache.zookeeper/zookeeper]]
                 [org.apache.kafka/kafka_2.11 "1.0.1"
                  :exclusions [org.slf4j/slf4j-api org.slf4j/slf4j-log4j12]]
                 [org.apache.kafka/kafka_2.11  "1.0.1"
                  :classifier "test"
                  :exclusions [org.slf4j/slf4j-api org.slf4j/slf4j-log4j12]]
                 [org.apache.kafka/kafka-clients  "1.0.1"
                  :classifier "test"
                  :exclusions [org.slf4j/slf4j-api org.slf4j/slf4j-log4j12]]
                 
                 ;; Kafka has a hardcoded reference to log4j ?
                 [log4j "1.2.16" :exclusions [com.sun.jmx/jmxri javax.jms/jms com.sun.jdmk/jmxtools]]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [com.stuartsierra/component "0.3.2"]
                 [integrant "0.4.0"]
                 [org.clojure/tools.logging "0.3.1"]]

  )
