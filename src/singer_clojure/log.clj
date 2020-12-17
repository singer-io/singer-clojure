(ns singer-clojure.log
  (:require
   [clojure.string :as clj-str]
   [clojure.tools.logging :as log]))

(defn compile-message [calling-ns msg ex]
  (if ex
    (str calling-ns " - " msg " : " (.getMessage ex) "\n")
    (str calling-ns " - " msg "\n")))

(defn fatal
  ([msg calling-ns]
   (log/error (compile-message calling-ns msg nil) ))
  ([msg calling-ns ex]
   (log/error (compile-message calling-ns msg ex) ex )))

(defmacro log-fatal
  ([msg]
   `(fatal ~msg ~*ns*))
  ([msg ex]
   `(fatal ~msg ~*ns* ~ex)))
