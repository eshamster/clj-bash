(ns clj-bash.core)

(use '[clj-bash.parser :only [do-dsl]])
(use '[clj-bash.str :only [str-main]])
(use '[clj-bash.pprint :only [pprint-tree]])

(def target (do-dsl (:cd test)
                    (:ls .)
                    (for a (:ls ..) (:echo $a))
                    (for i [0 1 2] (:echo $i))))

(defn -main [& args]
  (println target)
  (println "--------")
  (println (format "%s" (str-main target)))
  (println "--------")
  (pprint-tree (str-main target)))
