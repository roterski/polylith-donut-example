(ns com.xwxpmnyxwp.config-reader.core
  (:require
   [clojure.java.io :as io]
   [crypticbutter.snoop :refer [>defn =>]]
   [aero.core :as aero]
   [taoensso.timbre :as log]))

(>defn
 read-config-edn
 [f]
 [:keyword => [:or [:map] :nil]]
 (log/info "Loading " f " config.")
 (let [path (str "config/" (name f) ".edn")]
   (if-let [file (-> path io/resource)]
     (-> file aero/read-config)
     (log/warn path "not found and not loaded"))))

(>defn
 read-config-edn!
 [f]
 [:keyword => [:map]]
 (if-let [data (read-config-edn f)]
   data
   (throw (ex-info "read-config-edn! failed to load file" {:filename f}))))

(>defn
 start
 [{:keys [env] :as props}]
 [[:map
   [:env [:enum :dev :prod :test]]]
  => :any]
 (merge (read-config-edn! :default)
        (read-config-edn! env)
        (read-config-edn :local)))
