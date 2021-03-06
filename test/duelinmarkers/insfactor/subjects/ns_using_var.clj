(ns duelinmarkers.insfactor.subjects.ns-using-var
  (:require clojure.string
            [duelinmarkers.insfactor.subjects.ns-with-def :as deffer :refer [something]]))

(def my-something (apply str (reverse deffer/something)))

(defn my-func [s]
  (something s)
  (let [ss something]
    (str something))
  (try
    (println something)
    (catch Exception e
      (println e something))
    (finally
      (println "finally!" something)))
  (println {something :kw
            :kw something}))
