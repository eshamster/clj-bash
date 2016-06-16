(ns clj-bash.str
  (:require [clj-bash.str-body :refer :all]))

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

(defn- str-command [expr]
  (join-str-body " " (map str-element expr)))

(defn- str-cond [expr]
  (letfn [(str-if [head condition body]
            (list (str head
                       " [ "
                       (join-str-body " " (map str-element (rest condition)))
                       " ]; then")
                  (str-main (list body))))
          (str-else [body]
            (list "else"
                  (str-main (list body))))]
    (concat
     (mapcat #(match-seq
               %
               [:if condition body] (str-if "if" condition body)
               [:elif condition body] (str-if "elif" condition body)
               [:else body] (str-else body))
             expr)
     '("fi"))))

(defn- str-do [expr]
  (str-main expr))

;; TODO: test such a case as "a=$(for i in $(seq 0 2) ; do echo ${i}; done)"
(defn- str-eval [expr]
  (wrap-str-body "$(" (str-line expr) ")"))

(defn- str-for [expr]
  ;; TODO: should consider if the wrap-str-body return a list
  (let [[variable range & body] expr]
    `(~(wrap-str-body (str "for " variable " in ")
                      (str-line range)
                      "; do")
      ~(str-main body)
      "done")))

(defn- str-function [expr]
  (list (str "function " (first expr) "() {")
        (str-main (rest expr))
        "}"))

(defn- str-array [expr]
  (join-str-body " " (map str-element expr)))

(defn- str-local [expr]
  (when (not= (first expr) :set)
    (throw (Exception. (str "Invalid str-local (the first of expr should be the set-value clause): " expr))))
  (wrap-str-body "local " (str-set-value (rest expr)) ""))

(defn- str-pipe [expr]
  (join-str-body " | " (map str-line expr)))

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
    (string? element) element
    (number? element) (str element)
    (instance? clojure.lang.LazySeq element) (str-line element)
    :else (throw (Exception.
                  (format "not recognized element type: %s (%s)"
                          element (type element))))))

(defn- str-line [line]
  (match-seq line
             [:array & expr] (str-array expr)
             [:command & expr] (str-command expr)
             [:cond & expr] (str-cond expr)
             [:do & expr] (str-do expr)
             [:eval & expr] (str-eval expr)
             [:for & expr] (str-for expr)
             [:function & expr] (str-function expr)
             [:local & expr] (str-local expr)
             [:pipe & expr] (str-pipe expr)
             [:set & expr] (str-set-value expr)
             [:string & expr] (str-string expr)))

(defn str-main [parsed-tree]
  (construct-str-body-lst parsed-tree str-line))
