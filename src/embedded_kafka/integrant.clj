(ns embedded-kafka.integrant
  (:require [embedded-kafka.core :refer [default-config start-kafka stop-kafka broker-connect-string]]
            [integrant.core :as ig]
            [clojure.tools.logging :as log])
  (:import [org.apache.curator.test TestingServer]))

(defmethod ig/init-key ::zoo-keeper [_ _]
  (log/info "starting embedded zookeeper")
  (TestingServer.))

(defmethod ig/halt-key! ::zoo-keeper [_ server]
  (.close server)
  (log/info "stopped embedded zookeeper"))

(defmethod ig/init-key ::zoo-keeper-connect [_ {server :server}]
  (.getConnectString server))

(defmethod ig/init-key ::kafka [_ {:keys [config zoo-keeper-connect]}]
  (start-kafka config zoo-keeper-connect))

(defmethod ig/halt-key! ::kafka [_ [server log-dir-path]]
  (stop-kafka server log-dir-path))

(defmethod ig/init-key ::kafka-connect [_ {[server _] :server}]
  (broker-connect-string server))

