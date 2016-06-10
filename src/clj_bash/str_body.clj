(ns clj-bash.str-body)

;; str-body is defined by this checking function
;; Ex. "test", ("test"), ("a" "b" "c"), ("a" ("b" "c") "d")
(defn str-body? [elem]
  (or (string? elem)
      (and (seq? elem)
           (string? (first elem))
           (string? (last elem)))))

;; Ex. "test" "abc"                           -> "testabc"
;;     "test" ("abc" (0) "111")               -> ("testabc" (0) "111")
;;     ("abc" (0) "111") "test"               -> ("abc" (0) "111test")
;;     ("abc" (0) "111") ("test" '(1) "abcd") -> ("abc" (0) "111test" (quote (1)) "abcd")
(defn- concat-two-str-body [left right]
  (when-not (and (str-body? left) (str-body? right))
    (throw (IllegalArgumentException. "left and right should include only str-bodies")))
  (cond
    (and (string? left)
         (string? right))
    (str left right)
    (and (string? left)
         (not (string? right)))
    `(~(str left (first right)) ~@(rest right))
    (and (not (string? left))
         (string? right))
    `(~@(butlast left) ~(str (last left) right))
    (and (not (string? left))
         (not (string? right)))
    `(~@(butlast left) ~(str (last left) (first right)) ~@(rest right))))

(defn concat-str-body [left & rest-str-body-lst]
  (loop [head left
         rest-lst rest-str-body-lst]
    (if-not (empty? rest-lst)
      (recur (concat-two-str-body head (first rest-lst))
             (rest rest-lst))
      head)))

;; Ex. "[" ("str") "]" -> ("[str]")
;; Ex. "[" ("do" ("a" "b") "done") "]" -> ("[do" ("a" "b") "done]")
(defn wrap-str-body [left body right]
  (when-not (and (string? left)
                 (string? right))
    (throw (IllegalArgumentException. "left and right should be strings")))
  (when-not (str-body? body)
    (throw (IllegalArgumentException.
            "body should be a list and first and last of it should be strings")))
  (concat-str-body left body right))
