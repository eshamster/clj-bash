(ns clj-bash.parser)

(use '[clojure.string :only [join]]
     '[clj-bash.cb-macro :only [cb-macro? cb-macroexpand]])

(declare parse-line)
(declare parse-main)
(declare parse-set-value)

(defn- add-prefix [prefix rest]
  (concat (list prefix) rest))

(defn- cover-by-eval [seq]
  (add-prefix :eval seq))

(defn- parse-string [string]
  (add-prefix :string (list string)))

;; TODO: process each element of array for such a case as [0 (:expr 1 + 2) 1]
(defn- parse-arg [arg]
  (cond
    (list? arg) (cover-by-eval (parse-line arg))
    (vector? arg) (add-prefix :array arg)
    (number? arg) (str arg)
    (string? arg) (parse-string arg)
    (keyword? arg) (name arg)
    (symbol? arg) (name arg)
    :else (throw (Exception.
                  (format "not recognized arg type: %s (%s)"
                          arg (type arg))))))

(defn- parse-command [command rest]
  (add-prefix :command (concat (list command)
                               (map parse-arg rest))))

;; --- cond --- ;;

(defn- check-cond-body [body]
  (if-not (even? (count body))
    (throw (IllegalArgumentException. "cond requires an even number of forms"))))

(defn- process-cond-line [now-result condition expr]
  (when-not (seq? expr)
    (throw (IllegalArgumentException.
            (str "The process of cond should be a list: " expr))))
  (let [parsed-expr (list (parse-line expr))]
    (if (= condition :else)
      (add-prefix :else parsed-expr)
      (let [prefix (if (nil? now-result) :if :elif)
            test-clause (add-prefix :test
                                    (map parse-arg condition))]
        (add-prefix prefix (cons test-clause parsed-expr))))))

(defn- parse-cond [body]
  (check-cond-body body)
  (let [grouped-body (partition 2 body)]
    (loop [result nil
           line (first grouped-body)
           rest-body (rest grouped-body)]
      (if-not (nil? line)
        (recur (cons (process-cond-line result (first line) (second line))
                     result)
               (first rest-body)
               (rest rest-body))
        (add-prefix :cond (reverse result))))))

;; --- defn --- ;;

;; Note: For generality, it is probably better to move
;; this parsing process to clj-bash.str
(defn- parse-defn-args [args]
  (loop [rest-args args
         arg-index 1
         result nil]
    (if-not (empty? rest-args)
      (let [var-declare (add-prefix
                         :local
                         (parse-set-value (first rest-args)
                                          (format "$%d" arg-index)))]
        (recur (rest rest-args)
               (+ arg-index 1)
               (cons var-declare result)))
      (reverse result))))

(defn- parse-defn [name args body]
  (add-prefix :function
              (concat (list name)
                      (parse-defn-args args)
                      (parse-main body))))

;; --- --- ;;

(defn- parse-do [exprs]
  (add-prefix :do (map parse-line exprs)))

;; --- for --- ;;

(defn- parse-for [var array rest]
  (add-prefix
   :for (concat (list var (parse-arg array))
                (map parse-line rest))))

(defn- parse-set-value [name value]
  (add-prefix :set (list name (parse-arg value))))

(defn- parse-pipe [exprs]
  (add-prefix :pipe (map parse-line exprs)))

(defn- try-cb-macro [line]
  (if (cb-macro? (first line))
    (cb-macroexpand line)
    (throw (Exception. (str (first line) " is not a cb-macro")))))

;; TODO: refactor by using match
(defn- parse-line [line]
  (let [kind (first line)
        args (rest line)]
    (if (keyword? kind)
      (parse-command (name kind) args)
      (case (name kind)
        "cond" (parse-cond args)
        "defn" (parse-defn (first args) (second args) (nthrest args 2))
        "do" (parse-do args)
        "for" (parse-for (first args) (second args) (nthrest args 2))
        "set" (parse-set-value (first args) (second args))
        "->" (parse-pipe args)
        (try (parse-line (try-cb-macro line))
             (catch Exception e
               (str (name kind) " is not a reserved keyword or a cb-macro")))))))

(defn parse-main [body-lst]
  (map parse-line body-lst))
