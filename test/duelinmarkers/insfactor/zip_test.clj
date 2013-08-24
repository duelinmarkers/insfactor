(ns duelinmarkers.insfactor.zip-test
  (:use clojure.test duelinmarkers.insfactor.zip))

(deftest of-zipper-seq
  (is (= [[1 [2 3] 4] 1 [2 3] 2 3 4]
         (zipper-seq (clojure.zip/vector-zip [1 [2 3] 4]))))
  (is (= [[]]
         (zipper-seq (clojure.zip/vector-zip [])))))
