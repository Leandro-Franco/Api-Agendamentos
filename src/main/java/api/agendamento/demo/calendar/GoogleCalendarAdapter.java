package api.agendamento.demo.calendar;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import api.agendamento.demo.model.Agendamento;

@Component
@ConditionalOnProperty(name = "agendamentos.calendar.habilitado", havingValue = "true")
@EnableConfigurationProperties(CalendarProperties.class)
public class GoogleCalendarAdapter implements CalendarPort {

    private final Calendar calendar;
    private final CalendarProperties propriedades;
    private final ZoneId fuso;

    public GoogleCalendarAdapter(CalendarProperties propriedades)
            throws GeneralSecurityException, IOException {

        this.propriedades = propriedades;
        this.fuso = ZoneId.of(propriedades.fuso());

        GoogleCredentials credenciais;
        try (FileInputStream arquivo = new FileInputStream(propriedades.credenciais())) {
            credenciais = GoogleCredentials.fromStream(arquivo)
                    .createScoped(List.of(CalendarScopes.CALENDAR));
        }

        this.calendar = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credenciais))
                .setApplicationName("api-agendamentos")
                .build();
    }

    @Override
    public String criar(Agendamento agendamento) {
        try {
            Event criado = calendar.events()
                    .insert(propriedades.id(), paraEvento(agendamento))
                    .execute();
            return criado.getId();
        } catch (IOException e) {
            throw new CalendarIndisponivelException(
                    "Falha ao criar evento para o agendamento " + agendamento.getIdAgendamento(), e);
        }
    }

    @Override
    public void atualizar(String eventId, Agendamento agendamento) {
        try {
            calendar.events()
                    .update(propriedades.id(), eventId, paraEvento(agendamento))
                    .execute();
        } catch (IOException e) {
            throw new CalendarIndisponivelException("Falha ao atualizar o evento " + eventId, e);
        }
    }

    @Override
    public void remover(String eventId) {
        try {
            calendar.events().delete(propriedades.id(), eventId).execute();
        } catch (GoogleJsonResponseException e) {
            // O evento ja nao existe. O objetivo era que ele sumisse, e ele sumiu:
            // tratar como erro faria a reconciliacao insistir para sempre.
            if (e.getStatusCode() == 404 || e.getStatusCode() == 410) {
                return;
            }
            throw new CalendarIndisponivelException("Falha ao remover o evento " + eventId, e);
        } catch (IOException e) {
            throw new CalendarIndisponivelException("Falha ao remover o evento " + eventId, e);
        }
    }

    private Event paraEvento(Agendamento agendamento) {
        return new Event()
                .setSummary(agendamento.getTitulo())
                .setDescription(agendamento.getDescricao())
                .setStart(momento(agendamento.getDataInicio()))
                .setEnd(momento(agendamento.getDataFim()));
    }

    // Aqui o LocalDateTime ganha fuso, uma unica vez e num lugar so. Foi a ausencia
    // desta conversao explicita que fez o evento nascer uma hora adiantado quando o
    // N8N era quem falava com o Google.
    private EventDateTime momento(LocalDateTime quando) {
        return new EventDateTime()
                .setDateTime(new DateTime(Date.from(quando.atZone(fuso).toInstant())))
                .setTimeZone(fuso.getId());
    }
}
