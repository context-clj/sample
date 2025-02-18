(ns mysystem.ui
  (:require
   [system]
   [pg]
   [pg.repo]
   [http]
   [mysystem.ui.helpers :as h]
   [mysystem.ui.notebooks]
   [clojure.java.io]
   [clojure.string :as str]
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

(defn spinner []
   [:span.spinner.inline {:role "status"}
    [:svg.inline.w-4.h-4.me-2.text-gray-200.animate-spin.dark:text-gray-600.fill-blue-600 {:aria-hidden "true" :viewBox "0 0 100 101" :fill "none" :xmlns "http://www.w3.org/2000/svg"} [:path {:d "M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z" :fill "currentColor"}] [:path {:d "M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z" :fill "currentFill"}]] [:span.sr-only "Loading..."]])

(defn action-fn [context request params]
  (h/fragment
   [:button.border.p-2
    {:hx-post (h/rpc #'action-fn {:id "test"})
     :hx-swap "outerHTML"}
    "Press me; Last pressed at "
    (spinner)
    (str (java.time.Instant/now)) (pr-str params)]))

(defn encounters-html [context {{q :search} :query-params :as request}]
  (let [pts (pg.repo/select context {:table "patient"})]
    (h/layout
     context request
     {:content
      [:div.p-4 [:style " .spinner {display: none;} .htmx-request .spinner {display: inline;} "]
       [:div.flex.space-x-4.items-center
        [:div "Encounters"]
        [:button.border.p-2
         {:hx-post (h/rpc #'action-fn {:id "test"})
          :hx-indicator ".loader"
          :hx-swap "outerHTML"}
         "Press me "
         (spinner)]]
       (h/table [:id :ts :birthdate :gender :family :given] pts)]})))

(defn get-fn [fn-str]
  (let [[ns-name _fn-name] (str/split fn-str #"/")]
    (require [(symbol ns-name)])
    (resolve (symbol fn-str))))

(defn ui-rpc [context request]
  (let [m (:method (:query-params request))
        f (get-fn m)
        _ (println :fn f)
        res (f context request (:query-params request))]
    res))

(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path "/" :fn #'index-html})
  (http/register-endpoint context {:method :get :path "/ui/patients" :fn #'patients-html})
  (http/register-endpoint context {:method :get :path "/ui/encounters" :fn #'encounters-html})
  (http/register-endpoint context {:method :post :path  "/ui/rpc"  :fn #'ui-rpc})
  (http/register-endpoint context {:method :get :path  "/ui/rpc"  :fn #'ui-rpc})
  (http/register-endpoint context {:method :delete :path  "/ui/rpc"  :fn #'ui-rpc})
  (http/register-endpoint context {:method :put :path  "/ui/rpc"  :fn #'ui-rpc})
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
