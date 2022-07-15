(ns com.xwxpmnyxwp.http-server.core
  (:require [aleph.http :as aleph]
            [taoensso.timbre :as log]
            [clojure.string :refer [starts-with?]]
            [crypticbutter.snoop :refer [>defn =>]]))

(def AlephServer
  [:fn #(-> % str (starts-with? "AlephServer"))])

(def HttpServerConfig
  [:map
   [:router [:=> [:cat :any] :any]]
   [:config [:map
             [:http-server
              [:map
               [:port :int]]]]]])

(>defn
 start
 [{:keys [config router]}]
 [HttpServerConfig => AlephServer]
 (let [{:keys [http-server]} config]
   (log/info "Starting http-server with " http-server)
   (-> router
       (aleph/start-server http-server))))

(>defn
 stop
 [server]
 [AlephServer => :any]
 (log/info "Stopping http-server")
 (.close server))
