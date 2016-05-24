(ns clj-bash.core)

(use '[clj-bash.parser :only [do-dsl]])
(use '[clj-bash.pprint :only [pprint-tree]])

(defn -main [& args]
  (pprint-tree
   (do-dsl
    (:cd test)
    (:ls .))))
