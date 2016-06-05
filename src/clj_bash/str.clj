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

(defn- str-command [expr]
  (join " " (map str-element expr)))

(defn- str-cond [expr]
  (letfn [(str-if [head condition body]
            (list (str head
                       " [ "
                       (join " " (map str-element (rest condition)))
                       " ]; then")
                  (list (str-line body))))
          (str-else [body]
            (list "else"
                  (list (str-line body))))]
    (concat
     (mapcat #(match-seq
               %
               [:if condition body] (str-if "if" condition body)
               [:elif condition body] (str-if "elif" condition body)
               [:else body] (str-else body))
             expr)
     '("fi"))))

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
  (when (not= (first expr) :set)
    (throw (Exception. (str "Invalid str-local (the first of expr should be the set-value clause): " expr))))
  (str "local " (str-set-value (rest expr))))

(defn- str-pipe [expr]
  (join " | " (map str-line expr)))

(defn- str-set-value [expr]
  (when (not= (count expr) 2)
    (throw (Exception. (str "Invalid set-value: " expr))))
  (str (first expr) "=" (str-element (second expr))))

(defn- str-string [expr]
  (when (not= (count expr) 1)
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
                 (cons line result))))
      (reverse result))))
