(ns duelinmarkers.insfactor.project
  (:require [clojure.java.io :as io]
            [duelinmarkers.insfactor :as insfactor]))

(defn find-lein-project-file []
  (loop [dir (io/file (.getParent (io/file (.getAbsolutePath (io/file ".")))))]
    (let [file (io/file dir "project.clj")]
      (if (.exists file)
        file
        (if-let [parent (io/file (.getParent dir))]
          (recur parent)
          nil)))))

(defn src-paths-from-lein-project-file [file]
  (let [project-def (with-open [reader (java.io.PushbackReader. (io/reader file))]
                      (read reader))]))

(defn find-project-src-paths []
  (if-let [lein-project-file (find-lein-project-file)]
    (src-paths-from-lein-project-file lein-project-file)
    (throw (Exception. "Couldn't find lein project.clj"))))

(defn find-src-files [src-path])

(defn index-project! []
  (doseq [src-path (find-project-src-paths)
          src-file-path (find-src-files src-path)]
    (insfactor/index! ))
  )

(comment
  ;; defaults:
  :source-paths ["src"] :test-paths ["test"]
  )
