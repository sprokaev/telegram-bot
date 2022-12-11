package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");

    @Autowired
    private TelegramBot telegramBot;

    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Long chatId = update.message().chat().id();
            String messageText = update.message().text();

            if (messageText.equals("/start")) {
                SendMessage sendMessage = new SendMessage(chatId, "Привет!");
                SendResponse response = telegramBot.execute(sendMessage);
            }

            Matcher matcher = pattern.matcher(messageText);
            if (matcher.matches()) {
                SendMessage sendMessage = new SendMessage(chatId, messageText + " - данное напоминание сохранено!");
                SendResponse response = telegramBot.execute(sendMessage);

                String date = matcher.group(1);
                String message = matcher.group(3);

                System.out.println("date = " + date);
                System.out.println("message = " + message);

                LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                NotificationTask notificationTask = new NotificationTask();
                notificationTask.setChatId(chatId);
                notificationTask.setDate(dateTime);
                notificationTask.setMessage(message);
                notificationTaskRepository.save(notificationTask);

                System.out.println("dateTime = " + dateTime);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        List<NotificationTask> notificationTasks = notificationTaskRepository.findByDate(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        for (NotificationTask task : notificationTasks) {
            SendMessage sendMessage = new SendMessage(task.getChatId(), task + " - пора выполнять!!!");
            SendResponse response = telegramBot.execute(sendMessage);
        }
    }
}
