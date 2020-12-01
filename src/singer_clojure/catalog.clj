(ns singer-clojure.catalog
  (:require [clojure.test :refer [is]]))

(defn- shuffle-streams
  "Shuffle streams to place `currently_syncing` at the front. If currently_syncing does not exist, start over."
  [selected-streams state]
  (let [currently-syncing (get state "currently_syncing")
        front (take-while #(not= currently-syncing %) selected-streams)
        back (drop-while #(not= currently-syncing %) selected-streams)]
    (concat back front)))

(defn get-selected-streams
  "Takes a deserialized catalog and returns a sequence of the selected
  tap-stream-ids"
  ([catalog]
   (get-selected-streams catalog {}))
  ([catalog state]
   (-> (filter #(get-in catalog [% "metadata" "selected"]) (keys catalog))
       (shuffle-streams state))))


(defn- serialize-stream-metadata-property
  [[stream-metadata-property-name stream-metadata-property-metadata :as stream-metadata-property]]
  {"metadata"   stream-metadata-property-metadata
   "breadcrumb" ["properties" stream-metadata-property-name]})

(defn- serialize-stream-metadata-properties
  [stream-metadata-properties]
  (let [properties (stream-metadata-properties "properties")]
    (concat [{"metadata" (dissoc stream-metadata-properties "properties")
              "breadcrumb" []}]
            (map serialize-stream-metadata-property properties))))

(defn- serialize-stream-metadata
  [{:keys [metadata] :as stream}]
  (update stream "metadata" serialize-stream-metadata-properties))

(defn- serialize-metadata
  [catalog]
  (update catalog "streams" (partial map serialize-stream-metadata)))

(defn- serialize-stream-schema-property
  [[k v]]
  (if (nil? v)
    [k {}]
    [k v]))

(defn- serialize-stream-schema-properties
  [stream-schema-properties]
  (into {} (map serialize-stream-schema-property
                stream-schema-properties)))

(defn- serialize-stream-schema
  [stream-schema]
  (update stream-schema
          "properties"
          serialize-stream-schema-properties))

(defn- serialize-stream
  [stream-catalog-entry]
  (update stream-catalog-entry "schema"
          serialize-stream-schema))

(defn- serialize-streams
  [catalog]
  (assoc {}
         "streams"
         (map serialize-stream (vals catalog))))

(defn serialize-catalog
  [catalog]
  (-> catalog
      serialize-streams
      serialize-metadata))

(defn- deserialize-stream-metadata
  [serialized-stream-metadata]
  (reduce (fn [metadata serialized-metadata-entry]
            (reduce (fn [entry-metadata [k v]]
                      (assoc-in
                       entry-metadata
                       (conj (serialized-metadata-entry "breadcrumb") k)
                       v))
                    metadata
                    (serialized-metadata-entry "metadata")))
          {}
          serialized-stream-metadata))

(defn- get-unsupported-breadcrumbs
  [stream-schema-metadata]
  (->> (stream-schema-metadata "properties")
       (filter (fn [[k v]]
                 (= "unsupported" (v "inclusion"))))
       (map (fn [[k _]]
              ["properties" k]))))

(defn- deserialize-stream-schema
  [serialized-stream-schema stream-schema-metadata]
  (let [unsupported-breadcrumbs (get-unsupported-breadcrumbs stream-schema-metadata)]
    (reduce (fn [acc unsupported-breadcrumb]
              (assoc-in acc unsupported-breadcrumb nil))
            serialized-stream-schema
            unsupported-breadcrumbs)))

(defn- deserialize-stream
  [serialized-stream]
  {:pre [(is (map? serialized-stream) (format "serialized-stream %s is not a map" serialized-stream))]}
  (as-> serialized-stream ss
    (update ss "metadata" deserialize-stream-metadata)
    (update ss "schema" deserialize-stream-schema (ss "metadata"))))

(defn- deserialize-streams
  [serialized-streams]
  (reduce (fn [streams deserialized-stream]
            (assoc streams (deserialized-stream "tap_stream_id") deserialized-stream))
          {}
          (map deserialize-stream serialized-streams)))

(defn deserialize-catalog
  [serialized-catalog]
  (deserialize-streams (serialized-catalog "streams")))
