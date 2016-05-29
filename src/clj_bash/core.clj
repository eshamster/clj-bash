(ns clj-bash.core)

(use '[clj-bash.parser :only [parse-main]])
(use '[clj-bash.str :only [str-main]])
(use '[clj-bash.pprint :only [pprint-tree]])
(use 'clojure.java.io)

(def target '((:cd test)
              (:ls .)
              (for a (:ls ..)
                   (:echo $a)
                   (for i [0 1 2] (:echo $i)))))

(def header "#!/bin/bash

set -eu

")

(defmacro compile-bash [& body]
  `(str header
        (with-out-str
          (pprint-tree
           (str-main
            (parse-main '~body))))))

(defn -main [& args]
  (println (parse-main target))
  (println "--------")
  (println (format "%s" (str-main (parse-main target))))
  (println "--------")
  (print (eval `(compile-bash ~@target))))
