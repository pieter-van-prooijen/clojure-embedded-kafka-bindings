(ns embedded-kafka.util
  "Utilities for producing / consuming events"
  (:require [clojure.pprint :as pp]
            [clojure.tools.logging :as log])
  (:import [org.apache.kafka.common TopicPartition]
           [org.apache.kafka.common.serialization Serdes]
           [org.apache.kafka.clients.producer KafkaProducer ProducerRecord]
           [org.apache.kafka.clients.consumer KafkaConsumer ConsumerRecord]
           [kafka.admin]
           [java.util UUID]))

;; Default properties for test producer / consumer
(def default-serde (Serdes/String))
(def default-props {})

(def default-props-producer {"key.serializer" (.. default-serde serializer getClass getName)
                             "value.serializer" (.. default-serde serializer getClass getName)
                             "acks" "all"
                             "retries" (int 5)})

(def default-props-consumer {"key.deserializer" (.. default-serde deserializer getClass getName) 
                             "value.deserializer" (.. default-serde deserializer getClass getName)
                             "auto.offset.reset" "earliest"})

(defn produce-key-values-sync [topic kvs broker-connect extra-props]
  "Put the supplied values (with nil keys) on the specified topic and wait for confirmation.
   Auto-create topics should be on in the cluster"
  (log/debug "Sending record to topic " topic)
  (let [props (merge default-props default-props-producer {"bootstrap.servers" broker-connect} extra-props)
        producer (KafkaProducer. props)]
    (doseq [[k v] kvs]
      (let [record (ProducerRecord. topic (int 0) k v)
            result (.send producer record)
            record-meta-data (.get result)]
        (log/debug "Sent record " (str record-meta-data))))
    (.flush producer)
    (.close producer)))

(defn produce-values-sync [topic vs broker-connect props]
  (produce-key-values-sync topic (map (fn [x] [nil x]) vs) broker-connect props))

(defn from-consumer-record [record]
  [(.key record) (.value record)])

(defn consume-key-values
  ([topic nof-items broker-connect extra-props]
   (consume-key-values topic nof-items broker-connect extra-props 5000))
  ([topic nof-items broker-connect extra-props time-out]
   "Read at least nof-items key-value pairs from the specified topic, starting at beginning of the log."
   (let [group-id (str "embedded-kafka-" (UUID/randomUUID)) ; random group, so auto.offset.earliest works
         props (merge default-props default-props-consumer
                      {"group.id" group-id "bootstrap.servers" broker-connect}
                      extra-props)
         consumer (KafkaConsumer. props)]
     (log/debug "polling for key-values on topic " topic)
     (.subscribe consumer [topic])
     (let [consumer-records  (.poll consumer time-out) ; time-out when retrieving values
           key-values (->> consumer-records
                           (.iterator)
                           (iterator-seq)
                           (take nof-items)
                           (map (fn [r] (log/debug "Consumed record " (str r)) r))
                           (map (fn [r] (log/debug "Consumed record key: " (.key r)) r))
                           (map from-consumer-record)
                           (doall))]
       (log/debug "After poll, retrieved " (count key-values) " record(s)")
       (.close consumer)
       (log/debug key-values)
       key-values))))
