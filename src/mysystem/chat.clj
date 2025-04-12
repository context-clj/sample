(ns mysystem.chat
  (:require
   [system]
   [mysystem.ui.helpers :as h]
   [pg]
   [pg.repo]
   [http]
   [org.httpkit.server :as server]
   [clojure.string :as str]))


(defn keywordize [m]
  (cond
    (map? m) (reduce-kv (fn [acc k v] (assoc acc (if (string? k) (keyword k) k) (keywordize v))) {} m)
    (sequential? m) (mapv keywordize m)
    :else m))

(defn get-form [request]
  (when-let [b (:body request)]
    (keywordize (http/form-decode (slurp b)))))


(defn ^{:http {:path "/ui/chat/login" :method :get}}
  login [context request]
  (h/hiccup-response
   request
   [:div {}
    [:form {:class "max-w-2xl mx-auto mt-10 border border-gray-300 rounded-xl p-10"
            :action "/ui/chat/login"
            :method :post}
     [:h1 {:class "text-2xl font-bold my-4"} "Login"]
     [:input {:type "text" :name "username" :placeholder "username" :required true :class "w-full px-2 py-1 rounded-md border border-gray-300"}]
     [:div {:class "mt-2"}
      [:button {:type "submit" :class "w-full px-2 py-1 rounded-md border border-gray-300 bg-blue-500 text-white"} "Login"]]]]))

(defn ^{:http {:path "/ui/chat/login" :method :post}}
  do-login [context request]
  {:status 301 
   :headers {"Location" "/ui/chat" "cache-control" "no-cache, no-store"} 
   :cookies {"username" (:username (get-form request)) :max-age 60 :path "/"}})

(defn ^{:http {:path "/ui/chat/logout" :method :post}}
  do-logout [context request]
  {:status 301
   :headers {"Location" "/ui/chat/login" "cache-control" "no-cache, no-store"}
   :cookies {"username" nil :max-age 0 :path "/"}})

(defn channel-send [channel event html]
  (server/send!
   channel
   {:status 200 
    :headers {"content-type" "text/event-stream" "cache-control" "no-cache, no-store"} 
    :body   (str "event: " event "\n" "data: " (h/html html) "\n\n")} false))

(defn broadcast [context event data]
  (doseq [channel (system/get-system-state context [:connected-clients])]
    (try
      (channel-send channel event data)
      (catch Exception e
        (println "Error sending message to channel" channel "Error:" e)
        (system/update-system-state context [:connected-clients] (fn [x] (disj (or x #{}) channel)))))))

(defn ^{:http {:path "/ui/chat/printing/subscribe" :method :get}}
  subscribe
  [context request]
  (let [username (get-in request [:cookies "username" :value])]
    (server/with-channel request channel
      (server/on-close channel (fn [status]
                                 (broadcast context "printing" [:span {:hx-ext "remove-in"} (str username " is disconnected")])
                                 (system/update-system-state context [:connected-clients] (fn [x] (disj (or x #{}) channel)))))
      (system/update-system-state context [:connected-clients] (fn [x] (conj (or x #{}) channel)))
      (broadcast context "printing" [:span {:hx-ext "remove-in"} (str username " is connected")])
      {:status 200 :headers {"Content-Type" "text/event-stream"}})))


(defn ^{:http {:path "/ui/chat/printing"}}
  chat-printing [context request]
  (let [author (get-in request [:cookies "username" :value]) ]
    (broadcast context "printing" [:span {:hx-ext "remove-in"} (str author " is printing...")]))
  {:status 200})

(defn format-time [instant]
  (when instant
    (subs (second (str/split (str instant) #"( |\.)")) 0 5)))

(defn render-messages [context user & [idx no-oob]]
  (let [idx (or idx 0)
        messages (pg.repo/select context {:table "messages" :where [:> :id [:pg/param idx]] :order-by :id})
        next-id (or (:id (last messages)) 0)]
    (println :messages idx :next-id next-id)
    (if (= idx next-id)
      nil
      (list
       [:input {:id "next-id" :hx-swap-oob (when-not no-oob "true") :type "hidden" :name "seq" :value next-id}]
       [:div {:hx-swap-oob (when-not no-oob "beforeend:#messages")}
        (for [message messages]
          [:div {:class ["flex" (if (= (:author message) user) "justify-end" "justify-start")]}
           [:div {:class ["mt-2 shadow-md rounded-md px-2 py-1 rounded-md max-w-2xl w-fit"
                          (if (= (:author message) user) "bg-blue-50" "bg-gray-50")]}
            [:div {:class "text-xs font-semibold text-zinc-500 py-0.5"} (:author message)]
            [:div {:class "text-sm"} (:text message)]
            [:div {:class "text-gray-400 text-right text-[0.6rem]"} (format-time (:created_at message))]
            ]])]))))



(defn ^{:http {:path "/ui/chat/new-messages" :method :get}}
  new-messages [context request]
  (let [from-id (Integer/parseInt (:seq (:query-params request)))
        user (get-in request [:cookies "username" :value])]
    (h/html-response (render-messages context user from-id))))

(defn ^{:http {:path "/ui/chat" :method :post}}
  create-message [context request]
  (let [message (get-form request)
        user (get-in request [:cookies "username" :value]) ]
    (println "message" message)
    (pg.repo/insert context {:table "messages" :resource (assoc message :author user)})
    (broadcast context "new-message" [:span])
    {:status 200}))


(defn ^{:http {:path "/ui/chat" :method :get}}
  index-html [context request]
  (if-let [user (get-in request [:cookies "username" :value])]
    (h/hiccup-response
     request
     [:div {:hx-ext "sse"
            :sse-connect "/ui/chat/printing/subscribe"
            :class "h-full flex flex-col bg-gray-200"
            :style "position: absolute; top: 0px; bottom: 0px; left:0; right:0; overflow-y: hidden;"}
      [:div {:sse-swap "new-message"
             :id "null" :style "display:none"
             :hx-get "/ui/chat/new-messages"
             :hx-include "#next-id, #author"
             :hx-ext "scroll-to-bottom"
             :scroll-to-bottom "#messages"
             :hx-trigger "sse:new-message"}]
      [:div  {:class "px-4 py-1.5 border-b border-gray-400 shadow-md bg-gray-300 font-bold text-sm flex items-center space-x-2" :style "height: 40px"}
       [:span {:class"flex-1"} (str "htmx.clj chat for " user)]
       [:form {:action "/ui/chat/logout" :method :post}
        [:button {:type "submit" :class "px-2 py-0.5 text-xs rounded-md border border-gray-300 bg-blue-500 text-white"} "Logout"]]]

      [:div#messages {:class "p-4 flex-1" :style "position: absolute; top: 40px; bottom: 160px; left:0; right:0; overflow-y: auto;"} 
       (render-messages context user 0 true)]

      [:div {:style "position: absolute; bottom: 0; height: 150px; left:0; right:0"
             :class "px-4 py-2 bg-gray-100"}
       [:div {:sse-swap "printing"
              :class "text-center text-zinc-500 text-xs"
              :hx-swap "innerHTML"
              :style "position: absolute; top: -20px; width: 100%;"}]
       [:form  {:hx-post "/ui/chat"
                :hx-target "#null"
                :hx-trigger "keyup[key=='Enter' && (metaKey||ctrlKey)] from:#message"
                :hx-push-url "false"
                :hx-include "#next-id"
                (keyword "hx-on::after-request") "document.getElementById('message').value = ''"}
        [:textarea {:id "message"
                    :class "w-full px-2 py-1 rounded-md border border-gray-300"
                    :style "height: 130px;"
                    :hx-ext "publish-event"
                    :publish-event "printing"
                    :placeholder "Type your message here... and <ctrl>+<enter> to send"
                    :name "text"}]]]])
    {:status 302 :headers {"Location" "/ui/chat/login"}}))

(defn mount-routes [context]
  (http/register-ns-endpoints context 'mysystem.chat))

#_(mount-routes context)

(comment
  (def context mysystem/context)

  (mount-routes context)
  (pg/generate-migration "chat")
  (pg/migrate-up context)

  )

 
