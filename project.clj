(defproject duelinmarkers/insfactor "0.3.0-SNAPSHOT"
  :description "Tool to ease Clojure refactoring"
  :url "http://github.com/duelinmarkers/insfactor"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.analyzer.jvm "0.6.4"]
                 [org.clojure/tools.nrepl "0.2.3"]]
  :repl-options {:nrepl-middleware [duelinmarkers.insfactor.nrepl/index-on-load]}
  )
