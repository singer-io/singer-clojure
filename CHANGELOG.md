# Changelog

## 1.1.5
  * Prevent fatal logs from showing just the first line of the stack trace (`SEVERE: #error {`, or `FATAL: #error {`, depending on the logging configuration) by moving the full exception to `WARN` level [#16](https://github.com/singer-io/singer-clojure/pull/16)

## 1.1.0
  * Add `currently_syncing` feature to `get-selected-streams` [#10](https://github.com/singer-io/singer-clojure/pull/10)

## 1.0.0
  * License and initial major release [#8](https://github.com/singer-io/singer-clojure/pull/8)

## 0.2.0
  * Add messages, catalog, and parse namespaces [#4](https://github.com/singer-io/singer-clojure/pull/4)

## 0.1.0
  * Add bookmarks namespace
  * Initial project setup
