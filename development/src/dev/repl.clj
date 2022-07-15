(ns dev.repl
  (:require [com.xwxpmnyxwp.app.core]
            [donut.system :as ds]
            [donut.system.repl :as dsr]
            [donut.system.repl.state :as dsrs]))

(defmethod ds/named-system :dev
  [_]
  (ds/system :base {[:env :name] :dev}))

(defmethod ds/named-system ::ds/repl
  [_]
  (ds/system :dev))

(defn start []
  (dsr/start)
  :start)

(defn stop []
  (dsr/stop)
  :stop)

(defn restart []
  (dsr/restart)
  :restart)

(comment

  (start)
  (stop)
  (restart)


  )
