(ns clj-bash.str)

(use '[clojure.string :only [join]])
(use '[clojure.core.match :only [match]])

(declare str-main)
(declare str-line)

(defn- str-command [expr]
  (join " " (map #(if (list? %)
                    (str-line %)
                    %)
                 expr)))

(defn- str-eval [expr]
  (str "$(" (str-line expr) ")"))

(defn- str-for [expr]
  (list (str "for " (first expr) " in " (str-line (second expr)) "; do")
        (str-main (nthrest expr 2))
        "done"))

;; TODO: needs recur for such a case as (:array 0 (:eval :expr "0 + 5") 2)
(defn- str-array [expr]
  (join " " expr))

(defn- str-line [line]
  (match [line]
         [([:array & expr] :seq)] (str-array expr)
         [([:command & expr] :seq)] (str-command expr)
         [([:eval & expr] :seq)] (str-eval expr)
         [([:for & expr] :seq)] (str-for expr)))

(defn str-main [tree]
  (loop [target tree
         result nil]
    (if (not (empty? target))
      (recur (rest target)
             (let [line (str-line (first target)) ]
               (if (list? line)
                 (concat (reverse line) result)
                 (cons line result))))
      (reverse result))))
