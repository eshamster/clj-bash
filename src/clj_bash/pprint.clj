(ns clj-bash.pprint)

(use '[clj-bash.str :only [str-main]])

(def ^:dynamic num-indent 2)

(defn- print-line [depth str-line]
  (str (apply str (repeat (* depth num-indent) " "))
       str-line))

(defn pprint-tree [tree]
  (doseq [line (str-main tree)]
    (println line)))
