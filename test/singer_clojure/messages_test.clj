(ns singer-clojure.messages-test
  (:require [singer-clojure.messages :as singer-messages]
            [clojure.test :refer [is deftest]]
            [clojure.string :as string]
            [clojure.data.json :as json]))

#_(defn get-messages-from-output
  [catalog stream-name key-properties bookmark-properties]
   (as-> (with-out-str
           (singer-messages/write-schema! catalog stream-name key-properties bookmark-properties))
       output
       (string/split output #"\n")
       (filter (complement empty?) output)
       (map json/read-str
            output)
       (vec output)))

(defn get-messages-from-output
  [func & args]
   (as-> (with-out-str
           (apply func args))
       output
       (string/split output #"\n")
       (filter (complement empty?) output)
       (map json/read-str
            output)
       (vec output)))

(deftest write-schema-without-bookmark-properties-test
  (let [stream-name             "test-stream"
        key-properties          ["3"]
        bookmark-properties     nil
        catalog                 {stream-name {"metadata"      {"is-view"         true
                                                               "replication-key" "4"}
                                              "schema"        {"type"       "object"
                                                               "properties" {"id" {"type" ["null" "integer"]}}}
                                              "tap_stream_id" stream-name
                                              "table_name"    stream-name}}
        expected-schema-message {"type"           "SCHEMA"
                                 "stream"         "test-stream"
                                 "schema"         {"type"       "object"
                                                   "properties" {"id" {"type" ["null"
                                                                               "integer"]}}}
                                 "key_properties" key-properties}
        actual-schema-message   (first (get-messages-from-output singer-messages/write-schema! catalog stream-name key-properties bookmark-properties))]
    (is (= expected-schema-message
           actual-schema-message))))

(deftest write-schema-with-bookmark-properties-test
  (let [stream-name             "test-stream"
        key-properties          ["3"]
        bookmark-properties     ["4"]
        catalog                 {stream-name {"metadata"      {"is-view"         true
                                                               "replication-key" "4"}
                                              "schema"        {"type"       "object"
                                                               "properties" {"id" {"type" ["null" "integer"]}}}
                                              "tap_stream_id" stream-name
                                              "table_name"    stream-name}}
        expected-schema-message {"type"                "SCHEMA"
                                 "stream"              "test-stream"
                                 "schema"              {"type"       "object"
                                                        "properties" {"id" {"type" ["null"
                                                                                    "integer"]}}}
                                 "key_properties"      key-properties
                                 "bookmark_properties" bookmark-properties}
        actual-schema-message   (first (get-messages-from-output singer-messages/write-schema! catalog stream-name key-properties bookmark-properties))]
    (is (= expected-schema-message
           actual-schema-message))))

(deftest write-state-test
  (let [state                  {"bookmarks" {"test-stream" {"a-key" "a-value"}}}
        expected-state-message {"type"  "STATE"
                                "value" state}
        actual-state-message   (first (get-messages-from-output singer-messages/write-state! state))]
    (is (= expected-state-message
           actual-state-message))))
