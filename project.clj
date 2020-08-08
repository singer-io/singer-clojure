(defproject singer-clojure "1.0.0"
  :description "Clojure library for shared code between clojure taps"
  :url "https://github.com/singer-io/singer-clojure"
  :license {:name "GNU Affero General Public License Version 3; Other commercial licenses available."
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]

                 ;; repl
                 [org.clojure/tools.nrepl "0.2.13"]
                 [cider/cider-nrepl "0.17.0"]

                 [org.clojure/tools.cli "0.4.1"]]
  :plugins [[jonase/eastwood "0.3.10"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :system {:java-cmd "/usr/lib/jvm/java-11-openjdk-amd64/bin/java"}})
