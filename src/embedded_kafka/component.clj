(ns embedded-kafka.component
  (:require [embedded-kafka.core :refer [default-config start-kafka stop-kafka]]
            [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log])
  (:import [org.apache.curator.test TestingServer]))

(defrecord EmbeddedZooKeeper [server]
  component/Lifecycle
  (start [component]
    (log/info "starting zookeeper")
    (assoc component :server (TestingServer.)))
  (stop [component]
    (when-let [server (:server component)]
      (.close (:server component))
      (log/info "stopped zookeeper"))
    (assoc component :server nil)))

(defn new-cluster [config]
  (component/system-map
   :kafka-broker (component/using (map->EmbeddedKafka config) [:zoo-keeper])
   :zoo-keeper (map->EmbeddedZooKeeper config)))

(defrecord EmbeddedKafka [init-config zoo-keeper]
  component/Lifecycle
  (start [component]
    (let [zoo-keeper-connect-string  (.getConnectString (:server zoo-keeper))
          [server log-dir-path] (start-kafka {} zoo-keeper-connect-string)]
      (-> component
          (assoc :log-dir-path log-dir-path)
          (assoc :server server))))
  (stop [component]
    (when (:server component)
      (stop-kafka (:server component) (:log-dir-path component)))
    (assoc component :server nil)
    (assoc component :log-dir-path nil)))
