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
                                           (list finally-expr))))})

(defn- branch? [node]
  (if (map? node)
    (contains? op->children-fn (:op node))
    ;; TODO also :constant ops w/ coll vals?
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
    ;; example vals of :op :constant
    ;; #"regex"
    ;; {:expects # {"load-file"}}
    ;; {:arglists ([src]) :column 1 :line 6 :file "some.string"} (in :def :meta)
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
  (vec (@index val)))

(comment
  (index! 'duelinmarkers.insfactor)
  (find-usages #'index-usages)
  )

;;;;;;;;; Unintuitive ops

;; `ns is a :do :op with
;;   in-ns
;;   invoke anon fn that
;;     pushThreadBindings clojure.lang.Compiler/LOADER
;;     try
;;       refer clojure.core
;;       require WHATEVER
;;       etc
;;       finally popThreadBindings
;;   conj onto *loaded-libs*

;; `defn is a :def :op with :fn-expr init


;;;;;;;;; Special keys by :op

;; :do
;;   :exprs

;; :if
;;   :test
;;   :then
;;   :else

;; :def
;;   :var
;;   :meta {:op :constant,,,}
;;   :init (for defn, {:op :fn-expr,,,}
;;   :init-provided
;;   :is-dynamic

;; :constant
;;   :val

;; :string
;;   :val String

;; :nil
;;   :val nil

;; :fn-expr
;;   :methods seq of {:op :fn-method,,,}
;;   :variadic-method
;;   :tag nil when none

;; :fn-method
;;   :body {:op :do,,,}
;;   :required-params seq of {:op :local-binding,,,}
;;   :rest-param nil when none

;; :let
;;   :binding-inits seq of {:op :binding-init,,,}
;;   :body {:op :do,,,}
;;   :is-loop boolean

;; :binding-init
;;   :local-binding {:op :local-binding,,,}

;; :invoke
;;   :args seq of expressions
;;   :protocol-on
;;   :is-protocol
;;   :is-direct
;;   :tag nil when none
;;   :site-index
;;   :fexpr {:op :var,,,}

;; :var - use of a var
;;   :var
;;   :tag nil when none

;; :the-var - mention of a var (with #' or var special form)
;;   :var

;; :local-binding-expr - use of a local
;;   :local-binding {:op :local-binding,,,}

;; :local-binding
;;   :sym
;;   :tag
;;   :init nil on usage, valued in :binding-init

;; :static-method
;;   :class class
;;   :method-name String
;;   :method clojure.reflect.Method{:name Symbol, :return-type Class, :declaring-class Class, :parameter-types [], :exception-types [], :flags #{:public :final :static}}
;;   :args seq of expressions

;; :instance-method
;;   :target expression
;;   :method-name String
;;   :method {:name Symbol, :return-type Class, :declaring-class Class, :parameter-types [], :exception-types [], :flags #{:public :final}}
;;   :args seq of expressions

;; :map
;;   :keyvals
