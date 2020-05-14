(defproject singer-clojure "0.1.0-SNAPSHOT"
  :description "Clojure library for shared code between clojure taps"
  :url "https://github.com/singer-io/singer-clojure"
  :license {:name "GNU General Public License Version 3; Other commercial licenses available."
            :url "http://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]

                 ;; repl
                 [org.clojure/tools.nrepl "0.2.13"]
                 [cider/cider-nrepl "0.17.0"]
                 ]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
