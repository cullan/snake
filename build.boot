(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [adzerk/boot-cljs "2.1.4"]
                 [adzerk/boot-reload "0.6.0"]
                 [adzerk/boot-cljs-repl "0.4.0"]
                 [pandeiro/boot-http "0.8.3"]
                 [nrepl "0.4.5" :scope "test"]
                 [cider/piggieback "0.3.10" :scope "test"]
                 [weasel "0.7.0" :scope "test"]
                 [reagent "0.8.1"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

(deftask build []
  (comp (notify)
        (cljs)))

(deftask run []
  (comp (serve)
        (watch)
        (reload)
        (cljs-repl)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none})
  identity)

(deftask dev []
  (comp (development)
        (run)))
