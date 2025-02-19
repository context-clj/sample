(ns mysystem.ui.notebooks
  (:require
   [system]
   [pg]
   [pg.repo]
   [http]
   [mysystem.ui.helpers :as h]
   [clojure.java.io]
   [clojure.string :as str]
   [cheshire.core]))

;; TODO move cells logic into separate files

(defn index-html [context request]
  (h/layout
   context request
   {:content
    [:div.p-6
     (h/h1 [:span.flex-1 "Notebooks"]  [:a.border.px-2.py-1 {:href "/ui/notebooks/new"} "New"])
     [:div.mt-4
      (for [n (pg.repo/select context {:table "notebooks"})]
        [:a.text-sky-600.block.py-2.border-b.cursor-pointer.flex.space-x-4
         {:class "hover:bg-gray-50"
          :href (h/href ["ui" "notebooks" (:id n)])}
         [:div (:title n)]
         [:div.text-gray-600 (:description n)]])]]}))


(defn new-html [context request]
  (h/layout
   context request
   {:content
    [:div.p-6
     [:h1.text-lg.flex [:span.flex-1 "New"]]
     [:form#form.w-full {:method "POST"
                         :action "/ui/notebooks/create"}
      [:label.block "Title" [:br] [:input.border.p-2.w-full {:name "title"}]]
      [:label.block.mt-4 "Description" [:br] [:textarea.border.p-2.w-full {:name "description"}]]
      [:div.mt-4.py-4.border-t [:button.border.rounded.p-2 {:class "hover:bg-gray-100" :type "submit" } "Create"]]]]}))

(defn create-html [context request]
  (let [params (http/parse-params (slurp (:body request)))
        resource (pg.repo/insert context {:table "notebooks" :resource params})]
    {:status 302
     :headers {"HX-Location" (str "/ui/notebooks/" (:id resource))}}))

(defmulti cell-editor (fn [context request tp cid & [cell]] (keyword tp)))

(defmethod cell-editor :sql
  [_context _request _tp cid & [cell]]
  [:div
   [:textarea {:id (str "sql-" cid) :name "params.sql"}
    (get-in cell [:params :sql] "select * from patient limit 10")]
   (h/js "sqlEditor('sql-" cid "')")])

(defmethod cell-editor :md
  [_context _request _tp cid & [cell]]
  [:div
   [:textarea#md.mt-2.border.p-2.w-full
    {:id (str "md-" cid) :name "params.md"}
    (get-in cell [:params :md]) "# Title\n\n* item 1\n* item 2"]
   (h/js "mdEditor('md-" cid "')")])

(defn select [attrs opts & [value]]
  [:select.px-2.py-1.border.rounded
   attrs
   (for [{:keys [id name]} opts]
     [:option {:value id :selected (= value id)} (or name id)])])

(def methods-opts [{:id "GET"} {:id "POST"} {:id "PUT"}])
(def formats-opts [{:id "json"} {:id "yaml"} {:id "edn"}])

(defmethod cell-editor :rest
  [_context _request tp cid & [cell]]
  [:div
   [:div.flex.space-x-2
    (select {:name "params.method"} methods-opts (get-in cell [:params :method]))
    (select {:name "params.url"} [{:id "/Patient"} {:id "/Patient/:id"} {:id "Encounter"}] (get-in cell [:params :url]))
    (select {:name "params.format"} formats-opts (get-in cell [:params :format]))]
   [:textarea.mt-2.border.p-2.w-full {:name "params.body" :placeholder "body"}]])

(defmethod cell-editor :default
  [context request tp cid & [cell]]
  [:div.text-red-500.p-4 (str "Unknown " type)])

(defmulti eval-cell (fn [context request params] (keyword (:type params))))

(defmethod eval-cell :sql
  [context request params]
  (try
    {:status "ok"
     :result (pg/execute! context {:sql (get-in params [:params :sql])})}
    (catch Exception e
      {:status "error"
       :result (.getMessage e)})))

(defmethod eval-cell :md
  [context request params]
  params)

(defmethod eval-cell :rest
  [context request params]
  params)

(defmethod eval-cell :default
  [context request params]
  params)

(defmulti render-cell-result (fn [context request tp result] (keyword tp)))

(defmethod render-cell-result :sql
  [context request tp result]
  (if (= "ok" (:status result))
    [:div.bg-white.p-2 (h/table (keys (first (:result result))) (:result result))]
    [:div
     [:div.p-4.text-red-500 (str (:result result))]]))

(defmethod render-cell-result :md
  [context request tp result]
  [:pre (pr-str result)])

(defmethod render-cell-result :rest
  [context request tp result]
  [:pre (pr-str result)])

(defmethod render-cell-result :default
  [context request tp result]
  [:pre (pr-str result)])


(defn delete-cell [context request {cid :cid}]
  (pg/execute! context {:sql ["delete from notebook_cells where id = ?" cid]})
  {:status 200})

(declare render-cell)

(defn new-cell-fragment [context request {id :id}]
  (let [cell (pg.repo/upsert context {:table "notebook_cells" :resource {:id (java.util.UUID/randomUUID) :notebook id}})]
    (h/fragment (render-cell context request id cell))))

(def icon-class "w-[18px] h-[18px] text-gray-800 dark:text-white")

(def play-icon
  [:svg {:class icon-class :aria-hidden "true" :xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :fill "currentColor" :viewBox "0 0 24 24"} [:path {:fill-rule "evenodd" :d "M8.6 5.2A1 1 0 0 0 7 6v12a1 1 0 0 0 1.6.8l8-6a1 1 0 0 0 0-1.6l-8-6Z" :clip-rule "evenodd"}]])

(def move-icon
  [:svg {:class icon-class :aria-hidden "true" :xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :fill "none" :viewBox "0 0 24 24"} [:path {:stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "1.8" :d "M8 20V7m0 13-4-4m4 4 4-4m4-12v13m0-13 4 4m-4-4-4 4"}]])


(defn cell-editor-fragment [context {{type :type} :query-params :as request} params]
  (h/fragment (cell-editor context request type  (java.util.UUID/randomUUID))))

(defn process-params [params]
  (->> params
       (reduce (fn [acc [k v]]
                 (if (or (nil? v) (str/blank? v))
                   acc
                   (let [path (mapv keyword (str/split (name k) #"\."))]
                     (assoc-in acc path v)))) {})))

;; TODO params will come from rpc
(defn eval-cell-fragment [context request params]
  (let [params (process-params (http/parse-params (slurp (:body request))))]
    (try
      (let [result (eval-cell context request params)
            respond (render-cell-result context request (:type params) result)
            _cell (pg.repo/upsert context {:table "notebook_cells" :resource (assoc params :result result)})]
        (h/fragment [:div respond]))
      (catch Exception e
        (system/error context ::cell-eval-error (.getMessage e) )
        (h/fragment [:div.p-4.text-red-500
                     [:pre (cheshire.core/generate-string params)]
                     (.getMessage e)])))))

(defn render-cell [context request id & [cell]]
  (let [cid (or (:id cell) (java.util.UUID/randomUUID))
        form-id (str "form-" cid)
        res-id (str "cell-result-" cid)
        editor-id (str "cell-editor-" cid)]
    [:form.cell.w-full.mt-4.border.bg-gray-100
     {:id form-id
      :hx-post (h/rpc #'eval-cell-fragment {:id id})
      :data-cellId cid
      :hx-push-url "false"
      :hx-target (str "#" res-id)}
     (h/hiddens {:notebook id :id cid})
     [:div.flex.space-x-2.items-center.py-1.px-1
      [:div.text-lg.drag-handler.bg-gray-300.border.px-4.py-1.cursor-pointer move-icon]

      (select
       {:hx-get (h/rpc #'cell-editor-fragment)
        :hx-swap "innerHTML"
        :hx-target (str "#" editor-id)
        :name :type}
       [{:id "md"} {:id "sql"} {:id "rest"}]
       (get-in cell [:type]))

      (h/btn {:hx-post (h/rpc #'new-cell-fragment {:id id})
              :hx-target (str "#" form-id)
              "hx-on::after-request" "updateCellOrder()"
              :hx-swap "beforebegin"}
             "Add Cell Before")

      (h/btn {:hx-post (h/rpc #'new-cell-fragment {:id id})
              :hx-target (str "#" form-id)
              "hx-on::after-request" "updateCellOrder()"
              :hx-swap "afterend"}
             "Add Cell After")

      (h/btn {:hx-delete (h/rpc #'delete-cell {:cid cid})
              :hx-target (str "#" form-id)
              :hx-confirm "Delete cell?"
              :hx-swap "delete"}
             "Delete")

      [:div.flex-1]
      (h/btn {:class "hover:text-green-600" :type "submit" } play-icon)]
     [:div {:id editor-id}
      (cell-editor context request (or (:type cell) "md") cid cell)]
     [:div.border {:id res-id}
      (when cell (render-cell-result context request (:type cell) (:result cell)))]]))



(def notebooks-assets
  [:div
   [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/codemirror.min.js", :integrity "sha512-8RnEqURPUc5aqFEN04aQEiPlSAdE0jlFS/9iGgUyNtwFnSKCXhmB6ZTNl7LnDtDWKabJIASzXrzD0K+LYexU9g==", :crossorigin "anonymous", :referrerpolicy "no-referrer"}]
   [:script {:src "/static/sortable.js"}]
   [:script {:src "/static/notebooks.js"}]
   [:link {:rel "stylesheet", :href "https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/codemirror.min.css", :integrity "sha512-uf06llspW44/LZpHzHT6qBOIVODjWtv4MxCricRxkzvopAlSWnTf6hpZTFxuuZcuNE9CBQhqE0Seu1CoRk84nQ==", :crossorigin "anonymous", :referrerpolicy "no-referrer"}]
   [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/mode/sql/sql.min.js", :integrity "sha512-JOURLWZEM9blfKvYn1pKWvUZJeFwrkn77cQLJOS6M/7MVIRdPacZGNm2ij5xtDV/fpuhorOswIiJF3x/woe5fw==", :crossorigin "anonymous", :referrerpolicy "no-referrer"}]
   [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/mode/markdown/markdown.min.js", :integrity "sha512-DmMao0nRIbyDjbaHc8fNd3kxGsZj9PCU6Iu/CeidLQT9Py8nYVA5n0PqXYmvqNdU+lCiTHOM/4E7bM/G8BttJg==", :crossorigin "anonymous", :referrerpolicy "no-referrer"}]
   [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/codemirror/6.65.7/addon/hint/sql-hint.min.js", :integrity "sha512-O7YCIZwiyJYc9d/iPOSgEzhhlonTMGcmM1HmgYFffj5cGwVu2PLSzTaLvD9HSk8rSSf9rIpdhJPk8Yhu6wJBtA==", :crossorigin "anonymous", :referrerpolicy "no-referrer"}]
   (h/css ".CodeMirror { border: 1px solid #eee; height: auto;}")])

(defn show-html [context {{id :id} :route-params :as request}]
  (if-let [resource (pg.repo/read context {:table "notebooks" :match {:id id}})]
    (let [cells    (pg.repo/select context {:table "notebook_cells" :match {:notebook id} :order-by :position})]
      (h/layout
       context request
       {:content
        [:div
         notebooks-assets
         [:div.border-b.py-4
          [:h1.text-xl.flex [:div.flex-1 (:title resource)] [:span.text-sm.border.p-2 "Edit"]]
          [:p.mt-4.py-2 (:description resource)]]
         [:div#sortable
          (if (empty? cells)
            [:div#first-cell.mt-4.border.px-2.py-1.bg-white.cursor-pointer
             {:hx-post (h/rpc #'new-cell-fragment {:id id})
              :hx-target "#first-cell"
              :hx-swap "outerHTML"} "Add Cell"]
            (for [cell cells]
              (render-cell context request id cell)))]]}))
    (h/layout context request {:content [:div.p-6.text-red-600 "No notebook with id =" id]})))


(defn update-cell-order [context request]
  (->> (cheshire.core/parse-string (or (slurp (:body request)) "[]"))
       (map-indexed (fn [i cid]
                      (pg/execute! context {:sql ["update notebook_cells set position = ? where id = ?" i cid]})))
       (into []))
  {:status 200})


(defn ui-rpc [context {{cid :cid} :route-params}]
  (pg/execute! context {:sql ["delete from notebook_cells where id = ?" cid]})
  {:status 200})

(defn mount-routes [context]

  (http/register-endpoint context {:method :get :path  "/ui/notebooks"        :fn #'index-html})
  (http/register-endpoint context {:method :get :path  "/ui/notebooks/:id"    :fn #'show-html})
  (http/register-endpoint context {:method :get :path  "/ui/notebooks/new"    :fn #'new-html})
  (http/register-endpoint context {:method :post :path "/ui/notebooks/create" :fn #'create-html})
  ;; this is called from js
  (http/register-endpoint context {:method :post :path  "/ui/notebooks/update-cell-order"  :fn #'update-cell-order})

  )

(comment
  (def context mysystem/context)

  (mount-routes context)


 )
