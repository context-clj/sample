(ns mysystem.ui
  (:require
   [system]
   [pg]
   [pg.repo]
   [http]
   [mysystem.ui.helpers :as h]
   [cheshire.core]
   [clojure.string :as str]))

(system/defmanifest {:description "UI"})

(defn index-html [context {{q :search} :query-params :as request}]
  (h/layout context request {:content [:div.p-4 "Hello"]}))

(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path "/" :fn #'index-html})
  :ok)

(system/defstart [context _]
  (mount-routes context)
  {})


(comment
  (def context mysystem/context)
  (mount-routes context)

  (pg/execute! context {:sql "select url, version from codesystem limit 100"})
  (pg/execute! context {:sql "select count(*) from codesystem"})
  (pg/execute! context {:sql "select * from concept where hierarchy is not null limit 10"})




  )
