package br.psi.giganet.stockapi.dashboard.repository.factory;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.stock_moves.model.ExternalOrderType;

import java.time.LocalDate;

public class DashboardSqlFactory {

    public static String findAllPurchasedVersusReceivedProducts(LocalDate initialDate, LocalDate finalDate) {
        String template = "SELECT " +
                " purchase.productName AS \"product\"," +
                " purchase.purchased AS \"purchased\", " +
                " COALESCE(entry.received, 0) AS \"received\"" +
                " FROM  (" +
                "   SELECT " +
                "   prod.id AS productId," +
                "   prod.name AS productName," +
                "   SUM(item.quantity) AS \"purchased\"" +
                "   FROM " +
                "   purchase_order_items item" +
                "    INNER JOIN products prod ON prod.id = item.product" +
                "    INNER JOIN purchase_orders o ON o.id = item.purchase_order" +
                "    INNER JOIN purchase_order_freights f ON  o.id = f.purchaseorder" +
                "   WHERE " +
                "    (DATE(f.deliverydate) >= ':initialDate' AND DATE(f.deliverydate) <= ':finalDate') AND " +
                "    item.status IN ('PENDING', 'PARTIALLY_RECEIVED', 'RECEIVED')" +
                "   GROUP BY prod.id " +
                "   ORDER BY SUM(item.quantity) DESC" +
                " ) purchase" +
                " LEFT JOIN (" +
                "  SELECT " +
                "  prod.id AS productId," +
                "  prod.name AS productName," +
                "  SUM(item.quantity) AS \"received\"" +
                "  FROM " +
                "  entry_items item" +
                "   INNER JOIN products prod ON prod.id = item.product" +
                "  WHERE " +
                "   (DATE(item.createddate) >= ':initialDate' AND DATE(item.createddate) <= ':finalDate') AND " +
                "   item.status = 'RECEIVED'" +
                "  GROUP BY prod.id " +
                "  ORDER BY SUM(item.quantity) DESC" +
                " ) entry ON entry.productId = purchase.productId" +
                " ORDER BY purchase.purchased DESC";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString());
    }

    public static String findAllUsedItemsByOrderType(
            ExternalOrderType orderType,
            BranchOffice branchOffice,
            LocalDate initialDate,
            LocalDate finalDate) {
        final String template = "SELECT  " +
                "  prod.name AS \"item\", " +
                "  SUM(m.quantity) AS \"quantity\" " +
                "FROM technician_stock_moves techMove " +
                "    INNER JOIN stock_moves m ON m.id = techMove.id " +
                "    INNER JOIN products prod ON prod.id = m.product " +
                "    INNER JOIN stock_items item ON m.from = item.id " +
                "    INNER JOIN stocks stock ON item.stock = stock.id " +
                "WHERE " +
                "   ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "   techMove.ordertype = ':orderType' AND  " +
                "   m.branchoffice = :branchOffice AND " +
                "   stock.type = 'TECHNICIAN' " +
                "GROUP BY prod.name " +
                "ORDER BY SUM(m.quantity) DESC ";

        return template
                .replaceAll(":orderType", orderType.toString())
                .replaceAll(":branchOffice", branchOffice.getId().toString())
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString());
    }

    public static String totalsByTechnicianStocks(BranchOffice branchOffice) {
        final String template = "SELECT  " +
                " tec.name, " +
                " SUM(item.quantity * item.lastpriceperunit) AS \"total\" " +
                " FROM stock_items item " +
                "  INNER JOIN stocks stock ON stock.id = item.stock " +
                "  INNER JOIN technicians tec ON tec.id = stock.technician " +
                " WHERE stock.type = 'TECHNICIAN' AND stock.branchoffice = :branchOffice " +
                " GROUP BY tec.id " +
                " ORDER BY SUM(item.quantity * item.lastpriceperunit) DESC";
        return template
                .replaceAll(":branchOffice", branchOffice.getId().toString());
    }

    public static String findAllMainItems(Employee employee, BranchOffice branchOffice) {
        String template = "WITH custom_user_groups AS ( " +
                "  SELECT DISTINCT " +
                "  ug.group_id AS \"id\" " +
                "  FROM main_dashboard_items_group_has_users ug " +
                "  INNER JOIN main_dashboard_item_groups gr ON gr.id = ug.group_id " +
                "  WHERE gr.category = 'CUSTOM' AND gr.branchoffice = :branchOffice AND ug.user_id = :employee " +
                ")  " +
                " " +
                "SELECT  " +
                "  prod.name AS \"product\", " +
                "  COALESCE(shed.quantity, 0) AS \"quantity\", " +
                "  COALESCE(shed.level, 'UNDEFINED') AS \"currentLevel\", " +
                "  unit.abbreviation AS \"unit\"  " +
                "FROM main_dashboard_items mi  " +
                "  INNER JOIN main_dashboard_item_groups mig ON mi.group = mig.id " +
                "  LEFT JOIN main_dashboard_items_group_has_users group_users ON group_users.group_id = mig.id " +
                "  INNER JOIN products prod ON prod.id = mi.product  " +
                "  INNER JOIN units unit ON unit.id = prod.unit " +
                "  LEFT JOIN (  " +
                "  SELECT   " +
                "  item.product  AS \"product\",  " +
                "  item.quantity AS  \"quantity\",  " +
                "  item.currentlevel AS  \"level\" " +
                "  FROM stock_items item   " +
                "  INNER JOIN stocks stock ON item.stock = stock.id  " +
                "  WHERE stock.type = 'SHED' AND stock.branchoffice = :branchOffice " +
                "  ) shed ON shed.product = mi.product  " +
                "  WHERE mig.branchoffice = :branchOffice AND " +
                "       (group_users.user_id = :employee OR  " +
                "           (mig.category = 'DEFAULT' AND (SELECT COUNT(id) FROM custom_user_groups) = 0)) " +
                "  ORDER BY mi.index ASC";

        return template
                .replaceAll(":employee", employee.getId().toString())
                .replaceAll(":branchOffice", branchOffice.getId().toString());
    }

    public static String findAllMainItemsWithQuantityInShedAndTechnicianAndMaintenance(Employee employee, BranchOffice branchOffice) {
        String template = "WITH custom_user_groups AS ( " +
                " SELECT DISTINCT " +
                "   ug.group_id AS \"id\" " +
                "  FROM main_dashboard_items_group_has_users ug " +
                "   INNER JOIN main_dashboard_item_groups gr ON gr.id = ug.group_id " +
                "  WHERE gr.category = 'CUSTOM' AND gr.branchoffice = :branchOffice AND ug.user_id = :employee " +
                ")  " +
                " " +
                "SELECT  " +
                " prod.name AS \"product\",   " +
                " COALESCE(shed.quantity, 0) AS \"shed\",   " +
                " COALESCE(maintenance.quantity, 0) AS \"maintenance\",   " +
                " COALESCE(technician.quantity, 0) AS \"technician\",   " +
                " (COALESCE(shed.quantity, 0) + COALESCE(maintenance.quantity, 0) + COALESCE(technician.quantity, 0)) AS \"total\" " +
                "FROM main_dashboard_items mi  " +
                " INNER JOIN main_dashboard_item_groups mig ON mi.group = mig.id " +
                " LEFT JOIN main_dashboard_items_group_has_users group_users ON group_users.group_id = mig.id " +
                " INNER JOIN products prod ON prod.id = mi.product  " +
                " LEFT JOIN (    " +
                "  SELECT      " +
                "   item.product  AS \"product\",     " +
                "   item.quantity AS  \"quantity\"    " +
                "  FROM stock_items item      " +
                "   INNER JOIN stocks stock ON item.stock = stock.id    " +
                "  WHERE stock.type = 'SHED' AND stock.branchoffice = :branchOffice  " +
                " ) shed ON shed.product = mi.product      " +
                " LEFT JOIN (    " +
                "  SELECT      " +
                "   item.product  AS \"product\",     " +
                "   item.quantity AS  \"quantity\"    " +
                "  FROM stock_items item      " +
                "   INNER JOIN stocks stock ON item.stock = stock.id    " +
                "  WHERE stock.type = 'MAINTENANCE' AND stock.branchoffice = :branchOffice   " +
                " ) maintenance ON maintenance.product = mi.product      " +
                " LEFT JOIN (    " +
                "  SELECT      " +
                "   item.product  AS \"product\",     " +
                "   SUM(item.quantity) AS  \"quantity\"    " +
                "  FROM stock_items item      " +
                "   INNER JOIN stocks stock ON item.stock = stock.id    " +
                "  WHERE stock.type = 'TECHNICIAN'  AND stock.branchoffice = :branchOffice   " +
                "  GROUP BY item.product   " +
                " ) technician ON technician.product = mi.product  " +
                " WHERE mig.branchoffice = :branchOffice AND " +
                "       (group_users.user_id = :employee OR  " +
                "           (mig.category = 'DEFAULT' AND (SELECT COUNT(id) FROM custom_user_groups) = 0)) " +
                " ORDER BY mi.index ASC";

        return template
                .replaceAll(":employee", employee.getId().toString())
                .replaceAll(":branchOffice", branchOffice.getId().toString());
    }

    public static String findAllEntryItemsWithQuantityByDate(LocalDate initialDate, LocalDate finalDate, BranchOffice branchOffice) {
        final String template = "SELECT  " +
                " prod.name AS \"product\", " +
                " SUM(entry.quantity) AS \"quantity\" " +
                "FROM entry_items_from_order_stock_moves e " +
                " INNER JOIN stock_moves entry ON e.id = entry.id " +
                " INNER JOIN stock_items item ON entry.to = item.id " +
                " INNER JOIN stocks stock ON stock.id = item.stock " +
                " INNER JOIN products prod ON prod.id = entry.product " +
                "WHERE ( DATE(entry.createddate) >= ':initialDate' AND DATE(entry.createddate) <= ':finalDate') " +
                " AND stock.branchoffice = :branchOffice " +
                "GROUP BY prod.name";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":branchOffice", branchOffice.getId().toString());
    }

    public static String findAllObsoleteItems(BranchOffice branchOffice) {
        final String template = "SELECT  " +
                " prod.name  AS \"product\", " +
                " item.quantity AS \"quantity\" " +
                "FROM stock_items item " +
                " INNER JOIN stocks s ON s.id = item.stock " +
                " INNER JOIN products prod ON prod.id = item.product " +
                "WHERE s.type = 'OBSOLETE' AND item.quantity > 0  AND s.branchoffice = ':branchOffice' " +
                "ORDER BY item.quantity DESC";

        return template
                .replaceAll(":branchOffice", branchOffice.getId().toString());
    }

    public static String findAllStockItemsInShedTechnicianMaintenanceObsoleteDefective(BranchOffice branchOffice) {
        final String template = "SELECT  " +
                " prod.name AS \"product\", " +
                " SUM(item.quantity) AS \"quantity\" " +
                "FROM stock_items item " +
                " INNER JOIN stocks s ON s.id = item.stock " +
                " INNER JOIN products prod ON prod.id = item.product " +
                "WHERE s.type IN ('SHED', 'TECHNICIAN', 'MAINTENANCE', 'DEFECTIVE', 'OBSOLETE')  AND s.branchoffice = :branchOffice " +
                "GROUP BY prod.name HAVING SUM(item.quantity) > 0 " +
                "ORDER BY prod.name ASC";

        return template
                .replaceAll(":branchOffice", branchOffice.getId().toString());
    }

}
