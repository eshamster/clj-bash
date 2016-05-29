(ns clj-bash.core-test
  (:require [clojure.test :refer :all]
            [clj-bash.core :refer :all]
            [clj-bash.test-utils :refer :all]))

(deftest a-test
  (test-bash sample
             (:echo test1)
             (:echo test2)))
