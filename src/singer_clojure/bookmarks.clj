(ns singer-clojure.bookmarks
  (:gen-class))

(defn clear-bookmark
  "Clear the bookmark from state"
  [state tap-stream-id bookmark-key]
  (update-in state ["bookmarks" tap-stream-id] dissoc bookmark-key))

(defn reset-stream
  "Clear the stream's bookmarks from state"
  [state tap-stream-id]
  (update-in state ["bookmarks"] dissoc tap-stream-id))

(defn set-currently-syncing
  "Set currently syncing stream"
  [state tap-stream-id]
  (assoc state "currently_syncing" tap-stream-id))

(defn get-currently-syncing
  "Return current syncing stream"
  [state]
  (get state "currently_syncing"))
