(ns clj-bash.core-test
  (:require [clojure.test :refer :all]
            [clj-bash.core :refer :all]
            [clj-bash.test-utils :refer :all]))

(deftest a-test
  (test-bash simple-execution
             (:echo 0)
             (:echo -n "in _")
             (:echo test)))
