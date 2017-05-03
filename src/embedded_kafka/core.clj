(ns embedded-kafka.core
  (:require [clojure.tools.logging :as log])
  (:import [java.nio.file Files Paths SimpleFileVisitor FileVisitResult]
           [org.apache.kafka.common.network ListenerName]
           [org.apache.kafka.common.protocol SecurityProtocol]
           [org.apache.kafka.common.serialization Serdes]
           [org.apache.kafka.common TopicPartition]
           [org.apache.curator.test TestingServer]
           [org.apache.kafka.common.utils SystemTime]
           [kafka.utils TestUtils]
           [kafka.server KafkaConfig KafkaConfig$]))

(defn delete-dir [path-dir]
  (when (Files/isReadable path-dir)
    (Files/walkFileTree path-dir (proxy [SimpleFileVisitor] []
                                   (visitFile [path _]
                                     (Files/delete path)
                                     FileVisitResult/CONTINUE)
                                   (postVisitDirectory [dir _]
                                     (Files/delete dir)
                                     FileVisitResult/CONTINUE)))))

(def default-config
  (let [m  KafkaConfig$/MODULE$]
    {(.BrokerIdProp m) (int 0)
     (.HostNameProp m) "127.0.0.1"
     (.PortProp m) (int 0)              ; 0 selects a random port
     (.NumPartitionsProp m) (int 1)
     (.AutoCreateTopicsEnableProp m) true
     (.DeleteTopicEnableProp m) true
     (.LogCleanerDedupeBufferSizeProp m) (* 2 1024 1024)
     (.MessageMaxBytesProp m) (int (* 1024 1024)) 
     (.ControlledShutdownEnableProp m) true}))


(defn start-kafka [init-config zoo-keeper-connect]
  "Start the embedded kafka broker, answering a tupple of [server log-dir-path]." 
  (log/info "starting kafka broker")
  (let [log-dir-path (Files/createTempDirectory "kafka-embedded"
                                                (into-array java.nio.file.attribute.FileAttribute []))
        effective-config (-> default-config
                             (merge init-config)
                             (assoc "zookeeper.connect" zoo-keeper-connect)
                             (assoc (.LogDirProp KafkaConfig$/MODULE$) (str log-dir-path)))
        server (TestUtils/createServer (KafkaConfig. effective-config true) (SystemTime.))]
    [server log-dir-path]))

(defn stop-kafka [server log-dir-path]
  "Stop the supplied Kafka broker, erasing its log directory."
  (.shutdown server)
  (.awaitShutdown server)
  (log/info "stopped kafka broker")
  (delete-dir log-dir-path))

(defn broker-connect-string [kafka-broker]
  "Answer the broker connect string for the specified kafka broker"
  (let [host-name (.. kafka-broker config hostName)
        port (.boundPort kafka-broker (ListenerName/forSecurityProtocol SecurityProtocol/PLAINTEXT))]
    (str host-name ":" port)))

