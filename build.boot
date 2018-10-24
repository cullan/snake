(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [adzerk/boot-cljs "2.1.4"]
                 [pandeiro/boot-http "0.8.3"]
                 [nrepl "0.4.5" :scope "test"]
                 [adzerk/boot-reload "0.6.0"]
                 [adzerk/boot-cljs-repl "0.4.0"]
                 [cider/piggieback "0.3.10" :scope "test"]
                 [weasel "0.7.0" :scope "test"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

(deftask dev
  "Launch dev environment"
  []
  (comp
   (serve :dir "target")
   (watch)
   (reload)
   (cljs-repl)
   (cljs)
   (target :dir #{"target"})))
