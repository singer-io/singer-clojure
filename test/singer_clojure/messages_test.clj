(ns singer-clojure.messages-test
  (:require [singer-clojure.messages :as singer-messages]
            [clojure.test :refer [is deftest]]
            [clojure.string :as string]
            [clojure.data.json :as json]))

(defmacro with-out-str-and-value
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [v# ~@body]
         (vector (str s#)
                 v#)))))

(defn get-messages-from-output
  [func & args]
  (let [[stdout return-val] (with-out-str-and-value (apply func args))]
    (as-> stdout output
      (string/split output #"\n")
      (filter (complement empty?) output)
      (map json/read-str
           output)
      (vector (first output)
              return-val))))

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
        [actual-schema-message _]   (get-messages-from-output singer-messages/write-schema! catalog stream-name key-properties bookmark-properties)]
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
        [actual-schema-message _]   (get-messages-from-output singer-messages/write-schema! catalog stream-name key-properties bookmark-properties)]
    (is (= expected-schema-message
           actual-schema-message))))

(deftest write-state-test
  (let [state                  {"bookmarks" {"test-stream" {"a-key" "a-value"}}}
        expected-state-message {"type"  "STATE"
                                "value" state}
        [actual-state-message returned-state]   (get-messages-from-output singer-messages/write-state! state)]
    (is (= expected-state-message
           actual-state-message))
    (is (= state
           returned-state))))

(deftest write-activate-version-test
  (let [state                             {"a-key" "a-value"}
        version                           12345
        stream-name                       "testing-stream"
        expected-activate-version-message {"type"    "ACTIVATE_VERSION"
                                           "stream"  stream-name
                                           "version" version}
        [actual-activate-version-message returned-state] (get-messages-from-output singer-messages/write-activate-version! stream-name version state)]
    (is (= expected-activate-version-message
           actual-activate-version-message))
    (is (= state
           returned-state))))

(deftest write-record-without-version-and-without-time-extracted-test
  (let [stream-name                     "tst-stream"
        record                          {"a" "b" "c" "d"}
        expected-write-record-messsage  {"type"   "RECORD"
                                         "stream" stream-name
                                         "record" record}
        [actual-write-record-message _] (get-messages-from-output singer-messages/write-record! stream-name record)]
    (is (= expected-write-record-messsage
           actual-write-record-message))))

(deftest write-record-without-version-and-with-time-extracted-test
  (let [stream-name                     "tst-stream"
        record                          {"a" "b" "c" "d"}
        time-extracted                  "2020-05-19T15:42:07+0000"
        expected-write-record-messsage  {"type"   "RECORD"
                                         "stream" stream-name
                                         "record" record
                                         "time_extracted" time-extracted}
        [actual-write-record-message _] (get-messages-from-output singer-messages/write-record! stream-name record {:time-extracted time-extracted})]
    (is (= expected-write-record-messsage
           actual-write-record-message))))

(deftest write-record-with-version-and-without-time-extracted-test
  (let [stream-name                     "tst-stream"
        record                          {"a" "b" "c" "d"}
        version                         1234
        expected-write-record-messsage  {"type"   "RECORD"
                                         "stream" stream-name
                                         "record" record
                                         "version" version}
        [actual-write-record-message _] (get-messages-from-output singer-messages/write-record! stream-name record {:version version})]
    (is (= expected-write-record-messsage
           actual-write-record-message))))

(deftest write-record-with-version-and-time-extracted-test
  (let [stream-name                     "tst-stream"
        record                          {"a" "b" "c" "d"}
        version                         1234
        time-extracted                  "2020-05-19T15:42:07+0000"
        expected-write-record-messsage  {"type"   "RECORD"
                                         "stream" stream-name
                                         "record" record
                                         "version" version
                                         "time_extracted" time-extracted}
        [actual-write-record-message _] (get-messages-from-output singer-messages/write-record! stream-name record {:version version :time-extracted time-extracted})]
    (is (= expected-write-record-messsage
           actual-write-record-message))))
