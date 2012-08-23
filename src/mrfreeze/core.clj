(ns mrfreeze.core
  (:import com.amazonaws.services.glacier.AmazonGlacierClient
           (com.amazonaws.auth AWSCredentials PropertiesCredentials)
           (com.amazonaws.services.glacier.transfer ArchiveTransferManager UploadResult)))


(defn upload [vault filename access-key secret-key]
  "Upload a file to a glacier vault"
  ;; Create a client
  (let [credentials (com.amazonaws.auth.BasicAWSCredentials. access-key secret-key)
        client (AmazonGlacierClient. credentials)]
    ;; Set it's endpoint to https://glacier.us-east-1.amazonaws.com/
    (.setEndpoint client "https://glacier.us-east-1.amazonaws.com/")
    ;; Create an ArchiveTransferManager
    (let [atm (ArchiveTransferManager. client credentials)
          ;; upload the file
          result (.upload atm vault filename (clojure.java.io/file filename))]
      ;; print the archive id
      (println "Archive ID: " (.getArchiveId result)))))

(defn main [& args]
  (println "hello world"))
                                   
