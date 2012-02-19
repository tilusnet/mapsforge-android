 SELECT w.id, wt.v AS name, wt3.v AS zip, coordinates.waynodes
   FROM current_ways w
   JOIN current_way_tags wt ON w.id = wt.id
   JOIN current_way_tags wt2 ON w.id = wt2.id
   LEFT JOIN current_way_tags wt3 ON w.id = wt3.id AND wt3.k::text = 'postal_code'::text
   JOIN ( SELECT w2_1.id, array_to_string(array_agg((round(n2_1.latitude::numeric / 10.0) || ','::text) || round(n2_1.longitude::numeric / 10.0)), ','::text) AS waynodes
   FROM current_ways w2_1
   JOIN current_way_nodes wn2_1 ON w2_1.id = wn2_1.id
   JOIN current_nodes n2_1 ON wn2_1.node_id = n2_1.id
  GROUP BY w2_1.id) coordinates ON coordinates.id = w.id
  WHERE wt.k::text = 'name'::text AND wt2.k::text = 'highway'::text
  ORDER BY wt.v, w.id;
