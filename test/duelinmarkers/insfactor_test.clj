(ns duelinmarkers.insfactor-test
  (:require [clojure.test :refer :all]
            [duelinmarkers.insfactor :refer :all]
            [clojure.tools.analyzer.jvm :as ana]
            duelinmarkers.insfactor.subjects.ns-with-def))

(deftest usage-index-of-minimal-ns
  (is (= (index-usages {} "/path/to/minimal-ns.clj"
                       (ana/analyze-ns 'duelinmarkers.insfactor.subjects.minimal-ns))
         {#'clojure.core/conj {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/*loaded-libs* {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/deref {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/commute {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/refer {"/path/to/minimal-ns.clj" [[1 1]]}
          #'clojure.core/in-ns {"/path/to/minimal-ns.clj" [[1 1]]}
          ;; TODO probably ignore metadata.
          :file {"/path/to/minimal-ns.clj" [[1 5] [1 5] [1 5]]}
          :line {"/path/to/minimal-ns.clj" [[1 5] [1 5] [1 5]]}
          :column {"/path/to/minimal-ns.clj" [[1 5] [1 5] [1 5]]}
          :end-line {"/path/to/minimal-ns.clj" [[1 5] [1 5] [1 5]]}
          :end-column {"/path/to/minimal-ns.clj" [[1 5] [1 5] [1 5]]}
          ;; TODO this makes the test completely non-portable.
          "file:/home/hume/Projects/insfactor/test/duelinmarkers/insfactor/subjects/minimal_ns.clj"
          {"/path/to/minimal-ns.clj" [[1 5] [1 5] [1 5]]}})))

(deftest usages-of-var-in-many-contexts
  (is (= [[5 39]  ; namespaced, nested invoke arg in def init
          [8 4]   ; referred invoke in fn body
          [9 12]  ; let binding init expr
          [10 10] ; invoke arg in let body
          [12 14] ; invoke arg in try body
          [14 18] ; invoke arg in catch body
          [16 27] ; invoke arg in finally
          [17 13] ; k in map literal
          [18 17] ; v in map literal
          ]
         (get-in (index-usages {}
                               "/path/to/ns_using_var.clj"
                               (ana/analyze-ns 'duelinmarkers.insfactor.subjects.ns-using-var))
                 [#'duelinmarkers.insfactor.subjects.ns-with-def/something
                  "/path/to/ns_using_var.clj"]))))

(deftest usages-of-keyword
  (is (= [[17 12] ; start of map literal around kw
          [17 12] ; and again, since we use it twice
          ]
         (get-in (index-usages {}
                               "/path/to/ns_using_var.clj"
                               (ana/analyze-ns 'duelinmarkers.insfactor.subjects.ns-using-var))
                 [:kw "/path/to/ns_using_var.clj"]))))

(deftest of-coll->scalar-members
  (is (= #{:foo 1 2 :bar "s"}
         (set (coll->scalar-members {:foo [1 2] :bar [["s"]]})))))
