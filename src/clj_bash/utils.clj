(ns clj-bash.utils)

(use '[clojure.core.match :only [match]])

"This is a lapper for clojure.core.match/math to match a sequence
Example.
(match-seq target
  [:abc & expr] (some-func expr)
  [:bcd a b] (some-func2 a b))"
(defmacro match-seq [target & body]
  (if-not (even? (count body))
    (throw (IllegalArgumentException. "a body of match-seq requires an even number of forms")))
  `(match [~target]
          ~@(apply concat
                   (map (fn [pair#]
                          `([(~(first pair#) :seq)] ~(second pair#)))
                        (partition 2 body)))))
