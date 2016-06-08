(ns clj-bash.str-body-test
  (:require [clojure.test :refer :all]
            [clj-bash.str-body :refer :all]))

(deftest str-body?-test
  (are [lst] (str-body? lst)
    '("test")
    '("test" "test2")
    '("test" ("test1" 0) "test2"))
  (are [lst] (not (str-body? lst))
    '(0)
    '("test" ("test2"))
    '(("test2") "test")))

(deftest wrap-str-body-test
  (are [left body right expected]
      (= (wrap-str-body left body right)
         expected)
    "(" '("abc") ")"      '("(abc)")
    "([" '("abc") "])"    '("([abc])")
    "(" '("a" "b") ")"    '("(a" "b)")
    "(" '("a" ("b" "c") "d") ")"    '("(a" ("b" "c") "d)"))
  (testing "Illegal types"
    (are [left body right]
        (thrown? IllegalArgumentException
                 (wrap-str-body left body right))
      1 '("abc") ")"
      "(" '("abc") 0
      "(" '(0 "abc") ")")))
