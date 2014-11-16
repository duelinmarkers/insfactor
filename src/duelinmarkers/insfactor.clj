(ns duelinmarkers.insfactor
  (:require [clojure.zip :as z]
            [clojure.tools.analyzer.jvm :as ana]
            [duelinmarkers.insfactor.zip :as inzip]))

(defonce index (atom {}))

(defn- branch? [node]
  (if (map? node)
    (contains? node :children)
    (coll? node)))

(defn- children [node]
  (if (map? node)
    (mapcat (comp #(if (map? %) [%] %)
               node)
            (:children node))
    (seq node)))

(defn zipper [ns-analysis]
  (z/zipper branch? children inzip/no-editing ns-analysis))

(defn find-line-and-col [loc]
  (let [n (z/node loc)
        {:keys [line column]} (if (map? n) (:env n) nil)]
    (if (and line column)
      [line column]
      (if-let [parent (z/up loc)]
        (recur parent)
        nil))))

(defn next-non-branch [loc]
  (let [next-loc (z/next loc)]
    (if (and (branch? (z/node next-loc))
             (not= next-loc loc))
      (recur next-loc)
      next-loc)))

(defn coll->scalar-members [coll]
  (remove coll? (inzip/zipper-seq (z/zipper coll? seq nil coll))))

(defn indexable-vals [{:keys [op] :as node}]
  (condp = op
    :var (list (:var node))
    :the-var (list (:var node))
    :const (let [{:keys [type val]} node]
             (if (coll? val)
               (->> val
                    coll->scalar-members
                    (filter #(or (keyword? %)
                                 (string? %)
                                 (class? %))))
               (case type
                 (:keyword :string :class) [val]
                 (:nil :symbol :number :regex) nil
                 (println "unhandled const type:" type
                          "val:" (:val node)))))
    nil))

(defn index-usages [index ns-sym ns-analysis]
  (let [z (zipper ns-analysis)]
    (loop [loc (next-non-branch z) index index]
      (let [index (if-let [vals (seq (indexable-vals (z/node loc)))]
                    (reduce #(update-in %1 [%2 ns-sym] (fnil conj []) (find-line-and-col loc))
                            index vals)
                    index)]
        (if (z/end? loc)
          index
          (recur (next-non-branch loc) index))))))

(defn remove-usages [index ns-sym]
  (reduce #(update-in %1 [%2] dissoc ns-sym) index (keys index)))

(defn index! [ns-sym src-file-path]
  (let [ns-analysis (ana/analyze-ns ns-sym)]
    (swap! index remove-usages src-file-path)
    (swap! index index-usages src-file-path ns-analysis)))

(defn find-usages [val]
  (cons (str "Usages of " val)
        (map (fn [[k locs]] (cons k (map seq (sort locs)))) (@index val))))

(comment
  (index! 'duelinmarkers.insfactor "/foo/duelinmarkers/insfactor.clj")
  (find-usages #'index-usages)
  (swap! index empty)
  @index
  )
