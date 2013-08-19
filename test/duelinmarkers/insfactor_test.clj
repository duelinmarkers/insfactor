(ns duelinmarkers.insfactor-test
  (:require [clojure.test :refer :all]
            [duelinmarkers.insfactor :refer :all]
            [clojure.tools.analyzer :as ana]))

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

(deftest usages-in-def-and-fn-body
  (is (= [[5 30] ; start of reverse :invoke in def init
          [8 3]  ; start of str :invoke in fn body
          [10 5] ; start of str :invoke in let body, which is weird, since real usage is in binding init.
          [12 5] ; start of println in try body
          [14 7] ; start of println in catch
          [16 7] ; start of println in finally
          ]
         (get-in (index-usages {}
                               'duelinmarkers.insfactor.subjects.ns-using-var
                               (ana/analyze-ns 'duelinmarkers.insfactor.subjects.ns-using-var))
                 [#'duelinmarkers.insfactor.subjects.ns-with-def/something
                  'duelinmarkers.insfactor.subjects.ns-using-var]))))
