(ns wordless.graph)

(defn links-for-map
  "returns edges for graph"
  [m]
  (first (for [[k v] m]
           (if (map? v)
             (links-for-map v)
             (for [i v] [k i])))))

(defn size->edge-set [size]
  (map #(vector %1 %2)
       (repeat 0)
       (range 1 size)))

(defn nodes-and-links [syn-map]
  (let [nodes (-> syn-map
                  seq
                  flatten
                  distinct)
        edges (-> nodes
                  count
                  size->edge-set)]
    {:nodes (mapv #(assoc {} :label % :weight 0) nodes)
     :links (mapv (fn [[k v]] {:source k :target v}) edges)}))

