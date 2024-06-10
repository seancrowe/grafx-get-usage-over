(ns script
  (:require [babashka.http-client :as http])
  (:require [cheshire.core :as json]))

; Change with your token
(def auth "Bearer TRuvLAMXNtlK1pHCgXsWjPBqsdcDyRxbQ")

(def subscriptions-json-string (http/get "https://api.chiligrafx.com/api/v1/subscriptions" {:headers {"Authorization" auth}}))

(def subscriptions (json/parse-string (:body subscriptions-json-string)))

(def active-subs (filter #(and (get % "isActive") (not (= (get % "subscriptionType") "OnPremises"))) subscriptions))

(defn get-sub-info [sub]
  (let [sub-id (get sub "guid")]
    (json/parse-string (:body (http/get
                               (str
                                "https://api.chiligrafx.com/api/v1/subscriptions/"
                                sub-id)
                               {:headers {"Authorization" auth}})))))

(defn client-over [sub-info]
  (let
   [client-name (get sub-info "clientName")
    max-storage (get sub-info "maxStorage")
    storage-used (get sub-info "storageUsed")]
    (if (or (nil? max-storage) (nil? storage-used))
      {:client client-name :error "storage null"}
      (if (> storage-used max-storage)
        {:client client-name :storage storage-used :over (- storage-used max-storage)}
        nil))))

(def clients-over-including-errors
  (doall
   (filter #(or (contains? % :error) (contains? % :storage))
           (map (fn [sub]
                  (let [sub-id (get sub "guid")
                        client-name (get sub "clientName")]
                    (assoc
                     (do
                       (println (str "Checking... " client-name))
                       (Thread/sleep 300)
                       (client-over (get-sub-info sub-id)))
                     :sub-id sub-id)))
                active-subs))))

(def clients-with-error (filter
                         #(contains? % :error)
                         clients-over-including-errors))

(def clients-over (filter
                   #(not (contains? % :error))
                   clients-over-including-errors))

(spit "./clients-over.edn" (prn-str  clients-over))
(spit "./clients-over-error.edn" (prn-str  clients-with-error))

; (def test (reduce
;            (fn [acc sub]
;              (println (:index acc))
;              (println sub)
;              (let [client (client-over (get-sub-info sub))
;                    clients (:clients acc)]
;                (update (assoc acc :clients (conj clients client)) :index inc)))
;            {:index 0 :clients []} (take 10 active-subs)))
