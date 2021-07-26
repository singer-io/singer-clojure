(ns singer-clojure.log
  (:require
   [clojure.tools.logging :as log]
   [clojure.tools.logging.impl :as log-impl]
   [clojure.string :as string]))

;; clojure.tools.logging defaults to slf4j which doesn't have a critical
;; or fatal level defined. This is not great for taps, who will log extra
;; information (like the stack trace) at ERROR level and reserve the FATAL
;; level for the summary of the final error.
;;
;; Because the logger-factory is a dynamic binding, we can require that
;; log4j be used to output our logs, while still allowing Java libraries
;; (such as apache/avro) to include and use slf4j. This is an opinion
;; added to the Singer library, as that is the behavior expected from
;; Singer taps.
(defonce log4j-factory
  (delay
    (if-let [factory (log-impl/log4j-factory)]
      factory
      (throw (Exception. "[log4j not found] - In order to use singer-clojure fatal-level logging, log4j is required.")))))

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
