(ns clj-bash.utils-test
  (:require [clojure.test :refer :all]
            [clj-bash.utils :refer :all]))

(deftest match-seq-test
  (testing "normal conditions"
    (letfn [(test-match-seq [sequence]
              (match-seq
               sequence
               [:plus x y] (+ x y)
               [:list & elems] (apply list elems)
               :else 1000))]
      (are [lst expected]
          (= (test-match-seq lst) expected)
        '(:plus 10 20) 30
         [:plus 10 20] 30
        '(:list 1 2 3) '(1 2 3)
        '(5 4 3 2 1) 1000)))
  (testing "error conditions"
    (testing "A first argument is not a seq"
      (is (=       (match-seq '(:x 1 2) [:x y z] (+ y z)) 3))
      (is (thrown? Exception
                   (match-seq       12  [:x y z] (+ y z)))))
    (testing "A count of a body is not even"
      (is (thrown? Exception
                   (eval '(match-seq '(1 2)
                                     [x y] (+ x y)
                                     [x y z] ;; needs a expression
                                     )))))))

(deftest check-return-test
  (is (= (check-return
          [string? "should be a string"]
          100
          "test")
         "test"))
  (is (thrown-with-msg?
       Exception #"should be a string.*12"
       (check-return
        [string? "should be a string"]
        100
        12))))

(deftest ensure-no-unrecognized-keys-test
  (testing "legal arguments"
    (are [target recognized-key-lst]
        (= (ensure-no-unrecognized-keys target recognized-key-lst) true)
      '(:abc 1 :def 2) '(:abc :def)
      {:abc 1 :def 2}  '(:abc :def)
      '(:def 2 :abc 1) '(:abc :def)
      '(:abc 1) '(:abc :def)
      '() '(:abc :def)))
  (testing "illegal arguments"
    (are [target recognized-key-lst]
        (thrown-with-msg?
         IllegalArgumentException #"are not recognized keys"
         (ensure-no-unrecognized-keys target recognized-key-lst))
      '(:abc 1 :def 2) '(:abc)
      '(:abc 1 :def 2 :ghi 3) '(:ghi :abc))))
