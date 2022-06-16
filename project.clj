(defproject singer-clojure "1.1.8"
  :description "Clojure library for shared code between clojure taps"
  :url "https://github.com/singer-io/singer-clojure"
  :license {:name "GNU Affero General Public License Version 3; Other commercial licenses available."
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :resource-paths ["resources/base"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]

                 ;; repl
                 [org.clojure/tools.nrepl "0.2.13"]
                 [cider/cider-nrepl "0.17.0"]

                 ;; Basic log4j dependency to declare bare minimum
                 [org.clojure/tools.logging "1.2.4"]
                 [org.slf4j/slf4j-log4j12 "1.7.36"]
                 [org.apache.logging.log4j/log4j-1.2-api "2.17.1"]
                 [org.apache.logging.log4j/log4j-core "2.17.1"]

                 [org.clojure/tools.cli "0.4.1"]]
  :target-path "target/%s"
  :plugins [[cider/cider-nrepl "0.25.2"]]
  :profiles {:uberjar {:aot :all}
             :system {:java-cmd "/usr/lib/jvm/java-11-openjdk-amd64/bin/java"}})
