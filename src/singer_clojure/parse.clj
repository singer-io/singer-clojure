(ns singer-clojure.parse
  (:require [clojure.data.json :as json]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [clojure.test :refer [is]]
            [singer-clojure.catalog :as catalog]))

(defn slurp-json
  ([f]
   (-> f
       io/reader
       json/read))
  ([f key-fn]
   (-> f
       io/reader
       (json/read :key-fn key-fn))))

(defn config
  "This function exists as a test seam"
  [config-file]
  (slurp-json config-file keyword))

(defn state
  "This function exists as a test seam and for the post condition"
  [state-file]
  {:post [(is (map? %) (format "parsed state and returned value %s is not a map" %))]}
  (slurp-json state-file))

(defn catalog
  "This function exists as a test seam"
  [catalog-file]
  (slurp-json catalog-file))

(def cli-options
  [["-d" "--discover"       "Discovery Mode"]
   ["-r" "--repl"           "REPL Mode"]
   ["-c" "--config CONFIG"  "Singer Config File"
    :parse-fn #'config]
   [nil "--catalog CATALOG" "Singer Catalog File"
    :parse-fn (comp catalog/deserialize-catalog catalog)]
   ["-s" "--state STATE"    "Singer State File"
    :parse-fn #'state]
   ["-h" "--help"]])

(defn parse-args
  [& args]
  (cli/parse-opts args cli-options))
