(ns clj-bash.core
  (:require [clj-bash.default-cb-macros :refer :all]))

(use '[clj-bash.parser :only [parse-main]])
(use '[clj-bash.str :only [str-main]])
(use '[clj-bash.pprint :only [pprint-tree]])
(use 'clojure.java.io)

(def header "#!/bin/bash

set -eu

")

(defmacro compile-bash [& body]
  `(str header
        (with-out-str
          (pprint-tree
           (str-main
            (parse-main '~body))))))
