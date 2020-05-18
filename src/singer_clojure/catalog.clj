(ns singer-clojure.catalog)

(defn get-selected-streams
  [catalog]
  "Takes a deserialized catalog and returns a sequence of the selected
  tap-stream-ids"
  (filter #(get-in catalog [% "metadata" "selected"]) (keys catalog)))

;;
;;
;; SERIALIZATION
;;
;;
(defn serialize-stream-metadata-property
  [[stream-metadata-property-name stream-metadata-property-metadata :as stream-metadata-property]]
  {"metadata"   stream-metadata-property-metadata
   "breadcrumb" ["properties" stream-metadata-property-name]})

(defn serialize-stream-metadata-properties
  [stream-metadata-properties]
  (let [properties (stream-metadata-properties "properties")]
    (concat [{"metadata" (dissoc stream-metadata-properties "properties")
              "breadcrumb" []}]
            (map serialize-stream-metadata-property properties))))

(defn serialize-stream-metadata
  [{:keys [metadata] :as stream}]
  (update stream "metadata" serialize-stream-metadata-properties))

(defn serialize-metadata
  [catalog]
  (update catalog "streams" (partial map serialize-stream-metadata)))

(defn serialize-stream-schema-property
  [[k v]]
  (if (nil? v)
    [k {}]
    [k v]))

(defn serialize-stream-schema-properties
  [stream-schema-properties]
  (into {} (map serialize-stream-schema-property
                stream-schema-properties)))

(defn serialize-stream-schema
  [stream-schema]
  (update stream-schema
          "properties"
          serialize-stream-schema-properties))

(defn serialize-stream
  [stream-catalog-entry]
  (update stream-catalog-entry "schema"
          serialize-stream-schema))

(defn serialize-streams
  [catalog]
  (assoc {}
         "streams"
         (map serialize-stream (vals catalog))))

(defn serialize-catalog
  [catalog]
  (-> catalog
      serialize-streams
      serialize-metadata))



;;
;;
;; DESERIALIZATION
;;
;;
(defn deserialize-stream-metadata
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

(defn get-unsupported-breadcrumbs
  [stream-schema-metadata]
  (->> (stream-schema-metadata "properties")
       (filter (fn [[k v]]
                 (= "unsupported" (v "inclusion"))))
       (map (fn [[k _]]
              ["properties" k]))))

(defn deserialize-stream-schema
  [serialized-stream-schema stream-schema-metadata]
  (let [unsupported-breadcrumbs (get-unsupported-breadcrumbs stream-schema-metadata)]
    (reduce (fn [acc unsupported-breadcrumb]
              (assoc-in acc unsupported-breadcrumb nil))
            serialized-stream-schema
            unsupported-breadcrumbs)))

(defn deserialize-stream
  [serialized-stream]
  {:pre [(map? serialized-stream)]}
  (as-> serialized-stream ss
    (update ss "metadata" deserialize-stream-metadata)
    (update ss "schema" deserialize-stream-schema (ss "metadata"))))

(defn deserialize-streams
  [serialized-streams]
  (reduce (fn [streams deserialized-stream]
            (assoc streams (deserialized-stream "tap_stream_id") deserialized-stream))
          {}
          (map deserialize-stream serialized-streams)))

(defn deserialize-catalog
  [serialized-catalog]
  (deserialize-streams (serialized-catalog "streams")))