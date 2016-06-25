(ns clj-bash.str
  (:require [clj-bash.str-body :refer :all])
  (:require [clj-bash.utils :refer :all]))

(use '[clojure.string :only [join]])

(declare str-main)
(declare str-line)
(declare str-element)
(declare str-set-value)

(defn- str-and [expr]
  (join-str-body " && " (map str-line expr)))

(defn- str-command [expr]
  (join-str-body " " (map str-element expr)))

(defn- str-condition-clause [condition]
  (if (= (first condition) :test)
    (wrap-str-body "[ "
                   (join-str-body " " (map str-element (rest condition)))
                   " ]")
    (str-line condition)))

(defn- str-cond [expr]
  (letfn [(str-if [head condition body]
            (list (wrap-str-body (str head " ")
                                 (str-condition-clause condition)
                                 "; then")
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
  (let [[& {:keys [var range body]}] expr]
    `(~(wrap-str-body (str "for " var " in ")
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

(defn- str-or [expr]
  (join-str-body " || " (map str-line expr)))

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

(defn- str-var [expr]
  (when-not (and (= (count expr) 1)
                 (string? (first expr)))
    (throw (IllegalArgumentException. (str "str-var takes a list of one string: " expr))))
  (let [name (first expr)]
    (wrap-str-body "${" name "}")))

(def heredoc-identifier "[[EOF]]")

(defn- str-with-heredoc [expr]
  (let [[& {:keys [body out heredoc]}] expr]
    (flatten
     (list (unite-str-body (str-line body)
                           "<<EOF"
                           (if (nil? out)
                             ""
                             (unite-str-body (if (= (first out) :out) ">" ">>")
                                             (str-element (second out)))))
           (map #(str heredoc-identifier %) heredoc)
           (str heredoc-identifier "EOF")))))

(defn- str-while [expr]
  (let [[condition & body] expr]
    (list (wrap-str-body "while "
                         (str-condition-clause condition)
                         "; do")
          (str-main body)
          "done")))

;; --- basic parsers --- ;;

(defn- str-element [element]
  (check-return
   [str-body? "A return value of str-element should be a str-body"]
   (cond
     (list? element) (str-line element)
     (string? element) element
     (number? element) (str element)
     (instance? clojure.lang.LazySeq element) (str-line element)
     :else (throw (Exception.
                   (format "not recognized element type: %s (%s)"
                           element (type element)))))))

(defn- str-line [line]
  (check-return
   [str-body? "A return value of str-line should be a str-body"]
   (match-seq line
              [:and & expr] (str-and expr)
              [:array & expr] (str-array expr)
              [:command & expr] (str-command expr)
              [:cond & expr] (str-cond expr)
              [:do & expr] (str-do expr)
              [:eval & expr] (str-eval expr)
              [:for & expr] (str-for expr)
              [:function & expr] (str-function expr)
              [:local & expr] (str-local expr)
              [:or & expr] (str-or expr)
              [:pipe & expr] (str-pipe expr)
              [:set & expr] (str-set-value expr)
              [:string & expr] (str-string expr)
              [:var & expr] (str-var expr)
              [:with-heredoc & expr] (str-with-heredoc expr)
              [:while & expr] (str-while expr))))

(defn str-main [parsed-tree]
  (check-return [#(and (seq? %) (str-body? %))
                 "A return value of str-main should be a sequence && str-body"]
                (construct-str-body-lst parsed-tree str-line)))
