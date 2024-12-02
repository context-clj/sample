(ns mysystem
  (:require [system] [http] [pg]))

(system/defmanifest
  {:description "my system"})

(defn get-index [context req]
  {:status 200 :body "Hello"})

(defn get-patient [context req]
  {:status 200 :body (pg/execute! context ["select * from patients"])})

(defn migrate [context]
  (system/info context ::migrate "create tables etc..")
  (pg/execute! context ["create table if not exists patients (id text primary key, resource jsonb)"])
  (pg/execute! context ["truncate patients"])
  (pg/execute! context ["insert into patients (id,resource) values ('pt-1', '{}')"])
  )

(system/defstart
  [context config]

  (http/register-endpoint context {:method :get :path  "/" :fn  #'get-index})
  (http/register-endpoint context {:method :get :path  "/Patient" :fn  #'get-patient})

  (migrate context)
  {})


(comment
  (def context (system/start-system {:services ["http" "http.openapi" "pg" "mysystem"]
                                     :http {:port 8883}
                                     :pg {:host "localhost" :port 5400 :user "admin" :database "mysystem" :password "admin"}}))
  (system/stop-system context)

  (migrate context)
  (get-index context {})

  (http/request context {:path "/"})
  (http/request context {:path "/api"})
  (http/request context {:path "/Patient"})




  )
