(ns tap-netsuite-suite-analytics.singer.messages
  (:require [tap-netsuite-suite-analytics.singer.schema :as singer-schema]
            [tap-netsuite-suite-analytics.singer.transform :as singer-transform]
            [clojure.data.json :as json]))

(defn write!
  [message]
  {:pre [(valid? message)]}
  (-> message
      (json/write-str :value-fn serialize-datetimes)
      println))

(defn write-schema! [catalog stream-name]
  ;; TODO: Make sure that unsupported values are written with an empty schema
  (-> {"type"           "SCHEMA"
       "stream"         (calculate-destination-stream-name stream-name catalog)
       "key_properties" (get-in catalog [stream-name "metadata" "table-key-properties"])
       "schema"         (get-in catalog [stream-name "schema"])}
      (singer-schema/maybe-add-bookmark-properties-to-schema catalog stream-name)
      (singer-schema/make-unsupported-schemas-empty catalog stream-name)
      write!))

(defn write-state!
  [stream-name state]
  (write! {"type" "STATE"
           "value" state})
  ;; This is very important. This function needs to return state so that
  ;; the outer reduce can pass it in to the next iteration.
  state)

(defn write-record!
  [stream-name state record catalog]
  (let [record-message {"type"   "RECORD"
                        "stream" (calculate-destination-stream-name stream-name catalog)
                        "record" record}
        version        (get-in state ["bookmarks" stream-name "version"])]
    (if (nil? version)
      (write! record-message)
      (write! (assoc record-message "version" version)))))

(defn write-activate-version!
  [stream-name catalog state]
  (write! {"type"    "ACTIVATE_VERSION"
           "stream"  (calculate-destination-stream-name stream-name catalog)
           "version" (get-in state ["bookmarks" stream-name "version"])})
  ;; This must return state, as it appears in the pipeline of a sync
  state)
