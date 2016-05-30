(ns clj-bash.core-test
  (:require [clojure.test :refer :all]
            [clj-bash.core :refer :all]
            [clj-bash.test-utils :refer :all]))

(deftest a-test
  (test-bash simple-execution
             (:echo 0)
             (:echo -n "in _")
             (:echo test))
  (test-bash nested-execution
             (:echo 12 (:echo 34)))
  (test-bash for-array
             (for i [0 4 3 5] (:echo $i)))
  (test-bash for-expression
             (for count (:seq 0 5) (:echo $count)))
  (test-bash for-nested
             (for i [0 4 3]
                  (for j (:seq 0 3)
                       (:echo "$i, $j"))))
  (test-bash pipe
             (-> (:echo testabcdefg)
                 (:sed -e "s/es/aa/")
                 (:head -c 4))))
