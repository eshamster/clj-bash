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
