(ns clj-bash.str-body-test
  (:require [clojure.test :refer :all]
            [clj-bash.str-body :refer :all]))

(deftest str-body?-test
  (are [lst] (str-body? lst)
    "test"
    '("test")
    '("test" "test2")
    '("test" ("test1" 0) "test2"))
  (are [lst] (not (str-body? lst))
    0
    '(0)
    '("test" ("test2"))
    '(("test2") "test")))

(deftest concat-str-body-test
  (are [args expected] (= (apply concat-str-body args) expected)
    '("test") "test"
    '(("a" (1) "b")) '("a" (1) "b")
    '("test" " | " "abc") "test | abc"
    '("test" ("a" (1) "b")) '("testa" (1) "b")
    '(("a" (1) "b") "test") '("a" (1) "btest")
    '(("a" (1) "b") ("c" (2) "d")) '("a" (1) "bc" (2) "d")
    '(("a" (1) "b") " > " ("c" (2) "d")) '("a" (1) "b > c" (2) "d"))
  (testing "Wrong arguments"
    (is (thrown? Exception (concat-str-body)))
    (is (thrown? IllegalArgumentException (concat-str-body "test" 0 '("ab" "cd"))))))

(deftest wrap-str-body-test
  (are [left body right expected]
      (= (wrap-str-body left body right)
         expected)
    "(" "abc" ")"         "(abc)"
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

(deftest join-str-body-test
  (are [args expected]
      (= (join-str-body " | " args) expected)
    '() ""
    '("test") "test"
    '("a" "b" "c") "a | b | c"
    '("a" ("b") "c") '("a | b | c")
    '("a" ("b" "c" "d") "e") '("a | b" "c" "d | e")
    '(("for i in $(seq test); do" ("echo $i") "done") ("cat")) '("for i in $(seq test); do" ("echo $i") "done | cat"))
  (testing "Illegal types"
    (are [delimiter lst]
        (thrown? IllegalArgumentException
                 (join-str-body delimiter lst))
      0 '("a")
      "a" 0
      "a" '("a" 1 "b"))))

(deftest cons-str-body-test
  (are [left str-body-lst expected]
      (= (cons-str-body left str-body-lst) expected)
    "a" '("b")            '("a" "b")
    '("a" "b") '("c" "d") '("a" "b" "c" "d")
    '("a" "b") '("c" ("d" "e")) '("a" "b" "c" ("d" "e"))
    '("a" ("b") "c") '("d" "e") '("a" ("b") "c" "d" "e"))
  (testing "Illegal Argument"
    (are [left str-body-lst]
        (thrown? IllegalArgumentException
                 (cons-str-body left str-body-lst))
      0 '("b")
      "a" "b"
      "a" '("b" 0))))

(deftest construct-str-body-lst-test
  (testing "normal patterns"
    (testing "concat strings"
      (is (= (construct-str-body-lst '(1 2 3) str)
             '("1" "2" "3"))))
    (testing "concat lists"
      (is (= (construct-str-body-lst '(("a" ("ab") "b") ("c" "d")) reverse)
             '("b" ("ab") "a" "d" "c"))))
    (testing "concat both strings and lists"
      (is (= (construct-str-body-lst '(("a" ("ab") "b") "str1" ("c" "d") "str2") identity)
             '("a" ("ab") "b" "str1" "c" "d" "str2"))))))

(deftest reverse-str-body)
