(ns mysystem.ui
  (:require
   [system]
   [pg]
   [pg.repo]
   [http]
   [mysystem.ui.helpers :as h]
   [mysystem.ui.notebooks]
   [clojure.java.io]
   [cheshire.core]))

(system/defmanifest {:description "UI"})

(defn index-html [context {{q :search} :query-params :as request}]
  (let [rows [{:name "john"} {:name "jhn"}]]
    (h/layout
     context request
     {:content
      [:div.p-6
       [:script (slurp (clojure.java.io/resource "selection.js"))]
       [:div.flex.space-x-2.divide-x
        (for [r rows]
          [:div.border
           (for [[k v] r]
             [:div.p-2.cursor-pointer {"hx-on:click" "select(event)"
                                       :data-value v
                                       :data-field k} [:b k] v])])
        [:form
         [:b "name"]
         [:script "function $(id) {return document.getElementById(id)}"]
         [:input#name.border.p-2     {:name "name" :onkeyup "$('fullname').value = event.target.value"}]
         [:input#family.border.p-2   {:name "family" :onkeyup "$('fullname').value = event.target.value"}]
         [:input#fullname.border.p-2 {:name "full name"}]

         ]]]})))

(defn patients-html [context {{q :search} :query-params :as request}]
  (let [pts (pg.repo/select context {:table "patient"})]
    (h/layout
     context request
     {:content
      [:div.p-4 "Patients"
       (for [pt pts]
         [:div (pr-str pt)])]})))

(defn encounters-html [context {{q :search} :query-params :as request}]
  (let [pts (pg.repo/select context {:table "patient"})]
    (h/layout
     context request
     {:content
      [:div.p-4 "Encounters"
       (for [pt pts]
         [:div (pr-str pt)])]})))

(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path "/" :fn #'index-html})
  (http/register-endpoint context {:method :get :path "/ui/patients" :fn #'patients-html})
  (http/register-endpoint context {:method :get :path "/ui/encounters" :fn #'encounters-html})
  (mysystem.ui.notebooks/mount-routes context)
  :ok)

(system/defstart [context _]
  (mount-routes context)
  {})


(comment
  (def context mysystem/context)
  (mount-routes context)

  (pg/execute! context {:sql "select * from patient"})

  (pg.repo/upsert context {:table "patient" :resource {:family "Doe" :given "John"}})




  )
