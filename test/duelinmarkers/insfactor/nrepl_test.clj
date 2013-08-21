(ns duelinmarkers.insfactor.nrepl-test
  (:use clojure.test duelinmarkers.insfactor.nrepl))

(deftest of-file->ns-sym
  (is (= 'foo.bar (file->ns-sym "(ns foo.bar)")))
  (is (= 'foo.bar (file->ns-sym "(ns foo.bar\n  (:require foo.baz))")))
  (is (= 'foo.bar (file->ns-sym "(set! *warn-on-reflection* true)\n(ns foo.bar\n  \"some docs\")"))))
