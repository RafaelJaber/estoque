package br.psi.giganet.stockapi.stock_moves.model;

public enum MoveStatus {

    REQUESTED,
    APPROVED,
    REJECTED,
    CANCELED,
    REALIZED,
    FAILED;

    public static String getLabel(MoveStatus status) {
        switch (status.name()) {
        case "APPROVED": return "Aprovado";
        case "PARTIALLY_APPROVED": return  "Parcialmente Aprovado";
        case "REALIZED": return  "Realizado";
        case "IN_TRANSIT": return "Em Trânsito";
        case "PENDING": return "Pendente";
        case "CANCELED": return  "Cancelado";
        case "PARTIALLY_REJECTED": return   "Parcialmente Rejeitado";
        case "REJECTED": return  "Rejeitado";
        case "PARTIALLY_RECEIVED": return  "Parcialmente Recebido";
        case "RECEIVED": return  "Recebido";
        case "FINALIZED": return  "Finalizado";
        case "SCHEDULED": return  "Agendado";
        case "IN_PROGRESS": return "Em Progresso";
        case "SUCCESS": return "Sucesso";
        case "FAILED": return "Falha";
        case "REQUESTED": return "Solicitado";
        default:
            return null;
        }
    }
}
