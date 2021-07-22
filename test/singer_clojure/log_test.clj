(ns singer-clojure.log-test
  (:require [clojure.test :refer :all]
            [singer-clojure.log :as log]
            [clojure.tools.logging.impl :as impl]))

(defmacro capture-logs [& body]
  `(let [captured-logs# (atom [])]
     ;; impl/write! args are: [logger level throwable message]
     (with-redefs [impl/write! (fn [& args#] (swap! captured-logs# conj args#))]
       ~@body
       @captured-logs#)))

(deftest log-fatal-trims-error-from-exception []
  (let [captured-logs (capture-logs (log/log-fatal "Fatal Error Occured" (Exception. "BOOM from Java Exception")))
        fatal-logs (filter #(= :error (second %)) captured-logs)]
    (is (= 2 (count captured-logs)))
    (is (= 1 (count fatal-logs)))
    (is (= "singer-clojure.log-test - Fatal Error Occured - BOOM from Java Exception" (nth (first fatal-logs) 3)))
    (is (= :warn (second (last captured-logs))))))

(deftest log-fatal-trims-error-from-throwable []
  (let [captured-logs (capture-logs (log/log-fatal "Fatal Error Occured" (proxy [Throwable] ["BOOM from proxy Throwable"])))
        fatal-logs (filter #(= :error (second %)) captured-logs)]
    (is (= 2 (count captured-logs)))
    (is (= 1 (count fatal-logs)))
    (is (= "singer-clojure.log-test - Fatal Error Occured - BOOM from proxy Throwable" (nth (first fatal-logs) 3)))
    (is (= :warn (second (last captured-logs))))))

(deftest log-fatal-trims-error-from-exinfo []
  (let [captured-logs (capture-logs (log/log-fatal "Fatal Error Occured" (ex-info "BOOM" {})))
        fatal-logs (filter #(= :error (second %)) captured-logs)]
    (is (= 2 (count captured-logs)))
    (is (= 1 (count fatal-logs)))
    (is (= "singer-clojure.log-test - Fatal Error Occured - BOOM" (nth (first fatal-logs) 3)))
    (is (= :warn (second (last captured-logs))))))

(deftest log-fatal-trims-error-from-wrapped-exinfo []
  (let [captured-logs (capture-logs (log/log-fatal "Fatal Error Occured"  (ex-info "BOOM" {} (Exception. "Wrapped exception!"))))
        fatal-logs (filter #(= :error (second %)) captured-logs)]
    (is (= 2 (count captured-logs)))
    (is (= 1 (count fatal-logs)))
    (is (= "singer-clojure.log-test - Fatal Error Occured - BOOM" (nth (first fatal-logs) 3)))
    (is (= :warn (second (last captured-logs))))))
