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
          ~@(mapcat #(let [[condition# expr#] %]
                       (if-not (= condition# :else)
                         `([(~condition# :seq)] ~expr#)
                         `(:else ~expr#)))
                    (partition 2 body))))

"Check if a return value of the body satisfies the predication"
(defmacro check-return [[predicate err-message] & body]
  `(let [result# (do ~@body)]
     (if (~predicate result#)
       result#
       (throw (Exception. (str ~err-message " (The return value: " result# ")"))))))

(defn ensure-no-unrecognized-keys [target recognized-key-lst]
  (loop [rest-map (cond (empty? target) nil
                        (map? target) target
                        (seq? target) (apply hash-map target)
                        :else (throw (IllegalArgumentException.
                                      (str "not recognized type: "
                                           target
                                           "(type: " (type target) ")"))))
         rest-recog-key-lst recognized-key-lst]
    (if-not (empty? rest-recog-key-lst)
      (recur (dissoc rest-map (first rest-recog-key-lst))
             (rest rest-recog-key-lst))
      (if (empty? rest-map)
        true
        (throw (IllegalArgumentException.
                (str (keys rest-map) " are not recognized keys")))))))
