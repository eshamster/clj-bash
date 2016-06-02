(ns clj-bash.cb-macro-test
  (:require [clojure.test :refer :all]
            [clj-bash.cb-macro :refer :all]))

(defmacro check-not-expanded [& targets]
  `(do ~@(map (fn [x#] `(is (= (cb-macroexpand ~x#) ~x#)))
              targets)))

(deftest a-test
  (init-cb-macro-table)
  (testing "test simple registering and expanding"
    (check-not-expanded '(abc 0 3))
    (is (not (cb-macro? 'abc)))
    (def-cb-macro abc [x y] `(:echo ~x ~y))
    (is (= (cb-macroexpand '(abc 0 3))
           '(:echo 0 3)))
    (is (cb-macro? 'abc)))
  (testing "test nested expanding"
    (def-cb-macro macro-a [x] `(:cat ~x))
    (def-cb-macro macro-b [x] `(macro-a ~x))
    (is (= (cb-macroexpand '(macro-b test.txt))
           '(:cat test.txt))))
  (testing "test doing nothing if the first is not a cb-macro function"
    (check-not-expanded '(1 2 3)
                        1
                        "str"
                        '((abc 0 1))
                        nil))
  (init-cb-macro-table))
