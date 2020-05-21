(ns singer-clojure.parse-test
  (:require [clojure.test :refer :all]
            [singer-clojure.parse :refer :all]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [singer-clojure.catalog-test :as catalog-test]))

(deftest parse-config-file-test
  (testing "Confirm that config file can be parsed with keys as keywords"
    (let [temp-file       (java.io.File/createTempFile "config" ".json")
          temp-file-name  (.getAbsolutePath temp-file)
          expected-config {:a "foo" :b "bar"}
          config-json-str (json/write-str expected-config)
          _               (spit temp-file-name config-json-str)
          actual-config   (config temp-file-name)
          _               (.delete temp-file)]
      (is (= expected-config
             actual-config)))))

(deftest parse-state-file-test
  (testing "Confirm that state file can be parsed"
    (let [temp-file       (java.io.File/createTempFile "state" ".json")
          temp-file-name  (.getAbsolutePath temp-file)
          expected-state {"bookmarks" {"test-stream" {"a-key" "a-value"}}}
          state-json-str (json/write-str expected-state)
          _               (spit temp-file-name state-json-str)
          actual-state   (state temp-file-name)
          _               (.delete temp-file)]
      (is (= expected-state
             actual-state)))))

(deftest parse-catalog-file-test
  (testing "Confirm that catalog file can be parsed"
    (let [temp-file        (java.io.File/createTempFile "catalog" ".json")
          temp-file-name   (.getAbsolutePath temp-file)
          expected-catalog catalog-test/serialized-test-catalog
          catalog-json-str (json/write-str expected-catalog)
          _                (spit temp-file-name catalog-json-str)
          actual-catalog   (catalog temp-file-name)
          _                (.delete temp-file)]
      (is (= expected-catalog
             actual-catalog)))))

(deftest parse-args-test
  (testing "Confirm that we can read and parse all args when they're
  provided in both long and short formats"
    (let [config-file        (java.io.File/createTempFile "config" ".json")
          config-file-path   (.getAbsolutePath config-file)
          expected-config    {:a "foo" :b "bar"}
          _                  (spit config-file-path (json/write-str expected-config))
          state-file         (java.io.File/createTempFile "state" ".json")
          state-file-path    (.getAbsolutePath state-file)
          expected-state     {"bookmarks" {"test-stream" {"a-key" "a-value"}}}
          _                  (spit state-file-path (json/write-str expected-state))
          catalog-file       (java.io.File/createTempFile "catalog" ".json")
          catalog-file-path  (.getAbsolutePath catalog-file)
          _                  (spit catalog-file-path (json/write-str catalog-test/serialized-test-catalog))
          parsed-args-long   (parse-args "--config" config-file-path
                                         "--state" state-file-path
                                         "--catalog" catalog-file-path
                                         "--discover"
                                         "--repl")
          parsed-args-short  (parse-args "-c" config-file-path
                                         "-s" state-file-path
                                         "--catalog" catalog-file-path
                                         "-d"
                                         "-r")]
      (.delete config-file)
      (.delete state-file)
      (.delete catalog-file)
      (is (= expected-config
             (get-in parsed-args-long [:options :config])
             (get-in parsed-args-short [:options :config])))
      (is (= expected-state
             (get-in parsed-args-long [:options :state])
             (get-in parsed-args-short [:options :state])))
      (is (= catalog-test/deserialized-test-catalog
             (get-in parsed-args-long [:options :catalog])
             (get-in parsed-args-short [:options :catalog]))))))
