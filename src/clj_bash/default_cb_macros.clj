(ns clj-bash.default-cb-macros
  (:require [clj-bash.cb-macro :refer :all]))

(def-cb-macro-as-default if [condition if-expr & [else-expr]]
  (if (nil? else-expr)
    `(cond ~condition ~if-expr)
    `(cond ~condition ~if-expr
           :else ~else-expr)))

(def-cb-macro-as-default when [condition & body]
  `(if ~condition
     (do ~@body)))

(defn- make-modifier [target operation step]
  `(set ~target (:expr (var ~target)
                       ~operation
                       ~(if (nil? step) 1 step))))

(def-cb-macro-as-default inc [target & [step]]
  (make-modifier target :+ step))

(def-cb-macro-as-default dec [target & [step]]
  (make-modifier target :- step))
