(ns singer-clojure.bookmarks-test
  (:require [clojure.test :refer :all]
            [singer-clojure.bookmarks :refer :all]))

(deftest clear-bookmark-test
  (testing "Confirm bookmark cleared from state"
    (let [state {"bookmarks" {"a-stream" {"a-bookmark-key" "a-bookmark-value"}}}]
      (is (= (clear-bookmark state "a-stream" "a-bookmark-key") {"bookmarks" {"a-stream" {}}})))))

(deftest reset-stream-test
  (testing "Confirm stream's bookmark cleared from state"
    (let [state {"bookmarks" {"a-stream" {"bookmark-key" "bookmark-value"}}}]
      (is (= (reset-stream state "a-stream") {"bookmarks" {}})))))

(deftest set-currently-syncing-test
  (testing "Setting the currently_syncing stream does so"
    (let [state {"bookmarks" {"a-stream" {"bookmark-key" "bookmark-value"}}}]
      (is (= (set-currently-syncing state "a-stream")
             {"bookmarks"         {"a-stream" {"bookmark-key" "bookmark-value"}}
              "currently_syncing" "a-stream"})))))

(deftest get-currently-syncing-test
  (testing "Returns the currently_syncing stream"
    (let [state {"bookmarks"         {"a-stream" {"bookmark-key" "bookmark-value"}}
                 "currently_syncing" "a-stream"}]
      (is (= (get-currently-syncing state) "a-stream")))))
