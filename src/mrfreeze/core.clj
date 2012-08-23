(ns mrfreeze.core
  (:import com.amazonaws.services.glacier.AmazonGlacierClient
           (com.amazonaws.auth AWSCredentials PropertiesCredentials)
           (com.amazonaws.services.glacier.transfer ArchiveTransferManager UploadResult)
           (com.amazonaws.services.glacier.model DescribeVaultOutput DescribeVaultRequest DescribeVaultResult
                                                 ListVaultsRequest ListVaultsResult JobParameters InitiateJobRequest)))


(defn aws-credentials [access-key secret-key]
  "Return AWS credentials"
  (com.amazonaws.auth.BasicAWSCredentials. access-key secret-key))

(defn glacier-client [credentials]
  "Create a glacier client"
  (let [client (AmazonGlacierClient. credentials)]
    ;; Set it's endpoint to https://glacier.us-east-1.amazonaws.com/
    ;; should change this to be configurable
    (.setEndpoint client "https://glacier.us-east-1.amazonaws.com/")
    client))
  
(defn upload [vault filename credentials]
  "Upload a file to a glacier vault"
    ;; Create an ArchiveTransferManager
  (let [client (glacier-client credentials)
        atm (ArchiveTransferManager. client credentials)
        ;; upload the file
        result (.upload atm vault filename (clojure.java.io/file filename))]
      ;; print the archive id
      (println "Archive ID: " (.getArchiveId result)))))

(defn describe-vault [vault credentials]
  "Print information about what is in a vault"
  (let [client (glacier-client credentials)
        result (.describeVault client (DescribeVaultRequest. vault))]
    (println "Vault:" vault)
    (println "Vault ARN:" (.getVaultARN result))
    (println "Creation Date:" (.getCreationDate result))
    (println "Last Inventory Date:" (.getLastInventoryDate result))
    (println "Number of Archives:" (.getNumberOfArchives result))
    (println "Size in Bytes:" (.getSizeInBytes result))))
    
(defn inventory-vault [vault credentials]
  "Requests an inventory of a vault"
  (let [client (glacier-client credentials)
        parameters (JobParameters.)
        request (InitiateJobRequest.)]
    
    ;; Set the parameters for the job
    (.withType parameters "inventory-retrieval")
    ;; Create the job request
    (.withVaultName request vault)
    (.withJobParameters request parameters)

    (let [result (.initiateJob client request)]
      (println "Inventory job ID:" (.getJobId result)))))
                   
(defn main [& args]
  (println "hello world"))
                                   
