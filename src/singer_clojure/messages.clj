(ns singer-clojure.messages
  (:require [clojure.data.json :as json]
            [clojure.test :refer [is]]))

(defn valid?
  "Function to evalulate whether a singer message is valid"
  [message]
  (and (#{"SCHEMA" "STATE" "RECORD" "ACTIVATE_VERSION"} (message "type"))
       (case (message "type")
         "SCHEMA"
         (message "schema")

         "STATE"
         (message "value")

         "RECORD"
         (message "record")

         "ACTIVATE_VERSION"
         (message "version"))))

(defn write!
  "Writes a valid singer message to stdout"
  [message]
  {:pre [(is (valid? message) (format "message %s is not valid" message))]}
  (-> message
      (json/write-str)
      println))

(defn make-unsupported-schemas-empty [schema-message catalog stream-name]
  (let [schema-keys      (get-in catalog [stream-name "metadata" "properties"])
        unsupported-keys (map first (filter #(= "unsupported" ((second %) "inclusion"))
                                            (seq schema-keys)))]
    (reduce (fn [msg x] (assoc-in msg ["schema" "properties" x] {}))
            schema-message
            unsupported-keys)))

(defn write-schema!
  "Writes a schema message to std out, optionally takes bookmark-properties"
  ([catalog stream-name key-properties]
   (write-schema! catalog stream-name key-properties nil))
  ([catalog stream-name key-properties bookmark-properties]
   (-> (merge {"type"    "SCHEMA"
               "stream"         stream-name
               "key_properties" key-properties
               "schema"  (get-in catalog [stream-name "schema"])}
              (when bookmark-properties
                {"bookmark_properties" bookmark-properties}))
       (make-unsupported-schemas-empty catalog stream-name)
       write!)))

(defn write-state!
  "Writes a state message to std out and returns state unchanged"
  [state]
  (write! {"type" "STATE"
           "value" state})
  ;; This is very important. This function needs to return state so that
  ;; the outer reduce can pass it in to the next iteration.
  state)

(defn write-record!
  "Writes a record message to std out, optionally takes a version and time extracted"
  ([stream-name record]
   (write-record! stream-name record {}))
  ([stream-name record options]
   (let [{:keys [version time-extracted]
          :or   {version nil time-extracted nil}} options]
     (write! (merge {"type"   "RECORD"
                     "stream" stream-name
                     "record" record}
                    (when version
                      {"version" version})
                    (when time-extracted
                      {"time_extracted" time-extracted}))))))

(defn write-activate-version!
  "Writes an activate version message to std out and returns state unchanged"
  [stream-name version state]
  (write! {"type"    "ACTIVATE_VERSION"
           "stream"  stream-name
           "version" version})
  ;; This must return state, as it appears in the pipeline of a sync
  state)
