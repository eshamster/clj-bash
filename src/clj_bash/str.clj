(ns clj-bash.str)

(use '[clojure.string :only [join]])
(use '[clojure.core.match :only [match]])

(declare str-main)
(declare str-line)
(declare str-element)

(defn- str-command [expr]
  (join " " (map str-element expr)))

(defn- str-eval [expr]
  (str "$(" (str-line expr) ")"))

(defn- str-for [expr]
  (list (str "for " (first expr) " in " (str-line (second expr)) "; do")
        (str-main (nthrest expr 2))
        "done"))

;; TODO: needs recur for such a case as (:array 0 (:eval :expr "0 + 5") 2)
(defn- str-array [expr]
  (join " " (map str-element expr)))

(defn- str-pipe [expr]
  (join " | " (map str-line expr)))

(defn- str-string [expr]
  (when (not (and (= (count expr) 1)
                  (string? (first expr))))
    (throw (Exception. (str "Invalid string: " expr))))
  (str "\"" (first expr) "\""))

(defn- str-element [element]
  (cond
    (list? element) (str-line element)
    (string? element) element
    (number? element) (str element)
    (instance? clojure.lang.LazySeq element) (str-line element)
    :else (throw (Exception.
                  (format "not recognized element type: %s (%s)"
                          element (type element))))))

(defn- str-line [line]
  (match [line]
         [([:array & expr] :seq)] (str-array expr)
         [([:command & expr] :seq)] (str-command expr)
         [([:eval & expr] :seq)] (str-eval expr)
         [([:for & expr] :seq)] (str-for expr)
         [([:pipe & expr] :seq)] (str-pipe expr)
         [([:string & expr] :seq)] (str-string expr)))

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
