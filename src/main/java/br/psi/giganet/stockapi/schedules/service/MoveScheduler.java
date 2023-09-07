package br.psi.giganet.stockapi.schedules.service;

import br.psi.giganet.stockapi.common.messages.service.LogMessageService;
import br.psi.giganet.stockapi.config.project_property.ApplicationProperties;
import br.psi.giganet.stockapi.schedules.model.ScheduledStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class MoveScheduler {

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private LogMessageService logMessageService;

    @Autowired
    private ScheduledMoveService scheduledMoveService;

    @Scheduled(fixedDelay = 1000 * 60 * 2, initialDelay = 1000 * 30)
    @Transactional
    public void execute() {
        if (properties.getEnableMovesScheduler()) {
            final long interval = 1;
            final long errorMargin = 20;

            scheduledMoveService.findAllAutomaticAndScheduledBetween(
                    ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(errorMargin + interval), ZonedDateTime.now(ZoneId.of("UTC")))
                    .stream()
                    .filter(scheduled -> scheduled.getStatus().equals(ScheduledStatus.SCHEDULED))
                    .forEach(scheduled -> {
                        try {
                            scheduledMoveService.execute(scheduled);

                        } catch (Exception e) {
                            scheduled.setDescription("Horário: " +
                                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) +
                                    "    Motivo: " + e.getLocalizedMessage());
                            scheduledMoveService.updateOnError(scheduled);

                            Map<String, Object> errors = new HashMap<>();
                            errors.put("error", "Erro durante execução dos agendamentos");
                            errors.put("description", e.getLocalizedMessage());
                            try {
                                logMessageService.send(new ObjectMapper().writeValueAsString(errors));
                            } catch (JsonProcessingException ignored) {
                            }

                        }
                    });
        }
    }

}
