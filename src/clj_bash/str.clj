(ns clj-bash.str)

(use '[clojure.string :only [join]])
(use '[clojure.core.match :only [match]])

(declare str-main)
(declare str-line)
(declare str-element)
(declare str-set-value)

;; TODO: make a utils namespace and move this macro there
"Example.
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

;; Ex. "[" ("str") "]" -> "[str]"
;; Ex. "[" ("do" ("a" "b") "done") "]" -> ("[do" ("a" "b") "done]")
(defn wrap-str-body [left body right]
  (when-not (and (string? left)
                 (string? right))
    (throw (Exception. "left and right should be strings")))
  (when-not (and (seq? body)
                 (string? (first body))
                 (string? (last body)))
    (throw (Exception. "body should be a list and first and last of it should be strings")))
  (if (= (count body) 1)
    (list (str left (first body) right))
    `(~(str left (first body))
      ~@(rest (butlast body))
      ~(str (last body) right))))

(defn- str-command [expr]
  (list (join " " (mapcat str-element expr))))

(defn- str-cond [expr]
  (letfn [(str-if [head condition body]
            (list (str head
                       " [ "
                       (join " " (mapcat str-element (rest condition)))
                       " ]; then")
                  (str-line body)))
          (str-else [body]
            (list "else"
                  (str-line body)))]
    (concat
     (mapcat #(match-seq
               %
               [:if condition body] (str-if "if" condition body)
               [:elif condition body] (str-if "elif" condition body)
               [:else body] (str-else body))
             expr)
     '("fi"))))

;; TODO: test such a case as "a=$(for i in $(seq 0 2) ; do echo ${i}; done)"
(defn- str-eval [expr]
  (wrap-str-body "$(" (str-line expr) ")"))

(defn- str-for [expr]
  `(~@(wrap-str-body (str "for " (first expr) " in ")
                     (str-line (second expr))
                     "; do")
    ~(str-main (nthrest expr 2))
    "done"))

(defn- str-function [expr]
  (list (str "function " (first expr) "() {")
        (str-main (rest expr))
        "}"))

;; TODO: needs recur for such a case as (:array 0 (:eval :expr "0 + 5") 2)
(defn- str-array [expr]
  (list (join " " (mapcat str-element expr))))

(defn- str-local [expr]
  (when (not= (first expr) :set)
    (throw (Exception. (str "Invalid str-local (the first of expr should be the set-value clause): " expr))))
  (wrap-str-body "local " (str-set-value (rest expr)) ""))


;; TODO: Fix: this cannot correctory process nested form
(defn- str-pipe [expr]
  (list (join " | " (mapcat str-line expr))))

(defn- str-set-value [expr]
  (when (not= (count expr) 2)
    (throw (Exception. (str "Invalid set-value: " expr))))
  (wrap-str-body (str (first expr) "=")
                 (str-element (second expr))
                 ""))

(defn- str-string [expr]
  (when (not= (count expr) 1)
    (throw (Exception. (str "Invalid string: " expr))))
  (wrap-str-body "\"" expr "\""))

(defn- str-element [element]
  (cond
    (list? element) (str-line element)
    (string? element) (list element)
    (number? element) (list (str element))
    (instance? clojure.lang.LazySeq element) (str-line element)
    :else (throw (Exception.
                  (format "not recognized element type: %s (%s)"
                          element (type element))))))

(defn- str-line [line]
  (match-seq line
             [:array & expr] (str-array expr)
             [:command & expr] (str-command expr)
             [:cond & expr] (str-cond expr)
             [:eval & expr] (str-eval expr)
             [:for & expr] (str-for expr)
             [:function & expr] (str-function expr)
             [:local & expr] (str-local expr)
             [:pipe & expr] (str-pipe expr)
             [:set & expr] (str-set-value expr)
             [:string & expr] (str-string expr)))

(defn str-main [tree]
  (loop [target tree
         result nil]
    (if-not (empty? target)
      (recur (rest target)
             (let [line (str-line (first target)) ]
               (if (seq? line)
                 (concat (reverse line) result)
                 (throw (Exception. (format "str-line should be return a seq: %s" line))))))
      (reverse result))))
