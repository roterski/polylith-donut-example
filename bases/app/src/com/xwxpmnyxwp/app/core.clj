(ns com.xwxpmnyxwp.app.core
  (:require [donut.system :as ds]
            [com.xwxpmnyxwp.http-server.interface :as http-server]
            [com.xwxpmnyxwp.config-reader.interface :as config-reader]))

(def base-system
  {::ds/defs
   {:shared {:config #::ds{:start (fn [{::ds/keys [conf]}]
                                    (config-reader/start conf))
                           :conf {:env (ds/ref [:env :name])}}}
    :app {:http-server #::ds{:start (fn [{::ds/keys [conf]}]
                                      (http-server/start conf))
                             :stop  (fn [{::ds/keys [instance]}]
                                      (http-server/stop instance))
                             :conf {:config (ds/ref [:shared :config])
                                    :router (fn [req]
                                              {:status 200
                                               :headers {"content-type" "text/plain"}
                                               :body "hello!"})}}}}})

(defmethod ds/named-system :prod
  [_]
  (ds/system :base {[:env :name] :prod}))

(defmethod ds/named-system :base
  [_]
  base-system)
