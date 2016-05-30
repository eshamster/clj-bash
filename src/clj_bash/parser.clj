(ns clj-bash.parser)

(use '[clojure.string :only [join]])

(declare parse-line)

(defn- add-prefix [prefix rest]
  (concat (list prefix) rest))

(defn- cover-by-eval [seq]
  (add-prefix :eval seq))

(defn- parse-arg [arg]
  (cond
    (list? arg) (cover-by-eval (parse-line arg))
    (vector? arg) (add-prefix :array arg)
    (number? arg) (str arg)
    (string? arg) (str arg)
    (keyword? arg) (name arg)
    (symbol? arg) (name arg)
    :else (throw (Exception.
                  (format "not recognized arg type: %s (%s)"
                          arg (type arg))))))

(defn- parse-command [command rest]
  (add-prefix :command (concat (list command)
                               (map parse-arg rest))))

(defn- parse-for [var array rest]
  (add-prefix
   :for (concat (list var (parse-arg array))
                (map parse-line rest))))

(defn- parse-pipe [exprs]
  `(:pipe ~@(map parse-line exprs)))

(defn- parse-line [line]
  (let [kind (first line)
        args (rest line)]
    (if (keyword? kind)
      (parse-command (name kind) args)
      (case (name kind)
        "for" (parse-for (first args) (second args) (nthrest args 2))
        "->" (parse-pipe args)))))

(defn parse-main [body-lst]
  (map parse-line body-lst))
