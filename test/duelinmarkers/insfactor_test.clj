(ns duelinmarkers.insfactor-test
  (:require [clojure.test :refer :all]
            [duelinmarkers.insfactor :refer :all]
            [clojure.tools.analyzer :as ana]
            duelinmarkers.insfactor.subjects.ns-with-def))

(deftest usage-index-of-minimal-ns
  (is (= (index-usages {} "/path/to/minimal-ns.clj"
                       (ana/analyze-ns 'duelinmarkers.insfactor.subjects.minimal-ns))
         {#'clojure.core/conj {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/*loaded-libs* {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/deref {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/commute {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/refer {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/in-ns {"/path/to/minimal-ns.clj" [[1 1]]}})))

(deftest usages-of-var-in-many-contexts
  (is (= [[5 30] ; start of reverse :invoke in def init
          [8 3]  ; start of str :invoke in fn body
          [10 5] ; start of str :invoke in let body, which is weird, since real usage is in binding init.
          [12 5] ; start of println in try body
          [14 7] ; start of println in catch
          [16 7] ; start of println in finally
          [17 3] ; start of println around map literal
          [17 3] ; and again, since we use it twice
          ]
         (get-in (index-usages {}
                               "/path/to/ns_using_var.clj"
                               (ana/analyze-ns 'duelinmarkers.insfactor.subjects.ns-using-var))
                 [#'duelinmarkers.insfactor.subjects.ns-with-def/something
                  "/path/to/ns_using_var.clj"]))))

(deftest usages-of-keyword
  (is (= [[17 3] ; start of println around map literal
          [17 3] ; and again, since we use it twice
          ]
         (get-in (index-usages {}
                               "/path/to/ns_using_var.clj"
                               (ana/analyze-ns 'duelinmarkers.insfactor.subjects.ns-using-var))
                 [:kw "/path/to/ns_using_var.clj"]))))

(deftest of-coll->scalar-members
  (is (= [:foo 1 2 :bar "s"]
         (coll->scalar-members {:foo [1 2] :bar [["s"]]}))))
