(ns duelinmarkers.insfactor-test
  (:require [clojure.test :refer :all]
            [duelinmarkers.insfactor :refer :all]
            [clojure.tools.analyzer :as ana]
            duelinmarkers.insfactor.subjects.ns-with-def))

(deftest usage-index-of-minimal-ns
  (is (= (index-usages {} 'duelinmarkers.insfactor.subjects.minimal-ns
                       (ana/analyze-ns 'duelinmarkers.insfactor.subjects.minimal-ns))
         {#'clojure.core/conj
          {'duelinmarkers.insfactor.subjects.minimal-ns [[1 1]]}
          #'clojure.core/*loaded-libs*
          {'duelinmarkers.insfactor.subjects.minimal-ns [[1 1]]}
          #'clojure.core/deref
          {'duelinmarkers.insfactor.subjects.minimal-ns [[1 1]]}
          #'clojure.core/commute
          {'duelinmarkers.insfactor.subjects.minimal-ns [[1 1]]}
          #'clojure.core/refer
          {'duelinmarkers.insfactor.subjects.minimal-ns [[1 1]]}
          #'clojure.core/in-ns
          {'duelinmarkers.insfactor.subjects.minimal-ns [[1 1]]}})))

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
                               'duelinmarkers.insfactor.subjects.ns-using-var
                               (ana/analyze-ns 'duelinmarkers.insfactor.subjects.ns-using-var))
                 [#'duelinmarkers.insfactor.subjects.ns-with-def/something
                  'duelinmarkers.insfactor.subjects.ns-using-var]))))

(deftest usages-of-keyword
  (is (= [[17 3] ; start of println around map literal
          [17 3] ; and again, since we use it twice
          ]
         (get-in (index-usages {}
                               'duelinmarkers.insfactor.subjects.ns-using-var
                               (ana/analyze-ns 'duelinmarkers.insfactor.subjects.ns-using-var))
                 [:kw
                  'duelinmarkers.insfactor.subjects.ns-using-var]))))
