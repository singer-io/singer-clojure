(ns singer-clojure.messages
  (:require [clojure.data.json :as json]
            [clojure.test :refer [is]]))

(defn valid?
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
  ([catalog stream-name key-properties]
   (write-schema! catalog stream-name key-properties nil)
   )
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
  [state]
  (write! {"type" "STATE"
           "value" state})
  ;; This is very important. This function needs to return state so that
  ;; the outer reduce can pass it in to the next iteration.
  state)

(defn write-record!
  [stream-name state record catalog]
  (let [record-message {"type"   "RECORD"
                        "stream" stream-name
                        "record" record}
        version        (get-in state ["bookmarks" stream-name "version"])]
    (if (nil? version)
      (write! record-message)
      (write! (assoc record-message "version" version)))))

(defn write-activate-version!
  [stream-name catalog state]
  (write! {"type"    "ACTIVATE_VERSION"
           "stream"  stream-name 
           "version" (get-in state ["bookmarks" stream-name "version"])})
  ;; This must return state, as it appears in the pipeline of a sync
  state)
