(defproject duelinmarkers/insfactor "0.2.0"
  :description "Tool to ease Clojure refactoring"
  :url "http://github.com/duelinmarkers/insfactor"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/jvm.tools.analyzer "0.4.4" :exclusions [org.clojure/clojurescript]]
                 [org.clojure/tools.nrepl "0.2.3"]]
  :repl-options {:nrepl-middleware [duelinmarkers.insfactor.nrepl/index-on-load]})
