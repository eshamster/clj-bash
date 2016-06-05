(ns clj-bash.default-cb-macros
  (:require [clj-bash.cb-macro :refer :all]))

;; TODO: implement
(def-cb-macro-as-default if [condition expr]
  `(:echo "This is only mock for test default-cb-macro-table"))
