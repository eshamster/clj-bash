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
(defn- unite-two-str-body [left right]
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

(defn unite-str-body [left & rest-str-body-lst]
  (loop [head left
         rest-lst rest-str-body-lst]
    (if-not (empty? rest-lst)
      (recur (unite-two-str-body head (first rest-lst))
             (rest rest-lst))
      head)))

;; Ex. "[" ("str") "]" -> ("[str]")
;; Ex. "[" ("do" ("a" "b") "done") "]" -> ("[do" ("a" "b") "done]")
(defn wrap-str-body [left body right]
  (when-not (and (string? left)
                 (string? right))
    (throw (IllegalArgumentException. "left and right should be strings")))
  (when-not (str-body? body)
    (throw (IllegalArgumentException. "a body should be a list of str-body")))
  (unite-str-body left body right))

(defn- join-list [delimiter lst]
  (loop [result '()
         head (first lst)
         rest-lst (rest lst)]
    (let [consed (cons head result)]
      (if-not (empty? rest-lst)
        (recur (cons delimiter consed)
               (first rest-lst)
               (rest rest-lst))
        (reverse consed)))))

(defn join-str-body [delimiter str-body-lst]
  (when-not (string? delimiter)
    (throw (IllegalArgumentException. "a delimiter should be a string")))
  (when-not (every? str-body? str-body-lst))
  (if-not (empty? str-body-lst)
    (apply unite-str-body (join-list delimiter str-body-lst))
    ""))

;; Ex. "a" ("b")           -> ("a" "b")
;;     ("a" "b") ("c" "d") -> ("a" "b" "c" "d")
;;     ("a" "b") ("c" ("d" "e")) -> ("a" "b" "c" ("d" "e"))
;;     ("a" ("b") "c") ("d" "e") -> ("a" ("b") "c" "d" "e")
;; Note: This is public for only test
(defn cons-str-body [left str-body-lst]
  (when-not (str-body? left)
    (throw (IllegalArgumentException. (format "a left should be a str-body: " left))))
  (when-not (and (seq? str-body-lst)
                 (every? str-body? str-body-lst))
    (throw (IllegalArgumentException. (format "a str-body-lst should be a list of str-body: " str-body-lst))))
  (if (string? left)
    (cons left str-body-lst)
    (concat left str-body-lst)))

;; Ex. (("a" ("ab") "b") "str1" ("c" "d") "str2")
;;     -> ("a" ("ab") "b" "str1" "c" "d" "str2")
(defn construct-str-body-lst [src-lst fn-make-str-body]
  (loop [target src-lst
         result '()]
    (if-not (empty? target)
      (recur (rest target)
             (let [line (fn-make-str-body (first target)) ]
               (if (str-body? line)
                 (if (string? line)
                   (cons-str-body line result)
                   (cons-str-body (reverse line) result))
                 (throw (Exception. (format "str-line should be return a str-body: %s" line))))))
      (reverse result))))
