(ns mysystem
  (:require [system]
            [mysystem.ui]
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
  (pg/migrate-prepare context)
  (pg/migrate-up context))

(defn patient-json [context request]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (pg.repo/select context {:table "patient"})})

(system/defstart
  [context config]
  (migrate context)

  (http/register-endpoint context {:method :get :path "/patient" :fn #'patient-json})
  {})


(comment
  (def context (system/start-system {:services ["http" "http.openapi" "pg" "pg.repo" "mysystem" "mysystem.ui"]
                                     :http {:port 8884}
                                     :pg {:host "localhost" :port 5400 :user "admin" :database "mysystem" :password "admin"}}))

  (system/stop-system context)

  (migrate context)
  (pg.repo/clear-table-definitions-cache context)

  (pg/migrate-down context "notebooks")

  ;; (pg/generate-migration "init")
  ;; (pg/generate-migration "notebooks")

  (pg/execute! context {:sql "select * from patient"})

  (pg/execute! context {:sql "drop table if exists patient"})

  (system/stop-system context)

  (migrate context)
  (get-index context {})

  (http/request context {:path "/"})
  (http/request context {:path "/api"})
  (http/request context {:path "/Patient"})

  (str #'pg/manifest)

  )
