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

(defn indexable-val [{:keys [op] :as node}]
  (condp = op
    :var (:var node)
    :the-var (:var node)
    :keyword (:val node)
    :string (:val node)
    ;; :constant (:val node)
    nil))

(defn index-usages [index ns-sym ns-analysis]
  (let [z (zipper ns-analysis)]
    (loop [loc (next-non-branch z) index index]
      (let [index (if-let [v (indexable-val (z/node loc))]
                    (update-in index [v ns-sym] (fnil conj []) (find-line-and-col loc))
                    index)]
        (if (z/end? loc)
          index
          (recur (next-non-branch loc) index))))))

(defn index! [ns-sym]
  (let [ns-analysis (ana/analyze-ns ns-sym)]
    (swap! index index-usages ns-sym ns-analysis)))

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
