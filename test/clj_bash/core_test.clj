(ns clj-bash.core-test
  (:require [clojure.test :refer :all]
            [clj-bash.core :refer :all]
            [clj-bash.cb-macro :refer :all]
            [clj-bash.test-utils :refer :all]))

(deftest a-test
  (test-bash simple-execution
             (:echo 0)
             (:echo -n " in ")
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
                 (:head -c 4)))
  (test-bash set-value
             (set a 0)
             (set b (:expr $a + 1))
             (:echo "$a, $b"))
  (test-bash defn
             (defn add [x y]
               (:expr $x + $y))
             (:add 10 20))
  (test-bash cond
             (defn test-cond [a]
               (cond ($a -gt 0) (:echo 1)
                     ($a -lt 0) (:echo -1)
                     :else (:echo 0)))
             (:test-cond 10)
             (:test-cond -10)
             (:test-cond 0)
             (cond (0 -eq 0) (:echo 100))
             (cond (0 -ne 0) (:echo 0)
                   :else (:echo -100))))

(deftest cb-macro-test
  (init-cb-macro-table)
  (def-cb-macro test-addr [a b]
    `(:expr ~a + ~b))
  (test-bash cb-macro
             (test-addr 10 20)
             (:echo a (test-addr 20 50))
             (test-addr (test-addr 1 2) 3))
  (init-cb-macro-table))

(deftest default-cb-macros-test
  (test-bash if
             (defn test-if [x]
               (if ($x -gt 0)
                 (:echo 100)
                 (:echo -100))
               (if ($x -lt 0)
                 (:echo -99)))
             (:test-if 10)
             (:test-if -10)))
