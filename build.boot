(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [adzerk/boot-cljs "2.1.4"]])

(require '[adzerk.boot-cljs :refer [cljs]])
