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
  (let [captured-logs (capture-logs (log/log-fatal "Fatal Error Occured" (Exception. "BOOM from Java Exception")))]
    (is (= 1 (count captured-logs)))
    (is (= "singer-clojure.log-test - Fatal Error Occured - BOOM from Java Exception" (nth (first captured-logs) 3)))))

(deftest log-fatal-trims-error-from-throwable []
  (let [captured-logs (capture-logs (log/log-fatal "Fatal Error Occured" (proxy [Throwable] ["BOOM from proxy Throwable"])))]
    (is (= 1 (count captured-logs)))
    (is (= "singer-clojure.log-test - Fatal Error Occured - BOOM from proxy Throwable" (nth (first captured-logs) 3)))))

(deftest log-fatal-trims-error-from-exinfo []
  (let [captured-logs (capture-logs (log/log-fatal "Fatal Error Occured" (ex-info "BOOM" {})))]
    (is (= 1 (count captured-logs)))
    (is (= "singer-clojure.log-test - Fatal Error Occured - BOOM" (nth (first captured-logs) 3)))))

(deftest log-fatal-trims-error-from-wrapped-exinfo []
  (let [captured-logs (capture-logs (log/log-fatal "Fatal Error Occured"  (ex-info "BOOM" {} (Exception. "Wrapped exception!"))))]
    (is (= 1 (count captured-logs)))
    (is (= "singer-clojure.log-test - Fatal Error Occured - BOOM" (nth (first captured-logs) 3)))))
