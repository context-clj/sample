(ns mysystem
  (:require [system]
            [http]
            [pg]
            [pg.repo]))

(system/defmanifest
  {:description "my system"
   :deps ["pg" "pg.repo" "http"]})

(defn get-index [context req]
  {:status 200 :body "Hello"})

(defn get-patient [context req]
  {:status 200 :body (pg.repo/select context {:table "patient"})})

(defn migrate [context]
  (system/info context ::migrate "create tables etc..")
  (pg.repo/register-repo
   context {:table "patient"
            :primary-key [:id]
            :columns {:id {:type "text"}
                      :resource {:type "jsonb"}}})
  (pg.repo/upsert context {:table "patient" :resource {:id "pt-1", :name [{:family "John"}]}}))

(system/defstart
  [context config]

  (http/register-endpoint context {:method :get :path  "/" :fn  #'get-index})
  (http/register-endpoint context {:method :get :path "/Patient" :fn #'get-patient})

  (migrate context)
  {})


(comment
  (def context (system/start-system {:services ["http" "http.openapi" "pg" "pg.repo" "mysystem"]
                                     :http {:port 8884}
                                     :pg {:host "localhost" :port 5400 :user "admin" :database "mysystem" :password "admin"}}))


  (pg/execute! context {:sql "drop table if exists patient"})

  (system/stop-system context)

  (migrate context)
  (get-index context {})

  (http/request context {:path "/"})
  (http/request context {:path "/api"})
  (http/request context {:path "/Patient"})

  (str #'pg/manifest)

  )
