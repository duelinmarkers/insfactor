(ns duelinmarkers.insfactor
  (:require [clojure.zip :as z]
            [clojure.tools.analyzer :as ana]))

(defonce index (atom {}))

(def op->children-fn {:do :exprs
                      :if #(seq ((juxt :test :then :else) %))
                      :def (fn [{:keys [init-provided init]}]
                             (when init-provided (list init)))
                      :fn-expr :methods
                      :fn-method (comp seq :body)
                      :let (fn [{:keys [binding-inits body]}]
                             (concat (map :init binding-inits)
                                     (:exprs body)))
                      :invoke (fn [{:keys [fexpr args]}]
                                (cons fexpr args))
                      :static-method :args
                      :instance-method (fn [{:keys [target args]}]
                                         (cons target args))
                      :map :keyvals
                      :set :keys
                      :try (fn [{:keys [try-expr finally-expr catch-exprs]}]
                             (cons try-expr
                                   (concat (map :handler catch-exprs)
                                           (list finally-expr))))
                      :new :args
                      :catch (fn [{:keys [handler]}] (list handler))
                      })

(defn- branch? [node]
  (if (map? node)
    (contains? op->children-fn (:op node))
    (coll? node)))

(defn- children [node]
  (if (map? node)
    ((op->children-fn (:op node)) node)
    (seq node)))

(defn zipper [ns-analysis]
  (z/zipper branch? children
            #(throw (UnsupportedOperationException. "no editing!"))
            ns-analysis))

(defn find-line-and-col [loc]
  (let [n (z/node loc)
        {:keys [line column]} (if (map? n) (:env n) nil)]
    (if (and line column)
      [line column]
      (recur (z/up loc)))))

(defn next-non-branch [loc]
  (let [next-loc (z/next loc)]
    (if (and (branch? (z/node next-loc))
             (not= next-loc loc))
      (recur next-loc)
      next-loc)))

(defn zipper-seq
  ([zip] (zipper-seq (z/node zip) (z/next zip)))
  ([node next-loc]
     (cons node
           (when-not (z/end? next-loc)
             (lazy-seq (zipper-seq (z/node next-loc) (z/next next-loc)))))))

(defn coll->scalar-members [coll]
  (remove coll? (zipper-seq (z/zipper coll? seq nil coll))))

(defn indexable-vals [{:keys [op] :as node}]
  (condp = op
    :var (list (:var node))
    :the-var (list (:var node))
    :keyword (list (:val node))
    :string (list (:val node))
    :constant (let [{:keys [val]} node]
                (when (coll? val) (coll->scalar-members val)))
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

(defn index! [ns-sym]
  (let [ns-analysis (ana/analyze-ns ns-sym)]
    (swap! index remove-usages ns-sym)
    (swap! index index-usages ns-sym ns-analysis)))

(defn find-usages [val]
  (cons (str "Usages of " val)
        (map (fn [[k locs]] (cons k (map seq (sort locs)))) (@index val))))

(comment
  (index! 'duelinmarkers.insfactor)
  (find-usages #'index-usages)
  )
