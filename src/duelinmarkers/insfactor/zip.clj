(ns duelinmarkers.insfactor.zip
  (:require [clojure.zip :as z])
  (:import [java.io File]))

(defn zipper-seq [zip]
  (->> zip
       (iterate z/next)
       (take-while (comp not z/end?))
       (map z/node)))

(defn no-editing [& args]
  (throw (UnsupportedOperationException. "This zipper does not allow editing.")))

(defn file-zipper [base-dir]
  (z/zipper (memfn ^File isDirectory) (memfn ^File listFiles)
            no-editing base-dir))
