(ns singer-clojure.log
  (:require
   [clojure.tools.logging :as log]
   [clojure.tools.logging.impl :as log-impl]
   [clojure.string :as string]))

;; clojure.tools.logging now uses log4j2 via log4j-slf4j-impl. No bridging or 1.x compatibility required.
(defonce log4j-factory
  (delay
    (or (log-impl/log4j2-factory)
        (throw (Exception. "[log4j2 not found] - In order to use singer-clojure fatal-level logging, log4j2 is required.")))))

(defn fatal
  ([msg calling-ns]
   (binding [log/*logger-factory* @log4j-factory]
     (log/fatal (str calling-ns " - " msg))))
  ([msg calling-ns ex]
   (binding [log/*logger-factory* @log4j-factory]
     (doseq [next-msg (string/split (or (.getMessage ^Exception ex) "") #"\n")]
       (log/fatal (str calling-ns " - " msg " - " next-msg)))
     (log/error ex))))

(defmacro log-fatal
  ([msg]
   `(fatal ~msg ~*ns*))
  ([msg ex]
   `(fatal ~msg ~*ns* ~ex)))
