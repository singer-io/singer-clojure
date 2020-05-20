(ns singer-clojure.catalog-test
  (:require [clojure.test :refer :all]
            [singer-clojure.catalog :refer :all]))

(deftest get-selected-streams-test
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

;; `test-serialized-catalog` and `test-deserialized-catalog` are def'd
;; here since they are also used in the parse tests
(def test-serialized-catalog {"streams"
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

(def test-deserialized-catalog {"stream_a" { "stream"       "stream_a"
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
    (let [serialized-catalog            test-serialized-catalog
          expected-deserialized-catalog test-deserialized-catalog]
     (is (= (deserialize-catalog serialized-catalog)
            expected-deserialized-catalog))
     (is (= (serialize-catalog (deserialize-catalog serialized-catalog))
            serialized-catalog)))))
