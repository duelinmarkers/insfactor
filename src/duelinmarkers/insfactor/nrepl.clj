(ns duelinmarkers.insfactor.nrepl
  (:require [clojure.string :as string]
            [clojure.tools.nrepl.middleware :as middleware]
            [duelinmarkers.insfactor :as insfactor]))

(defn file->ns-sym [src]
  (symbol (fnext (re-find #"\(ns\s+([a-zA-Z0-9.*+!?_-]+)" src))))

(defn index-on-load [h]
  (fn [{:keys [op file] :as msg}]
    (h msg)
    (when (= op "load-file")
      (insfactor/index! (file->ns-sym file)))))

(middleware/set-descriptor! #'index-on-load
                            {:expects #{"load-file"}})
