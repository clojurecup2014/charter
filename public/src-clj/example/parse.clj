(ns example.parse
  (:use [hiccup.element]
        [hiccup.util])
  (:require [instaparse.core :as insta]
            [clojure.edn]
            [hiccup
             [page :refer [html5]]
             [page :refer [include-js]]]
            ))

(def knit-and-yarnover
  (insta/parser
   "instructions = line (<whitespace> line)*
    line = linenumber <':'>? <whitespace>? stitch (<whitespace>? stitch)*
    linenumber = R <whitespace>? strnumber
    <R> = 'r' | 'R' | 'row' | 'Row'
    <stitch> = k | yo | k2tog
    k : <'k'> (<whitespace>? repeat)?
    yo : <'yo'>  (<whitespace> repeat)?
    k2tog : <'k2tog'> (<whitespace> repeat)?
    repeat: (number | once | twice | number <whitespace 'times'>)+
    once = 'once'
    twice = 'twice'
    number : digit+
    <strnumber> : digit+
    <digit> = #'[0-9]+'
    whitespace = #'\\s+'
"
   ))


;; thanks to @adrgrunt for the tip via twitter!
;; (defn repeat_stitches [[x [_ n]]]
;;   (if n (apply str (repeat n ( name x)) " ")
;;                (apply str (name x) " ")))

;; but I can't apply that right, so the below is used!

;; (defn repeat_k ([] "k")
;;   ([[_ n]] (apply str (repeat n "k"))))

(defn repeat_it ([char] char)
  ([char [_ n]] (apply str (repeat n char))))


(def once_twice_trnsfm
  {:word str
   :number (comp clojure.edn/read-string str)
   :once (fn [_] 1)
   :twice (fn [_] 2)
   :line (fn [label & sts] {:li (apply str label sts)})
   :linenumber (fn [r n] (apply str "Row " n ": "))
   :k (partial repeat_it "k ")
   :k2tog (partial repeat_it "k2tog ")
   :yo (partial repeat_it "yo ")
   ;; :instructions (fn [ & rows] ('(:H1 "Instructions: "
   ;;                               rows)))
   })



(def outputtxt
  (insta/transform once_twice_trnsfm
                   ( knit-and-yarnover "Row 1 k3 yo k k2tog k k2tog k yo k3
                                  R2: k
                                  R3: k3 k2tog k yo k yo twice k k2tog k3
                                  R4: k
                                  R3: k3 k2tog once k yo 3 times k yo twice k k2tog k")
                    )
   )


([:instructions [:line "Row 1" [:k [:repeat 3]] [:yo] [:k] [:k2tog] [:k] [:k2tog] [:k] [:yo] [:k [:repeat 3]]] [:line "Row 2" [:k]] [:line "Row 3" [:k [:repeat 3]] [:k2tog] [:k] [:yo] [:k] [:yo [:repeat 2]] [:k] [:k2tog] [:k [:repeat 3]]] [:line "Row 4" [:k]] [:line "Row 3" [:k [:repeat 3]] [:k2tog [:repeat 1]] [:k] [:yo [:repeat 3]] [:k] [:yo [:repeat 2]] [:k] [:k2tog] [:k]]])


(def tmap {:instructions identity
           :line str
           }
  )

(insta/transform tmap outputtxt)



(defn parse-page []
  (html5
    [:head
     [:title "Hello World"]
     ]
    [:body
     [:h1 "Parsing test"]
     [:input {:id "query" :type "text"}]
     [:button {:id "search"} "Search"]
     (unordered-list ["Row 1 k3 yo k k2tog k k2tog k yo k3"
          "R2: k"
          "R3: k3 k2tog k yo k yo twice k k2tog k3"
          "R4: k"
          "R3: k3 k2tog once k yo 3 times k yo twice k k2tog k"])
     [:p {:id "results"} (to-str (vec outputtxt))]
     (include-js "/js/main.js")
     ]))
