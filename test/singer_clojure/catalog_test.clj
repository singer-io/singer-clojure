(ns singer-clojure.catalog-test
  (:require [clojure.test :refer :all]
            [singer-clojure.catalog :refer :all]))

(deftest get-selected-streams-respects-selected-metadata
  (testing "Test that the get-selected-stream properly returns a sequence
  of selected streams"
    (let [catalog {"stream_a" { "stream"       "stream_a"
                               "metadata"      {"selected" true}}
                   "stream_b" { "stream"       "stream_b"
                               "metadata"      {"selected" false}}
                   "stream_c" { "stream"       "stream_c"
                               "metadata"      {"selected" true}}
                   "stream_d" { "stream"       "stream_d"
                               "metadata"      {"selected" false}}
                   "stream_e" { "stream"       "stream_e"
                               "metadata"      {"selected" true}}}]
      (is (= (get-selected-streams catalog)
             '("stream_a" "stream_c" "stream_e"))))))

(deftest get-selected-streams-with-currently-syncing-shuffles
  (testing "Test that the get-selected-stream properly returns a sequence
  of selected streams, shuffled to respect `currently_syncing` in state"
    (let [catalog {"stream_a" { "stream"       "stream_a"
                               "metadata"      {"selected" true}}
                   "stream_b" { "stream"       "stream_b"
                               "metadata"      {"selected" false}}
                   "stream_c" { "stream"       "stream_c"
                               "metadata"      {"selected" true}}
                   "stream_d" { "stream"       "stream_d"
                               "metadata"      {"selected" false}}
                   "stream_e" { "stream"       "stream_e"
                               "metadata"      {"selected" true}}}
          state {:currently_syncing "stream_c"}]
      (is (= (get-selected-streams catalog state)
             '("stream_c" "stream_e" "stream_a"))))))

(deftest get-selected-streams-with-currently-syncing-deselected-does-not-shuffle
  (testing "Test that the get-selected-stream properly returns a sequence
  of selected streams, unshuffled, if `currently_syncing` is deselected."
    (let [catalog {"stream_a" { "stream"       "stream_a"
                               "metadata"      {"selected" true}}
                   "stream_b" { "stream"       "stream_b"
                               "metadata"      {"selected" false}}
                   "stream_c" { "stream"       "stream_c"
                               "metadata"      {"selected" true}}
                   "stream_d" { "stream"       "stream_d"
                               "metadata"      {"selected" false}}
                   "stream_e" { "stream"       "stream_e"
                               "metadata"      {"selected" true}}}
          state {:currently_syncing "stream_b"}]
      (is (= (get-selected-streams catalog state)
             '("stream_a" "stream_c" "stream_e"))))))

(deftest get-selected-streams-with-currently-syncing-missing-does-not-shuffle
  (testing "Test that the get-selected-stream properly returns a sequence
  of selected streams, unshuffled, if `currently_syncing` is missing."
    (let [catalog {"stream_a" { "stream"       "stream_a"
                               "metadata"      {"selected" true}}
                   "stream_b" { "stream"       "stream_b"
                               "metadata"      {"selected" false}}
                   "stream_c" { "stream"       "stream_c"
                               "metadata"      {"selected" true}}
                   "stream_d" { "stream"       "stream_d"
                               "metadata"      {"selected" false}}
                   "stream_e" { "stream"       "stream_e"
                               "metadata"      {"selected" true}}}
          state {:currently_syncing "stream_x"}]
      (is (= (get-selected-streams catalog state)
             '("stream_a" "stream_c" "stream_e"))))))

;; `serialized-test-catalog` and `deserialized-test-catalog` are def'd
;; here since they are also used in the parse tests
(def serialized-test-catalog {"streams"
                              (list {"stream"        "stream_a",
                                     "tap_stream_id" "stream_a",
                                     "table_name"    "stream_a",
                                     "schema"
                                     {"type" "object",
                                      "properties"
                                      {"field_a" {"type" ["integer" "null"]},
                                       "field_b" {"type" ["string" "null"]}}},
                                     "metadata"
                                     (list {"metadata"
                                            {"database-name"          "Stitch (DA7)",
                                             "table-key-properties"   [],
                                             "valid-replication-keys" []},
                                            "breadcrumb" []}
                                           {"metadata"   {"inclusion" "available", "sql-datatype" "VARCHAR2"},
                                            "breadcrumb" ["properties" "field_a"]}
                                           {"metadata"   {"inclusion" "available", "sql-datatype" "VARCHAR2"},
                                            "breadcrumb" ["properties" "field_b"]})}
                                    {"stream"        "stream_b",
                                     "tap_stream_id" "stream_b",
                                     "table_name"    "stream_b",
                                     "schema"
                                     {"type" "object",
                                      "properties"
                                      {"field_a" {"type" ["integer" "null"]},
                                       "field_b" {"type" ["string" "null"]}}},
                                     "metadata"
                                     (list {"metadata"
                                            {"database-name"          "Stitch (DA7)",
                                             "table-key-properties"   [],
                                             "valid-replication-keys" []},
                                            "breadcrumb" []}
                                           {"metadata"   {"inclusion" "available", "sql-datatype" "VARCHAR2"},
                                            "breadcrumb" ["properties" "field_a"]}
                                           {"metadata"   {"inclusion" "available", "sql-datatype" "VARCHAR2"},
                                            "breadcrumb" ["properties" "field_b"]})})})

(def deserialized-test-catalog {"stream_a" { "stream"       "stream_a"
                                            "tap_stream_id" "stream_a"
                                            "table_name"    "stream_a"
                                            "schema"        { "type"      "object"
                                                             "properties" {"field_a" { "type" ["integer" "null"]}
                                                                           "field_b" { "type" ["string" "null"]}}}
                                            "metadata"      {"database-name"          "Stitch (DA7)"
                                                             "table-key-properties"   []
                                                             "valid-replication-keys" []
                                                             "properties"             { "field_a" {"inclusion" "available" "sql-datatype" "VARCHAR2"}
                                                                                       "field_b"  {"inclusion" "available" "sql-datatype" "VARCHAR2"}}}}
                                "stream_b" { "stream"       "stream_b"
                                            "tap_stream_id" "stream_b"
                                            "table_name"    "stream_b"
                                            "schema"        { "type"      "object"
                                                             "properties" {"field_a" { "type" ["integer" "null"]}
                                                                           "field_b" { "type" ["string" "null"]}}}
                                            "metadata"      {"database-name"          "Stitch (DA7)"
                                                             "table-key-properties"   []
                                                             "valid-replication-keys" []
                                                             "properties"             { "field_a" {"inclusion" "available" "sql-datatype" "VARCHAR2"}
                                                                                       "field_b"  {"inclusion" "available" "sql-datatype" "VARCHAR2"}}}}})


(deftest serialize-catalog-test
  (testing "Confirm that a catalog can be correctly deserialized, and then
  serialized back to its original form"
    (let [serialized-catalog            serialized-test-catalog
          expected-deserialized-catalog deserialized-test-catalog]
     (is (= (deserialize-catalog serialized-catalog)
            expected-deserialized-catalog))
     (is (= (serialize-catalog (deserialize-catalog serialized-catalog))
            serialized-catalog)))))
