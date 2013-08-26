(ns duelinmarkers.insfactor.project
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [duelinmarkers.insfactor :as insfactor]))

(defn find-lein-project-file []
  (loop [dir (.getCanonicalFile (io/file "."))]
    (let [file (io/file dir "project.clj")]
      (if (.exists file)
        file
        (if-let [parent (.getParentFile dir)]
          (recur parent)
          nil)))))

(defn src-paths-from-lein-project-file [file]
  (let [project-def (with-open [reader (java.io.PushbackReader. (io/reader file))]
                      (read reader))]
    (when-not (= 'defproject (first project-def))
      (throw (Exception. (str "project file format unrecognized: " project-def))))
    (let [base-path (.getParentFile file)
          {:keys [source-paths test-paths]} (apply hash-map (next project-def))]
      (->> (concat (or source-paths ["src"])
                   (or test-paths ["test"]))
           (map #(io/file base-path %))
           set))))

(defn find-project-src-paths []
  (if-let [lein-project-file (find-lein-project-file)]
    (src-paths-from-lein-project-file lein-project-file)
    (throw (Exception. "Couldn't find lein project.clj"))))

(defn find-src-files [src-path]

  )

(defn ->ns-sym [relative-src-file-path]
  (-> relative-src-file-path
      (string/replace #".clj\Z" "")
      (string/replace "/" ".")
      (string/replace "_" "-")
      symbol))

(defn index-project! []
  (doseq [src-path (find-project-src-paths)
          [src-path relative-src-file-path] (find-src-files src-path)]
    (insfactor/index! (->ns-sym relative-src-file-path)
                      (.getPath (io/file src-path relative-src-file-path)))))
