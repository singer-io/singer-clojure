(ns singer-clojure.log-test
  (:require [clojure.test :refer :all]
            [singer-clojure.log :as log]
            [clojure.tools.logging.impl :as log-impl]))

(defmacro with-err-str
  "Copy of `with-out-str` implementation with `*out*` replaced by `*err*`"
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*err* s#]
       ~@body
       (str s#))))

(deftest log-fatal-trims-error-from-exception []
  (let [captured-logs (clojure.string/split
                       (with-err-str
                         (log/log-fatal "Fatal Error Occured" (Exception. "BOOM from Java Exception")))
                       #"\n")
        fatal-logs (filter #(= "FATAL" (apply str (take 5 %))) captured-logs)]
    (is (= 39 (count captured-logs)))
    (is (= 1 (count fatal-logs)))
    (is (re-matches #"^FATAL .+ \[nREPL-session-.+\] singer-clojure.log-test - Fatal Error Occured - BOOM from Java Exception$"
                      (first fatal-logs)))
    (is (= "ERROR" (apply str (take 5 (second captured-logs)))))
    (is (= " :cause" (apply str (take 7 (nth captured-logs 2)))))))

(deftest log-fatal-trims-error-from-throwable []
  (let [captured-logs (clojure.string/split
                       (with-err-str
                         (log/log-fatal "Fatal Error Occured" (proxy [Throwable] ["BOOM from proxy Throwable"])))
                       #"\n")
        fatal-logs (filter #(= "FATAL" (apply str (take 5 %))) captured-logs)]
    (is (= 39 (count captured-logs)))
    (is (= 1 (count fatal-logs)))
    (is (re-matches #"^FATAL .+ \[nREPL-session-.+\] singer-clojure.log-test - Fatal Error Occured - BOOM from proxy Throwable$"
                    (first fatal-logs)))
    (is (= "ERROR" (apply str (take 5 (second captured-logs)))))
    (is (= " :cause" (apply str (take 7 (nth captured-logs 2)))))))

(deftest log-fatal-trims-error-from-exinfo []
  (let [captured-logs (clojure.string/split
                       (with-err-str (log/log-fatal "Fatal Error Occured" (ex-info "BOOM" {})))
                       #"\n")
        fatal-logs (filter #(= "FATAL" (apply str (take 5 %))) captured-logs)]
    (is (= 43 (count captured-logs)))
    (is (= 1 (count fatal-logs)))
    (is (re-matches #"^FATAL .+ \[nREPL-session-.+\] singer-clojure.log-test - Fatal Error Occured - BOOM$"
                    (first fatal-logs)))
    (is (= "ERROR" (apply str (take 5 (second captured-logs)))))
    (is (= " :cause" (apply str (take 7 (nth captured-logs 2)))))))

(deftest log-fatal-trims-error-from-wrapped-exinfo []
  (let [captured-logs (clojure.string/split
                       (with-err-str (log/log-fatal "Fatal Error Occured"  (ex-info "BOOM" {} (Exception. "Wrapped exception!"))))
                       #"\n")
        fatal-logs (filter #(= "FATAL" (apply str (take 5 %))) captured-logs)]
    (is (= 43 (count captured-logs)))
    (is (= 1 (count fatal-logs)))
    (is (re-matches #"^FATAL .+ \[nREPL-session-.+\] singer-clojure.log-test - Fatal Error Occured - BOOM$"
                    (first fatal-logs)))
    (is (= "ERROR" (apply str (take 5 (second captured-logs)))))
    (is (= " :cause" (apply str (take 7 (nth captured-logs 2)))))))
