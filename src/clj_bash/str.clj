(ns clj-bash.str)

(use '[clojure.string :only [join]])
(use '[clojure.core.match :only [match]])

(declare str-main)
(declare str-line)
(declare str-element)
(declare str-set-value)

(defn- str-command [expr]
  (join " " (map str-element expr)))

(defn- str-eval [expr]
  (str "$(" (str-line expr) ")"))

(defn- str-for [expr]
  (list (str "for " (first expr) " in " (str-line (second expr)) "; do")
        (str-main (nthrest expr 2))
        "done"))

(defn- str-function [expr]
  (list (str "function " (first expr) "() {")
        (str-main (rest expr))
        "}"))

;; TODO: needs recur for such a case as (:array 0 (:eval :expr "0 + 5") 2)
(defn- str-array [expr]
  (join " " (map str-element expr)))

(defn- str-local [expr]
  (when (not (= (first expr) :set))
    (throw (Exception. (str "Invalid str-local (the first of expr should be the set-value clause): " expr))))
  (str "local " (str-set-value (rest expr))))

(defn- str-pipe [expr]
  (join " | " (map str-line expr)))

(defn- str-set-value [expr]
  (when (not (and (= (count expr) 2)))
    (throw (Exception. (str "Invalid set-value: " expr))))
  (str (first expr) "=" (str-element (second expr))))

(defn- str-string [expr]
  (when (not (and (= (count expr) 1)))
    (throw (Exception. (str "Invalid string: " expr))))
  (str "\"" (first expr) "\""))

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
  (match [line]
         [([:array & expr] :seq)] (str-array expr)
         [([:command & expr] :seq)] (str-command expr)
         [([:eval & expr] :seq)] (str-eval expr)
         [([:local & expr] :seq)] (str-local expr)
         [([:for & expr] :seq)] (str-for expr)
         [([:function & expr] :seq)] (str-function expr)
         [([:pipe & expr] :seq)] (str-pipe expr)
         [([:set & expr] :seq)] (str-set-value expr)
         [([:string & expr] :seq)] (str-string expr)))

(defn str-main [tree]
  (loop [target tree
         result nil]
    (if (not (empty? target))
      (recur (rest target)
             (let [line (str-line (first target)) ]
               (if (list? line)
                 (concat (reverse line) result)
                 (cons line result))))
      (reverse result))))
