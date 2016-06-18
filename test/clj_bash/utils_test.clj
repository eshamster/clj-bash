(ns clj-bash.utils-test
  (:require [clojure.test :refer :all]
            [clj-bash.utils :refer :all]))

(deftest match-seq-test
  (testing "normal conditions"
    (letfn [(test-match-seq [sequence]
              (match-seq
               sequence
               [:plus x y] (+ x y)
               [:list & elems] (apply list elems)))]
      (are [lst expected]
          (= (test-match-seq lst) expected)
        '(:plus 10 20) 30
         [:plus 10 20] 30
        '(:list 1 2 3) '(1 2 3))))
  (testing "error conditions"
    (testing "A first argument is not a seq"
      (is (=       (match-seq '(:x 1 2) [:x y z] (+ y z)) 3))
      (is (thrown? Exception
                   (match-seq       12  [:x y z] (+ y z)))))
    (testing "A count of a body is not even"
      (is (thrown? IllegalArgumentException
                   (eval '(match-seq '(1 2)
                                     [x y] (+ x y)
                                     [x y z] ;; needs a expression
                                     )))))))
