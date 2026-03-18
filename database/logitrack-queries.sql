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
    COALESCE(wo.name, 'EXTERNAL') AS origin_warehouse,
    COALESCE(wd.name, 'EXTERNAL') AS destination_warehouse,
    CONCAT(ru.first_name, ' ', ru.last_name) AS registered_by_name,
    CONCAT(eu.first_name, ' ', eu.last_name) AS performed_by_employee_name,
    m.created_at
FROM movement AS m
INNER JOIN product AS p ON p.id = m.product_id
INNER JOIN app_user AS ru ON ru.id = m.registered_by_user_id
INNER JOIN app_user AS eu ON eu.id = m.performed_by_employee_id
LEFT JOIN warehouse AS wo ON wo.id = m.origin_warehouse_id
LEFT JOIN warehouse AS wd ON wd.id = m.destination_warehouse_id
ORDER BY m.created_at, m.id;

-- 3. Current location of each active asset
SELECT
    p.name AS product_name,
    p.category,
    COALESCE(w.name, 'EXTERNAL') AS current_warehouse
FROM product AS p
LEFT JOIN warehouse AS w ON w.id = p.warehouse_id
ORDER BY current_warehouse, p.name;

-- 4. Movement summary by executing employee
SELECT
    CONCAT(u.first_name, ' ', u.last_name) AS employee_name,
    COUNT(m.id) AS total_movements
FROM movement AS m
INNER JOIN app_user AS u ON u.id = m.performed_by_employee_id
GROUP BY u.id, u.first_name, u.last_name
ORDER BY total_movements DESC;

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
