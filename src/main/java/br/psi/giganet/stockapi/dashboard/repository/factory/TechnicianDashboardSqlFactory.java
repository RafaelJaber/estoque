package br.psi.giganet.stockapi.dashboard.repository.factory;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;

import java.time.LocalDate;

public class TechnicianDashboardSqlFactory {

    public static String getAllStockItemsWithMoveCounts(String technicianId, LocalDate initialDate, LocalDate finalDate, BranchOffice branchOffice) {
        String template = "SELECT  " +
                "   prod.name, " +
                "   item.quantity AS quantity, " +
                "   COALESCE(replacement.quantity, 0 ) AS \"replacement\", " +
                "   COALESCE(retreat.quantity, 0 ) AS \"retreat\", " +
                "   COALESCE(devolution.quantity, 0 ) AS \"devolution\", " +
                "   COALESCE(installation.quantity, 0) AS \"installation\" " +
                " FROM products prod " +
                "  INNER JOIN stock_items item ON item.product = prod.id " +
                "  INNER JOIN stocks stock ON item.stock = stock.id " +

                "  LEFT JOIN " +
                "  ( " +
                "   SELECT  " +
                "   move.product, " +
                "   SUM(move.quantity) AS \"quantity\" " +
                "   FROM  " +
                "   stock_moves move " +
                "    LEFT JOIN stock_items stockItemTo ON move.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON move.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    (DATE(move.lastmodifieddate) >= ':initialDate' AND DATE(move.lastmodifieddate) <= ':finalDate') AND " +
                "     move.status IN ('REALIZED') AND  " +
                "     move.branchoffice = :branchOffice AND  " +
                "    (move.type = 'ENTRY_ITEM' OR (" +
                "           move.type = 'BETWEEN_STOCKS' AND " +
                "           stockFrom.type IN ('SHED', 'MAINTENANCE', 'OBSOLETE', 'DEFECTIVE', 'TECHNICIAN'))" +
                "     ) AND  " +
                "    stockTo.type = 'TECHNICIAN' AND stockTo.technician = ':technician' " +
                "   GROUP BY move.product  HAVING SUM(move.quantity) > 0 " +
                "  ) replacement ON replacement.product = prod.id " +

                "  LEFT JOIN " +
                "  ( " +
                "   SELECT  " +
                "   move.product, " +
                "   SUM(move.quantity) AS \"quantity\" " +
                "   FROM  " +
                "   stock_moves move " +
                "    LEFT JOIN stock_items stockItemTo ON move.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON move.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    (DATE(move.lastmodifieddate) >= ':initialDate' AND DATE(move.lastmodifieddate) <= ':finalDate') AND " +
                "     move.status IN ('REALIZED') AND  " +
                "     move.branchoffice = :branchOffice AND  " +
                "     stockFrom.type = 'CUSTOMER' AND  " +
                "     stockTo.type = 'TECHNICIAN' AND stockTo.technician = ':technician' " +
                "   GROUP BY move.product  HAVING SUM(move.quantity) > 0 " +
                "  ) retreat ON retreat.product = prod.id " +

                "  LEFT JOIN " +
                "  ( " +
                "   SELECT  " +
                "   move.product, " +
                "   SUM(move.quantity) AS \"quantity\" " +
                "   FROM  " +
                "   stock_moves move " +
                "    LEFT JOIN stock_items stockItemTo ON move.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON move.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    (DATE(move.lastmodifieddate) >= ':initialDate' AND DATE(move.lastmodifieddate) <= ':finalDate') AND " +
                "     move.status IN ('REALIZED') AND  " +
                "     move.branchoffice = :branchOffice AND  " +
                "    (move.type = 'OUT_ITEM' OR (" +
                "           move.type = 'BETWEEN_STOCKS' AND " +
                "           stockTo.type IN ('SHED', 'MAINTENANCE', 'OBSOLETE', 'DEFECTIVE', 'TECHNICIAN'))" +
                "     ) AND  " +
                "    stockFrom.type = 'TECHNICIAN' AND stockFrom.technician = ':technician' " +
                "   GROUP BY move.product  HAVING SUM(move.quantity) > 0 " +
                "  ) devolution ON devolution.product = prod.id " +

                "  LEFT JOIN " +
                "  ( " +
                "   SELECT  " +
                "   move.product, " +
                "   SUM(move.quantity) AS \"quantity\" " +
                "   FROM  " +
                "   stock_moves move " +
                "    LEFT JOIN stock_items stockItemTo ON move.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON move.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    (DATE(move.lastmodifieddate) >= ':initialDate' AND DATE(move.lastmodifieddate) <= ':finalDate') AND " +
                "     move.status IN ('REALIZED') AND  " +
                "     move.branchoffice = :branchOffice AND  " +
                "     stockTo.type = 'CUSTOMER' AND  " +
                "    stockFrom.type = 'TECHNICIAN' AND stockFrom.technician = ':technician' " +
                "   GROUP BY move.product  HAVING SUM(move.quantity) > 0 " +
                "  ) installation ON installation.product = prod.id " +

                " WHERE " +
                "   stock.type = 'TECHNICIAN' AND stock.technician = ':technician' AND stock.branchoffice = :branchOffice " +
                " ORDER BY prod.name";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":technician", technicianId)
                .replaceAll(":branchOffice", branchOffice.getId().toString());
    }

    public static String getAllStockItemsWithMoveCounts(LocalDate initialDate, LocalDate finalDate, BranchOffice branchOffice) {
        String template = "SELECT  " +
                "   prod.name, " +
                "   COALESCE(stockItem.quantity,0) AS quantity, " +
                "   COALESCE(replacement.quantity, 0 ) AS \"replacement\", " +
                "   COALESCE(retreat.quantity, 0 ) AS \"retreat\", " +
                "   COALESCE(devolution.quantity, 0 ) AS \"devolution\", " +
                "   COALESCE(installation.quantity, 0) AS \"installation\" " +
                " FROM products prod " +
                "  INNER JOIN (" +
                "       SELECT " +
                "           SUM(item.quantity) AS \"quantity\"," +
                "           item.product AS \"product\"" +
                "       FROM  stock_items item " +
                "           INNER JOIN stocks stock ON item.stock = stock.id " +
                "       WHERE stock.type = 'TECHNICIAN' AND stock.branchoffice = :branchOffice " +
                "           GROUP BY item.product HAVING SUM(item.quantity) > 0" +
                "   ) stockItem ON stockItem.product = prod.id " +

                "  LEFT JOIN " +
                "  ( " +
                "   SELECT  " +
                "   move.product, " +
                "   SUM(move.quantity) AS \"quantity\" " +
                "   FROM  " +
                "   stock_moves move " +
                "    LEFT JOIN stock_items stockItemTo ON move.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON move.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    (DATE(move.lastmodifieddate) >= ':initialDate' AND DATE(move.lastmodifieddate) <= ':finalDate') AND " +
                "     move.status IN ('REALIZED') AND  " +
                "     move.branchoffice = :branchOffice AND  " +
                "    (move.type = 'ENTRY_ITEM' OR (" +
                "           move.type = 'BETWEEN_STOCKS' AND " +
                "           stockFrom.type IN ('SHED', 'MAINTENANCE', 'OBSOLETE', 'DEFECTIVE'))" +
                "     ) AND  " +
                "    stockTo.type = 'TECHNICIAN' " +
                "   GROUP BY move.product  HAVING SUM(move.quantity) > 0 " +
                "  ) replacement ON replacement.product = prod.id " +

                "  LEFT JOIN " +
                "  ( " +
                "   SELECT  " +
                "   move.product, " +
                "   SUM(move.quantity) AS \"quantity\" " +
                "   FROM  " +
                "   stock_moves move " +
                "    LEFT JOIN stock_items stockItemTo ON move.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON move.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    (DATE(move.lastmodifieddate) >= ':initialDate' AND DATE(move.lastmodifieddate) <= ':finalDate') AND " +
                "     move.status IN ('REALIZED') AND  " +
                "     move.branchoffice = :branchOffice AND  " +
                "     stockFrom.type = 'CUSTOMER' AND  " +
                "     stockTo.type = 'TECHNICIAN' " +
                "   GROUP BY move.product  HAVING SUM(move.quantity) > 0 " +
                "  ) retreat ON retreat.product = prod.id " +

                "  LEFT JOIN " +
                "  ( " +
                "   SELECT  " +
                "   move.product, " +
                "   SUM(move.quantity) AS \"quantity\" " +
                "   FROM  " +
                "   stock_moves move " +
                "    LEFT JOIN stock_items stockItemTo ON move.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON move.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    (DATE(move.lastmodifieddate) >= ':initialDate' AND DATE(move.lastmodifieddate) <= ':finalDate') AND " +
                "     move.status IN ('REALIZED') AND  " +
                "     move.branchoffice = :branchOffice AND  " +
                "    (move.type = 'OUT_ITEM' OR (" +
                "           move.type = 'BETWEEN_STOCKS' AND " +
                "           stockTo.type IN ('SHED', 'MAINTENANCE', 'OBSOLETE', 'DEFECTIVE'))" +
                "     ) AND  " +
                "    stockFrom.type = 'TECHNICIAN' " +
                "   GROUP BY move.product HAVING SUM(move.quantity) > 0 " +
                "  ) devolution ON devolution.product = prod.id " +

                "  LEFT JOIN " +
                "  ( " +
                "   SELECT  " +
                "   move.product, " +
                "   SUM(move.quantity) AS \"quantity\" " +
                "   FROM  " +
                "   stock_moves move " +
                "    LEFT JOIN stock_items stockItemTo ON move.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON move.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    (DATE(move.lastmodifieddate) >= ':initialDate' AND DATE(move.lastmodifieddate) <= ':finalDate') AND " +
                "     move.status IN ('REALIZED') AND  " +
                "     move.branchoffice = :branchOffice AND  " +
                "     stockTo.type = 'CUSTOMER' AND  " +
                "     stockFrom.type = 'TECHNICIAN' " +
                "   GROUP BY move.product HAVING SUM(move.quantity) > 0 " +
                "  ) installation ON installation.product = prod.id " +

                " ORDER BY prod.name";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":branchOffice", branchOffice.getId().toString());
    }

    public static String countOutgoingMovesPerTypeGroupByDays(LocalDate initialDate, LocalDate finalDate) {
        final String template = "SELECT " +
                " DATE(days.day) as \"date\", " +
                " COALESCE(det.total, 0) AS \"detached\"," +
                " COALESCE(ins.total, 0) AS \"installation\"," +
                " COALESCE(rep.total, 0) AS \"repair\"," +
                " COALESCE(can.total, 0) AS \"cancellation\"," +
                " COALESCE(add.total, 0) AS \"address_change\"," +
                " COALESCE(sec.total, 0) AS \"second_point\"" +
                " FROM generate_series(':initialDate'\\:\\:timestamp,':finalDate', '1 day') AS days(\"day\") " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    LEFT JOIN stock_items stockItemTo ON m.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "    (m.type = 'OUT_ITEM' OR (" +
                "           m.type = 'BETWEEN_STOCKS' AND " +
                "           stockTo.type IN ('SHED', 'MAINTENANCE', 'OBSOLETE', 'DEFECTIVE'))" +
                "     ) AND  " +
                "    stockFrom.type = 'TECHNICIAN' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS det ON DATE(days.day) = (det.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "     techMove.orderType = 'INSTALLATION' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS ins ON DATE(days.day) = (ins.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "     techMove.orderType = 'REPAIR' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS rep ON DATE(days.day) = (rep.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "     techMove.orderType = 'CANCELLATION' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS can ON DATE(days.day) = (can.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "     techMove.orderType = 'ADDRESS_CHANGE' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS add ON DATE(days.day) = (add.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "     techMove.orderType = 'SECOND_POINT' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS sec ON DATE(days.day) = (sec.day) ";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString());
    }

    public static String countOutgoingMovesPerTypeGroupByDays(String technicianId, LocalDate initialDate, LocalDate finalDate) {
        final String template = "SELECT " +
                " DATE(days.day) as \"date\", " +
                " COALESCE(det.total, 0) AS \"detached\"," +
                " COALESCE(ins.total, 0) AS \"installation\"," +
                " COALESCE(rep.total, 0) AS \"repair\"," +
                " COALESCE(can.total, 0) AS \"cancellation\"," +
                " COALESCE(add.total, 0) AS \"address_change\"," +
                " COALESCE(sec.total, 0) AS \"second_point\"" +
                " FROM generate_series(':initialDate'\\:\\:timestamp,':finalDate', '1 day') AS days(\"day\") " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    LEFT JOIN stock_items stockItemTo ON m.to = stockItemTo.id " +
                "    LEFT JOIN stocks stockTo ON stockItemTo.stock = stockTo.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "    (m.type = 'OUT_ITEM' OR (" +
                "           m.type = 'BETWEEN_STOCKS' AND " +
                "           stockTo.type IN ('SHED', 'MAINTENANCE', 'OBSOLETE', 'DEFECTIVE'))" +
                "     ) AND  " +
                "    stockFrom.type = 'TECHNICIAN' AND " +
                "    stockFrom.technician = ':technician'  " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS det ON DATE(days.day) = (det.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "    stockFrom.technician = ':technician' AND  " +
                "     techMove.orderType = 'INSTALLATION' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS ins ON DATE(days.day) = (ins.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "    stockFrom.technician = ':technician' AND  " +
                "     techMove.orderType = 'REPAIR' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS rep ON DATE(days.day) = (rep.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "     stockFrom.technician = ':technician' AND  " +
                "     techMove.orderType = 'CANCELLATION' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS can ON DATE(days.day) = (can.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "     stockFrom.technician = ':technician' AND  " +
                "     techMove.orderType = 'ADDRESS_CHANGE' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS add ON DATE(days.day) = (add.day) " +


                "  LEFT JOIN (   " +
                "   SELECT " +
                "    DATE(m.createddate) AS \"day\", " +
                "    COUNT(m.id) AS \"total\"    " +
                "   FROM stock_moves m    " +
                "    INNER JOIN technician_stock_moves techMove ON techMove.id = m.id " +
                "    LEFT JOIN stock_items stockItemFrom ON m.from = stockItemFrom.id " +
                "    LEFT JOIN stocks stockFrom ON stockItemFrom.stock = stockFrom.id " +
                "   WHERE " +
                "    ( DATE(m.createddate) >= ':initialDate' AND DATE(m.createddate) <= ':finalDate') AND    " +
                "     m.status IN ('REALIZED') AND  " +
                "     stockFrom.type = 'TECHNICIAN' AND  " +
                "     stockFrom.technician = ':technician' AND  " +
                "     techMove.orderType = 'SECOND_POINT' " +
                "   GROUP BY DATE(m.createddate)" +
                "  ) AS sec ON DATE(days.day) = (sec.day) ";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":technician", technicianId);
    }

    public static String findAllPatrimoniesByTechnician(String technicianUserId) {
        final String template = "SELECT  " +
                "  pat.code, " +
                "  prod.name " +
                "  FROM patrimonies pat " +
                "    INNER JOIN products prod ON prod.id = pat.product " +
                "    INNER JOIN patrimony_locations loc ON loc.id = pat.currentlocation " +
                "  WHERE loc.code = ':technician' ";

        return template.replaceAll(":technician", technicianUserId);
    }

    public static String findAllPatrimoniesWithTechnicians() {
        final String template = "SELECT  " +
                "  pat.code, " +
                "  prod.name " +
                "  FROM patrimonies pat " +
                "    INNER JOIN products prod ON prod.id = pat.product " +
                "    INNER JOIN patrimony_locations loc ON loc.id = pat.currentlocation " +
                "  WHERE loc.type = 'TECHNICIAN' ";

        return template;
    }

}
