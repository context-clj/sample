(ns mysystem.ui
  (:require
   [system]
   [pg]
   [pg.repo]
   [http]
   [mysystem.ui.helpers :as h]
   [mysystem.ui.tailwindui :as t]
   [mysystem.ui.notebooks]
   [mysystem.ui.sdc]
   [mysystem.ui.htmx]
   [mysystem.ui.calendar]
   [clojure.java.io]
   [clojure.string :as str]
   [mysystem.toolkit]
   [cheshire.core]))

(system/defmanifest {:description "UI"})

(defn index-html [context {{q :search} :query-params :as request}]
  (h/l context request
       [:div.p-6 (t/h2 "Atomic EHR")
        (t/btn! "Save")
        ]))


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



(defn select [attrs opts & [value]]
  [:select.px-2.py-2.border.rounded
   attrs
   (for [{:keys [id name]} opts]
     [:option {:value id :selected (= value id)} (or name id)])])


(defn patient-details [context request params]
  (let [res (pg/execute! context {:sql "select * from information_schema.tables limit 10"})]
    [:pre (pr-str res)]))

(comment
  (patient-details context {} {})
  (preview-patient context {} {:id 1})

  )

(defn preview-patient [context _request {id :id}]
  (let [pt (pg.repo/read context {:table "patient" :match {:id id}})]
    [:div#preview.p-6.border.rounded.bg-gray-50
     (h/h1 [:a.text-sky-600 {:href (str "/ui/patients/" id)} (:given pt) " " (:family pt)])
     [:pre (cheshire.core/generate-string pt {:pretty true})]
     [:button {:hx-get (h/rpc #'patient-details)} "Load more"]
     ]))

(defn patients-search [context request {q :search g :gender :as params}]
  (let [pts (pg.repo/select
             context {:table "patient"
                      :where {:name   (when (and q (not (str/blank? q))) [:ilike
                                                                          [:|| :family " " :given]
                                                                          [:pg/param (str "%" q "%")]])
                              :gender (when (and g (not (str/blank? g))) [:= :gender g])}})]
    [:div#table.mt-4
     [:div.text-xs.text-gray-400.py-1  (pr-str (dissoc params :method))]
     (h/table [:id :name :birthdate :gender] pts
              (fn [p]
                [(:id p)
                 [:a.text-sky-600.cursor-pointer
                  {:hx-get (h/rpc #'preview-patient {:id (:id p)})
                   :hx-target "#preview"
                   :hx-swap "outerHTML"}
                  (str (:given p) " " (:family p))]
                 (str (:birthdate p))
                 (:gender p)]))]))

(defn patients-html [context {{q :search :as params} :query-params :as request}]
  (h/layout
   context request
   {:content
    [:div.p-4.flex.items-top
     [:style " .spinner {display: none;} .htmx-request .spinner {display: inline;} "]
     [:div.flex-1.px-4
      [:form.flex.space-x-4.items-center
       {:hx-get (h/rpc #'patients-search)
        :hx-target "#table"
        :hx-trigger "keyup delay:500ms, change"
        :hx-ext "push-url-params"
        :hx-indicator ".spinner"}
       [:div.font-bold "Patients"]
       [:input.border.px-4.py-2
        {:name "search"
         :autofocus true
         :value q
         :placeholder "name"}]
       (select {:name "gender"} [{:id "" :name "any"} {:id "male"} {:id "female"}] (:gender params))
       (spinner)]
      (patients-search context request (:query-params request))]
     [:div#preview]]}))

(defn patient-show-html [context {{id :id} :route-params :as request}]
  (let [pt (pg.repo/read context {:table "patient" :match {:id id}})]
    (h/layout
     context request
     {:content
      [:div.p-6
       (h/h1 [:a.text-sky-600 {:href (str "/ui/patients/" id)} (:given pt) " " (:family pt)])
       [:div.border-b]
       [:pre.mt-4 (cheshire.core/generate-string pt {:pretty true})]]})))

(defn encounters-html [context {{q :search :as params} :query-params :as request}]
  (h/layout
   context request
   {:content
    [:div.p-4.flex.items-top
     [:style " .spinner {display: none;} .htmx-request .spinner {display: inline;} "]
     (h/h1 "TBD")]}))



(defn get-fn [fn-str]
  (let [[ns-name _fn-name] (str/split fn-str #"/")]
    (require [(symbol ns-name)])
    (resolve (symbol fn-str))))

(defn ui-rpc [context request]
  (let [m (:method (:query-params request))
        f (get-fn m)
        _ (println :fn f)
        res (f context request (:query-params request))]
    (if (vector? res)
      (h/fragment res)
      res)))

(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path "/" :fn #'index-html})
  (http/register-endpoint context {:method :get :path "/ui/patients" :fn #'patients-html})
  (http/register-endpoint context {:method :get :path "/ui/patients/:id" :fn #'patient-show-html})
  (http/register-endpoint context {:method :get :path "/ui/encounters" :fn #'encounters-html})
  (http/register-endpoint context {:method :post :path  "/ui/rpc"  :fn #'ui-rpc})
  (http/register-endpoint context {:method :get :path  "/ui/rpc"  :fn #'ui-rpc})
  (http/register-endpoint context {:method :delete :path  "/ui/rpc"  :fn #'ui-rpc})
  (http/register-endpoint context {:method :put :path  "/ui/rpc"  :fn #'ui-rpc})

  (http/register-endpoint context {:method :get :path "/ui/kit" :fn #'mysystem.toolkit/index})

  (mysystem.ui.notebooks/mount-routes context)
  (mysystem.ui.tailwindui/mount-routes context)
  (mysystem.ui.sdc/mount-routes context)
  (mysystem.ui.htmx/mount-routes context)
  :ok)

(system/defstart [context _]
  (mount-routes context)
  {})


(comment
  (def context mysystem/context)
  (mount-routes context)

  (pg/execute! context {:sql "select * from patient"})

  (pg.repo/upsert context {:table "patient" :resource {:family "Ivan" :given "Ivanov" :gender "male"}})
  (pg.repo/upsert context {:table "patient" :resource {:family "Olivia" :given "Smith" :gender "female"}})
  (pg.repo/upsert context {:table "patient" :resource {:family "Potter" :given "Garry" :gender "male" :birthdate "1990-01-01"}})

  (pg.repo/upsert context {:table "patient" :resource {:family "Olga" :given "Plach" :gender "female" :birthdate "1970-02-02"}})




  )
