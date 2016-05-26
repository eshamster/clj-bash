(ns clj-bash.core)

(use '[clj-bash.parser :only [do-dsl]])
(use '[clj-bash.pprint :only [pprint-tree]])

(defn -main [& args]
  (println (do-dsl
            (:cd test)
            (:ls .)))
  (println "--------")
  (pprint-tree (do-dsl
                (:cd test)
                (:ls .))))
