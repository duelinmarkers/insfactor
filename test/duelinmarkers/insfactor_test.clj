(ns duelinmarkers.insfactor-test
  (:require [clojure.test :refer :all]
            [duelinmarkers.insfactor :refer :all]
            [clojure.tools.analyzer :as ana]))

(deftest usage-index-of-minimal-ns
  (is (= (index-usages {} 'duelinmarkers.insfactor.subjects.minimal-ns
                       (ana/analyze-ns 'duelinmarkers.insfactor.subjects.minimal-ns))
         {#'clojure.core/conj
          {'duelinmarkers.insfactor.subjects.minimal-ns [1 1]}
          #'clojure.core/*loaded-libs*
          {'duelinmarkers.insfactor.subjects.minimal-ns [1 1]}
          #'clojure.core/deref
          {'duelinmarkers.insfactor.subjects.minimal-ns [1 1]}
          #'clojure.core/commute
          {'duelinmarkers.insfactor.subjects.minimal-ns [1 1]}
          #'clojure.core/in-ns
          {'duelinmarkers.insfactor.subjects.minimal-ns [1 1]}})))
