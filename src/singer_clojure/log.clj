(ns singer-clojure.log
  (:require
   [clojure.string :as clj-str]
   [clojure.tools.logging :as log]
   [clojure.string :as string]))

(defn fatal
  ([msg calling-ns]
   (log/error (str calling-ns " - " msg)))
  ([msg calling-ns ex]
   (doseq [next-msg  (string/split (.getMessage ^Exception ex) #"\n")]
     (log/error (str calling-ns " - " msg " - " next-msg)))
   (log/error ex)))

(defmacro log-fatal
  ([msg]
   `(fatal ~msg ~*ns*))
  ([msg ex]
   `(fatal ~msg ~*ns* ~ex)))
