# singer-clojure
Clojure library for shared code between clojure taps

## Usage
#### Add dependency to  project clj
```
[singer-clojure "0.2.0"]
```

#### Import namespace
```
[singer-clojure.messages :as singer-messages]
```

#### Example use
```
(singer-messages/write-schema! catalog "my_table" ["id"] ["updated_at"])
(singer-messages/write-record! "my_table" {"id" "b" "updated_at" "2020-01-01T01:00:00Z"} {:version 1234 :time-extracted "2020-01-02T00:00:00Z"})
(singer-messages/write-record! "my_table" {"id" "d" "updated_at" "2020-01-01T02:00:00Z"} {:version 1234 :time-extracted "2020-01-02T00:00:00Z"})
(singer-messages/write-state! {"bookmarks" {"my_table" {"updated_at" "2020-01-01T02:00:00Z"}}})
(singer-messages/write-activate-version! "my_table" 1234)
```

## Deploying
See instructions in `how-to-sources`

## License

Distributed under the Apache License Version 2.0

---

Copyright © 2020 Stitch
