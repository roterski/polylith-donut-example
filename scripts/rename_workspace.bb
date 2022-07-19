(ns rename-workspace
  (:require [clojure.tools.cli :refer [parse-opts]]
            [rewrite-clj.zip :as z]
            [clojure.java.io :as io]
            [malli.core :as m]
            [malli.error :as me]
            [malli.transform :as mt]
            [malli.util :as mu]
            [clojure.string :as str]))

(def current-name "xwxpmnyxwp")

(def Input
  [:map
   [:path     {:optional true} :string]
   [:from     {:optional true} :string]
   [:to                        :string]
   [:dry-run? {:optional true} :boolean]])

(def cli-options
  [(let [schema (mu/get Input :to)]
     ["-t" "--to NAME" "A new workspace name"
      :parse-fn #(m/decode schema % mt/string-transformer)
      :validate [#(m/validate schema %) "Must be a string"]])
   (let [schema (mu/get Input :dry-run?)]
     ["-d" "--dry-run? BOOLEAN" "Boolean flag indicating dry run"
      :parse-fn #(m/decode schema % mt/string-transformer)])
   ["-h" "--help"]])

(defn rename-sexprs
  [zipper {:keys [from to dry-run?]}]
  (-> zipper
      (z/postwalk (fn select [zloc] (some-> zloc z/node :string-value (str/includes? from)))
                  (fn visit [zloc]
                    (let [[line col] (z/position zloc)
                          old-value (z/node zloc)
                          new-value (str/replace old-value from to)]
                      (when-not (= old-value new-value)
                        (println "\t\tline:" line "col:" col)
                        (when-not dry-run?
                          (z/edit zloc #(-> %
                                            (str/replace from to)
                                            symbol)))))))))

(defn rename-file-contents
  [path props]
  (println "\t" path ":")
  (->> (-> (z/of-file (io/file path) {:track-position? true})
           z/up
           (rename-sexprs props)
           z/->string)
       (spit path)))

(defn rename-directory [path {:keys [from to dry-run?]}]
  (let [to (str/replace to "-" "_")
        new-path (str/replace path from to)]
    (when-not (= path new-path)
      (println path " -> " new-path)
      (when-not dry-run?
        (-> (io/file path)
            (.renameTo (io/file new-path)))))))

(def allowed-extensions #{:clj :cljs :cljc :edn :bb})

(defn rename-workspace [{:keys [path] :as props}]
  (let [{:keys [directories files]} (->> (io/file path)
                                         file-seq
                                         (group-by #(if (.isDirectory %)
                                                      :directories
                                                      :files)))]
    (when-let [errors (-> (m/explain Input props)
                          (me/humanize))]
      (ex-info "Input validation failed" errors))
    (when (not-empty files)
      (println "Renaming file contents")
      (->> files
           (filter #(let [ext (some-> % .getName (str/split #"\.") last keyword)]
                      (contains? allowed-extensions ext)))
           (run! #(rename-file-contents (.getPath %) props))))

    (when (not-empty directories)
      (println "Renaming directories")
      (->> directories
           (run! #(rename-directory (.getPath %) props))))))

(defn -main [& _args]
  (let [{:keys [arguments options]} (parse-opts *command-line-args* cli-options)]

    (rename-workspace (merge {:path (first arguments)
                              :from current-name}
                             options))))

(-main)
