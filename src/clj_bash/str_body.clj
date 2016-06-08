(ns clj-bash.str-body)

;; str-body is defined by this checking function
;; Ex. "test", ("test"), ("a" "b" "c"), ("a" ("b" "c") "d")
(defn str-body? [elem]
  (or (string? elem)
      (and (seq? elem)
           (string? (first elem))
           (string? (last elem)))))

;; Ex. "[" ("str") "]" -> ("[str]")
;; Ex. "[" ("do" ("a" "b") "done") "]" -> ("[do" ("a" "b") "done]")
;; TODO: Fix because the definition of str-body is changed to accept a string 
(defn wrap-str-body [left body right]
  (when-not (and (string? left)
                 (string? right))
    (throw (IllegalArgumentException. "left and right should be strings")))
  (when-not (str-body? body)
    (throw (IllegalArgumentException.
            "body should be a list and first and last of it should be strings")))
  (if (= (count body) 1)
    (list (str left (first body) right))
    `(~(str left (first body))
      ~@(rest (butlast body))
      ~(str (last body) right))))
