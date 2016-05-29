(ns clj-bash.parser)

(use '[clojure.string :only [join]])

(declare parse-main)

(defn- add-prefix [prefix rest]
  (concat (list prefix) rest))

;; TODO: enable to accept numbers in rest (Note: Ex. (name 1) raises error)
(defn- parse-command [command rest]
  (add-prefix :command (concat (list command) (map name rest))))

(defn- cover-by-eval [seq]
  (add-prefix :eval seq))

(defn- parse-array [seq]
  (if (vector? seq)
    (add-prefix :array seq)
    (cover-by-eval (parse-line seq))))

(defn- parse-for [var array rest]
  (add-prefix
   :for (concat (list var (parse-array array))
                (map parse-line rest))))

(defn- parse-line [line]
  (let [kind (first line)
        args (rest line)]
    (if (keyword? kind)
      (parse-command (name kind) args)
      (case (name kind)
        "for" (parse-for (first args) (second args) (nthrest args 2))))))

(defn parse-main [body]
  (map parse-line body))
