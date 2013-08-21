(ns duelinmarkers.insfactor.nrepl-test
  (:use clojure.test duelinmarkers.insfactor.nrepl))

(deftest of-src->ns-sym
  (is (= 'foo.bar (src->ns-sym "(ns foo.bar)")))
  (is (= 'foo.bar (src->ns-sym "(ns foo.bar\n  (:require foo.baz))")))
  (is (= 'foo.bar (src->ns-sym "(set! *warn-on-reflection* true)\n(ns foo.bar\n  \"some docs\")"))))
