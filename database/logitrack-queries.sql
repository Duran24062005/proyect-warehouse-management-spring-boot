USE logiTrack;

-- 1. Products with current warehouse and manager
SELECT
    p.id,
    p.name AS product_name,
    p.category,
    p.price,
    w.name AS warehouse_name,
    CONCAT(u.first_name, ' ', u.last_name) AS manager_name
FROM product AS p
LEFT JOIN warehouse AS w ON w.id = p.warehouse_id
LEFT JOIN app_user AS u ON u.id = w.manager_user_id
ORDER BY p.id;

-- 2. Full movement history
SELECT
    m.id,
    m.movement_type,
    p.name AS product_name,
    m.quantity,
    COALESCE(wo.name, 'EXTERNAL') AS origin_warehouse,
    COALESCE(wd.name, 'EXTERNAL') AS destination_warehouse,
    CONCAT(u.first_name, ' ', u.last_name) AS employee_name,
    m.created_at
FROM movement AS m
INNER JOIN product AS p ON p.id = m.product_id
INNER JOIN app_user AS u ON u.id = m.employee_user_id
LEFT JOIN warehouse AS wo ON wo.id = m.origin_warehouse_id
LEFT JOIN warehouse AS wd ON wd.id = m.destination_warehouse_id
ORDER BY m.created_at, m.id;

-- 3. Current stock by warehouse and product based on movements
SELECT
    w.name AS warehouse_name,
    p.name AS product_name,
    SUM(stock_delta.quantity_delta) AS current_stock
FROM (
    SELECT
        destination_warehouse_id AS warehouse_id,
        product_id,
        CAST(quantity AS SIGNED) AS quantity_delta
    FROM movement
    WHERE destination_warehouse_id IS NOT NULL

    UNION ALL

    SELECT
        origin_warehouse_id AS warehouse_id,
        product_id,
        CAST(quantity AS SIGNED) * -1 AS quantity_delta
    FROM movement
    WHERE origin_warehouse_id IS NOT NULL
) AS stock_delta
INNER JOIN warehouse AS w ON w.id = stock_delta.warehouse_id
INNER JOIN product AS p ON p.id = stock_delta.product_id
GROUP BY w.id, w.name, p.id, p.name
HAVING SUM(stock_delta.quantity_delta) > 0
ORDER BY w.name, p.name;

-- 4. Movement summary by employee
SELECT
    CONCAT(u.first_name, ' ', u.last_name) AS employee_name,
    COUNT(m.id) AS total_movements,
    SUM(m.quantity) AS total_units_moved
FROM movement AS m
INNER JOIN app_user AS u ON u.id = m.employee_user_id
GROUP BY u.id, u.first_name, u.last_name
ORDER BY total_units_moved DESC, total_movements DESC;

-- 5. Audit log with actor and entity
SELECT
    a.id,
    a.operation_type,
    e.name AS affected_entity,
    u.email AS actor_email,
    a.old_values,
    a.new_values,
    a.created_at
FROM audit_change AS a
LEFT JOIN entity AS e ON e.id = a.affected_entity_id
LEFT JOIN app_user AS u ON u.id = a.actor_user_id
ORDER BY a.created_at, a.id;
